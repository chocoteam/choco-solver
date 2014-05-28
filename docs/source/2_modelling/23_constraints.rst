Declaring Constraints
=====================


Constraints define restrictions over variables that must be respected in order to get a feasible solution. 
A Constraint contains several propagators that will perform filtering over variables' domains  and may define its specific checker through the method ``isSatisfied()``.

For instance, if we want an integer variable ``sum`` to be equal to the sum of values of variables in the set ``atLeast``, we can use the ``IntConstraintFactory.sum`` constraint:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/CarSequencing.java
   :language: java
   :lines: 104

A constraint may define its specific checker through the method ``isSatisfied()``, but most of the time the checker is given by checking the entailment of each of its propagators. 
The satisfaction of the constraints' solver is done on each solution if assertions are enabled.

.. note:: 
   One can enable assertions by adding the ``-ea`` instruction in the JVM arguments.

It can thus be slower if the checker is often called (which is not the case in general). 
The advantage of this framework is the economy of code (less constraints need to be implemented), the avoidance of useless redundancy when several constraints use the same propagators (for instance ``IntegerChanneling`` constraint involves ``AllDifferent constraint``), which leads to better performances and an easier maintenance.

A constraint should either be posted or be reified.

Posting constraints
~~~~~~~~~~~~~~~~~~~

To be effective, a constraint must be posted to the solver. This is achieved using the method: ::
 
 solver.post(Constraint cstr);

Otherwise, if the ``solver.post(Constraint cstr)`` method is not called, the constraint will not be taken into account during the resolution process : it may not be satisfied in all solutions.

Reifying constraints
~~~~~~~~~~~~~~~~~~~~

In Choco |version|, it is possible to reify any constraint. Reifying a constraint means associating it with a ``BoolVar`` to represent whether the constraint holds or not: ::

 constraint.reifyWith(BoolVar b);

This means we allow the constraint not to be satisfied. Therefore, it should not be posted. 

The ``LogicalConstraintFactory`` enables to manipulate constraints through their reification. For instance, we can represent the constraint "either ``x<0`` or ``y>42``" as the following: ::

 Constraint a = IntConstraintFactory.arithm(x,"<",0);
 Constraint b = IntConstraintFactory.arithm(y,">",42);
 Constraint c = LogicalConstraintFactory.or(a,b);
 solver.post(c);

This will actually reify both constraints a and b and say that at least one of the corresponding boolean variables must be true.
Note that only the constraint c is posted.


