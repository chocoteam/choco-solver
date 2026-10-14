---
title: "The keysort example"
date: 2022-12-01
type: docs
math: "true"
weight: 102
description: >
  An example of the use of the keysort constraint.
---

In this example, we are going to see how to use the `StableKeysort` constraint.
The `StableKeysort(L,P,S,k)` provides a *view* $S$ of an array of variables $L$ in which those variables are sorted using a stable multi-criteria sort on the first k keys.
Hence, it eases the expression of constraints on both the *sorted* side of the problem.

:heavy_exclamation_mark: *This model was developed with Choco-solver v.4.10.11* :heavy_exclamation_mark:

## A scheduling problem

Consider a task scheduling problem, consisting of 10 tasks fixed in time, to be executed in a given day.
Any task is either easy or hard. 

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
int[] start_dates = {[6, 18, 2, 14, 2, 7, 0, 15, 7, 17]};
int[] durations = {[1, 2, 2, 1, 2, 1, 2, 1, 1, 1]};
int[] difficulties = {[1, 0, 0, 1, 0, 0, 0, 1, 1, 1]};
{{< /tab >}}
{{< /tabpane >}}

The team of workers consists of five people: three beginners and two experts. An expert can perform tasks of any difficulty and can work up to 9 hours a day. 
A beginner can only do easy tasks and cannot work more than 6 hours a day.

The aim is to assign each task to a worker while respecting the working time of each individual.


### Imports

First, let's import the needed classes. 

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.stream.IntStream;
{{< /tab >}}
{{< /tabpane >}}
And create an instance of the `Model` class:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = new Model("Scheduling");
{{< /tab >}}
{{< /tabpane >}}



### One worker per task
For convenience, we consider that beginners are assigned to a value in $[0,2]$ and experts in $[3,4]$.

Then, we can define the tasks, from the inputs:
{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] starts = IntStream.range(0, n)
                    .mapToObj(i -> model.intVar("S_" + i, start_dates[i])).toArray(IntVar[]::new);
IntVar[] durs = IntStream.range(0, n)
                    .mapToObj(i -> model.intVar("D_" + i, durations[i]))
                    .toArray(IntVar[]::new);
IntVar[] ends = IntStream.range(0, n)
                    .mapToObj(i -> model.intVar("E_" + i, start_dates[i] + durations[i]))
                    .toArray(IntVar[]::new);
IntVar[] users = IntStream.range(0, n)
                    .mapToObj(i -> model.intVar("U_" + i, difficulties[i] == 0 ? 0 : 3, 4))
                    .toArray(IntVar[]::new); // 0 -> 2 : beginners, 3-4 : experts
{{< /tab >}}
{{< /tabpane >}}
The `starts`, `durs` and `ends` variables are defined as constants, only the `users` are to be defined.
The domain of each variable that represents the user that does the task is adapted to the difficulty. Indeed, a difficult task cannot be done by a beginner, whereas an expert can do any type of task.

We declare a `DiffN` constraint to ensure that a worker cannot process two tasks at a time. 
The `DiffN` constraint holds if no two pairs of *rectangles* overlap in all dimensions.
A rectangle is a two-dimension object, defined by an origin and a length on each dimension.   
Here, the x-axis will indicate the time and the y-axis the resources.
So, a task is a rectangle whose the starting time and the user are the origins and the duration and the value $1$ are the lengths.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
model.diffN(
        starts, users, // origins
        durs, IntStream.range(0, n).mapToObj(i -> model.intVar(1)).toArray(IntVar[]::new), // lengths
        true // additional filtering based on cumulative reasoning
).post();
{{< /tab >}}
{{< /tabpane >}}

#### Redundant constraints

In our example, since starting times and durations are fixed, it is possible to analyze the tasks in order to detect tasks that overlap in time and thus must be executed by different users.
Some inequality constraints can be added.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
BiPredicate<Integer, Integer> overlap = (i, j) ->
        (start_dates[j] <= start_dates[i] && start_dates[i] < start_dates[j] + durations[j])
                || (start_dates[i] <= start_dates[j] && start_dates[j] < start_dates[i] + durations[i]);
for (int i = 0; i < n; i++) {
    for (int j = i + 1; j < n; j++) {
        if (overlap.test(i, j)) {
            System.out.printf("[%d,%d] ov [%d,%d]\n",
                    start_dates[i], start_dates[i] + durations[i],
                    start_dates[j], start_dates[j] + durations[j]);
            users[i].ne(users[j]).post();
        }
    }
}
{{< /tab >}}
{{< /tabpane >}} 
Here, only two pairs of tasks overlap in time, so posting $\ne$ constraints is sufficient.
But, in more complex cases, it would be worthwhile to detect clique of inequalities and post `AllDifferent` constraints.


### A sorted view of the world
Now that we have ensured that each task is performed by a single worker, we must ensure that everyone's working time is respected.
However, with the variables present, this is a relatively complicated exercise.

This is where the `StableKeysort` constraint comes in.

Description from *Beldiceanu et al. (2015) A Modelling Pearl with Sortedness Constraints. GCAI 2015, Tbilisi, Georgia.*.
> Given two lists of variables $L$ and $S$ and an $k$ an integer, this constraint holds if and
only if the list $S$ of variables form a rearrangement of $L$ that 
 is stably sorted by nondecreasing lexicographical order on the first k positions.

We already have the variables forming $L$ (namely, `starts`, `durations`, `ends` and `users`), so we need to introduce variables representing $S$.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] sortedStarts = model.intVarArray("SS", n, 0, 23);
IntVar[] sortedDurs = model.intVarArray("SD", n, 1, 3);
IntVar[] sortedEnds = model.intVarArray("SE", n, 1, 24);
IntVar[] sortedUsers = model.intVarArray("SU", n, 0, 4);
{{< /tab >}}
{{< /tabpane >}}

The constraint signature includes an optional array of permutation variables. We will declare it to simplify the display of solutions, but it is sometimes convenient to have access to it to constrain the sort.

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] permutations = model.intVarArray("P", n, 1, n);
model.keySort(
        IntStream.range(0, n).mapToObj(i -> new IntVar[]{users[i], starts[i], durs[i], ends[i]}).toArray(IntVar[][]::new),
        permutations,
        IntStream.range(0, n).mapToObj(i -> new IntVar[]{sortedUsers[i], sortedStarts[i], sortedDurs[i], sortedEnds[i]}).toArray(IntVar[][]::new),
        2
).post();
{{< /tab >}}
{{< /tabpane >}}
We specify 2 as the last parameter, indicating that the sorting only applies to `users` then `starts`.

We now have access to the tasks of each worker, sorted by increasing start. In fact, we do not have this information directly, however, the way the variables are ordered will allow us to easily extract this information.
In order to do this, we will first use refine equality constraints, indicating - when false - user changes. Such Boolean variables are named `y` in the following. 
The mathematical expression of these constraints is as follows:
1. $y_0  = false $
2. $ \forall i \in [1,n-1], y_i \iff (sU_{i-1} = sU_{i})$

Once these changes are known, it is possible to calculate the working time of each worker incrementally.
The working times depend on the durations of the tasks of a worker and the time elapsed between two tasks of the same worker.   

3. $w_0 = sD_0$
4. $\forall i \in [1,n-1], w_i = sD_i + y_i \times (sS_i - sE_{i-1})$

Now that the working time are valuated, they can be bounded. 
If the worker is a beginner, its working time may not exceed 6 hours, 9 hours otherwise.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
BoolVar[] y = model.boolVarArray(n);
IntVar[] w = new IntVar[n];
for (int i = 0; i < n; i++) {
    if (i == 0) {
        y[i].eq(0).post();
        w[i] = sortedDurs[i];
    } else {
        model.reifyXeqY(sortedUsers[i - 1], sortedUsers[i], y[i]);
        w[i] = sortedDurs[i].add(y[i].ift(w[i - 1].add(sortedStarts[i].sub(sortedEnds[i - 1])), 0)).intVar();
    }
    w[i].le(sortedUsers[i].lt(EXPERT).ift(beginnerWorkingTime, expertWorkingTime)).post();
}
{{< /tab >}}
{{< /tabpane >}}

All that remains is to define a research strategy.
It is optional but since only the users are to be found, we can restrict the decision variables to this set.

### Solving

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Solver solver = model.getSolver();                 
solver.printShortFeatures();                       
solver.setSearch(Search.inputOrderLBSearch(users));
{{< /tab >}}
{{< /tabpane >}}

We look for the first solution and print it.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
if (solver.solve()) {                                                                             
    System.out.printf("\nSolution #%d\n", solver.getSolutionCount());                             
    for (int i = 0; i < n; i++) {                                                                 
        System.out.printf("\tTask #%d [%d,%d] by user #%d (%s)\n",                                
                i + 1, starts[i].getValue(), ends[i].getValue(), users[i].getValue(),             
                users[i].getValue() < EXPERT ? "B" : "E");                                        
    }                                                                                             
    System.out.print("In sorted world:\n");                                                       
    for (int i = 0; i < n; i++) {                                                                 
        if (i == 0 || sortedUsers[i - 1].getValue() != sortedUsers[i].getValue()) {               
            System.out.printf("\tUser #%d (%s):\n", sortedUsers[i].getValue(),                    
                    sortedUsers[i].getValue() < EXPERT ? "B" : "E");                              
        }       
        System.out.printf("\t\tTask #%d [%d,%d]\n",                                               
                permutations[i].getValue(), sortedStarts[i].getValue(), sortedEnds[i].getValue());
        if (i == n - 1 || sortedUsers[i].getValue() != sortedUsers[i + 1].getValue()) {           
            System.out.printf("\t--> working time : %d\n", w[i].getValue());                      
        }                           
    }           
    solution = true;                                                          
}                                                                                                 
solver.printShortStatistics();                                                                    
{{< /tab >}}
{{< /tabpane >}}

```
Model[keysort], 250 variables, 127 constraints, building time: 0,264s, w/o user-defined search strategy, w/o complementary search strategy

Solution #1
  Task #1 [6,7] by user #3 (E)
  Task #2 [18,20] by user #0 (B)
  Task #3 [2,4] by user #1 (B)
  Task #4 [14,15] by user #3 (E)
  Task #5 [2,4] by user #2 (B)
  Task #6 [7,8] by user #1 (B)
  Task #7 [0,2] by user #2 (B)
  Task #8 [15,16] by user #4 (E)
  Task #9 [7,8] by user #3 (E)
  Task #10 [17,18] by user #4 (E)
In sorted world:
  User #0 (B):
    Task #2 [18,20]
  --> working time : 2
  User #1 (B):
    Task #3 [2,4]
    Task #6 [7,8]
  --> working time : 6
  User #2 (B):
    Task #7 [0,2]
    Task #5 [2,4]
  --> working time : 4
  User #3 (E):
    Task #1 [6,7]
    Task #9 [7,8]
    Task #4 [14,15]
  --> working time : 9
  User #4 (E):
    Task #8 [15,16]
    Task #10 [17,18]
  --> working time : 3
Model[keysort], 1 Solutions, Resolution time 0,244s, 105 Nodes (431,2 n/s), 186 Backtracks, 0 Backjumps, 96 Fails, 0 Restarts  
```

## Conclusion 
We have seen how the `StableKeysort` constraint can be useful to have a second representation of a state. It then becomes relatively simple to constrain each of the views. However, this requires the use of reification constraints to determine when a user is changed or, for a user, when tasks are changed. 
The use of this constraint is not trivial and requires care but it offers great flexibility and strong expressive power.



## All together

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
int EXPERT = 3;
int beginnerWorkingTime = 6;
int expertWorkingTime = 9;
Model model = new Model("keysort");
int n = 10;
int[] start_dates = IntStream.range(0, n).map(i -> rnd.nextInt(24)).toArray();
int[] durations = IntStream.range(0, n).map(i -> 1 + rnd.nextInt(2)).toArray();
int[] difficulties = IntStream.range(0, n).map(i -> rnd.nextInt(2)).toArray(); // 0: easy, 1: difficult

System.out.printf("int[] start_dates = {%s};\n", Arrays.toString(start_dates));
System.out.printf("int[] durations = {%s};\n", Arrays.toString(durations));
System.out.printf("int[] difficulties = {%s};\n", Arrays.toString(difficulties));

// Where the tasks are not ordered
IntVar[] starts = IntStream.range(0, n).mapToObj(i -> model.intVar("S_" + i, start_dates[i])).toArray(IntVar[]::new);
IntVar[] durs = IntStream.range(0, n).mapToObj(i -> model.intVar("D_" + i, durations[i])).toArray(IntVar[]::new);
IntVar[] ends = IntStream.range(0, n).mapToObj(i -> model.intVar("E_" + i, start_dates[i] + durations[i])).toArray(IntVar[]::new);
IntVar[] users = IntStream.range(0, n) 
        .mapToObj(i -> model.intVar("U_" + i,
                difficulties[i] == 0 ? 0 : 3, 4)) 
        .toArray(IntVar[]::new); // 0 -> 2 : beginners, 3-4 : experts

model.diffN(
        starts, users,
        durs, IntStream.range(0, n).mapToObj(i -> model.intVar(1)).toArray(IntVar[]::new),
        true
).post();

// Where (views of) the tasks are ordered by resources, then by starting time
IntVar[] sortedStarts = model.intVarArray("SS", n, 0, 23);
IntVar[] sortedDurs = model.intVarArray("SD", n, 1, 3);
IntVar[] sortedEnds = model.intVarArray("SE", n, 1, 24);
IntVar[] sortedUsers = model.intVarArray("SU", n, 0, 4);

// Ordered view of the tasks
IntVar[] permutations = model.intVarArray("P", n, 1, n);
model.keySort(
        IntStream.range(0, n).mapToObj(i -> new IntVar[]{users[i], starts[i], durs[i], ends[i]}).toArray(IntVar[][]::new),
        permutations,
        IntStream.range(0, n).mapToObj(i -> new IntVar[]{sortedUsers[i], sortedStarts[i], sortedDurs[i], sortedEnds[i]}).toArray(IntVar[][]::new),
        3
).post();

// In the sorted side
BoolVar[] y = model.boolVarArray("shift", n); // Boolean variable : y_i = (u'_(i-1) == u_i)
IntVar[] w = new IntVar[n];
for (int i = 0; i < n; i++) {
    if (i == 0) {
        y[i].eq(0).post();
        w[i] = sortedDurs[i];
    } else {
        model.reifyXeqY(sortedUsers[i - 1], sortedUsers[i], y[i]);
        w[i] = sortedDurs[i].add(y[i].ift(w[i - 1].add(sortedStarts[i].sub(sortedEnds[i - 1])), 0)).intVar();
    }
    w[i].le(sortedUsers[i].lt(EXPERT).ift(beginnerWorkingTime, expertWorkingTime)).post();
}

// Redundant constraints
BiPredicate<Integer, Integer> overlap = (i, j) ->
        (start_dates[j] <= start_dates[i] && start_dates[i] < start_dates[j] + durations[j])
                || (start_dates[i] <= start_dates[j] && start_dates[j] < start_dates[i] + durations[i]);
for (int i = 0; i < n; i++) {
    for (int j = i + 1; j < n; j++) {
        if (overlap.test(i, j)) {
            System.out.printf("[%d,%d] ov [%d,%d]\n",
                    start_dates[i], start_dates[i] + durations[i],
                    start_dates[j], start_dates[j] + durations[j]);
            users[i].ne(users[j]).post();
        }
    }
}
Solver solver = model.getSolver();
solver.printShortFeatures();
solver.setSearch(Search.inputOrderLBSearch(users));
if (solver.solve()) {
    System.out.printf("\nSolution #%d\n", solver.getSolutionCount());
    for (int i = 0; i < n; i++) {
        System.out.printf("\tTask #%d [%d,%d] by user #%d (%s)\n",
                i + 1, starts[i].getValue(), ends[i].getValue(), users[i].getValue(),
                users[i].getValue() < EXPERT ? "B" : "E");
    }
    System.out.print("In sorted world:\n");
    for (int i = 0; i < n; i++) {
        if (i == 0 || sortedUsers[i - 1].getValue() != sortedUsers[i].getValue()) {
            System.out.printf("\tUser #%d (%s):\n", sortedUsers[i].getValue(),
                    sortedUsers[i].getValue() < EXPERT ? "B" : "E");
        }
        System.out.printf("\t\tTask #%d [%d,%d]\n",
                permutations[i].getValue(), sortedStarts[i].getValue(), sortedEnds[i].getValue());
        if (i == n - 1 || sortedUsers[i].getValue() != sortedUsers[i + 1].getValue()) {
            System.out.printf("\t--> working time : %d\n", w[i].getValue());
        }
    }
    solution = true;
}
solver.printShortStatistics();
{{< /tab >}}
{{< /tabpane >}}






