/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.solver.constraints.nary.sum;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ValueRemoval;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator for SUM(x_i) = b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 use I in filterOn{G,L}eg
 * @since 18/03/11
 */
public class PropScalarEq extends Propagator<IntVar> {

    final int[] c; // list of coefficients
    final int pos; // index of the last positive coefficient
    final int l; // number of variables
    final int b; // bound to respect
    final int[] I; // variability of each variable -- domain amplitude
    int sumLB, sumUB; // sum of lower bounds, and sum of upper bounds


    protected static PropagatorPriority computePriority(int nbvars) {
        if (nbvars == 1) {
            return PropagatorPriority.UNARY;
        } else if (nbvars == 2) {
            return PropagatorPriority.BINARY;
        } else if (nbvars == 3) {
            return PropagatorPriority.TERNARY;
        } else {
            return PropagatorPriority.LINEAR;
        }
    }

    public PropScalarEq(IntVar[] variables, int[] coeffs, int pos, int b) {
        super(variables, computePriority(variables.length), false);
        this.c = coeffs;
        this.pos = pos;
        l = variables.length;
        this.b = b;
        I = new int[l];
    }

    protected void prepare() {
        int f = 0, e = 0, i = 0;
        int lb, ub;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB() * c[i];
            ub = vars[i].getUB() * c[i];
            f += lb;
            e += ub;
            I[i] = (ub - lb);
        }
        for (; i < l; i++) { // then the negative ones
            lb = vars[i].getUB() * c[i];
            ub = vars[i].getLB() * c[i];
            f += lb;
            e += ub;
            I[i] = (ub - lb);
        }
        sumLB = f;
        sumUB = e;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter(true, 2);
    }

    protected void filter(boolean startWithLeq, int nbRules) throws ContradictionException {
        prepare();
        boolean run;
        int nbR = 0;
        do {
            if (startWithLeq) {
                run = filterOnLeq();
            } else {
                run = filterOnGeq();
            }
            startWithLeq ^= true;
            nbR++;
        } while (run || nbR < nbRules);
        checkEntailment();
    }

    protected void checkEntailment() {
        if (sumUB - b <= 0 && sumLB - b >= 0) {
            this.setPassive();
        }
    }


    @SuppressWarnings({"NullableProblems"})
    boolean filterOnLeq() throws ContradictionException {
        boolean anychange = false;
        if (b - sumLB < 0) {
            this.contradiction(null, "b - sumLB < 0");
        }
        int lb, ub, i = 0;
        // positive coefficients first
        for (; i < pos; i++) {
            if (I[i] - (b - sumLB) > 0) {
                lb = vars[i].getLB() * c[i];
                ub = lb + I[i];
                if (vars[i].updateUpperBound(divFloor(b - sumLB + lb, c[i]), aCause)) {
                    int nub = vars[i].getUB() * c[i];
                    sumUB -= ub - nub;
                    I[i] = nub - lb;
                    anychange = true;
                }
            }
        }
        // then negative ones
        for (; i < l; i++) {
            if (I[i] - (b - sumLB) > 0) {
                lb = vars[i].getUB() * c[i];
                ub = lb + I[i];
                if (vars[i].updateLowerBound(divCeil(-(b - sumLB + lb), -c[i]), aCause)) {
                    int nub = vars[i].getLB() * c[i];
                    sumUB -= ub - nub;
                    I[i] = nub - lb;
                    anychange = true;
                }
            }
        }
        return anychange;
    }

    @SuppressWarnings({"NullableProblems"})
    boolean filterOnGeq() throws ContradictionException {
        boolean anychange = false;
        if (b - sumUB > 0) {
            this.contradiction(null, "b - sumUB > 0");
        }
        int lb, ub, i = 0;
        // positive coefficients first
        for (; i < pos; i++) {
            if (I[i] > -(b - sumUB)) {
                ub = vars[i].getUB() * c[i];
                lb = ub - I[i];
                if (vars[i].updateLowerBound(divCeil(b - sumUB + ub, c[i]), aCause)) {
                    int nlb = vars[i].getLB() * c[i];
                    sumLB += nlb - lb;
                    I[i] = ub - nlb;
                    anychange = true;
                }
            }
        }
        // then negative ones
        for (; i < l; i++) {
            if (I[i] > -(b - sumUB)) {
                ub = vars[i].getLB() * c[i];
                lb = ub - I[i];
                if (vars[i].updateUpperBound(divFloor(-(b - sumUB + ub), -c[i]), aCause)) {
                    int nlb = vars[i].getUB() * c[i];
                    sumLB += nlb - lb;
                    I[i] = ub - nlb;
                    anychange = true;
                }
            }
        }
        return anychange;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i = 0;
        for (; i < pos; i++) { // first the positive coefficients
            sumLB += vars[i].getLB() * c[i];
            sumUB += vars[i].getUB() * c[i];
        }
        for (; i < l; i++) { // then the negative ones
            sumLB += vars[i].getUB() * c[i];
            sumUB += vars[i].getLB() * c[i];
        }
        return compare(sumLB, sumUB);
    }

    protected ESat compare(int sumLB, int sumUB) {
        if (sumUB == b && sumLB == b) {
            return ESat.TRUE;
        } else if (sumLB > b || sumUB < b) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(vars[0].getName()).append('.').append(c[0]);
        int i = 1;
        for (; i < l; i++) {
            linComb.append(" + ").append(vars[i].getName()).append('.').append(c[i]);
        }
        linComb.append(" = ");
        linComb.append(b);
        return linComb.toString();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(solver.getExplainer().getPropagatorActivation(this));
        e.add(this);
        if (d != null && d.getmType() == Deduction.Type.ValRem) {
            ValueRemoval vr = (ValueRemoval) d;
            IntVar var = (IntVar) vr.getVar();
            int val = vr.getVal();
            // 1. find the pos of var in vars
            boolean ispos;
            if (pos < (l / 2)) {
                int i;
                for (i = 0; i < pos && vars[i].getId() != var.getId(); i++) {
                }
                ispos = i < pos;
            } else {
                int i;
                for (i = pos; i < l && vars[i].getId() != var.getId(); i++) {
                }
                ispos = i == l;
            }

            if (val < var.getLB()) { // explain LB
                int i = 0;
                for (; i < pos; i++) { // first the positive coefficients
                    if (vars[i] != var) {
                        vars[i].explain(ispos ? VariableState.UB : VariableState.LB, e);
                    }
                }
                for (; i < l; i++) { // then the negative ones
                    if (vars[i] != var) {
                        vars[i].explain(ispos ? VariableState.LB : VariableState.UB, e);
                    }
                }
            } else if (val > var.getUB()) { // explain UB
                int i = 0;
                for (; i < pos; i++) { // first the positive coefficients
                    if (vars[i] != var) {
                        vars[i].explain(ispos ? VariableState.LB : VariableState.UB, e);
                    }
                }
                for (; i < l; i++) { // then the negative ones
                    if (vars[i] != var) {
                        vars[i].explain(ispos ? VariableState.UB : VariableState.LB, e);
                    }
                }
            } else {
                super.explain(d, e);
            }

        } else {
            super.explain(d, e);
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropScalarEq(aVars, this.c, this.pos, this.b));
        }
    }

    private int divFloor(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return (a / b);
        } else {
            return (a - b + 1) / b;
        }
    }

    private int divCeil(int a, int b) {
        // <!> we assume b > 0
        if (a >= 0) {
            return ((a + b - 1) / b);
        } else {
            return a / b;
        }
    }

}
