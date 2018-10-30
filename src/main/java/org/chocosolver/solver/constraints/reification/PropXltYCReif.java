/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express in a compact way: (x < y + c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXltYCReif extends Propagator<IntVar> {

    int cste;

    public PropXltYCReif(IntVar x, IntVar y, int c, BoolVar r) {
        super(new IntVar[]{x, y, r}, PropagatorPriority.TERNARY, false);
        this.cste = c;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[2].getLB() == 1) {
            vars[0].updateUpperBound(vars[1].getUB() + cste - 1, this);
            vars[1].updateLowerBound(vars[0].getLB() - cste + 1, this);
            if (vars[0].getUB() < vars[1].getLB() + cste) {
                this.setPassive();
            }
        } else if (vars[2].getUB() == 0) {
            vars[0].updateLowerBound(vars[1].getLB() + cste, this);
            vars[1].updateUpperBound(vars[0].getUB() - cste, this);
            if (vars[0].getLB() >= vars[1].getUB() + cste) {
                setPassive();
            }
        } else {
            if (vars[0].getUB() < vars[1].getLB() + cste) {
                vars[2].instantiateTo(1, this);
                setPassive();
            } else if (vars[0].getLB() >= vars[1].getUB() + cste) {
                vars[2].instantiateTo(0, this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(vars[2].isInstantiatedTo(1)){
                return ESat.eval(vars[0].getValue() < vars[1].getValue() + cste);
            }else{
                return ESat.eval(vars[0].getValue() >= vars[1].getValue() + cste);
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "(" + vars[0].getName() +" < " + vars[1].getName() + " + "+cste+") <=> "+vars[2].getName();
    }
}
