---
title: "Follow-Up"
date: 2020-01-31T11:48:29+01:00
type: docs
weight: 13
description: >
  Some improvements to the original 8-queen model.
---

We will now see and comment some modifications of the code presented
previously :

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
int n = 8;
Model model = new Model(n + "-queens problem");
IntVar[] vars = new IntVar[n];
for(int q = 0; q < n; q++){
    vars[q] = model.intVar("Q_"+q, 1, n);
}
for(int i  = 0; i < n-1; i++){
    for(int j = i + 1; j < n; j++){
        model.arithm(vars[i], "!=",vars[j]).post();
        model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
        model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
    }
}
Solution solution = model.getSolver().findSolution();
if(solution != null){
    System.out.println(solution.toString());
}
{{< /tab >}}
{{< /tabpane >}}

Variables
---------

First, lines 3-6 can be compacted into:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
{{< /tab >}}
{{< /tabpane >}}

Doing so, an n-array of variables with [1,n]-domain is created. Each
variable name is "Q[i]" where *i* is its position in the array, starting
from 0. The last parameter, set to false, indicates that the domains
must be enumerated (not bounded).

Constraints
-----------

Second, lines 9 to 11 can be replaced by:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
vars[i].ne(vars[j]).post();
vars[i].ne(vars[j].sub(j - i)).post();
vars[i].ne(vars[j].add(j - i)).post();
{{< /tab >}}
{{< /tabpane >}}

where *ne* stands for *not equal*. Theses instructions express the same
constraints, or more complex expressions, in a convenient way. Here the
expression is posted as a decomposition: the AST is analyzed and
additional variables and constraints are added on the fly.

{{% alert title="Info" color="primary" %}}
Calling `e.post()` on an expression `e` is a syntactic sugar for
`e.decompose().post()`.
{{% /alert %}}


Alternatively, one can decide to generate the possible combinations from
the expression and post *table* constraints [^1]. To do so, the
expression should be first turned into extension constraint then be
posted

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
vars[i].ne(vars[j]).extension().post();
vars[i].ne(vars[j].sub(j - i)).extension().post();
vars[i].ne(vars[j].add(j - i)).extension().post();
{{< /tab >}}
{{< /tabpane >}}

Global constraints
------------------

Here we posted three groups of 28 constraints. The first group expresses
that two queens cannot be on the same column by posting a *clique* of
inequality constraints. The second and third groups express the same
conditions for each diagonal.

In other words, the variables of each groups must be *all different*.
Luckily, there exists a *global constraint* that captures that
conditions:

> Global constraints specify patterns that occur in many problems and exploit efficient and effective constraint propagation algorithms for pruning the search space. - [C.Bessière *et al.*, AAAI 2004](https://www.aaai.org/Papers/AAAI/2004/AAAI04-018.pdf).

We can reformulate the set of constraints to:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
IntVar[] diag1 = new IntVar[n];
IntVar[] diag2 = new IntVar[n];
for(int i = 0 ; i < n; i++){
    diag1[i] = vars[i].sub(i).intVar();
    diag2[i] = vars[i].add(i).intVar();
}
model.post(
    model.allDifferent(vars),
    model.allDifferent(diag1),
    model.allDifferent(diag2)
);
{{< /tab >}}
{{< /tabpane >}}

The constraint on line 8 simply states that all variables from vars must
be different. The constraint on line 9 (and 10) states that all variables
from a diagonal must be different. The variables of a diagonal are given
by expressions (line 4-5).
The function l.4 maps each index *i* in the
[0,n] range to an integer variable equals to vars[i].add(i). The call to
the intVar() method effectively turns the arithmetic expression into an
integer variable. This extraction may introduce additional variables and
constraints automatically.

Solver
------

To compare the first model and the modified one, we need to get features
and measures. A call to `solver.showStatistics();` will output commonly
used indicators to the console, such as the number of variables,
constraints, solutions found, open nodes, etc.

We can either let the solver explore the search space by itself or
define a search strategy, like:

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
solver.setSearch(Search.domOverWDegSearch(vars));
{{< /tab >}}
{{< /tabpane >}}

Updated code
------------

{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
int n = 8;
Model model = new Model(n + "-queens problem");
IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
IntVar[] diag1 = IntStream.range(0, n).mapToObj(i -> vars[i].sub(i).intVar()).toArray(IntVar[]::new);
IntVar[] diag2 = IntStream.range(0, n).mapToObj(i -> vars[i].add(i).intVar()).toArray(IntVar[]::new);
model.post(
    model.allDifferent(vars),
    model.allDifferent(diag1),
    model.allDifferent(diag2)
);
Solver solver = model.getSolver();
solver.showStatistics();
solver.setSearch(Search.domOverWDegSearch(vars));
Solution solution = solver.findSolution();
if (solution != null) {
    System.out.println(solution.toString());
}
{{< /tab >}}
{{< /tabpane >}}

Running the following code outputs something like:

```
** Choco 4.0.0 (2016-05) : Constraint Programming Solver, Copyleft (c) 2010-2016
- Model[8-queens problem] features:
    Variables : 32
    Constraints : 19
    Default search strategy : no
    Completed search strategy : no
1 solution found.
    Model[8-queens problem]
    Solutions: 1
    Building time : 0,000s
    Resolution time : 0,012s
    Nodes: 6 (491,9 n/s)
    Backtracks: 0
    Fails: 0
    Restarts: 0
    Variables: 32
    Constraints: 19
Solution: Q[0]=7, Q[1]=4, Q[2]=2, Q[3]=8, Q[4]=6, Q[5]=1, Q[6]=3, Q[7]=5,
```

Basically, the trace informs that:

-   there are 32 variables: the eight queens, and the additional ones
    induced by expressions extraction,
-   there are 19 constraints: three allDifferent constraints, and the
    additional ones induced by expressions extraction,
-   one solution has been found,
-   it took 11 ms to find it,
-   in the meantime, 6 decisions were made and none of them were
    wrong.

[^1]: such constraint are defined by a set of allowed/forbidden tuples.