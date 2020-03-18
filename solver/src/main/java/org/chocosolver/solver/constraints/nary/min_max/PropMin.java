/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import static org.chocosolver.solver.constraints.PropagatorPriority.LINEAR;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropMin extends Propagator<IntVar> {

    private final int n;

    public PropMin(IntVar[] variables, IntVar maxVar) {
        super(concat(variables, maxVar), LINEAR, false);
        n = variables.length;
        assert n > 0;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean filter;
        do {
            filter = false;
            int lb = Integer.MAX_VALUE;
            int ub = Integer.MAX_VALUE;
            int min = vars[n].getLB();
            // update min
            for (int i = 0; i < n; i++) {
                filter |= vars[i].updateLowerBound(min, this);
                lb = Math.min(lb, vars[i].getLB());
                ub = Math.min(ub, vars[i].getUB());
            }
            filter |= vars[n].updateLowerBound(lb, this);
            filter |= vars[n].updateUpperBound(ub, this);
            ub = Math.min(ub, vars[n].getUB()); // to deal with holes in vars[n] or its instantiation
            // back-propagation
            int c = 0, idx = -1;
            for (int i = 0; i < n; i++) {
                if (vars[i].getLB() > ub) {
                    c++;
                } else {
                    idx = i;
                }
            }
            if (c == vars.length - 2) {
                filter = false;
                vars[idx].updateBounds(vars[n].getLB(), vars[n].getUB(), this);
                if (vars[n].isInstantiated()) {
                    setPassive();
                } else if (vars[idx].hasEnumeratedDomain()) {
                    // for enumerated variables only
                    while (vars[n].getLB() != vars[idx].getLB()
                            || vars[n].getUB() != vars[idx].getUB()) {
                        vars[n].updateBounds(vars[idx].getLB(), vars[idx].getUB(), this);
                        vars[idx].updateBounds(vars[n].getLB(), vars[n].getUB(), this);
                    }
                }
            }
        } while (filter);
    }

    @Override
    public ESat isEntailed() {
        int lb = vars[n].getLB();
        int minLb = vars[0].getLB();
        for (int i = 0; i < n; i++) {
            minLb = Math.min(minLb,vars[i].getLB());
            if (vars[i].getUB() < lb) {
                return ESat.FALSE;
            }
        }
        if(minLb>vars[n].getUB()){
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() < lb) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(lb)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications ig, int p) {
        IntVar pivot = ig.getIntVarAt(p);
        int mask = ig.getEventMaskAt(p);
        int m = ig.getValueAt(p);
        if (pivot == vars[n]) {
            if (IntEventType.isInclow(mask)) {
                IntIterableRangeSet setn = explanation.getRootSet(vars[n]);
                setn.retainBetween(m, IntIterableRangeSet.MAX);
                explanation.addLiteral(vars[n], setn, true);
                for (int i = 0; i < n; i++) {
                    if (ig.getDomainAt(front.getValue(vars[i])).min() == m) {
                        IntIterableRangeSet seti = explanation.getRootSet(vars[i]);
                        seti.removeBetween(m, IntIterableRangeSet.MAX);
                        explanation.addLiteral(vars[i], seti, false);
                    }
                }
            } else if (IntEventType.isDecupp(mask)) {
                IntIterableRangeSet setn = explanation.getRootSet(vars[n]);
                setn.retainBetween(IntIterableRangeSet.MIN, m);
                explanation.addLiteral(vars[n], setn, true);
                for (int i = 0; i < n; i++) {
                    if (ig.getDomainAt(front.getValue(vars[i])).max() == m) {
                        IntIterableRangeSet seti = explanation.getRootSet(vars[i]);
                        seti.removeBetween(IntIterableRangeSet.MIN, m);
                        explanation.addLiteral(vars[i], seti, false);
                    }
                }
            }
        } else {
            if (IntEventType.isInclow(mask)) {
                IntIterableRangeSet setn = explanation.getRootSet(vars[n]);
                setn.retainBetween(IntIterableRangeSet.MIN, m - 1);
                explanation.addLiteral(vars[n], setn, false);
                IntIterableRangeSet seti = explanation.getRootSet(pivot);
                seti.retainBetween(m, IntIterableRangeSet.MAX);
                explanation.addLiteral(pivot, seti, true);
            } else if (IntEventType.isDecupp(mask)) {
                IntIterableRangeSet setn = explanation.getRootSet(vars[n]);
                setn.retainBetween(m + 1, IntIterableRangeSet.MAX);
                explanation.addLiteral(vars[n], setn, false);
                IntIterableRangeSet seti = explanation.getRootSet(pivot);
                seti.retainBetween(IntIterableRangeSet.MIN, m);
                explanation.addLiteral(pivot, seti, true);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(vars[n]).append(" = min(");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
