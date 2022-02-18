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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDelta;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.SetView;
import org.chocosolver.solver.variables.view.delta.SetViewOnSetsDeltaMonitor;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDifference;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Set view over set variables representing the union of these variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 29/03/2021
 */
public class SetUnionView extends SetView<SetVar> {

    protected SetUnion lb;
    protected SetUnion ub;
    protected ISet enforce;

    /**
     * Create a set union view.
     *
     * @param name      name of the variable
     * @param variables observed variables
     */
    public SetUnionView(String name, SetVar... variables) {
        super(name, variables);
        this.enforce = SetFactory.makeStoredSet(SetType.RANGESET, 0, getModel());
        ISet[] LBs = new ISet[variables.length + 1];
        ISet[] UBs = new ISet[variables.length];
        for (int i = 0; i < variables.length; i++) {
            LBs[i] = variables[i].getLB();
            UBs[i] = variables[i].getUB();
        }
        LBs[variables.length] = enforce;
        this.lb = new SetUnion(getModel(), LBs);
        this.ub = new SetUnion(getModel(), UBs);
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
        return false;
    }

    @Override
    public boolean isInstantiated() {
        return getLB().size() == getUB().size();
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        // If an element is removed from an observed SetVar, it may be necessary to enforce
        // an element that could not be enforced before.
        if ((event.getMask() & SetEventType.REMOVE_FROM_ENVELOPE.getMask()) > 0) {
            for (int i : enforce) {
                if (doForceSetElement(i)) {
                    enforce.remove(i);
                    break;
                }
            }
        }
        notifyPropagators(event, this);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        if (enforce.contains(element)) {
            contradiction(this, "Try to remove mandatory element");
        }
        boolean b = false;
        for (SetVar set : variables) {
            b |= set.remove(element, this);
        }
        return b;
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        int nb = 0;
        int idx = -1;
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getUB().contains(element)) {
                nb++;
                idx = i;
                if (nb > 1) {
                    break;
                }
            }
        }
        if (nb == 1) {
            return variables[idx].force(element, this);
        } else {
            enforce.add(element);
        }
        return false;
    }

    @Override
    public ISetDelta getDelta() {
        throw new UnsupportedOperationException("SetUnionView does not support getDelta()");
    }

    @Override
    public ISetDeltaMonitor monitorDelta(ICause propagator) {
        ISetDeltaMonitor[] deltaMonitors = new ISetDeltaMonitor[variables.length];
        for (int i = 0; i < variables.length; i++) {
            deltaMonitors[i] = variables[i].monitorDelta(propagator);
        }
        return new SetViewOnSetsDeltaMonitor(deltaMonitors) {
            final ISet remove = new SetUnion(removedValues);
            final ISet added = SetFactory.makeStoredSet(SetType.RANGESET, 0, getModel());
            final ISet add = new SetDifference(new SetUnion(addedValues), added);
            @Override
            public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
                fillValues();
                if (evt == SetEventType.ADD_TO_KER) {
                    for (int v : add) {
                        proc.execute(v);
                    }
                    for (int v : add) {
                        added.add(v);
                    }
                } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
                    for (int v : remove) {
                        if (!getUB().contains(v)) {
                            proc.execute(v);
                        }
                    }
                }
            }
        };
    }
}
