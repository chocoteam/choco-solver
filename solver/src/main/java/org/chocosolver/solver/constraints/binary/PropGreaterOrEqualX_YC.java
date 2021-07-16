/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.MathUtils;

/**
 * X >= Y + C
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropGreaterOrEqualX_YC extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualX_YC(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
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
        x.updateLowerBound(MathUtils.safeAdd(y.getLB(), this.cste), this);
        y.updateUpperBound(MathUtils.safeSubstract(x.getUB(), this.cste), this);
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateUpperBound(x.getUB() - this.cste, this);
        } else {
            x.updateLowerBound(y.getLB() + this.cste, this);
        }
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB() + cste)
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB() + this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     * Premise: x &ge; y + c
     * <p>The filtering algorithm states that:
     * <ol type="a">
     *     <li>lb(x) &larr; lb(y) + c : x cannot be smaller than the smallest value of y plus c</li>
     *     <li>ub(y) &larr; ub(x) - c : y cannot be greater than the greatest value of x minus c</li>
     * </ol>
     * </p>
     * <p>
     * Inference rules:
     * <ol type="a">
     *     <li>
     *         Let m = lb(y), then
     *         <pre>( x &isin; [m + c..+&infin;) &or; y &isin; (-&infin;..m - 1] ) </pre>
     *     </li>
     *     <li>
     *         Let n = ub(x), then
     *         <pre>( x &isin; [n + 1..+&infin;) &or; y &isin; (-&infin;..n - c] ) </pre>
     *     </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set;
        int m;
        if (explanation.readVar(p) == vars[0]) { // case a. (see javadoc)
            m = explanation.readDom(vars[1]).min();
            set = explanation.complement(vars[1]);
            set.retainBetween(IntIterableRangeSet.MIN, m - 1);
            vars[0].intersectLit(m + cste, IntIterableRangeSet.MAX, explanation);
            vars[1].unionLit(set, explanation);
        } else { // case b. (see javadoc)
            assert explanation.readVar(p) == vars[1];
            m = explanation.readDom(vars[0]).max();
            set = explanation.complement(vars[0]);
            set.retainBetween(m + 1, IntIterableRangeSet.MAX);
            vars[0].unionLit(set, explanation);
            vars[1].intersectLit(IntIterableRangeSet.MIN, m - cste, explanation);
        }
    }

    @Override
    public String toString() {
        return x.getName() + " >= " + y.getName() + " + " + cste;
    }

}
