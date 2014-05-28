Recording solutions
===================

Choco |version| requires each solution to be fully instantiated, i.e. every variable must be fixed.
Otherwise, an exception will be thrown if assertions are turned on (when ``-ea`` is added to the JVM parameters).
Choco |version| includes several ways to record solutions.

Solution storage
~~~~~~~~~~~~~~~~

A solution is usually stored through a ``Solution`` object which maps every variable with its current value.
Such an object can be erased to store new solutions.

Solution recording
~~~~~~~~~~~~~~~~~~

Built-in solution recorders
---------------------------

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
---------------

You can build you own way of manipulating and recording solutions by either implementing your own ``ISolutionRecorder`` object
or by simply using an ``ISolutionMonitor``, as follows:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/SMPTSP.java
   :language: java
   :lines: 118-124
   :linenos:

Solution restoration
~~~~~~~~~~~~~~~~~~~~

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
