/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.connectivity;

import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * @author Jean-Guillaume FAGES (cosling)
 * @since 18/01/2019.
 */
public class UGVarConnectivityHelper {

    // input data
    private final UndirectedGraphVar g;
    private final int n;

    // internal variable for graph exploration
    private final int[] fifo;

    // internal variables for Articulation Points and Bridge detection
    private int numOrder;
    private final int[] num;
    private final BitSet visited;

    // output data
    private final ISet articulationPoints = SetFactory.makeBipartiteSet(0);
    private final List<int[]> bridges = new ArrayList<>();

    // --- constructor
    public UGVarConnectivityHelper(UndirectedGraphVar g){
        this.g = g;
        this.n = g.getNbMaxNodes();
        this.fifo = new int[n];
        this.num = new int[n];
        this.visited = new BitSet(n);
    }

    //***********************************************************************************
    // CONNECTIVITY
    //***********************************************************************************

    public void exploreFrom(int root, BitSet visited) {
        int first = 0;
        int last = 0;
        int i = root;
        fifo[last++] = i;
        visited.set(i);
        while (first < last) {
            i = fifo[first++];
            for (int j : g.getPotentialNeighborsOf(i)) {
                if (!visited.get(j)) {
                    visited.set(j);
                    fifo[last++] = j;
                }
            }
        }
    }

    //***********************************************************************************
    // ARTICULATION POINTS AND BRIDGES
    //***********************************************************************************

    public void computeMandatoryArticulationPointsAndBridges() {
        articulationPoints.clear();
        bridges.clear();
        ISet mNodes = g.getMandatoryNodes();
        if(g.getMandatoryNodes().size()<2) return;

        visited.clear();
        Arrays.fill(num, 0);
        for(int root : mNodes) {
            if(!visited.get(root)) {
                // root node init
                visited.set(root);
                // DFS from root
                computeMandatoryArticulationPointsAndBridgesFrom(root);
            }
        }
    }

    /**
     * Computes ridge and articulation point detection linking mandatory nodes.
     * @param s root node, must be a mandatory node itself
     */
    private void computeMandatoryArticulationPointsAndBridgesFrom(int s) {
        assert g.getMandatoryNodes().contains(s);
        numOrder = 1;
        num[s] = numOrder++;
        for (int next:g.getPotentialNeighborsOf(s)) {
            if (num[next] == 0) {
                int[] LowMand = doFindArticulation(next, s);
                int lowN = LowMand[0];
                int mandN = LowMand[1];
                if(num[next] == lowN && mandN == 1 && !g.getMandatoryNeighborsOf(s).contains(next)){
                    bridges.add(new int[]{s,next});
                }
            }
        }
    }

    private int[] doFindArticulation (int s, int parent) {
        int lowpt = num[s] = numOrder++;
        int mand = g.getMandatoryNodes().contains(s)?1:0;
        for (int next:g.getPotentialNeighborsOf(s)) {
            if (num[next] == 0) {
                int[] LowMand = doFindArticulation(next, s);
                int lowN = LowMand[0];
                int mandN = LowMand[1];
                lowpt = Math.min(lowN, lowpt);
                mand = Math.max(mand, mandN);
                if (lowN >= num[s] && mandN == 1) {
                    articulationPoints.add(s);
                    if(num[next] == lowN && !g.getMandatoryNeighborsOf(s).contains(next)){
                        bridges.add(new int[]{s,next});
                    }
                }
            } else if (num[next] < num[s] && next != parent) {
                lowpt = Math.min(num[next],lowpt);
                mand = Math.max(mand, g.getMandatoryNodes().contains(next)?1:0);
            }
        }
        return new int[]{lowpt, mand};
    }

    // --- accessors

    public ISet getArticulationPoints() {
        return articulationPoints;
    }

    public List<int[]> getBridges() {
        return bridges;
    }
}
