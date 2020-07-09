/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

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
    public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications ig, int p) {
        IntVar pivot = ig.getIntVarAt(p);
        IntIterableRangeSet dbef = explanation.getSet(pivot);
        dbef.removeAll(ig.getDomainAt(p));
        assert dbef.size() == 1;
        for(int i = 0; i < vars.length; i++){
            if(vars[i].isInstantiatedTo(dbef.min()) && vars[i]!= pivot){
                IntIterableRangeSet set = explanation.getRootSet(vars[i]);
                set.remove(dbef.min());
                vars[i].joinWith(set, explanation);
                break;
            }
        }
        IntIterableRangeSet set = explanation.getRootSet(pivot);
        set.removeAll(dbef);
        pivot.crossWith(set, explanation);
        explanation.returnSet(dbef);
    }

}
