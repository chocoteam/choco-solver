/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.connectivity;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

import java.util.BitSet;

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
    private TIntArrayList bridgeFrom, bridgeTo;
    private BitSet hasMandInSubtree, visited;
    private ISet articulationPoints;
    private int[] parent, time, minT;
    private int[] timer = new int[1];

    public UGVarConnectivityHelper(UndirectedGraphVar g){
        this.g = g;
        this.n = g.getNbMaxNodes();
        this.fifo = new int[n];
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

    public ISet getArticulationPoints(){
        return articulationPoints;
    }

    public TIntArrayList getBridgeFrom() {
        return bridgeFrom;
    }

    public TIntArrayList getBridgeTo() {
        return bridgeTo;
    }

    public void findMandatoryArticulationPointsAndBridges() {
        if(articulationPoints == null){
            articulationPoints = SetFactory.makeBipartiteSet(0);
            bridgeFrom = new TIntArrayList();
            bridgeTo = new TIntArrayList();
            hasMandInSubtree = new BitSet(n);
            visited = new BitSet(n);
            parent = new int[n];
            time = new int[n];
            minT = new int[n];
        }
        articulationPoints.clear();
        bridgeFrom.clear();
        bridgeTo.clear();
        ISet mNodes = g.getMandatoryNodes();
        if(mNodes.size()>=2) {
            visited.clear();
            hasMandInSubtree.clear();
            for(int root : mNodes.toArray()) { // uses to array because default iterator may be used within the algorithm
                if(!visited.get(root)) {
                    // root node init
                    visited.set(root);
                    parent[root] = root;
                    timer[0] = 0;
                    // DFS from root
                    findMAPBFrom(root);
                }
            }
        }
    }

    private void findMAPBFrom(int i){
        int nbMandChilds = 0;
        for (int j : g.getPotentialNeighborsOf(i)) {
            if (!visited.get(j)) {
                visited.set(j);
                parent[j] = i;
                timer[0]++;
                minT[j] = time[j] = timer[0];
                if(g.getMandatoryNodes().contains(j)) hasMandInSubtree.set(j);
                findMAPBFrom(j);

                // j sub-tree has been fully explored
                // propagates to i if subtrees of j have links to ancestors of i
                minT[i] = Math.min(minT[i], minT[j]);
                // propagates to i if subtrees of j include mandatory nodes
                if(hasMandInSubtree.get(j)) hasMandInSubtree.set(i);

                if(hasMandInSubtree.get(j)){
                    nbMandChilds ++;
                }

                // If the lowest vertex reachable from subtree under j is below i in DFS tree,
                // then (i,j) is a bridge
                if (minT[j] > time[i] && !g.getMandatoryNeighborsOf(i).contains(j)){
                    bridgeFrom.add(i);
                    bridgeTo.add(j);
                }

                // root node ?
                if(parent[i] == i){
                    // root has >1 child with mandatory nodes in their subtrees
                    if(nbMandChilds>1 && !g.getMandatoryNodes().contains(i))
                        articulationPoints.add(i);

                }else{
                    // j sub-tree has been explored and cannot go above i
                    if(minT[j] >= time[i] && hasMandInSubtree.get(j) && !g.getMandatoryNodes().contains(i))
                        articulationPoints.add(i);
                }
            }
            if(j != parent[i]){ // i can reach j (which might be above i)
                minT[i] = Math.min(minT[i], time[j]);
            }
        }
    }

    /**
     * @return True if the graph var is biconnected, the empty graph is not considered biconnected
     */
    public boolean isBiconnected() {
        if (g.getPotentialNodes().size() <= 1) {
            return false;
        }
        int root = g.getPotentialNodes().iterator().next();
        if(visited==null)visited = new BitSet(n);
        exploreFrom(root,visited);
        if(visited.cardinality()<g.getPotentialNodes().size()) {
            return false;
        }
        // articulation point exist?
        findMandatoryArticulationPointsAndBridges();
        System.out.println(articulationPoints);
        return articulationPoints.isEmpty();
    }
}
