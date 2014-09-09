.. _31_solving_label:

Finding solutions
=================

Choco |version| provides different API, offered by ``Solver``, to launch the problem resolution.
Before everything, there are two methods which help interpreting the results.

**Feasibility**:
 Once the resolution ends, a call to the ``solver.isFeasible()`` method will return a boolean which indicates whether or not the problem is feasible.

 - ``true``: at least one solution has been found, the problem is proven to be feasible,

 - ``false``: in principle, the problem has no solution. More precisely, if the search space is guaranteed to be explored entirely, it is proven that the problem has no solution.


**Limitation**:
  When the resolution is limited (See :ref:`Limiting the resolution <33_searches_limit_label>` for details and examples), one may guess if a limit has been reached.
  The ``solver.hasReachedLimit()`` method returns ``true`` if a limit has bypassed the search process, ``false`` if it has ended *naturally*.

.. _Limits: 31_solving.html#limiting-the-resolution

.. warning::

 In some cases, the search may not be complete.
 For instance, if one enables restart on each failure with a static search strategy,
 there is a possibility that the same sub-tree is explored permanently.
 In those cases, the search may never stop or the two above methods may not be sufficient to confirm the lack of solution.

Satisfaction problems
---------------------

Finding a solution
^^^^^^^^^^^^^^^^^^

A call to ``solver.findSolution()`` launches a resolution which stops on the first solution found, if any.

.. literalinclude:: /../../choco-samples/src/test/java/docs/Overview.java
   :language: java
   :lines: 44-54
   :emphasize-lines: 54
   :linenos:

If a solution has been found, the resolution process stops on that solution,
thus each variable is instantiated to a value, and the method returns ``true``.

If the method returns ``false``, two cases must be considered:

- A limit has been reached.
  There may be a solution, but the solver has not been able to find it in the given limit
  or there is no solution but the solver has not been able to prove it (i.e., to close to search tree) in the given limit.
  The resolution process stops in no particular place in the search tree and the resolution can be run again.
- No limit has been declared. The problem has no solution, the complete exploration of the search tree proved it.

To ensure the problem has no solution, one may call ``solver.hasReachedLimit()``.
It returns ``true`` if a limit has been reached, ``false`` otherwise.



Enumerating solutions
^^^^^^^^^^^^^^^^^^^^^

Once the resolution has been started by a call to ``solver.findSolution()`` and if the problem is feasible,
the resolution can be resumed using ``solver.nextSolution()`` from the last solution found.
The method returns ``true`` if a new solution is found, ``false`` otherwise (a call to ``solver.hasReachedLimit()`` must confirm the lack of new solution).
If a solution has been found, alike ``solver.findSolution()``, the resolution stops on this solution,
each variable is instantiated, and the resolution can be resumed again until there is no more new solution.

One may enumerate all solution like this::

 if(solver.findSolution()){
    do{
        // do something, e.g. print out variables' value
    }while(solver.nextSolution());
 }

``solver.findSolution()`` and  ``solver.nextSolution()`` are the only ways to resume a resolution process which has already began.

.. tip::

    On a solution, one can get the value assigned to each variable by calling ::

        ivar.getValue(); // instantiation value of an IntVar, return a int
        svar.getValues(); // instantiation values of a SerVar, return a int[]
        rvar.getLB(); // lower bound of a RealVar, return a double
        rvar.getUB(); // upper bound of a RealVar, return a double



An alternative is to call ``solver.findAllSolutions()``. It attempts to find all solutions of the problem.
It returns the number of solutions found (in the given limit if any).


Optimization problems
---------------------

Choco |version| enables to solve optimization problems, that is, in which a variable must be optimized.

.. tip::

 For functions, one should declare an objective variable and declare it as the result of the function::

   // Function to maximize: 3X + 4Y
   IntVar OBJ = VF.bounded("objective", 0, 999, solver);
   solver.post(ICF.scalar(new IntVar[]{X,Y}, new int[]{3,4}, OBJ));
   solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, OBJ);


Finding one optimal solution
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Finding one optimal solution is made through a call to the ``solver.findOptimalSolution(ResolutionPolicy, IntVar)`` method.
The first argument defines the kind of optimization required: minimization (``ResolutionPolicy.MINIMIZE``)
or maximization (``ResolutionPolicy.MAXIMIZE``).
The second argument indicates the variable to optimize.

For instance::

 solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, OBJ);

states that the variable ``OBJ`` must be maximized.

The method does not return any value.
However, the best solution found so far is restored.

.. important::

 Because the best solution is restored, all variables are instantiated after a call to ``solver.findOptimalSolution(...)``.

The best solution found is the optimal one if the entire search space has been explored.

The process is the following: anytime a solution is found, the value of the objective variable is stored and a *cut* is posted.
The cut is an additional constraint which states that the next solution must be strictly better than the current one,
ie in minimization, strictly smaller.

Finding all optimal solutions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There could be more than one optimal solutions.
To find them all, one can call ``findAllOptimalSolutions(ResolutionPolicy, IntVar, boolean)``.
The two first arguments defines the optimisation policy and the variable to optimize.
The last argument states the way the solutions are computed.
Set to ``true`` the resolution will be achieved in two steps: first finding and proving an optimal solution,
then enumerating all solutions of optimal cost.
Set to ``false``, the posted cuts are *soft*.
When an equivalent solution is found, it is stored and the resolution goes on.
When a strictly better solution is found, previous solutions are removed.
Setting the boolean to ``false`` allow finding non-optimal intermediary solutions, which may be time consuming.



Multi-objective optimization problems
-------------------------------------

Finding the pareto front
^^^^^^^^^^^^^^^^^^^^^^^^

It is possible to solve a multi-objective optimization problems with Choco |version|,
using ``solver.findParetoFront(ResolutionPolicy policy, IntVar... objectives)``.
The first argument define the resolution policy, which can be ``Resolution.MINIMIZE`` or ``ResolutionPolicy.MAXIMIZE``.
Then, the second argument defines the list of variables to optimize.

.. note::

 All variables should respect the same resolution policy.

The underlying approach is naive, but it simplifies the process.
Anytime a solution is found, a cut is posted which states that at least one of the objective variables must be better.
Such as :math:`(X_0 < b_0 \lor X_1 < b_1 \lor \ldots \lor X_n < b_n` where :math:`X_i` is the ith objective variable and :math:`b_i`
its best known value.

The method ends by restoring the last solution found so far, if any.

Here is a simple illustration:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/Pareto.java
   :language: java
   :lines: 71,72,73,75,83,88-92
   :linenos:

Propagation
-----------

One may want to propagate each constraint manually.
This can be achieved by calling ``solver.propagate()``.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a ``ContradictionException`` if a contradiction occurs.
In that case, the propagation engine must be flushed calling ``solver.getEngine().flush()``
to ensure there is no pending events.

.. warning::

 If there are still pending events in the propagation engine, the propagation may results in unexpected results.