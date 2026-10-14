---
title: "Mathematical model"
date: 2020-01-31T14:26:10+01:00
type: docs
math: "true"
weight: 22
description: >
  A mathematical model of the problem.
---


Variables
---------

We associate a variable to each letter: *s, e, n, d, m, o, r, y*:

-   *e, n, d, o, r, y* are defined wih a $[\\![0-9]\\!]$-domain,
-   *s, m* are defined with a $[\\![1-9]\\!]$-domain.

Constraints
-----------

The first constraint to satisfy is that no two letters are assigned to
the same digit:

-   $\forall i,j \in \\{s, e, n, d, m, o, r, y\\}, i\ne j$

Since we handle the constraint "*a word cannot start with à 0*" directly
in the variables domain, the other constraints deal with the equation
itself. There are two options, either a unique scalar product, with no
additional variables, or cut it up wrt columns.

### Globally:

$$1000\times s + 100\times e + 10\times n + 1\times d$$
$$+ 1000\times m + 100\times o + 10\times r + 1\times e$$
$$= 10000\times m + 1000\times o + 100\times n + 10\times e + 1\times y$$

### Locally

$$d + e = y + 10\times r_1$$

$$r_1 + n + r = e +10\times r_2$$

$$r_2 + e + o = n +10\times r_3$$

$$r_3 + s + e = o + 10\times m$$

where $r_1,r_2,r_3$ are $[\\![0-1]\\!]$-domain variables and express the
carries.
