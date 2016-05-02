Choco 3 ChangeLog
===================

This file is dedicated to sum up the new features added and bugs fixed in Choco 3 since the first stable version, 3.1.0.

NEXT MILESTONE
-------------------

### Solver:

- `ParallelResolution` replaces `SMF.prepareForParallelResolution(List<Solver>)` (depreacted); it offers more services 
like getting a solver which a solution -- or the best one (#362)
- reduce memory footprint : `Solution.record(Solver)` (#370) and `Random` search strategy
- add impl of `IntIterableSet`: `IntIterableRangeSet`, (and a factory `IntIterableSetFactory` to be completed in the future)
- method `retrieveIntVars` in `Solver` must specify whether or not to include BoolVar in the result

### Variables
- add new methods in IntVar to get next/previous value out of a domain

### Constraint

- Use MDD as a table constraint (#366)
- Can use ints in the right hand side of sum and scalar constraints instead of an IntVar
- Deprecate constraint signatures to force specifying an operator for sum and scalar (#371)
- add constructive disjunction (see `SatFactory`) (#367)
- add ternary sum (X + Y = Z) achieving AC

### Propagation:

- Move pending event counter from `Propagator` to `SevenQueuePropagationEngine` (required for #367)

#### Bug fixes: 

\#361, #364, #365, #368, #369, #370, #379, #380

Fix array out of bound exception in `PropLargeMDDC`.

3.3.3 - 22 Dec 2015
-------------------

#### All:
 - remove deprecated interfaces, classes and methods.

#### Solver:
- add new APIs with an argument named `restoreLastSolution` which allow to indicate
whether or not the last solution found, if any, should be restored on exit;
Previous APIs (without the argument) restore the last solution by default (#354)
- update javadoc (in particular: #347)
- add default name to Solver + setter, modify measures printing to include the name.
- `SetVar` toString implementation has changed


#### Explanations
- refactor `PropNogoods` to deal with generalized no-goods

#### Bug fixes: 

\#346, #348, #349, #350, #351, #352, #353, #354, #355, #356, #357, #358, #359

3.3.2 - 17 Nov 2015
-------------------

#### Solver: 
- `ISolutionRecorder` implementations do not restore automatically the last/best solution found on exit.
This now has to be done calling either `solver.restoreLastSolution()` or `solver.restoreSolution(Solution)`.
- remove `MasterSolver` and `SlaveSolver` (#293)
- `Solver.duplicate()`, `Propagator.duplicate(Solver solver, THashMap<Object, Object> identitymap)` 
and `Variable.duplicate(Solver solver, THashMap<Object, Object> identitymap)` has been removed. 
The expected way to duplicate a model is to create a method which creates a `Solver`, fills it with variables and contraints
and returns it. Doing so, the models are sure to the same and reduces the risk of errors
- add a search monitor helper for parallel resolution with Java8 lambda (#293)
- lazy creation of ZERO, ONE, TRUE and FALSE through methods of `Solver` (#293)
- refactor `Solution` (#306)
- improve propagator iteration in propagation engine
- revamp `IMeasures` to avoid declaring its concrete class as monitor
- remove deprecated classes: `GenerateAndTest`, `LastConflict_old`
- add new API to `Solver` to declare eagerly the objective variable(s) and the precision
- enable printing decisions and solutions in ANSI colors (see `Settings.outputWithANSIColors()`)
- add connection to [cp-profiler](https://github.com/cp-profiler/cp-profiler) (#341)

#### Search:

- Deeply revamp the search loop to offer more flexibility 
- `SearchLoop.interrupt()`is now forbidden
- `AbstractStrategy.init()` cannot throw `ContradictionException` anymore but returns false instead
- revamp decisions to enable `IntMetaDecision` 
- change Decision's API, `getDecisionVariable()` becomes `getDecisionVariables()`
- move `FastDecision` to `../IntDecision`
- revamp `IntDecision.toString()`
- set `DecisionOperator` as an interface (instead of an abstract class)
- `org.chocosolver.solver.search.limits.ICounter` and its concrete class have been revisited, a `Solver` is now needed to create them.

#### Variables:

- add `IntVar.removeValues(IntIterableSet, ICause)`, `IntVar.removeAllValuesBut(IntIterableSet, ICause)`
and `IntVar.updateBounds(int, int, ICause) (#270)`
- improve `IntVar.removeInterval(int, int, ICause)` for enumerated domain integer variables
- prevent the user from using `ISF.random_value` on bounded vars

#### Constraints:

- add `keysorting()` constraint
- add `int_value_precede_chain()` constraint
- improve "DEFAULT" option for `ICF.alldifferent()`
- revamp scalar and sum (#324)
- fix lack of filtering in `PropMin`
- simplify and improve basic element propagator (#325)
- remove `aCause` from `Propagator`, should be replaced by `this`
- simplify `Propagation.contradiction(...)`
- add new setting to Settings to allow to not clone variables array input to Propagator's constructor
- `Propagator.getPropagationConditions()` becomes public


#### Explanations: 

- deal with unary decision (when once is set to true)
- add `PoolManager` for `Rules` and `Propagators`
- explain `PropCount_AC`
- fix lack of explanations in `SatSolver`
- fix `PropLessOrEqualXY_C` to add the right rule

#### Bug fixes: 

\#152, #153, #164, #176, #296, #297, #298, #301, #303, #309, #311, #313, #314, #317, #320, #323, #326, #327, 
\#331, #333, #334, #337, #338, #342

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
