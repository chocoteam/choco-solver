---
title: "Constraints over real variables"
date: 2020-01-07T16:07:15+01:00
weight: 27
math: "true" 
description: >
  How to declare constraints based on real variables?
---


Choco-solver offers the possibility to combine real variables together into an expression.
This is the easiest way to declare constraints over real variables.

## Expressions with `RealVar`

Two types of expressions can be defined with `RealVar` : arithmetic and relational.

{{% alert title="Python" color="primary" %}}
pychoco does not support real variables yet: the examples of this page are only available in Java.
{{% /alert %}}

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

Note that `y` can be either a double or an arithmetic expression.


An arithmetic expression can be turned into a `RealVar` by calling the `realVar(prec)` method on it.
Here, `prec` is the precision of the variable to return.
If necessary, it creates intermediary variable and posts intermediary constraints then returns the resulting variable.


{{% alert title="Info" color="primary" %}}

A call to this method requires [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) to be installed and configured.

{{% /alert %}}


{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "realVar" >}}
{{< /tabpane >}}


### Relational expressions

Based on an **arithmetic expression** `x`, a relational expression can be built using the following operators:
- `x.lt(y)`: states that $x < y$, 
- `x.le(y)`: states that $x \leq y$,
- `x.gt(y)`: states that $x > y$,
- `x.ge(y)`: states that $x \geq y$,
- `x.eq(y)`: states that $x = y$.

Note that `y` can be either a double or an arithmetic expression.

A relational expression can be posted into the model as an equation or added to an [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) instance.

#### As an equation

Calling `equation()` on a relation expression will return a `Constraint` object that embeds a propagator using HC4 algorithm for filtering values based on the equation expressed. It must be then posted or reified. The constraint stores the expression as an internal variable.

A call to this method **does not** create additional variables and returns a single constraint.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "equation" >}}
{{< /tabpane >}}

{{% alert title="Alert" color="secondary" %}}

The following operators are not supported by `equation()` and should be declared in [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}):
```
pow, atan2, ln, tan, acos, asin, atan, cosh, sinh, tanh, acosh, asinh, atanh 
```


{{% /alert %}}

#### Into Ibex

Alternatively, an expression can be added to [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}).
This is achieved calling the `ibex(prec)` method which returns a `Constraint` object where `prec` denotes the precision.
It must be then posted or reified.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "ibex" >}}
{{< /tabpane >}}

{{% alert title="Info" color="primary" %}}

A call to this method requires [Ibex]({{< ref "/docs/Advanced usages/Ibex.md" >}}) to be installed and configured.

{{% /alert %}}

## Other constraints

### Equality between a `RealVar` and an `IntVar`

It is sometimes relevant to map a real variable to an integer variable.
Doing so, the real variable is forced to take integer values but it can be declared in real constraints (either as an equation or in Ibex).
This is achieved by posting an `eq` constraint like this:

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "intEq" >}}
{{< /tabpane >}}

### Binding a real value from an array

It is possible to set the value of a real variable thanks to an array of double values and an integer variable as the position of the value in the array.
This relation is also known as an `element` constraint.
All double values in the array must be different and sorted in increasing order.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "element" >}}
{{< /tabpane >}}

### Scalar product

A scalar product where coefficients are double values can be defined over a set of integer variables and/or real variables.
Available operators are `"=", ">=", "<="`.

{{< tabpane langEqualsHeader=true >}}
{{< example "modeling/RealConstraints" "scalar" >}}
{{< /tabpane >}}