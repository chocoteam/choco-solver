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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.graph.UndirectedGraphView;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

/**
 * NODE INDUCED UNDIRECTED SUBGRAPH VIEWS:
 *
 * Construct a graph view G' = (V', E') from another graph G = (V, E) such that:
 *          V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
 *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class NodeSetVarInducedSubgraphView extends UndirectedGraphView<Variable> {

    protected UndirectedGraph lb;
    protected UndirectedGraph ub;

    protected UndirectedGraphVar graphVar;
    protected SetVar setVar;
    protected boolean exclude;

    /**
     * Creates a node-induced subgraph view.
     *
     * @param name      name of the view
     * @param graphVar observed variable
     * @param nodes the set of nodes to construct the view from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public NodeSetVarInducedSubgraphView(String name, UndirectedGraphVar graphVar, SetVar nodes, boolean exclude) {
        super(name, new Variable[] {graphVar, nodes});
        this.exclude = exclude;
        this.graphVar = graphVar;
        for (int i : nodes.getLB()) {
            assert graphVar.getMandatoryNodes().contains(i);
        }
        for (int i : nodes.getUB())
        this.lb = GraphFactory.makeNodeInducedSubgraph(getModel(), graphVar.getLB(), nodes.getLB(), exclude);
        this.ub = GraphFactory.makeNodeInducedSubgraph(getModel(), graphVar.getUB(), nodes.getUB(), exclude);
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
        return setVar.remove(node, this);
    }

    @Override
    protected boolean doEnforceNode(int node) throws ContradictionException {
        return setVar.force(node, this);
    }

    @Override
    protected boolean doRemoveEdge(int from, int to) throws ContradictionException {
        if (setVar.getLB().contains(from) && setVar.getLB().contains(to)) {
            this.contradiction(this, "Remove mandatory edge");
        }
        if (setVar.getLB().contains(from) && !setVar.getLB().contains(to)) {
            return setVar.remove(to, this);
        }
        if (!setVar.getLB().contains(from) && setVar.getLB().contains(to)) {
            return setVar.remove(from, this);
        }
        return false;
    }

    @Override
    protected boolean doEnforceEdge(int from, int to) throws ContradictionException {
        return setVar.force(from, this) || setVar.force(to, this);
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        // Node addition in observed variable can cause edge addition
        if ((event.getMask() & GraphEventType.ADD_NODE.getMask()) > 0) {
            notifyPropagators(GraphEventType.ADD_EDGE, this);
        }
        notifyPropagators(event, this);
    }
}
