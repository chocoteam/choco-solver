Declaring variables
===================

Choco |version| includes five types of variables: ``IntVar``, ``BoolVar``, ``SetVar``, ``GraphVar`` and ``RealVar``.
A factory is available to ease the declaration of variables: ``VariableFactory``.
At least, a varible requires a name and a solver to be declared in.
The name is only helpful for the user, to read the results computed.


Integer variable
~~~~~~~~~~~~~~~~

An integer variable is based on domain made with integer values. 
There exists under three different forms: **bounded**, **enumerated** or **boolean**.
An alternative is to declare variable-based views.


Bounded variable
----------------

Bounded (integer) variables take their value in :math:`[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a < b` (the case where :math:`a = b` is handled through views). 
Those variables are pretty light in memory (the domain requires two integers) but cannot represent holes in the domain.

To create a bounded variable, the ``VariableFactory`` should be used: ::

 IntVar v = VariableFactory.bounded("v", 1, 12, solver);

To create an array of 5 bounded variables of initial domain :math:`[\![-2,8]\!]`: ::

 IntVar[] vs = VariableFactory.boundedArray("vs", 5, -2, 8, solver);

To create a matrix of 5x6 bounded variables of initial domain :math:`[\![0,5]\!]` : ::

 IntVar[][] vs = VariableFactory.boundedMatrix("vs", 5, 6, 0, 5, solver);

Enumerated variable
-------------------

Integer variables with enumerated domains, or shortly, enumerated variables, take their value in `[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a < b` (the case where :math:`a = b` is handled through views) or in an array of ordered values :math:`{a,b,c,..,z}`, where :math:`a < b < c ... < z`. 
Enumerated variables provide more information than bounded variables but are heavier in memory (usually the domain requires a bitset).

To create an enumerated variable, the ``VariableFactory`` should be used: ::

 IntVar v = VariableFactory.enumerated("v", 1, 12, solver);

which is equivalent to : ::

 IntVar v = VariableFactory.enumerated("v", new int[]{1,2,3,4,5,6,7,8,9,10,11,12}, solver);

To create a variable with holes in its initial domain: ::

 IntVar v = VariableFactory.enumerated("v", new int[]{1,7,8}, solver);

To create an array of 5 enumerated variables with same domains: ::

 IntVar[] vs = VariableFactory.enumeratedArray("vs", 5, -2, 8, solver);

 IntVar[] vs = VariableFactory.enumeratedArray("vs", 5, new int[]{-10, 0, 10}, solver);

To create a matrix of 5x6 enumerated variables with same domains: ::

 IntVar[][] vs = VariableFactory.enumeratedMatrix("vs", 5, 6, 0, 5, solver);

 IntVar[][] vs = VariableFactory.enumeratedMatrix("vs", 5, 6, new int[]{1,2,3,5,6,99}, solver);

Boolean variable
-----------------

Boolean variables, BoolVar, are specific ``IntVar`` which take their value in :math:`[\![0,1]\!]`.

To create a new boolean variable: ::

 BoolVar b = VariableFactory.bool("b", solver);

To create an array of 5 boolean variables: ::

 BoolVar[] bs = VariableFactory.boolArray("bs", 5, solver);

To create a matrix of 5x6 boolean variables: ::

 BoolVar[] bs = VariableFactory.boolMatrix("bs", 5, 6, solver);


Variable's view
---------------

Views are particular integer variables, they can be used inside constraints. 
Their domains are implicitly defined by a function and implied variables.

``x`` is a constant : ::
 
 IntVar x = Views.fixed(1, solver);

``x = y + 2`` : ::
 
 IntVar x = Views.offset(y, 2);

``x = -y`` : ::
 
 IntVar x = Views.minus(y);

``x = 3*y`` : ::

 IntVar x = Views.scale(y, 3);

Views can be combined together: ::
 
 IntVar x = Views.offset(Views.scale(y,2),5);



Set variable
~~~~~~~~~~~~

Graph variable
~~~~~~~~~~~~~~

A graph variable ``GV`` is a kind of set variable designed to model graphs. It is defined by two graphs:

- the envelope ``G_E`` contains nodes/arcs that potentially figure in at least one solution,
- the kernel ``G_K`` contains nodes/arcs that figure in every solutions.

Initially ``G_K`` is empty while ``G_E`` is set to an initial domain.
Then, decisions and filtering algorithms will remove nodes or arcs from ``G_E`` and add some others to ``G_K``. 
A graph variable ``GV=(G_E,G_K)`` is instantiated if`and only if ``G_E = G_K``.

We distinguish two kind of graphs, ``DirectedGraphVar`` and ``UndirectedGraphVar``. 
Then for each kind, several data structures are available and can be found in enum ``GraphType``. 
For instance ``MATRIX`` involves a bitset representation while ``LINKED_LIST`` involves linked lists and is much more appropriate for sparse graphs.

**Reification graph**

 A graph variable ``GV=(G_E,G_K)`` can be used to model a matrix ``B`` of boolean variables.
 
 - Each arc ``(x,y)`` corresponds to the boolean variable ``B[x][y]``,
 - ``(x,y)`` not in ``G_E`` => ``B[x][y]`` is ``false```,
 - ``(x,y)`` in ``G_K`` => ``B[x][y]`` is ``true``.

 This channeling is very easy to set: ::

  UndirectedGraphVar GV = new UndirectedGraphVar(B.length);
  // create an empty default constraint
  Constraint c = ConstraintFactory.makeConstraint(solver);
  // channeling between B and GV
  c.addPropagator(PropagatorFactory.graphBooleanChanneling(GV,B,solver); 

**Relation graph**

 A graph variable ``GV=(G_E,G_K)`` can be used to model a binary relation ``R`` between a set of variables ``V``.

 - Each node ``x`` represents the variable ``V[x]``. If ``x`` is not in ``G_E`` then it is not concerned by the relation ``R``.
 - Each arc ``(x,y)`` of ``G_E`` represents the potential application of ``xRy``.
 - Each arc (x,y) not in G_E represents either x(!R)y, either nothing (depending of whether !R is defined or not, like reifications and half reifications).
 - Each arc (x,y) of G_K implies the application of xRy.

 For instance the global constraint NValue(V,N) which ensures that variables in V take exactly N different values can be reformulated by: ::
 
  // the meaning of an arc is the equivalence relation
  GraphRelation relation = GraphRelationFactory.equivalence(V); 
  UndirectedGraphVar GV = new UndirectedGraphVar(V.length);
  // the graph GVmust contain Ncliques
  Constraint nValues = GraphConstraintFactory.nCliques(GV,N,solver);i
  // channeling between V and GV
  nValues.addPropagator(PropagatorFactory.graphRelationChanneling(GV,V,R,solver); 

 The good thing is that such a model remains valid en case of vectorial variables (``NVector`` constraint).

 Relation graphs can be seen as a kind of reification but they require only 1 graph variable and :math:`O(1)` propagators running in :math:`O(n^2)` time, whereas a reified approach would imply :math:`n^2` boolean variables and propagators. Moreover, relation graphs treat the problem globally through graph theory's algorithms.

Real variable
~~~~~~~~~~~~~

Real variables have a specific status in Choco |version|.
Indeed, continuous variables and constraints are managed with `Ibex solver`_.

A real variable is declared with two doubles which defined its bound: ::

 RealVar x = VariableFactory.real("y", 0.2, 1.0e8, precision, solver);


.. literalinclude:: /../../choco-samples/src/main/java/samples/real/Grocery.java
   :language: java
   :lines: 65,70-79,86,93-94
   :linenos:
 

.. _Ibex solver: http://www.emn.fr/z-info/ibex/

