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
 * X + Y >= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropGreaterOrEqualXY_C extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INSTANTIATE.getMask() + IntEventType.DECUPP.getMask();
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(MathUtils.safeSubstract(this.cste, y.getUB()), this);
        y.updateLowerBound(MathUtils.safeSubstract(this.cste, x.getUB()), this);
        if (x.getLB() + y.getLB() >= this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateLowerBound(this.cste - x.getUB(), this);
        } else {
            x.updateLowerBound(this.cste - y.getUB(), this);
        }
        if (x.getLB() + y.getLB() >= this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getUB() + y.getUB() < cste)
            return ESat.FALSE;
        else if (x.getLB() + y.getLB() >= this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     * Premise: x + y &ge; c
     * <p>The filtering algorithm states that:
     * <ol type="a">
     *     <li>lb(x) &larr; c - ub(y) : x cannot be smaller than c minus the greatest value of y</li>
     *     <li>lb(y) &larr; c - ub(x) : y cannot be smaller than c minus the greatest value of x</li>
     * </ol>
     * </p>
     * <p>
     * Inference rules:
     * <ol type="a">
     *     <li>
     *         Let m = ub(y), then
     *         <pre>( x &isin; [c - m..+&infin;) &or; y &isin; [m + 1..+&infin;) ) </pre>
     *     </li>
     *     <li>
     *         Let n = ub(x), then
     *         <pre>( x &isin; [n + 1..+&infin;) &or; y &isin; [c - m..+&infin;) ) </pre>
     *     </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set;
        int m;
        if (explanation.readVar(p) == vars[0]) { // case a. (see javadoc)
            m = explanation.readDom(vars[1]).max();
            set = explanation.complement(vars[1]);
            set.retainBetween(m + 1, IntIterableRangeSet.MAX);
            vars[0].intersectLit(cste - m, IntIterableRangeSet.MAX, explanation);
            vars[1].unionLit(set, explanation);
        } else { // case b. (see javadoc)
            assert explanation.readVar(p) == vars[1];
            m = explanation.readDom(vars[0]).max();
            set = explanation.complement(vars[0]);
            set.retainBetween(m + 1, IntIterableRangeSet.MAX);
            vars[0].unionLit(set, explanation);
            vars[1].intersectLit(cste - m, IntIterableRangeSet.MAX, explanation);
        }
    }

    @Override
    public String toString() {
        return x.getName() + " + " + y.getName() + " >= " + cste;
    }

}
