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
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.SubgraphType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * GENERIC CLASS FOR BACKTRACKABLE UNDIRECTED SUBGRAPHS VIEWS:
 *
 *  - EXCLUDE_NODES:
 *      Construct an undirected graph view G' = (V', E') from another graph G = (V, E) such that:
 *          V' = E \ nodes;
 *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
 *
 *  - INDUCED_BY_NODES:
 *      Construct an undirected graph view G = (V', E') from G = (V, E) such that:
 *          V' = V \cap nodes;
 *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SubgraphInducedByNodesView extends UndirectedGraphView<UndirectedGraphVar> {

    protected UndirectedGraph lb;
    protected UndirectedGraph ub;

    protected UndirectedGraphVar graphVar;
    protected SubgraphType type;
    protected ISet nodes;

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param graphVar observed variable
     */
    public SubgraphInducedByNodesView(String name, UndirectedGraphVar graphVar, ISet nodes, SubgraphType type) {
        super(name, new UndirectedGraphVar[] {graphVar});
        this.nodes = nodes;
        this.type = type;
        this.graphVar = graphVar;
        this.lb = GraphFactory.makeSubgraphInducedByNodes(getModel(), graphVar.getLB(), nodes, type);
        this.ub = GraphFactory.makeSubgraphInducedByNodes(getModel(), graphVar.getUB(), nodes, type);
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
        return graphVar.removeNode(node, this);
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        return graphVar.enforceNode(node, this);
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
        notifyPropagators(event, this);
    }
}
