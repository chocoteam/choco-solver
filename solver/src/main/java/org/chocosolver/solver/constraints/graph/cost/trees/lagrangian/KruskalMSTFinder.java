/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees.lagrangian;

import gnu.trove.list.array.TIntArrayList;

import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.graphOperations.LCAGraphManager;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;

import java.util.BitSet;

public class KruskalMSTFinder extends AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected TIntArrayList ma;     //mandatory arcs (i,j) <-> i*n+j
    // indexes are sorted
    protected int[] sortedArcs;   // from sorted to lex
    protected int[][] indexOfArc; // from lex (i,j) to sorted (i+1)*n+j
    protected BitSet activeArcs; // if sorted is active
    // UNSORTED
    protected double[] costs;             // cost of the lex arc
    protected int[] p, rank;
    // CCtree
    protected int ccN;
    protected DirectedGraph ccTree;
    protected int[] ccTp;
    protected double[] ccTEdgeCost;
    protected LCAGraphManager lca;
    protected int fromInterest, cctRoot;
    protected BitSet useful;
    protected double minTArc, maxTArc;
    protected double[][] distMatrix;

    //sort
    protected ArraySort sorter;
    protected IntComparator comparator;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public KruskalMSTFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        activeArcs = new BitSet(n * n);
        rank = new int[n];
        costs = new double[n * n];
        sortedArcs = new int[n * n];
        indexOfArc = new int[n][n];
        p = new int[n];
        // CCtree
        ccN = 2 * n + 1;
        // backtrable
        ccTree = new DirectedGraph(ccN, SetType.LINKED_LIST, false);
        ccTEdgeCost = new double[ccN];
        ccTp = new int[n];
        useful = new BitSet(n);
        lca = new LCAGraphManager(ccN);
        //sort
        sorter = new ArraySort(n * n, false, true);
        comparator = (i1, i2) -> {
            if (costs[i1] < costs[i2])
                return -1;
            else if (costs[i1] > costs[i2])
                return 1;
            else return 0;
        };
    }

    //***********************************************************************************
    // findUF MST
    //***********************************************************************************

    public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
        g = graph;
        distMatrix = costs;
        ma = propHK.getMandatoryArcsList();
        sortArcs();
        treeCost = 0;
        cctRoot = n - 1;
        int tSize = addMandatoryArcs();
        connectMST(tSize);
    }

    protected void sortArcs() {
        int size = 0;
        for (int i = 0; i < n; i++) {
            p[i] = i;
            rank[i] = 0;
            ccTp[i] = i;
            Tree.getNeighborsOf(i).clear();
            ccTree.removeNode(i);
            ccTree.addNode(i);
            size += g.getNeighborsOf(i).size();
        }
        assert size % 2 == 0;
        size /= 2;
        ISet nei;
        int idx = 0;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    sortedArcs[idx] = i * n + j;
                    costs[i * n + j] = distMatrix[i][j];
                    idx++;
                }
            }
        }
        for (int i = n; i < ccN; i++) {
            ccTree.removeNode(i);
        }
        sorter.sort(sortedArcs, size, comparator);
        int v;
        activeArcs.clear();
        activeArcs.set(0, size);
        for (idx = 0; idx < size; idx++) {
            v = sortedArcs[idx];
            indexOfArc[v / n][v % n] = idx;
        }
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    public void performPruning(double UB) throws ContradictionException {
        double delta = UB - treeCost;
        assert delta >= 0;
        fromInterest = 0;
        if (selectAndCompress(delta)) {
            lca.preprocess(cctRoot, ccTree);
            pruning(fromInterest, delta);
        }
    }

    protected boolean selectRelevantArcs(double delta) throws ContradictionException {
        // Trivially no inference
        int idx = activeArcs.nextSetBit(0);
        while (idx >= 0 && costs[sortedArcs[idx]] - minTArc <= delta) {
            idx = activeArcs.nextSetBit(idx + 1);
        }
        if (idx == -1) {
            return false;
        }
        fromInterest = idx;
        // Maybe interesting
        while (idx >= 0 && costs[sortedArcs[idx]] - maxTArc <= delta) {
            idx = activeArcs.nextSetBit(idx + 1);
        }
        // Trivially infeasible arcs
        while (idx >= 0) {
            if (!Tree.containsEdge(sortedArcs[idx] / n, sortedArcs[idx] % n)) {
                propHK.remove(sortedArcs[idx] / n, sortedArcs[idx] % n);
                activeArcs.clear(idx);
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
        ccTree.addEdge(cctRoot, 0);
        return true;
    }

    protected boolean selectAndCompress(double delta) throws ContradictionException {
        // Trivially no inference
        int idx = activeArcs.nextSetBit(0);
        while (idx >= 0 && costs[sortedArcs[idx]] - minTArc <= delta) {
            idx = activeArcs.nextSetBit(idx + 1);
        }
        if (idx == -1) {
            return false;
        }
        fromInterest = idx;
        // Maybe interesting
        useful.clear();
        while (idx >= 0 && costs[sortedArcs[idx]] - maxTArc <= delta) {
            useful.set(sortedArcs[idx] / n);
            useful.set(sortedArcs[idx] % n);
            idx = activeArcs.nextSetBit(idx + 1);
        }
        // Trivially infeasible arcs
        while (idx >= 0) {
            if (!Tree.containsEdge(sortedArcs[idx] / n, sortedArcs[idx] % n)) {
                propHK.remove(sortedArcs[idx] / n, sortedArcs[idx] % n);
                activeArcs.clear(idx);
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
        if (useful.cardinality() == 0) {
            return false;
        }
        //contract ccTree
        for (int i = useful.nextClearBit(0); i < n; i = useful.nextClearBit(i + 1)) {
            ccTree.removeNode(i);
        }
        for (int i : ccTree.getNodes()) {
            if (ccTree.getSuccessorsOf(i).isEmpty()) {
                if (i >= n) {
                    ccTree.removeNode(i);
                }
            } else if (ccTree.getSuccessorsOf(i).size() == 1) {
                int s = ccTree.getSuccessorsOf(i).iterator().next();
                ccTree.removeNode(i);
                if (!ccTree.getPredecessorsOf(i).isEmpty()) {
                    int p = ccTree.getPredecessorsOf(i).iterator().next();
                    ccTree.addEdge(p, s);
                }
            }
        }
        cctRoot++;
        int newNode = cctRoot;
        ccTree.addNode(newNode);
        ccTEdgeCost[newNode] = propHK.getMinArcVal();
        for (int i : ccTree.getNodes()) {
            if (i != cctRoot && ccTree.getPredecessorsOf(i).isEmpty()) {
                ccTree.addEdge(cctRoot, i);
            }
        }
        return true;
    }

    protected void pruning(int fi, double delta) throws ContradictionException {
        int i, j;
        double repCost;
        for (int arc = activeArcs.nextSetBit(fi); arc >= 0; arc = activeArcs.nextSetBit(arc + 1)) {
            i = sortedArcs[arc] / n;
            j = sortedArcs[arc] % n;
            if (!Tree.containsEdge(i, j)) {
                if (propHK.isMandatory(i, j)) {
                    throw new UnsupportedOperationException();
                }
//				repCost = ccTEdgeCost[getLCA(i,j)];
                repCost = ccTEdgeCost[lca.getLCA(i, j)];
//				PropSymmetricHeldKarp.reducedCosts[i][j] = repCost;
                if (costs[i * n + j] - repCost > delta) {
                    activeArcs.clear(arc);
                    propHK.remove(i, j);
                }
            }
        }
    }

    //***********************************************************************************
    // Kruskal's
    //***********************************************************************************

    protected int addMandatoryArcs() throws ContradictionException {
        int from, to, rFrom, rTo, arc;
        int tSize = 0;
        double val = propHK.getMinArcVal();
        for (int i = ma.size() - 1; i >= 0; i--) {
            arc = ma.get(i);
            from = arc / n;
            to = arc % n;
            rFrom = findUF(from);
            rTo = findUF(to);
            if (rFrom != rTo) {
                linkUF(rFrom, rTo);
                Tree.addEdge(from, to);
                updateCCTree(rFrom, rTo, val);
                treeCost += costs[arc];
                tSize++;
            } else {
                propHK.contradiction();
            }
        }
        return tSize;
    }

    protected void connectMST(int treeSize) throws ContradictionException {
        int from, to, rFrom, rTo;
        int idx = activeArcs.nextSetBit(0);
        minTArc = -propHK.getMinArcVal();
        maxTArc = propHK.getMinArcVal();
        double cost;
        int tSize = treeSize;
        while (tSize < n - 1) {
            if (idx < 0) {
                propHK.contradiction();
            }
            from = sortedArcs[idx] / n;
            to = sortedArcs[idx] % n;
            rFrom = findUF(from);
            rTo = findUF(to);
            if (rFrom != rTo) {
                linkUF(rFrom, rTo);
                Tree.addEdge(from, to);
                cost = costs[sortedArcs[idx]];
                updateCCTree(rFrom, rTo, cost);
                if (cost > maxTArc) {
                    maxTArc = cost;
                }
                if (cost < minTArc) {
                    minTArc = cost;
                }
                treeCost += cost;
                tSize++;
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
    }

    protected void updateCCTree(int rfrom, int rto, double cost) {
        cctRoot++;
        int newNode = cctRoot;
        ccTree.addNode(newNode);
        ccTree.addEdge(newNode, ccTp[rfrom]);
        ccTree.addEdge(newNode, ccTp[rto]);
        ccTp[rfrom] = newNode;
        ccTp[rto] = newNode;
        ccTEdgeCost[newNode] = cost;
    }

    protected void linkUF(int x, int y) {
        if (rank[x] > rank[y]) {
            p[y] = p[x];
        } else {
            p[x] = p[y];
        }
        if (rank[x] == rank[y]) {
            rank[y]++;
        }
    }

    protected int findUF(int i) {
        if (p[i] != i) {
            p[i] = findUF(p[i]);
        }
        return p[i];
    }

//	BitSet marked = new BitSet(n*2);
//	private int getLCA(int i, int j) {
//		marked.clear();
//		marked.set(i);
//		marked.set(j);
//		int p = ccTree.getPredOf(i).getFirstElement();
//		while(p!=-1){
//			marked.set(p);
//			p = ccTree.getPredOf(p).getFirstElement();
//		}
//		p = ccTree.getPredOf(j).getFirstElement();
//		while(p!=-1 && !marked.get(p)){
//			p = ccTree.getPredOf(p).getFirstElement();
//		}
//		return p;
//	}
}
