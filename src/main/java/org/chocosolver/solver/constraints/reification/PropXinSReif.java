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
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

/**
 * A propagator dedicated to express in a compact way: (x = c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXinSReif extends Propagator<IntVar> {

    IntVar var;
    IntIterableRangeSet set;
    BoolVar r;

    public PropXinSReif(IntVar x, IntIterableRangeSet set, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false);
        this.set = set;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            var.removeAllValuesBut(set, this);
            setPassive();
        } else if (r.getUB() == 0) {
            if (var.removeValues(set, this) || !IntIterableSetUtils.intersect(var, set)) {
                setPassive();
            }
        } else {
            if (IntIterableSetUtils.includedIn(var, set)) {
                r.setToTrue(this);
                setPassive();
            } else if (!IntIterableSetUtils.intersect(var, set)) {
                r.setToFalse(this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(IntIterableSetUtils.includedIn(var, set));
            }else{
                return ESat.eval(!IntIterableSetUtils.intersect(var, set));
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" ∈ " + set + ") <=> "+r.getName();
    }

}
