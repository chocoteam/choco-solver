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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A propagator for SUM(x_i) = y + b, where x_i are boolean variables
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropSumBool extends Propagator<IntVar> {

    final int pos; // index of the last positive coefficient
    final int l; // number of variables
    final IntVar sum;
    final int b; // bound to respect
    int sumLB, sumUB; // sum of lower bounds, and sum of upper bounds
    final Operator o;


    protected PropSumBool(BoolVar[] variables, int pos, Operator o, IntVar sum, int b, boolean reactOnFineEvent) {
        super(ArrayUtils.append(variables, new IntVar[]{sum}), PropagatorPriority.BINARY, reactOnFineEvent);
        this.pos = pos;
        this.o = o;
        this.b = b;
        this.sum = sum;
        l = variables.length;
        super.linkVariables();
    }

    public PropSumBool(BoolVar[] variables, int pos, Operator o, IntVar sum, int b) {
        this(variables, pos, o, sum, b, false);
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
                return IntEventType.INSTANTIATE.getMask() + (vIdx == l ? IntEventType.DECUPP.getMask() : 0);
            case GE:
                return IntEventType.INSTANTIATE.getMask() + (vIdx == l ? IntEventType.INCLOW.getMask() : 0);
            default:
                return IntEventType.boundAndInst();
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    protected void prepare() {
        int i = 0, k;
        int lb = 0, ub = 0;
        for (; i < pos; i++) { // first the positive coefficients
            if (vars[i].isInstantiated()) {
                k = vars[i].getLB();
                lb += k;
                ub += k;
            } else {
                ub++;
            }
        }
        for (; i < l; i++) { // then the negative ones
            if (vars[i].isInstantiated()) {
                k = vars[i].getLB();
                lb -= k;
                ub -= k;
            } else {
                lb--;
            }
        }
        sumLB = lb - sum.getUB();
        sumUB = ub - sum.getLB();
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

    @SuppressWarnings({"NullableProblems"})
    void filterOnEq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0 || E < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // deal with sum
        lb = -sum.getUB();
        ub = -sum.getLB();
        if (sum.updateLowerBound(-F - lb, this)) {
            int nub = -sum.getLB();
            E += nub - ub;
            ub = nub;
        }
        if (sum.updateUpperBound(-ub + E, this)) {
            int nlb = -sum.getUB();
            F -= nlb - lb;
        }
        if (F == 0 || E == 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                if (F == 0 && !vars[i].isInstantiated() && vars[i].instantiateTo(0, this)) {
                    E++;
                }
                if (E == 0 && !vars[i].isInstantiated() && vars[i].instantiateTo(1, this)) {
                    F++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                if (F == 0 && !vars[i].isInstantiated() && vars[i].instantiateTo(1, this)) {
                    E--;
                }
                if (E == 0 && !vars[i].isInstantiated() && vars[i].instantiateTo(0, this)) {
                    F--;
                }
                i++;
            }
        }
    }


    @SuppressWarnings({"NullableProblems"})
    void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // deal with sum
        lb = -sum.getUB();
        ub = -sum.getLB();
        if (sum.updateLowerBound(-F - lb, this)) {
            int nub = -sum.getLB();
            E += nub - ub;
        }
        if (F == 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                if (!vars[i].isInstantiated() && vars[i].instantiateTo(0, this)) {
                    E++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                if (!vars[i].isInstantiated() && vars[i].instantiateTo(1, this)) {
                    E--;
                }
                i++;
            }
        }
        if (E <= 0) {
            this.setPassive();
        }
    }

    @SuppressWarnings({"NullableProblems"})
    void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (E < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // deal with sum
        lb = -sum.getUB();
        ub = -sum.getLB();
        if (sum.updateUpperBound(-ub + E, this)) {
            int nlb = -sum.getUB();
            F -= nlb - lb;
        }
        if (E == 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                if (!vars[i].isInstantiated() && vars[i].instantiateTo(1, this)) {
                    F++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                if (!vars[i].isInstantiated() && vars[i].instantiateTo(0, this)) {
                    F--;
                }
                i++;
            }
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
        for (int i = 0; i < l + 1; i++) {
            if (vars[i].isInstantiated()) {
                sum += i < pos ? vars[i].getValue() : -vars[i].getValue();
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
            sumLB += vars[i].getLB();
            sumUB += vars[i].getUB();
        }
        for (; i < l + 1; i++) { // then the negative ones
            sumLB -= vars[i].getUB();
            sumUB -= vars[i].getLB();
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

    @Override
    public String toString() {
        StringBuilder linComb = new StringBuilder(20);
        linComb.append(pos == 0 ? "-" : "").append(vars[0].getName());
        int i = 1;
        for (; i < pos; i++) {
            linComb.append(" + ").append(vars[i].getName());
        }
        for (; i < l; i++) {
            linComb.append(" - ").append(vars[i].getName());
        }
        linComb.append(" ").append(o).append(" ");
        linComb.append(vars[i].getName()).append(" ").append(b < 0 ? "- " : "+ ").append(Math.abs(b));
        return linComb.toString();
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        // 1. find the pos of var in vars
        boolean ispos;
        if (pos < ((l + 1) / 2)) {
            int i;
            i = 0;
            while (i < pos && vars[i] != var) {
                i++;
            }
            ispos = i < pos;
        } else {
            int i;
            i = pos;
            while (i < l + 1 && vars[i] != var) {
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
            for (; i < l + 1; i++) { // then the negative ones
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
            for (; i < l + 1; i++) { // then the negative ones
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
}
