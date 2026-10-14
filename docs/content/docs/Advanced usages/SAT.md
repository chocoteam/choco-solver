---
title: "SAT"
date: 2021-04-06T14:14:31+02:00
weight: 57
math: "true"
description: >
  Use the embedded SAT solver directly.
---

Choco-solver embeds a SAT solver, which can optionally be used to handle
declared clauses in a mixed SAT and CP problem,
and which is also used to handle no-goods.
These uses do not make the underlying SAT solver directly accessible.

### A Java transposition of MiniSat
Before version 4.10.7, Choco-solver embedded only an incomplete version of a SAT solver:
only the part concerning clause propagation had been transposed.
Since version 4.10.7, the solver is complete.
It is simply a Java version of [MiniSat](http://minisat.se/MiniSat.html)
(released under the MIT licence).
For read/write reasons in the internal structures,
  this solution was preferred to one based on an additional dependency.

{{% alert title="Important" color="secondary" %}}
If you are looking to use a SAT solver, we can only recommend dedicated libraries:
[Offline_SAT_solvers](https://en.wikipedia.org/wiki/Boolean_satisfiability_problem#Offline_SAT_solvers).
{{% /alert %}}

If you still want to use this implementation, `MiniSatSolver` is the complete solver
(`MiniSat` itself is the propagation core used by Choco-solver):

{{< tabpane langEqualsHeader=true >}}
{{< example "advanced/Sat" "miniSat" >}}
{{< /tabpane >}}

Alternatively, you can parse a CNF file:

```java
MiniSatSolver solver = new MiniSatSolver();
solver.parse("/path/to/instance.cnf");
ESat ret = solver.solve();
```

Now, if you want to mix SAT and CP, having access to the SAT solver might be helpful.

### Binding CP variables to SAT variables

Facilities are given to link a `BoolVar` to its equivalent on the SAT side.
This is achieved using `satVar()` API.
Calling this method on an instance of `BoolVar` will create (or return) the
variable in the SAT solver attached to a `Solver` and return its id (an `int`).

{{< tabpane langEqualsHeader=true >}}
{{< example "advanced/Sat" "bindBool" >}}
{{< /tabpane >}}

Then, it is possible to declare a clause using these SAT variables.
However, this requires the positive (`lit(sb)`) and negative (`neg(sa)`) literals to be entered.

{{< tabpane langEqualsHeader=true >}}
{{< example "advanced/Sat" "clause" >}}
{{< /tabpane >}}

One can also want to bind an integer variable to SAT variable.
In that case, a relationship is needed; this is achieved thanks to some binary relational expression,
turned into a `BoolVar`:

{{< tabpane langEqualsHeader=true >}}
{{< example "advanced/Sat" "bindInt" >}}
{{< /tabpane >}}

This can also be achieved by using views and reifications.

#### Go off the beaten track

It is also possible to declare its own links, by implementing the `Literalizer` interface.
Doing so, it is possible to bind manually a relationship to a SAT variable:

{{< tabpane langEqualsHeader=true >}}
{{< example "advanced/Sat" "literalizer" >}}
{{< /tabpane >}}

`SetInLit` is provided by Choco-solver (`Literalizer.SetInLit`).
It is reproduced below as a model to write your own `Literalizer`:


```java
import static org.chocosolver.sat.MiniSat.sgn;
import static org.chocosolver.sat.MiniSat.var;

class SetInLit implements Literalizer {
    public final SetVar cpVar;
    public final int val;
    public int satVar;

    public SetInLit(SetVar cpVar, int val) {
        this.cpVar = cpVar;
        this.val = val;
        this.satVar = -1;
    }

    /**
     * Set the SAT variable
     */
    @Override
    public void svar(int svar) {
        if (satVar == -1) {
            this.satVar = svar;
        } else {
            throw new UnsupportedOperationException("Overriding Literalizer's satVar is forbidden");
        }
    }

    /**
     * @return this SAT variable
     */
    @Override
    public int svar() {
        return satVar;
    }

    /**
     * @return this CP variable
     */
    @Override
    public Variable cvar() {
        return cpVar;
    }

    /**                                                 
     * @return {@code true} if this will fix its literal
     */                                                 
    @Override
    public boolean canReact() {
        return cpVar.getLB().contains(val) || !cpVar.getUB().contains(val);
    }

    /**                                                     
     * Turns an event into a literal and returns it.        
     *                                                      
     * @return the literal to enqueue in SAT                
     */                                                     
    @Override
    public int toLit() {
        return MiniSat.makeLiteral(satVar, cpVar.getLB().contains(val));
    }

    /**                                                                                                      
     * Turns an event from SAT side (in the form of a literal) to an event in CP side.                       
     * Actually, it is expected that the event is directly applied, that's why a contradiction may be thrown.
     *                                                                                                       
     * @param lit   the literal                                                                              
     * @param cause cause (for CP propagation purpose)                                                       
     * @return {@code true} if the CP variable has been modified                                             
     * @throws ContradictionException if the conversion leads to a failure                                   
     */                                                                                                      
    @Override
    public boolean toEvent(int lit, ICause cause) throws ContradictionException {
        assert satVar == var(lit);
        if (sgn(lit)) {
            return cpVar.force(val, cause);
        } else {
            return cpVar.remove(val, cause);
        }
    }

    /**                                                 
     * @return {@code true} if this relationship holds
     */                                                 
    @Override
    public boolean check(boolean sign) {
        return sign ? cpVar.getLB().contains(val) : !cpVar.getUB().contains(val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetInLit)) return false;
        SetInLit setInLit = (SetInLit) o;
        return val == setInLit.val && cpVar.equals(setInLit.cpVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpVar, val);
    }
}
```
