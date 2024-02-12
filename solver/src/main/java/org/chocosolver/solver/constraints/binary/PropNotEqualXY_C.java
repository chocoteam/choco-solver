/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

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
@Explained(partial = true, comment = "must be tested")
public class PropNotEqualXY_C extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

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
            if (y.removeValue(cste - x.getValue(), this,
                    lcg() ? Reason.r(x.getValLit()): Reason.undef())
                    || !y.contains(cste - x.getValue())) {
                this.setPassive();
            }
        } else if (y.isInstantiated()) {
            if (x.removeValue(cste - y.getValue(), this,
                    lcg() ? Reason.r(y.getValLit()): Reason.undef())
                    || !x.contains(cste - y.getValue())) {
                this.setPassive();
            }
        } else if (x.getLB() + y.getLB() > cste || x.getUB() + y.getUB() < cste) {
            setPassive();
        }
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
