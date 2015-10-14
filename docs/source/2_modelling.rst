******************
Modelling problems
******************

.. _21_solver_label:

The solver
==========

The object :code:`Solver` is the key component. It is built as following: ::

 Solver solver = new Solver();

or::

 Solver solver = new Solver("my problem");


This should be the first instruction, prior to any other modelling instructions.
Indeed, a solver is needed to declare variables, and thus constraints.

Here is a list of the commonly used Solver API.

.. note::
    The API related to resolution are not described here but detailed in :ref:`Solving <31_solving_label>`.
    Similarly, API provided to add a constraint to the solver are detailed in :ref:`Constraints <23_constraints_label>`.
    The other missing methods are only useful for internal behavior.

Getters
-------

Variables
^^^^^^^^^

+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **Method**                                    | **Definition**                                                                                                                                                               |
+===============================================+==============================================================================================================================================================================+
| ``Variable[] getVars()``                      | Return the array of variables declared in the solver. It includes all type of variables declared, integer, boolean, etc. but also *fixed* variables such as ``Solver.ONE``.  |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``int getNbVars()``                           | Return the number of variables involved in the solver.                                                                                                                       |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``Variable getVar(int i)``                    | Return the :math:`i^th` variable declared in the solver.                                                                                                                     |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``IntVar[] retrieveIntVars()``                | Extract from the solver variables those which are integer (ie whose *KIND* is set to *INT*, that is, including *fixed* integer variables).                                   |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``retrieveBoolVars()``                        | Extract from the solver variables those which are boolean (ie whose *KIND* is set to *BOOL*, that is, including ``Solver.ZERO`` and ``Solver.ONE``).                         |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``SetVar[] retrieveSetVars()``                | Extract from the solver variables those which are set (ie whose *KIND* is set to *SET*)                                                                                      |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``RealVar[] retrieveRealVars()``              | Extract from the solver variables those which are real (ie whose *KIND* is set to *REAL*)                                                                                    |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``BoolVar ZERO()``                            | Return the constant "0".                                                                                                                                                     |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``BoolVar ONE()``                             | Return the constant "1".                                                                                                                                                     |
+-----------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+


Constraints
^^^^^^^^^^^

+-----------------------------------------------+---------------------------------------------------------------+
| **Method**                                    | **Definition**                                                |
+===============================================+===============================================================+
| ``Constraint[] getCstrs()``                   | Return the array of constraints posted in the solver.         |
+-----------------------------------------------+---------------------------------------------------------------+
| ``getNbCstrs()``                              | Return the number of constraints posted in the solver.        |
+-----------------------------------------------+---------------------------------------------------------------+
| ``Constraint TRUE()``                         | Return the basic "true" constraint.                           |
+-----------------------------------------------+---------------------------------------------------------------+
| ``Constraint FALSE()``                        | Return the basic "false" constraint.                          |
+-----------------------------------------------+---------------------------------------------------------------+

Other
^^^^^

+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| **Method**                                    | **Definition**                                                                                    |
+===============================================+===================================================================================================+
| ``String getName()``                          | Return the name of the solver.                                                                    |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``IMeasures getMeasures()``                   | Return a reference to the measure recorder which stores resolution statistics.                    |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``AbstractStrategy getStrategy()``            | Return a reference to the declared search strategy.                                               |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``Settings getSettings()``                    | Return the current ``Settings`` used in the solver.                                               |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``ISolutionRecorder getSolutionRecorder()``   | Return the solution recorder.                                                                     |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``IEnvironment getEnvironment()``             | Return the internal *environment* of the solver, essential to manage backtracking.                |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``ObjectiveManager getObjectiveManager()``    | Return the objective manager of the solver, needed when an objective has to be optimized.         |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``ExplanationEngine getExplainer()``          | Return the explanation engine declared, (default is *NONE*).                                      |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``IPropagationEngine getEngine()``            | Return the propagation engine of the solver, which *orchestrate* the propagation of constraints.  |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``ISearchLoop getSearchLoop()``               | Return the search loop of the solver, which guide the search process.                             |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``Variable[] getObjectives()``                |  Return the objective variables.                                                                  |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+
| ``double getPrecision()``                     | In case of real variable to optimize, a precision is required.                                    |
+-----------------------------------------------+---------------------------------------------------------------------------------------------------+

Setters
-------

+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| **Method**                                    | **Definition**                                                                                                                                            |
+===============================================+===========================================================================================================================================================+
| ``set(Settings settings)``                    | Set the settings to use while modelling and solving.                                                                                                      |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(AbstractStrategy... strategies)``       | Set a strategy to explore the search space. In case many strategies are given, they will be called in sequence.                                           |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(ISolutionRecorder sr)``                 | Set a solution recorder, and erase the previous declared one (by default, ``LastSolutionRecorder`` is declared, it only stores the last solution found.   |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(ISearchLoop searchLoop)``               | Set the search loop to use during resolution. The default one is a binary search loop.                                                                    |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(IPropagationEngine propagationEngine)`` | Set the propagation engine to use during resolution. The default one is ``SevenQueuesPropagatorEngine``.                                                  |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(ExplanationEngine explainer)``          | Set the explanation engine to use during resolution. The default one is ``ExplanationEngine`` which does nothing.                                         |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(ObjectiveManager om)``                  | Set the objective manager to use during the resolution. *For advanced usage only*.                                                                        |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``setObjectives(Variable... objectives)``     | Define the variables to optimize (most of the time, only one variable is declared).                                                                       |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+
| ``set(ObjectiveManager om)``                  | In case of real variable to optimize, a precision is required.                                                                                            |
+-----------------------------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+


Others
------

+---------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------+
| **Method**                                        | **Definition**                                                                                                                       |
+===================================================+======================================================================================================================================+
| ``Solver duplicateModel()``                       | Duplicate the model associated with a solver, ie only variables and constraints, and return a new solver.                            |
+---------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------+
| ``void makeCompleteSearch(boolean isComplete)``   | Add a strategy to the declared one in order to ensure that all variables are covered by (at least) one strategy.                     |
+---------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------+
| ``void plugMonitor(ISearchMonitor sm)``           | Put a :ref:`search monitor <44_monitors_label>` to react on search events (solutions, decisions, fails, ...).                        |
+---------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------+
| ``void plugMonitor(FilteringMonitor fm)``         | Add an filtering monitor, that is an object that is kept informed of all (propagation) events generated during the resolution.       |
+---------------------------------------------------+--------------------------------------------------------------------------------------------------------------------------------------+


.. _22_variables_label:

Declaring variables
===================

Principle
---------

A variable is an *unknown*, mathematically speaking.
The goal of a resolution is to *assign* a *value* to each declared variable.
In Constraint Programming, the *domain* --set of values that a variable can initially take-- must be defined.


Choco |version| includes five types of variables: ``IntVar``, ``BoolVar``, ``SetVar`` and ``RealVar``.
A factory is available to ease the declaration of variables: ``VariableFactory`` (or ``VF`` for short).
At least, a variable requires a name and a solver to be declared in.
The name is only helpful for the user, to read the results computed.

Integer variable
----------------

An integer variable is based on domain made with integer values.
There exists under three different forms: **bounded**, **enumerated** or **boolean**.
An alternative is to declare variable-based views.

.. important::

    It is strongly recommended to not define unbounded domain like ``[Integer.MIN_VALUE, Integer.MAX_VALUE]``.
    Indeed, such domain definition may lead to :

    - incorrect domain size (``Integer.MAX_VALUE - Integer.MIN_VALUE +1 = 0``)
    - and to numeric overflow/underflow operations during propagation.

    If *undefined* domain is really required, the following range should be considered:
    ``[VariableFactory.MIN_INT_BOUND, VariableFactory.MAX_INT_BOUND]``.
    Such an interval defines `42949673` values, from `-21474836` to `21474836`.

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
    Not only the memory consumption should be considered but also the used constraints.
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

 IntVar x = VariableFactory.fixed(1, solver);

``x = y + 2`` : ::

 IntVar x = VariableFactory.offset(y, 2);

``x = -y`` : ::

 IntVar x = VariableFactory.minus(y);

``x = 3*y`` : ::

 IntVar x = VariableFactory.scale(y, 3);

Views can be combined together: ::

 IntVar x = VariableFactory.offset(VariableFactory.scale(y,2),5);

Set variable
------------

A set variable ``SV`` represents a set of integers.
Its domain is defined by a set interval: ``[S_E,S_K]``

- the envelope ``S_E`` is an ``ISet`` object which contains integers that potentially figure in at least one solution,
- the kernel ``S_K`` is an ``ISet`` object which contains integers that figure in every solution.

Initial values for both ``S_K`` and ``S_E`` can be specified. If no initial value is given for ``S_K``, it is empty by default.
Then, decisions and filtering algorithms will remove integers from ``S_E`` and add some others to ``S_K``.
A set variable is instantiated if and only if ``S_E = S_K``.

A set variable can be created as follows: ::

    // z initial domain
    int[] z_envelope = new int[]{2,1,3,5,7,12};
    int[] z_kernel = new int[]{2};
    z = VariableFactory.set("z", z_envelope, z_kernel, solver);


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
A list of constraints available through factories is given in :ref:`List of available constraints <23_constraints_label>`.

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

.. literalinclude:: /../../choco-samples/src/main/java/org/chocosolver/samples/integer/CarSequencing.java
   :language: java
   :lines: 107

A constraint may define its specific checker through the method ``isSatisfied()``, but most of the time the checker is given by checking the entailment of each of its propagators.
The satisfaction of the constraints' solver is done on each solution if assertions are enabled.

.. note::
   One can enable assertions by adding the ``-ea`` instruction in the JVM arguments.

It can thus be slower if the checker is often called (which is not the case in general).
The advantage of this framework is the economy of code (less constraints need to be implemented), the avoidance of useless redundancy when several constraints use the same propagators (for instance ``IntegerChanneling`` constraint involves ``AllDifferent constraint``), which leads to better performances and an easier maintenance.

.. note::

    To ease modelling, it is not required to manipulate propagators, but only constraints. However, one can define specific constraints by defining combinations of propagators and/or its own propagators. More detailed are given in :ref:`Defining its own constraint <46_define_constraint_label>`.


Choco |version| provides various types of constraints.


Available constraints
^^^^^^^^^^^^^^^^^^^^^

:ref:`51_icstr_fal`, :ref:`51_icstr_tru`

    On one integer variable

:ref:`51_icstr_ari`,
:ref:`51_icstr_mem`,
:ref:`51_icstr_nmem`.


    On two integer variables

:ref:`51_icstr_abs`,
:ref:`51_icstr_ari`,
:ref:`51_icstr_dist`,
:ref:`51_icstr_squa`,
:ref:`51_icstr_tab`,
:ref:`51_icstr_tim`.

    On three integer variables

:ref:`51_icstr_ari`,
:ref:`51_icstr_dist`,
:ref:`51_icstr_div`,
:ref:`51_icstr_max`,
:ref:`51_icstr_min`,
:ref:`51_icstr_mod`,
:ref:`51_icstr_tim`.

    On an undefined number of integer variables

:ref:`51_icstr_elm`,
:ref:`51_icstr_sor`,
:ref:`51_icstr_ksor`,
:ref:`51_icstr_tab`,
:ref:`51_icstr_mdd`.


:ref:`51_icstr_alld`,
:ref:`51_icstr_alldc`,
:ref:`51_icstr_alld_e0`,
:ref:`51_icstr_gcc`,

:ref:`51_icstr_amo`,
:ref:`51_icstr_atl`,
:ref:`51_icstr_atm`,
:ref:`51_icstr_cou`,
:ref:`51_icstr_nva`,

:ref:`51_icstr_booc`,
:ref:`51_icstr_clauc`,
:ref:`51_icstr_ich`.

:ref:`51_icstr_cum`,
:ref:`51_icstr_diffn`.

:ref:`51_icstr_lexcl`,
:ref:`51_icstr_lexce`,
:ref:`51_icstr_lexl`,
:ref:`51_icstr_lexe`,
:ref:`51_icstr_nvpc`.

:ref:`51_icstr_max`,
:ref:`51_icstr_min`,


:ref:`51_icstr_sca`,
:ref:`51_icstr_sum`.

:ref:`51_icstr_creg`,
:ref:`51_icstr_mcreg`,
:ref:`51_icstr_reg`.

:ref:`51_icstr_cir`,
:ref:`51_icstr_pat`,
:ref:`51_icstr_scir`,
:ref:`51_icstr_spat`,
:ref:`51_icstr_tree`.

:ref:`51_icstr_bin`,
:ref:`51_icstr_kna`,
:ref:`51_icstr_tsp`.


    On one set variable

:ref:`51_scstr_note`.    

    On two set variables

:ref:`51_scstr_dis`,
:ref:`51_scstr_off`.

    On an undefined number of set variables

:ref:`51_scstr_alldif`,
:ref:`51_scstr_alldis`,
:ref:`51_scstr_alleq`,
:ref:`51_scstr_bcha`,
:ref:`51_scstr_int`,
:ref:`51_scstr_inv`,
:ref:`51_scstr_mem`,
:ref:`51_scstr_nbe`,
:ref:`51_scstr_part`,
:ref:`51_scstr_sse`,
:ref:`51_scstr_sym`,
:ref:`51_scstr_uni`.
    

    On integer and set variables

:ref:`51_scstr_card`,
:ref:`51_scstr_elm`,
:ref:`51_scstr_icha`,
:ref:`51_scstr_max`,
:ref:`51_scstr_mem`,
:ref:`51_scstr_nme`,
:ref:`51_scstr_min`,
:ref:`51_scstr_sum`.

    On real variables

:ref:`51_rcstr_main`.


.. _512_constraint_things_to_know:

Things to know about constraints
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. _512_automaton:

Automaton-based Constraints
"""""""""""""""""""""""""""

:ref:`51_icstr_creg`, :ref:`51_icstr_mcreg` and :ref:`51_icstr_reg` rely on an automaton, declared implicitly or explicitly.
There are two kinds of ``IAutomaton`` : ``FiniteAutomaton``, needed for :ref:`51_icstr_creg`, and `CostAutomaton`, required for :ref:`51_icstr_mcreg` and :ref:`51_icstr_reg`.
A ``CostAutomaton`` is an extension of ``FiniteAutomaton`` where costs can be declared per transition.

``FiniteAutomaton`` embeds an ``Automaton`` object provided by the ``dk.brics.automaton`` library.
Such an automaton accepts fixed-size words made of multiple ``char`` s, but the regular constraints rely on ``IntVar`` s.
So,  mapping between ``char`` (needed by the underlying library) and ``int`` (declared in ``IntVar``) is made.
The mapping enables declaring regular expressions where a symbol is not only a digit between `0` and `9` but any positive number.
Then to distinct, in the word `101`, the symbols `0`, `1`, `10` and `101`, two additional ``char`` are allowed in a regexp: `<` and `>` which delimits numbers.

In summary, a valid regexp for the :ref:`51_icstr_creg`, :ref:`51_icstr_mcreg` and :ref:`51_icstr_reg` constraints
is a combination of **digits** and Java Regexp special characters.

.. admonition:: Examples of allowed RegExp

        ``"0*11111110+10+10+11111110*"``, ``"11(0|1|2)*00"``, ``"(0|<10> |<20>)*(0|<10>)"``.

.. admonition:: Example of forbidden RegExp

        ``"abc(a|b|c)*"``.


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

The ``LogicalConstraintFactory`` enables to manipulate constraints through their reification.

Reifying a constraint means that we allow the constraint not to be satisfied. Therefore, the reified constraint **should not** be posted.
Only the reifying constraint should be posted. Note also that, for performance reasons, some reifying constraints available
in the ``LogicalConstraintFactory`` are **automatically posted** (the factory method returns void).

For instance, we can represent the constraint "either ``x<0`` or ``y>42``" as the following: ::

 Constraint a = IntConstraintFactory.arithm(x,"<",0);
 Constraint b = IntConstraintFactory.arithm(y,">",42);
 Constraint c = LogicalConstraintFactory.or(a,b);
 solver.post(c);

This will actually reify both constraints ``a`` and ``b`` and say that at least one of the corresponding boolean variables must be true.
Note that only the constraint ``c`` is posted.

As a second reification example, let us consider "if ``x<0`` then ``y>42``": ::

 Constraint a = IntConstraintFactory.arithm(x,"<",0);
 Constraint b = IntConstraintFactory.arithm(y,">",42);
 LogicalConstraintFactory.ifThen(a,b);

This time the ``LogicalConstraintFactory.ifThen`` returns void, meaning that the constraint is automatically posted.
If one really needs to access an ``ifThen`` ``Constraint`` object, then the ``LogicalConstraintFactory.ifThen_reifiable``
method should be used instead.

SAT constraints
---------------

A SAT solver is embedded in Choco. It is not  designed to be accessed directly.
The SAT solver is internally managed as a constraint (and a propagator), that's why it is referred as SAT constraint in the following.

.. important::

    The SAT solver is directly inspired by `MiniSat <http://minisat.se/>`_:cite:`EenS03`.
    However, it only propagates clauses, no learning or search is implemented.

On a call to any methods of ``solver.constraints.SatFactory``, the SAT constraint (and propagator) is created and automatically posted to the solver.

How to add clauses
^^^^^^^^^^^^^^^^^^
Clauses can be added with calls to the ``solver.constraints.SatFactory``.


    On one boolean variable

:ref:`51_lcstr_true`,
:ref:`51_lcstr_false`.

    On two boolean variables

:ref:`51_lcstr_booleq`,
:ref:`51_lcstr_boolle`,
:ref:`51_lcstr_boollt`,
:ref:`51_lcstr_boolnot`.

    Reification on two boolean variables

:ref:`51_lcstr_booliseqvar`,
:ref:`51_lcstr_boolislevar`,
:ref:`51_lcstr_boolisltvar`,
:ref:`51_lcstr_boolisneqvar`,
:ref:`51_lcstr_oreqvar`,
:ref:`51_lcstr_xoreqvar`,
:ref:`51_lcstr_andeqvar`.

    On undefined number of boolean variables

:ref:`51_lcstr_orarrayqualtrue`,
:ref:`51_lcstr_atmostnminusone`,
:ref:`51_lcstr_atmostone`,
:ref:`51_lcstr_andarrayequalfalse`,
:ref:`51_lcstr_andarrayeqvar`,
:ref:`51_lcstr_orarrayeqvar`,
:ref:`51_lcstr_clauses`,
:ref:`51_lcstr_maxboolarraylesseqvar`,
:ref:`51_lcstr_sumboolarraygreatereqvar`,
:ref:`51_lcstr_sumboolarraylesseqvar`.

.. _542_complex_clauses:

Declaring complex clauses
^^^^^^^^^^^^^^^^^^^^^^^^^

There is a convenient way to declare complex clauses by calling :ref:`51_lcstr_clauses`.
The method takes a ``LogOp`` and an instance of ``Solver`` as input, extracts the underlying clauses and add them to the ``SatFactory``.


A ``LogOp`` is an implementation of ``ILogical``, just like ``BoolVar``, and provides the following API:

  ``LogOp and(ILogical... operands)`` : create a conjunction, results in `true` if all of its operands are `true`.

  ``LogOp ifOnlyIf(ILogical a, ILogical b)``:  create a biconditional, results in `true` if and only if both operands are false or both operands are `true`.

  ``LogOp ifThenElse(ILogical a, ILogical b, ILogical c)`` : create an implication, results in `true` if ``a`` is `true` and ``b`` is `true` or ``a`` is ``false` and ``c`` is `true`.

  ``LogOp implies(ILogical a, ILogical b)`` : create an implication, results in `true` if ``a`` is `false` or ``b`` is `true`.

  ``LogOp reified(BoolVar b, ILogical tree)`` : create a logical connection between ``b`` and ``tree``.

  ``LogOp or(ILogical... operands)`` : create a disjunction, results in `true` whenever one or more of its operands are `true`.

  ``LogOp nand(ILogical... operands)`` : create an alternative denial, results in if at least one of its operands is `false`.

  ``LogOp nor(ILogical... operands)`` : create a joint denial, results in `true` if all of its operands are `false`.

  ``LogOp xor(ILogical a, ILogical b)`` : create an exclusive disjunction, results in `true` whenever both operands differ.

  ``ILogical negate(ILogical l)`` : return the logical complement of `l`.

The resulting logical operation can be very verbose, but certainly more easy to declare: ::

    SatFactory.addClauses(LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e));
    SatFactory.addClauses(LogOp.nor(LogOp.or(LogOp.nand(a, b), c), d));
    SatFactory.addClauses(LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e));

