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
 * A specific <code>Propagator</code> extension defining filtering algorithm for:
 * <br/>
 * <b>X + Y =/= C</b>
 * <br>where <i>X</i> and <i>Y</i> are <code>Variable</code> objects and <i>C</i> a constant.
 * <br>
 * This <code>Propagator</code> defines the <code>propagate</code> and <code>awakeOnInst</code> methods. The other ones
 * throw <code>UnsupportedOperationException</code>.
 * <br/>
 * <br/>
 * <i>Based on Choco-2.1.1</i>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Arnaud Malapert
 * @version 0.01, june 2010
 * @since 0.01
 */
public class PropNotEqualXY_C extends Propagator<IntVar> {

    private IntVar x;
    private IntVar y;
    private int cste;

    @SuppressWarnings({"unchecked"})
    public PropNotEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, false);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return IntEventType.instantiation();
        }
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (x.isInstantiated()) {
            if (y.removeValue(cste - x.getValue(), this) || !y.contains(cste - x.getValue())) {
                this.setPassive();
            }
        } else if (y.isInstantiated()) {
            if (x.removeValue(cste - y.getValue(), this) || !x.contains(cste - y.getValue())) {
                this.setPassive();
            }
        } else if (x.getLB() + y.getLB() > cste || x.getUB() + y.getUB() < cste) {
            setPassive();
        }
    }

    @Override
    public void explain(ExplanationForSignedClause explanation,
                        ValueSortedMap<IntVar> front,
                        Implications ig, int p) {
        boolean isPivot;
        int m;
        IntIterableRangeSet set0, set1;
        if (isPivot = (ig.getIntVarAt(p) == vars[0])) {
            m = explanation.getSet(vars[1]).min();
            set0 = explanation.getRootSet(vars[0]);
            set1 = explanation.getRootSet(vars[1]);
            set0.remove(cste - m);
            set1.remove(m);
        } else {
            assert explanation.getSet(vars[0]).size() == 1;
            m = explanation.getSet(vars[0]).min();
            set1 = explanation.getRootSet(vars[1]);
            set0 = explanation.getRootSet(vars[0]);
            set0.remove(m);
            set1.remove(cste - m);
        }
        explanation.addLiteral(vars[0], set0, isPivot);
        explanation.addLiteral(vars[1], set1, !isPivot);
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() + y.getUB() < cste) ||
                (y.getLB() + x.getLB() > cste))
            return ESat.TRUE;
        else if (x.isInstantiated()
                && y.isInstantiated()
                && x.getValue() + y.getValue() == this.cste)
            return ESat.FALSE;
        else
            return ESat.UNDEFINED;
    }

}
