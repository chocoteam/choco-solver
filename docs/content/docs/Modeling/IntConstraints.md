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
{{< tab header="Java" >}}
model.arithm(x, ">=", 3).post();
{{< /tab >}}
{{< tab header="Python" >}}
model.arithm(x, ">=", 3).post()
{{< /tab >}}
{{< /tabpane >}}

Accepted mathematical operators for these constraints are `>=`, `>`, `<=`, `<`, `=` and `!=`. When more than one variable is constrained, one can use the following methods:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
String op = ">="; // among ">=", ">", "<=", "<", "=" and "!="
model.arithm(x, op, y).post();
// with x an IntVar and y either an IntVar or an int

String op2 = "+"; // either "+" or "-"
model.arithm(x, op2, y, op, z).post();
// with x and y IntVar, and z is either an int or an IntVar
{{< /tab >}}
{{< tab header="Python" >}}
op = ">=" # among ">=", ">", "<=", "<", "=" and "!="
model.arithm(x, op, y).post()
# with x an IntVar and y either an IntVar or an int

op2 = "+" # either "+" or "-"
model.arithm(x, op2, y, op, z).post()
# with x and y IntVar, and z is either an int or an IntVar
{{< /tab >}}
{{< /tabpane >}}

## Other mathematical constraints
Choco-solver also supports other mathematical constraints than arithmetical ones, such as *times*, *div*, *modulo*... In this section, we will consider `x`, `y` and `z` to be `IntVar` and `a` and `b` to be `int`.

### Binary and ternary constraints

*times* constraints are posted as following:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.times(x, a, z).post(); // x * a = z
model.times(x, y, a).post(); // x * y = a
model.times(x, y, z).post(); // x * y = z
{{< /tab >}}
{{< tab header="Python" >}}
model.times(x, a, z).post() # x * a = z
model.times(x, y, a).post() # x * y = a
model.times(x, y, z).post() # x * y = z
{{< /tab >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
For the first type of *times* constraint, one might prefer to use a view: `z = model.intScaleView(x, a);`.
{{% /alert %}}

A *div* constraint corresponds to a euclidean division. It is posted as following:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.div(x, y, z).post(); // x / y = z
// it assures that y != 0, and z is rounding towards 0.
{{< /tab >}}
{{< tab header="Python" >}}
model.div(x, y, z).post() # x / y = z
# it assures that y != 0, and z is rounding towards 0.
{{< /tab >}}
{{< /tabpane >}}

*modulo* constraints are of three types:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.mod(x, a, b).post(); // x % a = b
model.mod(x, a, z).post(); // x % a = z
model.mod(x, y, z).post(); // x % y = z
{{< /tab >}}
{{< tab header="Python" >}}
model.mod(x, a, b).post() # x % a = b
model.mod(x, a, z).post() # x % a = z
model.mod(x, y, z).post() # x % y = z
{{< /tab >}}
{{< /tabpane >}}

An *absolute* constraint is posted like this:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.absolute(x, y).post(); // x = |y|
{{< /tab >}}
{{< tab header="Python" >}}
model.absolute(x, y).post() # x = |y|
{{< /tab >}}
{{< /tabpane >}}

A *power* constraint can express `x = base ^ exponent`. Both the exponent and base can be variables:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.pow(base, exponent, x).post(); // x = base ^ exponent
// where base, exponent, and x are IntVar
// or exponent can be a fixed int: model.pow(base, 2, x).post()
{{< /tab >}}
{{< tab header="Python" >}}
model.pow(base, exponent, x).post() # x = base ^ exponent
# where base, exponent, and x are IntVar
# or exponent can be a fixed int: model.pow(base, 2, x).post()
{{< /tab >}}
{{< /tabpane >}}

### Global constraints

#### Minimum and maximum

Let `min` and `max` be `IntVar` and let `vars` be an array of `IntVar`. One can post the following mathematical constraints to assure that `min` is the
minimum of the variables in `vars` and `max` the maximum of them:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.min(min, vars).post();
model.max(max, vars).post();
{{< /tab >}}
{{< tab header="Python" >}}
model.min(min, vars).post()
model.max(max, vars).post()
{{< /tab >}}
{{< /tabpane >}}

#### Sum and scalar

Similarly, one can want to sum some variables. For such an operation, one should use the `sum` constraint:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
String op = ">="; // among ">=", ">", "<=", "<", "=" and "!="
model.sum(vars, op, x).post(); // here, it gives sum(vars) >= x
{{< /tab >}}
{{< tab header="Python" >}}
op = ">=" # among ">=", ">", "<=", "<", "=" and "!="
model.sum(vars, op, x).post() # here, it gives sum(vars) >= x
{{< /tab >}}
{{< /tabpane >}}

And, finally, for weighted sums, one should use the `scalar` constraint:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
String op = ">="; // among ">=", ">", "<=", "<", "=" and "!="
model.scalar(vars, coefs, op, x).post(); // coefs being an array of int
// here, it gives sum(vars[i] * coefs[i]) >= x
{{< /tab >}}
{{< tab header="Python" >}}
op = ">=" # among ">=", ">", "<=", "<", "=" and "!="
model.scalar(vars, coefs, op, x).post() # coefs being an array of int
# here, it gives sum(vars[i] * coefs[i]) >= x
{{< /tab >}}
{{< /tabpane >}}

## Remarkable global constraints

There exists a wide range of global constraints. In this section, we only list some of them. All of them are accessible through the `model` API.
One can consult the complete list of constraints in the [JavaDoc](https://javadoc.io/doc/org.choco-solver/choco-solver/latest/org.chocosolver/module-summary.html).

### AllDifferent

The `alldifferent` constraint is probably the most famous one. It ensures that all variables in its scope take a distinct value in any solution.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
IntVar[] vars = model.intVarArray("X", 4, 1, 5);
model.allDifferent(vars).post();
{{< /tab >}}
{{< tab header="Python" >}}
vars = model.intvars(4, 1, 5,"X")
model.all_different(vars).post()
{{< /tab >}}
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
{{< tab header="Java" >}}
IntVar occ0 = model.intVar("occ0", 0, 5);
IntVar[] vars = model.intVarArray("X", 7, 0, 5);
model.count(0, vars, occ0).post();
{{< /tab >}}
{{< tab header="Python" >}}
occ0 = model.intvar(0, 5, "occ0")
vars = model.intvars(7, 0, 5, "X")
model.count(0, vars, occ0).post();
{{< /tab >}}
{{< /tabpane >}}

A solution to this constraint is `vars = [0, 1, 0, 1, 5, 5, 5]`, `occ0 = 2`.

### Global Cardinality

This constraint is very helpful to count the number of occurences of some values in an array of variables.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
IntVar[] vars = model.intVarArray("X", 7, 0, 5);
int[] values = new int[]{1, 3, 5};
IntVar[] occs = model.intVarArray("O", 2, 0, 3);
model.globalCardinality(vars, values, occs, false).post();
{{< /tab >}}
{{< tab header="Python" >}}
vars = model.intvars(7, 0, 5, "X")
values = [1,3,5]
occs = model.intvars(2, 0, 3, "O")
model.global_cardinality(vars, values, occs, False).post();
{{< /tab >}}
{{< /tabpane >}}

A solution to this constraint is `vars = [0, 1, 0, 1, 5, 5, 5]`, `occs = [2, 0, 3]`.

If the last parameter is set to `true`, the variables of `vars` should take their values in `values`.
With that parameter to `true`, the previous solution is not correct anymore.
A solution is : `vars = [3, 1, 3, 1, 5, 5, 5]`, `occs = [2, 2, 3]`.

### Element

The `element` constraint is very convenient. It permits to dynamically assign a variable to a value based on an array and an index variable.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
IntVar idx = model.intVar("I", 0, 5, false);
IntVar rst = model.intVar("R", 0, 10, false);
model.element(rst, new int[]{0, 2, 4, 6, 7}, idx).post();
{{< /tab >}}
{{< tab header="Python" >}}
idx = model.intvar(0, 5, "I")
rst = model.intvar(0, 10, "R")
model.element(rst, [0, 2, 4, 6, 7], idx).post()
{{< /tab >}}
{{< /tabpane >}}

An assignment of `idx` and `rst` that satisfies this constraint is: `idx = 3` and `rst = 6`.

You can also use an array of variables as the second argument.


## Scheduling constraints

Scheduling problems can be modelled using Task variables and the cumulative constraint.  
A task represents the entity that should be scheduled, where it is unknown when the task starts and optionally how long it lasts.  
The cumulative constraint limits the number of concurrent tasks.  

### `Task` variables

A task needs a start `IntVar` and a duration `int`.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Task task = new Task(start, duration);
{{< /tab >}}
{{< tab header="Python" >}}
task = model.task(start, duration)
{{< /tab >}}
{{< /tabpane >}}

Optionally an end `IntVar` can be supplied. Task will ensure that *start + duration = end*, end being an offset view of *start + duration*.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Task task = new Task(start, duration, end);
{{< /tab >}}
{{< tab header="Python" >}}
task = model.task(start, duration, end)
{{< /tab >}}
{{< /tabpane >}}

A task can have an unknown duration. In this case create the task with 3 `IntVar`: start, varDuration and end. Task will ensure that *start + varDuration = end*, end being an offset view of *start + varDuration*.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Task task = new Task(start, varDuration, end);
{{< /tab >}}
{{< tab header="Python" >}}
task = model.task(start, varDuration, end)
{{< /tab >}}
{{< /tabpane >}}

Finally, a task can be created based on the `Model` and 5 `int`: earliestStart, latestStart, duration, earliestEnd, latestEnd.  
A start `IntVar` will be created with a domain of *[earliestStart, latestStart]*.  
An end `IntVar` will be created with a domain of *[earliestEnd, latestEnd]*.  
Task will ensure that *start + duration = end*, end being an offset view of *start + duration*.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Task task = new Task(model, earliestStart, latestStart, duration, earliestEnd, latestEnd);
{{< /tab >}}
{{< /tabpane >}}

### `Cumulative` constraint

The cumulative constraint ensures that at any point in time, the cumulated heights of a set of overlapping `Tasks` does not exceed a given capacity.  
Let tasks be an array of `Task`, heights an array of `IntVar` and capacity an `IntVar`, where *heights[i]* is the height for *tasks[i]*.  
Make sure $|tasks| = |heights|$  
Task duration and height should be $\geq$ 0. Tasks with duration or height equal to 0 will be discarded.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.cumulative(tasks, heights, capacity).post();
{{< /tab >}}
{{< tab header="Python" >}}
model.cumulative(tasks, heights, capacity).post()
{{< /tab >}}
{{< /tabpane >}}

If only one task can happen concurrently, set the heights fixed equal to the capacity, either by setting fixed values or posting constraints between the variables.  
Other combinations of concurrently (or not) planned tasks can be modelled by setting different values for the heights and the capacity, or by defining different constraints between these variables.   

Example: 4 tasks with a set height that cannot happen at the same time by setting a fixed capacity:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Task[] tasks = new Task[4];
IntVar[] heights = new IntVar[4];
for(int i = 0, i < starts.length; i++){
  tasks[i] = new Task(starts[i], durations[i]);
  heights[i] = model.intVar(1);
}
IntVar capacity = model.intVar(1);

model.cumulative(tasks, heights, capacity).post();
{{< /tab >}}
{{< tab header="Python" >}}
tasks = [ model.task(starts[i], durations[i]) for i in range(4) ]
heights = [ model.intvar(1) for _ in range(4) ]
capacity = model.intvar(1)

model.cumulative(tasks, heights, capacity).post()
{{< /tab >}}
{{< /tabpane >}}

Solving this will result in all 4 tasks happening consecutively so:
$start[i] + duration[i] \lt start[j]$

The cumulative constraint does not enforce a specific order of tasks. Define additional constraints between the variables for this if needed.


## Table constraints

Table constraints are really useful and make it possible to encode any relationships.
Table constraints expect an array of variables as input and a `Tuples` object.
The latter stores a list of allowed (resp. forbidden) tuples, each of them expresses an allowed (resp. forbidden) combination  for the variables.

Consider an ordered set of variables $X=\\{X_i \mid i\in I\\}$ and an allowed combination $t\in T$, which defined an ordered set of integer values. The ith value in $t$ is denoted $t_i$.

A Table constraint ensures that :
$$Table(X,T)\equiv \bigvee_{t \in T}\bigwedge_{i \in I}(X_i = t_i)$$

*When dealing with forbidden combinations, the `$=$` operator below is replaced by a `$\neq$` operator.*

Table constraints are also known as constraints *in extension* since all possible (or impossible) combinations are needed as input.

Table constraints usually provide domain consistency filtering algorithm, with diverse spatial and temporal complexities which depend on the number of variables involved and the number of tuples.

{{% alert title="Hybrid Tuples" color="info" %}}
The `HybridTuples` format provides a more efficient representation for large tables, especially when there are repeated values or patterns. Use `model.table(vars, new HybridTuples(tuples))` for automatic optimization.
{{% /alert %}}

### Allowed combinations

Let's take the example of four variables $X_i = [\\![0,3]\\!], i \in [1,4]$, that **must** all be equal. This relationship can be expressed with a Table constraint as follow:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Tuples allEqual = new Tuples(true); // true stands for 'allowed' combinations
allEqual.add({0,0,0,0});
allEqual.add({1,1,1,1});
allEqual.add({2,2,2,2});
allEqual.add({3,3,3,3});
model.table(X, allEqual, "CT+").post();
{{< /tab >}}
{{< tab header="Python" >}}
allEqual = []
allEqual.append([0,0,0,0])
allEqual.append([1,1,1,1])
allEqual.append([2,2,2,2])
allEqual.append([3,3,3,3])
model.table(X, allEqual, True, "CT+").post() # True stands for 'allowed' combinations
{{< /tab >}}
{{< /tabpane >}}

Only defined combinations are solutions.

The parameter `"CT+"` is optional and defines the filtering algorithm to use. `"CT+"` is a really good default choice.

### Forbidden combinations

Let's take the previous example but in that case, all variables **must not** be all be equal. This relationship can be expressed with a Table constraint as follow, with a little difference in the `Tuples` declaration:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
Tuples allEqual = new Tuples(false); // false stands for 'forbidden' combinations
allEqual.add({0,0,0,0});
allEqual.add({1,1,1,1});
allEqual.add({2,2,2,2});
allEqual.add({3,3,3,3});
model.table(X, allEqual, "CT+").post();
{{< /tab >}}
{{< tab header="Python" >}}
allEqual = []
allEqual.append([0,0,0,0])
allEqual.append([1,1,1,1])
allEqual.append([2,2,2,2])
allEqual.append([3,3,3,3])
model.table(X, allEqual, False, "CT+").post() # False stands for 'forbidden' combinations
{{< /tab >}}
{{< /tabpane >}}

All undefined combinations are solution, for example `{0,0,0,1}` or `{0,1,2,3}`.


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
It's a use case of universal value.
{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
int STAR = -1;
Tuples rel = new Tuples(true);
rel.setUniversalValue(STAR);
rel.add({0,0,STAR});
rel.add({3,2,1});
model.table(X, rel).post();
{{< /tab >}}
{{< /tabpane >}}
`STAR` represents the universal value, here $-1$.
The value must be taken outside variables domain.

## SAT constraints

A SAT solver is embedded in Choco-solver. It is not  designed to be accessed directly.
The SAT solver is internally managed as a constraint (and a propagator), that’s why it is referred to as SAT constraint in the following.

Clauses can be added with the `Model` thanks to methods whose name begins with `addClause*`.
There exists two type of clauses declaration: pre-defined ones (such as `addClausesBoolLt` or `addClausesAtMostNMinusOne`) or more free ones by specifying a `LogOp` that represents a clause expression:

{{< tabpane langEqualsHeader=true >}}
{{< tab header="Java" >}}
model.addClauses(LogOp.and(LogOp.nand(LogOp.nor(a, b), LogOp.or(c, d)), e));
// with static import of LogOp
model.addClauses(and(nand(nor(a, b), or(c, d)), e));
{{< /tab >}}
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

`CostAutomaton` is an extension of `FiniteAutomaton` where costs can be declared for each transition.
