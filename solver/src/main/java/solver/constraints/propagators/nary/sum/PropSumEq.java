/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.sum;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.io.Serializable;

/**
 * A propagator for SUM(x_i) <= b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p/>
 * /!\ : thanks to views and pre-treatment, coefficients are merge into variable
 *
 * @author Charles Prud'homme
 * @revision add Interval to avoid numerous calls to getLB() and getUB()
 * @since 18/03/11
 */
public class PropSumEq extends Propagator<IntVar> {


    final IntVar[] x; // list of variable
    final int l; // number of variables
    final int b; // bound to respect
    int sumLB, sumUB; // sum of lower bounds, and sum of upper bounds
    Interval[] intervals;


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

    public PropSumEq(IntVar[] vars, int b,
                     Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, solver, intVarPropagatorConstraint, computePriority(vars.length), false);
        this.x = vars.clone();
        l = x.length;
        this.b = b;
        intervals = new Interval[l];
        IntVar vt;
        for (int i = 0; i < l; i++) {
            vt = x[i];
            intervals[i] = new Interval(vt, i);
        }
    }

    protected void prepare() {
        int f = 0, e = 0, i = 0;
        for (; i < l; i++) {
            intervals[i].update();
            f += intervals[i].lb;
            e += intervals[i].ub;
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


    boolean filterOnLeq() throws ContradictionException {
        boolean doIt;
        boolean anychange = false;
        if (b - sumLB < 0) {
            this.contradiction(null, "b - sumLB < 0");
        }
        do {
            doIt = false;
            int lb, nub, i = 0;
            for (; i < l; i++) {
                if (intervals[i].card - (b - sumLB) > 0) {
                    lb = intervals[i].lb;
                    nub = b - sumLB + lb;
                    if (x[i].updateUpperBound(nub, this)) {
                        intervals[i].updateUB();
                        sumUB -= intervals[i].ub - nub;
                        intervals[i].card = nub - lb;
                        anychange = doIt = true;
                    }
                }
            }
        } while (doIt);
        return anychange;
    }

    boolean filterOnGeq() throws ContradictionException {
        boolean doIt;
        boolean anychange = false;
        if (b - sumUB > 0) {
            this.contradiction(null, "b - sumUB > 0");
        }
        do {
            doIt = false;
            int ub, nlb, i = 0;
            for (; i < l; i++) {
                if (intervals[i].card > -(b - sumUB)) {
                    ub = intervals[i].ub;
                    nlb = b - sumUB + ub;
                    if (x[i].updateLowerBound(nlb, this)) {
                        intervals[i].updateLB();
                        sumLB += nlb - intervals[i].lb;
                        intervals[i].card = ub - nlb;
                        doIt = anychange = true;
                    }
                }
            }
        } while (doIt);
        return anychange;
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int i, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
            filter(true, 2);
        } else if (EventType.isInclow(mask)) {
            filter(true, 1);
        } else if (EventType.isDecupp(mask)) {
            filter(false, 1);
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
            sumLB += x[i].getLB();
            sumUB += x[i].getUB();
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

    private static class Interval implements Serializable {
        IntVar var;
        int idx;
        int lb, ub;
        int card;

        private Interval(IntVar var, int idx) {
            this.var = var;
            this.idx = idx;
        }

        protected void update() {
            this.lb = var.getLB();
            this.ub = var.getUB();
            this.card = ub - lb;
        }

        protected void updateLB() {
            int t = this.lb;
            this.lb = var.getLB();
            card -= (lb - t);
        }

        protected void updateUB() {
            int t = this.ub;
            this.ub = var.getUB();
            card -= (t - ub);
        }
    }
}
