Posting constraints
===================

Constraints define restrictions over variables that must be respected in order to get a feasible solution. 
A Constraint contains several propagators that will perform filtering over variables' domains  and may define its specific checker through the method ``isSatisfied()``.

For instance, if we want an integer variable ``sum`` to be equal to the sum of values of variables in the set ``atLeast``, we can use the ``IntConstraintFactory.sum`` constraint:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/CarSequencing.java
   :language: java
   :lines: 104
   
To be effective, this constraint must be declared in the solver.
This achieves using the mehtod::
 solver.post(Constraint cstr);


A constraint may define its specific checker through the method ``isSatisfied()``, but most of the time the checker is given by checking the entailment of each of its propagators. 
The satisfaction of the constraints' solver is done on each solution if assertions are enabled.

.. note:: 
   One can enable assertions by adding the ``-ea`` instruction in the JVM arguments.


It can thus be slower if the checker is often called (which is not the case in general). 
The advantage of this framework is the economy of code (less constraints need to be implemented), the avoidance of useless redundancy when several constraints use the same propagators (for instance ``IntegerChanneling`` constraint involves ``AllDifferent constraint``), which leads to better performances and an easier maintenance.

Arithmetical constraints
~~~~~~~~~~~~~~~~~~~~~~~~

Binary constraints
~~~~~~~~~~~~~~~~~~

Ternary constraints
~~~~~~~~~~~~~~~~~~~
