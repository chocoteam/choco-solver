.. _23_constraints_label:

Constraints and propagators
===========================

Principle
---------

A constraint is a logic formula that defines allowed combinations of values for its variables, that is, restrictions over variables that must be respected in order to get a feasible solution.
A constraint is equipped with a (set of) filtering algorithm(s), named *propagator(s)*.
A propagator **removes**, from the domains of the targeted variables, values that cannot correspond to a valid combination of values.
A solution of a problem is an assignment of all its variables simultaneously verifying all the constraints.

Constraint can be declared in *extension*, by defining the valid/invalid tuples, or in *intension*, by defining a relation between the variables.
Choco |version| provides various factories to declare constraints (see :ref:`Overview <12_overview_label>` to have a list of available factories).
A list of constraints available through factories is given in :ref:`List of available constraints <61_constraints_label>`.

.. admonition:: **Modelling**: Selecting the right constraints

    Constraints, through propagators, suppress forbidden values of the domain of the variables.
    For a given paradigm, there can be several propagators available.
    A widely used example is the `AllDifferent` constraints which holds that all its variables should take a distinct value in a solution.
    Such a rule can be formulated :

    - using a clique of inequality constraints,
    - using a global constraint: either analysing bounds of variable (*Bound consistency*) or analysing all values of the variables (*Arc consistency*),
    - or using a table constraint --an extension constraint which list the valid tuples.

    The choice must be made by not only considering the gain in expressiveness of stress compared to others.
    Indeed, the effective yield of each option can be radically different as the efficiency in terms of computation time.

    Many global constraints are used to model problems that are inherently NP-complete.
    And only a partial domain filtering variables can be done through a polynomial algorithm.
    This is for example the case of `NValue` constraint that one aspect relates to the problem of "minimum hitting set."
    Finally, the *global* nature of this type of constraint also simplifies the work of the solver in that it provides all or part of the structure of the problem.



If we want an integer variable ``sum`` to be equal to the sum of values of variables in the set ``atLeast``, we can use the ``IntConstraintFactory.sum`` constraint:

.. literalinclude:: /../../choco-samples/src/main/java/samples/integer/CarSequencing.java
   :language: java
   :lines: 104

A constraint may define its specific checker through the method ``isSatisfied()``, but most of the time the checker is given by checking the entailment of each of its propagators.
The satisfaction of the constraints' solver is done on each solution if assertions are enabled.

.. note::
   One can enable assertions by adding the ``-ea`` instruction in the JVM arguments.

It can thus be slower if the checker is often called (which is not the case in general).
The advantage of this framework is the economy of code (less constraints need to be implemented), the avoidance of useless redundancy when several constraints use the same propagators (for instance ``IntegerChanneling`` constraint involves ``AllDifferent constraint``), which leads to better performances and an easier maintenance.

.. note::

    To ease modelling, it is not required to manipulate propagators, but only constraints. However, one can define specific constraints by defining combinations of propagators and/or its own propagators. More detailed are given in :ref:`Defining its own constraint <46_define_constraint_label>`.


Choco |version| provides various types of constraints: :ref:`unary constraints <61_constraints_unaries_label>`, :ref:`binary constraints <61_constraints_binaries_label>`, :ref:`ternary constraints <61_constraints_ternaries_label>` and :ref:`global constraints <61_constraints_global_label>`.
A constraint should either be posted or be reified.


Posting constraints
-------------------

To be effective, a constraint must be posted to the solver. This is achieved using the method: ::
 
 solver.post(Constraint cstr);

Otherwise, if the ``solver.post(Constraint cstr)`` method is not called, the constraint will not be taken into account during the resolution process : it may not be satisfied in all solutions.

+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **Method**                                    | **Definition**                                                                                                                                                                  |
+===============================================+=================================================================================================================================================================================+
| ``void post(Constraint c)``                   | Post **permanently** a constraint in the constraint network defined by the solver.                                                                                              |
|                                               | The constraint is not propagated on posting, but is added to the propagation engine.                                                                                            |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``void post(Constraint... cs)``               | Post **permanently** the constraints in the constraint network defined by the solver.                                                                                           |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``void postTemp(Constraint c)``               | Post a constraint **temporary** in the constraint network.                                                                                                                      |
|                                               | The constraint will active on the current sub-tree and be removed upon backtrack.                                                                                               |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``void unpost(Constraint c)``                 | Remove permanently the constraint from the constraint network                                                                                                                   |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+


Reifying constraints
--------------------

In Choco |version|, it is possible to reify any constraint. Reifying a constraint means associating it with a ``BoolVar`` to represent whether the constraint holds or not: ::

 BoolVar b = constraint.reify();

Or: ::

 BoolVar b = VF.bool("b", solver);
 constraint.reifyWith(b);

The first API ``constraint.reify()`` creates the variable, if it does not already exists, and reify the constraint.
The second API ``constraint.reifyWith(b)`` reify the constraint with the given variable.

.. note::

    A constraint is reified with only one boolean variable. If multiple reification are required, equality constraints will be created.


Reifying a constraint means that we allow the constraint not to be satisfied. Therefore, the constraint **should not** be posted.

The ``LogicalConstraintFactory`` enables to manipulate constraints through their reification.
For instance, we can represent the constraint "either ``x<0`` or ``y>42``" as the following: ::

 Constraint a = IntConstraintFactory.arithm(x,"<",0);
 Constraint b = IntConstraintFactory.arithm(y,">",42);
 Constraint c = LogicalConstraintFactory.or(a,b);
 solver.post(c);

This will actually reify both constraints ``a`` and ``b`` and say that at least one of the corresponding boolean variables must be true.
Note that only the constraint ``c`` is posted.


