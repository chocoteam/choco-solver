Choco Solver ChangeLog
======================

This file is dedicated to sum up the new features added and bugs fixed in Choco-solver since the version, 4.0.0.

NEXT MILESTONE
-------------------

* Major features:
    - restrict calls to `Solver.setEngine(...)` when propagation started. See javadoc for details.
    - remove global constructive disjunction, only local constructive disjunction is allowed.
    - add `Solution.restore()` to restore a solution (#354).
    
* Deprecated API:
    - `Solver.getState()` (#485)
    - `Measures.IN_SEC` (related to #486)
    - `Settings.outputWithANSIColors`, `IOutputFactory.ANSI_*`

* Closed issues: #468, #481, #486, #487, #488.

4.0.1 - 16 Dec 2016
-------------------


4.0.0 - 13 Sep 2016
-------------------
