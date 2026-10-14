---
title: "Restarts"
date: 2020-01-07T16:06:55+01:00
weight: 35
math: "true"
description: >
  How to define restart policy?
---

Restart means stopping the current tree search, then starting a new tree search from the root node.
Restarting makes sense only when coupled with randomized dynamic branching strategies ensuring that the same enumeration tree is not constructed twice.
The branching strategies based on the past experience of the search, such as adaptive search strategies, are more accurate in combination with a restart approach.

Unless the number of allowed restarts is limited, a tree search with restarts is not complete anymore. It is a good strategy, though, when optimizing an NP-hard problem in a limited time.

Some adaptive search strategies resolutions are improved by sometimes restarting the search exploration from the root node.
Thus, the statistics computed on the bottom of the tree search can be applied on the top of it.

Several restart strategies are available in `Solver`.

### On solutions

It may be relevant to restart after each solution.

```java
// Restarts after after each new solution.
solver.setRestartOnSolutions()
```

### Geometrical

Geometrical restarts perform a search with restarts controlled by the resolution event  `counter` which counts events occurring during the search.
Parameter `base` indicates the maximal number of events allowed in the first search tree.
Once this limit is reached, a restart occurs and the search continues until `base`$\times$` grow` events are done, and so on.
After each restart, the limit number of events is increased by the geometric factor `grow`.
`limit` states the maximum number of restarts.

```java
solver.setGeometricalRestart(int base, double grow, ICounter counter, int limit)
```

{{%alert title="Info"%}}
Some counters may required an argument on constructor that defines the limit to not overpass.
Such an argument is ignored by a restart strategy which overrides that value with its own computed one.
{{%/alert%}}

### Luby

The [Luby](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.47.5558) ’s restart policy is an alternative to the geometric restart policy.
It performs a search with restarts controlled by the number of resolution events  counted by `counter`.
The maximum number of events allowed at a given restart iteration is given by base multiplied by the Las Vegas coefficient at this iteration.
The sequence of these coefficients is defined recursively on its prefix subsequences:
starting from the first prefix $1$, the $(k+1)^{th}$ prefix is the $k^{th}$ prefix repeated `grow` times and
immediately followed by coefficient `grow`$^k$.


* the first coefficients for `grow` = 2 :
$$ [1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,1,\cdots]$$


* the first coefficients for `grow` =3 : 
$$ [1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,\cdots] $$

```java
solver.setLubyRestart(int base, int grow, ICounter counter, int limit)
```

You can design your own restart strategies using:

```java
solver.setRestarts( LongCriterion restartCriterion,
                    IRestartStrategy restartStrategy,
                    int restartsLimit);
```

### Recording no-goods on restart

When a restart occurs, one may want to prevent the search space explored before restarting to be discovered again in the future. This is achieved by recording *no-goods* on restart.

By calling: 
```java
solver.setNoGoodRecordingFromRestarts();
```
anytime a restart occurs, a *no-good* is extracted from the decision path in order to prevent the same scanning the same sub-tree in the futur.