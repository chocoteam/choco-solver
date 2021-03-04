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
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.Arrays;

/**
 * A GraphSetView representing the set of neighbors of a node in an undirected graph variable.
 * If the node is removed from the graph envelope, the set is empty.
 * @author Dimitri Justeau-Allaire
 * @since 03/03/2021
 */
public class GraphNeighSetView extends GraphSetView<UndirectedGraphVar> {

    protected int node;
    protected IGraphDeltaMonitor gdm;
    protected PairProcedure arcRemoved;
    protected PairProcedure arcEnforced;

    /**
     * Create a set view on the neighbors of an undirected graph variable node.
     *
     * @param name name of the variable
     * @param graphVar observed graph variable
     * @param node index of the observed node
     */
    protected GraphNeighSetView(String name, UndirectedGraphVar graphVar, int node) {
        super(name, graphVar);
        this.node = node;
        this.gdm = graphVar.monitorDelta(this);
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
    }

    @Override
    public ISet getLB() {
        return graphVar.getMandatoryNeighborsOf(node);
    }

    @Override
    public ISet getUB() {
        return graphVar.getPotentialNeighborsOf(node);
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
    public void notify(IEventType event) throws ContradictionException {
        if (event == GraphEventType.REMOVE_EDGE) {
            gdm.forEachEdge(arcRemoved, GraphEventType.REMOVE_EDGE);
        }
        if (event == GraphEventType.ADD_EDGE) {
            gdm.forEachEdge(arcEnforced, GraphEventType.ADD_EDGE);
        }
    }
}
