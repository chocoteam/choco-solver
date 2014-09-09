Logging
=======

Choco |version| has a simple logger which can be used by calling ::

 SearchMonitorFactory.log(Solver solver, boolean solution, boolean choices);

The first argument is the solver.
The second indicates whether or not each solution (and associated resolution statistics) should be printed.
The third argument indicates whether or not each branching decision should be printed. This may be useful for debugging.

In general, in order to have a reasonable amount of information, we set the first boolean to true and the second to false.

If the two booleans are set to false, the trace would start with a welcome message:


.. code-block:: none

    ** Choco 3.2.0 (2014-05) : Constraint Programming Solver, Copyleft (c) 2010-2014
    ** Solve : myProblem

Then, when the resolution process ends, a complementary message is printed, based on the measures recorded.

.. code-block:: none

     - Search complete - [ No solution. ]
        Solutions: {0}
     [  Maximize = {1}  ]
     [  Minimize = {2}  ]
        Building time : {3}s
        Initialisation : {4}s
        Initial propagation : {5}s
        Resolution : {6}s
        Nodes: {7}
        Backtracks: {8}
        Fails: {9}
        Restarts: {10}
        Max depth: {11}
        Propagations: {12} + {13}
        Memory: {14}mb
        Variables: {15}
        Constraints: {16}

Brackets *[instruction]* indicate an optional instruction.
If no solution has been found, the message "No solution." appears on the first line.
``Maximize`` –resp. ``Minimize``– indicates the best known value before exiting of the objective value using a ``ResolutionPolicy.MAXIMIZE`` –resp. ResolutionPolicy.MINIMIZE- policy.

Curly braces *{value}* indicate search statistics:

0. number of solutions found
1. objective value in maximization
2. objective value in minimization
3. building time in second (from ``new Solver()`` to ``findSolution()`` or equivalent)
4. initialisation time in second (before initial propagation)
5. initial propagation time in second
6. resolution time in second (from ``new Solver()`` till now)
7. number of decision created, that is, nodes in the binary tree search
8. number of backtracks achieved
9. number of failures that occurred
10. number of restarts operated
11. maximum depth reached in the binary tree search
12. number of *fine* propagations
13. number of *coarse* propagations
14. estimation of the memory used
15. number of variables in the model
16. number of constraints in the model


If the resolution process reached a limit before ending *naturally*, the title of the message is set to :

.. code-block:: none

    - Incomplete search - Limit reached.

The body of the message remains the same.
The message is formated thanks to the ``IMeasureRecorder`` which is a :ref:`search monitor <44_monitors_label>`.

When the first boolean of ``SearchMonitorFactory.log(Solver, boolean, boolean);`` is set to true, on each solution the following message will be printed:

.. code-block:: none

    {0} Solutions, [Maximize = {1}][Minimize = {2}], Resolution {6}s, {7} Nodes, \\
                                        {8} Backtracks, {9} Fails, {10} Restarts

followed by one line exposing the value of each decision variables (those involved in the search strategy).

When the second boolean of ``SearchMonitorFactory.log(Solver, boolean, boolean);`` is set to true, on each node a message will be printed indicating which decision is applied.
The message is prefixed by as many "." as nodes in the current branch of the search tree.
A decision is prefixed with ``[R]`` and a refutation is prefixed by ``[L]``.

.. warning::

    Printing the choices slows down the search process.