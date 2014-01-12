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
* Less Java Genericity:
- Remove Delta type from Variable 
- Remove Propagator type from Constraints
- Remove Variable type from views