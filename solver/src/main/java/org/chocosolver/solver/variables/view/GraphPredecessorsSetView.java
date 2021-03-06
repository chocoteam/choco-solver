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
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

import java.util.Arrays;

/**
 * A GraphSetView representing the set of predecessors of a node in a graph variable.
 * Note that if the graph variable is undirected, the set of predecessors is the set of neighbors.
 * If the node is removed from the graph envelope, the set is empty.
 * @author Dimitri Justeau-Allaire
 * @since 03/03/2021
 */
public class GraphPredecessorsSetView extends GraphSetView<GraphVar> {

    protected int node;
    protected IGraphDeltaMonitor gdm;
    protected PairProcedure arcRemoved;
    protected PairProcedure arcEnforced;

    /**
     * Create a set view over the predecessors of a graph variable node.
     * @param name name of the variable
     * @param graphVar observed graph variable
     * @param node index of the observed node
     */
    public GraphPredecessorsSetView(String name, GraphVar graphVar, int node) {
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

    /**
     * Create a set view over the predecessors of a graph variable node.
     * @param graphVar observed graph variable
     * @param node index of the observed node
     */
    public GraphPredecessorsSetView(GraphVar graphVar, int node) {
        this("PREDECESSORS_OF(" + graphVar.getName() + ", " + node + ")", graphVar, node);
    }

    @Override
    public ISet getLB() {
        return graphVar.getMandatoryPredecessorsOf(node);
    }

    @Override
    public ISet getUB() {
        return graphVar.getPotentialPredecessorOf(node);
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
        return graphVar.removeEdge(element, node, this);
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        return graphVar.enforceEdge(element, node, this);
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
