---
title: "Constraints over integer variables"
date: 2020-01-07T16:07:15+01:00
weight: 23
math: "true"
description: >
  Overview of constraints based on boolean and integer variables.
---

## Arithmetical constraints
These constraints are used to express arithmetical relationships between two or three variables, or between one or two variables and an integer constant.
For instance, a threshold constraint can be posted as following:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "arithm" >}}
{{< /tabpane >}}

Accepted mathematical operators for these constraints are `>=`, `>`, `<=`, `<`, `=` and `!=`. When more than one variable is constrained, one can use the following methods:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "arithm2" >}}
{{< /tabpane >}}

## Other mathematical constraints
Choco-solver also supports other mathematical constraints than arithmetical ones, such as *times*, *div*, *modulo*... In this section, we will consider `x`, `y` and `z` to be `IntVar` and `a` and `b` to be `int`.

### Binary and ternary constraints

*times* constraints are posted as following:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "times" >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
For the first type of *times* constraint, one might prefer to use a view: `IntVar z = model.mul(x, a);`.
{{% /alert %}}

A *div* constraint corresponds to a euclidean division. It is posted as following:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "div" >}}
{{< /tabpane >}}

*modulo* constraints are of three types:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "mod" >}}
{{< /tabpane >}}

An *absolute* constraint is posted like this:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "absolute" >}}
{{< /tabpane >}}

A *power* constraint can express `x = base ^ exponent`. Both the exponent and base can be variables (in Python, the exponent is a constant):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "pow" >}}
{{< /tabpane >}}

### Global constraints

#### Minimum and maximum

Let `min` and `max` be `IntVar` and let `vars` be an array of `IntVar`. One can post the following mathematical constraints to assure that `min` is the
minimum of the variables in `vars` and `max` the maximum of them:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "minMax" >}}
{{< /tabpane >}}

#### Sum and scalar

Similarly, one can want to sum some variables. For such an operation, one should use the `sum` constraint:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "sum" >}}
{{< /tabpane >}}

And, finally, for weighted sums, one should use the `scalar` constraint:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "scalar" >}}
{{< /tabpane >}}

## Remarkable global constraints

There exists a wide range of global constraints. In this section, we only list some of them. All of them are accessible through the `model` API.
One can consult the complete list of constraints in the [JavaDoc](https://javadoc.io/doc/org.choco-solver/choco-solver/latest/org.chocosolver/module-summary.html).

### AllDifferent

The `alldifferent` constraint is probably the most famous one. It ensures that all variables in its scope take a distinct value in any solution.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "allDifferent" >}}
{{< /tabpane >}}

An instantiation that satisfies this constraint is
`[1,5,2,3]`.

**Variants:**

- `allDifferentExcept0(IntVar[] vars)` ensures all variables are distinct except those assigned to 0.
- `allDifferentExceptValues(IntVar[] vars, int... values)` ensures all variables are distinct except those assigned to one of the specified values.
- `allDiffPrec(IntVar[] vars, int[][] predecessors, int[][] successors, String filter)` combines AllDifferent with precedence constraints (useful for scheduling problems).

### Count

This constraint is very helpful to count the number of occurences of a value in an array of variables.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "count" >}}
{{< /tabpane >}}

A solution to this constraint is `vars = [0, 1, 0, 1, 5, 5, 5]`, `occ0 = 2`.

### Global Cardinality

This constraint is very helpful to count the number of occurences of some values in an array of variables.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "globalCardinality" >}}
{{< /tabpane >}}

A solution to this constraint is `vars = [0, 1, 0, 1, 5, 5, 5]`, `occs = [2, 0, 3]`.

If the last parameter is set to `true`, the variables of `vars` should take their values in `values`.
With that parameter to `true`, the previous solution is not correct anymore.
A solution is : `vars = [3, 1, 3, 1, 5, 5, 5]`, `occs = [2, 2, 3]`.

### Element

The `element` constraint is very convenient. It permits to dynamically assign a variable to a value based on an array and an index variable.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "element" >}}
{{< /tabpane >}}

An assignment of `idx` and `rst` that satisfies this constraint is: `idx = 3` and `rst = 6`.

You can also use an array of variables as the second argument.


## Scheduling constraints

Scheduling problems can be modelled using Task variables and the cumulative constraint.  
A task represents the entity that should be scheduled, where it is unknown when the task starts and optionally how long it lasts.  
The cumulative constraint limits the number of concurrent tasks.  

### `Task` variables

A task needs a start `IntVar` and a duration `int`. The end is then a view of *start + duration*, so the relation always holds.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "task" >}}
{{< /tabpane >}}

Optionally an end `IntVar` can be supplied. A `Task` is also a propagator: *start + duration = end* only holds once the task is posted, or given to a scheduling constraint such as `cumulative`.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "taskEnd" >}}
{{< /tabpane >}}

A task can have an unknown duration. In this case create the task with 3 `IntVar`: start, varDuration and end, and post it to ensure *start + varDuration = end*.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "taskVarDuration" >}}
{{< /tabpane >}}

Finally, a task can be created from 5 `int`: earliestStart, latestStart, duration, earliestEnd, latestEnd.  
A start `IntVar` is created with a domain compatible with *[earliestStart, latestStart]* and *[earliestEnd - duration, latestEnd - duration]*,  
and the end is a view of *start + duration*.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "taskBounds" >}}
{{< /tabpane >}}

### `Cumulative` constraint

The cumulative constraint ensures that at any point in time, the cumulated heights of a set of overlapping `Tasks` does not exceed a given capacity.  
Let tasks be an array of `Task`, heights an array of `IntVar` and capacity an `IntVar`, where *heights[i]* is the height for *tasks[i]*.  
Make sure $|tasks| = |heights|$  
Task duration and height should be $\geq$ 0. Tasks with duration or height equal to 0 will be discarded.



If only one task can happen concurrently, set the heights fixed equal to the capacity, either by setting fixed values or posting constraints between the variables.  
Other combinations of concurrently (or not) planned tasks can be modelled by setting different values for the heights and the capacity, or by defining different constraints between these variables.   

Example: 4 tasks with a set height that cannot happen at the same time by setting a fixed capacity:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "cumulative" >}}
{{< /tabpane >}}

Solving this will result in the 4 tasks not overlapping: for any two tasks $i$ and $j$, either
$start[i] + duration[i] \leq start[j]$ or $start[j] + duration[j] \leq start[i]$.

The cumulative constraint does not enforce a specific order of tasks. Define additional constraints between the variables for this if needed.


## Table constraints

Table constraints are really useful and make it possible to encode any relationships.
Table constraints expect an array of variables as input and a `Tuples` object.
The latter stores a list of allowed (resp. forbidden) tuples, each of them expresses an allowed (resp. forbidden) combination  for the variables.

Consider an ordered set of variables $X=\\{X_i \mid i\in I\\}$ and an allowed combination $t\in T$, which defined an ordered set of integer values. The ith value in $t$ is denoted $t_i$.

A Table constraint ensures that :
$$Table(X,T)\equiv \bigvee_{t \in T}\bigwedge_{i \in I}(X_i = t_i)$$

*When dealing with forbidden combinations, the $=$ operator above is replaced by a $\neq$ operator.*

Table constraints are also known as constraints *in extension* since all possible (or impossible) combinations are needed as input.

Table constraints usually provide domain consistency filtering algorithm, with diverse spatial and temporal complexities which depend on the number of variables involved and the number of tuples.

### Allowed combinations

Let's take the example of four variables $X_i = [\\![0,3]\\!], i \in [1,4]$, that **must** all be equal. This relationship can be expressed with a Table constraint as follow:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "tableAllowed" >}}
{{< /tabpane >}}

Only defined combinations are solutions.

The parameter `"CT+"` is optional and defines the filtering algorithm to use. `"CT+"` is a really good default choice for allowed combinations.

### Forbidden combinations

Let's take the previous example but in that case, all variables **must not** be all equal. This relationship can be expressed with a Table constraint as follow, with a little difference in the `Tuples` declaration.
Note that `"CT+"` only handles allowed combinations: use `"CT"` (or no algorithm) for forbidden ones.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "tableForbidden" >}}
{{< /tabpane >}}

All undefined combinations are solutions, for example `{0,0,0,1}` or `{0,1,2,3}`.


### Universal value
Under certain conditions, the number of tuples can be reduced by introducing a *universal value*.
Consider $X_i = [\\![0,99]\\!], i \in [1,3]$, and the following relationship:

1. *if $X_1 = 0$ then $X_2 = 0$ and $X_3$ can take any value;*
2. *else if $X_1 = 3$ then $X_2 = 2$ and $X_3 = 1$.*

The allowed tuples are:
```
{0, 0, 0}
{0, 0, 1}
...
{0, 0, 98}
{0, 0, 99}
{3, 2, 1}
```
We can see that this relation requires 100 combinations to be expressed and 99 of them are needed to capture *$X_3$ can take any value*.
It's a use case of universal value, given to the `Tuples` constructor:
{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "universal" >}}
{{< /tabpane >}}
`STAR` represents the universal value, here $-1$.
The value must be taken outside variables domain.

### Hybrid tuples

`HybridTuples` express tuples in a compact way, with expressions such as `any()`, `eq(2)` or `col(0)` (the value of the first variable) instead of plain values:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "hybrid" >}}
{{< /tabpane >}}

## SAT constraints

A SAT solver is embedded in Choco-solver. It is not  designed to be accessed directly.
The SAT solver is internally managed as a constraint (and a propagator), that’s why it is referred to as SAT constraint in the following.

Clauses can be added with the `Model` thanks to methods whose name begins with `addClause*`.
There exists two type of clauses declaration: pre-defined ones (such as `addClausesBoolLt` or `addClausesAtMostNMinusOne`) or more free ones by specifying a `LogOp` that represents a clause expression:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntConstraints" "clauses" >}}
{{< /tabpane >}}

## Automaton-based Constraints

`regular`, `costRegular` and `multiCostRegular` rely on an automaton, declared either implicitly or explicitly.
There are two kinds of `IAutomaton` :
- `FiniteAutomaton`, needed for `regular`,
- `CostAutomaton`, required for `costRegular` and `multiCostRegular`.

`FiniteAutomaton` embeds an `Automaton` object provided by the `dk.brics.automaton` library.
Such an automaton accepts fixed-size words made of multiple `char`, but the regular constraints rely on `IntVar`,
so a mapping between `char` (needed by the underlying library) and `int` (declared in `IntVar`) has been made.
The mapping enables declaring regular expressions where a symbol is not only a digit between 0 and 9 but any **positive** number.
Then to distinct, in the word 101, the symbols 0, 1, 10 and 101, two additional `char` are allowed in a regexp: < and > which delimits numbers.

In summary, a valid regexp for the automaton-based constraints is a combination of **digits** and Java Regexp special characters.
Letters are not symbols: a regexp containing a letter is refused (write `<97>`, not `a`).
A symbol is in [0,65535]; values of the variables outside this range are simply never accepted.
`.` (any symbol) and negated classes such as `[^12]` are very slow on the whole range:
restrict it with `new FiniteAutomaton(regexp, min, max)`.
Posted on an empty array of variables, `regular` is satisfied if and only if the automaton accepts the empty word.

`CostAutomaton` is an extension of `FiniteAutomaton` where costs can be declared for each transition.
They are built with `CostAutomaton.makeSingleResource(automaton, costs, inf, sup)` for `costRegular`,
and `CostAutomaton.makeMultiResources(...)` for `multiCostRegular`.
In the cost arrays, a value is used as an index: `costs[layer][value]` is the cost of assigning `value`
to the variable at index `layer`, and `inf` and `sup` bound the cost of a word.

```java
IntVar[] vars = model.intVarArray("x", 3, 0, 2);
IntVar cost = model.intVar("cost", 0, 10);
int[][] costs = new int[3][];
Arrays.fill(costs, new int[]{0, 1, 5}); // the values 0, 1 and 2 cost 0, 1 and 5
ICostAutomaton auto = CostAutomaton.makeSingleResource(
        new FiniteAutomaton("(0|1|2)*"), costs, cost.getLB(), cost.getUB());
model.costRegular(vars, cost, auto).post();
```
