Recording solutions
===================

Choco |version| requires each solution to be fully instantiated, i.e. every variable must be fixed.
Otherwise, an exception will be thrown if assertions are turned on (when ``-ea`` is added to the JVM parameters).
Choco |version| includes several ways to record solutions.

Solution storage
~~~~~~~~~~~~~~~~

A solution is usually stored throw a ``Solution`` object which maps every variable with its current value.
Such an object can be erased to store new solutions.

Solution recording
~~~~~~~~~~~~~~~~~~

A solution recorder is a object in charge of recording variable values in solutions.
There exists many built-in solution recorders:
``LastSolutionRecorder``, ``AllSolutionsRecorder``, ``BestSolutionsRecorder`` or ``ParetoSolutionsRecorder``.

Last solution recorder
----------------------

``LastSolutionRecorder`` only keeps variable values of the last solution found. It is the default solution recorder.
Furthermore, it is possible to restore that solution after the search process ends.
This is used by default when seeking an optimal solution.

All solutions recorder
----------------------

``AllSolutionsRecorder`` records all solutions that are found.
As this may result in a memory explosion, it is not used by default.

Best solutions recorder
-----------------------

``BestSolutionsRecorder`` records all solutions but removes from the solution set each solution that is worse than the best solution value found so far.
This may be used to enumerate all optimal (or at least, best) solutions of a problem.

Pareto front solutions recorder
-------------------------------

``BestSolutionsRecorder`` records all solutions of the pareto front of the multi-objective problem.

Custom recorder
---------------

You can build you own way of recording solutions by either implementing your own ``ISolutionRecorder`` objects or simply using an ``ÌSolutionMonitor``.