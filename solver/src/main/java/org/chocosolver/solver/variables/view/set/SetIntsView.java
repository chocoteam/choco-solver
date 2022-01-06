/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDelta;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.delta.SetDelta;
import org.chocosolver.solver.variables.delta.monitor.SetDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.SetView;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.Arrays;

/**
 * Set view over an array of integer variables defined such that:
 * with v an array of integers and offset an integer (constants) intVariables[x - offset] = v[x - offset] <=> x in set.
 *
 * @author Dimitri Justeau-Allaire
 * @since 03/2021
 */
public class SetIntsView<I extends IntVar> extends SetView<I> {

    /**
     * Integer value array such that intVariables[x - offset] = v[x - offset] <=> x in set
     */
    private final int[] v;

    /**
     * Integer value such that intVariables[x - offset] = v[x - offset] <=> x in set
     */
    private final int offset;

    private final IIntDeltaMonitor[] idm;

    private final IntProcedure[] valRemoved;

    /**
     * Internal bounds only updated by the view.
     */
    private final ISet lb;
    private final ISet ub;

    protected boolean reactOnModification;
    private ISetDelta delta;

    /**
     * Instantiate a set view over an array of integer variables such that:
     * intVariables[x - offset] = v[x - offset] <=> x in set
     *
     * @param name  name of the variable
     * @param v integer array that "toggle" integer variables index inclusion in the set view.
     *          Must have the same size as the observed variable array.
     * @param offset offset such that if intVariables[x - offset] = v[x - offset] <=> x in set view.
     * @param variables observed variables
     */
    @SafeVarargs
    protected SetIntsView(String name, int[] v, int offset, I... variables) {
        super(name, variables);
        assert v.length == variables.length;
        this.v = v;
        this.offset = offset;
        this.idm = new IIntDeltaMonitor[getNbObservedVariables()];
        this.valRemoved = new IntProcedure[getNbObservedVariables()];
        this.lb = SetFactory.makeStoredSet(SetType.BITSET, 0, variables[0].getModel());
        this.ub = SetFactory.makeStoredSet(SetType.BITSET, 0, variables[0].getModel());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            this.idm[i] = getVariables()[i].monitorDelta(this);
            this.idm[i].startMonitoring();
            int finalI = i;
            this.valRemoved[i] = val -> {
                if (val == this.v[finalI]) {
                    this.ub.remove(finalI + offset);
                    if (reactOnModification) {
                        delta.add(finalI + offset, SetDelta.UB, this);
                    }
                    notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
                }
            };
        }
        // init
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].isInstantiatedTo(v[i])) {
                lb.add(i + offset);
            }
            if (variables[i].contains(v[i])) {
                ub.add(i + offset);
            }
        }
    }

    /**
     * Instantiate an set view over an array of integer variables such that:
     * intVariables[x - offset] = v[x - offset] <=> x in set
     *
     * @param v integer array that "toggle" integer variables index inclusion in the set view
     * @param offset offset between integer variables indices and set elements.
     * @param variables observed variables
     */
    @SafeVarargs
    public SetIntsView(int[] v, int offset, I... variables) {
        this("INTS_SET_VIEW["
                    + String.join(",", Arrays.stream(variables)
                        .map(Variable::getName)
                        .toArray(String[]::new))
                    + "]",
                v, offset, variables);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        if (getVariables()[element - this.offset].removeValue(this.v[element - this.offset], this)) {
            ub.remove(element);
            if (reactOnModification) {
                delta.add(element, SetDelta.UB, this);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        if (getVariables()[element - this.offset].instantiateTo(this.v[element - this.offset], this)) {
            lb.add(element);
            if (reactOnModification) {
                delta.add(element, SetDelta.LB, this);
            }
            return true;
        }
        return false;
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (this.getVariables()[variableIdx].isInstantiatedTo(this.v[variableIdx])) {
            lb.add(variableIdx + offset);
            if (reactOnModification) {
                delta.add(variableIdx + offset, SetDelta.LB, this);
            }
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        } else {
            this.idm[variableIdx].forEachRemVal(this.valRemoved[variableIdx]);
        }
    }

    @Override
    public ISet getLB() {
        return lb;
    }

    @Override
    public ISet getUB() {
        return ub;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        ISet s = SetFactory.makeConstantSet(Arrays.stream(value).map(i -> i - offset).toArray());
        for (int i = 0; i < getNbObservedVariables(); i++) {
            I var = getVariables()[i];
            if (s.contains(i)) {
                lb.add(i + offset);
                var.instantiateTo(this.v[i], this);
            } else {
                ub.remove(i + offset);
                var.removeValue(this.v[i], this);
            }
        }
        return changed;
    }

    @Override
    public boolean isInstantiated() {
        for (int i = 0; i < getNbObservedVariables(); i++) {
            if (!getVariables()[i].isInstantiated() && getVariables()[i].contains(this.v[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ISetDelta getDelta() {
        return delta;
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new SetDelta(model.getEnvironment());
        }
    }

    @Override
    public ISetDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new SetDeltaMonitor(getDelta(), propagator);
    }
}
