#################
Elements of Choco
#################

**********************************
Constraints over integer variables
**********************************

.. _61_constraints_label:

There are two specific constraints ``TRUE`` and ``FALSE`` which do not involved variables.
Posting the ``TRUE`` constraint as is has no effect, however, posting ``FALSE`` prevents a model from having a solution.
Indeed, those two constraints must be used with reification only.


absolute
========

The ``absolute`` constraint involves two variables `VAR1` and `VAR2`.
It ensures that `VAR1` = \| `VAR2` \| .

The API is: ::

    Constraint absolute(IntVar VAR1, IntVar VAR2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 93-96,98
          :emphasize-lines: 96
          :linenos:

    The solutions of the problem are :

     - `X = 0, Y = 0`
     - `X = 1, Y = -1`
     - `X = 1, Y = 1`
     - `X = 2, Y = -2`

alldifferent
============

The `alldifferent` constraints involves two or more integer variables `VARS` and holds that all variables from `VARS` take a different value.
A signature offers the possibility to specify the filtering algorithm to use:

- ``"BC"``: filters on bounds only, based on: "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint", A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek.
- ``"AC"``: filters on the entire domain of the variables. It uses Regin algorithm; it runs in `O(m.n)` worst case time for the initial propagation and then in `O(n+m)` time per arc removed from the support.
- ``"DEFAULT"``: uses ``"BC"`` plus a probabilistic ``"AC"`` propagator to get a compromise between ``"BC"`` and ``"AC"``.

The API are: ::

    Constraint alldifferent(IntVar[] VARS)
    Constraint alldifferent(IntVar[] VARS, String CONSISTENCY)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 225-230,232
          :emphasize-lines: 230
          :linenos:

    Some solutions of the problem are :

     - `X = -1, Y = 2, Z = 5, W = 1`
     - `X = 1, Y = 2, Z = 7, W = 0`
     - `X = 2, Y = 3, Z = 5, W = 0`
     - `X = 2, Y = 4, Z = 7, W = 1`


alldifferent_conditionnal
=========================

The `alldifferent_conditionnal` constraint is a variation of the `alldifferent` constraint.
It holds the `alldifferent` constraint on the subset of variables `VARS` which satisfies the given condition `CONDITION`.

A simple example is the `alldifferent_except_0` variation of the `alldifferent` constraint.

The API are: ::

    Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION)
    Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION, boolean AC)

One can force the `AC` algorithm to be used by calling the second signature.


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 238-246,248
          :emphasize-lines: 246
          :linenos:

    The condition in the example states that the values `1` and `3` can appear more than once, unlike other values.

    Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 1, XS[2] = 1, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 1, XS[1] = 2, XS[2] = 1, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 3, XS[4] = 3`


alldifferent_except_0
=====================

The `alldifferent_except_0` involves an array of variables `VARS`.
It ensures that all variables from `VAR` take a distinct value or 0, that is, all values but 0 can't appear more than once.

The API is: ::

    Constraint alldifferent_except_0(IntVar[] VARS)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 253-255,257
          :emphasize-lines: 255
          :linenos:

    Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 0`
     - `XS[0] = 0, XS[1] = 2, XS[2] = 0, XS[3] = 0`
     - `XS[0] = 2, XS[1] = 1, XS[2] = 0, XS[3] = 0`

among
=====

The `among` constraint involves:

 - an integer variable `NVAR`,
 - an array of integer variables `VARIABLES` and
 - an array of integers.

It holds that `NVAR` is the number of variables of the collection `VARIABLES` that take their value in `VALUES`.

The API is: ::

    Constraint among(IntVar NVAR, IntVar[] VARS, int[] VALUES)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 262-265,267
          :emphasize-lines: 265
          :linenos:

    Some solutions of the problem are :

     - `N = 2, XS[0] = 0, XS[1] = 0, XS[2] = 1, XS[3] = 1`
     - `N = 2, XS[0] = 0, XS[1] = 1, XS[2] = 3, XS[3] = 6`
     - `N = 3, XS[0] = 1, XS[1] = 1, XS[2] = 2, XS[3] = 4`
     - `N = 3, XS[0] = 3, XS[1] = 2, XS[2] = 1, XS[3] = 0`

arithm
======

The constraint `arithm` involves either:

- a integer variable `VAR`, an operator `OP` and a constant `CST`. It holds `VAR` `OP` `CSTE`, where `CSTE` must be chosen in ``{"=", "!=", ">","<",">=","<="}``.
- or two variables `VAR1` and `VAR2` and an operator `OP`. It ensures that `VAR1` `OP` `VAR2`, where `OP` must be chosen in ``{"=", "!=", ">","<",">=","<="}`` .
- or two variables `VAR1` and `VAR2`, two operators `OP1` and `OP2` and an constant `CSTE`. The operators must be different, taken from ``{"=", "!=", ">","<",">=","<="}``  or ``{"+", "-"}``, the constarint ensures that `VAR1` `OP1` `VAR2` `OP2` `CSTE`.


The API are: ::

    Constraint arithm(IntVar VAR, String OP, int CSTE)
    Constraint arithm(IntVar VAR1, String OP, IntVar VAR2)
    Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 48-50,52
          :emphasize-lines: 52
          :linenos:

    The solutions of the problem are :

        - `X = 3`
        - `X = 4`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 104-107,109
          :emphasize-lines: 107
          :linenos:

 The solutions of the problem are :

     - `X = 0, Y = -1`
     - `X = 0, Y = 0`
     - `X = 0, Y = 1`
     - `X = 1, Y = 0`
     - `X = 1, Y = 1`
     - `X = 2, Y = 1`



atleast_nvalues
===============

The `atleast_nvalues` constraint involves:

- an array of integer variables `VARS`,
- an integer variable `NVALUES` and
- a boolean `AC`.

Let `N` be the number of distinct values assigned to the variables of the `VARS` collection.
The constraint enforces the condition `N` :math:`\geq` `NVALUES` to hold.
The boolean `AC` set to true enforces arc-consistency.

The API is: ::

    Constraint atleast_nvalues(IntVar[] VARS, IntVar NVALUES, boolean AC)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 273-276,278
          :emphasize-lines: 276
          :linenos:

 Some solutions of the problem are :

     - `XS[0] = 0 XS[1] = 0 XS[2] = 0 XS[3] = 1 N = 2`
     - `XS[0] = 0 XS[1] = 1 XS[2] = 0 XS[3] = 1 N = 2`
     - `XS[0] = 0 XS[1] = 1 XS[2] = 2 XS[3] = 1 N = 2`
     - `XS[0] = 2 XS[1] = 0 XS[2] = 2 XS[3] = 1 N = 3`
     - `XS[0] = 2 XS[1] = 2 XS[2] = 1 XS[3] = 0 N = 3`

atmost_nvalues
==============

The `atmost_nvalues` constraint involves:

- an array of integer variables `VARS`,
- an integer variable `NVALUES` and
- a boolean `GREEDY`.

Let `N` be the number of distinct values assigned to the variables of the `VARS` collection.
The constraint enforces the condition `N` :math:`\leq` `NVALUES` to hold.
The boolean `GREEDY` set to true filters the conjunction of `atmost_nvalues` and disequalities (see Fages and Lapègue, CP'13 or Artificial Intelligence journal).
It automatically detects disequalities and `alldifferent` constraints. Presumably useful when `NVALUES` must be minimized

The API is: ::

    Constraint atmost_nvalues(IntVar[] VARS, IntVar NVALUES, boolean GREEDY)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 283-286,288
          :emphasize-lines: 286
          :linenos:

 Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 1`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 2`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 3`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 1, N = 2`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 1, XS[3] = 0, N = 2`
     - `XS[0] = 2, XS[1] = 2, XS[2] = 1, XS[3] = 0, N = 3`


bin_packing
===========

The `bin_packing` constraint involves:

 - an array of integer variables `ITEM_BIN`,
 - an array of integers `ITEM_SIZE`,
 - an array of integer variables `BIN_LOAD` and
 - an integer `OFFSET`.

It holds the Bin Packing Problem rules: a set of items with various to pack into bins with respect to the capacity of each bin.

- `ITEM_BIN` represents the bin of each item, that is, `ITEM_BIN[i] = j` states that the i :math:`^{th}` is put in the j :math:`^{th}` bin.
- `ITEM_SIZE` represents the size of each size.
- `BIN_LOAD` represents the load of each bin, that is, the sum of size of the items in it.

This constraint is not a built-in constraint and is based on various propagators.

The API is: ::

    Constraint[] bin_packing(IntVar[] ITEM_BIN, int[] ITEM_SIZE, IntVar[] BIN_LOAD, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 293-297,299
          :emphasize-lines: 297
          :linenos:

 Some solutions of the problem are :

     - `IBIN[0] = 1, IBIN[1] = 1, IBIN[2] = 2, IBIN[3] = 2, IBIN[4] = 3, BLOADS[0] = 5, BLOADS[1] = 5, BLOADS[2] = 2`
     - `IBIN[0] = 1, IBIN[1] = 3, IBIN[2] = 1, IBIN[3] = 2, IBIN[4] = 1, BLOADS[0] = 5, BLOADS[1] = 4, BLOADS[2] = 3`
     - `IBIN[0] = 2, IBIN[1] = 3, IBIN[2] = 1, IBIN[3] = 1, IBIN[4] = 3, BLOADS[0] = 5, BLOADS[1] = 2, BLOADS[2] = 5`



boolean_channeling
==================

The `boolean_channeling` constraint involves:

 - an array of boolean variables `BVARS`,
 - an integer variable `VAR` and
 - an integer `OFFSET`.

It ensures that: `VAR` = `i` :math:`\Leftrightarrow` `BVARS` [ `i-OFFSET` ] = `1`.
The `OFFSET` is typically set to 0.

The API is: ::

    Constraint boolean_channeling(BoolVar[] BVARS, IntVar VAR, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 305-308,310
          :emphasize-lines: 308
          :linenos:

 The solutions of the problem are :

     - `VAR = 1, BVARS[0] = 1, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 0`
     - `VAR = 2, BVARS[0] = 0, BVARS[1] = 1, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 0 `
     - `VAR = 3, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 1, BVARS[3] = 0, BVARS[4] = 0`
     - `VAR = 4, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 1, BVARS[4] = 0`
     - `VAR = 5, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 1`



circuit
=======

The `circuit` constraint involves:

 - an array of integer variables `VARS`,
 - an integer `OFFSET` and
 - a configuration `CONF`.

It ensures that the elements of `VARS` define a covering circuit where `VARS` [i] = `OFFSET` + `j`means that `j` is the successor of `i`.
The filtering algorithms are:

- subtour elimination,
- `alldifferent`,
- dominator-based,
- and strongly connected components based filtering.

The `CONF` is a defined by an ``enum``:

- ``CircuitConf.LIGHT``:
- ``CircuitConf.FIRST``:
- ``CircuitConf.RD``:
- ``CircuitConf.ALL``:

The API are: ::

    Constraint circuit(IntVar[] VARS, int OFFSET, CircuitConf CONF)
    Constraint circuit(IntVar[] VARS, int OFFSET) // with CircuitConf.RD

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 316-318,320
          :emphasize-lines: 318
          :linenos:

 Some solutions of the problem are :

     - `NODES[0] = 1 NODES[1] = 2 NODES[2] = 3 NODES[3] = 4 NODES[4] = 0`
     - `NODES[0] = 3 NODES[1] = 4 NODES[2] = 0 NODES[3] = 1 NODES[4] = 2`
     - `NODES[0] = 4 NODES[1] = 2 NODES[2] = 3 NODES[3] = 0 NODES[4] = 1`
     - `NODES[0] = 4 NODES[1] = 3 NODES[2] = 1 NODES[3] = 0 NODES[4] = 2`


cost_regular
============

The `cost_regular` constraint involves:

 - an array of integer variables `VARS`,
 - an integer variable `COST` and
 - a cost automaton `CAUTOMATON`.

It ensures that the assignment of a sequence of variables `VARS` is recognized by `CAUTOMATON`, a deterministic finite automaton,
and that the sum of the costs associated to each assignment is bounded by the cost variable.
This version allows to specify different costs according to the automaton state at which the assignment occurs (i.e. the transition starts).

The `CAUOTMATON` can be defined using the ``solver.constraints.nary.automata.FA.CostAutomaton` either:

- by creating a ``CostAutomaton``: once created, states should be added, then initial and final states are defined and finally, transitions are declared.
- or by first creating a ``FiniteAutomaton`` and then creating a matrix of costs and finally calling one of the following API:

    + ``ICostAutomaton makeSingleResource(IAutomaton pi, int[][][] costs, int inf, int sup)``
    + ``ICostAutomaton makeSingleResource(IAutomaton pi, int[][] costs, int inf, int sup)``

 The other API of ``CostAutomaton`` (``makeMultiResources(...)``) are dedicated to the `multicost_regular` constraint.

The API is: ::

    Constraint cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON)

.. admonition:: Example

     .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
           :language: java
           :lines: 327-349,351
           :emphasize-lines: 349
           :linenos:

  Some solutions of the problem are :

      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 0, VARS[3] = 0, VARS[4] = 1, COST = 10`
      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 0, VARS[3] = 1, VARS[4] = 1, COST = 9`
      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 1, VARS[3] = 2, VARS[4] = 1, COST = 6`
      - `VARS[0] = 1, VARS[1] = 2, VARS[2] = 1, VARS[3] = 0, VARS[4] = 1, COST = 8`




count
=====

The `count` constraint involves:

 - an integer `VALUE`,
 - an array of integer variables `VARS` and
 - an integer variable `LIMIT`.

The constraint holds that `LIMIT` is equal to the number of variables from  `VARS` assigned to the value `VALUE`.
An alternate signature enables `VALUE` to be an integer variable.

The API are: ::

    Constraint count(int VALUE, IntVar[] VARS, IntVar LIMIT)
    Constraint count(IntVar VALUE, IntVar[] VARS, IntVar LIMIT)

.. admonition:: Example

     .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
           :language: java
           :lines: 356-360,362
           :emphasize-lines: 360
           :linenos:

  Some solutions of the problem are :

      - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 0 VA = 1 CO = 0`
      - `VS[0] = 0 VS[1] = 1 VS[2] = 1 VS[3] = 0 VA = 1 CO = 2`
      - `VS[0] = 0 VS[1] = 2 VS[2] = 2 VS[3] = 1 VA = 3 CO = 0`
      - `VS[0] = 3 VS[1] = 3 VS[2] = 3 VS[3] = 3 VA = 3 CO = 4`


cumulative
==========

The `cumulative` constraints involves:

 - an array of task object `TASKS`,
 - an array of integer variable `HEIGHTS`,
 - an integer variable `CAPACITY` and
 - a boolean `INCREMENTAL`.

It ensures that at each point of the time the cumulated height of the set of tasks that overlap that point does not exceed the given capacity.

The API are: ::

    Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY)
    Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY, boolean INCREMENTAL)

The first API relies on the second, and set `INCREMENTAL` to ``TASKS.length > 500``.

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 368-381,383
          :emphasize-lines: 381
          :linenos:

 Some solutions of the problem are :

     - `S_0 = 0, HE_0 = 0, S_1 = 0, HE_1 = 0, S_2 = 0, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`
     - `S_0 = 4, HE_0 = 0, S_1 = 4, HE_1 = 0, S_2 = 1, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`
     - `S_0 = 0, HE_0 = 1, S_1 = 0, HE_1 = 0, S_2 = 1, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`


diffn
=====

The `diffn` constraint involves:

 - four arrays of integer variables `X`, `Y`, `WIDTH` and `HEIGHT` and
 - a boolean `USE_CUMUL`.

It ensures that each rectangle `i` defined by its coordinates (`X[i]`, `Y[i]`) and its dimensions (`WIDTH[i]`, `HEIGHT[i]`) does not overlap each other.
The option `USE_CUMUL`, recommended, indicates whether or not redundant `cumulative` constraints should be added on each dimension.

The API is: ::

    Constraint[] diffn(IntVar[] X, IntVar[] Y, IntVar[] WIDTH, IntVar[] HEIGHT, boolean USE_CUMUL)


.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 388-397,399
          :emphasize-lines: 381
          :linenos:

 Some solutions of the problem are :

     - `X[0] = 0 X[1] = 1, X[2] = 0, X[3] = 1, Y[0] = 0, Y[1] = 0, Y[2] = 1, Y[3]`
     - `X[0] = 1 X[1] = 0, X[2] = 1, X[3] = 0, Y[0] = 0, Y[1] = 0, Y[2] = 2, Y[3]`
     - `X[0] = 0 X[1] = 1, X[2] = 0, X[3] = 1, Y[0] = 1, Y[1] = 0, Y[2] = 2, Y[3]`



distance
========

The ``distance`` constraint involves either:

- two variables `VAR1` and `VAR2`, an operator `OP` and a constant  `CSTE`. It ensures that \| `VAR1` - `VAR2` \| `OP` `CSTE`, where `OP` must be chosen in ``{"=", "!=", ">","<"}`` .
- or three variables `VAR1`, `VAR2` and `VAR3` and an operator `OP`. It ensures that \| `VAR1` - `VAR2` \| `OP` `VAR3`, where `OP` must be chosen in ``{"=",">","<"}`` .

The API are: ::

    Constraint distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE)
    Constraint distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 114-117,119
          :emphasize-lines: 117
          :linenos:

 The solutions of the problem are :

     - `X = 0, Y = -1`
     - `X = 0, Y = 1`
     - `X = 1, Y = 0`
     - `X = 2, Y = 1`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 159-163,164
          :emphasize-lines: 163
          :linenos:

 The solutions of the problem are :

     - `X = 1, Y = 0, Z = 2`
     - `X = 1, Y = 1, Z = 2`
     - `X = 2, Y = 1, Z = 2`
     - `X = 1, Y = -1, Z = 3`
     - `X = 1, Y = 0, Z = 3`
     - `X = 1, Y = 1, Z = 3`
     - `X = 2, Y = 0, Z = 3`
     - `X = 2, Y = 1, Z = 3`
     - `X = 3, Y = 1, Z = 3`



element
=======

The `element` constraint involves either:

- two variables `VALUE` and `INDEX`, an array of values `TABLE`, an offset `OFFSET` and an ordering property `SORT`. `SORT` must be chosen among:

    + ``"none"``: if values in `TABLE` are not sorted,
    + ``"asc"``: if values in `TABLE` are sorted in increasing order,
    + ``"desc"``: if values in `TABLE` are sorted in decreasing order,
    + ``"detect"``: let the constraint detects the ordering of values in `TABLE`, if any (default value).

- or an integer variable `VALUE`, an array of integer variables `TABLE`, an integer variable `INDEX` and an integer `OFFSET`.

The `element` constraint ensures that `VALUE` = `TABLE` [`INDEX` - `OFFSET`]. `OFFSET` matches `INDEX.LB` and `TABLE[0]` (0 by default).

The API are: ::

    Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX)
    Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT)
    Constraint element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET)



.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 124-127,129
          :emphasize-lines: 127
          :linenos:

 The solutions of the problem are :

     - `V = -2, I = 1`
     - `V = -1, I = 3`
     - `V = 0, I = 4`
     - `V = 1, I = 2`
     - `V = 2, I = 0`


eucl_div
========

The `eucl_div` constraints involves three variables `DIVIDEND`, `DIVISOR` and `RESULT`.
It ensures that `DIVIDEND` / `DIVISOR` = `RESULT`, rounding towards 0.

The API is : ::

    Constraint eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 170-174,176
          :emphasize-lines: 174
          :linenos:

 The solutions of the problem are :

     - `X = 2, Y = 1, Z = 2`
     - `X = 3, Y = 1, Z = 3`


FALSE
=====

The `FALSE` constraint is always unsatisfied. It should only be used with ``LogicalFactory``.

global_cardinality
==================

The `global_cardinality` constraint involves:

 - an array of integer variables `VARS`,
 - an array of integer `VALUES`,
 - an array of integer variables `OCCURRENCES` and
 - a boolean `CLOSED`.

It ensures that each value `VALUES[i]` is taken by exactly `OCCURRENCES[i]` variables in `VARS`.
The boolean `CLOSED` set to `true` restricts the domain of `VARS` to the values defined in `VALUES`.

*The underlying propagator does not ensure a well-defined level of consistency, yet*.

The API is: ::

    Constraint global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 404-408,410
          :emphasize-lines: 408
          :linenos:

 The solutions of the problem are :

     - `VS[0] = 1, VS[1] = 1, VS[2] = 2, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 1, VS[1] = 2, VS[2] = 1, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 1, VS[1] = 2, VS[2] = 2, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 1, VS[2] = 1, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 1, VS[2] = 2, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 2, VS[2] = 1, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`

inverse_channeling
==================

The `inverse_channeling` constraint involves:

 - two arrays of integer variables `VARS1` and `VARS2` and
 - two integers `OFFSET1` and `OFFSET2`.

It ensures that `VARS1[i - OFFSET2] = j` :math:`\Leftrightarrow` `VARS2[j - OFFSET1] = i`.
It performs AC if the domains are enumerated. Otherwise, BC is not guaranteed.
It also automatically imposes one `alldifferent` constraints on each array of variables.

The API is: ::

    Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2, int OFFSET1, int OFFSET2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 404-408,410
          :emphasize-lines: 408
          :linenos:

   The solutions of the problems are:

   - `X[0] = 0, X[1] = 1, X[2] = 2, Y[0] = 1, Y[1] = 2, Y[2] = 3`
   - `X[0] = 0, X[1] = 2, X[2] = 1, Y[0] = 1, Y[1] = 3, Y[2] = 2`
   - `X[0] = 1, X[1] = 0, X[2] = 2, Y[0] = 2, Y[1] = 1, Y[2] = 3`
   - `X[0] = 1, X[1] = 2, X[2] = 0, Y[0] = 3, Y[1] = 1, Y[2] = 2`
   - `X[0] = 2, X[1] = 0, X[2] = 1, Y[0] = 2, Y[1] = 3, Y[2] = 1`
   - `X[0] = 2, X[1] = 1, X[2] = 0, Y[0] = 3, Y[1] = 2, Y[2] = 1`




knapsack
========

The `knapsack` constraint involves:
- an array of integer variables `OCCURRENCES`,
- an integer variable `TOTAL_WEIGHT`,
- an integer variable `TOTAL_ENERGY`,
- an array of integers `WEIGHT` and
- an an array of integers `ENERGY`.

It formulates the Knapsack Problem: to determine the count of each item to include in a collection so that the total weight is less than or equal to a given limit and the total value is as large as possible.

- `OCCURRENCES[i]` :math:`\times` `WEIGHT[i]` :math:`\leq` `TOTAL_WEIGHT` and
- `OCCURRENCES[i]` :math:`\times` `ENERGY[i]` = `TOTAL_ENERGY`.

The API is: ::

    Constraint knapsack(IntVar[] OCCURRENCES, IntVar TOTAL_WEIGHT, IntVar TOTAL_ENERGY,
                                          int[] WEIGHT, int[] ENERGY)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 425-434,436
          :emphasize-lines: 434
          :linenos:

   Some solutions of the problems are:

   - `IT_0 = 0, IT_1 = 0, IT_2 = 0, WE = 0, EN = 0`
   - `IT_0 = 3, IT_1 = 0, IT_2 = 0, WE = 3, EN = 3`
   - `IT_0 = 1, IT_1 = 1, IT_2 = 0, WE = 4, EN = 5`
   - `IT_0 = 2, IT_1 = 1, IT_2 = 0, WE = 5, EN = 6`


lex_chain_less
==============

The `lex_chain_less` constraint involves a matrix of integer variables `VARS`.
It ensures that, for each pair of consecutive arrays `VARS[i]` and `VARS[i+1]`,
`VARS[i]` is lexicographically strictly less than `VARS[i+1]`.

The API is: ::

    Constraint lex_chain_less(IntVar[]... VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 441-445,447
          :emphasize-lines: 445
          :linenos:

   Some solutions of the problems are:

   - `X[0] = -1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 1, Z[2] = 2`
   - `X[0] = 0, X[1] = 1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 2, Z[2] = 0`
   - `X[0] = 1, X[1] = 0, X[2] = 1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 2, Z[2] = 0`
   - `X[0] = -1, X[1] = 1, X[2] = 1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 2, Z[1] = 2, Z[2] = 1`


lex_chain_less_eq
=================

The `lex_chain_less` constraint involves a matrix of integer variables `VARS`.
It ensures that, for each pair of consecutive arrays `VARS[i]` and `VARS[i+1]`,
`VARS[i]` is lexicographically strictly less or equal than `VARS[i+1]`.


lex_less
========

lex_less_eq
===========

maximum
=======

The `maximum` constraints involves a set of integer variables and a third party integer variable, either:

- two integer variables `VAR1` and `VAR2` and an integer variable `MAX`, it ensures that `MAX`= maximum(`VAR1`, `VAR2`).
- or an array of integer variables `VARS` and an integer variable `MAX`, it ensures that `MAX` is the maximum value of the collection of domain variables `VARS`.
- or an array of boolean variables `BVARS` and a booean variable `MAX`, it ensures that `MAX` is the maximum value of the collection of boolean variables `BVARS`.

The API are: ::
    Constraint maximum(IntVar MAX, IntVar VAR1, IntVar VAR2)
    Constraint maximum(IntVar MAX, IntVar[] VARS)
    Constraint maximum(BoolVar MAX, BoolVar[] VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 181-185,187
          :emphasize-lines: 185
          :linenos:

    The solutions of the problem are :

        - `MAX = 2, Y = -1, Z = 2`
        - `MAX = 2, Y = 0, Z = 2`
        - `MAX = 2, Y = 1, Z = 2`
        - `MAX = 3, Y = -1, Z = 3`
        - `MAX = 3, Y = 0, Z = 3`
        - `MAX = 3, Y = 1, Z = 3`




member
======

A constraint which restricts the values a variable can be assigned to with respect to either:

- a given list of values, it involves a integer variable `VAR` and an array of distinct values `TABLE`. It ensures that `VAR` takes its values in `TABLE`.
- or two bounds (included), it involves a integer variable `VAR` and two integer `LB` and  `UB`. It ensures that `VAR` takes its values in [`LB`, `UB`].

The API are: ::

    Constraint member(IntVar VAR, int[] TABLE)
    Constraint member(IntVar VAR, int LB, int UB)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 57-59,61
          :emphasize-lines: 59
          :linenos:

    The solutions of the problem are :

        - `X = 1`
        - `X = 2`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 66-68,70
          :emphasize-lines: 68
          :linenos:

 The solutions of the problem are :

     - `X = 2`
     - `X = 3`
     - `X = 4`



minimum
=======

The `minimum` constraints involves a set of integer variables and a third party integer variable, either:

- two integer variables `VAR1` and `VAR2` and an integer variable `MIN`, it ensures that `MIN`= minimum(`VAR1`, `VAR2`).
- or an array of integer variables `VARS` and an integer variable `MIN`, it ensures that `MIN` is the minimum value of the collection of domain variables `VARS`.
- or an array of boolean variables `BVARS` and a booean variable `MIN`, it ensures that `MIN` is the minimum value of the collection of boolean variables `BVARS`.

The API are: ::
    Constraint minimum(IntVar MIN, IntVar VAR1, IntVar VAR2)
    Constraint minimum(IntVar MIN, IntVar[] VARS)
    Constraint minimum(BoolVar MIN, BoolVar[] VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 192-196,198
          :emphasize-lines: 196
          :linenos:

    The solutions of the problem are :

        - `MIN = 2, Y = -1, Z = 2`
        - `MIN = 2, Y = 0, Z = 2`
        - `MIN = 2, Y = 1, Z = 2`
        - `MIN = 3, Y = -1, Z = 3`
        - `MIN = 3, Y = 0, Z = 3`
        - `MIN = 3, Y = 1, Z = 3`

mod
===

The `mod` constraints involves three variables `X`, `Y` and `Z`.
It ensures that `X` :math:`\mod` `Y` = `Z`.
There is no native constraint for `mod`, so this is reformulated with the help of additional variables.

The API is : ::

    Constraint mod(IntVar X, IntVar Y, IntVar Z)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 203-207,209
          :emphasize-lines: 207
          :linenos:

 The solutions of the problem are :

     - `X = 2, Y = 3, Z = 2`
     - `X = 2, Y = 4, Z = 2`
     - `X = 3, Y = 2, Z = 1`
     - `X = 3, Y = 4, Z = 3`
     - `X = 4, Y = 3, Z = 1`



multicost_regular
=================

not_member
==========

A constraint which prevents a variable to be assigned to some values defined by either:

- a list of values, it involves a integer variable `VAR` and an array of distinct values `TABLE`. It ensures that `VAR` does not take its values in `TABLE`.
- two bounds (included), it involves a integer variable `VAR` and two integer `LB` and  `UB`. It ensures that `VAR` does not take its values in [`LB`, `UB`].

The constraint

The API are: ::

    Constraint not_member(IntVar VAR, int[] TABLE)
    Constraint not_member(IntVar VAR, int LB, int UB)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 75-77,79
          :emphasize-lines: 77
          :linenos:

    The solutions of the problem are :

        - `X = 3`
        - `X = 4`

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 85-87,89
          :emphasize-lines: 87
          :linenos:

    The solution of the problem is :

     - `X = 1`

nvalues
=======


path
====

regular
=======

scalar
======

sort
====

square
======

The ``square`` constraint involves two variables `VAR1` and `VAR2`.
It ensures that `VAR1` = `VAR2`:math:`^2`.

The API is: ::

    Constraint square(IntVar VAR1, IntVar VAR2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 134-137,139
          :emphasize-lines: 137
          :linenos:

 The solutions of the problem are :

     - `X = 1, Y = -1`
     - `X = 0, Y = 0`
     - `X = 1, Y = 1`
     - `X = 4, Y = 2`


subcircuit
==========

subpath
=======

sum
===

table
=====

The ``table`` constraint involves two variables `VAR1` and `VAR2`, a list of pair of values, named `TUPLES` and an algorithm `ALGORITHM`.
It is an extensional constraint enforcing, most of the time, arc-consistency.
The `ALGORITHM` must be chosen among:

- `"AC2001"`: applies the AC2001 algorithm,
- `"AC3"`: applies the AC3 algorithm,
- `"AC3rm"`: applies the AC3rm algorithm,
- `"AC3bit+rm"`: applies the AC3bit+rm algorithm,
- `"FC"`: applies the forward checking algorithm.

The API is: ::

    Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES, String ALGORITHM)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 144-152,154
          :emphasize-lines: 152
          :linenos:

 The solutions of the problem are :

     - `X = 1, Y = 1`
     - `X = 4, Y = 2`


times
=====

The `times` constraints involves either:

- three variables `X`, `Y` and `Z`. It ensures that `X` :math:`\times` `Y` = `Z`.
- or two variables `X` and `Z` and a constant `y`. It ensures that `X` :math:`\times` `y` = `Z`.

The propagator of the `times` constraint filters on bounds only.
If the option is enabled and under certain condition, the `times` constraint may be redefined with a `table` constraint, providing a better filtering algorithm.

The API is : ::

    Constraint times(IntVar X, IntVar Y, IntVar Z)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/ConstraintExamples.java
          :language: java
          :lines: 214-218,220
          :emphasize-lines: 218
          :linenos:

 The solution of the problem is :

     - `X = 2 Y = 3 Z = 6`

tree
====

TRUE
====

The `TRUE` constraint is always satisfied. It should only be used with ``LogicalFactory``.

tsp
===



******************************
Constraints over set variables
******************************

all_different
all_disjoint
all_equal
bool_channel
cardinality
disjoint
element
int_channel
intersection
inverse_set
max
max
member
member
min
min
nbEmpty
notEmpty
offSet
partition
subsetEq
sum
sum
symmetric
union


*******************************
Constraints over real variables
*******************************

*******************
Logical constraints
*******************

and
and
ifThen
ifThen
ifThenElse
ifThenElse
not
or
or
reification


**********
Sat solver
**********


addAtMostNMinusOne
addAtMostOne
addBoolAndArrayEqualFalse
addBoolAndArrayEqVar
addBoolAndArrayEqVar
addBoolAndEqVar
addBoolEq
addBoolIsEqVar
addBoolIsLeVar
addBoolIsLtVar
addBoolIsNEqVar
addBoolLe
addBoolLt
addBoolNot
addBoolOrArrayEqualTrue
addBoolOrArrayEqVar
addBoolOrEqVar
addBoolXorEqVar
addClauses
addClauses
addFalse
addMaxBoolArrayLessEqVar
addSumBoolArrayGreaterEqVar
addSumBoolArrayLessEqKVar
addTrue
buildOnLogicalOperator



********
Searches
********
