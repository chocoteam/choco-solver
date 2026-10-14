---
title: "A Lazy Clause Generation overview"
date: 2023-06-30
type: docs
math: "true"
weight: 90
description: >
  a quick look at Lazy Clause Generation (LCG), the conflict-driven clause learning framework of Choco-solver.
---


Lazy Clause Generation (LCG) adapts the well-known SAT [CDCL algorithm](https://en.wikipedia.org/wiki/Conflict-driven_clause_learning) to constraint programming.
Domain reductions are recorded as literals, together with the reasons (explanations) that caused them.
On failure, the chain of inferences that led to the contradiction is analysed and turned into a new clause, which is added to the problem.
This clause prevents the same situation from occurring again, and makes it possible to "backjump" (non-chronological backtrack) to the appropriate decision in the decision path.

LCG is disabled by default. See [Explanations]({{< ref "/docs/Advanced usages/Explanations.md" >}}) for how it works in Choco-solver and how to enable it.

#### Warning ####

> LCG requires each constraint to explain its inferences.
> In Choco-solver, some constraints come with dedicated explanations, others are decomposed into explained ones,
> and an exception is raised for the constraints that are not supported yet.


### RCPSP
The Resource-Constrained Project Scheduling Problem (RCPSP) is a well-known optimization problem in project management. It involves scheduling a set of activities or tasks, each with a specific duration and resource requirements, while considering limited resources availability.

In RCPSP, the goal is to find an optimal or near-optimal schedule that minimizes the project's duration or cost, while respecting resource constraints. These constraints typically include limitations on the availability of resources such as workers, machines, or materials. Activities cannot be scheduled simultaneously if they require the same resource, and the overall schedule must ensure that resource capacities are not exceeded at any given time.

The problem becomes more complex when considering dependencies between activities, where some activities must be completed before others can start. The objective is to create a feasible schedule that meets all constraints and completes the project as quickly and efficiently as possible.

Here is an instance for 30 activities.
Note that two dummy activities are added, a source (1) and a sink (32), for successors declaration.
Neither of them consumes any resources.


{{< tabpane langEqualsHeader=true >}}
{{< example "tutos/CdclOverview" "data" >}}
{{< /tabpane >}}

The model is built by a method that takes as parameter whether LCG is enabled:

{{< tabpane langEqualsHeader=true >}}
{{< example "tutos/CdclOverview" "model" >}}
{{< /tabpane >}}

#### Without LCG

Finding the optimal solution to this instance of the RCPSP requires 285,653 nodes:

{{< tabpane langEqualsHeader=true >}}
{{< example "tutos/CdclOverview" "withoutLCG" >}}
{{< /tabpane >}}

    Model[RCPSP], 6 Solutions, MINIMIZE makespan = 43, Resolution time 2,085s, Time to best solution 2,085s, 285653 Nodes (136 977,3 n/s), 571295 Backtracks, 0 Backjumps, 285642 Fails, 0 Restarts, 2543232 Propagations

#### With LCG

With LCG, the same optimal solution is found, and its optimality proved, in 356 nodes only:

{{< tabpane langEqualsHeader=true >}}
{{< example "tutos/CdclOverview" "withLCG" >}}
{{< /tabpane >}}

    Model[RCPSP], 6 Solutions, MINIMIZE makespan = 43, Resolution time 0,087s, Time to best solution 0,073s, 356 Nodes (4 095,3 n/s), 356 Backtracks, 52 Backjumps, 69 Fails, 6 Restarts, 10412 Propagations

Indeed, each of the 69 failures is turned into a new clause which helps reducing the search space.
Note that the LCG configuration also enables restarts.

## Limitations ##

One may notice that, in comparison to the approach without LCG, far fewer nodes are explored per second.
This is due to the cost of the algorithm:
1. Domain reductions need to be recorded, with their reasons.
2. Each conflict is analysed to derive a new clause.
3. The number of learned clauses increases with the number of conflicts, which can slow down propagation.

That's why, from time to time, some learned clauses are forgotten.

In conclusion, LCG can be very powerful in reducing the search space.
However, this reduction sometimes does not compensate the cost of the algorithm.
