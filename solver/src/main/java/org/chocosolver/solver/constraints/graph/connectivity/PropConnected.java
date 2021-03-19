/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import gnu.trove.list.array.TIntArrayList;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.UGVarConnectivityHelper;
import org.chocosolver.util.objects.setDataStructures.ISet;

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
            for (int o = visited.nextClearBit(0); o < n; o = visited.nextClearBit(o + 1)) {
                g.removeNode(o, this);
            }

            if (g.getMandatoryNodes().size() > 1) {

                helper.findMandatoryArticulationPointsAndBridges();

                // 2 --- enforce articulation points that link two mandatory nodes
                for(int ap:helper.getArticulationPoints()){
                    g.enforceNode(ap, this);
                }

                // 3 --- enforce isthma that link two mandatory nodes (current version is bugged)
//				ISet mNodes = g.getMandatoryNodes();
//				TIntArrayList brI = helper.getBridgeFrom();
//				TIntArrayList brJ = helper.getBridgeTo();
//				for(int k=0; k<brI.size(); k++){
//					int i = brI.get(k);
//					int j = brJ.get(k);
//					if(mNodes.contains(i) && mNodes.contains(j)){
//						g.enforceEdge(i, j, this);
//					}
//				}
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
