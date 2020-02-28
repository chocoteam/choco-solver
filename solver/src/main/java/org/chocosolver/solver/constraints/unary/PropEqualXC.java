/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * Unary propagator ensuring:
 * <br/>
 * X = C, where X is a variable and C is a constant
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropEqualXC extends Propagator<IntVar> {

    private final int constant;

    public PropEqualXC(IntVar var, int cste) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false);
        this.constant = cste;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[0].instantiateTo(constant, this);
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].isInstantiatedTo(constant)) {
            return ESat.TRUE;
        } else if (vars[0].contains(constant)) {
            return ESat.UNDEFINED;
        }
        return ESat.FALSE;
    }

    /**
     * @implSpec
     * <p>
     *     Consider that v1 has been modified by propagation of this.
     *     Before the propagation, the domains were like:
     * <pre>
     *         (v1 &isin; D1)
     *     </pre>
     * Then this propagates v1 = c, then:
     * <pre>
     *         (v1 &isin; D1) &rarr; v1 = c
     *     </pre>
     * Converting to DNF:
     * <pre>
     *         (v1 &isin; (U \ D1) &cup; {c})
     *     </pre>
     * </p>
     */
    @Override
    public void explain(ExplanationForSignedClause explanation,
                        ValueSortedMap<IntVar> front,
                        Implications ig, int p) {
        IntIterableRangeSet set = explanation.getFreeSet();
        set.add(constant);
        explanation.addLiteral(vars[0], set, true);
    }

    @Override
    public String toString() {
        return vars[0].getName() + " = " + constant;
    }

}
