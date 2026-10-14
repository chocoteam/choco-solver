---
title: "Handling constraints"
date: 2020-01-07T16:07:15+01:00
weight: 22
description: >
  How to handle constraints?
---

A constraint is a logic formula defining allowed combinations of values for a set of variables,
i.e., restrictions over variables that must be respected in order to get a feasible solution.
A constraint is equipped with a (set of) filtering algorithm(s), named *propagator(s)*.
A propagator **removes**, from the domains of the target variables, values that cannot correspond to a valid combination of values.
A solution of a problem is a variable-value assignment verifying all the constraints.

Constraint can be declared in *extension*, by defining the valid/invalid tuples, or in *intension*, by defining a relation between the variables.
For a given requirement, there can be several constraints/propagators available.
A widely used example is the AllDifferent constraint which ensures that all its variables take a distinct value in a solution.
Such a rule can be formulated using :


* a clique of basic inequality constraints,

* a generic table constraint (an extension constraint that lists the valid tuples),

* a dedicated global constraint analysing :

    * instantiated variables (*Forward checking propagator*),

    * variable domain bounds (*Bound consistency propagator*),

    * variable domains (*Arc consistency propagator*).

Depending on the problem to solve, the efficiency of each option may be dramatically different.
In general, we tend to use global constraints, that capture a good part of the problem structure.
However, these constraints often model problems that are inherently NP-complete so only a partial filtering is performed
in general, to keep polynomial time algorithms.
This is for example the case of NValue constraint that one aspect relates to the problem of “minimum hitting set.”

## Design choices

### Class organization

In Choco-solver, constraints are generally not associated with a specific java class.
Instead, each constraint is associated with a specific method in `Model` that will build
a generic `Constraint` with the right list of propagators.
Each propagator is associated with a unique java class.

Note that it is not required to manipulate propagators, but only constraints.
However, one can define specific constraints by defining combinations of existing and/or home-made propagators.

### Solution checking

The satisfaction of the constraints is done on each solution by calling the `isSatisfied()` method of every constraint.
By default, this method checks the `isEntailed()` method of each of its propagators.

{{% alert title="Info" color="primary" %}}
Additional checks (Java assertions) can be performed by adding the `-ea` instruction in the JVM arguments.
This is useful when debugging a program.
{{% /alert %}}


## List of available constraints

Please refer to the javadoc of `Model` to have the complete list of available constraints.

## Posting constraints

To be effective, a constraint must be posted to the solver. This is achieved using the `post()` method:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
model.allDifferent(vars).post();
{{< /tab >}}
{{< tab header="Python" >}}
model.all_different(vars).post()
{{< /tab >}} 
{{< /tabpane >}}


Otherwise, if the `post()` method is not called, the constraint will not be taken into account during the solution process :
it may not be satisfied in solutions.

## Reifying constraints

In Choco-solver, it is possible to reify any constraint. Reifying a constraint means associating it with a `BoolVar`
to represent whether or not the constraint is satisfied :

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
BoolVar b = constraint.reify();
{{< /tab >}}
{{< tab header="Python" >}}
b = constraint.reify()
{{< /tab >}} 
{{< /tabpane >}}


Or:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
BoolVar b = model.boolVar();
constraint.reifyWith(b);
{{< /tab >}} 
{{< /tabpane >}}


Reifying a constraint means that we allow the constraint not to be satisfied.
Therefore, the reified constraint **should not** be posted.
For instance, let us consider “if `x<0` then `y>42`”:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
model.ifThen(
   model.arithm(x,"<",0),
   model.arithm(y,">",42)
);
{{< /tab >}}
{{< /tabpane >}}



Such constraint states that *if* `x` takes a value strictly less than `0` *then* `y` should take a value stritctly greater than `42`. Conversely, *if* `x` takes a value greater or equal to `0` *then* `y` is not restricted.


{{% alert title="Info" color="primary" %}}
Reification is a specific process which does not rely on classical constraints.
This is why `ifThen`, `ifThenElse`, `ifOnlyIf` and `reification` return void and do not need to be posted.
{{% /alert %}}

{{% alert title="Info" color="primary" %}}
A constraint is reified with only one boolean variable. Multiple calls to `constraint.reify()` will return the same variable.
However, the following call will associate `b1` with the constraint and then post `b1 = b2`:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
BoolVar b1 = model.boolVar();
BoolVar b2 = model.boolVar();
constraint.reifyWith(b1);
constraint.reifyWith(b2);
{{< /tab >}}
{{< /tabpane >}}
{{% /alert %}}


