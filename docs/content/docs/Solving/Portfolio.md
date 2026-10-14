---
title: "Multi-thread resolution"
date: 2020-03-06T15:39:27+01:00
weight: 36
description: >
  How to use several threads to solve a problem?
---

Choco-solver provides a simple way to use several threads to treat a problem. The main idea of that driver is to solve the *same* model with different search strategies and to share information to make these threads help each other.

To use a portfolio of solvers in parallel, use `ParallelPortfolio` as follows:


```Java
import org.chocosolver.solver.ParallelPortfolio;

ParallelPortfolio portfolio = new ParallelPortfolio();
```

The instance of `ParallelPortfolio` is a solving manager.
The portfolio is not concerned with how models are declared. It simply executes the resolutions of each of them in parallel and shares information as soon as one of them finds a solution or finishes.

Once the instance has been created, the models need to be added. 
As soon as two or more models are added, we talk about parallel resolution.

In the following example, we will consider first a method that create and populate a model for the Golomb ruler problem:


```Java
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public static Model makeModel(int id, int m) {
    Model model = new Model(String.format("Golomb ruler (m=%d) #%d)", m, id));
    IntVar[] ticks = model.intVarArray("a", m, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);
    model.addHook("ticks", ticks);
    IntVar[] diffs = model.intVarArray("d", (m * m - m) / 2, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);
    model.addHook("diffs", diffs);
    model.arithm(ticks[0], "=", 0).post();

    for (int i = 0; i < m - 1; i++) {
        model.arithm(ticks[i + 1], ">", ticks[i]).post();
    }

    for (int k = 0, i = 0; i < m - 1; i++) {
        for (int j = i + 1; j < m; j++, k++) {
            // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
            model.arithm(ticks[j], "-", ticks[i], "=", diffs[k]).post();
            model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
            model.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2).post();
            model.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2).post();
        }
    }
    model.allDifferent(diffs, "BC").post();
    // break symetries
    if (m > 2) {
        model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
    }
    model.addHook("objective", ticks[m - 1]);
    model.setObjective(Model.MINIMIZE, ticks[m - 1]);
    return model;
}
```

Now, multiple occurrences of the same model can be declared in the portfolio:


```Java
int nbModels = 5;
for(int s=0;s<nbModels;s++){
    portfolio.addModel(makeModel(s, 10));
}
```

Here all models are the same and the portfolio will change the search heuristics of all models but the first one.
This means that the first thread will run according to your settings whereas the others will have a different configuration.

Then, a call to `portfolio.solve()` will look for the first solution, whatever model you use to find it.
The solver that found the solution first can then be consulted using the `portfolio.getBestModel()` method.

{{< tabpane langEqualsHeader=true >}}
{{< tab header="While loop" >}}
```Java
int nbSols = 0;
while (portfolio.solve()) {
    Model finder = portfolio.getBestModel();
    // get the solution
    System.out.println(finder.getObjective());
}
portfolio.getModels().forEach(m -> m.getSolver().reset()); // to solve the models several times
```
{{< /tab >}}
{{< tab header="Stream API" >}}
```Java
// Using Stream API for solution enumeration
portfolio.streamSolutions().forEach(solution -> {
    System.out.println(solution);
});
```
{{< /tab >}}
{{< /tabpane >}}

    a[9] = 80
    a[9] = 75
    a[9] = 73
    a[9] = 72
    a[9] = 68
    a[9] = 67
    a[9] = 64
    a[9] = 62
    a[9] = 60
    a[9] = 55


### Auto-configuration

In order to specify yourself the configuration of each thread, you need to create the portfolio by setting the optional
boolean argument `searchAutoConf` to false as follows:


```Java
ParallelPortfolio portfolio = new ParallelPortfolio(false);
```

When dealing with multi threading resolution, very few data is shared between threads:
every time a solution has been found its value is shared among solvers. Moreover,
when a solver ends, it communicates an interruption instruction to the others.
This enables to explore the search space in various way, using different model settings such as search strategies
(this should be done in the dedicated method which builds the model, though).

This assumes that the models are identical. It is possible to declare different models. But in the case of COP, you need to be aware that the value of the objective variable will be broadcast to the models for each new solution, to reduce the search space.

### Sharing no-goods

Some of the default search strategies are based on a restart policy.
In such case, one can allow no-goods when those threads restart.


```Java
portfolio.stealNogoodsOnRestarts();
```

Doing so, anytime a thread restarts, it records not only no-goods based on the search space it has explored since last restart, but also ones of other threads (restricted to those equipped with a restart policy). 
In that case, the models should all be identical: same variables and same constraints, declared in the same order.
Indeed, the unique variables' id is used to share no-goods between models.

### Stream-based solution enumeration

In Choco, you can use the Java Stream API to enumerate solutions from the portfolio:

```Java
// Get all solutions as a stream
portfolio.streamSolutions().forEach(solution -> {
    Model finder = solution.getModel();  // get the model that found this solution
    System.out.println(solution);
});
```

This is particularly useful when you want to process solutions in a functional style or apply transformations and filters to the solution stream.

### Complete example with helpers

Here's a complete example combining `stealNogoodsOnRestarts()` and stream-based enumeration:

```Java
int nbModels = 5;
ParallelPortfolio portfolio = new ParallelPortfolio();

for(int s = 0; s < nbModels; s++){
    portfolio.addModel(makeModel(s, 10));
}

// Enable no-good sharing across restarts
portfolio.stealNogoodsOnRestarts();

// Enumerate solutions using streams
int solutionCount = (int) portfolio.streamSolutions()
    .peek(sol -> System.out.println("Found: " + sol))
    .count();

System.out.println("Total solutions: " + solutionCount);
```
