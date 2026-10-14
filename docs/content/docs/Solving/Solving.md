---
title: "Lauching the resolution process"
date: 2020-01-07T16:06:55+01:00
weight: 31
description: >
  How to look for solutions?
---


## Finding one solution

A call to `solver.solve()` launches a resolution which stops on the first solution found, if any:

{{< tabpane langEqualsHeader=true >}}
{{< example "solving/Solving" "solve" >}}
{{< /tabpane >}}

If `solver.solve()` returns `true`, then a solution has been found and each variable is instantiated to a value.
Otherwise, two cases must be considered:


* A limit has been declared and reached (`solver.isStopCriterionMet()` returns true).
There may be a solution, but the solver has not been able to find it in the given limit
or there is no solution but the solver has not been able to prove it (i.e., to close to search tree) in the given limit.
The resolution process stops in no particular place in the search tree.


* No limit has been declared or reached: The problem has no solution and the solver has proved it.

An alternative API exists to search for a first [solution](/docs/solving/solutions/) and return it.

```java
Solution solution = solver.findSolution();
```

The behaviour of `findSolution()` is equivalent to that of `solve()`: when a solution has been found a `Solution` is returned. Otherwise, `null` is returned, indicating that no solution has been found below the given limit (if any).



## Enumerating all solutions

You can enumerate all solutions of a problem with a simple while loop as follows:

```java
while(solver.solve()){
    // do something, e.g. print out variable values
}
```

After the enumeration, the solver closes the search tree and variables are no longer instantiated to a value.

An alternative API exists to search for all [solutions](/docs/solving/solutions/) and return a list of them or an empty list if no solution was found.

```java
List<Solution> solutions = solver.findAllSolutions();
```

It is also possible to stream solutions:

```java
Stream<Solution> solutionsStream = solver.streamSolutions();
```


## Optimization

In Constraint-Programming, optimization is done by computing improving solutions, until reaching an optimum.
Therefore, it can be seen as solving multiple times the model while adding constraints on the fly to prevent the solver from computing dominated solutions.

### Mono-objective optimization

The optimization process is the following: anytime a solution is found, the value of the objective variable is stored and a *cut* is posted.
The cut is an additional constraint which states that the next solution must be (strictly) better than the current one.
To solve an optimization problem, you must specify which variable to optimize and in which direction:

```java
// to maximize X
model.setObjective(Model.MAXIMIZE, X);
// or model.setObjective(Model.MINIMIZE, X); to minimize X
while(solver.solve()){
    // an improving solution has been found
}
// the last solution found was optimal (if search completed)
```

It also possible to use an alternative API to search for the optimal [solution](/docs/solving/solutions/).
In that case, there is no need to declare the objective in the model, the paramters can directly be given as input of the methode:

```java
Solution solution = solver.findOptimalSolution(X, Model.MAXIMIZE);
```

Another API makes possible to find all optimal solutions.

```java
List<Solution> solutions = solver.findAllOptimalSolutions(X, Model.MAXIMIZE);
// alterantively, get a stream of optimal solutions
//Stream<Solution> solutionsStream = solver.streamOptimalSolutions(X, Model.MAXIMIZE);
```
This is a two-stage method that first searches for an optimal solution, then temporarily fixes the value of the objective and stores all the solutions with this additional constraint in a list.


#### Adapting the cut

You can use custom cuts by overriding the default cut behavior.
The *cut computer* function defines how the cut should bound the objective variable.
The input *number* is the best solution value found so far, the output *number* define the new bound.
When maximizing (resp. minimizing) a problem, the cut limits the lower bound (resp. upper bound) of the objective variable.
For instance, one may want to indicate that the value of the objective variable in the next solution should be
greater than or equal to the best value + 10:

{{< tabpane langEqualsHeader=true >}}
{{< example "solving/Solving" "cut" >}}
{{< /tabpane >}}

`setStrictDynamicCut()` (the default: strictly better solutions) and `setWalkingDynamicCut()` (solutions at least as good)
are predefined cuts.

### Multi-objective optimization

#### Pareto

If you have multiple objective to optimize, you have several options. First, you may aggregate them in a function so that you end up with only one objective variable. Second, you can solve the problem multiple times, each one optimizing one variable and possibly fixing some bounds on the other. Third, you can enumerate solutions (without defining any objective) and add constraints on the fly to prevent search from finding dominated solutions. This is done by the ParetoOptimizer object which does the following:
Anytime a solution is found, a constraint is posted which states that at least one of the objective variables must be strictly better:
Such as $(X_0 < b_0 \lor X_1 < b_1 \lor \ldots \lor X_n < b_n)$ where $X_i$ is the ith objective variable and $b_i$ its value.

Here is a simple example on how to use the `findParetoFront(...)` API to optimize two variables (a and b):



```Java
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.Solution;

// simple model
Model model = new Model();
IntVar a = model.intVar("a", 0, 2, false);
IntVar b = model.intVar("b", 0, 2, false);
IntVar c = model.intVar("c", 0, 2, false);
model.arithm(a, "+", b, "=", c).post();

Solver solver = model.getSolver();
// create an object that will store the best solutions and remove dominated ones
List<Solution> front = solver.findParetoFront(new IntVar[]{a,b},Model.MAXIMIZE); 
System.out.println("The pareto front has "+front.size()+" solutions : ");
for(Solution s: front){
        System.out.println("a = "+s.getIntVal(a)+" and b = "+s.getIntVal(b));
}
```

    The pareto front has 3 solutions : 
    a = 0 and b = 2
    a = 1 and b = 1
    a = 2 and b = 0


**NOTE**: All objectives must be optimized on the same direction (either minimization or maximization).


#### Lexico

It is also possible to consider several objectives $O_1, 02, ..., O_n$ ordered lexicographically.
Anytime a solution is found, a constraint is posted which states that the objective variables must be strictly better if the previous ones remain unchanged:
$(X_1 < b_1) \lor (X_1 = b_1 \land X_2 < b_2) \lor \ldots \lor( \bigwedge_{1 < i < n} X_i = b_i \land X_n < b_n)$
where $X_i$ is the $i^{th}$ objective variable and $b_i$ its value in the solution.

```java
Solution solution = solver.findLexOptimalSolution(objectives, Model.MAXIMIZE);
```

### Stream-based optimization

The `streamOptimalSolutions()` method returns a stream of optimal solutions:

```java
model.setObjective(Model.MINIMIZE, objective);
solver.streamOptimalSolutions(objective, Model.MINIMIZE)
    .forEach(solution -> System.out.println("Optimal: " + solution));
```

## Solution Processing Helpers

### Processing each solution with measurements

The `eachSolutionWithMeasure()` helper allows you to process each solution along with solver measurements (nodes, fails, time, etc.):

```java
solver.eachSolutionWithMeasure((solution, measures) -> {
    System.out.println("Solution found");
    System.out.println("  Nodes: " + measures.getNodeCount());
    System.out.println("  Time: " + measures.getTimeCountInNanoSeconds());
    System.out.println("  Fails: " + measures.getFailCount());
});
```

This is useful for profiling and understanding the solver's behavior when finding solutions.

### Solution sampling

The `tableSampling()` method enables you to enumerate a representative sample of solutions rather than all solutions. This is useful for large solution spaces:

```java
// Sample 100 solutions from the solution space
// - pivot: restart after N nodes to explore different parts of the tree
// - nbVariablesInTable: number of variables tracked for solution diversity
// - probaTuple: probability of accepting a solution tuple
List<Solution> samples = solver.tableSampling(100, 5, 0.8, new Random(42));

for (Solution s : samples) {
    System.out.println(s);
}
```

### Solution hook

The `onSolution()` method registers a callback that fires every time a solution is found:

```java
solver.onSolution(() -> {
    System.out.println("New solution found!");
    System.out.println("Objective = " + objective.getValue());
});

// Run search
while (solver.solve()) {
    // The callback above is called automatically for each solution
}
```

This is a simpler alternative to using monitors when you just need to execute code on each solution:

```java
// Compare with the monitor approach (still valid)
solver.plugMonitor((IMonitorSolution) () -> {
    System.out.println("New solution found!");
});
```

## Propagation

One may want to propagate all constraints without search for a solution.
This can be achieved by calling `solver.propagate()`.
This method runs, in turn, the domain reduction algorithms of the constraints until it reaches a fix point.
It may throw a `ContradictionException` if a contradiction occurs.
In that case, the propagation engine must be flushed calling `solver.getEngine().flush()`
to ensure there is no pending events.

**WARNING**: If there are still pending events in the propagation engine, the propagation may results in unexpected results.


```Java

```
