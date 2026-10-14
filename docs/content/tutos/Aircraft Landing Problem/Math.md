---
title: "Math"
date: 2020-02-03T13:41:25+01:00
type: docs
math: "true"
weight: 42
description: >
  A mathematical model of the problem.
---

Variables
---------

-   An integer variable $\text{plane}\_i$ per plane *i* indicates its
    landing time.

    $$\forall i \in [1,10],\\, \text{plane}\_i = [\\![LT_{i,0},LT_{i,2}]\\!]$$

-   An integer variable $\text{earliness}\_j$ per plane *i* indicates how
    early a plane lands.

    $$\forall i \in [1,10],\\, \text{earliness}\_i = [\\![0,LT_{i,1} - LT_{i,0}]\\!]$$

-   An integer variable $\text{tardiness}\_j$ per plane *i* indicates how
    late a plane lands.

    $$\forall i \in [1,10],\\, \text{tardiness}\_i = [\\![0,LT_{i,2} - LT_{i,1}]\\!]$$

-   An integer variable $tot_{dev}$ totals all costs:

    $$tot\_{dev} = [\\![0, {+\infty})$$


{{%alert title="Remark" color="primary"%}}
With the current input data, there is no need to distinguish earliness
and tardiness since penalty costs are symetric. A simple distance
between the target landing time and the real landing time is enough.
{{%/alert%}}

Constraints
-----------

-   One plane per runway at a time:

    $$\forall i,j \in [1,10]^2,\\, i\ne j, \text{plane}\_{i} \ne \text{plane}\_{j}$$

    We saw this type of constraint before, it is an *alldifferent*
    constraint.

-   the earliness of a plane *i*:

    $$\forall i \in [1,10],\\, \text{earliness}\_i = max(0,-\text{plane}\_i + \text{LT}\_{i,1})$$

    When the plane is on time or late, its earliness is equal to 0.
    Otherwise, a positive value is computed that corresponds to the
    difference between the real landing time and the target landing
    time.

-   the tardiness of a plane *i*:

    $$\forall i \in [1,10],\\, \text{tardiness}\_i = max(0,\text{plane}\_i - \text{LT}\_{i,1})$$

    When the plane is on time or early, its tardiness is equal to 0.
    Otherwise, a positive value is computed that corresponds to the
    difference between the real landing time and the target landing
    time.

-   the separation time required between two planes has to be satisfied:

    $$\forall i,j \in [1,10]^2,\\, i\ne j, (\text{plane}\_{i} + \text{ST}\_{i,j} \le \text{plane}\_{j}) \oplus (\text{plane}\_{j} + \text{ST}\_{j,i} \le \text{plane}\_{i})$$

    If plane *i* lands before plane *j* then plane *j* lands at least
    `ST[i][j]` after plane *i*. Otherwise, plane *j* lands first and plane
    *i* waits at least `ST[j][i]` after it to land. The two conditions
    cannot hold together because of the XOR logical operator. In this
    case a simple inequality constraint fits the need.

-   the deviation cost has then to be maintained:
    
    $$tot\_{dev} = \sum_{i = 1}^{10} \text{PC}\_{i,0} \cdot \text{earliness}\_i + \text{PC}\_{i,1} \cdot \text{tardiness}\_i$$

Objective
---------

The objective is to find a solution that minimizes tot\_dev.