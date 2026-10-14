---
title: "Math"
date: 2020-02-05T11:22:10+01:00
type: docs
math: "true"
weight: 52
description: >
  A mathematical model of the problem.
---

Variables
---------

-   A boolean variable $\text{cell}\_{i,j}$ per cells of the grid, set to
    true when the cell is shaded, false oterhwise.

    $$\forall i,j \in [1,15]^2, \text{cell}\_{i,j} = \\{0,1\\}$$

And that's enough !

Constraints
-----------

Any column and row should respect its sequence in `BLOCKS`. To do so,
each sequence of `BLOCKS` is turned into a Deterministic Finite
Automaton (DFA). There is a gap between two consecutive blocks, before
the first block and after the last block of a sequence there can be a
gap. The length of each block is injected in the DFA. Any gap is encoded
with a '0's and any block by '1's.

The constraint needed here is named *regular*. It takes a finite
sequence variables and a DFA as input. With the help of a *vocabulary*
(available values, in our case 0 and 1) and a *grammar* (the allowed
sequence), the constraint constructs only valid *words* of a given size
(the variables length). It ensures a very good level of filtering (AC),
but the major difficulty relies on building the DFA.

There are two ways to create it: based on a regular expression or
describing all states and transitions.

Objective
---------

The objective is to find a solution that satisfies each constraint.