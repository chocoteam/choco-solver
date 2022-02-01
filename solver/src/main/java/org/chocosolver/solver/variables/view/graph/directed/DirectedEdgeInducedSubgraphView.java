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

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.delta.GraphViewDeltaMonitor;
import org.chocosolver.solver.variables.view.graph.DirectedGraphView;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * EDGE INDUCED DIRECTED SUBGRAPH VIEWS:
 *
 * Construct an edge-induced directed graph view G = (V', E') from G = (V, E) such that:
 *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
 *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
 *
 * with edges a fixed set of edges.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class DirectedEdgeInducedSubgraphView extends DirectedGraphView<DirectedGraphVar> {

    protected DirectedGraph lb;
    protected DirectedGraph ub;

    protected DirectedGraphVar graphVar;
    protected boolean exclude;
    protected ISet enforceNodes;
    protected ISet LBnodes;
    protected ISet[] successors;

    /**
     * Construct an edge-induced directed graph view G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param name name of the view
     * @param graphVar the graph to construct the view
     * @param edges the set of edges (array of couples) to construct the subgraph view from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public DirectedEdgeInducedSubgraphView(String name, DirectedGraphVar graphVar, int[][] edges, boolean exclude) {
        super(name, new DirectedGraphVar[] {graphVar});
        this.enforceNodes = SetFactory.makeStoredSet(SetType.BITSET, 0, getModel());
        this.exclude = exclude;
        this.graphVar = graphVar;
        this.successors = DirectedGraph.edgesArrayToSuccessorsSets(getNbMaxNodes(), edges);
        this.lb = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getLB(), graphVar.getUB(), edges, exclude);
        this.ub = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getUB(), graphVar.getUB(), edges, exclude);
        this.LBnodes = new SetUnion(getModel(), this.lb.getNodes(), enforceNodes);
    }

    @Override
    public ISet getMandatoryNodes() {
        return this.LBnodes;
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
        return graphVar.getNbMaxNodes();
    }

    @Override
    public boolean isDirected() {
        return graphVar.isDirected();
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        for (int i : getPotentialPredecessorOf(node)) {
            doRemoveEdge(i, node);
        }
        for (int i : getPotentialSuccessorsOf(node)) {
            doRemoveEdge(node, i);
        }
        return !getPotentialNodes().contains(node);
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        boolean b = graphVar.enforceNode(node, this);
        if (!getLB().getNodes().contains(node)) {
            ISet potPred = getPotentialPredecessorOf(node);
            ISet potSucc = getPotentialSuccessorsOf(node);
            if (potPred.size() == 0 && potSucc.size() == 1) {
                b = graphVar.enforceEdge(node, potSucc.newIterator().nextInt(), this) || b;
            } else if (potPred.size() == 1 && potSucc.size() == 0) {
                b = graphVar.enforceEdge(potPred.newIterator().nextInt(), node, this) || b;
            } else {
                enforceNodes.add(node);
            }
        }
        return b;
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        return graphVar.removeEdge(from, to, this);
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        return graphVar.enforceEdge(from, to, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        // Edge addition in observed can cause node addition
        if ((event.getMask() & GraphEventType.ADD_EDGE.getMask()) > 0) {
            notifyPropagators(GraphEventType.ADD_NODE, this);
        }
        // Edge removal in observed variable can cause node removal
        if ((event.getMask() & GraphEventType.REMOVE_EDGE.getMask()) > 0){
            for (int node : enforceNodes) {
                ISet potPred = getPotentialPredecessorOf(node);
                ISet potSucc = getPotentialSuccessorsOf(node);
                if (potPred.size() == 0 && potSucc.size() == 1) {
                    graphVar.enforceEdge(node, potSucc.newIterator().nextInt(), this);
                    enforceNodes.remove(node);
                } else if (potPred.size() == 1 && potSucc.size() == 0) {
                    graphVar.enforceEdge(potPred.newIterator().nextInt(), node, this);
                    enforceNodes.remove(node);
                }
            }
            notifyPropagators(GraphEventType.REMOVE_NODE, this);
        }
        notifyPropagators(event, this);
    }

    @Override
    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        return new DirectedEdgeInducedSubgraphMonitor(this, graphVar.monitorDelta(propagator));
    }

    static class DirectedEdgeInducedSubgraphMonitor extends GraphViewDeltaMonitor {

        TIntIntHashMap nodes;
        DirectedEdgeInducedSubgraphView g;
        PairProcedure filter;

        DirectedEdgeInducedSubgraphMonitor(DirectedEdgeInducedSubgraphView g, IGraphDeltaMonitor... deltaMonitors) {
            super(deltaMonitors);
            this.g = g;
            nodes = new TIntIntHashMap(8);
            filter = (from, to) -> { // Count the edges effectively impacted in the view
                if ((g.exclude && !g.successors[from].contains(to)) || (!g.exclude && g.successors[from].contains(to))) {
                    if (!nodes.containsKey(from)) {
                        nodes.put(from, 1);
                    } else {
                        nodes.put(from, nodes.get(from) + 1);
                    }
                    if (!nodes.containsKey(to)) {
                        nodes.put(to, 1);
                    } else {
                        nodes.put(to, nodes.get(to) + 1);
                    }
                }
            };
        }

        @Override
        public void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException {
            nodes.clear();
            deltaMonitors[0].forEachEdge(filter, evt == GraphEventType.ADD_NODE ? GraphEventType.ADD_EDGE : GraphEventType.REMOVE_EDGE);
            if (evt == GraphEventType.ADD_NODE) {
                // A node is added iff all of its neighbors were added in the delta
                for (int node : nodes.keys()) {
                    if (nodes.get(node) == (g.getMandatoryPredecessorsOf(node).size() + g.getMandatorySuccessorsOf(node).size())) {
                        proc.execute(node);
                    }
                }
            } else if (evt == GraphEventType.REMOVE_NODE) {
                // A node is removed iff it had a neighbor removed and is not any more in the view
                for (int node : nodes.keys()) {
                    if (!g.getPotentialNodes().contains(node)) {
                        proc.execute(node);
                    }
                }
            }
        }
        @Override
        public void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException {
            deltaMonitors[0].forEachEdge((from, to) -> {
                if ((g.exclude && !g.successors[from].contains(to)) || (!g.exclude && g.successors[from].contains(to))) {
                    proc.execute(from, to);
                }
            }, evt);
        }
    }
}
