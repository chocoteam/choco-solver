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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;

/**
 * A propagator for SUM(x_i) = y + b, where x_i are boolean variables, maintained incrementally.
 * <br/>
 * Based on "Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class PropSumBoolIncr extends PropSumBool {

    /**
     * Sum of lower bounds maintained incrementally.
     * Main reason this version exists.
     */
    private final IStateInt bLB;
    /**
     * Sum of upper bounds maintained incrementally.
     * Main reason this version exists.
     */
    private final IStateInt bUB;

    /**
     * The filtering algorithm is triggered on some particular events.
     * This boolean indicates when the propagation should be executed.
     *
     */
    private boolean doFilter;

    /**
     * Creates a sum propagator: SUM(x_i) Op sum + b, where x_i are boolean variables, maintained incrementally.
     * Coefficients are induced by <code>pos</code>:
     * those before <code>pos</code> (included) are equal to 1,
     * the other ones are equal to -1.
     * @param variables list of boolean variables
     * @param pos position of the last positive (induced) coefficient
     * @param o operator
     * @param sum resulting variable
     * @param b bound to respect
     */
    public PropSumBoolIncr(BoolVar[] variables, int pos, Operator o, IntVar sum, int b) {
        super(variables, pos, o, sum, b, true);
        this.bLB = model.getEnvironment().makeInt();
        this.bUB = model.getEnvironment().makeInt();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
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
            for (; i < l -1; i++) { // then the negative ones
                if (vars[i].isInstantiated()) {
                    k = vars[i].getLB();
                    lb -= k;
                    ub -= k;
                } else {
                    lb--;
                }
            }
            bLB.set(lb);
            bUB.set(ub);
        }
        doFilter = false;
        filter();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < pos) {
            int k = vars[idxVarInProp].getLB();
            if (k == 1) {
                bLB.add(1);
                doFilter |= o != Operator.GE;
            } else {
                bUB.add(-1);
                doFilter |= o != Operator.LE;
            }
        } else if (idxVarInProp < l -1) {
            int k = vars[idxVarInProp].getLB();
            if (k == 0) {
                bLB.add(1);
                doFilter |= o != Operator.GE;
            } else {
                bUB.add(-1);
                doFilter |= o != Operator.LE;
            }
        } else {
            doFilter = true;
        }
        if (doFilter) {
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        }
    }

    @Override
    protected void prepare() {
        sumLB = bLB.get() - sum.getUB();
        sumUB = bUB.get() - sum.getLB();
    }

    @Override
    protected PropSum opposite(){
        BoolVar[] bvars = new BoolVar[vars.length-1];
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(vars, 0, bvars, 0, bvars.length);
        return new PropSumBoolIncr(bvars, pos, nop(o), vars[vars.length-1], b + nb(o));
    }

}
