---
title: "Search Strategies"
date: 2020-01-07T16:06:55+01:00
weight: 33
math: "true"
description: >
  How to define search strategies?
---

The search space induced by variable domains is equal to $S=|d_1|\times|d_2|\times\cdots\times|d_n|$ where $d_i$ is the domain of the $i^{th}$ variable.
Most of the time (not to say always), constraint propagation is not sufficient to build a solution, that is, to remove all values but one from variable domains.
Thus, the search space needs to be explored using one or more *search strategies*.
A search strategy defines how to explore the search space by computing *decisions*.
A decision involves a variables, a value and an operator, e.g. $x = 5$, and triggers new constraint propagation.
Decisions are computed and applied until all the variables are instantiated, that is, a solution has been found, or a failure has been detected (backtrack occurs).
Choco-solver builds a binary search tree: each decision can be refuted (if $x = 5$ leads to no solution, then $x \neq 5$ is applied).
The classical search is based on [Depth First Search](http://en.wikipedia.org/wiki/Depth-first_search).

**NOTE**: There are many ways to explore the search space and this steps should not be overlooked.
Search strategies or heuristics have a strong impact on resolution performances.
Thus, it is strongly recommended to adapt the search space exploration to the problem treated.

### Default search strategy

If no search strategy is specified to the resolver, Choco-solver relies on a default one,
built by [`BlackBoxConfigurator`](#blackboxconfigurator-automatic-search-configuration).
It splits variables according to their type and defines specific search strategies for each type that are sequentially applied:

1. integer and boolean variables: `intVarSearch(ivars)` (based on `domOverWDegSearch`),
2. set variables: `setVarSearch(svars)`,
3. graph variables: `graphVarSearch(gvars)`,
4. real variables: `realVarSearch(rvars)`,
5. objective variable, if any: lower bound or upper bound, depending on the optimization direction.

A last conflict meta-strategy is also plugged in.
In many cases, this strategy will not be sufficient to produce satisfying performances and it will be necessary
to specify a dedicated strategy, using `solver.setSearch(...)`, or to tune the black box.
The default search can be replaced with `SettingsBuilder.init().setDefaultSearch(...)`, when creating the model.

### Specifying a search strategy

You may specify a search strategy to the resolver by using `solver.setSearch(...)` method as follows:

```java
import static org.chocosolver.solver.search.strategy.Search.*;

// to use the default SetVar search on mySetVars
Solver s = model.getSolver();
s.setSearch(setVarSearch(mySetVars));

// to use activity based search on myIntVars
Solver s = model.getSolver();
s.setSearch(activityBasedSearch(myIntVars));

// to use activity based search on myIntVars
// then the default SetValSelectorFactoryVar search on mySetVars
Solver s = model.getSolver();
s.setSearch(activityBasedSearch(myIntVars), setVarSearch(mySetVars));
```

**NOTE**: Search strategies generally hold on some particular variable kinds only (e.g. integers, sets, etc.).

#### Example

Let us consider we have two integer variables `x` and `y` and we want our strategy to select
the variable of smallest domain and assign it to its lower bound.
There are several ways to achieve this:

```java
// 1) verbose approach using usual imports

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;


    Solver s = model.getSolver();
    s.setSearch(Search.intVarSearch(
                    // selects the variable of smallest domain size
                    new FirstFail(model),
                    // selects the smallest domain value (lower bound)
                    new IntDomainMin(),
                    // apply equality (var = val)
                    DecisionOperatorFactory.makeIntEq(),
                    // variables to branch on
                    x, y
    ));
```

```java
// 2) Shorter approach : Use a static import for Search
// and do not specify the operator (equality by default)

import static org.chocosolver.solver.search.strategy.Search.*;

import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;


    Solver s = model.getSolver();
    s.setSearch(intVarSearch(
                    // selects the variable of smallest domain size
                    new FirstFail(model),
                    // selects the smallest domain value (lower bound)
                    new IntDomainMin(),
                    // variables to branch on
                    x, y
    ));


// 3) Shortest approach using built-in strategies imports

import static org.chocosolver.solver.search.strategy.Search.*;

    Solver s = model.getSolver();
    s.setSearch(minDomLBSearch(x, y));
```

### List of available search strategy

Most available search strategies are listed in `Search`.
This factory enables you to create search strategies using static methods.
Most search strategies rely on :
- variable selectors (see package `org.chocosolver.solver.search.strategy.selectors.variables`)
- value selectors (see package `org.chocosolver.solver.search.strategy.selectors.values`)
- operators (see `DecisionOperator`)

`Search` is not exhaustive, look at the selectors package to see learn more search possibilities.

{{% alert title="Info" color="primary" %}}
Note that some strategies are *dynamic* and might work more efficiently when combined with a [restart policy]({{< ref "Restarts.md" >}}).
{{%/alert%}}

## Advanced Search Strategy Composition

### Round-robin search strategies

The `roundRobinSearch` combines multiple search strategies in a cyclic fashion:

```java
import static org.chocosolver.solver.search.strategy.Search.*;

// Alternate between different variable selection heuristics
solver.setSearch(roundRobinSearch(myIntVars));
```

The `adaptiveRoundRobinSearch` is more sophisticated — it uses a **multi-armed bandit** (UCB1 algorithm) to adaptively select the best combination of variable selector, value selector, and meta-strategy based on past performance:

```java
solver.setSearch(adaptiveRoundRobinSearch(myIntVars));
```

This is particularly useful for problems where:
- No single strategy dominates across different search phases
- You want the solver to automatically tune its search behavior
- You have multiple good strategies but no way to predict which will work best

### BlackBoxConfigurator: automatic search configuration

`BlackBoxConfigurator` builds a complete search strategy for a model: variables are grouped by type
(integer, set, graph, real), each group gets a strategy, then a meta-strategy (e.g. last conflict)
and a restart policy are added.
**It is what Choco uses when no search strategy is declared** (`BlackBoxConfigurator.init().make(model)`).

Two presets are tuned for satisfaction (`forCSP()`) and optimization (`forCOP()`) problems:

{{< tabpane langEqualsHeader=true >}}
{{< example "solving/Strategies" "blackBox" >}}
{{< /tabpane >}}

Each part can be changed with a `set...` method before calling `make(model)`:
`setIntVarStrategy`, `setSetVarStrategy`, `setGraphVarSearch`, `setRealVarStrategy`,
`setIntObjVarStrategy`, `setMetaStrategy`, `setRestartPolicy`, `setNogoodOnRestart`,
`setRestartOnSolution`, `setExcludeViews`, `setExcludeObjective`, ...

{{< tabpane langEqualsHeader=true >}}
{{< example "solving/Strategies" "blackBoxCustom" >}}
{{< /tabpane >}}

`complete(model, strategy)` completes a declared strategy with the black-box one, for the variables it does not cover.

### Value selectors: IntDomainSticky

`IntDomainSticky` is a value selector implementing *phase saving*:
it selects the last value a variable took during propagation, if this value is still in its domain,
and falls back to another value selector otherwise.
It is useful together with restarts, to drive the search back towards the regions it explored before:

{{< tabpane langEqualsHeader=true >}}
{{< example "solving/Strategies" "sticky" >}}
{{< /tabpane >}}
