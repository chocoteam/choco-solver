/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.constraints.propagators.nary.sum;

import common.ESat;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

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
public class PropSumEq extends Propagator<IntVar> {

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

    public PropSumEq(IntVar[] variables, int[] coeffs, int pos, int b) {
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
    public void propagate(int i, int mask) throws ContradictionException {
        filter(true, 2);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public final ESat isEntailed() {
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
