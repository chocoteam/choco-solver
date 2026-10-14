---
title: "Search loop"
date: 2020-01-07T16:06:55+01:00
weight: 54
math: "true"
description: >
  Understanding the search loop.
---

The search loop whichs drives the search is a freely-adapted version PLM .
PLM stands for: Propagate, Learn and Move.
Indeed, the search loop is composed of three parts, each of them with a specific goal.


* Propagate: it aims at propagating information throughout the constraint network when a decision is made,


* Learn: it aims at ensuring that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less,


* Move: it aims at, unlike the former ones, not pruning the search space but rather exploring it.

Any component can be freely implemented and attached to the search loop in order to customize its behavior.
There exists some pre-defined Move and Learn implementations.
One can also define its own Move or Learn implementation.

### Implementing a search loop component

A search loop is made of three components, each of them dealing with a specific aspect of the search.
Even if many Move and Learn implementation are already provided, it may be relevant to define its own component.

{{%alert title="Info" %}}
The Propagate component is less prone to be modified, it will not be described here.
However, its interface is minimalist and can be easily implemented.
A look to `org.chocosolver.solver.search.loop.propagate.PropagateBasic.java` is a good starting point.
{{%/alert%}}

The two components can be easily set in the `Solver` search loop:

```java
void setMove(Move m)
```

    The current Move component is replaced by m.

```java
Move getMove()
```

    The current Move component is returned.

`void setLearn(Learn l)` and `Learn getLearn()` are also avaiable.

Having access to the current `Move` (resp. `Learn`) component can be useful to combined it with another one.
For instance, the `MoveLNS` is activated on a solution and creates a partial solution.
It needs another Move to find the first solution and to complete the partial solution.

#### Move

Here is the API of Move:

```java
boolean extend(SearchLoop searchLoop)
```

    Perform a move when the CSP associated to the current node of the search space is not proven to be not consistent.
    It returns true if an extension can be done, false when no more extension is possible.
    It has to maintain the correctness of the reversibility of the action by pushing a backup world when needed.
    An extension is commonly based on a decision, which may be made on one or many variables.
    If a decision is created (thanks to the search strategy), it has to be linked to the previous one.

```java
boolean repair(SearchLoop searchLoop)
```

    Perform a move when the CSP associated to the current node of the search space is proven to be not consistent.
    It returns true if a reparation can be done, false when no more reparation is possible.
    It has to backtracking backup worlds when needed, and unlinked useless decisions.
    The depth and number of backtracks have to be updated too, and “up branch” search monitors of the search loop have to called
    (be careful, when many Move are combined).

```java
Move getChildMove()
```

    It returns the child Move or null.

```java
void setChildMove(Move aMove)
```

    It defined the child Move and erases the previously defined one, if any.

```java 
boolean init()
```

    Called before the search starts, it should initialize the search strategy, if any, and its child Move.
    It should return false if something goes wrong (the problem has trivially no solution), true otherwise.

```java
AbstractStrategy<V> getStrategy()
```

    It returns the search strategy in use, which may be null if none has been defined.

```java
void setStrategy(AbstractStrategy<V> aStrategy)
```

    It defines a search strategy and erases the previously defined one, that is, a service which computes and returns decisions.

`org.chocosolver.solver.search.loop.move.MoveBinaryDFS.java` is good starting point to see how a Move is implemented.
It defines a Depth-First Search with binary decisions.

#### Learn

The aim of the component is to make sure that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less. Here is the API of Learn

```java 
void record(SearchLoop searchLoop)
```

    It validates and records a new piece of knowledge, that is, the current position is a dead-end.
    This is alwasy called *before* calling Move.repair(SearchLoop).

```java
void forget(SearchLoop searchLoop)
```

    It forgets some pieces of knowledge.
    This is alwasy called *after* calling Move.repair(SearchLoop).

