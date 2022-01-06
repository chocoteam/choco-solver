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
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.ConnectivityFinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Propagator ensuring that the number of vertices of the largest connected is maxSizeCC
 * (cf. MAX_NCC graph property, http://www.emn.fr/x-info/sdemasse/gccat/sec2.2.2.4.html#uid922).
 *
 * Reference for bounds and filtering scheme: https://arxiv.org/abs/2105.00663v1
 *
 * @author Dimitri Justeau-Allaire
 */
public class PropSizeMaxCC extends Propagator<Variable> {

    /* Variables */

    private final UndirectedGraphVar g;
    private final IntVar sizeMaxCC;
    private final ConnectivityFinder GLBCCFinder;
    private final ConnectivityFinder GUBCCFinder;

    /* Constructor */

    public PropSizeMaxCC(UndirectedGraphVar graph, IntVar sizeMaxCC) {
        super(new Variable[]{graph, sizeMaxCC}, PropagatorPriority.QUADRATIC, false);
        this.g = graph;
        this.sizeMaxCC = sizeMaxCC;
        this.GLBCCFinder = new ConnectivityFinder(g.getLB());
        this.GUBCCFinder = new ConnectivityFinder(g.getUB());
    }

    /* Methods */

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ALL_EVENTS;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Find CCs and their sizes
        this.GLBCCFinder.findAllCC();
        this.GUBCCFinder.findAllCC();
        int nbCC_GLB = GLBCCFinder.getNBCC();
        // Retrieve MAX_NCC(g) lower and upper bounds from g
        int maxNCC_LB = GLBCCFinder.getSizeMaxCC();
        int maxNCC_UB = GUBCCFinder.getSizeMaxCC();
        // 1. Trivial case
        if (sizeMaxCC.getLB() > g.getUB().getNodes().size()) {
            fails();
        }
        // 2. Trivial case TODO: How to properly get |E_TU| ?
        // 3.
        if (maxNCC_UB < sizeMaxCC.getLB()) {
            fails();
        }
        // 4.
        if (maxNCC_LB > sizeMaxCC.getUB()) {
            fails();
        }
        // 5.
        sizeMaxCC.updateLowerBound(maxNCC_LB, this);
        // 6.
        sizeMaxCC.updateUpperBound(maxNCC_UB, this);
        // 7.
        if (maxNCC_UB > sizeMaxCC.getUB()) {
            boolean recomputeMaxNCC_UB = false;
            // a.
            if (sizeMaxCC.getUB() == 1) {
                for (int i : g.getPotentialNodes()) {
                    for (int j : g.getPotentialNeighborsOf(i)) {
                        g.removeEdge(i, j, this);
                    }
                }
            }
            // b.
            if (sizeMaxCC.getUB() == 0) {
                for (int i : g.getPotentialNodes()) {
                    g.removeNode(i, this);
                }
            }
            for (int cc = 0; cc < nbCC_GLB; cc++) {
                int[] sizeCC = GLBCCFinder.getSizeCC();
                // c.
                if (sizeCC[cc] == sizeMaxCC.getUB()) {
                    Map<Integer, Set<Integer>> ccPotentialNeighbors = getGLBCCPotentialNeighbors(cc);
                    for (int i : ccPotentialNeighbors.keySet()) {
                        for (int j : ccPotentialNeighbors.get(i)) {
                            g.removeEdge(i, j, this);
                        }
                    }
                } else {
                    // d.
                    for (int cc2 = cc + 1; cc2 < nbCC_GLB; cc2++) {
                        if (sizeCC[cc] + sizeCC[cc2] > sizeMaxCC.getUB()) {
                            Map<Integer, Set<Integer>> ccPotentialNeighbors = getGLBCCPotentialNeighbors(cc);
                            for (int i : ccPotentialNeighbors.keySet()) {
                                for (int j : ccPotentialNeighbors.get(i)) {
                                    if (getGLBCCNodes(cc2).contains(j)) {
                                        recomputeMaxNCC_UB = true;
                                        g.removeEdge(i, j, this);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // e.
            if (recomputeMaxNCC_UB) {
                this.GUBCCFinder.findAllCC();
                maxNCC_UB = GUBCCFinder.getSizeMaxCC();
                if (maxNCC_UB < sizeMaxCC.getLB()) {
                    fails();
                }
                if (sizeMaxCC.getUB() > maxNCC_UB) {
                    sizeMaxCC.updateUpperBound(maxNCC_UB, this);
                }
            }
        }
        // 8.
        int nb_candidates = 0;
        int candidate = -1;
        int size = 0;
        for (int cc = 0; cc < GUBCCFinder.getNBCC(); cc++) {
            int s = GUBCCFinder.getSizeCC()[cc];
            if (s >= sizeMaxCC.getLB()) {
                nb_candidates++;
                candidate = cc;
                size = s;
            }
            if (nb_candidates > 1) {
                break;
            }
        }
        if (nb_candidates == 1 && size == sizeMaxCC.getLB()) {
            int i = GUBCCFinder.getCCFirstNode()[candidate];
            while (i != -1) {
                g.enforceNode(i, this);
                i = GUBCCFinder.getCCNextNode()[i];
            }
            sizeMaxCC.instantiateTo(sizeMaxCC.getLB(), this);
        }
    }

    /**
     * Retrieve the nodes of a GLB CC.
     *
     * @param cc The GLB CC index.
     * @return The set of nodes of the GLB CC cc.
     */
    private Set<Integer> getGLBCCNodes(int cc) {
        Set<Integer> ccNodes = new HashSet<>();
        for (int i = GLBCCFinder.getCCFirstNode()[cc]; i >= 0; i = GLBCCFinder.getCCNextNode()[i]) {
            ccNodes.add(i);
        }
        return ccNodes;
    }

    /**
     * Retrieve the potential CC neighbors (i.e. not in the CC) of a GLB CC.
     *
     * @param cc The GLB CC index.
     * @return A map with frontier nodes of the CC as keys (Integer), and their potential neighbors that are
     * outside the CC (Set<Integer>). Only the frontier nodes that have at least one potential neighbor outside the
     * CC are stored in the map.
     * {
     * frontierNode1: {out-CC potential neighbors},
     * frontierNode3: {...},
     * ...
     * }
     */
    private Map<Integer, Set<Integer>> getGLBCCPotentialNeighbors(int cc) {
        Map<Integer, Set<Integer>> ccPotentialNeighbors = new HashMap<>();
        // Retrieve all nodes of CC
        Set<Integer> ccNodes = getGLBCCNodes(cc);
        // Retrieve neighbors of the nodes of CC that are outside the CC
        for (int i : ccNodes) {
            Set<Integer> outNeighbors = new HashSet<>();
            for (int j : g.getPotentialNeighborsOf(i)) {
                if (!ccNodes.contains(j)) {
                    outNeighbors.add(j);
                }
            }
            if (outNeighbors.size() > 0) {
                ccPotentialNeighbors.put(i, outNeighbors);
            }
        }
        return ccPotentialNeighbors;
    }


    @Override
    public ESat isEntailed() {
        // Find CCs and their sizes
        this.GLBCCFinder.findAllCC();
        this.GUBCCFinder.findAllCC();
        // Retrieve MAX_NCC(g) lower and upper bounds from g
        int maxNCC_LB = GLBCCFinder.getSizeMaxCC();
        int maxNCC_UB = GUBCCFinder.getSizeMaxCC();
        // Check entailment
        if (maxNCC_UB < sizeMaxCC.getLB() || maxNCC_LB > sizeMaxCC.getUB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            if (maxNCC_LB == sizeMaxCC.getValue()) {
                return ESat.TRUE;
            } else {
                return ESat.FALSE;
            }
        }
        return ESat.UNDEFINED;
    }
}
