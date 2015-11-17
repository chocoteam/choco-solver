****************
Solving problems
****************

.. _31_solving_label:

Finding solutions
=================

Choco |version| provides different API, offered by ``Solver``, to launch the problem resolution.
Before everything, there are two methods which help interpreting the results.

**Feasibility**:
 Once the resolution ends, a call to the ``solver.isFeasible()`` method will return a boolean which indicates whether or not the problem is feasible.

 - ``true``: at least one solution has been found, the problem is proven to be feasible,

 - ``false``: in principle, the problem has no solution. More precisely, if the search space is guaranteed to be explored entirely, it is proven that the problem has no solution.


**Limitation**:
  When the resolution is limited (See :ref:`Limiting the resolution <33_searches_limit_label>` for details and examples), one may guess if a limit has been reached.
  The ``solver.hasReachedLimit()`` method returns ``true`` if a limit has bypassed the search process, ``false`` if it has ended *naturally*.

.. _Limits: 31_solving.html#limiting-the-resolution

.. warning::

 In some cases, the search may not be complete.
 For instance, if one enables restart on each failure with a static search strategy,
 there is a possibility that the same sub-tree is explored permanently.
 In those cases, the search may never stop or the two above methods may not be sufficient to confirm the lack of solution.

Satisfaction problems
---------------------

Finding a solution
^^^^^^^^^^^^^^^^^^

A call to ``solver.findSolution()`` launches a resolution which stops on the first solution found, if any.

.. literalinclude:: /../../choco-samples/src/test/java/org/chocosolver/docs/Overview.java
   :language: java
   :lines: 47-59
   :linenos:

If a solution has been found, the resolution process stops on that solution,
thus each variable is instantiated to a value, and the method returns ``true``.

If the method returns ``false``, two cases must be considered:

- A limit has been reached.
  There may be a solution, but the solver has not been able to find it in the given limit
  or there is no solution but the solver has not been able to prove it (i.e., to close to search tree) in the given limit.
  The resolution process stops in no particular place in the search tree and the resolution can be run again.
- No limit has been declared. The problem has no solution, the complete exploration of the search tree proved it.

To ensure the problem has no solution, one may call ``solver.hasReachedLimit()``.
It returns ``true`` if a limit has been reached, ``false`` otherwise.



Enumerating solutions
^^^^^^^^^^^^^^^^^^^^^

Once the resolution has been started by a call to ``solver.findSolution()`` and if the problem is feasible,
the resolution can be resumed using ``solver.nextSolution()`` from the last solution found.
The method returns ``true`` if a new solution is found, ``false`` otherwise (a call to ``solver.hasReachedLimit()`` must confirm the lack of new solution).
If a solution has been found, alike ``solver.findSolution()``, the resolution stops on this solution,
each variable is instantiated, and the resolution can be resumed again until there is no more new solution.

One may enumerate all solution like this::

 if(solver.findSolution()){
    do{
        // do something, e.g. print out variables' value
    }while(solver.nextSolution());
 }

``solver.findSolution()`` and  ``solver.nextSolution()`` are the only ways to resume a resolution process which has already began.

.. tip::

    On a solution, one can get the value assigned to each variable by calling ::

        ivar.getValue(); // instantiation value of an IntVar, return a int
        svar.getValues(); // instantiation values of a SerVar, return a int[]
        rvar.getLB(); // lower bound of a RealVar, return a double
        rvar.getUB(); // upper bound of a RealVar, return a double



An alternative is to call ``solver.findAllSolutions()``. It attempts to find all solutions of the problem.
It returns the number of solutions found (in the given limit if any).


Optimization problems
---------------------

Choco |version| enables to solve optimization problems, that is, in which a variable must be optimized.

.. tip::

 For functions, one should declare an objective variable and declare it as the result of the function::

   // Function to maximize: 3X + 4Y
   IntVar OBJ = VF.bounded("objective", 0, 999, solver);
   solver.post(ICF.scalar(new IntVar[]{X,Y}, new int[]{3,4}, OBJ));
   solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, OBJ);


Finding one optimal solution
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Finding one optimal solution is made through a call to the ``solver.findOptimalSolution(ResolutionPolicy, IntVar)`` method.
The first argument defines the kind of optimization required: minimization (``ResolutionPolicy.MINIMIZE``)
or maximization (``ResolutionPolicy.MAXIMIZE``).
The second argument indicates the variable to optimize.

For instance::

 solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, OBJ);

states that the variable ``OBJ`` must be maximized.

The method does not return any value.
However, the best solution found so far can be restored (see below).

The best solution found is the optimal one if the entire search space has been explored.

The process is the following: anytime a solution is found, the value of the objective variable is stored and a *cut* is posted.
The cut is an additional constraint which states that the next solution must be strictly better than the current one,
ie in minimization, strictly smaller.

Finding all optimal solutions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There could be more than one optimal solutions.
To find them all, one can call ``findAllOptimalSolutions(ResolutionPolicy, IntVar, boolean)``.
The two first arguments defines the optimisation policy and the variable to optimize.
The last argument states the way the solutions are computed.
Set to ``true`` the resolution will be achieved in two steps: first finding and proving an optimal solution,
then enumerating all solutions of optimal cost.
Set to ``false``, the posted cuts are *soft*.
When an equivalent solution is found, it is stored and the resolution goes on.
When a strictly better solution is found, previous solutions are removed.
Setting the boolean to ``false`` allow finding non-optimal intermediary solutions, which may be time consuming.



Multi-objective optimization problems
-------------------------------------

Finding the pareto front
^^^^^^^^^^^^^^^^^^^^^^^^

It is possible to solve a multi-objective optimization problems with Choco |version|,
using ``solver.findParetoFront(ResolutionPolicy policy, IntVar... objectives)``.
The first argument define the resolution policy, which can be ``Resolution.MINIMIZE`` or ``ResolutionPolicy.MAXIMIZE``.
Then, the second argument defines the list of variables to optimize.

.. note::

 All variables should respect the same resolution policy.

The underlying approach is naive, but it simplifies the process.
Anytime a solution is found, a cut is posted which states that at least one of the objective variables must be better.
Such as :math:`(X_0 < b_0 \lor X_1 < b_1 \lor \ldots \lor X_n < b_n` where :math:`X_i` is the ith objective variable and :math:`b_i`
its best known value.

Here is a simple illustration:

.. literalinclude:: /../../choco-samples/src/main/java/samples/org/chocosolver/integer/Pareto.java
   :language: java
   :lines: 71,72,73,75,83,88-92
   :linenos:

Propagation
-----------

One may want to propagate each constraint manually.
This can be achieved by calling ``solver.propagate()``.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a ``ContradictionException`` if a contradiction occurs.
In that case, the propagation engine must be flushed calling ``solver.getEngine().flush()``
to ensure there is no pending events.

.. warning::

 If there are still pending events in the propagation engine, the propagation may results in unexpected results.

Recording solutions
===================

Choco |version| requires that each decision variable (that is, which is declared in the search strategy) is instantiated in a solution.
Otherwise, an exception will be thrown.
Non decision variables can be uninstantiated in a solution, however, if WARN logging is enable, a trace is shown to inform the user.
Choco |version| includes several ways to record solutions, the recommended way is to plug a `ISolutionMonitor` in.
See :ref:`44_monitors_label` for more details.

Solution storage
----------------

A solution is usually stored through a ``Solution`` object which maps every variable with its current value.
Such an object can be erased to store new solutions.

Solution recording
------------------

Built-in solution recorders
^^^^^^^^^^^^^^^^^^^^^^^^^^^

A solution recorder (``ISolutionRecorder``) is an object in charge of recording variable values in solutions.
There exists many built-in solution recorders:


``LastSolutionRecorder`` only keeps variable values of the last solution found. It is the default solution recorder.
Furthermore, it is possible to restore that solution after the search process ends.


``AllSolutionsRecorder`` records all solutions that are found.
As this may result in a memory explosion, it is not used by default.


``BestSolutionsRecorder`` records all solutions but removes from the solution set each solution that is worse than the best solution value found so far.
This may be used to enumerate all optimal (or at least, best) solutions of a problem.

``ParetoSolutionsRecorder`` records all solutions of the pareto front of the multi-objective problem.

Custom recorder
^^^^^^^^^^^^^^^

You can build you own way of manipulating and recording solutions by either implementing your own ``ISolutionRecorder`` object
or by simply using an ``ISolutionMonitor``, as follows:

.. literalinclude:: /../../choco-samples/src/main/java/samples/org/chocosolver/integer/SMPTSP.java
   :language: java
   :lines: 118-124
   :linenos:

Solution restoration
--------------------

A ``Solution`` object can be restored, i.e. variables are fixed back to their values in that solution.
This is achieved through the `Solver` by the calling one of the two following methods: ::

    solver.restoreLastSolution();
    // or
    Solution aSolution= ...;
    solver.restoreSolution(aSolution);

.. note::

    The restoration may detect inconsistency, for instance when the model has been externally modified since the solution to be restored to has been found.

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

 Choco |release| build a binary search tree: each decision can be refuted.
 When a decision has to be computed, the search strategy is called to provide one, for instance :math:`x = 5`.
 The decision is then applied, the variable, the domain of ``x`` is reduced to ``5``, and the decision is validated thanks to the propagation.
 If the application of the decision leads to a failure, the search backtracks and the decision is refuted (:math:`x \neq 5`) and validated through propagation.
 Otherwise, if there is no more free variables then a solution has been found, else a new decision is computed.

.. note::

    There are many ways to explore the search space and this steps should not be overlooked.
    Search strategies or heuristics have a strong impact on resolution performances.
    Thus, it is strongly recommended to adapt the search space exploration to the problem treated.


.. _31_zoom:

Zoom on IntStrategy
-------------------

A search strategy ``IntStrategy`` is dedicated to ``IntVar`` only.
It is based on a list of variables ``scope``, a selector of variable ``varSelector``, a value selector ``valSelector`` and an optional ``decOperator``.

#. ``scope``: array of variables to branch on.
#. ``varSelector``:  a variable selector, defines how to select the next variable to branch on.
#. ``valSelector``: a value selector, defines how to select a value in the domain of the selected variable.
#. ``decOperator``: a decision operator, defines how to modify the domain of the selected variable with the selected value.


On a call to ``IntStrategy.getDecision()``, ``varSelector`` try to find, among ``scope``, a variable not yet instantiated.
If such a variable does not exist, the method returns ``null``, saying that it can not compute decision anymore.
Otherwise, ``valSelector`` selects a value, within the domain of the selected variable.
A decision can then be computed with the selected variable and the selected value, and is returned to the caller.

By default, the decision built is an assignment: its application leads to an instantiation, its refutation, to a value removal.
It is possible create other types of decision by defining a decision operator ``DecisionOperator``.

**API**

    IntStrategyFactory.custom(VariableSelector<IntVar> VAR_SELECTOR, IntValueSelector VAL_SELECTOR,
                              DecisionOperator<IntVar> DEC_OPERATOR, IntVar... VARS)
    IntStrategyFactory.custom(VariableSelector<IntVar> VAR_SELECTOR, IntValueSelector VAL_SELECTOR,
                              IntVar... VARS)

    new IntStrategy(IntVar[] scope, VariableSelector<IntVar> varSelector, IntValueSelector valSelector)
    new IntStrategy(IntVar[] scope, VariableSelector<IntVar> varSelector, IntValueSelector valSelector,
    					   DecisionOperator<IntVar> decOperator)

Sometimes, on a call to the variable selector, several variables could be selected.
In that case, the order induced by ``VARS`` is used to break tie: the variable with the smallest index is selected.
However, it is possible to break tie with other ``VAR_SELECTOR``s.
They should be declared as parameters of ``VariablesSelectorWithTies``. ::

    solver.set(ISF.custom(
        new VariableSelectorWithTies(new FirstFail(), new Random(123L)),
        new IntDomainMin(), vars);

The variable with the smallest domain is selected first. If there are more than one variable whose
domain size is the smallest, ties are randomly broken.

.. note::
    Only variable selectors which implement ``VariableEvaluator`` can be used to break ties.


Very similar operations are achieved in ``SetStrategy`` and ``RealStrategy``.

See ``solver.search.strategy.IntStrategyFactory`` and ``solver.search.strategy.SetStrategyFactory`` for built-in strategies and selectors.

Available variable selectors
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    For integer variables
:ref:`51_svarsel_lex`,
:ref:`51_svarsel_rnd`,
:ref:`51_svarsel_mind`,
:ref:`51_svarsel_maxd`,
:ref:`51_svarsel_maxr`.

    For set variables

See ``solver.search.strategy.selectors.variables.MaxDelta``,
    ``solver.search.strategy.selectors.variables.MinDelta``.

    For real variables

See ``solver.search.strategy.selectors.variables.Cyclic``.


Available value selectors
^^^^^^^^^^^^^^^^^^^^^^^^^

    For integer variables

:ref:`51_svalsel_minv`,
:ref:`51_svalsel_midv`,
:ref:`51_svalsel_maxv`,
:ref:`51_svalsel_rndb`,
:ref:`51_svalsel_rndv`.

    For set variables

See ``solver.search.strategy.selectors.values.SetDomainMin``.

    For real variables

See ``solver.search.strategy.selectors.values.RealDomainMiddle``,
    ``solver.search.strategy.selectors.values.RealDomainMin``
    ``solver.search.strategy.selectors.values.RealDomainMax``.

Available decision operators
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

:ref:`51_sdecop_ass`,
:ref:`51_sdecop_rem`,
:ref:`51_sdecop_spl`,
:ref:`51_sdecop_rspl`.

Available strategies
^^^^^^^^^^^^^^^^^^^^

    For integer variables

:ref:`51_sstrat_cus`,
:ref:`51_sstrat_dic`,
:ref:`51_sstrat_once`,
:ref:`51_sstrat_seq`.

:ref:`51_sstrat_lexlb`,
:ref:`51_sstrat_lexnlb`,
:ref:`51_sstrat_lexspl`,
:ref:`51_sstrat_lexub`,
:ref:`51_sstrat_minlb`,
:ref:`51_sstrat_midlb`,
:ref:`51_sstrat_maxspl`,
:ref:`51_sstrat_minub`,
:ref:`51_sstrat_maxrlb`,
:ref:`51_sstrat_objbu`,
:ref:`51_sstrat_objdi`,
:ref:`51_sstrat_objtd`,
:ref:`51_sstrat_rndb`,
:ref:`51_sstrat_rndv`.

:ref:`51_sstrat_dwdeg`,
:ref:`51_sstrat_act`,
:ref:`51_sstrat_imp`.

:ref:`51_sstrat_lf`.

:ref:`51_sstrat_gat`.

    For set variables

:ref:`51_sstrat_cus`, :ref:`51_sstrat_seq`.

:ref:`51_sstrat_lexfi`,
:ref:`51_sstrat_maxdfi`,
:ref:`51_sstrat_mindfi`,
:ref:`51_sstrat_remfi`.

:ref:`51_sstrat_lf`.


.. important:: Black-box search strategies

    There are many ways of choosing a variable and computing a decision on it.
    Designing specific search strategies can be a very tough task to do.
    The concept of `Black-box search heuristic` (or adaptive search strategy) has naturally emerged from this statement.
    Most common black-box search strategies observe aspects of the CSP resolution in order to drive the variable selection, and eventually the decision computation (presumably, a value assignment).
    Three main families of heuristic, stemming from the concepts of variable impact, conflict and variable activity, can be found in Choco|release|.
    Black-box strategies can be augmented with restarts.



Default search strategies
-------------------------

If no search strategy is specified in the model, Choco |version| will rely on the default one (defined by a ``DefaultSearchBinder`` in ``Settings``).
In many cases, this strategy will not be sufficient to produce satisfying performances and it will be necessary to specify a dedicated strategy, using ``solver.set(...)``.
The default search strategy distinguishes variables per types and defines a specific search strategy per each type, sequentially applied:

#. integer variables and boolean variables : ``IntStrategyFactory.domOverWDeg(ivars, 0)``
#. set variables: :code:`SetStrategyFactory.force_minDelta_first(svars)`
#. real variables :code:`RealStrategyFactory.cyclic_middle(rvars)`
#. objective variable, if any: lower bound or upper bound, depending on the `ResolutionPolicy`

Note that `ISF.lastConflict(solver)` is also plugged-in.
Constants are excluded from search strategies' variable scope and the creation order is maintained per types.

``IntStrategyFactory``, ``SetStrategyFactory`` and ``RealStrategyFactory`` offer several built-in search strategies and a simple framework to build custom searches.


.. _31_searchbinder:

Search binder
^^^^^^^^^^^^^

It is possible to override the default search strategy by implementing an ``ISearchBinder``.
By default, a ``Solver`` is created with a ``DefaultSearchBinder`` declared in its settings.


An ``ISearchBinder`` has the following API:

``void configureSearch(Solver solver)``
    Configure the search strategy, and even more, of the given solver.
    The method is called from the search loop, after the initial propagation, if no search strategy is defined.
    Otherwise, it should be called before running the resolution.

The search binder to use must be declared in the ``Setting`` attached to a ``Solver`` (see :ref:`41_settings_label`).



Composition of strategies
-------------------------

Most of the time, it is necessary to combine various strategies.
A ``StrategiesSequencer`` enables to compose various ``AbstractStrategy``.
It is created on the basis of a list of ``AbstractStrategy``.
The current active strategy is called to compute a decision through its ``getDecision()`` method.
When no more decision can be computed for the current strategy, the following one becomes active.
The intersection of variables from each strategy does not have to be empty.
When a variable appears in various strategy, it is ignored as soon as it is instantiated.

When no environment is given in parameter,
the last active strategy is not stored, and strategies are evaluated in lexicographical order to find the first active one, based on its capacity to return a decision.

When an environment is given in parameter, the last active strategy is stored.

**API**

    IntStrategyFactory.sequencer(AbstractStrategy... strategies)

Note that a strategy sequencer is automatically generated when setting multiple strategies at the same time:

``solver.set(strategy1,strategy2);`` is equivalent to

``solver.set(ISF.sequencer(strategy1,strategy2));``

Finally, one can create its own strategy, see :ref:`Defining its own search <45_define_search_label>` for more details.


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

    :ref:`Large Neighborhood Search <41_LNS_label>`, :ref:`Explanations <43_explanations_label>`.

.. _34_chatternbox_label:

Resolution statistics
=====================

Resolution data are available thanks to the ``Chatterbox`` class, which outputs by default to ``System.out``.
It centralises widely used methods to have comprehensive feedback about the resolution process.
There are two types of methods: those who need to be called **before** the resolution, with a prefix `show`, and those who need to called **after** the resolution, with a prefix `print`.

For instance, one can indicate to print the solutions all resolution long: ::

    Chatterbox.showSolutions(solver);
    solver.findAllSolutions();

Or to print the search statistics once the search ends: ::

    solver.findSolution();
    Chatterbox.printStatistics(solver);


On a call to ``Chatterbox.printVersion()``, the following message will be printed:

.. code-block:: none

    ** Choco 3.3.2 (2015-11) : Constraint Programming Solver, Copyleft (c) 2010-2015

On a call to ``Chatterbox.printVersion()``, the following message will be printed:

.. code-block:: none

     - [ Search complete - [ No solution | {0} solution(s) found ]
       | Incomplete search - [ Limit reached | Unexpected interruption ] ].
        Solutions: {0}
     [  Maximize = {1}  ]
     [  Minimize = {2}  ]
        Building time : {3}s
        Resolution : {6}s
        Nodes: {7} ({7}/{6} n/s)
        Backtracks: {8}
        Fails: {9}
        Restarts: {10}
        Max depth: {11}
        Variables: {12}
        Constraints: {13}

Curly brackets *{instruction | }* indicate alternative instructions

Brackets *[instruction]* indicate an optional instruction.

If the search terminates, the message "Search complete" appears on the first line, followed with either the the number of solutions found or the message "No solution".
``Maximize`` –resp. ``Minimize``– indicates the best known value before exiting of the objective value using a ``ResolutionPolicy.MAXIMIZE`` –resp. ``ResolutionPolicy.MINIMIZE``- policy.

Curly braces *{value}* indicate search statistics:

0. number of solutions found
1. objective value in maximization
2. objective value in minimization
3. building time in second (from ``new Solver()`` to ``findSolution()`` or equivalent)
4. initialisation time in second (before initial propagation)
5. initial propagation time in second
6. resolution time in second (from ``new Solver()`` till now)
7. number of decision created, that is, nodes in the binary tree search
8. number of backtracks achieved
9. number of failures that occurred
10. number of restarts operated
11. maximum depth reached in the binary tree search
12. number of variables in the model
13. number of constraints in the model


If the resolution process reached a limit before ending *naturally*, the title of the message is set to :

.. code-block:: none

    - Incomplete search - Limit reached.

The body of the message remains the same.
The message is formatted thanks to the ``IMeasureRecorder`` which is a :ref:`search monitor <44_monitors_label>`.

On a call to ``Chatterbox.showSolutions(solver)``, on each solution the following message will be printed:

.. code-block:: none

    {0} Solutions, [Maximize = {1}][Minimize = {2}], Resolution {6}s, {7} Nodes, \\
                                        {8} Backtracks, {9} Fails, {10} Restarts

followed by one line exposing the value of each decision variables (those involved in the search strategy).

On a call to ``Chatterbox.showDecisions(solver)``, on each node of the search tree a message will be printed indicating which decision is applied.
The message is prefixed by as many "." as nodes in the current branch of the search tree.
A decision is prefixed with ``[R]`` and a refutation is prefixed by ``[L]``.

.. code-block:: none

    ..[L]x  ==  1 (0) //X = [0,5] Y = [0,6] ...

.. warning::

    ``Chatterbox.printDecisions(Solver solver)`` prints the tree search during the resolution.
    Printing the decisions slows down the search process.

