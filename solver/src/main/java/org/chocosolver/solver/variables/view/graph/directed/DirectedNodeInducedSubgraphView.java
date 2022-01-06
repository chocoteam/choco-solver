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
import org.chocosolver.solver.variables.view.delta.GraphViewDeltaMonitor;
import org.chocosolver.solver.variables.view.graph.DirectedGraphView;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * NODE INDUCED DIRECTED SUBGRAPH VIEWS:
 *
 * Construct a directed graph view G' = (V', E') from another graph G = (V, E) such that:
 *          V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
 *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class DirectedNodeInducedSubgraphView extends DirectedGraphView<DirectedGraphVar> {

    protected DirectedGraph lb;
    protected DirectedGraph ub;

    protected DirectedGraphVar graphVar;
    protected boolean exclude;
    protected ISet nodes;

    /**
     * Creates a node induced directed subgraph view.
     *
     * @param name      name of the view
     * @param graphVar observed variable
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public DirectedNodeInducedSubgraphView(String name, DirectedGraphVar graphVar, ISet nodes, boolean exclude) {
        super(name, new DirectedGraphVar[] {graphVar});
        this.exclude = exclude;
        this.graphVar = graphVar;
        this.nodes = nodes;
        this.lb = GraphFactory.makeNodeInducedSubgraph(getModel(), graphVar.getLB(), graphVar.getUB(), nodes, exclude);
        this.ub = GraphFactory.makeNodeInducedSubgraph(getModel(), graphVar.getUB(), graphVar.getUB(),  nodes, exclude);
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
        // Node addition in observed variable can cause edge addition
        if ((event.getMask() & GraphEventType.ADD_NODE.getMask()) > 0) {
            notifyPropagators(GraphEventType.ADD_EDGE, this);
        }
        notifyPropagators(event, this);
    }

    @Override
    public IGraphDeltaMonitor monitorDelta(ICause propagator) {
        return new GraphViewDeltaMonitor(graphVar.monitorDelta(propagator)) {
            @Override
            public void forEachNode(IntProcedure proc, GraphEventType evt) throws ContradictionException {
                IntProcedure filter = i -> {
                    if (exclude && !nodes.contains(i)) {
                        proc.execute(i);
                    } else if (!exclude && nodes.contains(i)) {
                        proc.execute(i);
                    }
                };
                deltaMonitors[0].forEachNode(filter, evt);
            }
            @Override
            public void forEachEdge(PairProcedure proc, GraphEventType evt) throws ContradictionException {
                PairProcedure filter = (from, to) -> {
                    if (exclude && !nodes.contains(from) && !nodes.contains(to)) {
                        proc.execute(from, to);
                    } else if (!exclude && nodes.contains(from) && nodes.contains(to)) {
                        proc.execute(from, to);
                    }
                };
                deltaMonitors[0].forEachEdge(filter, evt);
            }
        };
    }
}
