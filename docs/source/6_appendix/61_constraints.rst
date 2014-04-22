todo

Graph constraints
~~~~~~~~~~~~~~~~~

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

 Relation graphs can be seen as a kind of reification but they require only 1 graph variable and :math:`O(1)` propagators running in :math:`O(n^2)` time,
 whereas a reified approach would imply :math:`n^2` boolean variables and propagators. Moreover, relation graphs treat the problem globally through graph theory's algorithms.