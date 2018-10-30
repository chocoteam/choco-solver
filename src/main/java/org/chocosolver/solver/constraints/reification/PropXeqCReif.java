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
 * A propagator dedicated to express in a compact way: (x = c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXeqCReif extends Propagator<IntVar> {

    IntVar var;
    int cste;
    BoolVar r;

    public PropXeqCReif(IntVar x, int c, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false);
        this.cste = c;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            var.instantiateTo(cste, this);
            setPassive();
        } else if (r.getUB() == 0) {
            if (var.removeValue(cste, this) || !var.contains(cste)) {
                setPassive();
            }
        } else {
            if (var.isInstantiatedTo(cste)) {
                r.setToTrue(this);
                setPassive();
            } else if (!var.contains(cste)) {
                r.setToFalse(this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(var.contains(cste));
            }else{
                return ESat.eval(!var.contains(cste));
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" = " + cste + ") <=> "+r.getName();
    }
}
