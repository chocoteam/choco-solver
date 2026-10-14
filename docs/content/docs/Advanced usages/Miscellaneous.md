---
title: "Miscellaneous"
date: 2020-01-07T16:06:55+01:00
weight: 57
math: "true"
---

### Search monitors

#### Principle

A search monitor is an observer of the resolver.
It gives user access before and after executing each main step of the solving process:


* initialize: when the solving process starts and the initial propagation is run,


* open node: when a decision is computed,


* down branch: on going down in the tree search applying or refuting a decision,


* up branch: on going up in the tree search to reconsider a decision,


* solution: when a solution is got,


* restart search: when the search is restarted to a previous node, commonly the root node,


* close: when the solving process ends,


* contradiction: on a failure,

With the accurate search monitor, one can easily observe with the resolver, from pretty printing of a solution to learning nogoods from restart, or many other actions.

The interfaces to implement are:


* `IMonitorInitialize`,


* `IMonitorOpenNode`,


* `IMonitorDownBranch`,


* `IMonitorUpBranch`,


* `IMonitorSolution`,


* `IMonitorRestart`,


* `IMonitorContradiction`,


* `IMonitorClose`.

Most of them gives the opportunity to do something before and after a step. The other ones are called after a step.

Simple example to print every solution:

```
Solver s = model.getSolver();
s.plugMonitor(new IMonitorSolution() {
    @Override
    public void onSolution() {
        System.out.println("x = "+x.getValue());
    }
});
```

In Java 8 style:

```java
Solver s = model.getSolver();
s.plugMonitor((IMonitorSolution) () -> {System.out.println("x = "+x.getValue());});
```

Or with lambda:

```java
Solver s = model.getSolver();
s.onSolution(() -> System.out.println("x = " + x.getValue()));
```

## Model Analysis

### ModelAnalyser: Introspection and debugging

The `ModelAnalyser` class provides introspection capabilities to analyze the structure and state of your model. This is useful for:
- **Debugging**: Understanding what constraints and variables are present
- **Optimization**: Identifying unconstrained or redundant variables
- **Analysis**: Learning about propagator states and relationships

### Accessing the analyser

```java
Model model = new Model("My Problem");
// ... build model ...

ModelAnalyser analyser = model.getModelAnalyser();
```

### Analysis methods

**Overall analysis:**

```java
// Get comprehensive variable analysis
analyser.analyseVariables();

// Get comprehensive constraint analysis
analyser.analyseConstraints();

// Get full model analysis
analyser.analyseModel();
```

**Variable information:**

```java
// Get types of variables in the model
Set<String> varTypes = analyser.getVariableTypes();
// Returns: {"IntVar", "BoolVar", "SetVar", "RealVar", ...}

// Get unconstrained variables (not involved in any constraint)
List<Variable> unconstrained = analyser.getUnconstrainedVariables();

// Get variables that are views (derived from other variables)
List<Variable> viewVars = analyser.getVariablesWithViews();
```

**Constraint information:**

```java
// Get types of constraints in the model
Set<String> constraintTypes = analyser.getConstraintTypes();
// Returns: {"Arithm", "Element", "AllDifferent", ...}
```

**Propagator state analysis:**

```java
// Get propagators that are entailed (always satisfied)
List<Propagator<?>> entailed = analyser.getEntailedPropagators();

// Get propagators that are passive (no more filtering possible)
List<Propagator<?>> passive = analyser.getPassivePropagators();

// Get propagators where all variables are instantiated
List<Propagator<?>> instantiated = analyser.getCompletelyInstantiatedPropagators();

// Get reified propagators (inside if-then-else constraints)
List<Propagator<?>> reified = analyser.getReifiedPropagators();
```

### Use cases

**Finding unconstrained variables (common bug):**

```java
List<Variable> unconstrained = analyser.getUnconstrainedVariables();
if (!unconstrained.isEmpty()) {
    System.out.println("WARNING: Variables without constraints:");
    for (Variable v : unconstrained) {
        System.out.println("  " + v.getName());
    }
}
```

**Checking view usage:**

```java
List<Variable> views = analyser.getVariablesWithViews();
System.out.println("View variables (" + views.size() + "): ");
for (Variable v : views) {
    System.out.println("  " + v);
}
```

**Analyzing constraint distribution:**

```java
Set<String> types = analyser.getConstraintTypes();
System.out.println("Constraint types in model: " + types);

// Useful before choosing search strategy
// If many AllDifferent constraints, certain strategies are recommended
// If many Element constraints, others are better
```

**Detecting optimization opportunities:**

```java
// Find variables/constraints that don't contribute to search
List<Propagator<?>> entailed = analyser.getEntailedPropagators();
if (!entailed.isEmpty()) {
    System.out.println("Entailed propagators (" + entailed.size() + "):");
    // These constraints are automatically satisfied and don't filter domains
}

List<Propagator<?>> passive = analyser.getPassivePropagators();
if (!passive.isEmpty()) {
    System.out.println("Passive propagators (" + passive.size() + "):");
    // These can no longer filter and might be removed
}
```

### Best practices

- **Use analyser after model construction** to verify your model is correct
- **Check for unconstrained variables** — often indicates modeling errors
- **Analyze propagator states** before solving to understand expected behavior
- **Export results** to log files for complex models to help with debugging

---

## Settings

A `Settings` object is attached to each `Model`.
It declares default behavior for various purposes: from general purpose (such as the welcome message), modelling purpose (such as enabling views) or solving purpose (such as the search binder).

`Settings` is a _factory design pattern_ and provides two default settings: `Settings.dev()` and `Settings.prod()` which offers configurations adapted to a development or production environment. These can then be modified via setters. 


Settings are declared in a `Model` constructor.
Settings are not immutable but modifying value after `Model` construction can lead to unexpected behavior.

## Extensions of Choco

### choco-parsers

choco-parsers is an extension of Choco 4. It provides a parser for the FlatZinc language, a low-level solver input language that is the target language for MiniZinc.
This module follows the flatzinc standards that are used for the annual MiniZinc challenge. It only supports integer variables.
You will find it at [https://github.com/chocoteam/choco-parsers](https://github.com/chocoteam/choco-parsers)

### choco-graph

choco-graph is a Choco 4 module which allows to search for a graph, which may be subject to graph constraints.
The domain of a graph variable G is a graph interval in the form [G_lb,G_ub].
G_lb is the graph representing vertices and edges which must belong to any single solution whereas G_ub is the graph representing vertices and edges which may belong to one solution.
Therefore, any value G_v must satisfy the graph inclusion “G_lb subgraph of G_v subgraph of  G_ub”.
One may see a strong connection with set variables.
A graph variable can be subject to graph constraints to ensure global graph properties (e.g. connectedness, acyclicity) and channeling constraints to link the graph variable with some other binary, integer or set variables.
The solving process consists of removing nodes and edges from G_ub and adding some others to G_lb until having G_lb = G_ub, i.e. until G gets instantiated.
These operations stem from both constraint propagation and search. The benefits of graph variables stem from modeling convenience and performance.

This extension has documentation. You will find it at [https://github.com/chocoteam/choco-graph](https://github.com/chocoteam/choco-graph)

### choco-gui

choco-gui is an extension of Choco 4.
It provides a Graphical User Interface with various views which can be simply plugged on any Choco Model object.
You will find it at [https://github.com/chocoteam/choco-gui](https://github.com/chocoteam/choco-gui)

