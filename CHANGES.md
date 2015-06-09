Choco 3 ChangeLog
===================

This file is dedicated to sum up the new features added and bugs fixed in Choco 3 since the first stable version, 3.1.0.

NEXT MILESTONE
-------------------

- Add keysorting() constraint
- Add a solver interface (ISolver), a solver factory (SolverFactory) and a solver portfolio (Portfolio) + tests (#293)
- Update Variable and VariableFactory to deal with ISolver (#293)
- Remove MasterSolver and SlaveSolver (#293)
- Add unplugSearchMonitor() to SearchLoop, Solver ans SearchMonitorList (#300)
- Modify 'ISearchLoop.interrupt()', add new parameter 'avoidable' to qualify the strength of the interruption (#304)
- lazy creation of ZERO, ONE, TRUE and FALSE through methods of Solver (#293)

Bug fixes: #296, #297, #298, #303

3.3.1 - 11 May 2015
-------------------

- Change the default propagation engine (default is now SevenQueuesPropagatorEngine)
- Add clause_channeling constraint
- Remove IntVar.wipeOut(...)
- Enable hot variable addition to propagator
- Move nogood recording from solution and restart from constraint pkg to monitor +(revamp) + SMF API (#261)
- Explanations are enabled on initial propagation (#247)
- Improvement of the solution object (#254)
- add DBT for explanations
- Improve explanation engine
- Add an additional "worldPush" instruction after initial propagation to be coherent with restarts (#55)
- Remove restarts from ABS (#282)
- one-shot decision are not reinitialized (#283)
- Remove Propagation count (#284)
- Change the default search strategy (#290)
- Add possibility to complete the declared search strategy (#291)
- Add new methods to Chatterbox (#292)

Bug fixes: #168, #221, #239, #259, #262, #264, #265, #267, #269, #271, #273, #275, #279, #281, #295


3.3.0 - 04 Dec 2014
-------------------

- Preparation to MCR (#248)
- Update User Guide (#226, #243, #245)
- Add modifiable settings (#250)
- Simplify search binder (#229)
- All propagators of constraint factory allow duplication (#217)
- Update license and header (#241)
- Rollback to old release process (#246)

Bug fixes: #215, #252, #253, #255, #256, #258

3.2.2 - 17 Nov 2014
-------------------

- Fix #240: add notmember(IntVar, SetVar) constraint (more efficient than not(member))
- Fix #225: fix PropCostRegular, wrt to S.Demassey instructions.
- Fix #229: create MasterSolver and SlaveSolver classes to deal with multi-thread resolution
            + add external configuration of the search strategy through a binder
- Fix #227: deal with initial propagation
- fix #230: update release script
- fix #231: correct addTrue in SatFactory
- fix #234: improve reification (presolve and less overheads). As a side effect, reification constraints are automatically posted and cannot be reified directly.
- fix #233: remove java8 compliant code (temporary)
- Add a MDD-based propagator (ICF.mddc).
- fix #235: refactor logging fmwk. Add Chatterbox class as a unique entry point for messaging. Logging still relies on SLF4J.
- fix #236: bug in SatSolver

3.2.1 - 13 Oct 2014
-------------------

- Graph vars externalized into choco-graph module (https://github.com/chocoteam/choco-graph)
- Fix PropSymmetric (set vars)
* Fix #206: fix lack of robustness in eucl_div
- Better circuit constraint
- incremental and coarse propagation of graph variable degrees
- better samples for Hamiltonian cycle problems
- NValue now split into atleast and atmost
- fix LOGGER usage in parser (allows different levels of logging)
- integer signature for the Lagrangian 1-tree relaxation constraint (good for solving the TSP)
- Table constraint refactoring (STR2+) (seems to be not idempotent however)
- Table reformulation of small scalar products
- Minimum and Maximum over boolean arrays
- Issue #215: Fix generation of relation based on tuples
- Fix #214: Fix problems related to propagators dynamic addition and deletion
- Add a GenerateAndTest search strategy which can be combined with others
- Fix #218: return null when all variables are instantiated
- Fix #219: fix range iterator of enumerated integer variable
- Refactor IEventType (use interface and a concrete implementation for each variable type)
- EXTRACT GRAPH VAR MODULE to choco-graph project (https://github.com/chocoteam/choco-graph)
- Fix bug in CoupleTable due to wrong range use
- Issue #191: disable buildFakeHistory by default (add a condition to build fake history)
- Space are not filtered anymore from Operator
- Remove vars.clone() in Propagator constructor.
- Remove DSLEngine and dependencies
- Add a set constraint to get the set of values of an array of integer variables (SCF.int_values_union)

3.2.0 - 28 May 2014
-------------------
* Fix #148: update release script
* Refactoring  #149
* Less Java Genericity:
- Remove Delta type from Variable
- Remove Propagator type from Constraints
- Remove Variable type from views
* StrategySequencer now created automatically
* Strong constraints refactoring: A Constraint is defined as a set of propagators which can be reified
- propagators must all be given to the super constructor
- Remove IntConstraint
- Remove many constraint classes
- Remove isEntailed() from Constraint (isSatisfied does the job)
- RealConstraint slightly changes
* Move obsolete code to extra module
* Associate variables with the solver in AbstractVariable super constructor
* Unique ObjectiveManager (merge of IntObjectiveManager and RealObjectiveManager)
* Default implementation of Propagator.getPropagationConditions(int vIdx) which reacts to all kinds of fine event.
* Fix #146: a new propagation engine is now available which manages coarse propagations
* Fix #159: avoid stackoverflow using GCC_fast
* Fix #160: speed up count propagator
* Fix #161: Propagator: fine_evt and default implementation of propagate(int,int)
* Fix #162: update filtering algorithm of PropFivXYZ
* Fix #163: Constraint#isSatisfied() handles stateless propagators
* Fix #158 fix bug in PropMemberEnum
* Fix #165: reset AbstractSearchLoop.decision on a call to reset()
* Fix #152: manage dynamic (temporarily and permanently) addition of constraints
* Fix #167: ObjectiveManager toString able to handle real values
* new implementation of Among propagator
* Fix #176: bug fixed in PropMin/PropMax
* Fix #175: IMeasure objects can be cloned
* Fix #182: Set propagators manage ISetDeltaMonitors
* Fix #183: change release script
* Fix #177-#179: add a ContradictionException to NoPropagatioEngine to handle with empty domain variables
* Fix #173: modify default failure message in initial propagation
* Fix #172: fix retriveXXXvars() + tests
* Fix #171: define VF.MIN_INT_BOUND and VF.MAX_INT_BOUND
* Fix #170: update dependencies
* Fix #95-#186: simplify search strategies and enable tie breaking
* Fix #187: patch Once
* Fix #174: a default search strategy is now available, dealing with each type of variables present
* Fix #189: Added methods to enumerate and store all optimal solutions
* Fix #190: Entailment of PropBoolChannel
* Fix #191: Enable dynamic addition of variables during the resolution (cf. Pareto)
* Start documentation (see user_guide.pdf and http://chocoteam.github.io/choco3/)
* NogoodFromRestart now trigger propagation fixpoint
* Fix #192: NogoodFromSolution now available (only for integer variables)
* Fix #193: VF.enumerated() now copies the input array of values
* Strong refactoring of IntStrategyFactory (access to variable and value selectors, decision operators, and more simple to understand search names).
* Stronger AtMostNValue constraint with automatic detection of disequalities
* Fix #114: Enable to specify a time unit in time limits (ms, s, m or h)
* Fix #195: fix bug while using IntViews over BoolVar
* Fix #17: propagator annotations (PropAnn) have been removed
* Fix #127: a specific view problem remains (new issue opened)
* Fix #166: remove constants from default search strategy
* Fix #196: fix view awakening problem
- Views are now safe
- Possibility to reformulate views with channeling constraints
- Catch some particular cases of times in the factory
- AC guaranteed for times(X,Y,Z) constraint when Y is a constant
- Add path and subpath constraints, holding on integer variables
- Add SORT constraint
- Changes measure, times are now in second
* Fix#199: some deltamonitors were desynchronized with delta

3.1.1 - 10 Jan 2014
-------------------

* Explain PropSumEq
* Fix #118: activate DBT
* Feat #120: new framework to build set strategies, more built-in set strategies
* Fix #122: update Database schema
* Fix #123: Monotonic decreasing for real objective
* Fix #124: mzn script now handles free search
* Fix #125: remove shut down hooks when the resolution ends normally
* Cumulative energy-based greedy filter improvement
* Fix #130: fix delta for view
* Fix #142: synchronize nb pending events
* Fix #143: Dow/WDeg is now supporting propagator hot addition
* Fix #144: setObjectiveOptimal(...) in searchLoop.close() is correct


3.1.0 - 02 Sep 2013
-------------------

This the first stable version of Choco 3.
The CHANGES.md file starts at this version.
