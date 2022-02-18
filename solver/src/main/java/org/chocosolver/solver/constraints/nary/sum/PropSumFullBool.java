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
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IntEventType;

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
public class PropSumFullBool extends PropSum {

    /**
     * Creates a sum propagator: SUM(x_i) Op b, where x_i are boolean variables.
     * Coefficients are induced by <code>pos</code>:
     * those before <code>pos</code> (included) are equal to 1,
     * the other ones are equal to -1.
     * @param variables list of boolean variables
     * @param pos position of the last positive (induced) coefficient
     * @param o operator
     * @param b bound to respect
     * @param reactOnFineEvent set to <tt>true</tt> to react on fine events
     */
    protected PropSumFullBool(BoolVar[] variables, int pos, Operator o, int b, boolean reactOnFineEvent) {
        super(variables, pos, o, b, PropagatorPriority.BINARY, reactOnFineEvent);
    }

    /**
     * Creates a sum propagator: SUM(x_i) Op b, where x_i are boolean variables.
     * Coefficients are induced by <code>pos</code>:
     * those before <code>pos</code> (included) are equal to 1,
     * the other ones are equal to -1.
     * @param variables list of boolean variables
     * @param pos position of the last positive (induced) coefficient
     * @param o operator
     * @param b bound to respect
     */
    public PropSumFullBool(BoolVar[] variables, int pos, Operator o, int b) {
        this(variables, pos, o, b, false);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INSTANTIATE.getMask();
    }

    @Override
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
        for (; i < l ; i++) { // then the negative ones
            if (vars[i].isInstantiated()) {
                k = vars[i].getLB();
                lb -= k;
                ub -= k;
            } else {
                lb--;
            }
        }
        sumLB = lb;
        sumUB = ub;
    }

    @SuppressWarnings({"NullableProblems"})
    @Override
    protected void filterOnEq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        int lb, ub, i = 0;
        if (F <= 0 || E <= 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                lb = vars[i].getLB();
                if (F <= 0 && vars[i].updateUpperBound(F + lb, this)) {
                    E++;
                }
                ub = vars[i].getUB();
                if (E <= 0 && vars[i].updateLowerBound(ub - E, this)) {
                    F++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                lb = vars[i].getUB();
                if (F <= 0 && vars[i].updateLowerBound(-F + lb, this)) {
                    E--;
                }
                ub = vars[i].getLB();
                if (E <= 0 && vars[i].updateUpperBound(ub + E, this)) {
                    F--;
                }
                i++;
            }
        }
    }


    @SuppressWarnings({"NullableProblems"})
    @Override
    protected void filterOnLeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        int lb, i = 0;
        if (F <= 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                lb = vars[i].getLB();
                if (vars[i].updateUpperBound(F + lb, this)) {
                    E++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                lb = vars[i].getUB();
                if (vars[i].updateLowerBound(-F + lb, this)) {
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
    @Override
    protected void filterOnGeq() throws ContradictionException {
        int F = b - sumLB;
        int E = sumUB - b;
        int ub, i = 0;
        // deal with sum
        if (E <= 0) { // the main reason we implemented a dedicated version
            // positive coefficients first
            while (i < pos) {
                ub = vars[i].getUB();
                if (vars[i].updateLowerBound(ub - E, this)) {
                    F++;
                }
                i++;
            }
            // then negative ones
            while (i < l) {
                ub = vars[i].getLB();
                if (vars[i].updateUpperBound(ub + E, this)) {
                    F--;
                }
                i++;
            }
        }
        if (F <= 0) {
            this.setPassive();
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
        linComb.append(" ").append(o).append(" ").append(b);
        return linComb.toString();
    }

    @Override
    protected PropSum opposite(){
        BoolVar[] bvars = new BoolVar[vars.length];
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(vars, 0, bvars, 0, bvars.length);
        return new PropSumFullBool(bvars, pos, nop(o), b + nb(o), reactToFineEvt);
    }
}
