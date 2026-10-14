---
title: "Statistics"
date: 2020-03-06T17:54:44+01:00
weight: 34
---

Resolution data are available in the `Solver` object, whose default output is `System.out`.
It centralises widely used methods to have comprehensive feedback about the resolution process.
There are two types of methods: those who need to be called **before** the resolution, with a prefix show, and those who need to called **after** the resolution, with a prefix print.

For instance, one can indicate to print the solutions all resolution long:

```java
solver.showSolutions();
solver.findAllSolutions();
```

Or to print the search statistics once the search ends:

```java
solver.solve();
solver.printStatistics();
```

On a call to `solver.printVersion()`, the following message will be printed:

```none
** Choco 6.0.0 (2026-05) : Constraint Programming Solver, Copyright (c) 2010-2026
```

### Resolution measures

On a call to `solver.printStatistics()`, the following message will be printed:

```none
- [ Search complete - [ No solution | {0} solution(s) found ]
  | Incomplete search - [ Limit reached | Unexpected interruption ] ].
   Solutions: {0}
[  Maximize = {1}  ]
[  Minimize = {2}  ]
   Building time : {3}s
   Resolution : {6}s
   Nodes: {7} ({7}/{6} n/s)
   Backtracks: {8}
   Fails: {9}
   Restarts: {10}
   Max depth: {11}
   Variables: {12}
   Constraints: {13}
```

Curly brackets *{instruction | }* indicate alternative instructions

Brackets *[instruction]* indicate an optional instruction.

If the search terminates, the message “Search complete” appears on the first line, followed with either the number of solutions found or the message “No solution”.
`Maximize` –resp. `Minimize`– indicates the best known value for the objective variable before exiting when an (single) objective has been defined.

Curly braces *{value}* indicate search statistics:


1. number of solutions found
2. objective value in maximization
3. objective value in minimization
4. building time in second (from `new Model()` to `solve()` or equivalent)
5. initialisation time in second (before initial propagation)
6. initial propagation time in second
7. resolution time in second (from `new Model()` till now)
8. number of nodes in the binary tree search : one for the root node and between one and two for each decision (two when the decision has been refuted)
9. number of backtracks achieved
10. number of failures that occurred (conflict number)
11. number of restarts operated
12. maximum depth reached in the binary tree search
13. number of variables in the model
14. number of constraints in the model

If the resolution process reached a limit before ending *naturally*, the title of the message is set to:

```none
- Incomplete search - Limit reached.
```

The body of the message remains the same. The message is formatted thanks to the `IMeasureRecorder`.

### Showing solutions

On a call to `solver.showSolutions()`, on each solution the following message will be printed:

```none
{0} Solutions, [Maximize = {1}][Minimize = {2}], Resolution {6}s, {7} Nodes, \\
                                    {8} Backtracks, {9} Fails, {10} Restarts
```

followed by one line exposing the value of each decision variables (those involved in the search strategy).


### Showing decisions

On a call to `solver.showDecisions()`, on each node of the search tree a message will be printed indicating which decision is applied.
The message is prefixed by as many “.” as nodes in the current branch of the search tree.
A decision is prefixed with `[R]` and a refutation is prefixed by `[L]`.

```none
..[L]x  ==  1 (0) //X = [0,5] Y = [0,6] ...
```

{{% alert title="Warning"%}}
A call to `solver.showDecisions()` prints the tree search during the resolution.
Printing the decisions slows down the search process.
{{%/alert%}}
