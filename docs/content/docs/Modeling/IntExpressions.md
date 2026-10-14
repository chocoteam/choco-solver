---
title: "Building expressions on integer variables"
date: 2020-01-07T16:07:23+01:00
weight: 24
math: "true" 
description: >
  How to build and use expressions?
---

Choco-solver offers the possibility to combine integer variables into an expression.

## Expressions with `IntVar`

Three types of expressions can be defined with `IntVar` : arithmetic, logical and relational.

{{% alert title="Python" color="primary" %}}
pychoco does not build expression trees: Python operators (`+`, `-`, `*`, `/`, `**`, `==`, `<`, ...) directly
return a variable, a view or a reified constraint.
The Python examples below are equivalent formulations; the decomposition and the extension are only available in Java.
{{% /alert %}}


### Arithmetic expressions

First, any `IntVar` is an arithmetic expression itself.

Based on one variable `x`, an **arithmetic expression** can be built with the following operators : 
- `x.neg()`: returns $-x$, 
- `x.abs()`: returns $|x|$, 
- `x.sqr()`: returns $x^2$,
- `x.add(y1, y2, ...)`: returns $x+y_1+y_2+\ldots$, 
- `x.sub(y)`returns $x - y$, 
- `x.mul(y1, y2, ...)`: returns $x\times y_1\times y_2\times \ldots$, 
- `x.div(y)`: returns $\frac{x}{y}$, as an integer division, rounding towards $0$,
- `x.mod(y)`: returns $x \mod y$, 
- `x.pow(y)`: returns $x^y$,
- `x.min(y)`: returns $\min(x,y)$, 
- `x.max(y)`: returns $\max(x,y)$, 
- `x.dist(y)`: returns $|x - y|$.

Note that `y` can be either an integer or an arithmetic expression.

An arithmetic expression can be turned into an `IntVar` by calling the `intVar()` method on it.
If necessary, it creates intermediary variable and posts intermediary constraints then returns the resulting variable.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntExpressions" "arithmetic" >}}
{{< /tabpane >}}


### Relational expressions

Based on an **arithmetic expression** `x`, a relational expression can be built using the following operators:
- `x.lt(y)`: states that $x < y$, 
- `x.le(y)`: states that $x \leq y$,
- `x.gt(y)`: states that $x > y$,
- `x.ge(y)`: states that $x \geq y$,
- `x.ne(y)`: states that $x \neq y$,
- `x.eq(y)`: states that $x = y$.

Note that `y` can be either an integer or an arithmetic expression.

A relational expression can be posted into the model or be turned into a Boolean variable.

#### As a decomposition

Calling `decompose()` on a relation expression will return a `Constraint` object. It must be then posted or reified.

The expression forms a tree structure where nodes are either expressions (including variables) and branches are operators.
Leaves are either an `int` or `IntVar`.
When decomposed, an analysis of the tree structure is done, starting from leaves. 
A call to this method creates additional variables and posts additional constraints.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntExpressions" "decompose" >}}
{{< /tabpane >}}

Note that `post()` can be directly called from a relation expression and stands for `.decompose().post()`.

#### As a Table constraint

Alternatively, tuples can be extracted from a relation expression and a Table constraint be posted.
This is achieved calling the `extension()` method which returns a `Constraint` object (that needs to be posted).

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntExpressions" "extension" >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
The allowed combinations are extracted from the expression by generating all possible combinations and filtering the valid ones (the ones that satisfy the relationship).

{{% /alert %}}


#### As a Boolean variable

Any relation expression can be turned into a `BoolVar` by calling the `boolVar()` method.
The resulting Boolean variable indicates whether or not the relationship holds.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntExpressions" "boolVar" >}}
{{< /tabpane >}}


### Logical expressions

First, any logical expression is a relation expression itself.

Based on a **relational expression** `r`, a logical expression can be built using the following operators:
- `r.and(p1,p2,...)` : returns $(r \land p_1 \land p_2 \land \dots)$,
- `r.or(p1,p2,...)`: returns $(r \lor p_1 \lor p_2 \lor \dots)$,
- `r.xor(p1,p2,...)`: returns $(r \oplus p_1 \oplus p_2 \oplus \dots)$,
- `r.imp(p)`: returns $(r \Rightarrow p)$,
- `r.iff(p1,p2,...)`: returns $(r \Leftrightarrow p_1 \Leftrightarrow p_2 \Leftrightarrow \dots)$,
- `r.not()`: returns $(\neg r)$,
- `r.ift(y1,y2)`: returns $y_1$ if $r$ is true, returns $y_2$ otherwise.

Note that `pi` is relational expression and `yi` can be either an integer or an arithmetic expression.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/IntExpressions" "logical" >}}
{{< /tabpane >}}

## Expressions with `RealVar`

Expressions can also be built on real variables, see [Constraints over real variables]({{< ref "RealConstraints.md" >}}).
