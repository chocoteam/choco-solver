#################
Elements of Choco
#################

.. |gccat| replace::

.. _61_icstr_main:

**********************************
Constraints over integer variables
**********************************



.. _61_icstr_abs:

absolute
========

The ``absolute`` constraint involves two variables `VAR1` and `VAR2`.
It ensures that `VAR1` = \| `VAR2` \| .

**API**:  ::

    Constraint absolute(IntVar VAR1, IntVar VAR2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 98-101,103
          :emphasize-lines: 101
          :linenos:

    The solutions of the problem are :

     - `X = 0, Y = 0`
     - `X = 1, Y = -1`
     - `X = 1, Y = 1`
     - `X = 2, Y = -2`

.. _61_icstr_alld:

alldifferent
============

The `alldifferent` constraints involves two or more integer variables `VARS` and holds that all variables from `VARS` take a different value.
A signature offers the possibility to specify the filtering algorithm to use:

- ``"BC"``: filters on bounds only, based on: "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint", A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek.
- ``"AC"``: filters on the entire domain of the variables. It uses Regin algorithm; it runs in `O(m.n)` worst case time for the initial propagation and then in `O(n+m)` time per arc removed from the support.
- ``"DEFAULT"``: uses ``"BC"`` plus a probabilistic ``"AC"`` propagator to get a compromise between ``"BC"`` and ``"AC"``.

**See also**: `alldifferent <http://sofdem.github.io/gccat/gccat/Calldifferent.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`Regin94`, :cite:`Lopez-OrtizQTB03`.

**API**:  ::

    Constraint alldifferent(IntVar[] VARS)
    Constraint alldifferent(IntVar[] VARS, String CONSISTENCY)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 228-232,234
          :emphasize-lines: 232
          :linenos:

    Some solutions of the problem are :

     - `X = -1, Y = 2, Z = 5, W = 1`
     - `X = 1, Y = 2, Z = 7, W = 0`
     - `X = 2, Y = 3, Z = 5, W = 0`
     - `X = 2, Y = 4, Z = 7, W = 1`

.. _61_icstr_alldc:

alldifferent_conditionnal
=========================

The `alldifferent_conditionnal` constraint is a variation of the `alldifferent` constraint.
It holds the `alldifferent` constraint on the subset of variables `VARS` which satisfies the given condition `CONDITION`.

A simple example is the `alldifferent_except_0` variation of the `alldifferent` constraint.

**API**:  ::

    Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION)
    Constraint alldifferent_conditionnal(IntVar[] VARS, Condition CONDITION, boolean AC)

One can force the `AC` algorithm to be used by calling the second signature.


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 240-248,250
          :emphasize-lines: 248
          :linenos:

    The condition in the example states that the values `1` and `3` can appear more than once, unlike other values.

    Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 1, XS[2] = 1, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 1, XS[1] = 2, XS[2] = 1, XS[3] = 1, XS[4] = 1`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 3, XS[4] = 3`

.. _61_icstr_alld_e0:

alldifferent_except_0
=====================

The `alldifferent_except_0` involves an array of variables `VARS`.
It ensures that all variables from `VAR` take a distinct value or 0, that is, all values but 0 can't appear more than once.

**See also**: `alldifferent_except_0 <http://sofdem.github.io/gccat/gccat/Calldifferent_except_0.html>`_ in the Global Constraint Catalog.

**API**:  ::

    Constraint alldifferent_except_0(IntVar[] VARS)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 255-257,259
          :emphasize-lines: 257
          :linenos:

    Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 2, XS[3] = 0`
     - `XS[0] = 0, XS[1] = 2, XS[2] = 0, XS[3] = 0`
     - `XS[0] = 2, XS[1] = 1, XS[2] = 0, XS[3] = 0`

.. _61_icstr_amo:

among
=====

The `among` constraint involves:

 - an integer variable `NVAR`,
 - an array of integer variables `VARIABLES` and
 - an array of integers.

It holds that `NVAR` is the number of variables of the collection `VARIABLES` that take their value in `VALUES`.

**See also**: `among <http://sofdem.github.io/gccat/gccat/Camong.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`BessiereHHKW05`.

**API**: ::

    Constraint among(IntVar NVAR, IntVar[] VARS, int[] VALUES)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 264-267,269
          :emphasize-lines: 267
          :linenos:

    Some solutions of the problem are :

     - `N = 2, XS[0] = 0, XS[1] = 0, XS[2] = 1, XS[3] = 1`
     - `N = 2, XS[0] = 0, XS[1] = 1, XS[2] = 3, XS[3] = 6`
     - `N = 3, XS[0] = 1, XS[1] = 1, XS[2] = 2, XS[3] = 4`
     - `N = 3, XS[0] = 3, XS[1] = 2, XS[2] = 1, XS[3] = 0`

.. _61_icstr_ari:

arithm
======

The constraint `arithm` involves either:

- a integer variable `VAR`, an operator `OP` and a constant `CST`. It holds `VAR` `OP` `CSTE`, where `CSTE` must be chosen in ``{"=", "!=", ">","<",">=","<="}``.
- or two variables `VAR1` and `VAR2` and an operator `OP`. It ensures that `VAR1` `OP` `VAR2`, where `OP` must be chosen in ``{"=", "!=", ">","<",">=","<="}`` .
- or two variables `VAR1` and `VAR2`, two operators `OP1` and `OP2` and an constant `CSTE`. The operators must be different, taken from ``{"=", "!=", ">","<",">=","<="}``  or ``{"+", "-"}``, the constarint ensures that `VAR1` `OP1` `VAR2` `OP2` `CSTE`.


**API**:  ::

    Constraint arithm(IntVar VAR, String OP, int CSTE)
    Constraint arithm(IntVar VAR1, String OP, IntVar VAR2)
    Constraint arithm(IntVar VAR1, String OP1, IntVar VAR2, String OP2, int CSTE)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 51-53,55
          :emphasize-lines: 53
          :linenos:

    The solutions of the problem are :

        - `X = 3`
        - `X = 4`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 106-109,111
          :emphasize-lines: 109
          :linenos:

 The solutions of the problem are :

     - `X = 0, Y = -1`
     - `X = 0, Y = 0`
     - `X = 0, Y = 1`
     - `X = 1, Y = 0`
     - `X = 1, Y = 1`
     - `X = 2, Y = 1`


.. _61_icstr_atl:

atleast_nvalues
===============

The `atleast_nvalues` constraint involves:

- an array of integer variables `VARS`,
- an integer variable `NVALUES` and
- a boolean `AC`.

Let `N` be the number of distinct values assigned to the variables of the `VARS` collection.
The constraint enforces the condition `N` :math:`\geq` `NVALUES` to hold.
The boolean `AC` set to true enforces arc-consistency.

**See also**: `atleast_nvalues <http://sofdem.github.io/gccat/gccat/Catleast_nvalue.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`Regin95`.

**API**:  ::

    Constraint atleast_nvalues(IntVar[] VARS, IntVar NVALUES, boolean AC)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 275-278,280
          :emphasize-lines: 278
          :linenos:

 Some solutions of the problem are :

     - `XS[0] = 0 XS[1] = 0 XS[2] = 0 XS[3] = 1 N = 2`
     - `XS[0] = 0 XS[1] = 1 XS[2] = 0 XS[3] = 1 N = 2`
     - `XS[0] = 0 XS[1] = 1 XS[2] = 2 XS[3] = 1 N = 2`
     - `XS[0] = 2 XS[1] = 0 XS[2] = 2 XS[3] = 1 N = 3`
     - `XS[0] = 2 XS[1] = 2 XS[2] = 1 XS[3] = 0 N = 3`

.. _61_icstr_atm:

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


**See also**: `atmost_nvalues <http://sofdem.github.io/gccat/gccat/Catmost_nvalue.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint atmost_nvalues(IntVar[] VARS, IntVar NVALUES, boolean GREEDY)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 285-288,290
          :emphasize-lines: 288
          :linenos:

 Some solutions of the problem are :

     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 1`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 2`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 0, N = 3`
     - `XS[0] = 0, XS[1] = 0, XS[2] = 0, XS[3] = 1, N = 2`
     - `XS[0] = 0, XS[1] = 1, XS[2] = 1, XS[3] = 0, N = 2`
     - `XS[0] = 2, XS[1] = 2, XS[2] = 1, XS[3] = 0, N = 3`

.. _61_icstr_bin:

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

**See also**: `bin_packing <http://sofdem.github.io/gccat/gccat/Cbin_packing.html>`_ in the Global Constraint Catalog.

**API**:  ::

    Constraint[] bin_packing(IntVar[] ITEM_BIN, int[] ITEM_SIZE, IntVar[] BIN_LOAD, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 295-299,301
          :emphasize-lines: 299
          :linenos:

 Some solutions of the problem are :

     - `IBIN[0] = 1, IBIN[1] = 1, IBIN[2] = 2, IBIN[3] = 2, IBIN[4] = 3, BLOADS[0] = 5, BLOADS[1] = 5, BLOADS[2] = 2`
     - `IBIN[0] = 1, IBIN[1] = 3, IBIN[2] = 1, IBIN[3] = 2, IBIN[4] = 1, BLOADS[0] = 5, BLOADS[1] = 4, BLOADS[2] = 3`
     - `IBIN[0] = 2, IBIN[1] = 3, IBIN[2] = 1, IBIN[3] = 1, IBIN[4] = 3, BLOADS[0] = 5, BLOADS[1] = 2, BLOADS[2] = 5`


.. _61_icstr_booc:

boolean_channeling
==================

The `boolean_channeling` constraint involves:

 - an array of boolean variables `BVARS`,
 - an integer variable `VAR` and
 - an integer `OFFSET`.

It ensures that: `VAR` = `i` :math:`\Leftrightarrow` `BVARS` [ `i-OFFSET` ] = `1`.
The `OFFSET` is typically set to 0.

**API**:  ::

    Constraint boolean_channeling(BoolVar[] BVARS, IntVar VAR, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 306-309,311
          :emphasize-lines: 309
          :linenos:

 The solutions of the problem are :

     - `VAR = 1, BVARS[0] = 1, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 0`
     - `VAR = 2, BVARS[0] = 0, BVARS[1] = 1, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 0 `
     - `VAR = 3, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 1, BVARS[3] = 0, BVARS[4] = 0`
     - `VAR = 4, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 1, BVARS[4] = 0`
     - `VAR = 5, BVARS[0] = 0, BVARS[1] = 0, BVARS[2] = 0, BVARS[3] = 0, BVARS[4] = 1`


.. _61_icstr_cir:

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

**See also**: `circuit <http://sofdem.github.io/gccat/gccat/Ccircuit.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint circuit(IntVar[] VARS, int OFFSET, CircuitConf CONF)
    Constraint circuit(IntVar[] VARS, int OFFSET) // with CircuitConf.RD

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 316-318,320
          :emphasize-lines: 318
          :linenos:

 Some solutions of the problem are :

     - `NODES[0] = 1 NODES[1] = 2 NODES[2] = 3 NODES[3] = 4 NODES[4] = 0`
     - `NODES[0] = 3 NODES[1] = 4 NODES[2] = 0 NODES[3] = 1 NODES[4] = 2`
     - `NODES[0] = 4 NODES[1] = 2 NODES[2] = 3 NODES[3] = 0 NODES[4] = 1`
     - `NODES[0] = 4 NODES[1] = 3 NODES[2] = 1 NODES[3] = 0 NODES[4] = 2`

.. _61_icstr_creg:

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
- or by first creating a ``FiniteAutomaton`` and then creating a matrix of costs and finally calling one of the following API from ``CostAutomaton``:

    + ``ICostAutomaton makeSingleResource(IAutomaton pi, int[][][] costs, int inf, int sup)``
    + ``ICostAutomaton makeSingleResource(IAutomaton pi, int[][] costs, int inf, int sup)``

 The other API of ``CostAutomaton`` (``makeMultiResources(...)``) are dedicated to the `multicost_regular` constraint.

**Implementation based on**: :cite:`DemasseyPR06`.

**API**:  ::

    Constraint cost_regular(IntVar[] VARS, IntVar COST, ICostAutomaton CAUTOMATON)

.. admonition:: Example

     .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
           :language: java
           :lines: 325-347,349
           :emphasize-lines: 347
           :linenos:

  Some solutions of the problem are :

      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 0, VARS[3] = 0, VARS[4] = 1, COST = 10`
      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 0, VARS[3] = 1, VARS[4] = 1, COST = 9`
      - `VARS[0] = 0, VARS[1] = 0, VARS[2] = 1, VARS[3] = 2, VARS[4] = 1, COST = 6`
      - `VARS[0] = 1, VARS[1] = 2, VARS[2] = 1, VARS[3] = 0, VARS[4] = 1, COST = 8`


.. _61_icstr_cou:


count
=====

The `count` constraint involves:

 - an integer `VALUE`,
 - an array of integer variables `VARS` and
 - an integer variable `LIMIT`.

The constraint holds that `LIMIT` is equal to the number of variables from  `VARS` assigned to the value `VALUE`.
An alternate signature enables `VALUE` to be an integer variable.

**See also**: `count <http://sofdem.github.io/gccat/gccat/Ccount.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint count(int VALUE, IntVar[] VARS, IntVar LIMIT)
    Constraint count(IntVar VALUE, IntVar[] VARS, IntVar LIMIT)

.. admonition:: Example

     .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
           :language: java
           :lines: 354-358,360
           :emphasize-lines: 358
           :linenos:

  Some solutions of the problem are :

      - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 0 VA = 1 CO = 0`
      - `VS[0] = 0 VS[1] = 1 VS[2] = 1 VS[3] = 0 VA = 1 CO = 2`
      - `VS[0] = 0 VS[1] = 2 VS[2] = 2 VS[3] = 1 VA = 3 CO = 0`
      - `VS[0] = 3 VS[1] = 3 VS[2] = 3 VS[3] = 3 VA = 3 CO = 4`

.. _61_icstr_cum:

cumulative
==========

The `cumulative` constraints involves:

 - an array of task object `TASKS`,
 - an array of integer variable `HEIGHTS`,
 - an integer variable `CAPACITY` and
 - a boolean `INCREMENTAL`.

It ensures that at each point of the time the cumulated height of the set of tasks that overlap that point does not exceed the given capacity.


**See also**: `cumulative <http://sofdem.github.io/gccat/gccat/Ccumulative.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY)
    Constraint cumulative(Task[] TASKS, IntVar[] HEIGHTS, IntVar CAPACITY, boolean INCREMENTAL)

The first API relies on the second, and set `INCREMENTAL` to ``TASKS.length > 500``.

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 365-378,380
          :emphasize-lines: 378
          :linenos:

 Some solutions of the problem are :

     - `S_0 = 0, HE_0 = 0, S_1 = 0, HE_1 = 0, S_2 = 0, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`
     - `S_0 = 4, HE_0 = 0, S_1 = 4, HE_1 = 0, S_2 = 1, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`
     - `S_0 = 0, HE_0 = 1, S_1 = 0, HE_1 = 0, S_2 = 1, HE_2 = 1, S_3 = 0, HE_3 = 2 S_4 = 4, HE_4 = 3, CA = 3`

.. _61_icstr_diffn:

diffn
=====

The `diffn` constraint involves:

 - four arrays of integer variables `X`, `Y`, `WIDTH` and `HEIGHT` and
 - a boolean `USE_CUMUL`.

It ensures that each rectangle `i` defined by its coordinates (`X[i]`, `Y[i]`) and its dimensions (`WIDTH[i]`, `HEIGHT[i]`) does not overlap each other.
The option `USE_CUMUL`, recommended, indicates whether or not redundant `cumulative` constraints should be added on each dimension.

**See also**: `diffn <http://sofdem.github.io/gccat/gccat/Cdiffn.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint[] diffn(IntVar[] X, IntVar[] Y, IntVar[] WIDTH, IntVar[] HEIGHT, boolean USE_CUMUL)


.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 385-394,396
          :emphasize-lines: 394
          :linenos:

 Some solutions of the problem are :

     - `X[0] = 0 X[1] = 1, X[2] = 0, X[3] = 1, Y[0] = 0, Y[1] = 0, Y[2] = 1, Y[3]`
     - `X[0] = 1 X[1] = 0, X[2] = 1, X[3] = 0, Y[0] = 0, Y[1] = 0, Y[2] = 2, Y[3]`
     - `X[0] = 0 X[1] = 1, X[2] = 0, X[3] = 1, Y[0] = 1, Y[1] = 0, Y[2] = 2, Y[3]`


.. _61_icstr_dist:

distance
========

The ``distance`` constraint involves either:

- two variables `VAR1` and `VAR2`, an operator `OP` and a constant  `CSTE`. It ensures that \| `VAR1` - `VAR2` \| `OP` `CSTE`, where `OP` must be chosen in ``{"=", "!=", ">","<"}`` .
- or three variables `VAR1`, `VAR2` and `VAR3` and an operator `OP`. It ensures that \| `VAR1` - `VAR2` \| `OP` `VAR3`, where `OP` must be chosen in ``{"=",">","<"}`` .


**See also**: `distance <http://sofdem.github.io/gccat/gccat/Cdistance.html>`_ in the Global Constraint Catalog.

**API**:  ::

    Constraint distance(IntVar VAR1, IntVar VAR2, String OP, int CSTE)
    Constraint distance(IntVar VAR1, IntVar VAR2, String OP, IntVar VAR3)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 116-119,121
          :emphasize-lines: 119
          :linenos:

 The solutions of the problem are :

     - `X = 0, Y = -1`
     - `X = 0, Y = 1`
     - `X = 1, Y = 0`
     - `X = 2, Y = 1`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 161-165,166
          :emphasize-lines: 165
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


.. _61_icstr_elm:

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


**See also**: `element <http://sofdem.github.io/gccat/gccat/Celement.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX)
    Constraint element(IntVar VALUE, int[] TABLE, IntVar INDEX, int OFFSET, String SORT)
    Constraint element(IntVar VALUE, IntVar[] TABLE, IntVar INDEX, int OFFSET)



.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 126-129,131
          :emphasize-lines: 129
          :linenos:

 The solutions of the problem are :

     - `V = -2, I = 1`
     - `V = -1, I = 3`
     - `V = 0, I = 4`
     - `V = 1, I = 2`
     - `V = 2, I = 0`

.. _61_icstr_div:

eucl_div
========

The `eucl_div` constraints involves three variables `DIVIDEND`, `DIVISOR` and `RESULT`.
It ensures that `DIVIDEND` / `DIVISOR` = `RESULT`, rounding towards 0.

The API is : ::

    Constraint eucl_div(IntVar DIVIDEND, IntVar DIVISOR, IntVar RESULT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 172-176,178
          :emphasize-lines: 176
          :linenos:

 The solutions of the problem are :

     - `X = 2, Y = 1, Z = 2`
     - `X = 3, Y = 1, Z = 3`


.. _61_icstr_fal:

FALSE
=====

The `FALSE` constraint is always unsatisfied. It should only be used with ``LogicalFactory``.

.. _61_icstr_gcc:

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

**See also**: `global_cardinality <http://sofdem.github.io/gccat/gccat/Cglobal_cardinality.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint global_cardinality(IntVar[] VARS, int[] VALUES, IntVar[] OCCURRENCES, boolean CLOSED)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 402-406,408
          :emphasize-lines: 406
          :linenos:

 The solutions of the problem are :

     - `VS[0] = 1, VS[1] = 1, VS[2] = 2, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 1, VS[1] = 2, VS[2] = 1, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 1, VS[1] = 2, VS[2] = 2, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 1, VS[2] = 1, VS[3] = 2, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 1, VS[2] = 2, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`
     - `VS[0] = 2, VS[1] = 2, VS[2] = 1, VS[3] = 1, OCC[0] = 0, OCC[1] = 2, OCC[2] = 2`

.. _61_icstr_ich:

inverse_channeling
==================

The `inverse_channeling` constraint involves:

 - two arrays of integer variables `VARS1` and `VARS2` and
 - two integers `OFFSET1` and `OFFSET2`.

It ensures that `VARS1[i - OFFSET2] = j` :math:`\Leftrightarrow` `VARS2[j - OFFSET1] = i`.
It performs AC if the domains are enumerated. Otherwise, BC is not guaranteed.
It also automatically imposes one `alldifferent` constraints on each array of variables.

**API**:  ::

    Constraint inverse_channeling(IntVar[] VARS1, IntVar[] VARS2, int OFFSET1, int OFFSET2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 412-415,417
          :emphasize-lines: 415
          :linenos:

   The solutions of the problems are:

   - `X[0] = 0, X[1] = 1, X[2] = 2, Y[0] = 1, Y[1] = 2, Y[2] = 3`
   - `X[0] = 0, X[1] = 2, X[2] = 1, Y[0] = 1, Y[1] = 3, Y[2] = 2`
   - `X[0] = 1, X[1] = 0, X[2] = 2, Y[0] = 2, Y[1] = 1, Y[2] = 3`
   - `X[0] = 1, X[1] = 2, X[2] = 0, Y[0] = 3, Y[1] = 1, Y[2] = 2`
   - `X[0] = 2, X[1] = 0, X[2] = 1, Y[0] = 2, Y[1] = 3, Y[2] = 1`
   - `X[0] = 2, X[1] = 1, X[2] = 0, Y[0] = 3, Y[1] = 2, Y[2] = 1`


.. _61_icstr_kna:

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

**API**:  ::

    Constraint knapsack(IntVar[] OCCURRENCES, IntVar TOTAL_WEIGHT, IntVar TOTAL_ENERGY,
                                          int[] WEIGHT, int[] ENERGY)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 422-431,433
          :emphasize-lines: 431
          :linenos:

   Some solutions of the problems are:

   - `IT_0 = 0, IT_1 = 0, IT_2 = 0, WE = 0, EN = 0`
   - `IT_0 = 3, IT_1 = 0, IT_2 = 0, WE = 3, EN = 3`
   - `IT_0 = 1, IT_1 = 1, IT_2 = 0, WE = 4, EN = 5`
   - `IT_0 = 2, IT_1 = 1, IT_2 = 0, WE = 5, EN = 6`

.. _61_icstr_lexcl:

lex_chain_less
==============

The `lex_chain_less` constraint involves a matrix of integer variables `VARS`.
It ensures that, for each pair of consecutive arrays `VARS[i]` and `VARS[i+1]`,
`VARS[i]` is lexicographically strictly less than `VARS[i+1]`.

**See also**: `lex_chain_less <http://sofdem.github.io/gccat/gccat/Clex_chain_less.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`CarlssonB02`.

**API**:  ::

    Constraint lex_chain_less(IntVar[]... VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 438-442,444
          :emphasize-lines: 442
          :linenos:

   Some solutions of the problems are:

   - `X[0] = -1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 1, Z[2] = 2`
   - `X[0] = 0, X[1] = 1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 2, Z[2] = 0`
   - `X[0] = 1, X[1] = 0, X[2] = 1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 2, Z[2] = 0`
   - `X[0] = -1, X[1] = 1, X[2] = 1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 2, Z[1] = 2, Z[2] = 1`

.. _61_icstr_lexce:

lex_chain_less_eq
=================

The `lex_chain_less_eq` constraint involves a matrix of integer variables `VARS`.
It ensures that, for each pair of consecutive arrays `VARS[i]` and `VARS[i+1]`,
`VARS[i]` is lexicographically strictly less or equal than `VARS[i+1]`.

**See also**: `lex_chain_less_eq <http://sofdem.github.io/gccat/gccat/Clex_chain_lesseq.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`CarlssonB02`.

**API**:  ::

    Constraint lex_chain_less_eq(IntVar[]... VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 449-453,455
          :emphasize-lines: 453
          :linenos:

   Some solutions of the problems are:

   - `X[0] = -1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 1, Z[2] = 1`
   - `X[0] = -1, X[1] = 1, X[2] = 1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 1, Z[1] = 1, Z[2] = 1`
   - `X[0] = 0, X[1] = 1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1, Z[0] = 2, Z[1] = 1, Z[2] = 2`
   - `X[0] = -1, X[1] = -1, X[2] = 0, Y[0] = 1, Y[1] = 1, Y[2] = 2, Z[0] = 2, Z[1] = 2, Z[2] = 2`

.. _61_icstr_lexl:

lex_less
========

The `lex_less` constraint involves two arrays of integer variables `VARS1` and `VARS2`.
It ensures that `VARS1` is lexicographically strictly less than `VARS2`.

**See also**: `lex_less <http://sofdem.github.io/gccat/gccat/Clex_less.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`FrischHKMW02`.

**API**:  ::

    Constraint lex_less(IntVar[] VARS1, IntVar[] VARS2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 460-463,465
          :emphasize-lines: 463
          :linenos:

   Some solutions of the problems are:

   - `X[0] = -1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1`
   - `X[0] = -1, X[1] = 0, X[2] = 0, Y[0] = 1, Y[1] = 2, Y[2] = 1`
   - `X[0] = -1, X[1] = 0, X[2] = -1, Y[0] = 2, Y[1] = 1, Y[2] = 1`
   - `X[0] = -1, X[1] = -1, X[2] = 0, Y[0] = 2, Y[1] = 2, Y[2] = 2`

.. _61_icstr_lexe:

lex_less_eq
===========

The `lex_less_eq` constraint involves two arrays of integer variables `VARS1` and `VARS2`.
It ensures that `VARS1` is lexicographically strictly less or equal than `VARS2`.

**See also**: `lex_less_eq <http://sofdem.github.io/gccat/gccat/Clex_lesseq.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`FrischHKMW02`.

**API**:  ::

    Constraint lex_less_eq(IntVar[] VARS1, IntVar[] VARS2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 470-473,474
          :emphasize-lines: 473
          :linenos:

   Some solutions of the problems are:

   - `X[0] = -1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1`
   - `X[0] = 1, X[1] = -1, X[2] = -1, Y[0] = 1, Y[1] = 1, Y[2] = 1`
   - `X[0] = 0, X[1] = 0, X[2] = 0, Y[0] = 2, Y[1] = 1, Y[2] = 2`
   - `X[0] = 1, X[1] = 1, X[2] = 1, Y[0] = 2, Y[1] = 2, Y[2] = 2`

.. _61_icstr_max:

maximum
=======

The `maximum` constraints involves a set of integer variables and a third party integer variable, either:

- two integer variables `VAR1` and `VAR2` and an integer variable `MAX`, it ensures that `MAX`= maximum(`VAR1`, `VAR2`).
- or an array of integer variables `VARS` and an integer variable `MAX`, it ensures that `MAX` is the maximum value of the collection of domain variables `VARS`.
- or an array of boolean variables `BVARS` and a booean variable `MAX`, it ensures that `MAX` is the maximum value of the collection of boolean variables `BVARS`.

**See also**: `maximum <http://sofdem.github.io/gccat/gccat/Cmaximum.html>`_ in the Global Constraint Catalog.

**API**:  ::
    Constraint maximum(IntVar MAX, IntVar VAR1, IntVar VAR2)
    Constraint maximum(IntVar MAX, IntVar[] VARS)
    Constraint maximum(BoolVar MAX, BoolVar[] VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 182-187,189
          :emphasize-lines: 187
          :linenos:

    The solutions of the problem are :

        - `MAX = 2, Y = -1, Z = 2`
        - `MAX = 2, Y = 0, Z = 2`
        - `MAX = 2, Y = 1, Z = 2`
        - `MAX = 3, Y = -1, Z = 3`
        - `MAX = 3, Y = 0, Z = 3`
        - `MAX = 3, Y = 1, Z = 3`


.. _61_icstr_mem:

member
======

A constraint which restricts the values a variable can be assigned to with respect to either:

- a given list of values, it involves a integer variable `VAR` and an array of distinct values `TABLE`. It ensures that `VAR` takes its values in `TABLE`.
- or two bounds (included), it involves a integer variable `VAR` and two integer `LB` and  `UB`. It ensures that `VAR` takes its values in [`LB`, `UB`].

**API**:  ::

    Constraint member(IntVar VAR, int[] TABLE)
    Constraint member(IntVar VAR, int LB, int UB)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 59-61,63
          :emphasize-lines: 61
          :linenos:

    The solutions of the problem are :

        - `X = 1`
        - `X = 2`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 68-70,72
          :emphasize-lines: 70
          :linenos:

 The solutions of the problem are :

     - `X = 2`
     - `X = 3`
     - `X = 4`

.. _61_icstr_min:

minimum
=======

The `minimum` constraints involves a set of integer variables and a third party integer variable, either:

- two integer variables `VAR1` and `VAR2` and an integer variable `MIN`, it ensures that `MIN`= minimum(`VAR1`, `VAR2`).
- or an array of integer variables `VARS` and an integer variable `MIN`, it ensures that `MIN` is the minimum value of the collection of domain variables `VARS`.
- or an array of boolean variables `BVARS` and a booean variable `MIN`, it ensures that `MIN` is the minimum value of the collection of boolean variables `BVARS`.

**See also**: `minimum <http://sofdem.github.io/gccat/gccat/Cminimum.html>`_ in the Global Constraint Catalog.

**API**:  ::
    Constraint minimum(IntVar MIN, IntVar VAR1, IntVar VAR2)
    Constraint minimum(IntVar MIN, IntVar[] VARS)
    Constraint minimum(BoolVar MIN, BoolVar[] VARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 194-198,200
          :emphasize-lines: 198
          :linenos:

    The solutions of the problem are :

        - `MIN = 2, Y = -1, Z = 2`
        - `MIN = 2, Y = 0, Z = 2`
        - `MIN = 2, Y = 1, Z = 2`
        - `MIN = 3, Y = -1, Z = 3`
        - `MIN = 3, Y = 0, Z = 3`
        - `MIN = 3, Y = 1, Z = 3`

.. _61_icstr_mod:

mod
===

The `mod` constraints involves three variables `X`, `Y` and `Z`.
It ensures that `X` :math:`\mod` `Y` = `Z`.
There is no native constraint for `mod`, so this is reformulated with the help of additional variables.

The API is : ::

    Constraint mod(IntVar X, IntVar Y, IntVar Z)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 205-209,211
          :emphasize-lines: 209
          :linenos:

 The solutions of the problem are :

     - `X = 2, Y = 3, Z = 2`
     - `X = 2, Y = 4, Z = 2`
     - `X = 3, Y = 2, Z = 1`
     - `X = 3, Y = 4, Z = 3`
     - `X = 4, Y = 3, Z = 1`

.. _61_icstr_mcreg:

multicost_regular
=================

The `multicost_regular` constraint involves:

 - an array of integer variables `VARS`,
 - an array of integer variables `CVARS` and
 - a cost automaton `CAUTOMATON`.

It ensures that the assignment of a sequence of variables `VARS` is recognized by `CAUTOMATON`, a deterministic finite automaton,
and that the sum of the cost array associated to each assignment is bounded by the `CVARS`.
This version allows to specify different costs according to the automaton state at which the assignment occurs (i.e. the transition starts).

The `CAUOTMATON` can be defined using the ``solver.constraints.nary.automata.FA.CostAutomaton` either:

- by creating a ``CostAutomaton``: once created, states should be added, then initial and final states are defined and finally, transitions are declared.
- or by first creating a ``FiniteAutomaton`` and then creating a matrix of costs and finally calling one of the following API from ``CostAutomaton``:

    + ``ICostAutomaton makeMultiResources(IAutomaton pi, int[][][] layer_value_resource, int[] infs, int[] sups)``
    + ``ICostAutomaton makeMultiResources(IAutomaton pi, int[][][][] layer_value_resource_state, int[] infs, int[] sups)``
    + ``ICostAutomaton makeMultiResources(IAutomaton auto, int[][][][] c, IntVar[] z)``
    + ``ICostAutomaton makeMultiResources(IAutomaton auto, int[][][] c, IntVar[] z)``

 The other API of ``CostAutomaton`` (``makeSingleResource(...)``) are dedicated to the `cost_regular` constraint.

**Implementation based on**: :cite:`MenanaD09`.

**API**:  ::

    Constraint multicost_regular(IntVar[] VARS, IntVar[] CVARS, ICostAutomaton CAUTOMATON)

.. admonition:: Example

     *TBD*

.. _61_icstr_nmem:

not_member
==========

A constraint which prevents a variable to be assigned to some values defined by either:

- a list of values, it involves a integer variable `VAR` and an array of distinct values `TABLE`. It ensures that `VAR` does not take its values in `TABLE`.
- two bounds (included), it involves a integer variable `VAR` and two integer `LB` and  `UB`. It ensures that `VAR` does not take its values in [`LB`, `UB`].

The constraint

**API**:  ::

    Constraint not_member(IntVar VAR, int[] TABLE)
    Constraint not_member(IntVar VAR, int LB, int UB)

.. admonition:: Example 1

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 77-79,81
          :emphasize-lines: 79
          :linenos:

    The solutions of the problem are :

        - `X = 3`
        - `X = 4`

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 87-89,91
          :emphasize-lines: 89
          :linenos:

    The solution of the problem is :

     - `X = 1`

.. _61_icstr_nva:

nvalues
=======

The `nvalues` constraint involves:

- an array of integer variables `VARS` and
- an integer variable `NVALUES`.

The constraint ensures that `NVALUES` is the number of distinct values assigned to the variables of the `VARS` array.
This constraint is a combination of the `atleast_nvalues` and `atmost_nvalues` constraints.

This constraint is not a built-in constraint and is based on various propagators.

**See also**: `nvalues <http://sofdem.github.io/gccat/gccat/Cnvalues.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint[] nvalues(IntVar[] VARS, IntVar NVALUES)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 510-513,515
          :emphasize-lines: 513
          :linenos:

    Some solutions of the problem are :

     - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 0 N = 1`
     - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 1 N = 2`
     - `VS[0] = 0 VS[1] = 1 VS[2] = 2 VS[3] = 2 N = 3`

.. _61_icstr_pat:

path
====

The `path` constraint involves:

- an array of integer variables `VARS`,
- an integer variable `START`,
- an integer variable `END` and
- an integer `OFFSET`.

It ensures that the elements of `VARS` define a covering path from `START` to `END`,
where `VARS[i] = OFFSET + j` means that `j` is the successor of `i`.
Moreover, `VARS[END-OFFSET]` = \|`VARS` \|+ `OFFSET`.
The constraint relies on the `circuit` propagators.

**See also**: `path <http://sofdem.github.io/gccat/gccat/Cpath.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint[] path(IntVar[] VARS, IntVar START, IntVar END, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 520-524,526
          :emphasize-lines: 524
          :linenos:

    Some solutions of the problem are :

     - `VS[0] = 1, VS[1] = 2, VS[2] = 3, VS[3] = 4, S = 0, E = 3`
     - `VS[0] = 1, VS[1] = 3, VS[2] = 0, VS[3] = 4, S = 2, E = 3`
     - `VS[0] = 3, VS[1] = 4, VS[2] = 0, VS[3] = 1, S = 2, E = 1`
     - `VS[0] = 4, VS[1] = 3, VS[2] = 1, VS[3] = 0, S = 2, E = 0`

.. _61_icstr_reg:

regular
=======

The `regular` constraint involves:

- an array of integer variables `VARS` and
- a deterministic finite automaton `AUTOMATON`.

It enforces the sequences of `VARS` to be a word recognized by `AUTOMATON`.

There are various ways to declare the automaton:

- create a ``FiniteAutomaton`` and add states, initial and final ones and transitions (see ``FiniteAutomaton`` API for more details),
- create a ``FiniteAutomaton`` with a regexp as argument.


**Implementation based on**: :cite:`Pesant04`.
**API**:  ::

    Constraint regular(IntVar[] VARS, IAutomaton AUTOMATON)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 531-534,536
          :emphasize-lines: 533-534
          :linenos:

    The solutions of the problem are :

     - `CS[0] = 1, CS[1] = 3, CS[2] = 3, CS[3] = 4`
     - `CS[0] = 1, CS[1] = 3, CS[2] = 3, CS[3] = 5`
     - `CS[0] = 2, CS[1] = 3, CS[2] = 3, CS[3] = 4`
     - `CS[0] = 2, CS[1] = 3, CS[2] = 3, CS[3] = 5`


.. _61_icstr_sca:

scalar
======

The `scalar` constraint involves:

- an array of integer variables `VARS`,
- an array of integer `COEFFS`,
- an optional operator `OPERATOR` and
- an integer variable `SCALAR`.

It ensures that `sum(VARS[i]*COEFFS[i]) OPERATOR SCALAR`; where `OPERATOR` must be chosen from ``{"=", "!=", ">","<",">=","<="}``.
The `scalar` constraint filters on bounds only.
The constraint suppress variables with coefficients set to 0, recognizes `sum` (when all coefficients are equal to `-1`, or all equal to `-1`),
and enables, under certain conditions, to reformulate the constraint with a `table` constraint providint AC filtering algorithm.

**See also**: `scalar <http://sofdem.github.io/gccat/gccat/Cscalar_product.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`HarveyS02`.

**API**: ::

    Constraint scalar(IntVar[] VARS, int[] COEFFS, IntVar SCALAR)
    Constraint scalar(IntVar[] VARS, int[] COEFFS, String OPERATOR, IntVar SCALAR)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 541-544,546
          :emphasize-lines: 544
          :linenos:

    Some solutions of the problem are :

     - `CS[0] = 1, CS[1] = 1, CS[2] = 1, CS[3] = 1, R = 10`
     - `CS[0] = 1, CS[1] = 2, CS[2] = 3, CS[3] = 1, R = 18`
     - `CS[0] = 1, CS[1] = 4, CS[2] = 2, CS[3] = 1, R = 19`
     - `CS[0] = 1, CS[1] = 2, CS[2] = 1, CS[3] = 3, R = 20`

.. _61_icstr_sor:

sort
====

The `sort` constraint involves two arrays of integer variables `VARS` and `SORTEDVARS`.
It ensures that the variables of `SORTEDVARS` correspond to the variables of `VARS` according to a permutation.
Moreover, the variable of `SORTEDVARS` are sorted in increasing order.

**See also**: `sort <http://sofdem.github.io/gccat/gccat/Csort.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`MehlhornT00`.

**API**: ::

    Constraint sort(IntVar[] VARS, IntVar[] SORTEDVARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 553-556,558
          :emphasize-lines: 556
          :linenos:

    Some solutions of the problem are :

     - `X[0] = 0, X[1] = 0, X[2] = 0, Y[0] = 0, Y[1] = 0, Y[2] = 0`
     - `X[0] = 1, X[1] = 0, X[2] = 2, Y[0] = 0, Y[1] = 1, Y[2] = 2`
     - `X[0] = 2, X[1] = 1, X[2] = 0, Y[0] = 0, Y[1] = 1, Y[2] = 2`
     - `X[0] = 2, X[1] = 1, X[2] = 2, Y[0] = 1, Y[1] = 2, Y[2] = 2`

.. _61_icstr_squa:

square
======

The ``square`` constraint involves two variables `VAR1` and `VAR2`.
It ensures that `VAR1` = `VAR2`:math:`^2`.

**API**:  ::

    Constraint square(IntVar VAR1, IntVar VAR2)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 136-139,141
          :emphasize-lines: 139
          :linenos:

 The solutions of the problem are :

     - `X = 1, Y = -1`
     - `X = 0, Y = 0`
     - `X = 1, Y = 1`
     - `X = 4, Y = 2`

.. _61_icstr_scir:

subcircuit
==========

The `subcircuit` constraint involves:

- an array of integer variables `VARS`,
- an integer `OFFSET` and
- an integer variable `SUBCIRCUIT_SIZE`.

It ensures that the elements of `VARS` define a single circuit of `SUBCIRCUIT_SIZE` nodes where:

- `VARS[i] = OFFSET+j` means that `j` is the successor of `i`,
- `VARS[i] = OFFSET+i` means that `i` is not part of the circuit.

It also ensures that \| `{VARS[i]` :math:`\neq` `OFFSET+i}` \| = `SUBCIRCUIT_SIZE`.

**API**:  ::

    Constraint subcircuit(IntVar[] VARS, int OFFSET, IntVar SUBCIRCUIT_SIZE)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 562-565,567
          :emphasize-lines: 565
          :linenos:

 Some solutions of the problem are :

     - `NS[0] = 0, NS[1] = 1, NS[2] = 2, NS[3] = 4, NS[4] = 3, SI = 2`
     - `NS[0] = 4, NS[1] = 1, NS[2] = 2, NS[3] = 3, NS[4] = 0, SI = 2`
     - `NS[0] = 1, NS[1] = 2, NS[2] = 0, NS[3] = 3, NS[4] = 4, SI = 3`
     - `NS[0] = 3, NS[1] = 1, NS[2] = 2, NS[3] = 4, NS[4] = 0, SI = 3`

.. _61_icstr_spat:

subpath
=======

The `subpath` constraint involves:

- an array of integer variables `VARS`,
- an integer variable `START`,
- an integer variable `END`,
- an integer `OFFSET` and
- an integer variable `SIZE`.

It ensures that the elements of `VARS` define a path of `SIZE` vertices, leading from `START` to `END` where:

+ `VARS[i] = OFFSET+j` means that `j` is the successor of `i`,
+ `VARS[i] = OFFSET+i` means that vertex `i` is excluded from the path.
Moreover, `VARS[END-OFFSET]` = \| `VARS` \| +`OFFSET`.

**See also**: `subpath <http://sofdem.github.io/gccat/gccat/Cpath_from_to.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`tbd`.

**API**:  ::

    Constraint[] subpath(IntVar[] VARS, IntVar START, IntVar END, int OFFSET, IntVar SIZE)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 572-577,579
          :emphasize-lines: 579
          :linenos:

 Some solutions of the problem are :

     - `VS[0] = 1, VS[1] = 4, VS[2] = 2, VS[3] = 3, S = 0, E = 1, SI = 2`
     - `VS[0] = 4, VS[1] = 1, VS[2] = 2, VS[3] = 0, S = 3, E = 0, SI = 2`
     - `VS[0] = 3, VS[1] = 1, VS[2] = 4, VS[3] = 2, S = 0, E = 2, SI = 3`
     - `VS[0] = 0, VS[1] = 2, VS[2] = 4, VS[3] = 1, S = 3, E = 2, SI = 3`


.. _61_icstr_sum:

sum
===

The `sum` constraint involves:

- an array of integer (or boolean) variables `VARS`,
- an optional operator `OPERATOR` and
- an integer variable `SUM`.

It ensures that `sum(VARS[i]) OPERATOR SUM`; where operator must be chosen among ``{"=", "!=", ">","<",">=","<="}``.
If no operator is defined, ``"="`` is set by default.
Note that when the operator differs from ``"="``, an intermediate variable is declared and an `arithm` constraint is returned.
For performance reasons, a specialization for boolean variables is provided.

**See also**: `scalar <http://sofdem.github.io/gccat/gccat/Cscalar_product.html>`_ in the Global Constraint Catalog.

**Implementation based on**: *Bounds Consistency Techniques for Long Linear Constraints*. [#cscalar]_

**API**:  ::

    Constraint sum(IntVar[] VARS, IntVar SUM)
    Constraint sum(IntVar[] VARS, String OPERATOR, IntVar SUM)
    Constraint sum(BoolVar[] VARS, IntVar SUM)
    Constraint sum(BoolVar[] VARS, String OPERATOR, IntVar SUM)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 583-587,589
          :emphasize-lines: 587
          :linenos:

 Some solutions of the problem are :

     - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 0 SU = 2`
     - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 2 SU = 2`
     - `VS[0] = 0 VS[1] = 0 VS[2] = 0 VS[3] = 3 SU = 3`
     - `VS[0] = 1 VS[1] = 1 VS[2] = 0 VS[3] = 0 SU = 3`

.. _61_icstr_tab:

table
=====

The ``table`` constraint involves either:

- two variables `VAR1` and `VAR2`, a list of pair of values, named `TUPLES` and an algorithm `ALGORITHM`.
- or an array of variables `VARS`, a list of tuples of values, named `TUPLES` and an algorithm `ALGORITHM`.
It is an extensional constraint enforcing, most of the time, arc-consistency.

When only two variables are involved, the available algorithms are:

- ``"AC2001"``: applies the AC2001 algorithm,
- ``"AC3"``: applies the AC3 algorithm,
- ``"AC3rm"``: applies the AC3rm algorithm,
- ``"AC3bit+rm"``: (default) applies the AC3bit+rm algorithm,
- ``"FC"``: applies the forward checking algorithm.


When more than two variables are involved, the available algorithms are:

- ``"GAC2001"``: applies the GAC2001 algorithm,
- ``"GAC2001+"``: applies the GAC2001 algorithm for allowed tuples only,
- ``"GAC3rm"``: applies the GAC3 algorithm,
- ``"GAC3rm+"``: (default) applies the GAC3rm algorithm for allowed tuples only,
- ``"GACSTR+"``: applies the GAC version STR for allowed tuples only,
- ``"STR2+"``: applies the GAC STR2 algorithm for allowed tuples only,
- ``"FC"``: applies the forward checking algorithm.


**Implementation based on**: TBD. [#table]_

.. [#table] TBD.

**API**:  ::

    Constraint table(IntVar VAR1, IntVar VAR2, Tuples TUPLES, String ALGORITHM)
    Constraint table(IntVar[] VARS, Tuples TUPLES, String ALGORITHM)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 146-154,156
          :emphasize-lines: 154
          :linenos:

 The solutions of the problem are :

     - `X = 1, Y = 1`
     - `X = 4, Y = 2`

.. _61_icstr_tim:

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

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 216-220,222
          :emphasize-lines: 220
          :linenos:

 The solution of the problem is :

     - `X = 2 Y = 3 Z = 6`

.. _61_icstr_tree:

tree
====

The `tree` constraint involves:

- an array of integer variables `SUCCS`,
- an integer variable `NBTREES` and
- an integer `OFFSET`.

It partitions the `SUCCS` variables into `NBTREES` (anti) arborescences:

- `SUCCS[i] = OFFSET+j` means that `j` is the successor of `i`,
- `SUCCS[i] = OFFSET+i` means that `i` is a root.


**See also**: `tree <http://sofdem.github.io/gccat/gccat/Ctree.html>`_ in the Global Constraint Catalog.

**Implementation based on**: :cite:`FagesL11`.

**API**:  ::

    Constraint tree(IntVar[] SUCCS, IntVar NBTREES, int OFFSET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 594-597,599
          :emphasize-lines: 597
          :linenos:

 Some solutions of the problem are :

     - `VS[0] = 0, VS[1] = 1, VS[2] = 1, VS[3] = 1, NT = 2`
     - `VS[0] = 1, VS[1] = 1, VS[2] = 2, VS[3] = 1, NT = 2`
     - `VS[0] = 2, VS[1] = 0, VS[2] = 2, VS[3] = 3, NT = 2`
     - `VS[0] = 0, VS[1] = 3, VS[2] = 2, VS[3] = 3, NT = 3`
     - `VS[0] = 3, VS[1] = 1, VS[2] = 2, VS[3] = 3, NT = 3`


.. _61_icstr_tru:

TRUE
====

The `TRUE` constraint is always satisfied. It should only be used with ``LogicalFactory``.


.. _61_icstr_tsp:

tsp
===

The `tsp` constraint involves:

- an array of integer variables `SUCCS`,
- an integer variable `COST` and
- a matrix of integers `COST_MATRIX`.

It formulates the Travelling Salesman Problem: the variables `SUCCS` form a hamiltonian circuit of value `COST`.
Going from `i` to `j`, `SUCCS[i] = j`, costs `COST_MATRIX[i][j]`.

This constraint is not a built-in constraint and is based on various propagators.


**API**:  ::

    Constraint[] tsp(IntVar[] SUCCS, IntVar COST, int[][] COST_MATRIX)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/IntConstraintExamples.java
          :language: java
          :lines: 603-607,609
          :emphasize-lines: 607
          :linenos:

 The solutions of the problem are :

     - `VS[0] = 2, VS[1] = 0, VS[2] = 3, VS[3] = 1, CO = 8`
     - `VS[0] = 3, VS[1] = 0, VS[2] = 1, VS[3] = 2, CO = 10`
     - `VS[0] = 1, VS[1] = 2, VS[2] = 3, VS[3] = 0, CO = 10`
     - `VS[0] = 3, VS[1] = 2, VS[2] = 0, VS[3] = 1, CO = 14`
     - `VS[0] = 1, VS[1] = 3, VS[2] = 0, VS[3] = 2, CO = 8`
     - `VS[0] = 2, VS[1] = 3, VS[2] = 1, VS[3] = 0, CO = 14`


.. _61_scstr_main:

******************************
Constraints over set variables
******************************

.. _61_scstr_alldif:

all_different
=============

The `all_different` constraint involves an array of set variables `SETS`.
It ensures that sets in `SETS` are all different (not necessarily disjoint).
Note that there cannot be more than two empty sets.

**API**:  ::

    Constraint all_different(SetVar[] SETS)

.. _61_scstr_alldis:

all_disjoint
============

The `all_disjoint` constraint involves an array of set variables `SETS`.
It ensures that all sets from `SETS` are disjoint.
Note that there can be multiple empty sets.

**API**:  ::

    Constraint all_disjoint(SetVar[] SETS)


.. _61_scstr_alleq:

all_equal
=========

The `all_equal` constraint involves an array of set variables `SETS`.
It ensures that sets in `SETS` are all equal.

**API**:  ::

    Constraint all_equal(SetVar[] SETS)

.. _61_scstr_bcha:

bool_channel
============

The `bool_channel` constraint involves:

- an array of boolean variables `BOOLEANS`,
- a set variable `SET` and
- an integer `OFFSET`.

It channels `BOOLEANS` and `SET` such that : `i` :math:`\in` SET :math:`\Leftrightarrow` `BOOLEANS[i-OFFSET] = 1`.

**API**:  ::

    Constraint bool_channel(BoolVar[] BOOLEANS, SetVar SET, int OFFSET)

.. _61_scstr_card:

cardinality
===========

The `cardinality` constraint involves:

- a set variable `SET` and
- an integer variable `CARD`.

It ensures that \| `SET_VAR` \| = `CARD`.

The API is : ::

    Constraint cardinality(SetVar SET, IntVar CARD)

.. _61_scstr_dis:

disjoint
========

The `disjoint` constraint involves two set variables `SET_1` and `SET_2`.
It ensures that `SET_1` and `SET_2` are disjoint, that is, they cannot contain the same element.
Note that they can be both empty.

**API**:  ::

    Constraint disjoint(SetVar SET_1, SetVar SET_2)

.. _61_scstr_elm:

element
=======

The `element` constraint involves:

- an integer variable `INDEX`,
- and array of set variables `SETS`,
- an integer `OFFSET` and
- a set variable `SET`.

It ensures that `SETS[INDEX-OFFSET] = SET`.

**API**:  ::

    Constraint element(IntVar INDEX, SetVar[] SETS, int OFFSET, SetVar SET)

.. _61_scstr_icha:

int_channel
===========

The `int_channel` constraint involves:

- an array of set variables `SETS`,
- an array of integer variables `INTEGERS`,
- two integers `OFFSET_1` and `OFFSET_2`.

It ensures that: `x` :math:`\in` `SETS[y-OFFSET_1]` :math:`\Leftrightarrow` `INTEGERS[x-OFFSET_2] = y`.

The API is : ::

    Constraint int_channel(SetVar[] SETS, IntVar[] INTEGERS, int OFFSET_1, int OFFSET_2)


.. _61_scstr_int:

intersection
============

The `intersection` constraint involves:

- an array of set variables `SETS` and
- a set variable `INTERSECTION`.

It ensures that `INTERSECTION` is the intersection of the sets `SETS`.

The API is : ::

    Constraint intersection(SetVar[] SETS, SetVar INTERSECTION)

.. _61_scstr_inv:

inverse_set
===========

The `inverse_set` constraint involves:

 - an array of set variables `SETS`,
 - an array of set variable `INVERSE_SETS` and
 - two integers `OFFSET_1` and `OFFSET_2`.

It ensures that `x :math:`\in` `SETS[y-OFFSET_1]` :math:`\Leftrightarrow` y :math:`\in` `INVERSE_SETS[x-OFFSET_2]`.

**API**:  ::

    Constraint inverse_set(SetVar[] SETS, SetVar[] INVERSE_SETS, int OFFSET_1, int OFFSET_2)

.. _61_scstr_max:

max
===

The `max` constraint involves:

- either:

    + a set variable `SET`,
    + an integer variable `MAX_ELEMENT_VALUE` and
    + a boolean `NOT_EMPTY`.

    It ensures that `MIN_ELEMENT_VALUE` is equal to  the maximum element of `SET`.

- or:

    + a set variable `SET`,
    + an array of integer `WEIGHTS`,
    + an integer `OFFSET`,
    + an integer variable `MAX_ELEMENT_VALUE` and
    + a boolean `NOT_EMPTY`.

    It ensures that `max(WEIGHTS[i-OFFSET] | i in INDEXES) = MAX_ELEMENT_VALUE`.

The boolean `NOT_EMPTY` set to `true` states that `INDEXES` cannot be empty.

**API**:  ::

    Constraint max(SetVar SET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY)
    Constraint max(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MAX_ELEMENT_VALUE, boolean NOT_EMPTY)

.. _61_scstr_mem:

member
======

The `member` constraint involves:

- either:

    + an array of set variables `SETS` and
    + a set variable `SET`.

    It ensures that `SET` belongs to `SETS`.

- or:

    + an integer variable `INTEGER` and
    + a set variable `SET`.

    It ensures that `INTEGER` is included in `SET`.

**API**:  ::

        Constraint member(SetVar[] SETS, SetVar SET)
        Constraint member(IntVar INTEGER, SetVar SET)

.. _61_scstr_min:

min
===

The `min` constraint involves:

- either:

    + a set variable `SET`,
    + an integer variable `MIN_ELEMENT_VALUE` and
    + a boolean `NOT_EMPTY`.

    It ensures that `MIN_ELEMENT_VALUE` is equal to  the minimum element of `SET`.

- or:

    + a set variable `SET`,
    + an array of integer `WEIGHTS`,
    + an integer `OFFSET`,
    + an integer variable `MAX_ELEMENT_VALUE` and
    + a boolean `NOT_EMPTY`.

    It ensures that `min(WEIGHTS[i-OFFSET] | i in INDEXES) = MIN_ELEMENT_VALUE`.

The boolean `NOT_EMPTY` set to `true` states that `INDEXES` cannot be empty.

**API**:  ::

    Constraint min(SetVar SET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY)
    Constraint min(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar MIN_ELEMENT_VALUE, boolean NOT_EMPTY)


.. _61_scstr_nbe:

nbEmpty
=======

The `nbEmpty` constraint involves:

- an array of set variables `SETS` and
- an integer variable `NB_EMPTY_SETS`.

It restricts the number of empty sets in `SETS` to be equal `NB_EMPTY_SET`.

**API**:  ::

    Constraint nbEmpty(SetVar[] SETS, IntVar NB_EMPTY_SETS)

.. _61_scstr_note:

notEmpty
========

The `notEmpty` constraint involves a set variable `SET`.

It prevents `SET` to be empty.

**API**:  ::

    Constraint notEmpty(SetVar SET)


.. _61_scstr_off:

offSet
======

The `offset` constraint involves:

- two set variables `SET_1` and `SET_2` and
- an integer `OFFSET`.

It ensures that to any value `x` in `SET_1`, the value `x+OFFSET` is in `SET_2` (and reciprocally).

**API**:  ::

    Constraint offSet(SetVar SET_1, SetVar SET_2, int OFFSET)


.. _61_scstr_part:

partition
=========

The `partition` constraint involves:

- an array of set variables `SETS` and
- a set variable `UNIVERSE`.

It ensures that `UNVIVERSE` is partitioned in disjoint sets `SETS`.

**API**:  ::

    Constraint partition(SetVar[] SETS, SetVar UNIVERSE)

.. _61_scstr_sse:

subsetEq
========

The `subsetEq` constraint involves an array of set variables `SETS`.
It ensures that `i<j` :math:`\Leftrightarrow` `SET_VARS[i]` :math:`\subseteq` `SET_VARS[j]`.

The API is : ::

    Constraint subsetEq(SetVar[] SETS)

.. _61_scstr_sum:

sum
===

The `sum` constraint involves:

- a set variables `INDEXES`,
- an array of integer `WEIGHTS`,
- an integer `OFFSET`,
- an integer variable `SUM` and
- a boolean `NOT_EMPTY`.

The constraint ensures that `sum(WEIGHTS[i-OFFSET] | i in INDEXES) = SUM`.
The boolean `NOT_EMPTY` set to `true` states that `INDEXES` cannot be empty.

**API**:  ::

    Constraint sum(SetVar INDEXES, int[] WEIGHTS, int OFFSET, IntVar SUM, boolean NOT_EMPTY)

.. _61_scstr_sym:

symmetric
=========

The `symmetric` constraint involves:

- an array of set variables `SETS` and
- an integer `OFFSET`.

It ensures that: `x` :math:`\in`  `SETS[y-OFFSET]` :math:`\Leftrightarrow` `y` :math:`\in` `SETS[x-OFFSET]`.

**API**:  ::

    Constraint symmetric(SetVar[] SETS, int OFFSET)

.. _61_scstr_uni:

union
=====

The `union` constraint involves:

- an array of set variables `SETS` and
- a set variable `UNION`.

It ensures that `SET_UNION` is equal to the union if the sets in `SET_VARS`.

The API is : ::

    Constraint union(SetVar[] SETS, SetVar UNION)


.. _61_rcstr_main:

*******************************
Constraints over real variables
*******************************


.. _61_lcstr_main:

*******************
Logical constraints
*******************

.. _61_lcstr_and:

and
===

.. _61_lcstr_it:

ifThen
======

.. _61_lcstr_ite:

ifThenElse
==========

.. _61_lcstr_not:

not
===

.. _61_lcstr_or:

or
==

.. _61_lcstr_rei:

reification
===========


.. _61_satsolver:

**********
Sat solver
**********


A SAT solver is embedded in Choco. It should not be acessed directly but clauses can be added using the ``solver.constraints.SatFactory``.
The SAT solver is considered as a constraint which receives and generates events on boolean variables, that's why it is referred as SAT constraint in the following.

addAtMostNMinusOne
==================

Add a clause to the SAT constraint whic states that:
`BOOLVARS`:math:`_1` :math:`+` `BOOLVARS`:math:`_2` :math:`+` ... :math:`+` `BOOLVARS`:math:`_n` :math:`<` `\|BOOLVARS\|`.


**API**: ::

    boolean addAtMostNMinusOne(BoolVar[] BOOLVARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 48-50,52
          :emphasize-lines: 50
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, BS[3] = 0`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 1, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 1, BS[3] = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 0`



addAtMostOne
============

Add a clause to the SAT constraint whic states that:
`BOOLVARS`:math:`_1` :math:`+` `BOOLVARS`:math:`_2` :math:`+` ... :math:`+` `BOOLVARS`:math:`_n` :math:`\leq` 1.


**API**: ::

    boolean addAtMostOne(BoolVar[] BOOLVARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 57-59,61
          :emphasize-lines: 59
          :linenos:

    The solutions of the problem are :

        - `BS[0] = 1, BS[1] = 0, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 1, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 0`


addBoolAndArrayEqualFalse
=========================

Add a clause to the SAT constraint whic states that:
:math:`|not`(`BOOLVARS`:math:`_1` :math:`\land` `BOOLVARS`:math:`_2` :math:`\land` ... :math:`\land` `BOOLVARS`:math:`_n`).

**API**:

    boolean addBoolAndArrayEqualFalse(BoolVar[] BOOLVARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 66-68,70
          :emphasize-lines: 68
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, BS[3] = 0`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 1, BS[3] = 1`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 0, BS[3] = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 0`

addBoolAndArrayEqVar
====================

Add a clause to the SAT constraint which states that:
(`BOOLVARS`:math:`_1` :math:`\land` `BOOLVARS`:math:`_2` :math:`\land` ... :math:`\land` `BOOLVARS`:math:`_n`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolAndArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 179-182,184
          :emphasize-lines: 182
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, BS[3] = 1 T = 1`
        - `BS[0] = 1, BS[1] = 1, BS[2] = 0, BS[3] = 1, T = 0`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 0, BS[3] = 0, T = 0`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 0, T = 0`

addBoolAndEqVar
===============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`\land` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolAndEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 84-88,90
          :emphasize-lines: 88
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 1`
        - `L = 1, R = 0, T = 0`
        - `L = 0, R = 1, T = 0`
        - `L = 0, R = 0, T = 0`

addBoolEq
=========

Add a clause to the SAT constraint which states that the two boolean variables `LEFT` and `RIGHT` are equal.

**API**: ::

    boolean addBoolEq(BoolVar LEFT, BoolVar RIGHT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 95-98,100
          :emphasize-lines: 98
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1`
        - `L = 0, R = 0`

addBoolIsEqVar
==============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`=` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolIsEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 105-109,111
          :emphasize-lines: 109
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 1`
        - `L = 1, R = 0, T = 0`
        - `L = 0, R = 1, T = 0`
        - `L = 0, R = 0, T = 1`

addBoolIsLeVar
==============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`\leq` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolIsLeVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)


.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 116-120,122
          :emphasize-lines: 120
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 1`
        - `L = 1, R = 0, T = 0`
        - `L = 0, R = 1, T = 1`
        - `L = 0, R = 0, T = 1`

addBoolIsLtVar
==============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`<` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolIsLtVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 127-131,133
          :emphasize-lines: 131
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 0`
        - `L = 1, R = 0, T = 0`
        - `L = 0, R = 1, T = 1`
        - `L = 0, R = 0, T = 0`

addBoolIsNeqVar
===============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`\neq` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolIsNeqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 138-142,144
          :emphasize-lines: 142
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 0`
        - `L = 1, R = 0, T = 1`
        - `L = 0, R = 1, T = 1`
        - `L = 0, R = 0, T = 0`

addBoolLe
=========

Add a clause to the SAT constraint which states that the boolean variable `LEFT` is less or equal than the boolean variable `RIGHT`.

**API**: ::

    boolean addBoolLe(BoolVar LEFT, BoolVar RIGHT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 149-152,154
          :emphasize-lines: 152
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1`
        - `L = 0, R = 1`
        - `L = 0, R = 0`

addBoolLt
=========

Add a clause to the SAT constraint which states that the boolean variable `LEFT` is less than the boolean variable `RIGHT`.

**API**: ::

    boolean addBoolLt(BoolVar LEFT, BoolVar RIGHT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 159-162,164
          :emphasize-lines: 162
          :linenos:

    The solutions of the problem are :

        - `L = 0, R = 1`

addBoolNot
==========

Add a clause to the SAT constraint which states that the two boolean variables `LEFT` and `RIGHT` are not equal.

**API**: ::

    boolean addBoolNot(BoolVar LEFT, BoolVar RIGHT)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 149-152,154
          :emphasize-lines: 152
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 0`
        - `L = 0, R = 1`

addBoolOrArrayEqualTrue
=======================

Add a clause to the SAT constraint which states that:
`BOOLVARS`:math:`_1` :math:`\lor` `BOOLVARS`:math:`_2` :math:`\lor` ... :math:`\lor` `BOOLVARS`:math:`_n`.

**API**: ::

    boolean addBoolOrArrayEqualTrue(BoolVar[] BOOLVARS)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 75-77,79
          :emphasize-lines: 77
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, BS[3] = 1`
        - `BS[0] = 1, BS[1] = 1, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 0, BS[3] = 0`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 1`

addBoolOrArrayEqVar
===================

Add a clause to the SAT constraint which states that:
(`BOOLVARS`:math:`_1` :math:`\lor` `BOOLVARS`:math:`_2` :math:`\lor` ... :math:`\lor` `BOOLVARS`:math:`_n`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolOrArrayEqVar(BoolVar[] BOOLVARS, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 189-192,194
          :emphasize-lines: 192
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, BS[3] = 1, T = 1`
        - `BS[0] = 1, BS[1] = 1, BS[2] = 0, BS[3] = 1, T = 1`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 0, BS[3] = 0, T = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, BS[3] = 0, T = 0`

addBoolOrEqVar
==============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`\lor` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.


**API**: ::

    boolean addBoolOrEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 199-203,205
          :emphasize-lines: 203
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 1`
        - `L = 1, R = 0, T = 1`
        - `L = 0, R = 1, T = 1`
        - `L = 0, R = 0, T = 0`

addBoolXorEqVar
===============

Add a clause to the SAT constraint which states that:
(`LEFT` :math:`\oplus` `RIGTH`) :math:`\Leftrightarrow` `TARGET`.

**API**: ::

    boolean addBoolXorEqVar(BoolVar LEFT, BoolVar RIGHT, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 210-214,216
          :emphasize-lines: 214
          :linenos:

    The solutions of the problem are :

        - `L = 1, R = 1, T = 0`
        - `L = 1, R = 0, T = 1`
        - `L = 0, R = 1, T = 1`
        - `L = 0, R = 0, T = 0`

addClauses
==========

Adding a clause involved either:

- a logical operator `TREE` and an instance of the solver,
- or, two arrays of boolean variables.

The two methods add a clause to the SAT constraint.

- The first method adds one or more clauses defined by a ``LogOp``.
``LopOp`` aims at simplifying the declaration of clauses by providing some static methods.
However, it should be considered as a last resort, due to the verbosity it comes with.

- The second API add one or more clauses defined by two arrays `POSLITS` and `NEGLITS`.
The first array declares positive boolean variables, those who should be satisfied;
the second array declares negative boolean variables, those who should not be satisfied.


**API**: ::

    boolean addClauses(LogOp TREE, Solver SOLVER)
    boolean addClauses(BoolVar[] POSLITS, BoolVar[] NEGLITS)


.. admonition:: Example 1

            .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
                  :language: java
                  :lines: 221-228,230
                  :emphasize-lines: 226-228
                  :linenos:

            Some solutions of the problem are :

                - `C1 = 1, C2 = 0, R = 1, AR = 1`
                - `C1 = 1, C2 = 0, R = 0, AR = 1`
                - `C1 = 0, C2 = 1, R = 1, AR = 0`
                - `C1 = 0, C2 = 0, R = 0, AR = 1`

.. admonition:: Example 2

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 235-240,242
          :emphasize-lines: 240
          :linenos:

    Some solutions of the problem are :

        - `P1 = 1, P2 = 1, P3 = 1, N = 1`
        - `P1 = 1, P2 = 1, P3 = 1, N = 0`
        - `P1 = 1, P2 = 0, P3 = 1, N = 0`
        - `P1 = 0, P2 = 0, P3 = 1, N = 1`

addFalse
========

Add a unit clause to the SAT constraint which states that the boolean variable `BOOLVAR` must be false (equal to 0).

**API**: ::

    boolean addFalse(BoolVar BOOLVAR)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 247-249,251
          :emphasize-lines: 249
          :linenos:

    The solution of the problem is :

        - `B = 0`

addMaxBoolArrayLessEqVar
========================

Add a clause to the SAT constraint which states that:
maximum(`BOOLVARS`:math:`_i`) :math:`\leq` `TARGET`.


**API**: ::

    boolean addMaxBoolArrayLessEqVar(BoolVar[] BOOLVARS, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 256-259,261
          :emphasize-lines: 259
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, T = 1`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 1, T = 1`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 1, T = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, T = 0`


addSumBoolArrayGreaterEqVar
===========================

Add a clause to the SAT constraint which states that:
sum(`BOOLVARS`:math:`_i`) :math:`\geq` `TARGET`.


**API**: ::

    boolean addSumBoolArrayGreaterEqVar(BoolVar[] BOOLVARS, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 266-269,271
          :emphasize-lines: 269
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1, BS[1] = 1, BS[2] = 1, T = 1`
        - `BS[0] = 1, BS[1] = 0, BS[2] = 1, T = 1`
        - `BS[0] = 0, BS[1] = 1, BS[2] = 1, T = 1`
        - `BS[0] = 0, BS[1] = 0, BS[2] = 0, T = 0`


addSumBoolArrayLessEqKVar
=========================

Add a clause to the SAT constraint which states that:
sum(`BOOLVARS`:math:`_i`) :math:`\leq` `TARGET`.


**API**: ::

    boolean addSumBoolArrayLessEqKVar(BoolVar[] BOOLVARS, BoolVar TARGET)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 276-279,281
          :emphasize-lines: 279
          :linenos:

    Some solutions of the problem are :

        - `BS[0] = 1 BS[1] = 1 BS[2] = 1 T = 1`
        - `BS[0] = 1 BS[1] = 0 BS[2] = 1 T = 1`
        - `BS[0] = 0 BS[1] = 1 BS[2] = 1 T = 1`
        - `BS[0] = 0 BS[1] = 0 BS[2] = 0 T = 1`

addTrue
=======

Add a unit clause to the SAT constraint which states that the boolean variable `BOOLVAR` must be true (equal to 1).

**API**: ::

    boolean addTrue(BoolVar BOOLVAR)

.. admonition:: Example

    .. literalinclude:: /../../choco-solver/src/test/java/doc/SatConstraintExamples.java
          :language: java
          :lines: 286-288,290
          :emphasize-lines: 288
          :linenos:

    The solution of the problem is :

        - `B = 1`

.. _61_searches:

********
Searches
********
