/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph.directed;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.delta.GraphUnionViewDeltaMonitor;
import org.chocosolver.solver.variables.view.graph.DirectedGraphView;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * @author Dimitri Justeau-Allaire
 * @since 26/05/2021
 */
public class DirectedGraphUnionView extends DirectedGraphView<DirectedGraphVar> {

    protected DirectedGraph lb;
    protected DirectedGraph ub;
    protected ISet nodesToEnforce;
    protected ISet[] edgesToEnforce;

    public DirectedGraphUnionView(String name, DirectedGraphVar... graphs) {
        super(name, graphs);
        DirectedGraph[] LBs = new DirectedGraph[graphs.length];
        DirectedGraph[] UBs = new DirectedGraph[graphs.length];
        for (int i = 0; i < graphs.length; i++) {
            LBs[i] = graphs[i].getLB();
            UBs[i] = graphs[i].getUB();
        }
        this.ub = GraphFactory.makeUnionGraph(getModel(), UBs);
        this.nodesToEnforce = SetFactory.makeStoredSet(SetType.BITSET, 0, getModel());
        this.edgesToEnforce = new ISet[getNbMaxNodes()];
        for (int i = 0; i < getNbMaxNodes(); i++) {
            this.edgesToEnforce[i] = SetFactory.makeStoredSet(SetType.BITSET, 0, getModel());
        }
        this.lb = new DirectedGraph(getModel(), nodesToEnforce, edgesToEnforce, LBs);

    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        if (nodesToEnforce.contains(node)) {
            contradiction(this, "Try to remove mandatory node");
        }
        boolean b = false;
        for (DirectedGraphVar g : variables) {
            b |= g.removeNode(node, this);
        }
        return b;
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        int nb = 0;
        int idx = -1;
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getPotentialNodes().contains(node)) {
                nb++;
                idx = i;
                if (nb > 1) {
                    break;
                }
            }
        }
        if (nb == 1) {
            variables[idx].enforceNode(node, this);
            return true;
        } else {
            nodesToEnforce.add(node);
        }
        return false;
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        if (edgesToEnforce[from].contains(to)) {
            contradiction(this, "Try to remove mandatory edge");
        }
        boolean b = false;
        for (DirectedGraphVar g : variables) {
            b |= g.removeEdge(from, to, this);
        }
        return b;
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        int nb = 0;
        int idx = -1;
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getUB().containsEdge(from, to)) {
                nb++;
                idx = i;
                if (nb > 1) {
                    break;
                }
            }
        }
        if (nb == 1) {
            variables[idx].enforceEdge(from, to, this);
            return true;
        } else {
            edgesToEnforce[from].add(to);
        }
        return false;
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        // Check if previous multiple support removed node can be effectively removed
        if ((event.getMask() & GraphEventType.REMOVE_NODE.getMask()) > 0) {
            for (int i : nodesToEnforce) {
                if (doEnforceNode(i)) {
                    nodesToEnforce.remove(i);
                    break;
                }
            }
        }
        if ((event.getMask() & GraphEventType.REMOVE_EDGE.getMask()) > 0) {
            boolean b = false;
            for (int i = 0; i < edgesToEnforce.length; i++) {
                for (int j : edgesToEnforce[i]) {
                    if (doEnforceEdge(i, j)) {
                        edgesToEnforce[i].remove(j);
                        b = true;
                        break;
                    }
                }
                if (b) {
                    break;
                }
            }
        }
        notifyPropagators(event, this);
    }

    @Override
    public DirectedGraph getLB() {
        return lb;
    }

    @Override
    public DirectedGraph getUB() {
        return ub;
    }

    @Override
    public int getNbMaxNodes() {
        return ub.getNbMaxNodes();
    }

    @Override
    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        IGraphDeltaMonitor[] deltaMonitors = new IGraphDeltaMonitor[variables.length];
        for (int i = 0; i < variables.length; i++) {
            deltaMonitors[i] = variables[i].monitorDelta(propagator);
        }
        return new GraphUnionViewDeltaMonitor(this, deltaMonitors);
    }
}
