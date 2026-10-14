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

Settings are built with `SettingsBuilder`, which provides three starting points: `SettingsBuilder.init()` (default),
`SettingsBuilder.dev()` and `SettingsBuilder.prod()`, adapted to a development or production environment.
They can then be modified via setters, e.g. `new Model("m", SettingsBuilder.init().setLCG(true))`.


Settings are declared in a `Model` constructor.
Settings are not immutable but modifying value after `Model` construction can lead to unexpected behavior.

## Former extensions of Choco

The former extensions *choco-parsers* and *choco-graph* are now part of Choco-solver:
the parsers are described in [Parsing]({{< ref "/docs/Parsing" >}})
and graph variables in [Constraints over graph variables]({{< ref "/docs/Modeling/GraphConstraints.md" >}}).
Their repositories are archived.
