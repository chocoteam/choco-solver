/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * X % mod = Y
 * A propagator for the constraint Y = X % mod where X and Y are integer, possibly negative, variables and mod is an int
 * The filtering algorithm both supports bounded and enumerated integer variables
 *
 * @author Arthur Godet
 * @since 29/03/2019
 */
public class PropModXY extends Propagator<IntVar> {
    private IntVar x;
    private IntVar y;
    private int mod;
    private IntIterableBitSet usedValues;

    public PropModXY(IntVar x, int mod, IntVar y) {
        super(new IntVar[]{x, y}, PropagatorPriority.BINARY, false);
        this.x = x;
        this.y = y;
        this.mod = mod;
        if(y.hasEnumeratedDomain()) {
            usedValues = new IntIterableBitSet();
            usedValues.setOffset(y.getLB());
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(y.getLB()<0) {
            y.updateLowerBound(-(mod-1), this);
        }
        if(y.getUB()>0) {
            y.updateUpperBound(mod-1, this);
        }
        if(x.getUB() <= 0) {
            y.updateUpperBound(0, this);
        }
        if(x.getLB() >= 0) {
            y.updateLowerBound(0, this);
        }

        if(x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
            propagateEnumerated();
        } else {
            propagateBounded();
        }
    }

    private void propagateEnumerated() throws ContradictionException {
        usedValues.clear();
        for(int v = x.getLB(); v<=x.getUB(); v=x.nextValue(v)) {
            if(y.contains(v%mod)) {
                usedValues.add(v%mod);
            } else {
                x.removeValue(v, this);
            }
        }
        y.removeAllValuesBut(usedValues, this);
    }

    private void propagateBounded() throws ContradictionException {
        boolean hasChange = true;
        while(hasChange) {
            hasChange = false;

            // filter bounds for X
            while(!y.contains(x.getLB()%mod)) {
                x.updateLowerBound(x.getLB()+1, this);
                hasChange = true;
            }
            while(!y.contains(x.getUB()%mod)) {
                x.updateUpperBound(x.getUB()-1, this);
                hasChange = true;
            }
            // filter bounds for Y
            while(!containsOneDivid(x, mod, y.getLB())) {
                y.updateLowerBound(y.getLB()+1, this);
                hasChange = true;
            }
            while(!containsOneDivid(x, mod, y.getUB())) {
                y.updateUpperBound(y.getUB()-1, this);
                hasChange = true;
            }
        }
    }

    private static boolean containsOneDivid(IntVar X, int mod, int value) {
        for(int i = X.getLB(); i<=X.getUB(); i=X.nextValue(i)) {
            if(i % mod == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ESat isEntailed() {
        if(x.isInstantiated() && y.isInstantiated()) {
            return x.getValue()%mod == y.getValue() ? ESat.TRUE : ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName()+" % "+ mod +" = "+ y.getName();
    }

}
