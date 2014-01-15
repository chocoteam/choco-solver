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

package solver.deprecatedPropagators;

import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * A propagator for SUM(x_i) <= b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p/>
 * /!\ : thanks to views and pre-treatment, coefficients are merge into variable
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 use I in filterOn{G,L}eg
 * @since 18/03/11
 * @deprecated since 09/05/2012, not maintained
 */
public class PropSumEqIncr extends Propagator<IntVar> {

    final IStateInt[] oldx;
    final IStateInt[] I; // variability of each variable -- domain amplitude
    final int l; // number of variables
    final int b; // bound to respect
    final IStateInt sumLB, sumUB; // sum of lower bounds, and sum of upper bounds
    final IStateInt idxMaxI;
    int sumLB_, sumUB_;


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

    public PropSumEqIncr(IntVar[] variables, int b) {
        super(variables, computePriority(variables.length), true);
        l = vars.length;
        this.b = b;
        I = new IStateInt[l];
        oldx = new IStateInt[l];
        for (int i = 0; i < l; i++) {
            oldx[i] = environment.makeInt();
            I[i] = environment.makeInt();
        }
        sumLB = environment.makeInt();
        sumUB = environment.makeInt();
        idxMaxI = environment.makeInt();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            // initialize
            int f = 0, e = 0, i = 0;
            int max = Integer.MIN_VALUE;
            int idx = -1;
            int lb, ub;
            for (; i < l; i++) {
                lb = vars[i].getLB();
                oldx[i].set(lb);
                ub = vars[i].getUB();
                f += lb;
                e += ub;
                I[i].set(ub - lb);
                if (max < (ub - lb)) {
                    max = ub - lb;
                    idx = i;
                }
            }
            sumLB.set(f);
            sumUB.set(e);
            idxMaxI.set(idx);
        }
        filter(true, 2);
    }

    protected void filter(boolean startWithLeq, int nbRules) throws ContradictionException {
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
        if (sumUB.get() - b <= 0 && sumLB.get() - b >= 0) {
            this.setPassive();
        }
    }


    @SuppressWarnings({"NullableProblems"})
    boolean filterOnLeq() throws ContradictionException {
        boolean doIt;
        boolean anychange = false;
        sumLB_ = sumLB.get();
        if (b - sumLB_ < 0) {
            this.contradiction(null, "b - sumLB < 0");
        }
        do {
            doIt = false;
            int lb, ub, i = 0;
            // positive coefficients first
            for (; i < l; i++) {
                int Ii = I[i].get();
                if (Ii - (b - sumLB_) > 0) {
                    lb = oldx[i].get();
                    ub = lb + Ii;
                    if (vars[i].updateUpperBound(b - sumLB_ + lb, aCause)) {
                        int nub = vars[i].getUB();
                        sumUB.add(nub - ub);
                        I[i].set(nub - lb);
                        if (idxMaxI.get() == i) {
                            findMax();
                        }
                        anychange = doIt = true;
                    }
                }
            }
        } while (doIt);
        return anychange;
    }

    @SuppressWarnings({"NullableProblems"})
    boolean filterOnGeq() throws ContradictionException {
        boolean doIt;
        boolean anychange = false;
        sumUB_ = sumUB.get();
        if (b - sumUB_ > 0) {
            this.contradiction(null, "b - sumUB > 0");
        }
        do {
            doIt = false;
            int lb, ub, i = 0;
            // positive coefficients first
            for (; i < l; i++) {
                int Ii = I[i].get();
                if (Ii > -(b - sumUB_)) {
                    lb = oldx[i].get();
                    ub = lb + Ii;
                    if (vars[i].updateLowerBound(b - sumUB_ + ub, aCause)) {
                        int nlb = vars[i].getLB();
                        sumLB.add(nlb - lb);
                        I[i].set(ub - nlb);
                        oldx[i].set(nlb);
                        if (idxMaxI.get() == i) {
                            findMax();
                        }
                        doIt = anychange = true;
                    }
                }
            }
        } while (doIt);
        return anychange;
    }

    private void findMax() {
        int id = 0;
        int iM = I[id].get();
        for (int i = 1; i < l; i++) {
            if (iM < I[i].get()) {
                iM = I[i].get();
                id = i;
            }
        }
        idxMaxI.set(id);
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        int lb = vars[i].getLB();
        int ub = vars[i].getUB();
        int olb = oldx[i].get();
        int Ii = I[i].get();
        int oub = olb + Ii;
        sumLB.add(lb - olb);
        sumUB.add(ub - oub);
        oldx[i].set(lb);
        I[i].set(ub - lb);
        if (idxMaxI.get() == i) {
            findMax();
        }

        int max = I[idxMaxI.get()].get();
        if ((b - sumLB.get()) < max || (sumUB.get() - b) > -max) {
            int nbR = 1;
            boolean swl = true;
            if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
                nbR++;
            }
            if (EventType.isDecupp(mask)) {
                swl = false;
            }
            filter(swl, nbR);
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i = 0;
        for (; i < l; i++) {
            sumLB += vars[i].getLB();
            sumUB += vars[i].getUB();
        }
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
        linComb.append(vars[0].getName());
        int i = 1;
        for (; i < l; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        linComb.append(" = ");
        linComb.append(b);
        return linComb.toString();
    }
}
