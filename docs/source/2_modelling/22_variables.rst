.. _22_variables_label:

Declaring variables
===================

Principle
---------

A variable is an *unknown*, mathematically speaking.
The goal of a resolution is to *assign* a *value* to each declared variable.
In Constraint Programming, the *domain* --set of values that a variable can initially take-- must be defined.


Choco |version| includes five types of variables: ``IntVar``, ``BoolVar``, ``SetVar``, ``GraphVar`` and ``RealVar``.
A factory is available to ease the declaration of variables: ``VariableFactory`` (or ``VF`` for short).
At least, a variable requires a name and a solver to be declared in.
The name is only helpful for the user, to read the results computed.

Integer variable
----------------

An integer variable is based on domain made with integer values. 
There exists under three different forms: **bounded**, **enumerated** or **boolean**.
An alternative is to declare variable-based views.

Bounded variable
^^^^^^^^^^^^^^^^

Bounded (integer) variables take their value in :math:`[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a < b` (the case where :math:`a = b` is handled through views). 
Those variables are pretty light in memory (the domain requires two integers) but cannot represent holes in the domain.

To create a bounded variable, the ``VariableFactory`` should be used: ::

 IntVar v = VariableFactory.bounded("v", 1, 12, solver);

To create an array of 5 bounded variables of initial domain :math:`[\![-2,8]\!]`: ::

 IntVar[] vs = VariableFactory.boundedArray("vs", 5, -2, 8, solver);

To create a matrix of 5x6 bounded variables of initial domain :math:`[\![0,5]\!]` : ::

 IntVar[][] vs = VariableFactory.boundedMatrix("vs", 5, 6, 0, 5, solver);

.. note::
   When using bounded variables, branching decisions must either be domain splits or bound assignments/removals.
   Indeed, assigning a bounded variable to a value strictly comprised between its bounds may results in disastrous performances,
   because such branching decisions will not be refutable.

Enumerated variable
^^^^^^^^^^^^^^^^^^^

Integer variables with enumerated domains, or shortly, enumerated variables, take their value in :math:`[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a < b` (the case where :math:`a = b` is handled through views) or in an array of ordered values :math:`{a,b,c,..,z}`, where :math:`a < b < c ... < z`.
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


.. admonition:: **Modelling**: Bounded or Enumerated?

    The choice of representation of the domain variables should not be done lightly.
    Not only the memory consumption should be considered but also the type of constraints used.
    Indeed, some constraints only update bounds of integer variables, using them with bounded variables is enough.
    Others make holes in variables' domain, using them with enumerated variables takes advantage of the *power* of the filtering algorithm.
    Most of the time, variables are associated with propagators of various *power*.
    The choice of domain representation must then be done on a case by case basis.



Boolean variable
^^^^^^^^^^^^^^^^

Boolean variables, BoolVar, are specific ``IntVar`` which take their value in :math:`[\![0,1]\!]`.

To create a new boolean variable: ::

 BoolVar b = VariableFactory.bool("b", solver);

To create an array of 5 boolean variables: ::

 BoolVar[] bs = VariableFactory.boolArray("bs", 5, solver);

To create a matrix of 5x6 boolean variables: ::

 BoolVar[] bs = VariableFactory.boolMatrix("bs", 5, 6, solver);


.. _22_variables_constant_label:

Constants
---------

Fixed-value integer variables should be created with a call to the following functions: ::

 VariableFactory.fixed("seven", 7, solver);

Or: ::

 VariableFactory.fixed(8, Solver)

where 7 and 8 are the constant values.
Not specifying a name to a constant enables the solver to use *cache* and avoid multiple occurrence of the same object in memory.



.. _22_variables_view_label:

Variable views
--------------

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
------------

A set variable ``SV`` represents a set of integers.
Its domain is defined by a set interval: ``[S_E,S_K]``

- the envelope ``S_E`` is an ``ISet`` object which contains integers that potentially figure in at least one solution,
- the kernel ``S_K`` is an ``ISet`` object which contains integers that figure in every solutions.

Initial values for both ``S_K`` and ``S_E`` can be specified. If no initial value is given for ``S_K``, it is empty by default.
Then, decisions and filtering algorithms will remove integers from ``S_E`` and add some others to ``S_K``.
A set variable is instantiated if and only if ``S_E = S_K``.

A set variable can be created as follows: ::

    // z initial domain
    int[] z_envelope = new int[]{2,1,3,5,7,12};
    int[] z_kernel = new int[]{2};
    z = VariableFactory.set("z", z_envelope, z_kernel, solver);


Graph variable
--------------

A graph variable ``GV`` is a kind of set variable designed to model graphs.
Its domain is defined by a graph interval: ``[G_E,G_K]``

- the envelope ``G_E`` is a graph object which contains nodes/arcs that potentially figure in at least one solution,
- the kernel ``G_K`` is a graph object which contains nodes/arcs that figure in every solutions.

Initially ``G_K`` is empty while ``G_E`` is set to an initial domain.
Then, decisions and filtering algorithms will remove nodes or arcs from ``G_E`` and add some others to ``G_K``. 
A graph variable ``GV=(G_E,G_K)`` is instantiated if and only if ``G_E = G_K``.

We distinguish two kind of graph variables, ``DirectedGraphVar`` and ``UndirectedGraphVar``.
Then for each kind, several data structures are available and can be found in enum ``GraphType``. 
For instance ``BITSET`` involves a bitset representation while ``LINKED_LIST`` involves linked lists and is much more appropriate for sparse graphs.


Real variable
-------------

Real variables have a specific status in Choco |version|.
Indeed, continuous variables and constraints are managed with `Ibex solver`_.

A real variable is declared with two doubles which defined its bound: ::

 RealVar x = VariableFactory.real("y", 0.2d, 1.0e8d, 0.001d, solver);

Or a real variable can be declared on the basis of on integer variable: ::

 IntVar ivar = VariableFactory.bounded("i", 0, 4, solver);
 RealVar x = VariableFactory.real(ivar, 0.01d);

.. _Ibex solver: http://www.emn.fr/z-info/ibex/

