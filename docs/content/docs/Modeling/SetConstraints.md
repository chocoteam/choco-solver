---
title: "Constraints over set variables"
date: 2020-01-07T16:07:30+01:00
weight: 25
math: "true"
description: >
  Overview of constraints on set variables.
---

Set variables represent subsets of a reference set. Constraints over set variables enable us to model relationships between sets and relationships between sets and integer variables.

For example, suppose we have a reference set $\\{0, 1, 2, 3, 4\\}$ and we want to model membership, union, intersection, and cardinality constraints on subsets. This section presents the main set constraints available in Choco-solver.

## Basic Set Constraints

### Membership constraints

The `member` and `notMember` constraints check whether a value belongs to a set variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
model.member(2, s).post();      // 2 must be in s
model.notMember(3, s).post();   // 3 must not be in s
{{< /tab >}}
{{< tab header="Python" >}}
s = model.setvar([], [0, 1, 2, 3, 4], "s")
model.member(2, s).post()       # 2 must be in s
model.not_member(3, s).post()   # 3 must not be in s
{{< /tab >}}
{{< /tabpane >}}

### Subset constraints

The `subsetEq` constraint ensures that one set is a subset of another:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2, 3});
SetVar s2 = model.setVar("s2", new int[]{0, 1}, new int[]{0, 1, 2, 3, 4});
model.subsetEq(s1, s2).post();  // s1 ⊆ s2
{{< /tab >}}
{{< tab header="Python" >}}
s1 = model.setvar([], [0, 1, 2, 3], "s1")
s2 = model.setvar([0, 1], [0, 1, 2, 3, 4], "s2")
model.subset_eq(s1, s2).post()  # s1 ⊆ s2
{{< /tab >}}
{{< /tabpane >}}

### Set comparison constraints

The `setLe` and `setLt` constraints provide a lexicographic ordering on sets:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 2});
model.setLe(s1, s2).post();     // s1 ≤lex s2
{{< /tab >}}
{{< /tabpane >}}

## Cardinality and Size Constraints

### Cardinality

The `nbEmpty` constraint counts how many set variables are empty:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] sets = model.setVarArray("S", 5, new int[]{}, new int[]{0, 1, 2, 3, 4});
IntVar nbEmpty = model.intVar("nbEmpty", 0, 5);
model.nbEmpty(sets, nbEmpty).post();  // how many sets are empty?
{{< /tab >}}
{{< /tabpane >}}

The `notEmpty` constraint ensures that a set is non-empty:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3});
model.notEmpty(s).post();  // s must contain at least one element
{{< /tab >}}
{{< /tabpane >}}

### Size and sum

The `offSet` constraint relates a set's size to an integer variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
IntVar size = model.intVar("size", 0, 5);
model.offSet(s, 0, size).post();  // size = |s|
{{< /tab >}}
{{< /tabpane >}}

The `sum` and `sumElements` constraints relate the sum of elements in a set to an integer variable:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3, 4});
IntVar sumVar = model.intVar("sum", 0, 10);
model.sum(s, sumVar).post();  // sumVar = sum of elements in s

// With explicit weights
int[] weights = new int[]{1, 2, 3, 4, 5};
model.sumElements(s, weights, sumVar).post();
{{< /tab >}}
{{< /tabpane >}}

## Set Operations

### Union and intersection

The `union` constraint computes the union of a set of sets:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 3});
SetVar union = model.setVar("union", new int[]{}, new int[]{0, 1, 2, 3});
model.union(new SetVar[]{s1, s2}, union).post();  // union = s1 ∪ s2
{{< /tab >}}
{{< /tabpane >}}

The `intersection` constraint computes the intersection of a set of sets:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
SetVar s2 = model.setVar("s2", new int[]{0, 1}, new int[]{0, 1, 2, 3});
SetVar inter = model.setVar("inter", new int[]{}, new int[]{0, 1, 2, 3});
model.intersection(new SetVar[]{s1, s2}, inter).post();  // inter = s1 ∩ s2
{{< /tab >}}
{{< /tabpane >}}

### Min and max

The `min` and `max` constraints relate the minimum and maximum elements in a set:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{1, 2, 3, 4, 5});
IntVar minVar = model.intVar("min", 1, 5);
IntVar maxVar = model.intVar("max", 1, 5);
model.min(minVar, s).post();  // minVar = minimum element in s
model.max(maxVar, s).post();  // maxVar = maximum element in s
{{< /tab >}}
{{< /tabpane >}}

## Global Set Constraints

### Disjoint constraints

The `disjoint` constraint ensures two sets are disjoint:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
SetVar s2 = model.setVar("s2", new int[]{3, 4}, new int[]{3, 4, 5});
model.disjoint(s1, s2).post();  // s1 ∩ s2 = ∅
{{< /tab >}}
{{< /tabpane >}}

The `allDisjoint` constraint ensures all sets in an array are pairwise disjoint:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2, 3, 4, 5});
model.allDisjoint(sets).post();  // all sets are pairwise disjoint
{{< /tab >}}
{{< /tabpane >}}

### Partition constraint

The `partition` constraint ensures that a set of sets partitions a reference set (they are disjoint and their union is the reference set):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] partition = model.setVarArray("Part", 3, new int[]{}, new int[]{0, 1, 2, 3, 4});
SetVar reference = model.setVar("ref", new int[]{0, 1, 2, 3, 4}, new int[]{0, 1, 2, 3, 4});
model.partition(partition, reference).post();  // partition partitions reference
{{< /tab >}}
{{< /tabpane >}}

### Equality constraints

The `allDifferent` and `allEqual` constraints apply to sets:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] sets = model.setVarArray("S", 3, new int[]{}, new int[]{0, 1, 2});
model.allEqual(sets).post();  // all sets must be equal
{{< /tab >}}
{{< /tabpane >}}

## Channeling Constraints

### Set-Boolean channeling

The `setBoolsChanneling` constraint links a set variable to an array of boolean variables, where boolean variable `i` is true iff `i` is in the set:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s = model.setVar("s", new int[]{}, new int[]{0, 1, 2, 3});
BoolVar[] bools = model.boolVarArray("b", 4);
model.setBoolsChanneling(bools, s, 0).post();  // bools[i] ⟺ i ∈ s
{{< /tab >}}
{{< /tabpane >}}

### Set-Integer channeling

The `setsIntsChanneling` constraint links set variables to integer variables. For example, if `x[i]` is in set `s[j]`, then `x[i] = j` (with some offset):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
IntVar[] x = model.intVarArray("x", 5, 0, 2);
SetVar[] s = model.setVarArray("s", 3, new int[]{}, new int[]{0, 1, 2, 3, 4});
model.setsIntsChanneling(s, x, 0).post();  // x[i] = j if i ∈ s[j]
{{< /tab >}}
{{< /tabpane >}}

### Sorted set-integer channeling

The `sortedSetIntsChanneling` constraint additionally ensures that the integers in each set form a sorted sequence:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
IntVar[] x = model.intVarArray("x", 5, 0, 2);
SetVar[] s = model.setVarArray("s", 3, new int[]{}, new int[]{0, 1, 2, 3, 4});
model.sortedSetIntsChanneling(s, x, 0).post();
{{< /tab >}}
{{< /tabpane >}}

### Symmetric constraint

The `symmetric` constraint enforces symmetry between two set variables (useful for undirected graphs):

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar s1 = model.setVar("s1", new int[]{}, new int[]{0, 1, 2});
SetVar s2 = model.setVar("s2", new int[]{}, new int[]{0, 1, 2});
model.symmetric(s1, s2).post();  // symmetric relationship between s1 and s2
{{< /tab >}}
{{< /tabpane >}}

### Inverse set constraint

The `inverseSet` constraint enforces an inverse relationship between two arrays of sets:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] sets1 = model.setVarArray("S1", 3, new int[]{}, new int[]{0, 1, 2});
SetVar[] sets2 = model.setVarArray("S2", 3, new int[]{}, new int[]{0, 1, 2});
model.inverseSet(sets1, sets2, 0, 0).post();
{{< /tab >}}
{{< /tabpane >}}

## Element Constraint

The `element` constraint (for sets) retrieves a set from an array of sets at an index:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
SetVar[] setArray = model.setVarArray("sets", 4, new int[]{}, new int[]{0, 1, 2, 3});
SetVar result = model.setVar("result", new int[]{}, new int[]{0, 1, 2, 3});
IntVar index = model.intVar("index", 0, 3);
model.element(result, setArray, index, 0).post();
{{< /tab >}}
{{< /tabpane >}}
