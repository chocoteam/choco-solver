Logging
=======

Choco |version| has a simple logger which can be used by calling ::

 SearchMonitorFactory.log(Solver solver, boolean solution, boolean choices);

The first argument is the solver. The second indicates whether or not each solution (and associated resolution statistics) should be printed. The third argument indicates whether or not each branching decision should be printed. This may be useful for debugging. 

In general, in order to have a reasonable amount of information, we set the first boolean to true and the second to false.