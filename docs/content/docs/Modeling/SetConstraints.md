---
title: "Constraints over set variables"
date: 2020-01-07T16:07:30+01:00
weight: 25
math: "true"
description: >
  Overview of constraints on set variables.
---

Set variables represent subsets of a reference set. Constraints over set variables enable us to model relationships between sets and relationships between sets and integer variables.
This section presents the main set constraints available in Choco-solver.
In Python (pychoco), most of them are prefixed with `set_`.

## Basic set constraints

### Membership

`member` and `notMember` state that a constant or the value of an integer variable belongs, or not, to a set
(`set_member_int` and `set_not_member_int` in Python):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "member" >}}
{{< /tabpane >}}

`member(sets, set)` states that a set variable is one of the sets of an array.

### Subset

`subsetEq` ensures that each set is a subset of the next one:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "subsetEq" >}}
{{< /tabpane >}}

### Lexicographic ordering

`setLe` and `setLt` order two sets lexicographically, by comparing the sorted lists of their elements:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "setLe" >}}
{{< /tabpane >}}

## Cardinality

The cardinality of a set variable is an integer variable, given by `getCard()`:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "cardinality" >}}
{{< /tabpane >}}

`nbEmpty` counts how many sets of an array are empty, and `notEmpty` prevents a set from being empty:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "nbEmpty" >}}
{{< /tabpane >}}

## Sum, minimum and maximum

`sum` relates the sum of the elements of a set to an integer variable,
and `sumElements` the sum of weights indexed by its elements:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "sum" >}}
{{< /tabpane >}}

`min` and `max` relate the smallest and the largest element of a set to an integer variable.
The last parameter states whether the set can be empty (`false`) or not (`true`).
Variants take weights indexed by the elements of the set, as `sumElements`.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "minMax" >}}
{{< /tabpane >}}

## Set operations

### Union and intersection

`union` and `intersection` relate a set to the union or the intersection of an array of sets:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "union" >}}
{{< /tabpane >}}

### Offset

`offSet` shifts the elements of a set: $x \in s_1 \Leftrightarrow x + o \in s_2$:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "offSet" >}}
{{< /tabpane >}}

## Global set constraints

### Disjointness

`disjoint` ensures that two sets have no common element, and `allDisjoint` that the sets of an array are pairwise disjoint:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "disjoint" >}}
{{< /tabpane >}}

### Partition

`partition` ensures that an array of sets partitions a reference set: they are pairwise disjoint and their union is the reference set:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "partition" >}}
{{< /tabpane >}}

### Different and equal sets

`allDifferent` ensures that the sets of an array are pairwise different (at most one of them can be empty),
and `allEqual` that they are all equal:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "allEqual" >}}
{{< /tabpane >}}

## Channeling constraints

### Sets and booleans

`setBoolsChanneling` links a set variable to an array of boolean variables: `bools[i]` is true if and only if `i` is in the set
(a variant takes an offset):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "setBoolsChanneling" >}}
{{< /tabpane >}}

### Sets and integers

`setsIntsChanneling` links an array of set variables to an array of integer variables:
`i` is in `s[j]` if and only if `x[i] = j` (a variant takes offsets):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "setsIntsChanneling" >}}
{{< /tabpane >}}

`sortedSetIntsChanneling` links a set to the sorted list of its elements:
`x[i]` is the i-th smallest element of the set, or a given value when the set has fewer elements.
It is not yet available in pychoco.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "sortedSetIntsChanneling" >}}
{{< /tabpane >}}

### Symmetric and inverse sets

`symmetric` ensures that `i` is in `sets[j]` if and only if `j` is in `sets[i]`:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "symmetric" >}}
{{< /tabpane >}}

`inverseSet` relates two arrays of sets: `i` is in `sets[j]` if and only if `j` is in `inverses[i]`:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "inverseSet" >}}
{{< /tabpane >}}

## Element

`element` retrieves the set at a variable index in an array of sets:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/SetConstraints" "element" >}}
{{< /tabpane >}}
