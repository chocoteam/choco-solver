/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X % a = Y
 * A propagator for the constraint Y = X % a where X and Y are integer, possibly negative, variables and a is an int
 * The filtering algorithm both supports bounded and enumerated integer variables
 *
 * @author Arthur Godet
 * @since 29/03/2019
 */
public class PropModXY extends Propagator<IntVar> {
    private IntVar x;
    private IntVar y;
    private int a;

    public PropModXY(IntVar x, int a, IntVar y) {
        super(new IntVar[]{x, y}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.y = y;
        this.a = a;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    private TIntArrayList usedValues = new TIntArrayList();

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(a != 0) {
            usedValues.clear();
            for(int v = x.getLB(); v<=x.getUB(); v=x.nextValue(v)) {
                if(y.contains(v%a)) {
                    usedValues.add(v%a);
                } else {
                    x.removeValue(v, this);
                }
            }
            for(int v = y.getLB(); v<= y.getUB(); v= y.nextValue(v)) {
                if(!usedValues.contains(v)) {
                    y.removeValue(v, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(x.isInstantiated() && y.isInstantiated()) {
            return a==0 || x.getValue()%a == y.getValue() ? ESat.TRUE : ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName()+" % "+a+" = "+ y.getName();
    }

}
