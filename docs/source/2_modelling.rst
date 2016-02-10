
########
Modeling
########

*********
The Model
*********

The object :code:`Model` is the key component. It is built as follows: ::

 Model model = new Model();

or::

 Model model = new Model("my problem");

This should be the first instruction, prior to any other modeling instructions, as it is needed to declare variables and constraints.

*********
Variables
*********

Principle
=========

A variable is an *unknown*, mathematically speaking.
The goal of a resolution is to *assign* a *value* to each variable.
The *domain* of a variable --(super)set of values it may take-- must be defined in the model.

Choco |version| includes several types of variables: ``BoolVar``, ``IntVar``, ``SetVar`` and ``RealVar``.
Variables are created using the ``Model`` object.
When creating a variable, the user can specify a name to help reading the output.

Integer variables
=================

An integer variable an unknown whose value should be an integer. Therefore, the domain of an integer variable is a set of integers (representing possible values).
To create an integer variable, the ``Model`` should be used: ::

 // Create a constant variable equal to 42
 IntVar v0 = model.intVar("v0", 3);
 // Create a variable taking its value in [1, 3] (the value is 1, 2 or 3)
 IntVar v1 = model.intVar("v1", 1, 3);
 // Create a variable taking its value in {1, 3} (the value is 1 or 3)
 IntVar v2 = model.intVar("v2", new int[]{1, 3});

It is then possible to build directly arrays and matrices of variables having the same initial domain with: ::

 // Create an array of 5 variables taking their value in [-1, 1]
 IntVar[] vs = model.intVarArray("vs", 5, -1, 1);
 // Create a matrix of 5x6 variables taking their value in [-1, 1]
 IntVar[][] vs = model.intVarMatrix("vs", 5, 6, -1, 1);

.. important::

    It is strongly recommended to define an initial domain that is close to expected values
    instead of defining unbounded domains like ``[Integer.MIN_VALUE, Integer.MAX_VALUE]`` that may lead to :

    - incorrect domain size (``Integer.MAX_VALUE - Integer.MIN_VALUE +1 = 0``)
    - numeric overflow/underflow operations during propagation.

    If *undefined* domain is really required, the following range should be considered:
    ``[IVariableFactory.MIN_INT_BOUND, IVariableFactory.MAX_INT_BOUND]``.
    Such an interval defines `42949673` values, from `-21474836` to `21474836`.

There exists different ways to encode the domain of an integer variable.

Bounded domain
--------------

When the domain of an integer variable is said to be *bounded*, it is represented through
an interval of the form :math:`[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a <= b`.
This representation is pretty light in memory (it requires only two integers) but it cannot represent *holes* in the domain.
For instance, if we have a variable whose domain is :math:`[\![0,10]\!]` and a constraint enables to detect that
values 2, 3, 7 and 8 are infeasible, then this learning will be lost as it cannot be encoded in the domain (which remains the same).

To specify you want to use bounded domains, set the ``boundedDomain`` argument to ``true`` when creating variables: ::

 IntVar v = model.intVar("v", 1, 12, true);

.. note::
When using bounded domains, branching decisions must either be domain splits or bound assignments/removals.
   Indeed, assigning a bounded variable to a value strictly comprised between its bounds may results in infinite loop
   because such branching decisions will not be refutable.

Enumerated domains
------------------

When the domain of an integer variable is said to be *enumerated*, it is represented through
the set of possible values, in the form:
 - :math:`[\![a,b]\!]` where :math:`a` and :math:`b` are integers such that :math:`a <= b`
 - {:math:`a,b,c,..,z`}, where :math:`a < b < c ... < z`.
Enumerated domains provide more information than bounded domains but are heavier in memory (the domain usually requires a bitset).

To specify you want to use enumerated domains, either set the ``boundedDomain`` argument to ``false`` when creating variables by specifying two bounds
or use the signature that specifies the array of possible values: ::

 IntVar v = model.intVar("v", 1, 4, false);
 IntVar v = model.intVar("v", new int[]{1,2,3,4});

.. admonition:: **Modelling**: Bounded or Enumerated?

    The choice of domain types may have strong impact on performance.
    Not only the memory consumption should be considered but also the used constraints.
    Indeed, some constraints only update bounds of integer variables, using them with bounded domains is enough.
    Others make holes in variables' domain, using them with enumerated domains takes advantage of the *power* of their filtering algorithm.
    Most of the time, variables are associated with propagators of various *power*.
    The choice of domain representation should then be done on a case by case basis.

Boolean variable
================

Boolean variables, ``BoolVar``, are specific ``IntVar`` that take their value in :math:`[\![0,1]\!]`.
The avantage of ``BoolVar`` is twofold:
 - They can be used to say whether or not constraint should be satisfied (reification)
 - They domain, and some filtering algorithms, are optimized

To create a new boolean variable: ::

 BoolVar b = model.boolVar("b");

Set variables
=============

A set variable, ``SetVar``, represents a set of integers, i.e. its value is a set of integers.
Its domain is defined by a set interval ``[S_K,S_E]`` where:

- the kernel, ``S_K``, is an ``ISet`` object which contains integers that figure in every solution.
- the envelope, ``S_E``, is an ``ISet`` object which contains integers that potentially figure in at least one solution,

Initial values for both ``S_K`` and ``S_E`` should be such that ``S_K`` is a subset of ``S_E``.
Then, decisions and filtering algorithms will remove integers from ``S_E`` and add some others to ``S_K``.
A set variable is instantiated if and only if ``S_E = S_K``.

A set variable can be created as follows: ::

    // Constant SetVar equal to {2,3,12}
    SetVar x = model.setVar("x", new int[]{2,3,12});

    // SetVar representing a subset of {1,2,3,5,12}
    SetVar y = model.setVar("y", new int[]{}, new int[]{1,2,3,5,12});
    // possible values: {}, {2}, {1,3,5} ...

    // SetVar representing a superset of {2,3} and a subset of {1,2,3,5,12}
    SetVar z = model.setVar("z", new int[]{2,3}, new int[]{1,2,3,5,12});
    // possible values: {2,3}, {2,3,5}, {1,2,3,5} ...

Real variables
==============

The domain of a real variable is an interval of doubles. Conceptually, the value of a real variable is a double.
However, it uses a precision parameter for floating number computation,
so its actual value is generally an interval of doubles, whose size is constrained by the precision parameter.
Real variables have a specific status in Choco |version|, which uses `Ibex solver`_ to define constraints.

A real variable is declared with three doubles defining its bound and a precision: ::

 RealVar x = model.realVar("x", 0.2d, 3.4d, 0.001d);

.. _Ibex solver: http://www.emn.fr/z-info/ibex/

Views: Creating variables from constraints
==========================================

When a variable is defined as a function of another variable, views can be
used to make the model shorter. In some cases, the view has a specific (optimized) domain representation.
Otherwise, it is simply a modeling shortcut to create a variable and post a constraint at the same time.
Few examples:

``x = y + 2`` : ::

 IntVar x = model.intOffsetView(y, 2);

``x = -y`` : ::

 IntVar x = model.intMinusView(y);

``x = 3*y`` : ::

 IntVar x = model.intScaleView(y, 3);

Views can be combined together, e.g. ``x = 2*y + 5`` is: ::

 IntVar x = model.intOffsetView(model.intScaleView(y,2),5);

We can also use a view mecanism to link an integer variable with a real variable.

 IntVar ivar = model.intVar("i", 0, 4);
 double precision = 0.001d;
 RealVar rvar = model.realIntView(ivar, precision);

This code enables to embed an integer variable in a constraint that is defined over real variables.

***********
Constraints
***********

Constraints and propagators
===========================

Main principles
---------------

A constraint is a logic formula defining allowed combinations of values for a set of variables,
i.e., restrictions over variables that must be respected in order to get a feasible solution.
A constraint is equipped with a (set of) filtering algorithm(s), named *propagator(s)*.
A propagator **removes**, from the domains of the target variables, values that cannot correspond to a valid combination of values.
A solution of a problem is variable-value assignment verifying all the constraints.

Constraint can be declared in *extension*, by defining the valid/invalid tuples, or in *intension*, by defining a relation between the variables.
For a given requirement, there can be several constraints/propagators available.
A widely used example is the `AllDifferent` constraint which ensures that all its variables take a distinct value in a solution.
Such a rule can be formulated using :
 - a clique of basic inequality constraints,
 - a generic table constraint --an extension constraint which list the valid tuples,
 - a dedicated global constraint analysing bounds of variable (*Bound consistency*),
 - a dedicated global constraint analysing all values of the variables (*Arc consistency*).

Depending on the problem to solve, the efficiency of each option may be dramatically different.
In general, we tend to use global constraints, that capture a good part of the problem structure.
However, these constraints often model problems that are inherently NP-complete so only a partial filtering is performed
in general, to keep polynomial time algorithms.
This is for example the case of `NValue` constraint that one aspect relates to the problem of "minimum hitting set."

Design choices
--------------

Class organization
~~~~~~~~~~~~~~~~~~

In Choco Solver |version|, constraints are generally not associated with a specific java class.
Instead, each constraint is associated with a specific method in ``Model`` that will build
a generic ``Constraint`` with the right list of propagators.
Each propagator is associated with a unique java class.

Note that it is not required to manipulate propagators, but only constraints.
However, one can define specific constraints by defining combinations of existing and/or its own propagators.

Solution checking
~~~~~~~~~~~~~~~~~

The satisfaction of the constraints is done on each solution when assertions are enabled.
This means that, by default, solutions are not checked, to save computational time.
Indeed, constraint-propagation should be sufficient to guarantee obtaining correct solutions.

.. note::

    One can enable assertions by adding the ``-ea`` instruction in the JVM arguments.

A constraint may define its specific checker by overwriting the method ``isSatisfied()``.
By default, this method checks the ``isEntailed()`` method of each of its propagators.

List of available constraints
=============================

Please refer to the javadoc of ``Model`` to have the list of available constraints.


Posting constraints
===================

To be effective, a constraint must be posted to the solver. This is achieved using the ``post()`` method: ::

 model.allDifferent(vars).post();

Otherwise, if the ``post()`` method is not called, the constraint will not be taken into account during the solution process :
it may not be satisfied in solutions.

Reifying constraints
====================

In Choco |version|, it is possible to reify any constraint. Reifying a constraint means associating it with a ``BoolVar``
to represent whether or not the constraint is satisfied : ::

 BoolVar b = constraint.reify();

Or: ::

 BoolVar b = model.boolVar();
 constraint.reifyWith(b);



Reifying a constraint means that we allow the constraint not to be satisfied.
Therefore, the reified constraint **should not** be posted.
For instance, let us consider "if ``x<0`` then ``y>42``": ::

    model.ifThen(
       model.arithm(x,"<",0),
       model.arithm(y,">",42)
    );

.. note::

    Reification is a specific process which does not rely on classical constraints.
    This is why ``ifThen``, ``ifThenElse``, ``ifOnlyIf`` and ``reification`` return void and do not need to be posted.


.. note::

    A constraint is reified with only one boolean variable. Multiple calls to ``constraint.reify()`` will return the same variable.
    However, the following call will associate ``b1`` with the constraint and then post ``b1 = b2``: ::

       BoolVar b1 = model.boolVar();
       BoolVar b2 = model.boolVar();
       constraint.reifyWith(b1);
       constraint.reifyWith(b2);

Some specific constraints
=========================

SAT constraints
---------------

A SAT solver is embedded in Choco. It is not  designed to be accessed directly.
The SAT solver is internally managed as a constraint (and a propagator), that's why it is referred to as SAT constraint in the following.

.. important::

    The SAT solver is directly inspired by `MiniSat <http://minisat.se/>`_:cite:`EenS03`.
    However, it only propagates clauses. Neither learning nor search is implemented.

Clauses can be added with the ``SatFactory`` (refer to javadoc for details).
On any call to a method of ``SatFactory``, the SAT constraint (and its propagator) is created and automatically posted to the solver.
To declare complex clauses, you can call ``SatFactory.addClauses(...)`` by specifying a ``LogOp`` that represents a clause expression: ::

    SatFactory.addClauses(LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e), model);
    // with static import of LogOp
    SatFactory.addClauses(and(nand(nor(a, b), or(c, d)), e), model);

Automaton-based Constraints
---------------------------

``regular``, ``costRegular`` and ``multiCostRegular`` rely on an automaton, declared either implicitly or explicitly.
There are two kinds of ``IAutomaton`` :
 - ``FiniteAutomaton``, needed for ``regular``,
 - ``CostAutomaton``, required for ``costRegular`` and ``multiCostRegular``.


``FiniteAutomaton`` embeds an ``Automaton`` object provided by the ``dk.brics.automaton`` library.
Such an automaton accepts fixed-size words made of multiple ``char``, but the regular constraints rely on ``IntVar``,
so a mapping between ``char`` (needed by the underlying library) and ``int`` (declared in ``IntVar``) has been made.
The mapping enables declaring regular expressions where a symbol is not only a digit between `0` and `9` but any **positive** number.
Then to distinct, in the word `101`, the symbols `0`, `1`, `10` and `101`, two additional ``char`` are allowed in a regexp: `<` and `>` which delimits numbers.

In summary, a valid regexp for the automaton-based constraints is a combination of **digits** and Java Regexp special characters.

.. admonition:: Examples of allowed RegExp

        ``"0*11111110+10+10+11111110*"``,
        ``"11(0|1|2)*00"``,
        ``"(0|<10> |<20>)*(0|<10>)"``.

.. admonition:: Example of forbidden RegExp

        ``"abc(a|b|c)*"``.

``CostAutomaton`` is an extension of ``FiniteAutomaton`` where costs can be declared for each transition.


Designing your own constraint
=============================

You can create your own constraint by creating a generic ``Constraint`` object with the appropriate propagators: ::

    Constraint c = new Constraint("MyConstraint", new MyPropagator(vars));

.. important::

    The array of variables given in parameter of a ``Propagator`` constructor is not cloned but referenced.
    That is, if a permutation occurs in the array of variables, all propagators referencing the array will be incorrect.

The only tricky part lies in the propagator implementation.
Your propagator must extend the ``Propagator`` class but not all methods have to be overwritted.
We will see two ways to implement a propagator ensuring that ``X >= Y``.

Basic propagator
----------------

You must at least call the super constructor to specifies the scope (set of variables) of the propagator.
Then you must implement the two following methods:


``void propagate(int evtmask)``

    This method applies the global filtering algorithm of the propagator, that is, from *scratch*.
    It is called once during initial propagation (to propagate initial domains) and then during the solving process if
    the propagator is not incremental. It is the most important method of the propagator.

``isEntailed()``

    This method checks the current state of the propagator. It is used for constraint reification.
    It checks whether the propagator will be always satisfied (``ESat.TRUE``), never satisfied (``ESat.FALSE``)
    or undefined (``ESat.UNDEFINED``) according to the current state of its domain variables. For instance,
     - :math:`A \neq B` will always be satisfied when $A=\{0,1,2\}$ and :math:`B=\{4,5\}`.
     - :math:`A = B` will never be satisfied when :math:`A=\{0,1,2\}` and :math:`B=\{4,5\}`.
     - The entailment of :math:`A \neq B` cannot be defined when :math:`A=\{0,1,2\}` and :math:`B=\{1,2,3\}`.

``ESat isEntailed()`` implementation may be approximate but should at least cover the case where all variables are instantiated.
This method is also called to check solutions when assertions are enabled, i.e. when the `-ea` JVM option is used.

Here is an example of how to implement a propagator for ``X >= Y``: ::

    // Propagator to apply X >= Y
    public class MySimplePropagator extends Propagator<IntVar> {

        IntVar x, y;

        public MySimplePropagator(IntVar x, IntVar y) {
            super(new IntVar[]{x,y});
            this.x = x;
            this.y = y;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            x.updateLowerBound(y.getLB(), this);
            y.updateUpperBound(x.getUB(), this);
        }

        @Override
        public ESat isEntailed() {
            if (x.getUB() < y.getLB())
                return ESat.FALSE;
            else if (x.getLB() >= y.getUB())
                return ESat.TRUE;
            else
                return ESat.UNDEFINED;
        }
    }

Elaborated propagator
---------------------

The super constructor ``super(Variable[], PropagatorPriority, boolean);`` brings more information.
``PropagatorPriority`` enables to optimize the propagation engine (low arity for fast propagators is better).
The boolean argument allows to specifies the propagator is incremental.
When set to ``true``, the method ``propagate(int varIdx, int mask)`` must be implemented.

.. note::
    Note that if many variables are modified between two calls, a non-incremental filtering may be faster (and simpler).

The method ``propagate(int varIdx, int mask)`` defines the incremental filtering.
It is called for every variable ``vars[varIdx]`` whose domain has changed since the last call.

The method ``getPropagationConditions(int vIdx)`` enables not to react on every kind of domain modification.

The method ``setPassive()`` enables to desactivate the propagator when it is entailed, to save time.
The propagator is automatically reactivated upon backtrack.

The method ``why(...)`` explains the filtering, to allow learning.

Here is an example of how to implement a propagator for ``X >= Y``: ::

    // Propagator to apply X >= Y
    public final class PropGreaterOrEqualX_Y extends Propagator<IntVar> {

        IntVar x, y;

        public PropGreaterOrEqualX_Y(IntVar x, IntVar y) {
            super(new IntVar[]{x,y}, PropagatorPriority.BINARY, true);
            this.x = x;
            this.y = y;
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            if (vIdx == 0) {
                // awakes if x gets instantiated or if its upper bound decreases
                return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.DECUPP);
            } else {
                // awakes if y gets instantiated or if its lower bound increases
                return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.INCLOW);
            }
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            x.updateLowerBound(y.getLB(), this);
            y.updateUpperBound(x.getUB(), this);
            if (x.getLB() >= y.getUB()) {
                this.setPassive();
            }
        }

        @Override
        public void propagate(int varIdx, int mask) throws ContradictionException {
            if (varIdx == 0) {
                y.updateUpperBound(x.getUB(), this);
            } else {
                x.updateLowerBound(y.getLB(), this);
            }
            if (x.getLB() >= y.getUB()) {
                this.setPassive();
            }
        }

        @Override
        public ESat isEntailed() {
            if (x.getUB() < y.getLB())
                return ESat.FALSE;
            else if (x.getLB() >= y.getUB())
                return ESat.TRUE;
            else
                return ESat.UNDEFINED;
        }

        @Override
        public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
            boolean newrules = ruleStore.addPropagatorActivationRule(this);
            if (var.equals(x)) {
                newrules |=ruleStore.addLowerBoundRule(y);
            } else if (var.equals(y)) {
                newrules |=ruleStore.addUpperBoundRule(x);
            } else {
                newrules |=super.why(ruleStore, var, evt, value);
            }
            return newrules;
        }

        @Override
        public String toString() {
            return "prop(" + vars[0].getName() + ".GEQ." + vars[1].getName() + ")";
        }
    }

Idempotency
===========

We distinguish two kinds of propagators:

    *Necessary* propagators, which ensure constraints to be satisfied.

    *Redundant* (or *Implied*) propagators that come in addition to some necessary propagators in order to get a stronger filtering.


Some propagators cannot be idempotent (Lagrangian relaxation, use of randomness, etc.).
For some others, forcing idempotency may be very time consuming.
A redundant propagator does not have to be idempotent but **a necessary propagator should be idempotent** [#fidem]_ .


.. [#fidem] **idempotent**: calling a propagator twice has no effect, i.e. calling it
with its output domains returns its output domains. In that case, it has reached a fix point.

.. [#fmono] **monotonic**: calling a propagator with two input domains :math:`A` and :math:`B`
    for which :math:`A \subseteq B` returns two output domains :math:`A'` and :math:`B'` for which :math:`A' \subseteq B'`.

Trying to make a propagator idempotent directly may not be straightforward.
We provide three implementation possibilities.

The *decomposed*  (recommended) option:

    Split the original propagator into (partial) propagators so that the fix point is performed through the propagation engine.
    For instance, a channeling propagator :math:`A \Leftrightarrow B` can be decomposed into two propagators :math:`A \Rightarrow B` and :math:`B \Rightarrow A`.
    The propagators can (but does not have to) react on fine events.

The *lazy* option:

    Simply post the propagator twice.
    Thus, the fix point is performed through the propagation engine.

The *coarse* option:

    the propagator will perform its fix point by itself.
    The propagator does not react to fine events.
    The coarse filtering algorithm should be surrounded like this: ::

        // In the case of ``SetVar``, replace ``getDomSize()`` by ``getEnvSize()-getKerSize()``.
        long size;
        do{
          size = 0;
          for(IntVar v:vars){
            size+=v.getDomSize();
          }
          // really update domain variables here
          for(IntVar v:vars){
            size-=v.getDomSize();
          }
        }while(size>0);


.. note::

    Domain variable modifier returns a boolean valued to ``true`` if the domain variable has been modified.


**************
Solving models
**************

Solution computation
====================

Finding one solution
--------------------

A call to ``model.solve()`` launches a resolution which stops on the first solution found, if any: ::

    if(model.solve()){
        // do something, e.g. print out variable values
    }else if(model.getResolver().hasReachedLimit()){
        System.out.println("The could not find a solution
                            nor prove that none exists in the given limits");
    }else {
        System.out.println("The solver has proved the problem has no solution");
    }

If ``model.solve()`` returns ``true``, then a solution has been found and each variable is instantiated to a value.
Otherwise, two cases must be considered:

- A limit has been declared and reached (``model.getResolver().hasReachedLimit()`` returns true).
  There may be a solution, but the solver has not been able to find it in the given limit
  or there is no solution but the solver has not been able to prove it (i.e., to close to search tree) in the given limit.
  The resolution process stops in no particular place in the search tree.
- No limit has been declared or reached: The problem has no solution and the solver has proved it.

Enumerating all solutions
-------------------------

You can enumerate all solutions of a problem with a simple while loop as follows: ::

    while(model.solve()){
        // do something, e.g. print out variable values
    }

After the enumeration, the solver closes the search tree and variables are no longer instantiated to a value.

.. tip::

    On a solution, one can get the value assigned to each variable by calling ::

        ivar.getValue();    // instantiation value of an IntVar, return a int
        svar.getValue();    // instantiation values of a SerVar, return a int[]
        rvar.getLB();       // lower bound of a RealVar, return a double
        rvar.getUB();       // upper bound of a RealVar, return a double


Optimization
============

Mono-objective optimization
---------------------------

In Constraint-Programming, the optimization process is the following: anytime a solution is found, the value of the objective variable is stored and a *cut* is posted.
The cut is an additional constraint which states that the next solution must be strictly better than the current one.
To solve an optimization problem, you must specify which variable to optimize and in which direction: ::

   // to maximize X
   model.setObjectives(ResolutionPolicy.MAXIMIZE, X);
   model.solve();

   // to minimize X
   model.setObjectives(ResolutionPolicy.MINIMIZE, X);
   model.solve();

The method returns true if at least one solution (not necessarily optimal) has been found.
The best solution found so far is automatically restored at the end of ``model.solve()``,
so that you can access variable values.




.. tip::

    When the objective is a function over multiple variables, you need to model it through
    one objective variable and additionnal constraints: ::

        // Model objective function 3X + 4Y
        IntVar OBJ = model.intVar("objective", 0, 999);
        model.scalar(new IntVar[]{X,Y}, new int[]{3,4}, OBJ)).post();
        // Specify objective
        model.setObjectives(ResolutionPolicy.MAXIMIZE, OBJ);
        // Compute optimum
        model.solve();

Multi-objective optimization
----------------------------

It is possible to solve multi-objective optimization problems with Choco |version|.
You must then specify a set of incomparable objective variable you want to optimize: ::

   // to maximize X and Y
   model.setObjectives(ResolutionPolicy.MINIMIZE, X, Y);
   model.solve();

This method will compute the Pareto front of the problem.
The underlying approach is currently a bit naive, but it simplifies the process:
Anytime a solution is found, a cut is posted which states that at least one of the objective variables must be strictly better:
Such as :math:`(X_0 < b_0 \lor X_1 < b_1 \lor \ldots \lor X_n < b_n)` where :math:`X_i` is the ith objective variable and :math:`b_i`
its best known value.

.. note::

 All objectives must be optimized on the same direction (either minimization or maximization).


Constraint propagation
======================

One may want to propagate all constraints without search for a solution.
This can be achieved by calling ``solver.propagate()``.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a ``ContradictionException`` if a contradiction occurs.
In that case, the propagation engine must be flushed calling ``solver.getEngine().flush()``
to ensure there is no pending events.

.. warning::

 If there are still pending events in the propagation engine, the propagation may results in unexpected results.