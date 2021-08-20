/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph.undirected;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.delta.GraphViewDeltaMonitor;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetIntersection;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * @author Dimitri Justeau-Allaire
 * @since 26/05/2021
 */
public class UndirectedGraphUnionView extends UndirectedGraphView<UndirectedGraphVar> {

    protected UndirectedGraph lb;
    protected UndirectedGraph ub;
    protected ISet nodesToEnforce;
    protected ISet[] edgesToEnforce;

    public UndirectedGraphUnionView(String name, UndirectedGraphVar... graphs) {
        super(name, graphs);
        UndirectedGraph[] LBs = new UndirectedGraph[graphs.length];
        UndirectedGraph[] UBs = new UndirectedGraph[graphs.length];
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
        this.lb = new UndirectedGraph(getModel(), nodesToEnforce, edgesToEnforce, LBs);
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        if (nodesToEnforce.contains(node)) {
            contradiction(this, "Try to remove mandatory node");
        }
        boolean b = false;
        for (UndirectedGraphVar g : variables) {
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
        for (UndirectedGraphVar g : variables) {
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
    public UndirectedGraph getLB() {
        return lb;
    }

    @Override
    public UndirectedGraph getUB() {
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
        return new GraphUnionDeltaMonitor(deltaMonitors);
    }

    class GraphUnionDeltaMonitor extends GraphViewDeltaMonitor {

        ISet[] nodesAdded;
        ISet[][] edgesAdded;
        ISet[] nodesRemoved;
        ISet[][] edgesRemoved;
        ISet addNode;
        ISet removeNode;
        ISet[] addEdge;
        ISet[] removeEdge;

        public GraphUnionDeltaMonitor(IGraphDeltaMonitor... deltaMonitors) {
            super(deltaMonitors);
            // Init nodes structs
            nodesAdded = new ISet[deltaMonitors.length];
            nodesRemoved = new ISet[deltaMonitors.length];
            for (int i = 0; i < deltaMonitors.length; i++) {
                nodesAdded[i] = SetFactory.makeSmallBipartiteSet();
                nodesRemoved[i] = SetFactory.makeSmallBipartiteSet();
            }
            addNode = new SetUnion(nodesAdded);
            removeNode = new SetIntersection(nodesRemoved);
            // Init edges structs
            edgesAdded = new ISet[getNbMaxNodes()][];
            edgesRemoved = new ISet[getNbMaxNodes()][];
            for (int i = 0; i < getNbMaxNodes(); i++) {
                edgesAdded[i] = new ISet[deltaMonitors.length];
                edgesRemoved[i] = new ISet[deltaMonitors.length];
                for (int j = 0; j < deltaMonitors.length; j++) {
                    edgesAdded[i][j] = SetFactory.makeSmallBipartiteSet();
                    edgesRemoved[i][j] = SetFactory.makeSmallBipartiteSet();
                }
            }
            addEdge = new ISet[getNbMaxNodes()];
            removeEdge = new ISet[getNbMaxNodes()];
            for (int i = 0; i < getNbMaxNodes(); i++) {
                addEdge[i] = new SetUnion(edgesAdded[i]);
                removeEdge[i] = new SetIntersection(edgesRemoved[i]);
            }
        }

        protected void fillAddNodes() throws ContradictionException {
            for (int i = 0; i < deltaMonitors.length; i++) {
                int finalI = i;
                nodesAdded[i].clear();
                deltaMonitors[i].forEachNode(x -> nodesAdded[finalI].add(x), GraphEventType.ADD_NODE);
            }
        }

        protected void fillRemoveNodes() throws ContradictionException {
            for (int i = 0; i < deltaMonitors.length; i++) {
                int finalI = i;
                nodesRemoved[i].clear();
                deltaMonitors[i].forEachNode(x -> nodesRemoved[finalI].add(x), GraphEventType.REMOVE_NODE);
            }
        }

        protected void fillAddEdges() throws ContradictionException {
            for (int i = 0; i < deltaMonitors.length; i++) {
                int finalI = i;
                for (int j = 0; j < getNbMaxNodes(); j++) {
                    edgesAdded[j][i].clear();
                }
                deltaMonitors[i].forEachEdge((from, to) -> edgesAdded[from][finalI].add(to), GraphEventType.ADD_EDGE);
            }
        }

        protected void fillRemoveEdges() throws ContradictionException {
            for (int i = 0; i < deltaMonitors.length; i++) {
                int finalI = i;
                for (int j = 0; j < getNbMaxNodes(); j++) {
                    edgesRemoved[j][i].clear();
                }
                deltaMonitors[i].forEachEdge((from, to) -> edgesRemoved[from][finalI].add(to), GraphEventType.REMOVE_EDGE);
            }
        }

        @Override
        public void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException {
            if (evt == GraphEventType.ADD_NODE) {
                fillAddNodes();
                for (int v : addNode) {
                    proc.execute(v);
                }
            } else if (evt == GraphEventType.REMOVE_NODE) {
                fillRemoveNodes();
                for (int v : removeNode) {
                    proc.execute(v);
                }
            }
        }

        @Override
        public void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException {
            if (evt == GraphEventType.ADD_EDGE) {
                fillAddEdges();
                for (int from = 0; from < getNbMaxNodes(); from++) {
                    for (int to : addEdge[from]) {
                        proc.execute(from, to);
                    }
                }
            } else if (evt == GraphEventType.REMOVE_EDGE) {
                fillRemoveEdges();
                for (int from = 0; from < getNbMaxNodes(); from++) {
                    for (int to : removeEdge[from]) {
                        proc.execute(from, to);
                    }
                }
            }
        }
    }
}
