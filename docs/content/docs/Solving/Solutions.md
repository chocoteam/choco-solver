---
title: "Dealing with solutions"
date: 2020-03-06T16:42:00+01:00
weight: 32
description: >
  How to deal with `Solution`?
---

## Recording solutions

A solution can be stored through a `Solution` object which maps every variable with its current value.
It can be created as follows:

```java
Solution solution = new Solution(model);
```

### Reducing data recorded 

By default, a solution records the value of every variable, but you can specify a smaller scope in the `Solution` constructor.

Let `X` be the set of decision variables and `Y` another variable set that you need to store.
To record other variables (e.g. an objective variables) you have two options:


* Declare them in the search strategy using a complementary strategy

```java
solver.set(strategy(X),strategy(Y)).
```


* Specify which variables to store in the solution constructor

```java
Solution solution = new Solution(model(), ArrayUtils.append(X,Y));
```

You can record the last solution found as follows :

```java
Solution solution = new Solution(model);
while (solver.solve()) {
    solution.record();
}
```

You can also use a monitor as follows:

```java
Solution solution = new Solution(model);
solver.plugMonitor(new IMonitorSolution() {
      @Override
      public void onSolution() {
          s.record();
      }
});
```

Or with lambdas:

```java
Solution solution = new Solution(model);
solver.plugMonitor((IMonitorSolution) () -> s.record());
```

Note that the solution is erased on each new recording.
To store all solutions, you need to create one new solution object for each solution.

You can then access the value of a variable in a solution as follows:

```java
int val = s.getIntVal(Y[0])
```

The solution object can be used to store all variables in Choco Solver (binaries, integers, sets and reals)

## Solution Management

### Checking solution existence

The `exists()` method checks whether a solution has been recorded:

```java
Solution solution = new Solution(model);
if (solver.solve()) {
    solution.record();
}
if (solution.exists()) {
    System.out.println("Solution was recorded");
}
```

### Restoring model state

The `restore()` method restores the model to the state captured by a solution. This is useful when you want to return to a previously found solution state:

```java
Solution solution = new Solution(model);
while (solver.solve()) {
    solution.record();
}
// Model variables are now instantiated to values from the last solution
// Restore the last solution to ensure variables are instantiated
solution.restore();
System.out.println("x = " + x.getValue());
```

This is particularly useful in multi-objective optimization or when you need to manipulate variable values after the search is complete.

### Copying solutions

The `copySolution()` method creates a deep copy of a solution. This is useful when you want to preserve a solution before the next iteration overwrites it:

```java
Solution solution = new Solution(model);
List<Solution> allSolutions = new ArrayList<>();

while (solver.solve()) {
    allSolutions.add(solution.copySolution());  // deep copy
}

// Now allSolutions contains all found solutions
// (the original solution object was reused across iterations)
for (Solution s : allSolutions) {
    System.out.println("x = " + s.getIntVal(x));
}
```

## Accessing variable value

The value of a variable can be accessed directly through the `getValue()` method only once the variable is instantiated, i.e. the value has been computed
(call `isInstantiated()` to check it). Otherwise, the lower bound is returned (or an exception is thrown if `-ea` is set as JVM argument).

For instance, the following code will return the lower bound of `var` (or an assertion exception) since the resolution has not begun:

```java
int val = var.getValue();
solver.solve();
```

On the other hand, the following code may return the lower bound of `var` (or an assertion exception) if no solution could be found (unsat problem or time limit reached):

```java
solver.solve();
int val = var.getValue();
```

The correct approach to get the value of a variable `var` in a solution is :

```java
if(solver.solve()){
    int val = var.getValue();
}
```

In optimization, you can print its value in each solution:

```java
while(solver.solve()){
    System.out.println(variable.getValue());
}
```

The last printed value corresponds to the one in the best solution found.

However, the following code does *NOT* display the best solution found:

```java
while(solver.solve()){
    System.out.println(variable.getValue());
}
System.out.println("best solution found: "+variable.getValue());
```

Because it is outside the while loop, this code is reached once the search tree has been closed.
It does not correspond to a *solution state* and therefore variable is no longer instantiated at this stage.
To use solutions afterward, you need to record them using `Solution` objects.