/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.lp.ILinearRelaxation;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.Arrays;

/**
 * Propagator for AllDifferent that only reacts on instantiation
 *
 * @author Charles Prud'homme
 */
@Explained
public class PropAllDiffInst extends Propagator<IntVar> {

    protected static class FastResetArrayStack extends TIntArrayStack {
        void resetQuick() {
            this._list.resetQuick();
        }
    }

    protected final int n;
    protected FastResetArrayStack toCheck = new FastResetArrayStack();

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables array of integer variables
     */
    public PropAllDiffInst(IntVar[] variables) {
        super(variables, PropagatorPriority.UNARY, true);
        n = vars.length;
        if (lcg() && Arrays.stream(variables).allMatch(IntVar::hasEnumeratedDomain)) {
            IntIterableRangeSet set = new IntIterableRangeSet();
            for (IntVar var : vars) {
                set.addAll(var);
            }
            if (set.size() == vars.length) { // there are no spare values
                TIntArrayList ps = new TIntArrayList();
                for (int v : set) {
                    for (int i = 0; i < vars.length; i++) {
                        ps.add(vars[i].getLit(v, IntVar.LR_EQ));
                    }
                    getModel().getSolver().getSat().addClause(ps);
                    ps.resetQuick();
                }
            }
        }
    }


    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.instantiation();
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
//        toCheck.clear();
        toCheck.resetQuick();
        for (int v = 0; v < n; v++) {
            if (vars[v].isInstantiated()) {
                toCheck.push(v);
            }
        }
        fixpoint();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
//        toCheck.clear();
        toCheck.resetQuick();
        toCheck.push(varIdx);
        fixpoint();
    }

    protected void fixpoint() throws ContradictionException {
        while (toCheck.size() > 0) {
            int vidx = toCheck.pop();
            int val = vars[vidx].getValue();
            for (int i = 0; i < n; i++) {
                if (i != vidx) {
                    if (!lcg()) {
                        if (vars[i].removeValue(val, this) && vars[i].isInstantiated()) {
                            toCheck.push(i);
                        }
                    } else {
                        if (vars[i].isInstantiatedTo(val)) {
                            this.fails(explain(i, vidx));
                        }
                        if (vars[i].removeValue(val, this, explain(vidx, -1))
                                && vars[i].isInstantiated()) {
                            toCheck.push(i);
                        }
                    }
                }
            }
        }
    }

    private Reason explain(int i, int j) {
        return this.r(vars[i].getValLit(), j > -1 ? vars[j].getValLit() : 0);
    }


    @Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                nbInst++;
                for (int j = i + 1; j < n; j++) {
                    if (vars[j].isInstantiatedTo(vars[i].getValue())) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    /**
     * Linear relaxation of AllDifferent based on sum bounds.
     * <p>
     * If x_1, x_2, ..., x_n are all different, then:
     * <ul>
     *   <li>Σ x_i ≥ sum of the n smallest distinct values reachable (lower bound)</li>
     *   <li>Σ x_i ≤ sum of the n largest distinct values reachable (upper bound)</li>
     * </ul>
     * These are encoded as two ≤ inequalities in the LP matrix.
     */
    @Override
    public int addLinearExpression(ILinearRelaxation lp, TObjectIntHashMap<IntVar> vmap) {
        // Compute the smallest n distinct values reachable by the variables.
        // Sort lower bounds; then greedily pick: each variable must take at least max(lb_i, prev+1).
        int[] lbs = new int[n];
        int[] ubs = new int[n];
        for (int i = 0; i < n; i++) {
            lbs[i] = vars[i].getLB();
            ubs[i] = vars[i].getUB();
        }
        Arrays.sort(lbs);
        Arrays.sort(ubs);

        // Lower bound: Σ xi >= sumMin
        // Each variable, sorted by LB ascending, takes at least max(lb_i, prev + 1)
        long sumMin = lbs[0];
        for (int i = 1; i < n; i++) {
            lbs[i] = Math.max(lbs[i], lbs[i - 1] + 1);
            sumMin += lbs[i];
        }

        // Upper bound: Σ xi <= sumMax
        // Each variable, sorted by UB descending, takes at most min(ub_i, prev - 1)
        long sumMax = ubs[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            ubs[i] = Math.min(ubs[i], ubs[i + 1] - 1);
            sumMax += ubs[i];
        }

        // Row 1:  -x_1 - x_2 - ... - x_n <= -sumMin  (i.e. Σ xi >= sumMin)
        ILinearRelaxation.RowMutator row = lp.addInequalityConstraint((double) -sumMin);
        for (int i = 0; i < n; i++) {
            row.add(vmap.get(vars[i]), -1.0);
        }

        // Row 2:  x_1 + x_2 + ... + x_n <= sumMax
        row = lp.addInequalityConstraint((double) sumMax);
        for (int i = 0; i < n; i++) {
            row.add(vmap.get(vars[i]), 1.0);
        }

        return 2;
    }
}
