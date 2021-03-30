/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.SetView;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;

/**
 * Set view over an array of boolean variables defined such that:
 * boolVars[x - offset] = True <=> x in setView
 * This view is equivalent to the {@link org.chocosolver.solver.constraints.set.PropBoolChannel} constraint.
 *
 * @author Dimitri Justeau-Allaire
 * @since 03/2021
 */
public class SetBoolsView<B extends BoolVar> extends SetView<B> {

    /**
     * Offset between boolVars array indices and set elements
     */
    private int offset;

    /**
     * Internal bounds only updated by the view.
     */
    private ISet llb;
    private ISet uub;

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param name  name of the variable
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    protected SetBoolsView(String name, int offset, B... variables) {
        super(name, variables);
        this.offset = offset;
        this.llb = SetFactory.makeStoredSet(SetType.BITSET, 0, variables[0].getModel());
        this.uub = SetFactory.makeStoredSet(SetType.BITSET, 0, variables[0].getModel());
        // init
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].isInstantiatedTo(BoolVar.kTRUE)) {
                llb.add(i + offset);
            }
            if (variables[i].contains(BoolVar.kTRUE)) {
                uub.add(i + offset);
            }
        }
    }

    /**
     * Instantiate an set view over an array of boolean variables such that:
     * boolVars[x - offset] = True <=> x in setView
     *
     * @param offset Offset between boolVars array indices and set elements
     * @param variables observed variables
     */
    public SetBoolsView(int offset, B... variables) {
        this("BOOLS_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(i -> i.getName())
                        .toArray(String[]::new))
                    + "]",
                offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        if (getVariables()[element - this.offset].instantiateTo(BoolVar.kFALSE, this)) {
            uub.remove(element);
            return true;
        }
        return false;
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        if (getVariables()[element - this.offset].instantiateTo(BoolVar.kTRUE, this)) {
            llb.add(element);
            return true;
        }
        return false;
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(BoolVar.kTRUE)) {
            llb.add(variableIdx + offset);
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else if (this.getVariables()[variableIdx].isInstantiatedTo(BoolVar.kFALSE)) {
            uub.remove(variableIdx + offset);
            notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
        }
    }

    @Override
    public ISet getLB() {
        return llb;
    }

    @Override
    public ISet getUB() {
        return uub;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        ISet s = SetFactory.makeConstantSet(Arrays.stream(value).map(i -> i - offset).toArray());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            B var = getVariables()[i];
            if (s.contains(i)) {
                llb.add(i + offset);
                var.instantiateTo(BoolVar.kTRUE, this);
            } else {
                uub.remove(i + offset);
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
