/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
 * A propagator for SUM(x_i) o b
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropSum extends Propagator<IntVar> {

    /**
     * The position of the last positive coefficient
     */
    protected final int pos;

    /**
     * Number of variables
     */
    protected final int l;

    /**
     * Bound to respect
     */
    protected final int b;

    /**
     * Variability of each variable (ie domain amplitude)
     */
    protected final int[] I;

    /**
     * SUm of lower bounds
     */
    protected int sumLB;

    /**
     * Sum of upper bounds
     */
    protected int sumUB;

    /**
     * The operator among EQ, LE, GE and NE
     */
    protected final Operator o;


    /**
     * Creates a sum propagator: SUM(x_i) o b
     * Coefficients are induced by <code>pos</code>:
     * those before <code>pos</code> (included) are equal to 1,
     * the other ones are equal to -1.
     *
     * @param variables list of integer variables
     * @param pos position of the last positive coefficient
     * @param o operator amng EQ, LE, GE and NE
     * @param b bound to respect
     */
    public PropSum(IntVar[] variables, int pos, Operator o, int b) {
        this(variables, pos, o, b, computePriority(variables.length), false);
    }


    PropSum(IntVar[] variables, int pos, Operator o, int b, PropagatorPriority priority, boolean reactOnFineEvent){
        super(variables, priority, reactOnFineEvent);
        this.pos = pos;
        this.o = o;
        this.b = b;
        l = variables.length;
        I = new int[l];
    }

    /**
     * Compute the priority of the propagator wrt the number of involved variables
     * @param nbvars number of variables
     * @return the priority
     */
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

    @Override
    public int getPropagationConditions(int vIdx) {
        switch (o) {
            case NQ:
                return IntEventType.instantiation();
            case LE:
                return IntEventType.combine(IntEventType.INSTANTIATE, vIdx < pos ? IntEventType.INCLOW : IntEventType.DECUPP);
            case GE:
                return IntEventType.combine(IntEventType.INSTANTIATE, vIdx < pos ? IntEventType.DECUPP : IntEventType.INCLOW);
            default:
                return IntEventType.boundAndInst();
        }
    }


    /**
     * Prepare the propagation: compute sumLB, sumUB and I
     */
    protected void prepare() {
        sumLB = sumUB = 0;
        int i = 0;
        int lb, ub;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB();
            ub = vars[i].getUB();
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
        }
        for (; i < l; i++) { // then the negative ones
            lb = -vars[i].getUB();
            ub = -vars[i].getLB();
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    /**
     * Execute filtering wrt the operator
     * @throws ContradictionException if contradiction is detected
     */
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

    /**
     * Apply filtering when operator is EQ
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnEq() throws ContradictionException {
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
                    lb = vars[i].getLB();
                    ub = lb + I[i];
                    if (vars[i].updateUpperBound(F + lb, this)) {
                        int nub = vars[i].getUB();
                        E += nub - ub;
                        I[i] = nub - lb;
                        anychange = true;
                    }
                }
                if (I[i] + E > 0) {
                    ub = vars[i].getUB();
                    lb = ub - I[i];
                    if (vars[i].updateLowerBound(ub - E, this)) {
                        int nlb = vars[i].getLB();
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
                    lb = -vars[i].getUB();
                    ub = lb + I[i];
                    if (vars[i].updateLowerBound(-F - lb, this)) {
                        int nub = -vars[i].getLB();
                        E += nub - ub;
                        I[i] = nub - lb;
                        anychange = true;
                    }
                }
                if (I[i] - E > 0) {
                    ub = -vars[i].getLB();
                    lb = ub - I[i];
                    if (vars[i].updateUpperBound(-ub + E, this)) {
                        int nlb = -vars[i].getUB();
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

    /**
     * Apply filtering when operator is LE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // positive coefficients first
        while (i < pos) {
            if (I[i] - F > 0) {
                lb = vars[i].getLB();
                ub = lb + I[i];
                if (vars[i].updateUpperBound(F + lb, this)) {
                    int nub = vars[i].getUB();
                    E += nub - ub;
                    I[i] = nub - lb;
                }
            }
            i++;
        }
        // then negative ones
        while (i < l) {
            if (I[i] - F > 0) {
                lb = -vars[i].getUB();
                ub = lb + I[i];
                if (vars[i].updateLowerBound(-F - lb, this)) {
                    int nub = -vars[i].getLB();
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

    /**
     * Apply filtering when operator is GE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (E < 0) {
            fails();
        }
        int lb, ub, i = 0;
        // positive coefficients first
        while (i < pos) {
            if (I[i] - E > 0) {
                ub = vars[i].getUB();
                lb = ub - I[i];
                if (vars[i].updateLowerBound(ub - E, this)) {
                    int nlb = vars[i].getLB();
                    F -= nlb - lb;
                    I[i] = ub - nlb;
                }
            }
            i++;
        }
        // then negative ones
        while (i < l) {
            if (I[i] - E > 0) {
                ub = -vars[i].getLB();
                lb = ub - I[i];
                if (vars[i].updateUpperBound(-ub + E, this)) {
                    int nlb = -vars[i].getUB();
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

    /**
     * Apply filtering when operator is NE
     * @throws ContradictionException if contradiction is detected
     */
    protected void filterOnNeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        if (F < 0 || E < 0) {
            setPassive();
            return;
        }
        int w = -1;
        int sum = 0;
        for (int i = 0; i < l; i++) {
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
        for (; i < l; i++) { // then the negative ones
            sumLB -= vars[i].getUB();
            sumUB -= vars[i].getLB();
        }
        return check(sumLB, sumUB);
    }

    /**
     * Whether the current state of the scalar product is entailed
     * @param sumLB sum of lower bounds
     * @param sumUB sum of upper bounds
     * @return the entailment check
     */
    protected ESat check(int sumLB, int sumUB){
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
                if (sumLB > b) {
                    return ESat.FALSE;
                }
                return ESat.UNDEFINED;
            case GE:
                if (sumLB >= b) {
                    return ESat.TRUE;
                }
                if (sumUB < b) {
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
}
