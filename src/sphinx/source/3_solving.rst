#######
Solving
#######

Up to here, we have seen how to model a problem with the ``Model`` object. To solve it, we need to use
the ``Solver`` object that is obtained from the model as follows: ::

    Solver solver = model.getSolver();

The ``Solver`` is in charge of alternating constraint-propagation with search, and possibly learning,
in order to compute solutions. This object may be configured in various ways.

********************
Core solving methods
********************

Solution computation
====================

Finding one solution
--------------------

A call to ``solver.solve()`` launches a resolution which stops on the first solution found, if any: ::

    if(solver.solve()){
        // do something, e.g. print out variable values
    }else if(solver.hasReachedLimit()){
        System.out.println("The could not find a solution
                            nor prove that none exists in the given limits");
    }else {
        System.out.println("The solver has proved the problem has no solution");
    }

If ``solver.solve()`` returns ``true``, then a solution has been found and each variable is instantiated to a value.
Otherwise, two cases must be considered:

- A limit has been declared and reached (``solver.hasReachedLimit()`` returns true).
  There may be a solution, but the solver has not been able to find it in the given limit
  or there is no solution but the solver has not been able to prove it (i.e., to close to search tree) in the given limit.
  The resolution process stops in no particular place in the search tree.
- No limit has been declared or reached: The problem has no solution and the solver has proved it.

Enumerating all solutions
-------------------------

You can enumerate all solutions of a problem with a simple while loop as follows: ::

    while(solver.solve()){
        // do something, e.g. print out variable values
    }

After the enumeration, the solver closes the search tree and variables are no longer instantiated to a value.

.. tip::

    On a solution, one can get the value assigned to each variable by calling ::

        ivar.getValue();    // instantiation value of an IntVar, return a int
        svar.getValue();    // instantiation values of a SerVar, return a int[]
        rvar.getLB();       // lower bound of a RealVar, return a double
        rvar.getUB();       // upper bound of a RealVar, return a double


Optimization
============

In Constraint-Programming, optimization is done by computing improving solutions, until reaching an optimum.
Therefore, it can be seen as solving multiple times the model while adding constraints on the fly to prevent the solver from computing dominated solutions.

Mono-objective optimization
---------------------------

The optimization process is the following: anytime a solution is found, the value of the objective variable is stored and a *cut* is posted.
The cut is an additional constraint which states that the next solution must be (strictly) better than the current one.
To solve an optimization problem, you must specify which variable to optimize and in which direction: ::

   // to maximize X
   model.setObjectives(ResolutionPolicy.MAXIMIZE, X);
   // or model.setObjectives(ResolutionPolicy.MINIMIZE, X); to minimize X
   while(solver.solve()){
       // an improving solution has been found
   }
   // the last solution found was optimal (if search completed)

You can use custom cuts by overriding the default cut behavior.
The *cut computer* function defines how the cut should bound the objective variable.
The input *number* is the best solution value found so far, the output *number* define the new bound.
When maximizing (resp. minimizing) a problem, the cut limits the lower bound (resp. upper bound) of the objective variable.
For instance, one may want to indicate that the value of the objective variable is the next solution should be
 greater than or equal to the best value + 10 ::

    ObjectiveManager<IntVar, Integer> oman = solver.getObjectiveManager();
    oman.setCutComputer(n -> n - 10);



.. tip::

    When the objective is a function over multiple variables, you need to model it through
    one objective variable and additional constraints: ::

        // Model objective function 3X + 4Y
        IntVar OBJ = model.intVar("objective", 0, 999);
        model.scalar(new IntVar[]{X,Y}, new int[]{3,4}, OBJ)).post();
        // Specify objective
        model.setObjectives(ResolutionPolicy.MAXIMIZE, OBJ);
        // Compute optimum
        model.getSolver().solve();

Multi-objective optimization
----------------------------

If you have multiple objective to optimize, you have several options. First, you may aggregate them in a function so that you end up with only one objective variable. Second, you can solve the problem multiple times, each one optimizing one variable and possibly fixing some bounds on the other. Third, you can enumerate solutions (without defining any objective) and add constraints on the fly to prevent search from finding dominated solutions. This is done by the `ParetoOptimizer` object which does the following:
Anytime a solution is found, a constraint is posted which states that at least one of the objective variables must be strictly better:
Such as :math:`(X_0 < b_0 \lor X_1 < b_1 \lor \ldots \lor X_n < b_n)` where :math:`X_i` is the ith objective variable and :math:`b_i` its value.

Here is a simple example on how to use the `ParetoOptimizer` to optimize two variables (a and b): ::

		// simple model
		Model model = new Model();
		IntVar a = model.intVar("a", 0, 2, false);
		IntVar b = model.intVar("b", 0, 2, false);
		IntVar c = model.intVar("c", 0, 2, false);
		model.arithm(a, "+", b, "=", c).post();

		// create an object that will store the best solutions and remove dominated ones
		ParetoOptimizer po = new ParetoOptimizer(ResolutionPolicy.MAXIMIZE,new IntVar[]{a,b});
		Solver solver = model.getSolver();
		solver.plugMonitor(po);

		// optimization
		while(solver.solve());

		// retrieve the pareto front
		List<Solution> paretoFront = po.getParetoFront();
		System.out.println("The pareto front has "+paretoFront.size()+" solutions : ");
		for(Solution s:paretoFront){
			System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
		}


.. note::

 All objectives must be optimized on the same direction (either minimization or maximization).


Constraint propagation
======================

One may want to propagate all constraints without search for a solution.
This can be achieved by calling ``solver.propagate()``.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a ``ContradictionException`` if a contradiction occurs.
In that case, the propagation engine must be flushed calling ``solver.getEngine().flush()``
to ensure there is no pending events.

.. warning::

 If there are still pending events in the propagation engine, the propagation may results in unexpected results.

Accessing variable values
=========================

The value of a variable can be accessed directly through the ``getValue()`` method only once the variable is instantiated, i.e. the value has been computed
(call ``isInstantiated()`` to check it). Otherwise, the lower bound is returned (and an exception is thrown if ``-ea`` is on).

For instance, the following code may throw an exception because the solution has not been computed yet: ::

    int v = variable.getValue();
    solver.solve();

The following code may throw an exception in case no solution could be found (unsat problem or time limit reached): ::

    solver.solve();
    int v = variable.getValue();

The correct approach is the following : ::

    if(solver.solve()){
        int v = variable.getValue();
    }

In optimization, you can print every solution as follows: ::

    while(solver.solve()){
        System.out.println(variable.getValue());
    }

The last print correspond to the best solution found.

However, the following code does *NOT* display the best solution found: ::

    while(solver.solve()){
        System.out.println(variable.getValue());
    }
    System.out.println("best solution found: "+variable.getValue());

Because it is outside the while loop, this code is reached once the search tree has been closed.
It does not correspond to a solution state and therefore variable is no longer instantiated at this stage.
To use solutions afterward, you need to record them using ``Solution`` objects.

Recording solutions
===================

A solution can be stored through a ``Solution`` object which maps every variable with its current value.
It can be created as follows: ::

        Solution solution = new Solution(model());

By default, a solution only records decision variables, that is, variables declared in the search heuristic.

Let ``X`` be the set of decision variables and ``Y`` another variable set that you need to store.
To record other variables (e.g. an objective variables) you have two options:

- Declare them in the search strategy using a complementary strategy ::

    solver.set(strategy(X),strategy(Y)).

- Specify which variables to store in the solution constructor ::

    Solution solution = new Solution(model(), ArrayUtils.append(X,Y));

You can record the last solution found as follows : ::

    Solution solution = new Solution(model());
    while (solver.solve()) {
        solution.record();
    }

You can also use a monitor as follows: ::

    Solution solution = new Solution(model());
    solver.plugMonitor(new IMonitorSolution() {
          @Override
          public void onSolution() {
              s.record();
          }
    });

Or with lambdas: ::

    Solution solution = new Solution(model());
    solver.plugMonitor((IMonitorSolution) () -> s.record());

Note that the solution is erased on each new recording.
To store all solutions, you need to create one new solution object for each solution.

You can then access the value of a variable in a solution as follows: ::

    int val = s.getIntVal(Y[0])

The solution object can be used to store all variables in Choco Solver (binaries, integers, sets and reals)

Search monitors
===============

Principle
---------

A search monitor is an observer of the resolver.
It gives user access before and after executing each main step of the solving process:

- `initialize`: when the solving process starts and the initial propagation is run,
- `open node`: when a decision is computed,
- `down branch`: on going down in the tree search applying or refuting a decision,
- `up branch`: on going up in the tree search to reconsider a decision,
- `solution`: when a solution is got,
- `restart search`: when the search is restarted to a previous node, commonly the root node,
- `close`: when the solving process ends,
- `contradiction`: on a failure,

With the accurate search monitor, one can easily observe with the resolver, from pretty printing of a solution to learning nogoods from restart, or many other actions.

The interfaces to implement are:

- ``IMonitorInitialize``,
- ``IMonitorOpenNode``,
- ``IMonitorDownBranch``,
- ``IMonitorUpBranch``,
- ``IMonitorSolution``,
- ``IMonitorRestart``,
- ``IMonitorContradiction``,
- ``IMonitorClose``.

Most of them gives the opportunity to do something before and after a step. The other ones are called after a step.

.. important::

	A search monitor should not modify the resolver behavior (forcing restart and interrupting the search, for instance).
	This is the goal of the Move component of a resolver :ref:`440_loops_label`.

Simple example to print every solution: ::

        Solver s = model.getSolver();
        s.plugMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
                System.out.println("x = "+x.getValue());
            }
        });

In Java 8 style: ::

        Solver s = model.getSolver();
        s.plugMonitor((IMonitorSolution) () -> {System.out.println("x = "+x.getValue());});

Search limits
=============

Built-in search limits
----------------------

Search can be limited in various ways using the ``Solver`` (from ``model.getSolver()``).

- ``limitTime`` stops the search when the given time limit has been reached. This is the most common limit, as many applications have a limited available runtime.

.. note::
    The potential search interruption occurs at the end of a propagation, i.e. it will not interrupt a propagation algorithm, so the overall runtime of the solver might exceed the time limit.

- ``limitSolution`` stops the search when the given solution limit has been reached.
- ``limitNode`` stops the search when the given search node limit has been reached.
- ``limitFail`` stops the search when the given fail limit has been reached.
- ``limitBacktrack`` stops the search when the given backtrack limit has been reached.

For instance, to interrupt search after 10 seconds: ::

    Solver s = model.getSolver();
    s.limitTime("10s");
    model.getSolver().solve();

Custom search limits
--------------------

You can design you own search limit by implementing a ``Criterion`` and using ``resolver.limitSearch(Criterion c)``: ::

        Solver s = model.getSolver();
        s.limitSearch(new Criterion() {
            @Override
            public boolean isMet() {
                // todo return true if you want to stop search
            }
        });

In Java 8, this can be shortened using lambda expressions: ::

        Solver s = model.getSolver();
        s.limitSearch(() -> { /*todo return true if you want to stop search*/ });


Using resolution statistics
===========================

Resolution data are available in the ``Solver`` object, whose default output is ``System.out``.
It centralises widely used methods to have comprehensive feedback about the resolution process.
There are two types of methods: those who need to be called **before** the resolution, with a prefix `show`, and those who need to called **after** the resolution, with a prefix `print`.

For instance, one can indicate to print the solutions all resolution long: ::

    solver.showSolutions();
    solver.findAllSolutions();

Or to print the search statistics once the search ends: ::

    solver.solve();
    solver.printStatistics();


On a call to ``solver.printVersion()``, the following message will be printed:

.. code-block:: none

    ** Choco |version| (2015-12) : Constraint Programming Solver, Copyleft (c) 2010-2015

On a call to ``solver.printVersion()``, the following message will be printed:

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
3. building time in second (from ``new Model()`` to ``solve()`` or equivalent)
4. initialisation time in second (before initial propagation)
5. initial propagation time in second
6. resolution time in second (from ``new Model()`` till now)
7. number of nodes in the binary tree search : one for the root node and between one and two for each decision (two when the decision has been refuted)
8. number of backtracks achieved
9. number of failures that occurred (conflict number)
10. number of restarts operated
11. maximum depth reached in the binary tree search
12. number of variables in the model
13. number of constraints in the model


If the resolution process reached a limit before ending *naturally*, the title of the message is set to :

.. code-block:: none

    - Incomplete search - Limit reached.

The body of the message remains the same. The message is formatted thanks to the ``IMeasureRecorder`` interface which is a :ref:`search monitor <44_monitors_label>`.

On a call to ``solver.showSolutions()``, on each solution the following message will be printed:

.. code-block:: none

    {0} Solutions, [Maximize = {1}][Minimize = {2}], Resolution {6}s, {7} Nodes, \\
                                        {8} Backtracks, {9} Fails, {10} Restarts

followed by one line exposing the value of each decision variables (those involved in the search strategy).

On a call to ``solver.showDecisions()``, on each node of the search tree a message will be printed indicating which decision is applied.
The message is prefixed by as many "." as nodes in the current branch of the search tree.
A decision is prefixed with ``[R]`` and a refutation is prefixed by ``[L]``.

.. code-block:: none

    ..[L]x  ==  1 (0) //X = [0,5] Y = [0,6] ...

.. warning::

    ``solver.printDecisions()`` prints the tree search during the resolution.
    Printing the decisions slows down the search process.

*****************
Search Strategies
*****************

The search space induced by variable domains is equal to  :math:`S=|d_1|*|d_2|*...*|d_n|` where :math:`d_i` is the domain of the :math:`i^{th}` variable.
Most of the time (not to say always), constraint propagation is not sufficient to build a solution, that is, to remove all values but one from variable domains.
Thus, the search space needs to be explored using one or more *search strategies*.
A search strategy defines how to explore the search space by computing *decisions*.
A decision involves a variables, a value and an operator, e.g. :math:`x = 5`, and triggers new constraint propagation.
Decisions are computed and applied until all the variables are instantiated, that is, a solution has been found, or a failure has been detected (backtrack occurs).
Choco |release| builds a binary search tree: each decision can be refuted (if :math:`x = 5` leads to no solution, then :math:`x != 5` is applied).
The classical search is based on `Depth First Search <http://en.wikipedia.org/wiki/Depth-first_search>`_.

.. note::

    There are many ways to explore the search space and this steps should not be overlooked.
    Search strategies or heuristics have a strong impact on resolution performances.
    Thus, it is strongly recommended to adapt the search space exploration to the problem treated.

Default search strategy
=======================

If no search strategy is specified to the resolver, Choco |version| will rely on the default one (defined by a ``defaultSearch`` in ``SearchStrategyFactory``).
In many cases, this strategy will not be sufficient to produce satisfying performances and it will be necessary to specify a dedicated strategy, using ``solver.setSearch(...)``.
The default search strategy splits variables according to their type and defines specific search strategies for each type that are sequentially applied:

#. integer variables and boolean variables : ``intVarSearch(ivars)`` (calls ``domOverWDegSearch``)
#. set variables: :code:`setVarSearch(svars)`
#. real variables :code:`realVarSearch(rvars)`
#. objective variable, if any: lower bound or upper bound, depending on the `ResolutionPolicy`

Note that `lastConflict` is also plugged-in.

Specifying a search strategy
============================

You may specify a search strategy to the resolver by using ``solver.setSearch(...)`` method as follows: ::

        import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

        // to use the default SetVar search on mySetVars
        Solver s = model.getSolver();
        s.setSearch(setVarSearch(mySetVars));

        // to use activity based search on myIntVars
        Solver s = model.getSolver();
        s.setSearch(activityBasedSearch(myIntVars));

        // to use activity based search on myIntVars
        // then the default SetValSelectorFactoryVar search on mySetVars
        Solver s = model.getSolver();
        s.setSearch(activityBasedSearch(myIntVars), setVarSearch(mySetVars));

.. note::

    Search strategies generally hold on some particular variable kinds only (e.g. integers, sets, etc.).

Example
-------

Let us consider we have two integer variables ``x`` and ``y`` and we want our strategy to select
the variable of smallest domain and assign it to its lower bound.
There are several ways to achieve this: ::

    // 1) verbose approach using usual imports

    import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
    import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
    import org.chocosolver.solver.search.strategy.selectors.values.*;
    import org.chocosolver.solver.search.strategy.selectors.variables.*;


        Solver s = model.getSolver();
        s.setSearch(SearchStrategyFactory.intVarSearch(
                        // selects the variable of smallest domain size
                        new FirstFail(model),
                        // selects the smallest domain value (lower bound)
                        new IntDomainMin(),
                        // apply equality (var = val)
                        DecisionOperator.int_eq,
                        // variables to branch on
                        x, y
        ));

    // 2) Shorter approach

    Use a static import for SearchStrategyFactory and do not specify the operator (equality by default)

    import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

    import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
    import org.chocosolver.solver.search.strategy.selectors.values.*;
    import org.chocosolver.solver.search.strategy.selectors.variables.*;


        Solver s = model.getSolver();
        s.setSearch(intVarSearch(
                        // selects the variable of smallest domain size
                        new FirstFail(model),
                        // selects the smallest domain value (lower bound)
                        new IntDomainMin(),
                        // variables to branch on
                        x, y
        ));


    // 3) Shortest approach using built-in strategies imports

    import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

        Solver s = model.getSolver();
        s.setSearch(minDomLBSearch(x, y));

.. important:: Black-box search strategies

    There are many ways of choosing a variable and computing a decision on it.
    Designing specific search strategies can be a very tough task.
    The concept of `Black-box search heuristic` has naturally emerged from this statement.
    Most common black-box search strategies observe aspects of the CSP resolution in order to drive the variable selection,
    and eventually the decision computation (presumably, a value assignment).
    Three main families of heuristic, stemming from the concepts of variable conflict, activity and impact may be found in Choco|release|.
    Black-box strategies can be augmented with restarts.

List of available search strategy
=================================

Most available search strategies are listed in ``SearchStrategyFactory``.
This factory enables you to create search strategies using static methods.
Most search strategies rely on :
 - variable selectors (see package ``org.chocosolver.solver.search.strategy.selectors.values``)
 - value selectors (see package ``org.chocosolver.solver.search.strategy.selectors.variables``)
 - operators (see ``DecisionOperator``)

``SearchStrategyFactory`` is not exhaustive, look at the selectors package to see learn more search possibilities.


Designing your own search strategy
==================================

Using selectors
---------------

To design your own strategy using SearchStrategyFactory.intVarSearch, you simply have to implement
your own variable and value selectors: ::

    public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                        IntValueSelector valSelector,
                                        IntVar... vars)

For instance, to select the first non instantiated variable and assign it to its lower bound: ::


        Solver s = model.getSolver();
        s.setSearch(intVarSearch(
                // variable selector
                (VariableSelector<IntVar>) variables -> {
                    for(IntVar v:variables){
                        if(!v.isInstantiated()){
                            return v;
                        }
                    }
                    return null;
                },
                // value selector
                (IntValueSelector) var -> var.getLB(),
                // variables to branch on
                x, y
        ));

.. note::

    When all variables are instantiated, a ``VariableSelector`` must return ``null``.

From scratch
------------

You can design your own strategy by creating ``Decision`` objects directly as follows: ::

        s.setSearch(new AbstractStrategy<IntVar>(x,y) {
            // enables to recycle decision objects (good practice)
            PoolManager<IntDecision> pool = new PoolManager();
            @Override
            public Decision getDecision() {
                IntDecision d = pool.getE();
                if(d==null) d = new IntDecision(pool);
                IntVar next = null;
                for(IntVar v:vars){
                    if(!v.isInstantiated()){
                        next = v; break;
                    }
                }
                if(next == null){
                    return null;
                }else {
                    // next decision is assigning nextVar to its lower bound
                    d.set(next,next.getLB(), DecisionOperator.int_eq);
                    return d;
                }
            }
        });

.. attention::

    A particular attention should be made while using ``IntVar`` and their type of domain.
    Indeed, bounded domains do not support making holes in their domain.
    Thus, removing a value which is not a current bound will be missed, and can lead to an infinite loop.

Making a decision greedy
========================

You can make a decision non-refutable by using ``decision.setRefutable(false)``

To make an entire search strategy greedy, use: ::

        Solver s = model.getSolver();
        s.setSearch(greedySearch(inputOrderLBSearch(x,y,z)));

Restarts
========

Restart means stopping the current tree search, then starting a new tree search from the root node.
Restarting makes sense only when coupled with randomized dynamic branching strategies ensuring that the same enumeration tree is not constructed twice.
The branching strategies based on the past experience of the search, such as adaptive search strategies, are more accurate in combination with a restart approach.

Unless the number of allowed restarts is limited, a tree search with restarts is not complete anymore. It is a good strategy, though, when optimizing an NP-hard problem in a limited time.

Some adaptive search strategies resolutions are improved by sometimes restarting the search exploration from the root node.
Thus, the statistics computed on the bottom of the tree search can be applied on the top of it.

Several restart strategies are available in ``Solver``: ::

    // Restarts after after each new solution.
    solver.setRestartOnSolutions()

Geometrical restarts perform a search with restarts controlled by the resolution event [#f1]_ ``counter`` which counts events occurring during the search.
Parameter ``base`` indicates the maximal number of events allowed in the first search tree.
Once this limit is reached, a restart occurs and the search continues until ``base``*``grow`` events are done, and so on.
After each restart, the limit number of events is increased by the geometric factor ``grow``.
``limit`` states the maximum number of restarts. ::

    solver.setGeometricalRestart(int base, double grow, ICounter counter, int limit)

The `Luby <http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.47.5558>`_ ’s restart policy is an alternative to the geometric restart policy.
It performs a search with restarts controlled by the number of resolution events [#f1]_ counted by ``counter``.
The maximum number of events allowed at a given restart iteration is given by base multiplied by the Las Vegas coefficient at this iteration.
The sequence of these coefficients is defined recursively on its prefix subsequences:
starting from the first prefix :math:`1`, the :math:`(k+1)^th` prefix is the :math:`k^th` prefix repeated ``grow`` times and
immediately followed by coefficient ``grow``:math:`^k`.

- the first coefficients for ``grow`` =2: [1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,1,...]
- the first coefficients for ``grow`` =3 : [1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,...] ::

    solver.setLubyRestart(int base, int grow, ICounter counter, int limit)

You can design your own restart strategies using: ::

    solver.setRestarts( LongCriterion restartCriterion,
                        IRestartStrategy restartStrategy,
                        int restartsLimit);


*****
Moves
*****

Large Neighborhood Search (LNS)
===============================

Local search techniques are very effective to solve hard optimization problems.
Most of them are, by nature, incomplete.
In the context of constraint programming (CP) for optimization problems, one of the most well-known and widely used local search techniques is the Large Neighborhood Search (LNS) algorithm [#q1]_.
The basic idea is to iteratively relax a part of the problem, then to use constraint programming to evaluate and bound the new solution.


.. [#q1] Paul Shaw. Using constraint programming and local search methods to solve vehicle routing problems. In Michael Maher and Jean-Francois Puget, editors, *Principles and Practice of Constraint Programming, CP98*, volume 1520 of *Lecture Notes in Computer Science*, pages 417–431. Springer Berlin Heidelberg, 1998.

Principle
---------

LNS is a two-phase algorithm which partially relaxes a given solution and repairs it.
Given a solution as input, the relaxation phase builds a partial solution (or neighborhood) by choosing a set of variables to reset to their initial domain;
The remaining ones are assigned to their value in the solution.
This phase is directly inspired from the classical Local Search techniques.
Even though there are various ways to repair the partial solution, we focus on the technique in which Constraint Programming is used to bound the objective variable and
to assign a value to variables not yet instantiated.
These two phases are repeated until the search stops (optimality proven or limit reached).

The ``LNSFactory`` provides pre-defined configurations.
Here is the way to declare LNS to solve a problem: ::

    LNSFactory.rlns(solver, ivars, 30, 20140909L, new FailCounter(solver, 100));
    solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);

It declares a *random* LNS which, on a solution, computes a partial solution based on ``ivars``.
If no solution are found within 100 fails (``FailCounter(solver, 100)``), a restart is forced.
Then, every ``30`` calls to this neighborhood, the number of fixed variables is randomly picked.
``20140909L`` is the seed for the ``java.util.Random``.


The instruction ``LNSFactory.rlns(solver, vars, level, seed, frcounter)`` runs:

.. literalinclude:: /../../choco-solver/src/main/java/org/chocosolver/solver/search/loop/lns/LNSFactory.java
   :language: java
   :lines: 112-114
   :linenos:

The factory provides other LNS configurations together with built-in neighbors.

Neighbors
---------

While the implementation of LNS is straightforward, the main difficulty lies in the design of neighborhoods able to move the search further.
Indeed, the balance between diversification (i.e., evaluating unexplored sub-tree) and intensification (i.e., exploring them exhaustively) should be well-distributed.


Generic neighbors
^^^^^^^^^^^^^^^^^

One drawback of LNS is that the relaxation process is quite often problem dependent.
Some works have been dedicated to the selection of variables to relax through general concept not related to the class of the problem treated [5,24].
However, in conjunction with CP, only one generic approach, namely Propagation-Guided LNS [24], has been shown to be very competitive with dedicated ones on a variation of the Car Sequencing Problem.
Nevertheless, such generic approaches have been evaluated on a single class of problem and need to be thoroughly parametrized at the instance level, which may be a tedious task to do.
It must, in a way, automatically detect the problem structure in order to be efficient.


Combining neighborhoods
^^^^^^^^^^^^^^^^^^^^^^^

There are two ways to combine neighbors.

Sequential
""""""""""

Declare an instance of ``SequenceNeighborhood(n1, n2, ..., nm)``.
Each neighbor ni is applied in a sequence until one of them leads to a solution.
At step k, the :math:`(k \mod m)^{th}` neighbor is selected.
The sequence stops if at least one of the neighbor is complete.

Adaptive
""""""""

Declare an instance of ``AdaptiveNeighborhood(1L, n1, n2, ..., nm)``.
At the beginning a weight of 1 at assigned to each neighbor ni.
Then, if a neighbor leads to solution, its weight :math:`w_i` is increased by 1.
Any time a partial solution has to be computed, a value ``W`` between 1 and :math:`w_1+w_2+...+w_n` is randomly picked (``1L`` is the seed).
Then the weight of each neighbor is subtracted from ``W``, as soon as ``W``:math:`\leq 0`, the corresponding neighbor is selected.
For instance, let's consider three neighbors n1, n2 and n3, their respective weights w1=2, w2=4, w3=1.
``W`` = 3  is randomly picked between 1 and 7.
Then, the weight of n1 is subtracted, ``W``2-=1; the weight of n2 is subtracted, ``W``-4 = -3, ``W`` is less than 0 and n2 is selected.


Defining its own neighborhoods
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

One can define its own neighbor by extending the abstract class ``INeighbor``.
It forces to implements the following methods:

+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| **Method**                                                             |   **Definition**                                                                                                       |
+========================================================================+========================================================================================================================+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``void recordSolution()``                                              | Action to perform on a solution (typicallu, storing the current variables' value).                                     |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``Decision fixSomeVariables()``                                        | Fix some variables to their value in the last solution, computing a partial solution and returns it as a decision.     |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``void restrictLess()``                                                | Relax the number of variables fixed. Called when no solution was found during a LNS run (trapped into a local optimum).|
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``boolean isSearchComplete()``                                         | Indicates whether the neighbor is complete, that is, can end.                                                          |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+

Restarts
--------

A generic and common way to reinforce diversification of LNS is to introduce restart during the search process.
This technique has proven to be very flexible and to be easily integrated within standard backtracking procedures [#q2]_.

.. [#q2] Laurent Perron. Fast restart policies and large neighborhood search. In Francesca Rossi, editor, *Principles and Practice of Constraint Programming at CP 2003*, volume 2833 of *Lecture Notes in Computer Science*. Springer Berlin Heidelberg, 2003.


Walking
-------

A complementary technique that appear to be efficient in practice is named `Walking` and consists in accepting equivalent intermediate solutions in a search iteration instead of requiring a strictly better one.
This can be achieved by defining an ``ObjectiveManager`` like this: ::

    solver.set(new ObjectiveManager(objective, ResolutionPolicy.MAXIMIZE, false));

Where the last parameter, named ``strict`` must be set to false to accept equivalent intermediate solutions.

Other optimization policies may be encoded by using either search monitors or a custom ``ObjectiveManager``.

.. _43_explanations_label:


********
Learning
********

Explanations
============

Choco |version| natively support explanations [#1]_. However, no explanation engine is plugged-in by default.


.. [#1] Narendra Jussien. The versatility of using explanations within constraint programming. Technical Report 03-04-INFO, 2003.


Principle
---------

Nogoods and explanations have long been used in various paradigms for improving search.
An explanation records some sufficient information to justify an inference made by the solver (domain reduction, contradiction, etc.).
It is made of a subset of the original propagators of the problem and a subset of decisions applied during search.
Explanations represent the logical chain of inferences made by the solver during propagation in an efficient and usable manner.
In a way, they provide some kind of a trace of the behavior of the solver as any operation needs to be explained.

Explanations have been successfully used for improving constraint programming search process.
Both complete (as the mac-dbt algorithm) and incomplete (as the decision-repair algorithm) techniques have been proposed.
Those techniques follow a similar pattern: learning from failures by recording each domain modification with its associated explanation (provided by the solver) and taking advantage of the information gathered to be able to react upon failure by directly pointing to relevant decisions to be undone.
Complete techniques follow a most-recent based pattern while incomplete technique design heuristics to be used to focus on decisions more prone to allow a fast recovery upon failure.

The current explanation engine is coded to be *Asynchronous, Reverse, Low-intrusive and Lazy*:

Asynchronous:
    Explanations are not computed during the propagation.

Reverse:
    Explanations are computed in a bottom-up way, from the conflict to the first event generated, *keeping* only relevant events to compute the explanation of the conflict.

Low-intrusive:
    Basically, propagators need to implement only one method to furnish a convenient explanation schema.

Lazy:
    Explanations are computed on request.


To do so, all events are stored during the descent to a conflict/solution, and are then evaluated and kept if relevant, to get the explanation.

In practice
-----------

Consider the following example:

.. literalinclude:: /../../choco-samples/src/test/java/org/chocosolver/docs/ExplanationExamples.java
   :language: java
   :lines: 52-56,59
   :linenos:

The problem has no solution since the two constraints cannot be satisfied together.
A naive strategy such as ``inputOrderLB(bvars)`` (which selects the variables in lexicographical order) will detect lately and many times the failure.
By plugging-in an explanation engine, on each failure, the reasons of the conflict will be explained.

.. literalinclude:: /../../choco-samples/src/test/java/org/chocosolver/docs/ExplanationExamples.java
   :language: java
   :lines: 57
   :linenos:

The explanation engine records *deductions* and *causes* in order to compute explanations.
In that small example, when an explanation engine is plugged-in, the two first failures will enable to conclude that the problem has no solution.
Only three nodes are created to close the search, seven are required without explanations.

.. note::

    Only unary, binary, ternary and limited number of nary propagators over integer variables have a dedicated explanation algorithm.
    Although global constraints over integer variables are compatible with explanations, they should be either accurately explained or reformulated to fully benefit from explanations.


Cause
^^^^^

A cause implements ``ICause`` and must defined the ``boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value)`` method.
Such a method add new *event filtering* rules to the ruleStore in parameter in order to *filter* relevant events among all generated during the search.
Every time a variable is modified, the cause is specified in order to compute explanations afterwards.
For instance, when a propagator updates the bound of an integer variable, the cause is the propagator itself.
So do decisions, objective manager, etc.

Computing explanations
^^^^^^^^^^^^^^^^^^^^^^

When a contradiction occurs during propagation, it can only be thrown by:

- a propagator which detects unsatisfiability, based on the current domain of its variables;
- or a variable whom domain became empty.

Consequently, in addition to causes, variables can also explain the current state of their domain.
Computing the explanation of a failure consists in going up in the stack of all events generated in the current branch of the search tree and filtering the one relative to the conflict.
The entry point is either a the unsatisfiabable propagator or the empty variable.

.. note::

    Explanations can be computed without failure. The entry point is a variable, and only removed values can be explained.


Each propagator embeds its own explanation algorithm which relies on the relation it defines over variables.


.. warning::

    Even if a naive (and weak) explanation algorithm could be provided by all constraints, we made the choice to throw an `SolverException` whenever a propagator does not defined its own explanation algorithm.
    This is restrictive, but almost all non-global constraints support explanation, which enables reformulation.
    The missing explanation schemas will be integrated all needs long.



For instance, here is the algorithm of ``PropGreaterOrEqualX_YC`` (:math:`x \geq y + c`, ``x`` and ``y`` are integer variables, ``c`` is a constant):

.. literalinclude:: /../../choco-solver/src/main/java/org/chocosolver/solver/constraints/binary/PropGreaterOrEqualX_YC.java
   :language: java
   :lines: 112-122
   :linenos:

The first lines indicates that the deduction is due to the application of the propagator (l.2), maybe through reification.
Then, depending on the variable touched by the deduction, either the lower bound of ``y`` (l.4) or the upper bound of ``x`` (l.6) explains the deduction.
Indeed, such a propagator only updates lower bound of ``y`` based on the upper bound of ``x`` and *vice versa*.

Let consider that the deduction involves ``x`` and is explained by the lower bound of ``y``.
The lower bound ``y`` needs to be explained.
A new rule is added to the ruleStore to specify that events on the lower bound of ``y`` needs to be kept during the event stack analyse (only events generated before the current are relevant).
When such events are found, the ruleStore can be updated, until the first event is analyzed.

The results is a set of branching decisions, and a set a propagators, which applied altogether leads the conflict and thus, explained it.


Explanations for the system
---------------------------

Explanations for the system, which try to reduce the search space, differ from the ones giving feedback to a user about the unsatisfiability of its model.
Both rely on the capacity of the explanation engine to motivate a failure, during the search form system explanations and once the search is complete for user ones.

.. important::

    Most of the time, explanations are raw and need to be processed to be easily interpreted by users.


Conflict-based backjumping
^^^^^^^^^^^^^^^^^^^^^^^^^^

When Conflict-based Backjumping (CBJ) is plugged-in, the search is hacked in the following way.
On a failure, explanations are retrieved.
From all left branch decisions explaining the failure, the last taken, *return decision*, is stored to jump back to it.
Decisions from the current one to the return decision (excluded) are erased.
Then, the return decision is refuted and the search goes on.
If the explanation is made of no left branch decision, the problem is proven to have no solution and search stops.


**Factory**: ``solver.explanations.ExplanationFactory``

**API**: ::

    CBJ.plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn)


+ *solver*: the solver to explain.
+ *nogoodsOn*: set to `true` to extract nogood from each conflict,. Extracting nogoods slows down the overall resolution but can reduce the search space.
+ *userFeedbackOn*: set to `true` to store the very last explanation of the search (recommended value: `false`).

Dynamic backtracking
^^^^^^^^^^^^^^^^^^^^

This strategy, Dynamic backtracking (DBT) corrects a lack of deduction of Conflict-based backjumping.
On a failure, explanations are retrieved.
From all left branch decisions explaining the failure, the last taken, *return decision*, is stored to jump back to it.
Decisions from the current one to the return decision (excluded) are maintained, only the return decision is refuted and the search goes on.
If the explanation is made of no left branch decision, the problem is proven to have no solution and search stops.


**Factory**: ``solver.explanations.ExplanationFactory``

**API**: ::

    DBT.plugin(Solver solver, boolean nogoodsOn, boolean userFeedbackOn)

+ *solver*: the solver to explain.
+ *nogoodsOn*: set to `true` to extract nogood from each conflict,. Extracting nogoods slows down the overall resolution but can reduce the search space.
+ *userFeedbackOn*: set to `true` to store the very last explanation of the search (recommended value: `false`).

Explanations for the end-user
-----------------------------

Explaining the last failure of a complete search without solution provides information about the reasons why a problem has no solution.
For the moment, there is no simplified way to get such explanations.
CBJ and DBT enable retrieving an explanation of the last conflict. ::

    // .. problem definition ..
    // First manually plug CBJ, or DBT
    ExplanationEngine ee = new ExplanationEngine(solver, userFeedbackOn);
    ConflictBackJumping cbj = new ConflictBackJumping(ee, solver, nogoodsOn);
    solver.plugMonitor(cbj);
    if(!solver.solve()){
        // If the problem has no solution, the end-user explanation can be retrieved
        System.out.println(cbj.getLastExplanation());
    }

Incomplete search leads to incomplete explanations: as far as at least one decision is part of the explanation, there is no guarantee the failure does not come from that decision.
On the other hand, when there is no decision, the explanation is complete.


.. _440_loops_label:

Search loop
===========

The search loop whichs drives the search is a freely-adapted version PLM [#PLM]_.
PLM stands for: Propagate, Learn and Move.
Indeed, the search loop is composed of three parts, each of them with a specific goal.

- Propagate: it aims at propagating information throughout the constraint network when a decision is made,
- Learn: it aims at ensuring that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less,
- Move: it aims at, unlike the former ones, not pruning the search space but rather exploring it.

.. [#PLM] Narendra Jussien and Olivier Lhomme. Unifying search algorithms for CSP. Technical report 02-3-INFO, EMN.

Any component can be freely implemented and attached to the search loop in order to customize its behavior.
There exists some pre-defined `Move` and `Learn` implementations, avaiable in :ref:`550_slf`.

**Move**:

:ref:`550_slfdfs`,
:ref:`550_slflds`,
:ref:`550_slfdds`,
:ref:`550_slfhbfs`,
:ref:`550_slfseq`,
:ref:`550_slfrestart`,
:ref:`550_slfrestartonsol`,
:ref:`550_slflns`.

**Learn**:

:ref:`550_slfcbj`,
:ref:`550_slfdbt`,

One can also define its own `Move` or `Learn` implementation, more details are given in :ref:`48_plm`.


.. _45_define_search_label:

Implementing a search loop component
====================================

A search loop is made of three components, each of them dealing with a specific aspect of the search.
Even if many `Move` and `Learn` implementation are already provided, it may be relevant to define its own component.

.. note::

	The `Propagate` component is less prone to be modified, it will not be described here.
	However, its interface is minimalist and can be easily implemented.
	A look to `org.chocosolver.solver.search.loop.propagate.PropagateBasic.java` is a good starting point.

The two components can be easily set in the `Solver` search loop:

``void setMove(Move m)``
	The current `Move` component is replaced by `m`.

``Move getMove()``
	The current `Move` component is returned.

`void setLearn(Learn l)` and `Learn getLearn()` are also avaiable.

Having access to the current `Move` (resp. `Learn`) component can be useful to combined it with another one.
For instance, the `MoveLNS` is activated on a solution and creates a partial solution.
It needs another `Move` to find the first solution and to complete the partial solution.

Move
----

Here is the API of `Move`:


``boolean extend(SearchLoop searchLoop)``
	Perform a move when the CSP associated to the current node of the search space is not proven to be not consistent.
	It returns `true` if an extension can be done, `false` when no more extension is possible.
	It has to maintain the correctness of the reversibility of the action by pushing a backup world when needed.
	An extension is commonly based on a decision, which may be made on one or many variables.
	If a decision is created (thanks to the search strategy), it has to be linked to the previous one.

``boolean repair(SearchLoop searchLoop)``
	Perform a move when the CSP associated to the current node of the search space is proven to be not consistent.
	It returns `true` if a reparation can be done, `false` when no more reparation is possible.
	It has to backtracking backup worlds when needed, and unlinked useless decisions.
	The depth and number of backtracks have to be updated too, and "up branch" search monitors of the search loop have to called
 	(be careful, when many `Move` are combined).


``Move getChildMove()``
	It returns the child `Move` or `null`.

``void setChildMove(Move aMove)``
	It defined the child `Move` and erases the previously defined one, if any.

``boolean init()``
	Called before the search starts, it should initialize the search strategy, if any, and its child `Move`.
     	It should return `false` if something goes wrong (the problem has trivially no solution), `true` otherwise.

``AbstractStrategy<V> getStrategy()``
	It returns the search strategy in use, which may be `null` if none has been defined.

``void setStrategy(AbstractStrategy<V> aStrategy)``
	It defines a search strategy and erases the previously defined one, that is, a service which computes and returns decisions.


``org.chocosolver.solver.search.loop.move.MoveBinaryDFS.java`` is good starting point to see how a `Move` is implemented.
It defines a Depth-First Search with binary decisions.

Learn
-----

The aim of the component is to make sure that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less. Here is the API of `Learn`

``void record(SearchLoop searchLoop)``
	It validates and records a new piece of knowledge, that is, the current position is a dead-end.
	This is alwasy called *before* calling `Move.repair(SearchLoop)`.

``void forget(SearchLoop searchLoop)``
	It forgets some pieces of knowledge.
	This is alwasy called *after* calling `Move.repair(SearchLoop)`.

``org.chocosolver.solver.search.loop.learn.LearnCBJ`` is good, yet not trivial, example of `Learn`.

***********************
Multi-thread resolution
***********************

Choco |version| provides a simple way to use several threads to treat a problem. The main idea of that driver is to solve the *same* model with different search strategies and to share few information to make these threads help each others.

To use a portfolio of solvers in parallel, use ``ParallelPortfolio`` as follows: ::

        ParallelPortfolio portfolio = new ParallelPortfolio();
        int nbModels = 5;
        for(int s=0;s<nbModels;s++){
            portfolio.addModel(makeModel());
        }
        portfolio.solve();

In this example, ``makeModel()`` is a method you have to implement to create a ``Model`` of the problem.
Here all models are the same and the portfolio will change the search heuristics of all models but the first one.
This means that the first thread will run according to your settings whereas the others will have a different configuration.

In order to specify yourself the configuration of each thread, you need to create the portfolio by setting the optional
boolean argument ``searchAutoConf`` to false as follows: ::

        ParallelPortfolio portfolio = new ParallelPortfolio(false);
        int nbModels = 5;
        for(int s=0;s<nbModels;s++){
            portfolio.addModel(makeModel(s));
        }
        portfolio.solve();

In this second example, the parameter ``s`` enables you to change the search strategy within the ``makeModel`` method (e.g. using a ``switch(s)``).

When dealing with multithreading resolution, very few data is shared between threads:
everytime a solution has been found its value is shared among solvers. Moreover,
when a solver ends, it communicates an interruption instruction to the others.
This enables to explore the search space in various way, using different model settings such as search strategies
(this should be done in the dedicated method which builds the model, though).

.. _48_plm: