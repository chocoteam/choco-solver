/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph.undirected;

import gnu.trove.map.hash.TIntIntHashMap;
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
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * EDGE INDUCED UNDIRECTED SUBGRAPH VIEWS:
 *
 * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
 *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
 *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
 *
 * with edges a fixed set of edges.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class EdgeInducedSubgraphView extends UndirectedGraphView<UndirectedGraphVar> {

    protected UndirectedGraph lb;
    protected UndirectedGraph ub;

    protected UndirectedGraphVar graphVar;
    protected boolean exclude;
    protected ISet enforceNodes;
    protected ISet LBnodes;
    protected ISet[] edges;

    /**
     * Construct an edge-induced subgraph view G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * @param name      name of the view
     * @param graphVar observed variable
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public EdgeInducedSubgraphView(String name, UndirectedGraphVar graphVar, int[][] edges, boolean exclude) {
        super(name, new UndirectedGraphVar[] {graphVar});
        this.exclude = exclude;
        this.graphVar = graphVar;
        this.edges = UndirectedGraph.edgesArrayToEdgesSets(getNbMaxNodes(), edges);
        this.enforceNodes = SetFactory.makeStoredSet(SetType.BITSET, 0, getModel());
        this.lb = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getLB(), graphVar.getUB(), edges, exclude);
        this.ub = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getUB(), graphVar.getUB(), edges, exclude);
        this.LBnodes = new SetUnion(getModel(), this.lb.getNodes(), enforceNodes);
    }

    @Override
    public ISet getMandatoryNodes() {
        return this.LBnodes;
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
        return graphVar.getNbMaxNodes();
    }

    @Override
    public boolean isDirected() {
        return graphVar.isDirected();
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        for (int i : getPotentialNeighborsOf(node)) {
            doRemoveEdge(node, i);
        }
        return !getPotentialNodes().contains(node);
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        boolean b = graphVar.enforceNode(node, this);
        if (!getLB().getNodes().contains(node)) {
            ISet potNeigh = getPotentialNeighborsOf(node);
            if (potNeigh.size() == 1) {
                b = graphVar.enforceEdge(node, potNeigh.newIterator().nextInt(), this) || b;
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
                ISet potNeigh = getPotentialNeighborsOf(node);
                if (potNeigh.size() == 1) {
                    graphVar.enforceEdge(node, potNeigh.newIterator().nextInt(), this);
                    enforceNodes.remove(node);
                }
            }
            notifyPropagators(GraphEventType.REMOVE_NODE, this);
        }
        notifyPropagators(event, this);
    }

    @Override
    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        return new EdgeInducedSubgraphMonitor(this, graphVar.monitorDelta(propagator));
    }

    class EdgeInducedSubgraphMonitor extends GraphViewDeltaMonitor {

        TIntIntHashMap nodes;
        EdgeInducedSubgraphView g;
        PairProcedure filter;

        EdgeInducedSubgraphMonitor(EdgeInducedSubgraphView g, IGraphDeltaMonitor... deltaMonitors) {
            super(deltaMonitors);
            this.g = g;
            this.nodes = new TIntIntHashMap(8);
            this.filter = (from, to) -> { // Count the edges effectively impacted in the view
                if ((g.exclude && !g.edges[from].contains(to)) || (!g.exclude && g.edges[from].contains(to))) {
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
                    if (nodes.get(node) == g.getMandatoryNeighborsOf(node).size()) {
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
                if ((g.exclude && !g.edges[from].contains(to)) || (!g.exclude && g.edges[from].contains(to))) {
                    proc.execute(from, to);
                }
            }, evt);
        }
    }
}
