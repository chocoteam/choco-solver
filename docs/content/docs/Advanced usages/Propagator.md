---
title: "Designing a propagator"
date: 2020-01-07T16:07:15+01:00
weight: 52
math: "true" 
description: >
  How to develop a propagator?
---


### Designing your own constraint

You can create your own constraint by creating a generic `Constraint` object with the appropriate propagator:

```java
Constraint c = new Constraint("MyConstraint", new MyPropagator(vars));
```

The only tricky part relies in the propagator implementation.
Your propagator must extend the `Propagator` class but, at the begining, not all methods have to be overwritted.
We will see two ways to implement a propagator ensuring that `X >= Y`.

A guided implementation of a scalar constraint is presented in the tutorial: [Designing a constraint]({{< ref "/tutos/Constraints.md" >}}).

#### Basic propagator

You must at least call the super constructor to specifies the scope (set of variables) of the propagator.
Then you must implement the two following methods:

```java 
void propagate(int evtmask)
```

> This method applies the global filtering algorithm of the propagator, that is, from *scratch*.
> It is called once during initial propagation (to propagate initial domains) and then during the solving process if
> the propagator is not incremental. It is the most important method of the propagator.

```java 
ESat isEntailed()
```

> This method checks the current state of the propagator. It is used for constraint reification.
> It checks whether the propagator will be always satisfied (`ESat.TRUE`), never satisfied (`ESat.FALSE`)
> or undefined (`ESat.UNDEFINED`) according to the current state of its domain variables. For instance,
> - $A \neq B$ will always be satisfied when $A={0,1,2}$ and $B=\{4,5\}$.
> - $A = B$ will never be satisfied when $A=\{0,1,2\}$ and $B=\{4,5\}$.
> - The entailment of $A \neq B$ cannot be defined when $A=\{0,1,2\}$ and $B=\{1,2,3\}$.

`ESat isEntailed()` implementation may be approximate
but should at least cover the case where all variables are instantiated, in order to check solutions.

Here is an example of how to implement a propagator for `X >= Y`:

```java
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
```

#### Elaborated propagator

The super constructor `super(Variable[], PropagatorPriority, boolean);` brings more information.
`PropagatorPriority` enables to optimize the propagation engine (low arity for fast propagators is better).
The boolean argument allows to specifies the propagator is incremental.
When set to `true`, the method `propagate(int varIdx, int mask)` must be implemented.

**NOTE**: Note that if many variables are modified between two calls, a non-incremental filtering may be faster (and simpler).

The method `propagate(int varIdx, int mask)` defines the incremental filtering.
It is called for every variable `vars[varIdx]` whose domain has changed since the last call.

The method `getPropagationConditions(int vIdx)` enables not to react on every kind of domain modification.

The method `setPassive()` enables to desactivate the propagator when it is entailed, to save time.
The propagator is automatically reactivated upon backtrack.

The method `why(...)` explains the filtering, to allow learning.

Here is an example of how to implement a propagator for `X >= Y`:

```java
// Propagator to apply X >= Y
public final class MyIncrementalPropagator extends Propagator<IntVar> {

    IntVar x, y;

    public MyIncrementalPropagator(IntVar x, IntVar y) {
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
    public String toString() {
        return "prop(" + vars[0].getName() + ".GEQ." + vars[1].getName() + ")";
    }
}
```

### Idempotency

We distinguish two kinds of propagators:

> *Necessary* propagators, which ensure constraints to be satisfied.

> *Redundant* (or *Implied*) propagators that come in addition to some necessary propagators in order to get a stronger filtering.

Some propagators cannot be idempotent (Lagrangian relaxation, use of randomness, etc.).
For some others, forcing idempotency may be very time consuming.
A redundant propagator does not have to be idempotent but **a necessary propagator should be idempotent**  .

Trying to make a propagator idempotent directly may not be straightforward.
We provide three implementation possibilities.

The *decomposed*  (recommended) option:

> Split the original propagator into (partial) propagators so that the fix point is performed through the propagation engine.
> For instance, a channeling propagator $A \Leftrightarrow B$ can be decomposed into two propagators $A \Rightarrow B$ and $B \Rightarrow A$.
> The propagators can (but do not have to) react on fine events.

The *lazy* option:

> Simply post the propagator twice.
> Thus, the fix point is performed through the propagation engine.

The *coarse* option:

> the propagator will perform its fix point by itself.
> The propagator does not react to fine events.
> The coarse filtering algorithm should be surrounded like this:

> ```java
> // In the case of ``SetVar``, replace ``getDomSize()`` by ``getEnvSize()-getKerSize()``.
> long size;
> do{
>   size = 0;
>   for(IntVar v:vars){
>     size+=v.getDomSize();
>   }
>   // really update domain variables here
>   for(IntVar v:vars){
>     size-=v.getDomSize();
>   }
> }while(size>0);
> ```

**NOTE**: Domain variable modifier returns a boolean valued to `true` if the domain variable has been modified.