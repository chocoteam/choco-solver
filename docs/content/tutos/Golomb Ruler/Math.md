---
title: "Math"
date: 2020-02-05T16:01:10+01:00
type: docs
math: "true"
weight: 62
description: >
  A mathematical model of the problem.
---

Variables
---------

-   An integer variable $\text{tick}\_i$ per mark *i* indicates its
    position.

    $$\forall i \in [1,m], \text{tick}\_i = [\\![0,9999]\\!]$$

-   An integer variable $\text{diff}\_{i,j}$ per couple of marks $i,j$
    ($i < j$) indicates distance between the two marks.

    $$\forall i,j \in [1,m], i < j, \text{diff}\_{i,j} = [\\![0,9999]\\!]$$

Constraints
-----------

-   Maks are ordered :

    $$\forall i \in [2,m], \text{tick}\_{i-1} < \text{tick}\_i$$

-   Distance between two distinct marks :

    $$\forall i,j \in [1,m], i < j, \text{diff}\_{i,j} = \text{tick}\_j - \text{tick}\_i$$

-   No two pairs of marks are the same distance apart

    $$\forall i,j \in [1,m], i \ne j, \text{tick}\_{i} \ne \text{tick}\_{j}$$

    We saw this type of constraint before, it is an *alldifferent*
    constraint.

-   The first mark can be set to `0`:
    $$\text{tick}\_0 = 0$$

Those variables and constraints are sufficient to define the problem.
However, this initial model can be improved by adding *redundant
constraints* and by *breaking symmetries*.

### Redundant constraints

These types of constraints are not required to find solutions, they are
implied by the other constraints of the model. Thus, their role is to
bring more filtering and to possibly detect infeasible combinations
earlier in the search.

The following reasoning is based on the fact that:

$$\forall i,j \in [1,m], i < j, \text{diff}\_{i,j} = \text{diff}\_{i,i+1} + \ldots + \text{diff}\_{j-1,j}$$

Because all distances must be different, we can estimate the minimal sum
of distances as a sum of *j-i* different positive numbers.

$$\forall i,j \in [1,m], i < j, \text{diff}\_{i,j} \geq \frac\{(j-i) * (j-i+1)}{2}$$

Moreover, remember that

$$\text{diff}\_{1,m} = \text{tick}\_{m} - \text{tick}\_1$$

and

$$\text{diff}\_{1,m} = \text{diff}\_{1,2} + \ldots + \text{diff}\_{i,i+1} + \ldots + \text{diff}\_{m-1,m}$$

Thus, since $\text{tick}\_1$ is equal to 0, we deduce that:

$$\text{tick}\_{m} = \text{diff}\_{1,2} + \ldots + \text{diff}\_{i,i+1} + \ldots + \text{diff}\_{m-1,m}$$

There are *m-1-j+i* different numbers so the upper for
$\text{diff}\_{i,j}$ can be defined as:

$$\forall i,j \in [1,m], i < j, \text{diff}\_{i,j} \leq \text{tick}\_m - \frac{(m - 1 - j + i) * (m - j + i)}{2}$$

To go into details, please read [Barták, "Effective modeling with
constraints"](https://www.researchgate.net/publication/221644589_Effective_Modeling_with_Constraints).

### Symmetry-breaking constraints

These types of constraints aims at breaking symmetries and reducing the
search space size. Indeed, they avoid finding new solutions that are
symmetric to previously found ones.

In a Golomb ruler, an easy way to break symmetries is to define an order
between the first and the last distances:

$$\text{diff}\_{0,1} < \text{diff}\_{m-1,m}$$

Objective
---------

The objective is to find a solution that minimizes the position of the
last mark $\text{tick}_m$.