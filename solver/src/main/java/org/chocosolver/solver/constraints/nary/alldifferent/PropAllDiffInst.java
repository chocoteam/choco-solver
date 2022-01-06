/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import gnu.trove.stack.array.TIntArrayStack;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.stream.IntStream;

/**
 * Propagator for AllDifferent that only reacts on instantiation
 *
 * @author Charles Prud'homme
 */
public class PropAllDiffInst extends Propagator<IntVar> {

    protected static class FastResetArrayStack extends TIntArrayStack{
        void resetQuick(){
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
                    if (vars[i].removeValue(val, this) && vars[i].isInstantiated()) {
                        toCheck.push(i);
                    }
                }
            }
        }
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
     * Find in the implication graph and add in the explanation one instantiation event to the value t
     * @param t value
     */
    private void explainEqualExistit(ExplanationForSignedClause e, int[] indexes, int t){
        for (int i : indexes)  {
            if (vars[i].isInstantiatedTo(t)){
                vars[i].unionLit(e.complement(vars[i]), e);
                break;
            }
        }
    }
    /**
     * @implSpec
     * This version of alldiff algo only reacts on instantiation.
     * So, the explaining algorithm should also be basic, and only infer on instantiation.
     * <p>
     *     First, from Dx and Dx', resp. the domain of x before and after propagation, deduce the values removed.
     *     Then, scan other variables and store those that intersect the set of values.
     *     Fill the clause with stored variables only.
     * </p>
     * <p>
     *     Optionally, since this propagator can filter multiple variables in one loop, a good approach is to
     *     go back in the implication graph as much as possible.
     * </p>
     *
     */
    @Override
    public void explain(int p,ExplanationForSignedClause e) {
        IntVar pivot = e.readVar(p);
        int[] X = IntStream.rangeClosed(0, vars.length - 1).filter(i->vars[i]!=pivot).toArray();
        switch (e.readMask(p)) {
            case 1://REMOVE
                IntIterableRangeSet dbef = e.domain(pivot);
                dbef.removeAll(e.readDom(p));
                int t = dbef.min();
                explainEqualExistit(e, X, t);
                IntIterableRangeSet set = e.universe();
                set.remove(t);
                pivot.intersectLit(set, e);
                break;
            case 2://INCLOW
            case 4://DECUPP
            case 8://INSTANTIATE
            case 0://VOID
            case 6://BOUND inclow+decup
            default:
                throw new UnsupportedOperationException("Unknown event type for explanation");
        }
    }
}
