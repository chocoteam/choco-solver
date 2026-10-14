---
title: "The knapsack problem"
type: docs
date: 2023-06-23
math: "true"
weight: 103
description: >
  A model for the knapsack problem.
---

<a href="http://en.wikipedia.org/wiki/Knapsack_problem">Wikipedia</a>:<br/>
> Given a set of items, each with a weight and a value, determine the count of each item to include in a collection so that the total weight is less than or equal to a given limit and the total value is as large as possible. It derives its name from the problem faced by someone who is constrained by a fixed-size knapsack and must fill it with the most useful items."

First manage imports.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.inputOrderUBSearch;
{{< /tab >}}
{{< /tabpane >}}

## Input data




{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
int n = 10; // number of different items
int W = 67; // a maximum weight capacity 
int[] w = new int[]{23, 26,20,18,32, 27, 29, 26, 30, 27}; // weight of items
int[] v = new int[]{505, 352, 458, 220, 354, 414, 498, 545, 473, 543}; // value of items
{{< /tab >}}
{{< /tabpane >}}

## The model

Then, we can start modelling the problem with choco.
The first step is to defined a `Model` instance.
It is required to declare and store the variables and the constraints.
For convenience, an instance can be declared with a name.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Model model = new Model("Knapsack");
{{< /tab >}}
{{< /tabpane >}}

For each object, a variable is declared to count the number of times it appears in the knapsack.


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] items = new IntVar[n];
for (int i = 0; i < n; i++) {
    items[i] = model.intVar("item_" + (i + 1), 0, (int) Math.ceil(W*1. / w[i]));
}
{{< /tab >}}
{{< /tabpane >}}

The paramaters are:
- the prefix for setting the variables' name,
- the lower bound and the upper bound of each variable.

**Remark:**
*To model 0-1 knapsack problem, the upper bound of each variable must be set to $1$.*

The problem is to maximize the sum of the values of the items in the knapsack.
This amount is maintained in a variable:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar value = model.intVar("value", 0, Arrays.stream(v).max().orElse(999) * n);
{{< /tab >}}
{{< /tabpane >}}

The sum of the weights is less than or equal to the knapsack's capacity:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar weight = model.intVar("weight", 0, W);
{{< /tab >}}
{{< /tabpane >}}

All variables are now declared, the `knapsack` constraint can be declared:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
model.knapsack(items, weight, value, w, v).post();
{{< /tab >}}
{{< /tabpane >}}

The `value` variable has to be maximized:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
model.setObjective(Model.MAXIMIZE, value);
{{< /tab >}}
{{< /tabpane >}}

## Finding the optimal solution

Now that the model is ready, the solving step can be set up.
Here we define a top-down maximization:


{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
Solver solver = model.getSolver();
solver.setSearch(
    inputOrderUBSearch(value), 
    inputOrderLBSearch(items));
{{< /tab >}}
{{< /tabpane >}}

Let's execute the solving:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
while (solver.solve()) {
    System.out.printf("Knapsack -- %d items\n", n);
    System.out.println("\tItem: Count");
    for (int i = 0; i < items.length; i++) {
        System.out.printf("\tItem #%d: %d\n", (i+1), items[i].getValue());
    }
    System.out.printf("\n\tWeight: %d\n", weight.getValue());
    System.out.printf("\n\tValue: %d\n", value.getValue());
}
solver.reset(); // to solve the model several times
{{< /tab >}}
{{< /tabpane >}}

```
Knapsack -- 10 items
	Item: Count
	Item #1: 2
	Item #2: 0
	Item #3: 1
	Item #4: 0
	Item #5: 0
	Item #6: 0
	Item #7: 0
	Item #8: 0
	Item #9: 0
	Item #10: 0

	Weight: 66

	Value: 1468
```

The optimal value for this instance of the knapsack problem is $1468$ with a total weight of $66$.

When turned to a 0-1 knapsack problem, the optimal value is $1270$ with a total weight of $67$.
