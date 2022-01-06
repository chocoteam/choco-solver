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
 * Propagator ensuring that the number of vertices of the smallest connected is equal to sizeMinCC
 * (cf. MIN_NCC graph property http://www.emn.fr/x-info/sdemasse/gccat/sec2.2.2.4.html#uid940).
 *
 * Reference for bounds and filtering scheme: https://arxiv.org/abs/2105.00663v1
 *
 * @author Dimitri Justeau-Allaire
 */
public class PropSizeMinCC extends Propagator<Variable> {

    /* Variables */

    private final UndirectedGraphVar g;
    private final IntVar sizeMinCC;
    private final ConnectivityFinder GLBCCFinder;
    private final ConnectivityFinder GUBCCFinder;

    /* Constructor */

    public PropSizeMinCC(UndirectedGraphVar graph, IntVar sizeMinCC) {
        super(new Variable[]{graph, sizeMinCC}, PropagatorPriority.QUADRATIC, false);
        this.g = graph;
        this.sizeMinCC = sizeMinCC;
        this.GLBCCFinder = new ConnectivityFinder(g.getLB());
        this.GUBCCFinder = new ConnectivityFinder(g.getUB());
    }

    /* Methods */

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ALL_EVENTS;
    }

    /**
     * @param nbNodesT The number of T-vertices.
     * @param nbNodesU The number of U-vertices.
     * @return The lower bound of the graph variable MIN_NCC property.
     * Beware that this.GLBCCFinder.findAllCC() and this.GLBCCFinder.findCCSizes() must be called before.
     */
    private int getLBMinNCC(int nbNodesT, int nbNodesU) {
        if (nbNodesT == 0) {
            return 0;
        } else {
            if (nbNodesU > 0) {
                return 1;
            } else {
                return this.GLBCCFinder.getSizeMinCC();
            }
        }
    }

    /**
     * @param nbNodesT The number of T-vertices.
     * @return The upper bound of the graph variable MIN_NCC property.
     * Beware that this.GUBCCFinder.findAllCC() and this.GUBCCFinder.findCCSizes() must be called before.
     */
    private int getUBMinNCC(int nbNodesT) {
        if (nbNodesT > 0) {
            return getGUBMandatoryCCs().stream().mapToInt(cc -> GUBCCFinder.getSizeCC()[cc]).min().getAsInt();
        } else {
            return this.GUBCCFinder.getSizeMaxCC();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Find CCs and their sizes
        this.GLBCCFinder.findAllCC();
        this.GUBCCFinder.findAllCC();
        // Compute |V_T|, |V_U| and |V_TU|
        int nbNodesT = g.getMandatoryNodes().size();
        int nbNodesTU = g.getPotentialNodes().size();
        int nbNodesU = nbNodesTU - nbNodesT;
        // Compute MIN_NCC(g) lower and upper bounds from g
        int minNCC_LB = getLBMinNCC(nbNodesT, nbNodesU);
        int minNCC_UB = getUBMinNCC(nbNodesT);
        // 1. Trivial case
        if (sizeMinCC.getLB() > nbNodesTU) {
            fails();
        }
        // 2. Trivial case TODO: How to properly get |E_TU| ?
        // 3.
        if (minNCC_UB < sizeMinCC.getLB()) {
            fails();
        }
        // 4.
        if (minNCC_LB > sizeMinCC.getUB()) {
            fails();
        }
        // 5.
        sizeMinCC.updateLowerBound(minNCC_LB, this);
        // 6.
        sizeMinCC.updateUpperBound(minNCC_UB, this);
        // 7.
        for (int cc : getGUBOptionalCCs()) {
            if (GUBCCFinder.getSizeCC()[cc] < sizeMinCC.getLB()) {
                int i = GUBCCFinder.getCCFirstNode()[cc];
                while (i != -1) {
                    g.removeNode(i, this);
                    i = GUBCCFinder.getCCNextNode()[i];
                }
            }
        }
        // 8.
        boolean recomputeMinNCC_LB = false;
        Set<Integer> GUBMandatoryCCs = getGUBMandatoryCCs();
        if (minNCC_LB < sizeMinCC.getLB()) {
            // a
            for (int cc : GUBMandatoryCCs) {
                if (GUBCCFinder.getSizeCC()[cc] == sizeMinCC.getLB()) {
                    int i = GUBCCFinder.getCCFirstNode()[cc];
                    while (i != -1) {
                        g.enforceNode(i, this);
                        i = GUBCCFinder.getCCNextNode()[i];
                    }
                    sizeMinCC.instantiateTo(sizeMinCC.getLB(), this);
                }
            }
            // b.
            for (int cc = 0; cc < GLBCCFinder.getNBCC(); cc++) {
                if (GLBCCFinder.getSizeCC()[cc] < sizeMinCC.getLB()) {
                    Map<Integer, Set<Integer>> ccPotentialNeighbors = getGLBCCPotentialNeighbors(cc);
                    if (ccPotentialNeighbors.size() == 1) {
                        int i = ccPotentialNeighbors.keySet().iterator().next();
                        Set<Integer> outNeighbors = ccPotentialNeighbors.get(i);
                        if (outNeighbors.size() == 1) {
                            int j = outNeighbors.iterator().next();
                            g.enforceNode(j, this);
                            g.enforceEdge(i, j, this);
                            recomputeMinNCC_LB = true;
                        }
                    }
                    if (ccPotentialNeighbors.size() > 1) {
                        Set<Integer> outNeighbors = new HashSet<>();
                        for (Set<Integer> i : ccPotentialNeighbors.values()) {
                            outNeighbors.addAll(i);
                        }
                        if (outNeighbors.size() == 1) {
                            int j = outNeighbors.iterator().next();
                            g.enforceNode(j, this);
                            recomputeMinNCC_LB = true;
                        }
                    }
                }
            }
        }
        // 9.
        if (recomputeMinNCC_LB) {
            // Recompute minNCC_LB
            this.GLBCCFinder.findAllCC();
            nbNodesT = g.getMandatoryNodes().size();
            nbNodesU = nbNodesTU - nbNodesT;
            minNCC_LB = getLBMinNCC(nbNodesT, nbNodesU);
            // Repeat 4. and 5.
            if (minNCC_LB > sizeMinCC.getUB()) {
                fails();
            }
            if (sizeMinCC.getLB() < minNCC_LB) {
                sizeMinCC.updateLowerBound(minNCC_LB, this);
            }
        }
        // 10.
        if (minNCC_UB > sizeMinCC.getUB()) {
            // a.
            if (sizeMinCC.getUB() == 0) {
                for (int i : g.getPotentialNodes()) {
                    g.removeNode(i, this);
                }
            }
            // b.
            if (sizeMinCC.getUB() == 1 && nbNodesU == 1 && GLBCCFinder.getSizeMinCC() > 1) {
                for (int i : g.getPotentialNodes()) {
                    if (g.getLB().getNodes().contains(i)) {
                        g.enforceNode(i, this);
                        for (int j : g.getPotentialNeighborsOf(i)) {
                            if (i != j) {
                                g.removeEdge(i, j, this);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return The indices of the mandatory GUB CCs (i.e. containing at least one node in GLB).
     */
    private Set<Integer> getGUBMandatoryCCs() {
        Set<Integer> mandatoryCCs = new HashSet<>();
        for (int cc = 0; cc < this.GUBCCFinder.getNBCC(); cc++) {
            int i = GUBCCFinder.getCCFirstNode()[cc];
            while (i != -1) {
                if (g.getLB().getNodes().contains(i)) {
                    mandatoryCCs.add(cc);
                    break;
                }
                i = GUBCCFinder.getCCNextNode()[i];
            }
        }
        return mandatoryCCs;
    }

    /**
     * @return The indices of the optional GUB CCs (i.e. not containing any node in GLB).
     */
    private Set<Integer> getGUBOptionalCCs() {
        Set<Integer> optionalCCs = new HashSet<>();
        for (int cc = 0; cc < this.GUBCCFinder.getNBCC(); cc++) {
            int currentNode = GUBCCFinder.getCCFirstNode()[cc];
            boolean addCC = true;
            while (currentNode != -1) {
                if (g.getLB().getNodes().contains(currentNode)) {
                    addCC = false;
                    break;
                }
                currentNode = GUBCCFinder.getCCNextNode()[currentNode];
            }
            if (addCC) {
                optionalCCs.add(cc);
            }
        }
        return optionalCCs;
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
     * Retrieve the potential CC neighbors (i.e. in GUB and not in the CC) of a GLB CC.
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
        // Compute |V_T|, |V_U| and |V_TU|
        int nbNodesT = g.getMandatoryNodes().size();
        int nbNodesTU = g.getPotentialNodes().size();
        int nbNodesU = nbNodesTU - nbNodesT;
        // Compute MIN_NCC(g) lower bound from g
        int minNCC_LB = getLBMinNCC(nbNodesT, nbNodesU);
        // Compute MIN_NCC(g) upper bound from g
        int minNCC_UB = getUBMinNCC(nbNodesT);
        // Check entailment
        if (minNCC_UB < sizeMinCC.getLB() || minNCC_LB > sizeMinCC.getUB()) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            if (minNCC_LB == sizeMinCC.getValue()) {
                return ESat.TRUE;
            } else {
                return ESat.FALSE;
            }
        }
        return ESat.UNDEFINED;
    }
}
