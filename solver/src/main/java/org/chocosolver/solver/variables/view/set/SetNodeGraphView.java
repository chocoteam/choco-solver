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
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.delta.SetGraphViewDeltaMonitor;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;

import java.util.Arrays;

/**
 * A GraphSetView representing the set of nodes of an observed graph variable.
 *
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class SetNodeGraphView<E extends GraphVar<?>> extends SetGraphView<E> {

    /**
     * Create a set view over the set of nodes of a graph variable.
     * @param name name of the variable
     * @param graphVar observed graph variable
     */
    public SetNodeGraphView(String name, E graphVar) {
        super(name, graphVar);
    }

    /**
     * Creates a set view over the set of nodes of a graph variable.
     * @param graphVar observed graph variable
     */
    public SetNodeGraphView(E graphVar) {
        this("NODES(" + graphVar.getName() + ")", graphVar);
    }

    @Override
    public ISet getLB() {
        return graphVar.getMandatoryNodes();
    }

    @Override
    public ISet getUB() {
        return graphVar.getPotentialNodes();
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        boolean changed = !isInstantiated();
        for (int i : value) {
            force(i, cause);
        }
        if (getLB().size() != value.length) {
            contradiction(cause, this.getName() + " cannot be instantiated to " + Arrays.toString(value));
        }
        if (getUB().size() != value.length) {
            for (int i : getUB()) {
                if (!getLB().contains(i)) {
                    remove(i, cause);
                }
            }
        }
        return changed;
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        return graphVar.removeNode(element, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return graphVar.enforceNode(element, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (event == GraphEventType.REMOVE_NODE) {
            notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
        }
        if (event == GraphEventType.ADD_NODE) {
            notifyPropagators(SetEventType.ADD_TO_KER, this);
        }
    }

    @Override
    public boolean isInstantiated() {
        return getLB().size() == getUB().size();
    }

    public ISetDeltaMonitor monitorDelta(ICause propagator) {
        return new SetGraphViewDeltaMonitor(graphVar.monitorDelta(propagator)) {
            @Override
            public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
                if (evt == SetEventType.ADD_TO_KER) {
                    deltaMonitor.forEachNode(proc, GraphEventType.ADD_NODE);
                } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
                    deltaMonitor.forEachNode(proc, GraphEventType.REMOVE_NODE);
                }
            }
        };
    }
}
