---
title: "Basic Objects"
date: 2020-01-31T11:48:22+01:00
type: docs
math: "true"
draft: true
weight: 12
description: >
  A quick tour of the library's essential items.
---

The content of this section is extracted from the Javadoc and the
Choco-solver's User Guide. Here, we **briefly** described the main aspects of
the most commonly used objects. This does not aim at being complete: it
covers the basic information.

The Model
---------

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = new Model("My model");
{{< /tab >}}
{{< /tabpane >}}

As said before, the `Model` is a key component of the library. It has to
be the first instruction declared, since it provides entry point methods
that help modelling a problem.

A good habit is to declare a model with a name, otherwise a random one
will be assigned by default.

We designed the model in such a way that you can reach almost everything
needed to describe a problem from it.

For example, it stores its variables and constraints. Variables and
constraints of a model can be retrieved thanks to API :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
model.retrieveIntVars(true); // extract IntVars, including BoolVars
model.getCstrs(); // extract posted constraints
{{< /tab >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
We strongly encourage you to attach the Javadoc (provides either on the website or on Maven Central Repository) to the library in your IDE.
{{% /alert %}}

The Variables
-------------

A variable is an *unknown*, mathematically speaking. In a solution of a
given problem (considering that at least one exists), each variable is
assigned to a *value* selected within its domain. The notion of *value*
differs from one type of variable to the other.

{{% alert title="Info" color="primary" %}}
A variable can be declared in only one model at a time. Indeed, a reference to the declaring model is maintained in it.
{{% /alert %}}


### Integer variable

An integer variable, `IntVar`, should be assigned to an integer value. There are
many ways to declare an `IntVar`:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// A variable with a unique value in its domain, in other words, a constant
IntVar two = model.intVar("TWO", 2);
// Any value in [1..4] can be assigned to this variable
IntVar x = model.intVar("X", 1, 4);
// Only the values 1, 3 and 4 can be assigned to this variable
IntVar y = model.intVar("X", new int[]{1, 3, 4});
{{< /tab >}}
{{< /tabpane >}}

{{% alert title="Warning" color="secondary" %}}
Declaring a variable with an *infinite* domain, like :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
model.intVar("X", Integer.MIN_VALUE, Integer.MAX_VALUE)
{{< /tab >}}
{{< /tabpane >}}
is clearly a bad idea.

Too large domains may lead to underflow or overflow issues and most of
the time, even if Choco-solver will finally compute the right bounds by
itself, you certainly want to save space and time by directly
declaring relevant bounds.
{{% /alert %}}

The domain of an integer variable in Choco-solver can either be *bounded* or
*enumerated*. In a bounded domain, only current bounds are stored in
memory. This saves memory (only two integers are needed) but it
restricts its usage: there is no possibility to make holes in it.

On the contrary, with an enumerated domain, all possible values are
explicitly stored in memory. This consumes more memory (one integer and
a bitset -- many longs -- are needed) but it allows making holes in it.

{{% alert title="Bounded or Enumerated?" color="primary" %}}

The memory consumption should not be the only criterion to consider
when one needs to choose between one representation and the other.
Indeed, the *filtering* strength of the model, through constraints,
has to be considered too. For instance, some constraints can only
deduce bound updates, in that case bounded domains fit the need. Other
constraints can make holes in variables' domain, in that case
enumerated domains are relevant.

If you don't know what to do, the following scenario can be applied:
-   domain's cardinality greater than 262144 should be bounded
-   domain's cardinality smaller than 32768 can be enumerated
    without loss of efficiency
-   in any case, empirical evaluation is a good habit.
{{% /alert %}}

### Boolean variable

An boolean variable, `BoolVar`, should be assigned to a boolean value. A `BoolVar`
is a specific IntVar with a domain restricted to $[0,1]$, 0 stands for
false, 1 for true. Thus a BoolVar can be declared in any integer
constraint (*e.g.*, a sum) and boolean constraints (*e.g.*, in clauses
store).

Here is the common way to declare a BoolVar

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// A [0,1]-variable
BoolVar b = model.boolVar("b");
{{< /tab >}}
{{< /tabpane >}}

### Set variable

A set variable, `SetVar`, should be assigned to a set of integer values
(possibly empty or singleton). Its domain is defined by a set of
intervals $[\\![m,o]\\!]$ where $m$ denotes the integers that figure in all
solutions and $o$ the integers that potentially figure in a solution.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// SetVar representing a subset of {1,2,3,5,12}
SetVar y = model.setVar("y", new int[]{}, new int[]{1,2,3,5,12});
// possible values: {}, {2}, {1,3,5} ...
{{< /tab >}}
{{< /tabpane >}}


### Real variable

A real variable, `RealVar`, should be assigned an interval of doubles. Its
domain is defined by its bounds and a *precision*. The precision
parameter helps considering a real variable as instantiated: when the
distance between the two bounds is less than or equal to the precision.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// A [0.2d, 3.4d]-variable, with a precision of 0.001d
RealVar x = model.realVar("x", 0.2d, 3.4d, 0.001d);
{{< /tab >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
Using RealVar requires to install [Ibex](http://www.ibx-lib.org)
before. 
Indeed, Choco-solver relies on Ibex to deal with continuous
constraints.
{{% /alert %}}

The Constraints
---------------

A constraint is a relation between one or more variables of a model. It
defines conditions over these variables that must be respected in a
solution. A constraint has a semantic (*e.g.*, "greater than" or "all
different") and is equipped with *filtering algorithms* that ensure
conditions induced by the semantic hold.

A filtering algorithm, or *propagator*, removes from variables' domain
values that cannot appear in any solution. A propagator has a *filtering
strength* and a time complexity to achieve it. The filtering strength,
or *level of consistency*, determines how accurate a propagator is when
values to be removed are detected.

### Posting a constraint

For a constraint to be integrated in a model, a call to post() is
required :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// x and y must be different in any solution
model.arithm(x, "!=", y).post();
// or, in a more verbose way
model.post(model.arithm(x, "<", z));
{{< /tab >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
A constraint can be posted in only one model at a time. Indeed, a
reference to the declaring model is maintained in it.
{{% /alert %}}

Once posted, a constraint is known from a model and will be integrated
in the filtering loop.

{{% alert title="Info" color="primary" %}}
Posting a constraint does not remove any value from its variables'
domain. Indeed, Choco-solver runs the *initial propagation* only when a
resolution is called.
{{% /alert %}}

The only reason why a constraint is not posted a model is to *reify* it.

### Reifying a constraint

Alternatively, a constraint can be reified with a BoolVar :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// the constraint is reified with `b`
BoolVar r1 = model.arithm(x, "!=", y).reify();
// equivalent to:
BoolVar r2 = model.boolVar("r2");
model.arithm(x, "<", z).reifyWith(r2);
{{< /tab >}}
{{< /tabpane >}}

The BoolVar that reifies a constraint represents whether or not a
constraint is satisfied. If the constraint is satisfied, the boolean
variable is set to true, false otherwise. If the boolean variable is set
to true the constraint should be satisfied, unsatisfied otherwise.

Reifying constraints is helpful to express conditions like:
(x = y) xor (x \> 15) :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
BoolVar c1 = model.arithm(x, "=", y).reify();
BoolVar c2 = model.arithm(x, ">", 15).reify();
model.arithm(c1, "+", c2, "=", 1).post();
{{< /tab >}}
{{< /tabpane >}}


{{% alert title="Warning" color="secondary" %}}
A reified constraint **should not** be posted. Indeed, posting it will
declare it as a *hard* constraint, to be satisfied, reifying it will
declare it as a *soft* constraint, that can be unsatisfied. Both state
cannot co-exist simultaneously: hard state dominates soft one.
{{% /alert %}}

{{% alert title="Caution" color="primary" %}}
A constraint that is neither posted or reified **is not considered at
all** in the resolution. Make sure all constraints are either posted
or reified.
{{% /alert %}}

There are more than 80 constraints in Choco-solver, and anyone can create its
own constraint easily. Native constraints are provided by the model, as
seen before. A look at the Javadoc gives a big picture of the ones
available. In this tutorial, we will have a look at the most commonly
used ones.

The Solver
----------

The `Model` serves at describing the problem with variables and
constraints. The resolution is managed by the Solver.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = new Model("My problem");
// variables declaration
// constraints declaration
Solver solver = model.getSolver();
Solution solution = solver.findSolution();
{{< /tab >}}
{{< /tabpane >}}

Having access to the Solver is needed to tune the resolution and launch
it. It provides methods to configure *search strategies*, to define
resolution goals (*i.e.*, finding one solution, all solutions or optimal
solutions) and getting resolution statistics.

Instead of listing all resolution features, we will see some of them in
the following.

Modelling and Solving
---------------------

Carefully selecting variables and constraints to describe a problem in a
model is a tough task to do. Indeed, some knowledge of the available
constraints (or their reformulations), their filtering strength and
complexity, is needed to take advantage of Constraint Programming. This
has to be both taught and experimented. Same goes with the resolution
tuning. Using Choco-solver has a black-box solver results in good performance
on average. But, injecting problem expertise in the search process is a
key component of success. Choco-solver offers a large range of features to let
you good chances to master your problem.