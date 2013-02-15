/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.graph;

import memory.IEnvironment;
import common.util.objects.graphs.IGraph;
import common.util.objects.setDataStructures.ISet;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.GraphDelta;
import solver.variables.delta.IGraphDelta;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.delta.monitor.GraphDeltaMonitor;


/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 feb. 2011
 */
public abstract class GraphVar<E extends IGraph> extends AbstractVariable<IGraphDelta, GraphVar<E>>
        implements Variable<IGraphDelta> {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////
    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected E envelop, kernel;
    protected IEnvironment environment;
    protected IGraphDelta delta;
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
    public GraphVar(String name, Solver solver) {
        super(name, solver);
        solver.associates(this);
        this.environment = solver.getEnvironment();
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean instantiated() {
        if (getEnvelopOrder() != getKernelOrder()) {
            return false;
        }
        ISet suc;
        ISet act = getEnvelopGraph().getActiveNodes();
        for (int i = act.getFirstElement(); i >= 0; i = act.getNextElement()) {
            suc = envelop.getSuccsOrNeigh(i);
            if (suc.getSize() != getKernelGraph().getSuccsOrNeigh(i).getSize()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove node x from the maximal partial subgraph
     *
     * @param x     node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     */
    public boolean removeNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        if (kernel.getActiveNodes().contain(x)) {
            this.contradiction(cause, EventType.REMOVENODE, "remove mandatory node");
            return true;
        } else if (!envelop.getActiveNodes().contain(x)) {
            return false;
        }
        ISet nei = envelop.getSuccsOrNeigh(x);
        for (int i = nei.getFirstElement(); i >= 0; i = nei.getNextElement()) {
            removeArc(x, i, cause);
        }
        nei = envelop.getPredsOrNeigh(x);
        for (int i = nei.getFirstElement(); i >= 0; i = nei.getNextElement()) {
            removeArc(i, x, cause);
        }
        if (envelop.desactivateNode(x)) {
            if (reactOnModification) {
                delta.add(x, IGraphDelta.NR, cause);
            }
            EventType e = EventType.REMOVENODE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    /**
     * Enforce the node x to belong to any partial subgraph
     *
     * @param x     node's index
     * @param cause algorithm which is related to the modification
     * @return true iff the node is effectively added to the mandatory structure
     */
    public boolean enforceNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        if (envelop.getActiveNodes().contain(x)) {
            if (kernel.activateNode(x)) {
                if (reactOnModification) {
                    delta.add(x, IGraphDelta.NE, cause);
                }
                EventType e = EventType.ENFORCENODE;
                notifyPropagators(e, cause);
                return true;
            }
            return false;
        }
        this.contradiction(cause, EventType.ENFORCENODE, "enforce node which is not in the domain");
        return true;
    }

    /**
     * Remove node y from the neighborhood of node x from the maximal partial subgraph
     *
     * @param x     node's index
     * @param y     node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the removal has an effect
     * @throws ContradictionException
     */
    public abstract boolean removeArc(int x, int y, ICause cause) throws ContradictionException;

    /**
     * Enforce the node y into the neighborhood of node x in any partial subgraph
     *
     * @param x     node's index
     * @param y     node's index
     * @param cause algorithm which is related to the removal
     * @return true iff the node y is effectively added in the neighborhooh of node x
     */
    public abstract boolean enforceArc(int x, int y, ICause cause) throws ContradictionException;

    //***********************************************************************************
    // ACCESSORS
    //***********************************************************************************

    /**
     * Compute the order of the graph in its current state (ie the number of nodes that may belong to an instantiation)
     *
     * @return the number of nodes that may belong to an instantiation
     */
    public int getEnvelopOrder() {
        return envelop.getActiveNodes().getSize();
    }

    /**
     * Compute the order of the graph in its final state (ie the minimum number of nodes that necessarily belong to any instantiation)
     *
     * @return the minimum number of nodes that necessarily belong to any instantiation
     */
    public int getKernelOrder() {
        return kernel.getActiveNodes().getSize();
    }

    /**
     * @return the graph representing the domain of the variable graph
     */
    public E getKernelGraph() {
        return kernel;
    }

    /**
     * @return the graph representing the instantiated values (nodes and edges) of the variable graph
     */
    public E getEnvelopGraph() {
        return envelop;
    }

    /**
     * @return true iff the graph is directed
     */
    public abstract boolean isDirected();

    //***********************************************************************************
    // VARIABLE STUFF
    //***********************************************************************************

    @Override
    public void explain(VariableState what, Explanation to) {
        throw new UnsupportedOperationException("GraphVar does not (yet) implement method explain(...)");
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        throw new UnsupportedOperationException("GraphVar does not (yet) implement method explain(...)");
    }

    @Override
    public IGraphDelta getDelta() {
        return delta;
    }

    @Override
    public int getTypeAndKind() {
        return VAR + GRAPH;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph_var " + getName());
        sb.append("\ninstantiated = " + instantiated() + "\n");
        sb.append("\nenvelope graph \n");
        sb.append(envelop.toString());
        sb.append("\nkernel graph \n");
        sb.append(kernel.toString());
        return getName();
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new GraphDelta(solver.getSearchLoop());
        }
    }

    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new GraphDeltaMonitor(delta, propagator);
    }

    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event, cause);
        if ((modificationEvents & event.mask) != 0) {
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

    public void notifyMonitors(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event, cause);
        }
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        assert cause != null;
        solver.getEngine().fails(cause, this, message);
    }

    //***********************************************************************************
    // SOLUTIONS : STORE AND RESTORE
    //***********************************************************************************

    /**
     * @return the value of the graph variable represented through an adjacency matrix
     *         plus a set of nodes (last line of value).
     *         This method is not supposed to be used except for restoring solutions.
     */
    public boolean[][] getValue() {
        int n = getEnvelopGraph().getNbNodes();
        boolean[][] vals = new boolean[n + 1][n];
        ISet kerNodes = getKernelGraph().getActiveNodes();
        ISet kerSuccs;
        for (int i = kerNodes.getFirstElement(); i >= 0; i = kerNodes.getNextElement()) {
            kerSuccs = getKernelGraph().getSuccsOrNeigh(i);
            for (int j = kerSuccs.getFirstElement(); j >= 0; j = kerSuccs.getNextElement()) {
                vals[i][j] = true; // arc in
            }
            vals[n][i] = true; // node in
        }
        return vals;
    }

    /**
     * Instantiates <code>this</code> to value which represents an adjacency
     * matrix plus a set of nodes (last line of value).
     * This method is not supposed to be used except for restoring solutions.
     *
     * @param value value of <code>this</code>
     * @param cause
     * @throws ContradictionException
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
                if (value[i][j]) {//arcs
                    enforceArc(i, j, cause);
                } else {
                    removeArc(i, j, cause);
                }
            }
        }
    }
}
