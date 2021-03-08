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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Set view over an array of boolean variables defined such that:
 * boolVars[x - offset] = True <=> x in setView
 * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropBoolChannel} constraint.
 */
public class BoolsSetView<B extends BoolVar> extends SetView<B> {

    /**
     * Offset between boolVars array indices and set elements
     */
    private int offset;

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param name  name of the variable
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    protected BoolsSetView(String name, int offset, B... variables) {
        super(name, variables);
        this.offset = offset;
    }

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    public BoolsSetView(int offset, B... variables) {
        this("BOOLS_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(i -> i.getName())
                        .toArray(String[]::new))
                    + "]",
                offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].instantiateTo(BoolVar.kFALSE, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return getVariables()[element - this.offset].instantiateTo(BoolVar.kTRUE, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(BoolVar.kTRUE)) {
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else {
            notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
        }
    }

    @Override
    public ISet getLB() {
        int[] lb = IntStream.range(0, getNbObservedVariables())
                .filter(i -> getVariables()[i].isInstantiatedTo(BoolVar.kTRUE))
                .map(i -> i + this.offset)
                .toArray();
        return SetFactory.makeConstantSet(lb);
    }

    @Override
    public ISet getUB() {
        int[] ub = IntStream.range(0, getNbObservedVariables())
                .filter(i -> getVariables()[i].contains(BoolVar.kTRUE))
                .map(i -> i + this.offset)
                .toArray();
        return SetFactory.makeConstantSet(ub);
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        ISet s = SetFactory.makeConstantSet(Arrays.stream(value).map(i -> i - offset).toArray());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            B var = getVariables()[i];
            if (s.contains(i)) {
                var.instantiateTo(BoolVar.kTRUE, this);
            } else {
                var.instantiateTo(BoolVar.kFALSE, this);
            }
        }
        return changed;
    }

    @Override
    public boolean isInstantiated() {
        for (B var : getVariables()) {
            if (!var.isInstantiated()) {
                return false;
            }
        }
        return true;
    }
}
