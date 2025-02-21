/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

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
        if (lcg()) {
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
        return Reason.r(vars[i].getValLit(), j > -1 ? vars[j].getValLit() : 0);
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
}
