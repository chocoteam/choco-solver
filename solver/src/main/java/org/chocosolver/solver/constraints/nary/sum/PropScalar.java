/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.clauses.ClauseBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import static org.chocosolver.solver.constraints.Operator.*;

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
public class PropScalar extends PropSum {

    /**
     * The coefficients
     */
    private final int[] c;

    /**
     * Create a scalar product: SUM(x_i*c_i) o b
     * Variables and coefficients are excepted to be ordered wrt to coefficients: first positive ones then negative ones.
     * @param variables list of integer variables
     * @param coeffs list of coefficients
     * @param pos position of the last positive coefficient
     * @param o operator
     * @param b bound to respect.
     */
    public PropScalar(IntVar[] variables, int[] coeffs, int pos, Operator o, int b) {
        super(variables, pos, o, b);
        this.c = coeffs;
    }


    @Override
    protected void prepare() {
        sumLB = sumUB = 0;
        int i = 0, lb, ub;
        maxI = 0;
        for (; i < pos; i++) { // first the positive coefficients
            lb = vars[i].getLB() * c[i];
            ub = vars[i].getUB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
        for (; i < l; i++) { // then the negative ones
            lb = vars[i].getUB() * c[i];
            ub = vars[i].getLB() * c[i];
            sumLB += lb;
            sumUB += ub;
            I[i] = (ub - lb);
            if(maxI < I[i])maxI = I[i];
        }
    }


    @Override
    protected void filterOnEq() throws ContradictionException {
        boolean anychange;
        int F = b - sumLB;
        int E = sumUB - b;
        do {
            anychange = false;
            // When explanations are on, no global failure allowed
            if (model.getSolver().isLearnOff() && F < 0 || E < 0) {
                fails();
            }
            if (maxI > F || maxI > E) {
                maxI = 0;
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
                    if(maxI < I[i])maxI = I[i];
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
                    if(maxI < I[i])maxI = I[i];
                    i++;
                }
            }
            if (F <= 0 && E <= 0) {
                this.setPassive();
                return;
            }
        } while (anychange);
    }

    @Override
    protected void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (model.getSolver().isLearnOff() &&F < 0) {
            fails();
        }
        if (maxI > F) {
            int lb, ub, i = 0;
            maxI = 0;
            // positive coefficients first
            while (i < pos) {
                maxI = 0;
                if (I[i] - F > 0) {
                    lb = vars[i].getLB() * c[i];
                    ub = lb + I[i];
                    if (vars[i].updateUpperBound(divFloor(F + lb, c[i]), this)) {
                        int nub = vars[i].getUB() * c[i];
                        E += nub - ub;
                        I[i] = nub - lb;
                    }
                }
                if(maxI < I[i])maxI = I[i];
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
                if(maxI < I[i])maxI = I[i];
                i++;
            }
        }
        if (E <= 0) {
            this.setPassive();
        }
    }

    @Override
    protected void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        // When explanations are on, no global failure allowed
        if (model.getSolver().isLearnOff() && E < 0) {
            fails();
        }
        if (maxI > E) {
            maxI = 0;
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
                if(maxI < I[i])maxI = I[i];
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
                if(maxI < I[i])maxI = I[i];
                i++;
            }
        }
        if (F <= 0) {
            this.setPassive();
        }
    }

    @Override
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
                sum += vars[i].getValue() * c[i];
            } else if (w == -1) {
                w = i;
            } else return;
        }
        if (w == -1) {
            if (sum == b) {
                this.fails();
            }
        } else if(c[w]!=0 && (b - sum)%c[w]==0){
            vars[w].removeValue((b - sum)/c[w], this);
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
        return check(sumLB, sumUB);
    }

    @Override
    void doExplain(ExplanationForSignedClause explanation, int p) {
        IntVar pivot = explanation.readVar(p);
        IntIterableRangeSet dom_before;
        // first, compute F and E
        int sumLB = 0;
        int sumUB = 0;
        int i = 0, lb, ub, la = 0, ua = 0, ca = 0, a = 0;
        for (; i < pos; i++) { // first the positive coefficients
            dom_before = explanation.readDom(vars[i]);
            lb = dom_before.min() * c[i];
            ub = dom_before.max() * c[i];
            if (vars[i] == pivot) {
                la = dom_before.min();
                ua = dom_before.max();
                ca = c[i];
                a = i;
            }
            sumLB += lb;
            sumUB += ub;
        }
        for (; i < l; i++) { // then the negative ones
            dom_before = explanation.readDom(vars[i]);
            lb = dom_before.max() * c[i];
            ub = dom_before.min() * c[i];
            if (vars[i] == pivot) {
                la = dom_before.min();
                ua = dom_before.max();
                ca = c[i];
                a = i;
            }
            sumLB += lb;
            sumUB += ub;
        }
        int F = b - sumLB;
        int E = sumUB - b;
        if (explanation.readDom(p).isEmpty()) {
            doExplainGlobalFailure(explanation, F, E);
            return;
        }
        IntIterableRangeSet domain;
        int la2 = IntIterableRangeSet.MIN, ua2 = IntIterableRangeSet.MAX;
        if (ca > 0) {
            if (!o.equals(GE)) { // ie, LE or EQ
                ua2 = divFloor(F + la * ca, ca);
            }
            if (!o.equals(LE)) { // ie, GE or EQ
                la2 = divCeil(ca * ua - E, ca);
            }
        } else {
            if (!o.equals(GE)) { // ie, LE or EQ
                la2 = divCeil(-F - ua * ca, -ca);
            }
            if (!o.equals(LE)) { // ie, GE or EQ
                ua2 = divFloor(-la * ca + E, -ca);
            }
        }
        domain = explanation.empty();
        if(la2 <= ua2){
            domain.addBetween(la2, ua2);
        }
        vars[a].intersectLit(domain, explanation);
        i = 0;
        for (; i < pos; i++) {
            int min = IntIterableRangeSet.MIN;
            int max = IntIterableRangeSet.MAX;
            if (vars[i] != pivot) {
                dom_before = explanation.readDom(vars[i]);
                if (!o.equals(GE)) { // ie, LE or EQ
                    max = divFloor(
                            F + c[i] * dom_before.min() - ca * (ca > 0 ? (ua2 + 1 - la) : (la2 - 1 - ua)),
                            c[i]);
                }
                if (!o.equals(LE)) { // ie, GE or EQ
                    min = divCeil(-E + c[i] * dom_before.max() - ca * (ca > 0 ? la2 - 1 - ua : ua2 + 1 - la), c[i]);
                }
                domain = explanation.complement(vars[i]);
                if(o.equals(EQ)) {
                    assert max+1 <= min-1 : "empty range";
                    domain.removeBetween(max + 1, min - 1);
                }else{
                    domain.retainBetween(min, max);
                }
                vars[i].unionLit(domain, explanation);
            }
        }
        for (; i < l; i++) {
            int min = IntIterableRangeSet.MIN;
            int max = IntIterableRangeSet.MAX;
            if (vars[i] != pivot) {
                dom_before = explanation.readDom(vars[i]);
                if (!o.equals(GE)) { // ie, LE or EQ
                    min = divCeil(
                            -(F + c[i] * dom_before.max() - ca * (ca > 0 ? ua2 + 1 - la : la2 - 1 - ua)), // done
                            -c[i]);
                }
                if (!o.equals(LE)) { // ie, GE or EQ
                    max = divFloor(
                            -(-E + c[i] * dom_before.min() - ca * (ca > 0 ? la2 - 1 - ua : ua2 + 1 - la)) // done
                            , -c[i]);
                }
                domain = explanation.complement(vars[i]);
                if(o.equals(EQ)) {
                    assert max+1 <= min-1 : "empty range";
                    domain.removeBetween(max + 1, min - 1);
                }else {
                    domain.retainBetween(min, max);
                }
                vars[i].unionLit(domain, explanation);
            }
        }
    }

    @Override
    protected void explainGlobal(ExplanationForSignedClause explanation, int F, int E) {
        assert (F < 0)^(E < 0);
        IntIterableRangeSet dom_before;
        IntIterableRangeSet domain;
        int i = 0;
        ClauseBuilder ngb = model.getClauseBuilder();
        for (; i < l; i++) {
            int min = IntIterableRangeSet.MIN;
            int max = IntIterableRangeSet.MAX;
            dom_before = explanation.readDom(vars[i]);
            if (F < 0) {
                // BEWARE // second part of the equation differs from non-global-fail case
                if(i < pos) {
                    max = divFloor(F + c[i] * dom_before.min(), c[i]);
                }else{
                    min = divCeil(-(F + c[i] * dom_before.max()), -c[i]);
                }
            }else /*if (E <  0)*/ {
                // BEWARE // second part of the equation differs from non-global-fail case
                if(i < pos) {
                    min = divCeil(-E + c[i] * dom_before.max(), c[i]);
                }else{
                    max = divFloor(-(-E + c[i] * dom_before.min()), -c[i]);
                }
            }
            domain = explanation.root(vars[i]);
            domain.retainBetween(min, max);
            ngb.put(vars[i], domain);
            int k = 0;
            for (; k < l; k++) {
                if (k != i) {
                    min = IntIterableRangeSet.MIN;
                    max = IntIterableRangeSet.MAX;
                    dom_before = explanation.readDom(vars[k]);
                    if (F < 0) {
                        if(k < pos) {
                            min = dom_before.min();
                        }else{
                            max = dom_before.max();
                        }
                    }else /*if (E <  0) */{
                        if(k < pos) {
                            max = dom_before.max();
                        }else{
                            min = dom_before.min();
                        }
                    }
                    domain = explanation.root(vars[k]);
                    domain.removeBetween(min, max);
                    ngb.put(vars[k], domain);
                }
            }
            ngb.buildNogood(model);
            if(E == -1 || F == -1)return; // the same nogood will be learned all the time
        }
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

    @Override
    protected PropSum opposite(){
        return new PropScalar(vars, c, pos, nop(o), b + nb(o));
    }

}
