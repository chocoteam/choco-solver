---
title: "Math"
date: 2020-06-03T11:18:39+02:00
type: docs
math: "true"
weight: 72
description: >
  A mathematical model of the problem.
---

Variables
---------

-   An integer variable $\text{succ}\_i$ per city *i* is needed. It represents the successor of city *i* in the route. 

    $$\forall i \in [1,C],\\, \text{succ}\_i = [\\![1,C]\\!]$$

-   An integer variable $\text{dist}\_i$ per city *i* is needed. it
    maintains the distance between city *i* and its successor in the route.

    $$\forall i \in [1,C],\\, \text{dist}\_i = [\\![1,M]\\!]$$

    where $M$ is the maximum value in the *D* matrix.

-   An integer variable $totDist$ totals all distances:

    $$totDist = [\\![0, C\times M]\\!]$$

Constraints
-----------

- The distance from a city *i* to its successor should be read from D:

    $$\forall i \in [1,C], \text{dist}\_{i} = \text{D}_{i,\text{succ}\_i}$$

- The route over cities should form an Hamiltonian path.

-   the total distance has then to be maintained:

    $$totDist = \sum_{i = 1}^{C} \text{dist}\_i$$

Objective
---------

The objective is not to simply find a solution but one that minimizes
$totDist$.