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
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.delta.SetGraphViewDeltaMonitor;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.Arrays;

/**
 * A GraphSetView representing the set of successors of a node in a graph variable.
 * Note that if the graph variable is undirected, the set of successors is the set of neighbors.
 * If the node is removed from the graph envelope, the set is empty.
 *
 * @author Dimitri Justeau-Allaire
 * @since 03/03/2021
 */
public class SetSuccessorsGraphView<E extends GraphVar<?>> extends SetGraphView<E> {

    protected int node;
    protected IGraphDeltaMonitor gdm;
    protected PairProcedure arcRemoved;
    protected PairProcedure arcEnforced;

    /**
     * Create a set view over the successors of a graph variable node.
     * @param name name of the variable
     * @param graphVar observed graph variable
     * @param node index of the observed node
     */
    public SetSuccessorsGraphView(String name, E graphVar, int node) {
        super(name, graphVar);
        this.node = node;
        this.gdm = graphVar.monitorDelta(this);
        this.gdm.startMonitoring();
        if (!graphVar.isDirected()) {
            this.arcRemoved = (from, to) -> {
                if (from == node || to == node) {
                    notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
                }
            };
            this.arcEnforced = (from, to) -> {
                if (from == node || to == node) {
                    notifyPropagators(SetEventType.ADD_TO_KER, this);
                }
            };
        } else {
            this.arcRemoved = (from, to) -> {
                if (from == node) {
                    notifyPropagators(SetEventType.REMOVE_FROM_ENVELOPE, this);
                }
            };
            this.arcEnforced = (from, to) -> {
                if (from == node) {
                    notifyPropagators(SetEventType.ADD_TO_KER, this);
                }
            };
        }
    }

    /**
     * Create a set view over the successors of a graph variable node.
     * @param graphVar observed graph variable
     * @param node index of the observed node
     */
    public SetSuccessorsGraphView(E graphVar, int node) {
        this("SUCCESSORS_OF(" + graphVar.getName() + ", " + node + ")", graphVar, node);
    }

    @Override
    public ISet getLB() {
        return graphVar.getMandatorySuccessorsOf(node);
    }

    @Override
    public ISet getUB() {
        return graphVar.getPotentialSuccessorsOf(node);
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
    public boolean isInstantiated() {
        return getLB().size() == getUB().size();
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        return graphVar.removeEdge(node, element, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return graphVar.enforceEdge(node, element, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (event == GraphEventType.REMOVE_EDGE) {
            gdm.forEachEdge(arcRemoved, GraphEventType.REMOVE_EDGE);
        }
        if (event == GraphEventType.ADD_EDGE) {
            gdm.forEachEdge(arcEnforced, GraphEventType.ADD_EDGE);
        }
    }

    public ISetDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new SetGraphViewDeltaMonitor(graphVar.monitorDelta(propagator)) {
            @Override
            public void forEach(IntProcedure proc, SetEventType evt) throws ContradictionException {
                PairProcedure filter = (from, to) -> {
                    if (from == node) {
                        proc.execute(to);
                    }
                };
                if (evt == SetEventType.ADD_TO_KER) {
                    deltaMonitor.forEachEdge(filter, GraphEventType.ADD_EDGE);
                } else if (evt == SetEventType.REMOVE_FROM_ENVELOPE) {
                    deltaMonitor.forEachEdge(filter, GraphEventType.REMOVE_EDGE);
                }
            }
        };
    }
}
