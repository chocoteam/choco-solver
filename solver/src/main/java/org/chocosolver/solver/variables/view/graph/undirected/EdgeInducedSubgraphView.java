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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

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
        this.enforceNodes = SetFactory.makeStoredSet(SetType.BITSET, 0, getModel());
        this.lb = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getLB(), edges, exclude);
        this.ub = GraphFactory.makeEdgeInducedSubgraph(getModel(), graphVar.getUB(), edges, exclude);
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
    public boolean isInstantiated() {
        if (getPotentialNodes().size() != getMandatoryNodes().size()) {
            return false;
        }
        ISet suc;
        for (int i : getUB().getNodes()) {
            suc = getPotentialNeighborsOf(i);
            if (suc.size() != getLB().getNeighborsOf(i).size()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean doRemoveNode(int node) throws ContradictionException {
        if (enforceNodes.contains(node)) {
            contradiction(this, "Try to remove mandatory node");
        }
        return true;
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        boolean b = graphVar.enforceNode(node, this);
        if (!getMandatoryNodes().contains(node)) {
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
    public int getTypeAndKind() {
        return Variable.NON_INJECTIVE_VIEW | Variable.GRAPH;
    }
}
