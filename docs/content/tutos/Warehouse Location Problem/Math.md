---
title: "Math"
date: 2020-02-03T13:27:42+01:00
type: docs
math: "true"
weight: 32
description: >
  A mathematical model of the problem.
---

Variables
---------

- A boolean variable $\text{open}\_i$ per warehouse *i* is needed, set to `true` if the corresponding warehouse is open, `false` otherwise.

    $$\forall i \in [1,5],\\, \text{open}\_i = \\{0,1\\}$$

-   An integer variable $\text{supplier}\_j$ per store *j* is needed, it
    indicates which warehouse supplies it.

    $$\forall j \in [1,10],\\, \text{supplier}\_j = [\\![1,5]\\!]$$

-   An integer variable $\text{cost}\_j$ per store *j* is needed too, it
    stores the cost of being supplied by a warehouse (the range is
    deduced from the matrix P).

    $$\forall j \in [1,10],\\, \text{cost}\_j = [\\![1, 96]\\!]$$

-   An integer variable $tot_cost$ totals all costs:

    $$tot\_{cost} = [\\![1, {+\infty})$$

Constraints
-----------

-   if a warehouse *i* supplies a store *j*, then, the warehouse is
    open:

    $$\forall j \in [1,10], \text{open}\_{\text{supplier}\_j} = 1$$

    Here $\text{supplier}\_j$ defines the index the array $\text{open}$
    to be valuated to 1. This is a encoded with an element constraint.

-   if a warehouse *i* supplies a store *j*, it is related to a specific
    cost:

    $$\forall j \in [1,10], P_{j,\text{supplier}\_{j}} = \text{cost}\_j$$

    Here again, an element constraint is used to bind the supplier and
    the supply cost matrix to the cost of supplying a store.

-   the maximum number of stores a warehouse *i* can supply is limited
    to $K_i$:

    $$\forall i \in [1,5], \sum_{j = 1}^{10} (\text{supplier}\_j == i) = \text{occ}\_i$$
    $$\forall i \in [1,5], \text{occ}\_i \leq K_i$$
    $$\forall i \in [1,5], \text{occ}\_i \geq \text{open}\_i$$

    The first constraint counts the number of occurrences of the value
    *i* in the array supplier and stores the result $\text{occ}\_i$
    variable. This variable is then constrained to be less than or equal
    to $K_i$, to ensure the capacity is satisfied, but also to be
    greater or equal to $\text{open}\_i$ to better propagation.

-   the assignment cost has then to be maintained, including fixed costs
    and supplying costs:

    $$tot\_{cost} = \sum_{i = 1}^{5} 30 \cdot \text{open}\_i + \sum_{j = 1}^{10} \text{cost}\_j$$

Objective
---------

The objective is not to simply find a solution but one that minimizes
$tot\_{cost}$.