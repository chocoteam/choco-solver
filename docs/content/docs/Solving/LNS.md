---
title: "Large Neighborhood Search"
date: 2020-01-07T16:06:55+01:00
weight: 35
math: "true"
description: >
  Using LNS
---

Local search techniques are very effective to solve hard optimization problems.
Most of them are, by nature, incomplete.
In the context of constraint programming (CP) for optimization problems, one of the most well-known and widely used local search techniques is the Large Neighborhood Search (LNS) algorithm .
The basic idea is to iteratively relax a part of the problem, then to use constraint programming to evaluate and bound the new solution.

## Principle

LNS is a two-phase algorithm which partially relaxes a given solution and repairs it.
Given a solution as input, the relaxation phase builds a partial solution (or neighborhood) by choosing a set of variables to reset to their initial domain;
The remaining ones are assigned to their value in the solution.
This phase is directly inspired from the classical Local Search techniques.
Even though there are various ways to repair the partial solution, we focus on the technique in which Constraint Programming is used to bound the objective variable and
to assign a value to variables not yet instantiated.
These two phases are repeated until the search stops (optimality proven or limit reached).

The `INeighborFactory` provides pre-defined configurations.
Here is the way to declare LNS to solve a problem:

```java
solver.setLNS(INeighborFactory.random(ivars), new FailCounter(solver, 100));
solver.findOptimalSolution(objective, Model.MINIMIZE);
```

It declares a *random* LNS which, on a solution, computes a partial solution based on `ivars`.
If no solution are found within 100 fails (`FailCounter(solver, 100)`), a restart is forced.

The factory provides other built-in neighbors.

## Neighbors

While the implementation of LNS is straightforward, the main difficulty lies in the design of neighborhoods able to move the search further.
Indeed, the balance between diversification (i.e., evaluating unexplored sub-tree) and intensification (i.e., exploring them exhaustively) should be well-distributed.

### Generic neighbors

One drawback of LNS is that the relaxation process is quite often problem dependent.
Some works have been dedicated to the selection of variables to relax through general concept not related to the class of the problem treated [5,24].
However, in conjunction with CP, only one generic approach, namely Propagation-Guided LNS [24], has been shown to be very competitive with dedicated ones on a variation of the Car Sequencing Problem.
Nevertheless, such generic approaches have been evaluated on a single class of problem and need to be thoroughly parametrized at the instance level, which may be a tedious task to do.
It must, in a way, automatically detect the problem structure in order to be efficient. 
The details of the methods to be implemented are given in the [Advanced section]("docs/Advanced usages/Strategies.md#large-neighborhood-search-lns") 

### Combining neighborhoods

There are two ways to combine neighbors.

#### Sequential

Declare an instance of `SequenceNeighborhood(n1, n2, ..., nm)`.
Each neighbor `ni` is applied in a sequence until one of them leads to a solution.
At step `k`, the $(k \mod m)^{th}$ neighbor is selected.
The sequence stops if at least one of the neighbor is complete.

#### Adaptive

Declare an instance of `AdaptiveNeighborhood(1L, n1, n2, ..., nm)`.
At the beginning a weight $w_i = 1$ is assigned to each neighbor `ni`.

If a neighbor leads to solution, its weight $w_i$ is increased by 1.
Any time a partial solution has to be computed, a value `W` between 1 and $w_1+w_2+...+w_n$ is randomly picked (`1L` is the seed).

Then the weight of each neighbor is subtracted from `W`, as soon as `W`$\leq 0$, the corresponding neighbor is selected.

For instance, let’s consider three neighbors `n1`, `n2` and `n3`, their respective weights $w1=2, w2=4, w3=1$. 3 is randomly assigned to `W`.
Then, the weight of `n1` is subtracted, `W`$- 2 = 1$ ; the weight of `n2` is subtracted, `W`$- 3 = -3$, `W` is less than 0 and n2 is selected.


## Restarts

A generic and common way to reinforce diversification of LNS is to introduce [restarts]({{< ref "Restarts.md" >}}) during the search process.
This technique has proven to be very flexible and to be easily integrated within standard backtracking procedures .

## Walking

A complementary technique that appear to be efficient in practice is named `Walking` and consists in accepting equivalent intermediate solutions in a search iteration instead of requiring a strictly better one.

This can be achieved by defining an `ObjectiveManager` like this:

```java
solver.setObjectiveManager(new ObjectiveManager(objective, ResolutionPolicy.MAXIMIZE, false));
```

Where the last parameter, named `strict` must be set to false to accept equivalent intermediate solutions.

Other optimization policies may be encoded by using either search monitors or a custom `ObjectiveManager`.