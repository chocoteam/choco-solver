/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X % Y = Z
 * A propagator for the constraint Z = X % Y where X, Y and Z are integer, possibly negative, variables
 * The filtering algorithm both supports bounded and enumerated integer variables
 *
 * @author Arthur Godet
 * @since 29/03/2019
 */
public class PropModXYZ extends Propagator<IntVar> {
    private IntVar x;
    private IntVar y;
    private IntVar z;

    public PropModXYZ(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y, z}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    private TIntArrayList usedValues = new TIntArrayList();

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        y.removeValue(0, this);
        usedValues.clear();
        for(int vx = x.getLB(); vx<=x.getUB(); vx=x.nextValue(vx)) {
            boolean toRemove = true;
            for(int vy = y.getLB(); vy<=y.getUB(); vy=y.nextValue(vy)) {
                if(vy!=0 && z.contains(vx%vy)) {
                    usedValues.add(vx%vy);
                    toRemove = false;
                }
            }
            if(toRemove) {
                x.removeValue(vx, this);
            }
        }
        for(int v = z.getLB(); v<=z.getUB(); v=z.nextValue(v)) {
            if(!usedValues.contains(v)) {
                z.removeValue(v, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(x.isInstantiated() && y.isInstantiated() && z.isInstantiated()) {
            return y.getValue()!=0 && x.getValue()%y.getValue()==z.getValue() ? ESat.TRUE : ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName()+" % "+y.getName()+" = "+z.getName();
    }

}
