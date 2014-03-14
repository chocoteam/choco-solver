Choco 3 ChangeLog
===================

This file is dedicated to sum up the new features added and bugs fixed in Choco 3 since the first stable version, 3.1.0.

3.1.0 - 02 Sep 2013
-------------------

This the first stable version of Choco 3.
The CHANGES.md file starts at this version.

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

????
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