/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.delta.monitor.GraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.scheduler.GraphEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

public abstract class AbstractGraphVar<E extends IGraph> extends AbstractVariable implements GraphVar<E> {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////
    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected E UB, LB;
    protected GraphDelta delta;
    protected int n;
    ///////////// Attributes related to Variable ////////////
    protected boolean reactOnModification;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a graph variable
     *
     * @param solver
     */
    protected AbstractGraphVar(String name, Model solver, E LB, E UB) {
        super(name, solver);
        this.LB = LB;
        this.UB = UB;
        this.n = UB.getNbMaxNodes();
        assert n == LB.getNbMaxNodes();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean isInstantiated() {
        if (getPotentialNodes().size() != getMandatoryNodes().size()) {
            return false;
        }
        ISet suc;
        for (int i : getUB().getNodes()) {
            suc = UB.getSuccessorsOf(i);
            if (suc.size() != getLB().getSuccessorsOf(i).size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove node x from the domain
     * Removes x from the upper bound graph
     *
     * @param x     node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     */
    public boolean removeNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        assert (x >= 0 && x < n);
        if (LB.getNodes().contains(x)) {
            this.contradiction(cause, "remove mandatory node");
            return true;
        } else if (!UB.getNodes().contains(x)) {
            return false;
        }
        int[] succ = UB.getSuccessorsOf(x).toArray();
        int[] pred = UB.getPredecessorsOf(x).toArray();
        if (UB.removeNode(x)) {
            if (reactOnModification) {
                delta.add(x, GraphDelta.NODE_REMOVED, cause);
                for (int i : succ) {
                    delta.add(x, GraphDelta.EDGE_REMOVED_TAIL, cause);
                    delta.add(i, GraphDelta.EDGE_REMOVED_HEAD, cause);
                }
                for (int i : pred) {
                    delta.add(i, GraphDelta.EDGE_REMOVED_TAIL, cause);
                    delta.add(x, GraphDelta.EDGE_REMOVED_HEAD, cause);
                }
            }
            if (succ.length > 0 || pred.length > 0) {
                notifyPropagators(GraphEventType.REMOVE_EDGE, cause);
            }
            notifyPropagators(GraphEventType.REMOVE_NODE, cause);
            return true;
        }
        return false;
    }

    /**
     * Enforce the node x to belong to any solution
     * Adds x to the lower bound graph
     *
     * @param x     node's index
     * @param cause algorithm which is related to the modification
     * @return true iff the enforcing has an effect
     */
    public boolean enforceNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        assert (x >= 0 && x < n);
        if (UB.getNodes().contains(x)) {
            if (LB.addNode(x)) {
                if (reactOnModification) {
                    delta.add(x, GraphDelta.NODE_ENFORCED, cause);
                }
                notifyPropagators(GraphEventType.ADD_NODE, cause);
                return true;
            }
            return false;
        }
        this.contradiction(cause, "enforce node which is not in the domain");
        return true;
    }

    @Override
    public boolean removeEdge(int x, int y, ICause cause) throws ContradictionException {
        assert cause != null;
        if (LB.containsEdge(x, y)) {
            this.contradiction(cause, "remove mandatory edge");
            return false;
        }
        if (UB.removeEdge(x, y)) {
            if (reactOnModification) {
                delta.add(x, GraphDelta.EDGE_REMOVED_TAIL, cause);
                delta.add(y, GraphDelta.EDGE_REMOVED_HEAD, cause);
            }
            notifyPropagators(GraphEventType.REMOVE_EDGE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean enforceEdge(int x, int y, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean addX = !LB.containsNode(x);
        boolean addY = !LB.containsNode(y);
        if (UB.containsEdge(x, y)) {
            if (LB.addEdge(x, y)) {
                if (reactOnModification) {
                    delta.add(x, GraphDelta.EDGE_ENFORCED_TAIL, cause);
                    delta.add(y, GraphDelta.EDGE_ENFORCED_HEAD, cause);
                    if (addX) {
                        delta.add(x, GraphDelta.NODE_ENFORCED, cause);
                    }
                    if (addY) {
                        delta.add(y, GraphDelta.NODE_ENFORCED, cause);
                    }
                }
                if (addX || addY) {
                    notifyPropagators(GraphEventType.ADD_NODE, cause);
                }
                notifyPropagators(GraphEventType.ADD_EDGE, cause);
                return true;
            }
            return false;
        }
        this.contradiction(cause, "enforce edge which is not in the domain");
        return false;
    }

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    /**
     * @return the lower bound graph (having mandatory nodes and edges)
     */
    public E getLB() {
        return LB;
    }

    /**
     * @return the upper bound graph (having possible nodes and edges)
     */
    public E getUB() {
        return UB;
    }

    /**
     * @return the maximum number of node the graph variable may have.
     * Nodes are comprised in the interval [0,getNbMaxNodes()]
     * Therefore, any vertex should be strictly lower than getNbMaxNodes()
     */
    public int getNbMaxNodes() {
        return n;
    }

    //***********************************************************************************
    // VARIABLE STUFF
    //***********************************************************************************


    @Override
    public GraphDelta getDelta() {
        return delta;
    }

    @Override
    public int getTypeAndKind() {
        return VAR | GRAPH;
    }

    @Override
    public EvtScheduler createScheduler() {
        return new GraphEvtScheduler();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph_var ").append(getName());
        if (isInstantiated()) {
            sb.append("\nValue: \n");
            sb.append(UB.toString());
        } else {
            sb.append("\nUpper bound: \n");
            sb.append(UB.toString());
            sb.append("\nLower bound: \n");
            sb.append(LB.toString());
        }
        return sb.toString();
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new GraphDelta(getEnvironment());
        }
    }

    @Override
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEngine().onVariableUpdate(this, event, cause);
        notifyMonitors(event);
        notifyViews(event, cause);
    }

    //***********************************************************************************
    // SOLUTIONS : STORE AND RESTORE
    //***********************************************************************************

    @Override
    public void instantiateTo(E value, ICause cause) throws ContradictionException {
        ISet nodes = value.getNodes();
        for (int i = 0; i < n; i++) {
            if (nodes.contains(i)) {
                enforceNode(i, cause);
            } else {
                removeNode(i, cause);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (nodes.contains(i) && nodes.contains(j)) {
                    if (value.getSuccessorsOf(i).contains(j)) {
                        enforceEdge(i, j, cause);
                    } else {
                        removeEdge(i, j, cause);
                    }
                }
            }
        }
    }

    /**
     * @return the value of the graph variable represented through an adjacency matrix
     * plus a set of nodes (last row of the matrix).
     * This method is not supposed to be used except for restoring solutions.
     */
    public boolean[][] getValueAsBoolMatrix() {
        int n = getUB().getNbMaxNodes();
        boolean[][] vals = new boolean[n + 1][n];
        for (int i : getLB().getNodes()) {
            for (int j : getLB().getSuccessorsOf(i)) {
                vals[i][j] = true; // edge in
            }
            vals[n][i] = true; // node in
        }
        return vals;
    }

    /**
     * Instantiates <code>this</code> to value which represents an adjacency
     * matrix plus a set of nodes (last row of the matrix).
     * This method is not supposed to be used except for restoring solutions.
     *
     * @param value value of <code>this</code>
     * @param cause
     * @throws ContradictionException if the edge was mandatory
     */
    public void instantiateTo(boolean[][] value, ICause cause) throws ContradictionException {
        int n = value.length - 1;
        for (int i = 0; i < n; i++) {
            if (value[n][i]) {//nodes
                enforceNode(i, cause);
            } else {
                removeNode(i, cause);
            }
            for (int j = 0; j < n; j++) {
                if (value[i][j]) {//edges
                    enforceEdge(i, j, cause);
                } else {
                    removeEdge(i, j, cause);
                }
            }
        }
    }

    @Override
    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new GraphDeltaMonitor(getDelta(), propagator);
    }
}
