---
title: "A CD-CL overview"
date: 2023-06-30
type: docs
math: "true"
weight: 90
description: >
  a quick look a the Conflict-Driven Clause learning (CDCL) framework.
---


The CD-CL framework is an adapation of the well-konw SAT [CDCL algorithm](https://en.wikipedia.org/wiki/Conflict-driven_clause_learning) to discrete constraint solver.
By exploiting the implication graph (that records events, i.e. variables' modifications), this algorithm is able to derive a new constraint from the events that led to a contradiction. 

Once added to the constraint network, this constraint makes possible to "backjump" (non-chronological backtrack) to the appropriate decision in the decision path.

In CP, learned constraints are denoted "signed-clauses" which is a disjunction of signed-literals, *i.e.* membership unary constraints : $\bigvee_{i = 0}^{n} X_i \in D_{i}$ where $X_i$ are variables and $D_i$ a set of values.
A signed-clause is satisfied when at least one signed-literal is satisfied. 

#### Warning #### 

> In CP, CDCL algorithm requires that each constraint of a problem can be explained. Even though a default explanation function for any constraint, dedicated functions offers better performances. 
> In `choco-solver` a few set of constraints is equiped with dedicated explanation function (unary constraints, binary and ternary, sum and scalar). 


### RCPSP
The Resource-Constrained Project Scheduling Problem (RCPSP) is a well-known optimization problem in project management. It involves scheduling a set of activities or tasks, each with a specific duration and resource requirements, while considering limited resources availability.

In RCPSP, the goal is to find an optimal or near-optimal schedule that minimizes the project's duration or cost, while respecting resource constraints. These constraints typically include limitations on the availability of resources such as workers, machines, or materials. Activities cannot be scheduled simultaneously if they require the same resource, and the overall schedule must ensure that resource capacities are not exceeded at any given time.

The problem becomes more complex when considering dependencies between activities, where some activities must be completed before others can start. The objective is to create a feasible schedule that meets all constraints and completes the project as quickly and efficiently as possible.

Here is an instance for 30 activities.
Note that two dummy activities are added, a source (1) and a sink (32), for successors declaration.
Neither of them consumes any resources.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
// Set up the problem data
int numActivities = 32;  // Number of activities, including source (1) and sink (32)
int numResources = 4;  // Number of resources
int[] resourceCapacities = new int[]{10, 12, 4, 12};  // Resource capacities
int[][] successors = new int[][]{
        {2, 3, 4},
        {6, 11, 15},
        {7, 8, 13},
        {5, 9, 10},
        {20},
        {30},
        {27},
        {12, 19, 27},
        {14},
        {16, 25},
        {20, 26},
        {14},
        {17, 18},
        {17},
        {25},
        {21, 22},
        {22},
        {20, 22},
        {24, 29},
        {23, 25},
        {28},
        {23},
        {24},
        {30},
        {30},
        {31},
        {28},
        {31},
        {32},
        {32},
        {32},
        {},
};

int[] durations = {0, 8, 4, 6, 3, 8, 5, 9, 2, 7, 9, 2, 6, 3, 9, 10, 6, 5, 3, 7, 2, 7, 2, 3, 3, 7, 8, 3, 7, 2, 2, 0};

int[][] requirements = new int[][]{
        {0, 0, 0, 0},
        {4, 0, 0, 0},
        {10, 0, 0, 0},
        {0, 0, 0, 3},
        {3, 0, 0, 0},
        {0, 0, 0, 8},
        {4, 0, 0, 0},
        {0, 1, 0, 0},
        {6, 0, 0, 0},
        {0, 0, 0, 1},
        {0, 5, 0, 0},
        {0, 7, 0, 0},
        {4, 0, 0, 0},
        {0, 8, 0, 0},
        {3, 0, 0, 0},
        {0, 0, 0, 5},
        {0, 0, 0, 8},
        {0, 0, 0, 7},
        {0, 1, 0, 0},
        {0, 10, 0, 0},
        {0, 0, 0, 6},
        {2, 0, 0, 0},
        {3, 0, 0, 0},
        {0, 9, 0, 0},
        {4, 0, 0, 0},
        {0, 0, 4, 0},
        {0, 0, 0, 7},
        {0, 8, 0, 0},
        {0, 7, 0, 0},
        {0, 7, 0, 0},
        {0, 0, 2, 0},
        {0, 0, 0, 0},
};
{{< /tab >}}
{{< /tabpane >}}


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public Model rcpsp() {
   // Create the Choco model
   Model model = new Model("RCPSP");
   // Create the start time variables for each activity
   IntVar[] starts = model.intVarArray("S", numActivities, 0, 999);
   Task[] tasks = IntStream.range(0, numActivities) 
           .mapToObj(i -> new Task(starts[i], durations[i])) 
           .toArray(Task[]::new);
   
   // Add capacity constraints
   for (int r = 0; r < numResources; r++) {
       List<Task> cTasks = new ArrayList<>();
       List<IntVar> cHeights = new ArrayList<>();
       for (int i = 0; i < numActivities; i++) {
           if (requirements[i][r] > 0) {
               cTasks.add(tasks[i]);
               cHeights.add(model.intVar(requirements[i][r]));
           }
       }
       model.cumulative(cTasks.toArray(new Task[0]),
               cHeights.toArray(new IntVar[0]),
               model.intVar(resourceCapacities[r]), 
               true, 
               Cumulative.Filter.NAIVETIME
       ).post();
   }
     
   // Add precedency constraints
   for (int i = 0; i < numActivities; i++) {
       for (int j : successors[i]) {
           tasks[i].getEnd().le(tasks[j - 1].getStart()).post();
       }
   }
     
   // Define the objective function
   IntVar makespan = model.intVar(0, IntVar.MAX_INT_BOUND);
   model.max(makespan, starts).post();
   // Set the objective
   model.setObjective(Model.MINIMIZE, makespan);
     
   // Create the solver
   Solver solver = model.getSolver();
   solver.setSearch(Search.inputOrderLBSearch(starts));
   solver.showShortStatistics();
   return model;
}
{{< /tab >}}
{{< /tabpane >}}

#### Without CDCL

Finding the optimal solution to this instance of the RCPSP requires 285653 nodes:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = rcpsp();
while(model.getSolver().solve());
{{< /tab >}}
{{< /tabpane >}}

    Model[RCPSP], 1 Solutions, MINIMIZE IV_1 = 49, Resolution time 0,020s, Time to best solution 0,018s, 33 Nodes (1 671,6 n/s), 0 Backtracks, 0 Backjumps, 0 Fails, 0 Restarts
    [0mModel[RCPSP], 2 Solutions, MINIMIZE IV_1 = 47, Resolution time 3,375s, Time to best solution 3,375s, 256956 Nodes (76 129,8 n/s), 513826 Backtracks, 0 Backjumps, 256927 Fails, 0 Restarts
    [0mModel[RCPSP], 3 Solutions, MINIMIZE IV_1 = 46, Resolution time 3,378s, Time to best solution 3,377s, 256977 Nodes (76 082,1 n/s), 513903 Backtracks, 0 Backjumps, 256955 Fails, 0 Restarts
    [0mModel[RCPSP], 4 Solutions, MINIMIZE IV_1 = 45, Resolution time 3,548s, Time to best solution 3,548s, 285620 Nodes (80 499,4 n/s), 571178 Backtracks, 0 Backjumps, 285594 Fails, 0 Restarts
    [0mModel[RCPSP], 5 Solutions, MINIMIZE IV_1 = 44, Resolution time 3,550s, Time to best solution 3,550s, 285637 Nodes (80 457,0 n/s), 571220 Backtracks, 0 Backjumps, 285612 Fails, 0 Restarts
    [0mModel[RCPSP], 6 Solutions, MINIMIZE IV_1 = 43, Resolution time 3,552s, Time to best solution 3,552s, 285653 Nodes (80 427,2 n/s), 571251 Backtracks, 0 Backjumps, 285627 Fails, 0 Restarts
    [0mModel[RCPSP], 6 Solutions, MINIMIZE IV_1 = 43, Resolution time 3,554s, Time to best solution 3,552s, 285653 Nodes (80 382,1 n/s), 571295 Backtracks, 0 Backjumps, 285642 Fails, 0 Restarts
    [0m

#### With CDCL
Using CDCL, the same solution can be found in only 186 nodes only:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = rcpsp();
model.getSolver().setLearningSignedClauses();
while(model.getSolver().solve());
{{< /tab >}}
{{< /tabpane >}}

    Model[RCPSP], 1 Solutions, MINIMIZE IV_1 = 49, Resolution time 0,007s, Time to best solution 0,006s, 33 Nodes (4 968,7 n/s), 0 Backtracks, 0 Backjumps, 0 Fails, 0 Restarts
    [0mModel[RCPSP], 2 Solutions, MINIMIZE IV_1 = 47, Resolution time 0,057s, Time to best solution 0,057s, 112 Nodes (1 948,1 n/s), 100 Backtracks, 9 Backjumps, 28 Fails, 0 Restarts
    [0mModel[RCPSP], 3 Solutions, MINIMIZE IV_1 = 46, Resolution time 0,068s, Time to best solution 0,067s, 131 Nodes (1 939,1 n/s), 151 Backtracks, 14 Backjumps, 32 Fails, 0 Restarts
    [0mModel[RCPSP], 4 Solutions, MINIMIZE IV_1 = 45, Resolution time 0,072s, Time to best solution 0,072s, 156 Nodes (2 161,3 n/s), 178 Backtracks, 16 Backjumps, 33 Fails, 0 Restarts
    [0mModel[RCPSP], 5 Solutions, MINIMIZE IV_1 = 44, Resolution time 0,075s, Time to best solution 0,075s, 172 Nodes (2 294,5 n/s), 197 Backtracks, 17 Backjumps, 35 Fails, 0 Restarts
    [0mModel[RCPSP], 6 Solutions, MINIMIZE IV_1 = 43, Resolution time 0,079s, Time to best solution 0,078s, 186 Nodes (2 367,2 n/s), 214 Backtracks, 19 Backjumps, 36 Fails, 0 Restarts
    [0mModel[RCPSP], 6 Solutions, MINIMIZE IV_1 = 43, Resolution time 0,080s, Time to best solution 0,078s, 186 Nodes (2 313,4 n/s), 236 Backtracks, 20 Backjumps, 36 Fails, 0 Restarts
    [0m

Indeed, each of the 36 failures is derived into a new signed-clause which helps reducing the search space.
Since the enumeration strategy is static, so the 2nd search space is ensured to be strictly included in the 1st one. 

#### Remark ####
> The model can be improved by using stronger filtering algorithms than `Cumulative.Filter.NAIVETIME` for the CUMULATIVE constraint. But, the current implementation of the CDCL framework in choco does not support them.


## Limitations ##

One may noticed that, in comparison to the no-CDCL approach, very few nodes can be explored per second.
This is due to the application of the CDCL algorithm which globally slows down the resolution process.
Indeed:
1. Events need to be recorded in the implication graph.
2. New operations are executed on each conflict to derive a signed-clauses. 
3. The number of learned signed-clauses increases w.r.t. the number of conflict, this can slow down the propagation step (reaching a fix-point or detecting a new conflict).

The last point is probably the most expensive one. That's why, from time to time, some learned clauses need to be forgotten (in `DefaultSettings`: every 100000 failures, half of them are removed under conditions).
Beware, removing a learned sign may not be inconsequential.
For example, the "same" contradiction can thrown again in the future, or the enumeration strategy may be impacted.

In conclusion, CDCL can be very powerful in reducing search space. 
However, this reduction sometimes does not compensate the algorithm cost it comes with.
