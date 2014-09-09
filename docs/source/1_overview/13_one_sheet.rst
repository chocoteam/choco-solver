Choco |version| quick documentation
===================================

Solver
------

The ``Solver`` is a central object and must be created first: ``Solver solver = new Solver();``.

:ref:`[Solver] <21_solver_label>` , :ref:`Limiting the resolution <33_searches_limit_label>` .

Variables
---------

The ``VariableFactory`` (``VF`` for short) eases the creation of variables.
Available variables are: ``BoolVar``, ``IntVar``, ``SetVar``, ``GraphVar`` and ``RealVar``.
Note, that an ``IntVar`` domain can be bounded (only bounds are stored) or enumerated (all values are stored); a boolean variable is a 0-1 IntVar.

:ref:`[Variables] <22_variables_label>`

Views
-----

A view is a variable whose domain is defined by a function over another variable domain.
Available views are: ``not``, ``offset``, ``eq``, ``minus``, ``scale`` and ``real``.

:ref:`[Views] <22_variables_view_label>`

Constants
---------

Fixed-value integer variables should be created with the specific ``VF.fixed(int, Solver)`` function.

:ref:`[Constants] <22_variables_constant_label>`

Constraints
-----------

Several constraint factories ease the creation of constraints: ``LogicalConstraintFactory`` (``LCF``), ``IntConstraintFactory`` (``ICF``), ``SetConstraintsFactory`` (``SCF``) and ``GraphConstraintFactory`` (``GCF``).
``RealConstraint`` is created with a call to new and to ``addFunction`` method. It requires the `Ibex <http://www.ibex-lib.org/>`_ solver.
Constraints hold once posted: ``solver.post(c);``.
Reified constraints should not be posted.

:ref:`[Constraints] <23_constraints_label>`

Search
------

Defining a specific way to traverse the search space is made thanks to: ``solver.set(AbstractStrategy)``.
Predefined strategies are available in ``IntStrategyFactory`` (``ISF``), ``SetStrategyFactory`` and ``GraphStrategyFactory``.

Large Neighborhood Search (LNS)
-------------------------------

Various LNS (random, propagation-guided, etc.) can be created from the LNSFactory to improve performance on optimization problems.

Monitors
--------

An ``ISearchMonitor`` is a callback which enables to react on some resolution events (failure, branching, restarts, solutions, etc.).
``SearchMonitorFactory`` (``SMF``) lists most useful monitors.
User-defined monitors can be added with ``solver.plugSearchMonitor(...)``.

Limits
------

A limit may be imposed on the search. The search stops once a limit is reached. Available limits are ``SMF.limitTime(solver, 5000)``, ``SMF.limitFail(solver, 100)``, etc.

Restarts
--------

Restart policies may also be applied ``SMF.geometrical(...)`` and ``SMF.luby(...)`` are available.

Logging
-------

Logging the search is possible.
There are variants but the main way to do it is made through the ``SMF.log(Solver, boolean, boolean)``.
The first boolean indicates whether or not logging solutions, the second indicates whether or not logging search decisions.
It also print, by default, main statistics of the search (time, nodes, fails, etc.)


Solving
-------

Finding if a problem has a solution is made through a call to: ``solver.findSolution()``.
Looking for the next solution is made thanks to ``nextSolution()``.
``findAllSolutions()`` enables to enumerate all solutions of a problem.
To optimize an objective function, call ``findOptimalSolution(...)``.
Resolutions perform a Depth First Search.

Solutions
---------

By default, the last solution is restored at the end of the search.
Solutions can be accessed as they are discovered by using an ``IMonitorSolution``.

Explanations
------------

Choco natively supports explained constraints to reduce the search space and to give feedback to the user.
Explanations are disabled by default.
