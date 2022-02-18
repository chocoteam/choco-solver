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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.UGVarConnectivityHelper;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Propagator that ensures that the final graph consists in K Connected Components (CC)
 * <p/>
 * complete filtering in linear time
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbCC extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar g;
    private final IntVar k;
    private final UGVarConnectivityHelper helper;
    private final BitSet visitedMin, visitedMax;
    private final int[] fifo, ccOf;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbCC(UndirectedGraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, false);
        this.g = graph;
        this.k = k;
        this.helper = new UGVarConnectivityHelper(g);
        this.visitedMin = new BitSet(g.getNbMaxNodes());
        this.visitedMax = new BitSet(g.getNbMaxNodes());
        this.fifo = new int[g.getNbMaxNodes()];
        this.ccOf = new int[g.getNbMaxNodes()];
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {

        // trivial case
        k.updateBounds(0, g.getPotentialNodes().size(), this);
        if (k.getUB() == 0) {
            for (int i : g.getPotentialNodes()) g.removeNode(i, this);
            return;
        }

        // bound computation
        int min = minCC();
        int max = maxCC();
        k.updateLowerBound(min, this);
        k.updateUpperBound(max, this);

        // The number of CC cannot increase :
        // - remove unreachable nodes
        // - force articulation points and bridges
        if(min != max) {
            if (k.getUB() == min) {

                // 1 --- remove unreachable nodes
                int n = g.getNbMaxNodes();
                for (int o : g.getPotentialNodes()) {
                    if (!visitedMin.get(o)) {
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
            // a maximal number of CC is required : remaining nodes will be singleton
            else if(k.getLB() == max){
                // --- transform every potential node into a mandatory isolated node
                ISet mNodes = g.getMandatoryNodes();
                for(int i:g.getPotentialNodes()){
                    if(!mNodes.contains(i)){
                        for(int j:g.getPotentialNeighborsOf(i)){
                            g.removeEdge(i,j,this);
                        }
                        g.enforceNode(i,this);
                    }
                }
                // --- remove edges between mandatory nodes that would merge 2 CC
                // note that it can happen that 2 mandatory node already belong to the same CC
                // if so the edge should not be filtered
                for(int i:g.getPotentialNodes()){
                    for(int j:g.getPotentialNeighborsOf(i)){
                        if(ccOf[i] != ccOf[j]) {
                            g.removeEdge(i,j,this);
                        }
                    }
                }
            }
        }
    }

    private int minCC() {
        int min = 0;
        visitedMin.clear();
        for (int i : g.getMandatoryNodes()) {
            if (!visitedMin.get(i)) {
                helper.exploreFrom(i, visitedMin);
                min++;
            }
        }
        return min;
    }

    private int maxCC() {
        int nbK = 0;
        visitedMax.clear();
        for(int i:g.getMandatoryNodes()) {
            if(!visitedMax.get(i)) {
                exploreLBFrom(i, visitedMax);
                nbK++;
            }
        }
        int delta = g.getPotentialNodes().size() - g.getMandatoryNodes().size();
        return nbK + delta;
    }

    private void exploreLBFrom(int root, BitSet visited) {
        int first = 0;
        int last = 0;
        int i = root;
        fifo[last++] = i;
        visited.set(i);
        ccOf[i] = root; // mark cc of explored node
        while (first < last) {
            i = fifo[first++];
            for (int j : g.getMandatoryNeighborsOf(i)) { // mandatory edges only
                if (!visited.get(j)) {
                    visited.set(j);
                    ccOf[j] = root; // mark cc of explored node
                    fifo[last++] = j;
                }
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        if (k.getUB() < minCC() || k.getLB() > maxCC()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
