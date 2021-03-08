/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Set view over an array of integer variables defined such that:
 * with v and offset two integers (constant) intVariables[x - offset] = c <=> x in set.
 */
public class IntsSetView<I extends IntVar> extends SetView<I> {

    /**
     * Integer value such that intVariables[x - offset] = v <=> x in set
     */
    private int v;

    /**
     * Integer value such that intVariables[x - offset] = v <=> x in set
     */
    private int offset;

    private IIntDeltaMonitor[] idm;

    private IntProcedure valRemoved;

    /**
     * Instantiate an IntArraySetView from an array of integer variables.
     *
     * @param name  name of the variable
     * @param v integer that "toggle" integer variables index inclusion in the set view
     * @param offset offset such that if intVariables[x - offset] = v <=> x in set view.
     * @param variables observed variables
     */
    protected IntsSetView(String name, int v, int offset, I... variables) {
        super(name, variables);
        this.v = v;
        this.offset = offset;
        this.idm = new IIntDeltaMonitor[getNbObservedVariables()];
        for (int i = 0; i < getNbObservedVariables(); i++) {
            this.idm[i] = getVariables()[i].monitorDelta(this);
        }
        this.valRemoved = i -> {
            if (i == this.v) {
                notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
            }
        };
    }

    /**
     * Instantiate an IntArraySetView from an array of integer variables.
     *
     * @param v integer that "toggle" integer variables index inclusion in the set view
     * @param offset offset such that if intVariables[x - offset] = v <=> x in set view.
     * @param variables observed variables
     */
    public IntsSetView(int v, int offset, I... variables) {
        this("INT_ARRAY_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(i -> i.getName())
                        .toArray(String[]::new))
                    + "]",
                v, offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].removeValue(this.v, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].instantiateTo(this.v, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(this.v)) {
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else {
            this.idm[variableIdx].forEachRemVal(this.valRemoved);
        }
    }

    @Override
    public ISet getLB() {
        int[] lb = IntStream.range(0, getNbObservedVariables())
                .filter(i -> getVariables()[i].isInstantiatedTo(this.v))
                .map(i -> i + this.offset)
                .toArray();
        return SetFactory.makeConstantSet(lb);
    }

    @Override
    public ISet getUB() {
        int[] ub = IntStream.range(0, getNbObservedVariables())
                .filter(i -> getVariables()[i].contains(this.v))
                .map(i -> i + this.offset)
                .toArray();
        return SetFactory.makeConstantSet(ub);
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        ISet s = SetFactory.makeConstantSet(Arrays.stream(value).map(i -> i - offset).toArray());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            I var = getVariables()[i];
            if (s.contains(i)) {
                var.instantiateTo(this.v, this);
            } else {
                var.removeValue(this.v, this);
            }
        }
        return changed;
    }

    @Override
    public boolean isInstantiated() {
        for (I var : getVariables()) {
            if (!var.isInstantiated() && var.contains(this.v)) {
                return false;
            }
        }
        return true;
    }
}
