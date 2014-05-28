Overview of Choco |version|
===========================

The following steps should be enough to start using Choco |version|.
The minimal problem should at least contains a solver, some variables and constraints to linked them together. 

To facilitate the modeling, Choco |version| provides factories for almost every required component of CSP and its resolution:


+------------------------------+--------------+-------------------------------------------+
| **Factory**                  | **Shortcut** |  **Enables to create**                    |
+==============================+==============+===========================================+
| ``VariableFactory``          | VF           | Variables and views                       |
|                              |              | (integer, boolean, set, graph and real)   |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``IntConstraintFactory``     | ICF          | Constraints over variables                |
+------------------------------+--------------+-------------------------------------------+
| ``SetConstraintFactory``     | SCF          |                                           |
+------------------------------+--------------+-------------------------------------------+
| ``GraphConstraintFactory``   | GCF          |                                           |
+------------------------------+--------------+-------------------------------------------+
| ``LogicalConstraintFactory`` | LCF          | (Manages constraint reification)          |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``IntStrategyFactory``       | ISF          | Custom or black-box search strategies     |
+------------------------------+--------------+-------------------------------------------+
| ``SetStrategyFactory``       | SSF          |                                           |
+------------------------------+--------------+-------------------------------------------+
| ``GraphStrategyFactory``     | GSF          |                                           |
+------------------------------+--------------+-------------------------------------------+
+------------------------------+--------------+-------------------------------------------+
| ``SearchMonitorFactory``     | SMF          | log, resolution limits, restarts etc.     |
+------------------------------+--------------+-------------------------------------------+

Note that, in order to have a concise and readable model, factories have shortcut names. Furthermore, they can be imported in a static way.

Let say we want to model and solve the following equation: :math:`x + y < 5`, where the :math:`x \in [\![0,5]\!]` and :math:`y \in [\![0,5]\!]`.
Here is a short example which illustrates the main steps of a CSP modeling and resolution with Choco |version| to treat this equation.

 

.. literalinclude:: /../../choco-samples/src/test/java/docs/Overview.java
   :language: java
   :lines: 44-54
   :emphasize-lines: 44,45
   :linenos:




One may notice that there is no distinction between model objects and solver objects. This makes easier for beginners to model and solve problems (reduction of concepts and terms to know) and for developers to implement their own constraints and strategies (short cutting process).

Don't be afraid to take a look at the sources, we think it is a good start point.
