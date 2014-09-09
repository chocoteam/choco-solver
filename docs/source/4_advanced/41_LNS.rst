.. _41_LNS_label:

Large Neighborhood Search (LNS)
===============================

Local search techniques are very effective to solve hard optimization problems.
Most of them are, by nature, incomplete.
In the context of constraint programming (CP) for optimization problems, one of the most well-known and widely used local search techniques is the Large Neighborhood Search (LNS) algorithm [#q1]_.
The basic idea is to iteratively relax a part of the problem, then to use constraint programming to evaluate and bound the new solution.


.. [#q1] Paul Shaw. Using constraint programming and local search methods to solve vehicle routing problems. In Michael Maher and Jean-Francois Puget, editors, *Principles and Practice of Constraint Programming, CP98*, volume 1520 of *Lecture Notes in Computer Science*, pages 417–431. Springer Berlin Heidelberg, 1998.

Principle
---------

LNS is a two-phase algorithm which partially relaxes a given solution and repairs it.
Given a solution as input, the relaxation phase builds a partial solution (or neighborhood) by choosing a set of variables to reset to their initial domain;
The remaining ones are assigned to their value in the solution.
This phase is directly inspired from the classical Local Search techniques.
Even though there are various ways to repair the partial solution, we focus on the technique in which Constraint Programming is used to bound the objective variable and
to assign a value to variables not yet instantiated.
These two phases are repeated until the search stops (optimality proven or limit reached).

The ``LNSFactory`` provides pre-defined configurations.
Here is the way to declare LNS to solve a problem: ::

    LNSFactory.rlns(solver, ivars, 30, 20140909L, new FailCounter(100));
    solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);

It declares a *random* LNS which, on a solution, computes a partial solution based on ``ivars``.
If no solution are found within 100 fails (``FailCounter(100)``), a restart is forced.
Then, every ``30`` calls to this neighborhood, the number of fixed is randomly picked.
``20140909L`` is the seed for the ``java.util.Random``.


The instruction ``LNSFactory.rlns(solver, vars, level, seed, frcounter)`` runs:

.. literalinclude:: /../../choco-solver/src/main/java/solver/search/loop/lns/LNSFactory.java
   :language: java
   :lines: 112-114

The factory provides other LNS configurations together with built-in neighbors.

Neighbors
---------

While the implementation of LNS is straightforward, the main difficulty lies in the design of neighborhoods able to move the search further.
Indeed, the balance between diversification (i.e., evaluating unexplored sub-tree) and intensification (i.e., exploring them exhaustively) should be well-distributed.


Generic neighbors
^^^^^^^^^^^^^^^^^

One drawback of LNS is that the relaxation process is quite often problem dependent.
Some works have been dedicated to the selection of variables to relax through general concept not related to the class of the problem treated [5,24].
However, in conjunction with CP, only one generic approach, namely Propagation-Guided LNS [24], has been shown to be very competitive with dedicated ones on a variation of the Car Sequencing Problem.
Nevertheless, such generic approaches have been evaluated on a single class of problem and need to be thoroughly parametrized at the instance level, which may be a tedious task to do.
It must, in a way, automatically detect the problem structure in order to be efficient.


Combining neighborhoods
^^^^^^^^^^^^^^^^^^^^^^^

There are two ways to combine neighbors.

Sequential
""""""""""

Declare an instance of ``SequenceNeighborhood(n1, n2, ..., nm)``.
Each neighbor ni is applied in a sequence until one of them leads to a solution.
At step k, the :math:`(k \mod m)^{th}` neighbor is selected.
The sequence stops if at least one of the neighbor is complete.

Adaptive
""""""""

Declare an instance of ``AdaptiveNeighborhood(1L, n1, n2, ..., nm)``.
At the beginning a weight of 1 at assigned to each neighbor ni.
Then, if a neighbor leads to solution, its weight :math:`w_i` is increased by 1.
Any time a partial solution has to be computed, a value ``W`` between 1 and :math:`w_1+w_2+...+w_n` is randomly picked (``1L`` is the seed).
Then the weight of each neighbor is subtracted from ``W``, as soon as ``W``:math:`\leq 0`, the corresponding neighbor is selected.
For instance, let's consider three neighbors n1, n2 and n3, their respective weights w1=2, w2=4, w3=1.
``W`` = 3  is randomly picked between 1 and 7.
Then, the weight of n1 is subtracted, ``W``2-=1; the weight of n2 is subtracted, ``W``-4 = -3, ``W`` is less than 0 and n2 is selected.


Defining its own neighborhoods
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

One can define its own neighbor by extending the abstract class ``ANeighbor``.
It forces to implements the following methods:

+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| **Method**                                                             |   **Definition**                                                                                                       |
+========================================================================+========================================================================================================================+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``void recordSolution()``                                              | Action to perform on a solution (typicallu, storing the current variables' value).                                     |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``void fixSomeVariables(ICause cause) throws ContradictionException``  | Fix some variables to their value in the last solution, computing a partial solution.                                  |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``void restrictLess()``                                                | Relax the number of variables fixed. Called when no solution was found during a LNS run (trapped into a local optimum).|
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+
| ``boolean isSearchComplete()``                                         | Indicates whether the neighbor is complete, that is, can end.                                                          |
+------------------------------------------------------------------------+------------------------------------------------------------------------------------------------------------------------+

Restarts
--------

A generic and common way to reinforce diversification of LNS is to introduce restart during the search process.
This technique has proven to be very flexible and to be easily integrated within standard backtracking procedures [#q2]_.

.. [#q2] Laurent Perron. Fast restart policies and large neighborhood search. In Francesca Rossi, editor, *Principles and Practice of Constraint Programming at CP 2003*, volume 2833 of *Lecture Notes in Computer Science*. Springer Berlin Heidelberg, 2003.


Walking
-------

A complementary technique that appear to be efficient in practice is named `Walking` and consists in accepting equivalent intermediate solutions in a search iteration instead of requiring a strictly better one.
This can be achieved by defining an ``ObjectiveManager`` like this: ::

    solver.set(new ObjectiveManager(objective, ResolutionPolicy.MAXIMIZE, false));

Where the last parameter, named ``strict`` must be set to false to accept equivalent intermediate solutions.

