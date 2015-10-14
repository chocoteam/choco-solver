/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator for SUM(x_i*c_i) = b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropScalar extends Propagator<IntVar> {

    final int[] c; // list of coefficients
    final int pos; // index of the last positive coefficient
    final int l; // number of variables
    final int b; // bound to respect
    final int[] I; // variability of each variable -- domain amplitude
    int sumLB, sumUB; // sum of lower bounds, and sum of upper bounds
    final Operator o;


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

    public PropScalar(IntVar[] variables, int[] coeffs, int pos, Operator o, int b) {
        super(variables, computePriority(variables.length), false);
        this.c = coeffs;
        this.pos = pos;
        this.o = o;
        this.b = b;
        l = variables.length;
        I = new int[l];
        super.linkVariables();
    }

    @Override
    protected void linkVariables() {
        // do nothing, the linking is postponed because getPropagationConditions() needs some internal data
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        switch (o) {
            case NQ:
                return IntEventType.INSTANTIATE.getMask();
            case LE:
                return IntEventType.INSTANTIATE.getMask() + (vIdx < pos ? IntEventType.INCLOW.getMask() : IntEventType.DECUPP.getMask());
            case GE:
                return IntEventType.INSTANTIATE.getMask() + (vIdx < pos ? IntEventType.DECUPP.getMask() : IntEventType.INCLOW.getMask());
            default:
                return IntEventType.boundAndInst();
        }
    }


    protected void prepare() {
        sumLB = sumUB = 0;
        int i = 0, lb, ub;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB() * c[i];
            ub = vars[i].getUB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
        }
        for (; i < l; i++) { // then the negative ones
            lb = vars[i].getUB() * c[i];
            ub = vars[i].getLB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    protected void filter() throws ContradictionException {
        prepare();
        switch (o) {
            case LE:
                filterOnLeq();
                break;
            case GE:
                filterOnGeq();
                break;
            case NQ:
                filterOnNeq();
                break;
            default:
                filterOnEq();
                break;
        }
    }


    void filterOnEq() throws ContradictionException {
        boolean anychange;
        int F = b - sumLB;
        int E = sumUB - b;
        do {
            anychange = false;
            if (F < 0 || E < 0) {
                fails();
            }
            int lb, ub, i = 0;
            // positive coefficients first
            while (i < pos) {
                if (I[i] - F > 0) {
                    lb = vars[i].getLB() * c[i];
                    ub = lb + I[i];
                    if (vars[i].updateUpperBound(divFloor(F + lb, c[i]), this)) {
                        int nub = vars[i].getUB() * c[i];
                        E += nub - ub;
                        I[i] = nub - lb;
                        anychange = true;
                    }
                }
                if (I[i] - E > 0) {
                    ub = vars[i].getUB() * c[i];
                    lb = ub - I[i];
                    if (vars[i].updateLowerBound(divCeil(ub - E, c[i]), this)) {
                        int nlb = vars[i].getLB() * c[i];
                        F -= nlb - lb;
                        I[i] = ub - nlb;
                        anychange = true;
                    }
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                if (I[i] - F > 0) {
                    lb = vars[i].getUB() * c[i];
                    ub = lb + I[i];
                    if (vars[i].updateLowerBound(divCeil(-F - lb, -c[i]), this)) {
                        int nub = vars[i].getLB() * c[i];
                        E += nub - ub;
                        I[i] = nub - lb;
                        anychange = true;
                    }
                }
                if (I[i] - E > 0) {
                    ub = vars[i].getLB() * c[i];
                    lb = ub - I[i];
                    if (vars[i].updateUpperBound(divFloor(-ub + E, -c[i]), this)) {
                        int nlb = vars[i].getUB() * c[i];
                        F -= nlb - lb;
                        I[i] = ub - nlb;
                        anychange = true;
                    }
                }
                i++;
            }
        } while (anychange);
        // useless since true when all variables are instantiated
        /*if (F <= 0 && E <= 0) {
            this.setPassive();
        }*/
    }

    void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // positive coefficients first
        while (i < pos) {
            if (I[i] - F > 0) {
                lb = vars[i].getLB() * c[i];
                ub = lb + I[i];
                if (vars[i].updateUpperBound(divFloor(F + lb, c[i]), this)) {
                    int nub = vars[i].getUB() * c[i];
                    E += nub - ub;
                    I[i] = nub - lb;
                }
            }
            i++;
        }
        // then negative ones
        while (i < l) {
            if (I[i] - F > 0) {
                lb = vars[i].getUB() * c[i];
                ub = lb + I[i];
                if (vars[i].updateLowerBound(divCeil(-F - lb, -c[i]), this)) {
                    int nub = vars[i].getLB() * c[i];
                    E += nub - ub;
                    I[i] = nub - lb;
                }
            }
            i++;
        }
        if (E <= 0) {
            this.setPassive();
        }
    }

    void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (E < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // positive coefficients first
        while (i < pos) {
            if (I[i] - E > 0) {
                ub = vars[i].getUB() * c[i];
                lb = ub - I[i];
                if (vars[i].updateLowerBound(divCeil(ub - E, c[i]), this)) {
                    int nlb = vars[i].getLB() * c[i];
                    F -= nlb - lb;
                    I[i] = ub - nlb;
                }
            }
            i++;
        }
        // then negative ones
        while (i < l) {
            if (I[i] - E > 0) {
                ub = vars[i].getLB() * c[i];
                lb = ub - I[i];
                if (vars[i].updateUpperBound(divFloor(-ub + E, -c[i]), this)) {
                    int nlb = vars[i].getUB() * c[i];
                    F -= nlb - lb;
                    I[i] = ub - nlb;
                }
            }
            i++;
        }
        if (F <= 0) {
            this.setPassive();
        }
    }

    void filterOnNeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0 || E < 0) {
            setPassive();
            return;
        }
        int w = -1;
        int sum = 0;
        for (int i = 0; i < l; i++) {
            if (!vars[i].isInstantiated()) {
                sum += vars[i].getValue() * c[i];
            } else if (w == -1) {
                w = i;
            } else return;
        }
        if (w == -1) {
            if (sum == b) {
                this.fails();
            }
        } else {
            vars[w].removeValue(w < pos ? b - sum : b + sum, this);
        }
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
        switch (o) {
            case NQ:
                if (sumUB < b || sumLB > b) {
                    return ESat.TRUE;
                }
                if (sumUB == b && sumLB == b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case LE:
                if (sumUB <= b) {
                    return ESat.TRUE;
                }
                if (b < sumLB) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case GE:
                if (sumLB <= b) {
                    return ESat.TRUE;
                }
                if (b < sumUB) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            default:
                if (sumLB == b && sumUB == b) {
                    return ESat.TRUE;
                }
                if (sumUB < b || sumLB > b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
        }
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
        linComb.append(c[0]).append('.').append(vars[0].getName());
        int i = 1;
        for (; i < pos; i++) {
            linComb.append(" + ").append(c[i]).append('.').append(vars[i].getName());
        }
        for (; i < l; i++) {
            linComb.append(" - ").append(-c[i]).append('.').append(vars[i].getName());
        }
        linComb.append(" ").append(o).append(" ");
        linComb.append(b);
        return linComb.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // 1. find the pos of var in vars
        boolean ispos;
        if (pos < (l / 2)) {
            int i;
            i = 0;
            while (i < pos && vars[i] != var) {
                i++;
            }
            ispos = i < pos;
        } else {
            int i;
            i = pos;
            while (i < l && vars[i] != var) {
                i++;
            }
            ispos = i == l;
        }
        // to deal with BoolVar: any event is automatically promoted to INSTANTIATE
        if (IntEventType.isInstantiate(evt.getMask())) {
            assert var.isBool() : "BoolVar excepted";
            evt = (var.getValue() == 0 ? IntEventType.DECUPP : IntEventType.INCLOW);
        }
        if (IntEventType.isInclow(evt.getMask())) { // explain LB
            int i = 0;
            for (; i < pos; i++) { // first the positive coefficients
                if (vars[i] != var) {
                    if (ispos) {
                        newrules |= ruleStore.addUpperBoundRule(vars[i]);
                    } else {
                        newrules |= ruleStore.addLowerBoundRule(vars[i]);
                    }
                }
            }
            for (; i < l; i++) { // then the negative ones
                if (vars[i] != var) {
                    if (ispos) {
                        newrules |= ruleStore.addLowerBoundRule(vars[i]);
                    } else {
                        newrules |= ruleStore.addUpperBoundRule(vars[i]);
                    }
                }
            }
        } else if (IntEventType.isDecupp(evt.getMask())) { // explain UB
            int i = 0;
            for (; i < pos; i++) { // first the positive coefficients
                if (vars[i] != var) {
                    if (ispos) {
                        newrules |= ruleStore.addLowerBoundRule(vars[i]);
                    } else {
                        newrules |= ruleStore.addUpperBoundRule(vars[i]);
                    }
                }
            }
            for (; i < l; i++) { // then the negative ones
                if (vars[i] != var) {
                    if (ispos) {
                        newrules |= ruleStore.addUpperBoundRule(vars[i]);
                    } else {
                        newrules |= ruleStore.addLowerBoundRule(vars[i]);
                    }
                }
            }
        } else {
            for (int i = 0; i < vars.length; i++) {
                newrules |= ruleStore.addFullDomainRule(vars[i]);
            }
        }
        return newrules;
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
