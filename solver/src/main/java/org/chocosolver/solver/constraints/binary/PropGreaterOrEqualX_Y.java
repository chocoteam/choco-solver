/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

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
 * X >= Y
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public final class PropGreaterOrEqualX_Y extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualX_Y(IntVar[] vars) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.DECUPP);
        } else {
            return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.INCLOW);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(y.getLB(), this);
        y.updateUpperBound(x.getUB(), this);
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx == 0) {
            y.updateUpperBound(x.getUB(), this);
        } else {
            x.updateLowerBound(y.getLB(), this);
        }
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB())
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB())
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     * Premise: x &ge; y
     * <p>The filtering algorithm states that:
     * <ol type="a">
     *     <li>lb(x) &larr; lb(y) : x cannot be smaller than the smallest value of y</li>
     *     <li>ub(y) &larr; ub(x) : y cannot be greater than the greatest value of x</li>
     * </ol>
     * </p>
     * <p>
     * Inference rules:
     * <ol type="a">
     *     <li>
     *         Let m = lb(y), then
     *         <pre>( x &isin; [m..+&infin;) &or; y &isin; (-&infin;..m - 1] ) </pre>
     *     </li>
     *     <li>
     *         Let n = ub(x), then
     *         <pre>( x &isin; [n + 1..+&infin;) &or; y &isin; (-&infin;..n] ) </pre>
     *     </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(ExplanationForSignedClause explanation,
                        ValueSortedMap<IntVar> front,
                        Implications ig, int p) {
        IntIterableRangeSet set0, set1;
        int m;
        boolean isPivot;
        if(isPivot = (ig.getIntVarAt(p) == vars[0])){ // case a. (see javadoc)
            m = explanation.getSet(vars[1]).min();
            set0 = explanation.getRootSet(vars[0]);
            set1 = explanation.getComplementSet(vars[1]);
            set0.retainBetween(m, IntIterableRangeSet.MAX);
            set1.retainBetween(IntIterableRangeSet.MIN, m - 1);
        }else{ // case b. (see javadoc)
            assert ig.getIntVarAt(p) == vars[1];
            m = explanation.getSet(vars[0]).max();
            set0 = explanation.getComplementSet(vars[0]);
            set1 = explanation.getRootSet(vars[1]);
            set0.retainBetween(m + 1, IntIterableRangeSet.MAX);
            set1.retainBetween(IntIterableRangeSet.MIN, m);
        }
        explanation.addLiteral(vars[0], set0, isPivot);
        explanation.addLiteral(vars[1], set1, !isPivot);
    }

    @Override
    public String toString() {
        return "prop(" + vars[0].getName() + ".GEQ." + vars[1].getName() + ")";
    }

}
