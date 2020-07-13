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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.tools.MathUtils;

/**
 * X + Y <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropLessOrEqualXY_C extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

    @SuppressWarnings({"unchecked"})
    public PropLessOrEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.INCLOW);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateUpperBound(MathUtils.safeSubstract(this.cste, y.getLB()), this);
        y.updateUpperBound(MathUtils.safeSubstract(this.cste, x.getLB()), this);
        if (x.getUB() + y.getUB() <= this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateUpperBound(this.cste - x.getLB(), this);
        } else {
            x.updateUpperBound(this.cste - y.getLB(), this);
        }
        if (x.getUB() + y.getUB() <= this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getLB() + y.getLB() > cste)
            return ESat.FALSE;
        else if (x.getUB() + y.getUB() <= this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     * Premise: x + y &le; c
     * <p>The filtering algorithm states that:
     * <ol type="a">
     *     <li>ub(x) &larr; c - lb(y) : x cannot be greater than c minus the smallest value of y</li>
     *     <li>ub(y) &larr; c - lb(x) : y cannot be greater than c minus the smallest value of x</li>
     * </ol>
     * </p>
     * <p>
     * Inference rules:
     * <ol type="a">
     *     <li>
     *         Let m = lb(y), then
     *         <pre>( x &isin; (-&infin;..c - m] &or; y &isin; (-&infin;..m - 1] ) </pre>
     *     </li>
     *     <li>
     *         Let n = lb(x), then
     *         <pre>( x &isin; (-&infin;..n - 1] &or; y &isin; (-&infin;..c - m] ) </pre>
     *     </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set0, set1;
        int m;
        if(explanation.readVar(p) == vars[0]){ // case a. (see javadoc)
            m = explanation.readDom(vars[1]).min();
            set0 = explanation.empty();
            set0.addBetween(IntIterableRangeSet.MIN, cste - m);
            set1 = explanation.complement(vars[1]);
            set1.retainBetween(IntIterableRangeSet.MIN, m - 1);
            vars[0].intersectLit(set0, explanation);
            vars[1].unionLit(set1, explanation);
        }else{ // case b. (see javadoc)
            assert explanation.readVar(p) == vars[1];
            m = explanation.readDom(vars[0]).min();
            set0 = explanation.complement(vars[0]);
            set0.retainBetween(IntIterableRangeSet.MIN, m - 1);
            set1 = explanation.empty();
            set1.addBetween(IntIterableRangeSet.MIN, cste - m);
            vars[0].unionLit(set0, explanation);
            vars[1].intersectLit(set1, explanation);
        }
    }

    @Override
    public String toString() {
        return x.getName() + " + " + y.getName() + " <= " + cste;
    }

}
