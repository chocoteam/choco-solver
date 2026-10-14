---
title: "Declaring variables"
date: 2020-01-07T16:06:55+01:00
weight: 21
description: >
  How to declare variables?
math: "true"  
---


A variable is an *unknown*, mathematically speaking.
The goal of a resolution is to *assign* a *value* to each variable.
The *domain* of a variable –(super)set of values it may take– must be defined in the model.

Choco-solver includes several types of variables: `IntVar`, `BoolVar`, `SetVar`, `GraphVar` and `RealVar`, plus `Task` for scheduling.
Variables are created using the `Model` object.
When creating a variable, the user can specify a name to help reading the output.

## Integer variables

An integer variable is an unknown whose value should be an integer. Therefore, the domain of an integer variable is a set of integers (representing possible values).
To create an integer variable, the `Model` should be used:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "intVar" >}}
{{< /tabpane >}}

It is then possible to build directly arrays and matrices of variables having the same initial domain with:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "intVarArray" >}}
{{< /tabpane >}}

There exists different ways to encode the domain of an integer variable: bounded domain or enumerated domain.

### Bounded domain

When the domain of an integer variable is said to be *bounded*, it is represented through
an interval of the form $[\\![a,b]\\!]$ where $a$ and $b$ are integers such that $a \leq b$.
This representation is pretty light in memory (it requires only two integers) but it cannot represent *holes* in the domain.
For instance, if we have a variable whose domain is $[\\![0,10]\\!]$ and a constraint enables to detect that
values 2, 3, 7 and 8 are infeasible, then this learning will be lost as it cannot be encoded in the domain (which remains the same).
However, whenever the values 9 and 10 are removed from the variable's domain, the upper bound of the variable will be set to 6 by such a constraint.

To specify you want to use bounded domains, set the `boundedDomain` argument to `true` when creating variables:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "bounded" >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
When using bounded domains, branching decisions must either be domain splits or bound assignments/removals.
Indeed, assigning a bounded variable to a value strictly comprised between its bounds may results in infinite loop
because such branching decisions will not be refutable.
{{% /alert %}}

### Enumerated domains

When the domain of an integer variable is said to be *enumerated*, it is represented through
the set of possible values, in the form:
- $[\\![a,b]\\!]$ where $a$ and $b$ are integers such that $a \leq b$
- $\{a,b,c,..,z\}$, where $a < b < c ... < z$.
Enumerated domains provide more information than bounded domains but are heavier in memory (the domain usually requires a bitset).

To specify you want to use enumerated domains, either set the `boundedDomain` argument to `false` when creating variables by specifying two bounds
or use the signature that specifies the array of possible values:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "enumerated" >}}
{{< /tabpane >}}


{{% alert title="Info" color="primary" %}}
If the API without the `boundedDomain` argument is used, the engine will select the more accurate representation 
depending on the domain's cardinality. 
{{% /alert %}}

## Boolean variables

Boolean variables, `BoolVar`, are specific `IntVar` that take their value in $[\\![0,1]\\!]$.
The avantage of `BoolVar` is twofold:
- They can be used to say whether or not constraint should be satisfied (reification)
- Their domain, and some filtering algorithms, are optimized

To create a new boolean variable:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "boolVar" >}}
{{< /tabpane >}}

## Set variables

A set variable, `SetVar`, represents a set of integers, i.e. its value is a set of integers.
Its domain is defined by a set interval $[\\![m,o]\\!]$ where:


* the lower bound, $m$, is an `ISet` object which contains integers that figure in every solution.


* the upper bound, $o$, is an `ISet` object which contains integers that potentially figure in at least one solution,

Initial values for both $m$ and $o$ should be such that $m \subseteq o$.
Then, decisions and filtering algorithms will remove integers from $o$ and add some others to $m$.
A set variable is instantiated if and only if $m = o$.

A set variable can be created as follows:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "setVar" >}}
{{< /tabpane >}}

## Graph Variables

A graph variable $G$ is a variable which takes its values in a finite set of graphs. The domain of a graph variable $G$ is a graph interval $[\underline{G}, \overline{G}]$, with $\underline{G}$ a graph called the _kernel_ (or _lower bound_) of $G$ and $\overline{G}$ the _envelope_ (or _upper bound_) of $G$. Any instantiation $v_G$ of $G$ is such that $\underline{G} \subseteq v_G \subseteq \overline{G}$.

The lower bound is composed of two sets: the set of nodes  and the set of edges that belong to all instantiation of $G$.

The upper bound is composed of two sets: the set of nodes and the set of edges that potentially figure in at least one solution.

A graph variable is instantiated if and only if $\underline{G} = \overline{G}$.

A graph variable can be created as follows:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "graphVar" >}}
{{< /tabpane >}}


## Real variables

The domain of a real variable is an interval of doubles. Conceptually, the value of a real variable is a double.
However, it uses a precision parameter for floating number computation,
so its actual value is generally an interval of doubles, whose size is constrained by the precision parameter.
Constraints over real variables rely on the [Ibex solver](http://www.ibex-lib.org/), see [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}).

A real variable is declared with three doubles defining its bound and a precision:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "realVar" >}}
{{< /tabpane >}}


{{% alert title="Info" color="primary" %}}
`pychoco` version does not support `realvar`.
{{% /alert %}}

## Task variables

Task variables are used for scheduling problems. A task has a start time (IntVar), a duration, and an end time (IntVar).
A `Task` is also a propagator ensuring `start + duration = end`.
When the end is built by Choco as a view of the start (fixed duration, no end given), the relation always holds.
Otherwise, it only holds once the task is **posted** (`task.post()`) or given to a scheduling constraint, such as `cumulative`.

### Basic Task

A task is created with a start variable and a fixed duration:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "task" >}}
{{< /tabpane >}}

You can also specify an end variable or a variable duration:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "taskVariants" >}}
{{< /tabpane >}}

Or from time bounds (EST, LST, ECT, LCT):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "taskBounds" >}}
{{< /tabpane >}}

### Optional Task

An optional task is a task that may or may not be performed. It has an additional Boolean variable `performed` that indicates whether the task is active.
Optional tasks are not yet available in pychoco.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "optionalTask" >}}
{{< /tabpane >}}

Optional tasks are useful in scheduling problems where some tasks are optional or conditional.

### Task arrays

Create arrays of task variables for modeling multiple scheduling entities (not yet available in pychoco):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "taskArray" >}}
{{< /tabpane >}}

## Views: Creating variables from constraints

When a variable is defined as a function of another variable, views can be
used to make the model shorter. In some cases, the view has a specific (optimized) domain representation.
Otherwise, it is simply a modeling shortcut to create a variable and post a constraint at the same time.

### Arithmetical views

An arithmetical view requires an integer variable.
In Java, `offset`, `neg`, `mul` and `intView` replace `intOffsetView`, `intMinusView`, `intScaleView` and `intAffineView`, which are deprecated;
pychoco keeps the former names.

#### $x = y + 2$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "offsetView" >}}
{{< /tabpane >}}

#### $x = -y$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "negView" >}}
{{< /tabpane >}}

#### $x = 3\times y$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "mulView" >}}
{{< /tabpane >}}

### Logical views

A logical view is based on an integer variable, a basic arithmetical relation ($=,\neq,\leq,\geq$) and a constant. The resulting view states whether or not the relation holds.
In Java, `isEq`, `isNeq`, `isLeq` and `isGeq` replace `intEqView`, `intNeView`, `intLeView` and `intGeView`, which are deprecated.

#### $b \Leftrightarrow (x = 4)$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "isEq" >}}
{{< /tabpane >}}

#### $b \Leftrightarrow (x \neq 4)$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "isNeq" >}}
{{< /tabpane >}}


#### $b \Leftrightarrow (x \leq 4)$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "isLeq" >}}
{{< /tabpane >}}

#### $b \Leftrightarrow (x \geq 4)$ :

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "isGeq" >}}
{{< /tabpane >}}

#### $d \Leftrightarrow \neg b$
This is a specific case, related to the negation of a `BoolVar`.
No additional variable is needed, a view based on the variable to refute is enough. 

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "boolNot" >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
The same result can be obtained in shorted version (in Java only):

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "boolNotShort" >}}
{{< /tabpane >}}


{{% /alert %}}



### Composition 

Views can be combined together, e.g. $x = 2\times y + 5$ is an affine view:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "composition" >}}
{{< /tabpane >}}

### View over real variable  
We can also use a view mecanism to link an integer variable with a real variable.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "realIntView" >}}
{{< /tabpane >}}

This code enables to embed an integer variable in a constraint that is defined over real variables.

## Result Variables

Result variables are convenience methods that create a new variable and immediately post a constraint linking it to other variables or values. This eliminates the need to manually create the variable and post the constraint separately.
They are not yet available in pychoco.

### Sum result variable

Create a sum variable directly:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "sumResult" >}}
{{< /tabpane >}}

### Count result variable

Create a count variable that counts occurrences of a value:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "countResult" >}}
{{< /tabpane >}}

### Min/Max result variables

Create minimum and maximum variables:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "minMaxResult" >}}
{{< /tabpane >}}

### Argument min/max

Create variables representing the index of the minimum or maximum:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "argminResult" >}}
{{< /tabpane >}}

### Element result variable

Create an element variable that retrieves a value from a table by index:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "elementResult" >}}
{{< /tabpane >}}

### Set result variables

Result variables for set operations:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/Variables" "setResults" >}}
{{< /tabpane >}}

These result variables are particularly useful when you need an intermediate variable that's purely derived from other variables, avoiding the boilerplate of creating the variable and posting the constraint separately.
