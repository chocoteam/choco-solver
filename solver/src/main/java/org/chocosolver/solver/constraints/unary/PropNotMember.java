/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.UpdatablePropagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

/**
 * This propagator manages a singleton nogood.
 * <p>
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 12/10/2016.
 */
public class PropNotMember extends Propagator<IntVar> implements UpdatablePropagator<IntIterableRangeSet> {

    /**
     * List of forbidden values.
     */
    private final IntIterableRangeSet range;

    /**
     * Maintain : <i>var</i>&notin;<i>range</i>
     *
     * @param var   a variable
     * @param range list of possible values
     */
    public PropNotMember(IntVar var, IntIterableRangeSet range) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false);
        this.range = range.duplicate();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.all();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (enforce(vars[0], range, this)) {
            setPassive();
        }
    }

    private static boolean enforce(IntVar var, IntIterableRangeSet fset, Propagator<IntVar> prop) throws ContradictionException {
        return var.removeValues(fset, prop)
                && (var.hasEnumeratedDomain() || IntIterableSetUtils.notIncludedIn(var, fset));
    }

    @Override
    public ESat isEntailed() {
        if (IntIterableSetUtils.includedIn(vars[0], range)) {
            return ESat.FALSE;
        } else if (range.intersect(vars[0])) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    /**
     * @implSpec <p>
     * Consider that v1 has been modified by propagation of this.
     * Before the propagation, the domains were like:
     * <pre>
     *         (v1 &isin; D1)
     *     </pre>
     * Then this propagates v1 &notin; S, then:
     * <pre>
     *         (v1 &isin; D1) &rarr; v1 &notin; S
     *     </pre>
     * Converting to DNF:
     * <pre>
     *         (v1 &isin; (U \ D1) &cup; (U \ S))
     *     </pre>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set = explanation.universe();
        set.removeAll(range);
        vars[0].intersectLit(set, explanation);
    }

    @Override
    public String toString() {
        return vars[0].getName() + " \u2209 " + range;
    }

    @Override
    public void update(IntIterableRangeSet values, boolean thenForcePropagate) {
        this.range.clear();
        this.range.addAll(values);
        if (thenForcePropagate) forcePropagationOnBacktrack();
    }

    @Override
    public IntIterableRangeSet getUpdatedValue() {
        return this.range.duplicate();
    }
}
