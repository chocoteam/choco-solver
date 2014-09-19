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

.. literalinclude:: /../../choco-samples/src/test/java/docs/Overview.java
   :language: java
   :lines: 44-54
   :emphasize-lines: 54
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
However, the best solution found so far is restored.

.. important::

 Because the best solution is restored, all variables are instantiated after a call to ``solver.findOptimalSolution(...)``.

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

The method ends by restoring the last solution found so far, if any.

Here is a simple illustration:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/Pareto.java
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

Choco |version| requires each solution to be fully instantiated, i.e. every variable must be fixed.
Otherwise, an exception will be thrown if assertions are turned on (when ``-ea`` is added to the JVM parameters).
Choco |version| includes several ways to record solutions.

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
This is used by default when seeking an optimal solution.


``AllSolutionsRecorder`` records all solutions that are found.
As this may result in a memory explosion, it is not used by default.


``BestSolutionsRecorder`` records all solutions but removes from the solution set each solution that is worse than the best solution value found so far.
This may be used to enumerate all optimal (or at least, best) solutions of a problem.

``ParetoSolutionsRecorder`` records all solutions of the pareto front of the multi-objective problem.

Custom recorder
^^^^^^^^^^^^^^^

You can build you own way of manipulating and recording solutions by either implementing your own ``ISolutionRecorder`` object
or by simply using an ``ISolutionMonitor``, as follows:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/SMPTSP.java
   :language: java
   :lines: 118-124
   :linenos:

Solution restoration
--------------------

A ``Solution`` object can be restored, i.e. variables are fixed back to their values in that solution.
For this purpose, we recommend to restore initial domains and then restore the solution,
with the following code: ::

    try{
       solver.getSearchLoop().restoreRootNode();
       solver.getEnvironment().worldPush();
       solution.restore();
    }catch (ContradictionException e){
       throw new UnsupportedOperationException("restoring the solution ended in a failure");
    }
    solver.getEngine().flush();

Note that if initial domains are not restored, then the solution restoration may lead to a failure.
This would happen when trying to restore out of the current domain.

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

Logging
=======

Choco |version| has a simple logger which can be used by calling ::

 SearchMonitorFactory.log(Solver solver, boolean solution, boolean choices);

The first argument is the solver.
The second indicates whether or not each solution (and associated resolution statistics) should be printed.
The third argument indicates whether or not each branching decision should be printed. This may be useful for debugging.

In general, in order to have a reasonable amount of information, we set the first boolean to true and the second to false.

If the two booleans are set to false, the trace would start with a welcome message:


.. code-block:: none

    ** Choco 3.2.0 (2014-05) : Constraint Programming Solver, Copyleft (c) 2010-2014
    ** Solve : myProblem

Then, when the resolution process ends, a complementary message is printed, based on the measures recorded.

.. code-block:: none

     - Search complete - [ No solution. ]
        Solutions: {0}
     [  Maximize = {1}  ]
     [  Minimize = {2}  ]
        Building time : {3}s
        Initialisation : {4}s
        Initial propagation : {5}s
        Resolution : {6}s
        Nodes: {7}
        Backtracks: {8}
        Fails: {9}
        Restarts: {10}
        Max depth: {11}
        Propagations: {12} + {13}
        Memory: {14}mb
        Variables: {15}
        Constraints: {16}

Brackets *[instruction]* indicate an optional instruction.
If no solution has been found, the message "No solution." appears on the first line.
``Maximize`` –resp. ``Minimize``– indicates the best known value before exiting of the objective value using a ``ResolutionPolicy.MAXIMIZE`` –resp. ResolutionPolicy.MINIMIZE- policy.

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
12. number of *fine* propagations
13. number of *coarse* propagations
14. estimation of the memory used
15. number of variables in the model
16. number of constraints in the model


If the resolution process reached a limit before ending *naturally*, the title of the message is set to :

.. code-block:: none

    - Incomplete search - Limit reached.

The body of the message remains the same.
The message is formated thanks to the ``IMeasureRecorder`` which is a :ref:`search monitor <44_monitors_label>`.

When the first boolean of ``SearchMonitorFactory.log(Solver, boolean, boolean);`` is set to true, on each solution the following message will be printed:

.. code-block:: none

    {0} Solutions, [Maximize = {1}][Minimize = {2}], Resolution {6}s, {7} Nodes, \\
                                        {8} Backtracks, {9} Fails, {10} Restarts

followed by one line exposing the value of each decision variables (those involved in the search strategy).

When the second boolean of ``SearchMonitorFactory.log(Solver, boolean, boolean);`` is set to true, on each node a message will be printed indicating which decision is applied.
The message is prefixed by as many "." as nodes in the current branch of the search tree.
A decision is prefixed with ``[R]`` and a refutation is prefixed by ``[L]``.

.. warning::

    Printing the choices slows down the search process.

