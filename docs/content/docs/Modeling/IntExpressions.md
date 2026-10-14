---
title: "Building expressions on integer variables"
date: 2020-01-07T16:07:23+01:00
weight: 24
math: "true" 
description: >
  How to build and use expressions?
---

Choco-solver offers the possibility to combine integer variables  into an expression.

## Expressions with `IntVar`

Three types of expressions can be defined with `IntVar` : arithmetic, logical and relational.


### Arithmetic expressions

First, any `IntVar` is an arithmetic expression itself.

Based on one variable `x`, an **arithmetic expression** can be built with the following operators : 
- `x.neg()`: returns $-x$, 
- `x.abs()`: returns $|x|$, 
- `x.sqr()`: returns $x^2$,
- `x.add(y1, y2, ...)`: returns $x+y_1+y_2+\ldots$, 
- `x.sub(y)`returns $x - y$, 
- `x.mul(y1, y2, ...)`: returns $x\times y_1\times y_2\times \ldots$, 
- `x.div(y)`: returns $\frac{x}{y}$, as an Euclidean division, rounding towars $0$,
- `x.mod(y)`: returns $x \mod y$, 
- `x.pow(y)`: returns $x^y$,
- `x.min(y)`: returns $\min(x,y)$, 
- `x.max(y)`: returns $\max(x,y)$, 
- `x.dist(y)`: returns $|x - y|$.

Note that `y` can be either an integer or an arithemic expression.

An arithmetic expression can be turned into an `IntVar` by calling the `intVar()` method on it.
If necessary, it creates intermediary variable and posts intermediary constraints then returns the resulting variable.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
IntVar x = model.intVar(1, 5);
IntVar y = model.intVar(1, 5);
// z = min((x+5)%3, y^2);
IntVar z = x.add(5).mod(3).min(y.pow(2)).intVar();
{{< /tab >}}
{{< /tabpane >}}


### Relational expressions

Based on an **arithmetic expression** `x`, a relational expression can be built using the following operators:
- `x.lt(y)`: states that $x < y$, 
- `x.le(y)`: states that $x \leq y$,
- `x.gt(y)`: states that $x > y$,
- `x.ge(y)`: states that $x \geq y$,
- `x.ne(y)`: states that $x \neq y$,
- `x.eq(y)`: states that $x = y$.

Note that `y` can be either an integer or an arithemic expression.

A relational expression can be posted into the model or be turned into a Boolean variable.

#### As a decomposition

Calling `decompose()` on a relation expression will return a `Constraint` object. It must be then posted or reified.

The expression forms a tree structure where nodes are either expressions (including variables) and branches are operators.
Leaves are either an `int` or `IntVar`.
When decomposed, an analysis of the tree structure is done, starting from leaves. 
A call to this method creates additional variables and posts additional constraints.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
IntVar x = model.intVar(0, 5);
IntVar y = model.intVar(0, 5);
x.ge(y).decompose().post();
{{< /tab >}}

{{< /tabpane >}}
Note that `post()` can be directly called from a relation expression and stands for `.decompose().post()`;

#### As a Table constraint

Alternatively, tuples can be extracted from a relation expression and a Table constraint be posted.
This is achieved calling the `extension()` method which returns a `Constraint` object (that needs to be posted).

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
IntVar x = model.intVar(0, 5);
IntVar y = model.intVar(0, 5);
x.ge(y).extension().post();
{{< /tab >}}

{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}
The allowed combinations are extracted from the expression by generating all possible combinations and filtering the valid ones (the ones that satisfy the relationship).

{{% /alert %}}


#### As a Boolean variable

Any relation expression can be turned into a `BoolVar` by calling the `boolVar()` method.
The resulting Boolean variable indicates whether or not the relationship holds.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
IntVar x = model.intVar(0, 5);
IntVar y = model.intVar(0, 5);
BoolVar b = x.gt(y).boolVar();
{{< /tab >}}

{{< /tabpane >}}


### Logical expressions

First, any logical expression is a relation expression itself.

Based on an **relational expression** `r`, a logical expression can be built using the following operators:
- `r.and(p1,p2,...)` : returns $(r \land p_1 \land p_2 \land \dots)$,
- `r.or(p1,p2,...)`: returns $(r \lor p_1 \lor p_2 \lor \dots)$,
- `r.xor(p1,p2,...)`: returns $(r \oplus p_1 \oplus p_2 \oplus \dots)$,
- `r.imp(p)`: returns $(r \Rightarrow p)$,
- `r.iff(p1,p2,...)`: returns $(r \Leftrightarrow p_1 \Leftrightarrow p_2 \Leftrightarrow \dots)$,
- `r.not()`: returns $(\neg r)$,
- `r.ift(y1,y2)`: returns $y_1$ if $r$ is true, returns $y_2$ otherwise.

Note that `pi` is relational expression and `yi` can be either an integer or an arithemic expression.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
IntVar x = model.intVar(0, 5);
IntVar y = model.intVar(0, 5);
// (x = y + 1) ==> (x + 2 < 6)
x.eq(y.add(1)).imp(x.add(2).le(6)).post();
{{< /tab >}}

{{< /tabpane >}}

## Expressions with `RealVar`

Two types of expressions can be defined with `RealVar` : arithmetic and relational.

### Arithmetic expressions
 
First any `RealVar` is an arithmetic expression itself.

Based on one variable `x`, an **arithmetic expression** can be built with the following operators : 
- `x.neg()`: returns $-x$, 
- `x.abs()`: returns $|x|$, 
- `x.add(y)`: returns $x+y$, 
- `x.sub(y)`returns $x - y$, 
- `x.mul(y)`: returns $x\times y$, 
- `x.div(y)`: returns $\frac{x}{y}$,
- `x.pow(y)`: returns $x^y$,
- `x.min(y)`: returns $\min(x,y)$, 
- `x.max(y)`: returns $\max(x,y)$, 
- `x.atan2(y)`: returns $\operatorname{atan2}{(x,y)}$, 
- `x.exp()`: returns $e^x$,
- `x.ln()`: returns $\ln{(x)}$,
- `x.sqr()`: returns $x^2$,
- `x.sqrt()`: returns $\sqrt{x}$,
- `x.cub()`: returns $x^3$,
- `x.cbrt()`: returns $\sqrt[3]{x}$,
- `x.cos()`: returns $\cos{(x)}$,
- `x.sin()`: returns $\sin{(x)}$,
- `x.tan()`: returns $\tan{(x)}$,
- `x.acos()`: returns $\arccos{(x)}$,
- `x.asin()`: returns $\arcsin{(x)}$,
- `x.atan()`: returns $\arctan{(x)}$,
- `x.cosh()`: returns $\cosh{(x)}$,
- `x.sinh()`: returns $\sinh{(x)}$,
- `x.tanh()`: returns $\tanh{(x)}$,
- `x.acosh()`: returns $\operatorname{acosh}{(x)}$,
- `x.asinh()`: returns $\operatorname{asinh}{(x)}$,
- `x.atanh()`: returns $\operatorname{atanh}{(x)}$,

Note that `y` can be either a double or an arithemic expression.


An arithmetic expression can be turned into a `RealVar` by calling the `realVar(prec)` method on it.
Here, `prec` is the precision of the variable to return.
If necessary, it creates intermediary variable and posts intermediary constraints then returns the resulting variable.


{{% alert title="Info" color="primary" %}}

A call to this method requires [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) to be installed and configured.

{{% /alert %}}


{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
double p = 0.01d;
RealVar x = model.realVar(1, 5, p);
RealVar y = model.realVar(1, 5, p);
// z = x^(y-2)
RealVar z = x.pow(y.sub(2))).realVar(p);
{{< /tab >}}

{{< /tabpane >}}


### Relational expressions

Based on an **arithmetic expression** `x`, a relational expression can be built using the following operators:
- `x.lt(y)`: states that $x < y$, 
- `x.le(y)`: states that $x \leq y$,
- `x.gt(y)`: states that $x > y$,
- `x.ge(y)`: states that $x \geq y$,
- `x.eq(y)`: states that $x = y$.

Note that `y` can be either an double or an arithemic expression.

A relational expression can be posted into the model as an equation or added to an [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) instance.

#### As an equation

Calling `equation()` on a relation expression will return a `Constraint` object that embeds a propagator using HC4 algorithm for filtering values based on the equation expressed. It must be then posted or reified. The constraint stores the expression as an internal variable.

A call to this method **does not** create additional variables and returns a single constraint.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
double p = 0.01d;
RealVar x = model.realVar(1, 5, p);
RealVar y = model.realVar(1, 5, p);
// x / (y-2)
x.div(y.sub(2))).equation().post();
{{< /tab >}}

{{< /tabpane >}}

{{% alert title="Alert" color="secondary" %}}

The following operators are not supported by `equation()` and should be declared in [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}):
```
pow, atan2, ln, tan, acos, asin, atan, cosh, sinh, tanh, acosh, asinh, atanh 
```


{{% /alert %}}

#### Into Ibex

Alternatively, an expression can be added [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}).
This is achieved calling the `ibex(prec)` method which returns a `Constraint` object where `prec` denotes the precision.
It must be then posted or reified.

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
double p = 0.01d;
RealVar x = model.realVar(1, 5, p);
RealVar y = model.realVar(1, 5, p);
// x / (y-2)
x.div(y.sub(2))).ibex(p).post();
{{< /tab >}}

{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}

A call to this method requires [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) to be installed and configured.

{{% /alert %}}

