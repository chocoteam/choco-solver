Search Strategies
=================

Principle
---------

The search space induces by variables' domain is equal to  :math:`S=|d_1|*|d_2|*...*|d_n|` where :math:`d_i` is the domain of the :math:`i^{th}` variable.
Most of the time (not to say always), constraint propagation is not sufficient to build a solution, that is, to remove all values but one from (integer) variables' domain.
Thus, the search space needs to be explored using one or more *search strategies*.
A search strategy performs a performs a `Depth First Search <http://en.wikipedia.org/wiki/Depth-first_search>`_  and reduces the search space by making *decisions*.
A decision involves a variables, a value and an operator, for instance :math:`x = 5`.
Decisions are computed and applied until all the variables are instantiated, that is, a solution is found, or a failure has been detected.

 Choco|release| build a binary search tree: each decision can be refuted.
 When a decision has to be computed, the search strategy is called to provide one, for instance :math:`x = 5`.
 The decision is then applied, the variable, the domain of ``x`` is reduced to ``5``, and the decision is validated thanks to the propagation.
 If the application of the decision leads to a failure, the search backtracks and the decision is refuted (:math:`x \neq 5`) and validated through propagation.
 Otherwise, if there is no more free variables then a solution has been found, else a new decision is computed.

.. note::

    There are many ways to explore the search space and this steps should not be overlooked. Search strategies or heuristics have a strong impact on resolution performances.


Default search strategies
-------------------------

If no search strategy is specified in the model, Choco |version| will generate a default one. In many cases, this strategy will not be sufficient to produce satisfying performances and it will be necessary to specify a dedicated strategy, using ``solver.set(...)``.
The default search strategy distincts variables per types and defines a specific search strategy per each type:

#. integer variables (but boolean variables: ``IntStrategyFactory.minDom_LB(ivars)``
#. boolean variables: :code:`IntStrategyFactory.lexico_UB(bvars)`
#. set variables: :code:`SetStrategyFactory.force_minDelta_first(svars)`
#. graph variables :code:`GraphStrategyFactory.graphLexico(gvar)`
#. real variables :code:`new RealStrategy(rvars, new Cyclic(), new RealDomainMiddle())`

Constants are excluded from search strategies' variable scope.

``IntStrategyFactory``, ``SetStrategyFactory`` and ``GraphStrategyFactory`` offer several built-in search strategies and a simple framework to build custom searches.

A search strategy
-----------------

It is strongly recommended to adapt the search space exploration to the problem treated.
To do so, one can use built-in search strategies provided in ``IntSearchStrategy``, ``SetStrategyFactory`` and ``GraphStrategyFactory``.

It is also possible to create an assignment strategy (over integer variables) by using : ::

    IntSearchStrategy.custom(VAR_SELECTOR, VAL_SELECTOR, VARS)

or: ::

    IntSearchStrategy.custom(VAR_SELECTOR, VAL_SELECTOR, DEC_OPERATOR, VARS)

In the first case, the ``DEC_OPERATOR`` is set to ``IntSearchStrategy.assign()``.
Such methods required the declaraion of a :

#. ``VAR_SELECTOR``:  a variable selector, defines how to select the next variable to branch on,
#. ``VAL_SELECTOR``: a value selector, defines how to select a value in the domain of the selected variable,
#. ``DEC_OPERATOR``: a decision operator, defines how to modify the domain of the selected variable with the selected value,
#. ``VARS``: sets of variables to branch on.


Some ``VariableSelector``, ``IntValueSelector`` and ``DecisionOperator`` are provided in ``IntSearchStrategy``.

Finally, one can create its own strategy, see :ref:`Defining its own search <45_define_search_label>` for more details.

Black-box search strategies
^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are many ways of choosing a variable and computing a decision on it.
Designing specific search strategies can be a very tough task to do.
The concept of black-box search heuristic (or adaptive search strategy) has naturally emerged from this statement.
Most common black-box search strategies observe aspects of the CSP resolution in order to drive the variable selection, and eventually the decision computation (presumably, a value assignment).
Three main families of heuristic, stemming from the concepts of variable impact, conflict and variable activity, can be found in Choco|release|.


Restarts
--------

Restart means stopping the current tree search, then starting a new tree search from the root node.
Restarting makes sense only when coupled with randomized dynamic branching strategies ensuring that the same enumeration tree is not constructed twice.
The branching strategies based on the past experience of the search, such as adaptive search strategies, are more accurate in combination with a restart approach.

Unless the number of allowed restarts is limited, a tree search with restarts is not complete anymore. It is a good strategy, though, when optimizing an NP-hard problem in a limited time.



Some adaptive search strategies resolutions are improved by sometimes restarting the search exploration from the root node.
Thus, the statistics computed on the bottom of the tree search can be applied on the top of it.

There a two restart strategies available in ``SearchMonitorFactory``: ::

    geometrical(Solver solver, int base, double grow, ICounter counter, int limit)

It performs a search with restarts controlled by the resolution event [#f1]_ ``counter`` which counts events occurring during the search.
Parameter ``base`` indicates the maximal number of events allowed in the first search tree.
Once this limit is reached, a restart occurs and the search continues until ``base``*``grow`` events are done, and so on.
After each restart, the limit number of events is increased by the geometric factor ``grow``.
``limit`` states the maximum number of restarts.

and: ::

    luby(Solver solver, int base, int grow, ICounter counter, int limit)

The `Luby <http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.47.5558>`_ ’s restart policy is an alternative to the geometric restart policy.
It performs a search with restarts controlled by the number of resolution events [#f1]_ counted by ``counter``.
The maximum number of events allowed at a given restart iteration is given by base multiplied by the Las Vegas coefficient at this iteration.
The sequence of these coefficients is defined recursively on its prefix subsequences:
starting from the first prefix :math:`1`, the :math:`(k+1)^th` prefix is the :math:`k^th` prefix repeated ``grow`` times and
immediately followed by coefficient ``grow``:math:`^k`.

- the first coefficients for ``grow`` =2: [1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,1,...]
- the first coefficients for ``grow`` =3 : [1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,...]

.. _33_searches_limit_label:

Limiting the resolution
-----------------------

Built-in search limits
^^^^^^^^^^^^^^^^^^^^^^

The exploration of the search tree can be limited in various ways.
Some usual limits are provided in ``SearchMonitorFactory``, or ``SMF`` for short:

- ``limitTime`` stops the search when the given time limit has been reached. This is the most common limit, as many applications have a limited available runtime.

.. note::
    The potential search interruption occurs at the end of a propagation, i.e. it will not interrupt a propagation algorithm, so the overall runtime of the solver might exceed the time limit.

- ``limitSolution`` stops the search when the given solution limit has been reached.
- ``limitNode`` stops the search when the given search node limit has been reached.
- ``limitFail`` stops the search when the given fail limit has been reached.
- ``limitBacktrack`` stops the search when the given backtrack limit has been reached.

Custom search limits
^^^^^^^^^^^^^^^^^^^^

You can decide to interrupt the search process whenever you want with one of the following instructions: ::

 solver.getSearchLoop().reachLimit();
 solver.getSearchLoop().interrupt(String message);

Both options will interrupt the search process but only the first one will inform the solver that the search stops because of a limit. In other words, calling ::

 solver.hasReachedLimit()

will return false if the second option is used.


.. [#f1] Resolution events are: backtracks, fails, nodes, solutions, time or user-defined ones.

.. admonition:: Going further

    :ref:`Large Neighborhood Search <41_LNS_label>`, :ref:`Multi-threading <42_mutlithreads_label>`, :ref:`Explanations <43_explanations_label>`.
