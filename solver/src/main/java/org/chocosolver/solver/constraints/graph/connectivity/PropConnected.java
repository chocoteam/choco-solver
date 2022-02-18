/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.UGVarConnectivityHelper;

import java.util.BitSet;

/**
 * Propagator checking that the graph is connected
 * (Allows graphs with 0 or 1 nodes)
 * Complete Filtering
 *
 * @author Jean-Guillaume Fages
 */
public class PropConnected extends Propagator<UndirectedGraphVar> {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final UndirectedGraphVar g;
    private final BitSet visited;
    private final UGVarConnectivityHelper helper;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropConnected(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.n = graph.getNbMaxNodes();
        this.visited = new BitSet(n);
        this.helper = new UGVarConnectivityHelper(g);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.REMOVE_EDGE.getMask() + GraphEventType.ADD_NODE.getMask() + GraphEventType.REMOVE_NODE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // 0-node or 1-node graphs are accepted
        if (g.getPotentialNodes().size() <= 1) {
            setPassive();
            return;
        }
        // cannot filter if no mandatory node
        if (g.getMandatoryNodes().size() > 0) {

            // 1 --- explore the graph from the first mandatory node and
            // remove unreachable nodes (fail if mandatory node is not reached)
            visited.clear();
            int root = g.getMandatoryNodes().iterator().next();
            helper.exploreFrom(root, visited);
            for (int o : g.getPotentialNodes()) {
                if (!visited.get(o)) {
                    g.removeNode(o, this);
                }
            }

            // 2 --- enforce articulation points and bridges that link two mandatory nodes
            helper.computeMandatoryArticulationPointsAndBridges();
            for(int ap:helper.getArticulationPoints()) {
                g.enforceNode(ap, this);
            }
            for(int[] bridge:helper.getBridges()) {
                g.enforceEdge(bridge[0], bridge[1], this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        // 0-node or 1-node graphs are accepted
        if (g.getPotentialNodes().size() <= 1) {
            return ESat.TRUE;
        }
        // cannot conclude if less than 2 mandatory nodes
        if (g.getMandatoryNodes().size() < 2) {
            return ESat.UNDEFINED;
        }
        // BFS from a mandatory node
        visited.clear();
        int root = g.getMandatoryNodes().iterator().next();
        helper.exploreFrom(root, visited);
        // every mandatory node is reached?
        for (int i : g.getMandatoryNodes()) {
            if (!visited.get(i)) {
                return ESat.FALSE;
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
