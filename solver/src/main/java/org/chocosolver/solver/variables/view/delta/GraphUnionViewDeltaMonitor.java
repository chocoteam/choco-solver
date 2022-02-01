/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.delta;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.view.GraphView;
import org.chocosolver.solver.variables.view.graph.directed.DirectedGraphUnionView;
import org.chocosolver.solver.variables.view.graph.undirected.UndirectedGraphUnionView;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDifference;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

public class GraphUnionViewDeltaMonitor extends GraphViewDeltaMonitor {

    GraphView<?,?> g;
    ISet[] nodesAdded;
    ISet[][] edgesAdded;
    ISet[] nodesRemoved;
    ISet[][] edgesRemoved;
    ISet addedNode;
    ISet addNode;
    ISet removeNode;
    ISet[] addedEdge;
    ISet[] addEdge;
    ISet[] removeEdge;

    public GraphUnionViewDeltaMonitor(GraphView<?,?> graphUnionView, IGraphDeltaMonitor... deltaMonitors) {
        super(deltaMonitors);
        assert graphUnionView instanceof UndirectedGraphUnionView || graphUnionView instanceof DirectedGraphUnionView;
        g = graphUnionView;
        // Init nodes structs
        nodesAdded = new ISet[deltaMonitors.length];
        nodesRemoved = new ISet[deltaMonitors.length];
        for (int i = 0; i < deltaMonitors.length; i++) {
            nodesAdded[i] = SetFactory.makeSmallBipartiteSet();
            nodesRemoved[i] = SetFactory.makeSmallBipartiteSet();
        }
        addedNode = SetFactory.makeStoredSet(SetType.BITSET, 0, g.getModel());
        addNode = new SetDifference(new SetUnion(nodesAdded), addedNode);
        removeNode = new SetUnion(nodesRemoved);
        // Init edges structs
        edgesAdded = new ISet[g.getNbMaxNodes()][];
        edgesRemoved = new ISet[g.getNbMaxNodes()][];
        for (int i = 0; i < g.getNbMaxNodes(); i++) {
            edgesAdded[i] = new ISet[deltaMonitors.length];
            edgesRemoved[i] = new ISet[deltaMonitors.length];
            for (int j = 0; j < deltaMonitors.length; j++) {
                edgesAdded[i][j] = SetFactory.makeSmallBipartiteSet();
                edgesRemoved[i][j] = SetFactory.makeSmallBipartiteSet();
            }
        }
        addedEdge = new ISet[g.getNbMaxNodes()];
        addEdge = new ISet[g.getNbMaxNodes()];
        removeEdge = new ISet[g.getNbMaxNodes()];
        for (int i = 0; i < g.getNbMaxNodes(); i++) {
            addedEdge[i] = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, g.getModel());
            addEdge[i] = new SetDifference(new SetUnion(edgesAdded[i]), addedEdge[i]);
            removeEdge[i] = new SetUnion(edgesRemoved[i]);
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
            for (int j = 0; j < g.getNbMaxNodes(); j++) {
                edgesAdded[j][i].clear();
            }
            deltaMonitors[i].forEachEdge((from, to) -> edgesAdded[from][finalI].add(to), GraphEventType.ADD_EDGE);
        }
    }

    protected void fillRemoveEdges() throws ContradictionException {
        for (int i = 0; i < deltaMonitors.length; i++) {
            int finalI = i;
            for (int j = 0; j < g.getNbMaxNodes(); j++) {
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
            for (int v : addNode) {
                addedNode.add(v);
            }
        } else if (evt == GraphEventType.REMOVE_NODE) {
            fillRemoveNodes();
            for (int v : removeNode) {
                if (!g.getPotentialNodes().contains(v)) {
                    proc.execute(v);
                }
            }
        }
    }

    @Override
    public void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException {
        if (evt == GraphEventType.ADD_EDGE) {
            fillAddEdges();
            for (int from = 0; from < g.getNbMaxNodes(); from++) {
                for (int to : addEdge[from]) {
                    proc.execute(from, to);
                }
                for (int to : addEdge[from]) {
                    addedEdge[from].add(to);
                }
            }
        } else if (evt == GraphEventType.REMOVE_EDGE) {
            fillRemoveEdges();
            for (int from = 0; from < g.getNbMaxNodes(); from++) {
                for (int to : removeEdge[from]) {
                    if (!g.getUB().containsEdge(from, to)) {
                        proc.execute(from, to);
                    }
                }
            }
        }
    }
}