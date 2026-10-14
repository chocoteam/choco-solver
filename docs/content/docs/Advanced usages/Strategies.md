---
title: "Designing a search strategy"
date: 2020-01-07T16:06:55+01:00
weight: 53
math: "true"
description: >
  How to design search strategies?
---

## Automatic Search Configuration with BlackBoxConfigurator

### Overview

`BlackBoxConfigurator` is the **recommended approach** for configuring search strategies. Rather than manually selecting variable selectors, value selectors, and meta-strategies, you provide high-level problem characteristics and the configurator builds an optimized strategy automatically.

This is particularly useful when:
- You're unsure which combination of heuristics will work best
- You want robust default behavior across different problem types
- You need quick prototyping without deep search expertise
- You want automatic tuning based on problem characteristics

### Basic usage

```java
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;

Solver solver = model.getSolver();

// For CSP (Constraint Satisfaction): find any feasible solution
BlackBoxConfigurator.init()
    .forCSP()
    .build()
    .configureSearch(solver);

// For COP (Constraint Optimization): find optimal solution
BlackBoxConfigurator.init()
    .forCOP()
    .build()
    .configureSearch(solver);
```

### Customization options

You can customize the configurator with additional settings:

```java
var configurator = BlackBoxConfigurator.init()
    .forCOP()
    .searchRandomized()           // Use randomized value selection
    .maxDomainValues(1000)        // Threshold for adaptive heuristics
    .build();

configurator.configureSearch(solver);
```

**Common customization methods:**
- `.searchDomOverWDeg()` — Use dom/wdeg heuristic (impact-based)
- `.searchRandomized()` — Use randomized value selection
- `.maxDomainValues(int)` — Set domain size threshold for branching decisions
- `.withMaxRestarts(int)` — Configure restart policy

### How it works

The `BlackBoxConfigurator` internally:
1. Analyzes the problem structure (number of variables, constraint density, etc.)
2. Selects appropriate variable and value selectors
3. Optionally applies meta-strategies (restart, last-conflict, etc.)
4. Composes multiple strategies for different variable types (IntVar, SetVar, RealVar)

This is more robust than manual configuration for most problems.

---

## MetaStrategy: Composing strategies

### Overview

`MetaStrategy` is an abstract class for wrapping base strategies with meta-heuristics. Meta-strategies enhance the behavior of a base strategy without changing the base strategy itself.

Common meta-strategies include:
- **Last-Conflict** (LastConflict): Focuses on variables involved in recent failures
- **Conflict Ordering Search** (COS): Combines search history with variable selection

### Using MetaStrategy

```java
import org.chocosolver.solver.search.strategy.strategy.MetaStrategy;
import static org.chocosolver.solver.search.strategy.Search.*;

Solver solver = model.getSolver();

// Wrap a base strategy with last-conflict
AbstractStrategy<IntVar> baseStrategy = minDomLBSearch(x, y, z);
AbstractStrategy<IntVar> metaStrategy = lastConflict(baseStrategy);

solver.setSearch(metaStrategy);
```

### Composing multiple meta-strategies

You can compose multiple strategies, with the first being executed, then falling back to subsequent ones:

```java
solver.setSearch(
    lastConflict(domOverWDegSearch(x, y, z)),
    setVarSearch(sets),
    realVarSearch(reals)
);
```

### Common meta-strategies in Search factory

- `lastConflict(AbstractStrategy<IntVar>, int k)` — Focus on k variables involved in recent failures
- `conflictOrderingSearch(AbstractStrategy<IntVar>)` — COS: use conflict history
- `greedySearch(AbstractStrategy<IntVar>)` — No backtracking (greedy)
- `sequencer(AbstractStrategy...)` — Chain multiple strategies

---

## IntDomainSticky: Memory-based value selection

### Overview

`IntDomainSticky` is a value selector that **remembers previously chosen values** for variables and tends to revisit them. This can be very effective for problems with **strong locality**.

### Usage

```java
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainSticky;
import static org.chocosolver.solver.search.strategy.Search.*;

Solver solver = model.getSolver();

solver.setSearch(intVarSearch(
    new FirstFail(model),              // Variable selector
    new IntDomainSticky(),             // Value selector (sticky)
    x, y, z
));
```

### When to use

`IntDomainSticky` is particularly useful for:
- **Clustering problems**: Solutions tend to group around certain value assignments
- **Warm-start scenarios**: You have hints from previous runs or similar problems
- **Search with locality**: Neighboring solutions are often good (e.g., scheduling, routing)

### Example: Scheduling with locality

In scheduling problems, if task A was scheduled at time 10 in a previous solution, scheduling it near time 10 again is often promising:

```java
IntVar[] taskStarts = model.intVarArray("taskStart", nbTasks, 0, horizon);

solver.setSearch(intVarSearch(
    new FirstFail(model),
    new IntDomainSticky(),    // Remember previous start times
    taskStarts
));
```

---

### Designing your own search strategy

#### Using selectors

To design your own strategy using `Search.intVarSearch`, you simply have to implement
your own variable and value selectors:

```java
public static IntStrategy intVarSearch(VariableSelector<IntVar> varSelector,
                                    IntValueSelector valSelector,
                                    IntVar... vars)
```

For instance, to select the first non instantiated variable and assign it to its lower bound:

```java
Solver s = model.getSolver();
s.setSearch(intVarSearch(
        // variable selector
        (VariableSelector<IntVar>) variables -> {
            for(IntVar v:variables){
                if(!v.isInstantiated()){
                    return v;
                }
            }
            return null;
        },
        // value selector
        (IntValueSelector) var -> var.getLB(),
        // variables to branch on
        x, y
));
```

**NOTE**: When all variables are instantiated, a `VariableSelector` must return `null`.

#### From scratch

You can design your own strategy by creating `Decision` objects directly as follows:

```java
s.setSearch(new AbstractStrategy<IntVar>(x,y) {
    // enables to recycle decision objects (good practice)
    PoolManager<IntDecision> pool = new PoolManager();
    @Override
    public Decision getDecision() {
        IntDecision d = pool.getE();
        if(d==null) d = new IntDecision(pool);
        IntVar next = null;
        for(IntVar v:vars){
            if(!v.isInstantiated()){
                next = v; break;
            }
        }
        if(next == null){
            return null;
        }else {
            // next decision is assigning nextVar to its lower bound
            d.set(next,next.getLB(), DecisionOperator.int_eq);
            return d;
        }
    }
});
```

### Making a decision greedy

You can make a decision non-refutable by using `decision.setRefutable(false)` inside your 

To make an entire search strategy greedy, use:

```java
Solver s = model.getSolver();
s.setSearch(greedySearch(inputOrderLBSearch(x,y,z)));
```



### Large Neighborhood Search (LNS)


##### Defining its own neighborhoods

A presentation of the [functioning of LNS]({{< ref "/docs/Solving/LNS.md" >}}) was given earlier. It is said that anyone can define its own neighbor, dedicated to the problem solved.
This is achieved by extending either the abstract class `IntNeighbor` or by implementing the interface `INeighbor`. 
The former implements all methods from the latter but `void fixSomeVariables() throws ContradictionException;` that defines which variables should be fixed to their value in the last solution. 


`INeighbor` forces to implements the following methods:

```java
/**
 * Action to perform on a solution. 
 * Typically, storing the current variables’ value.
 */
public void recordSolution() {
    // where `values` and `variables` are class instances
    for (int i = 0; i < variables.length; i++) {
        values[i] = variables[i].getValue();
    }
}  
```


```java
/**
 * Fix some variables to their value in the last solution.
 */
public void fixSomeVariables() throws ContradictionException{
    // An example of random neighbor where a coin is tossed
    // for each variable to choose if it is fixed or not in the current fragment
    for (int i = 0; i < variables.length; i++) {
        if(Math.random() < .9){
            variables[i].instantiateTo(values[i], this);
            // alternatively call: `this.freeze(i);`    
        }
    }
}

```

```java
/**
 * Relax the number of variables fixed. 
 * Called when no solution was found during a LNS run 
 * (i.e., trapped into a local optimum).
 * if the fragment is based on a class instance (e.g, number of fixed variables)
 * it may be updated there
 */
void restrictLess()
// for instance, the threshold (0.9) previously declare in `fixSomeVariables()`
// can be reduced here
```

```java
/** 
 * Indicates whether the neighbor is complete, that is, can end.
 * Most of the time, this is related to `restrictLess()`
 */
boolean isSearchComplete()
```

## Recommendations and best practices

### When to use each approach

| Situation | Recommended | Reason |
|-----------|-------------|--------|
| **Quick prototyping** | BlackBoxConfigurator | Automatic tuning, minimal code |
| **CSP/COP problem** | BlackBoxConfigurator | Handles both satisfaction and optimization |
| **Custom domain knowledge** | Manual selectors | Fine-grained control for expert optimization |
| **Research/education** | Manual design | Understanding how search works |
| **Production system** | BlackBoxConfigurator | Robustness across problem variations |
| **LNS with custom operators** | Manual + IntNeighbor | Domain-specific optimizations |

### Strategy selection workflow

1. **Start with BlackBoxConfigurator** — it handles most problems well
2. **Profile and identify bottlenecks** — use solver statistics
3. **If performance is insufficient**, consider:
   - Customizing BlackBoxConfigurator settings
   - Adding meta-strategies (lastConflict, COS)
   - Implementing custom selectors
4. **For LNS**, implement domain-specific neighbors

### Performance tips

- **Use `IntDomainSticky`** if the problem has strong locality
- **Combine strategies** rather than replacing them
- **Apply lastConflict** to adaptive/dynamic problems
- **Monitor solver statistics** to detect inefficiencies
- **Test different configurations** with your instances
// for instance, if the threshold is 0. after a certain number of attempts.
```




