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

public class KruskalMSTGAC extends AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private TIntArrayList ma;     //mandatory arcs (i,j) <-> i*n+j
    // indexes are sorted
    private final int[] sortedArcs;   // from sorted to lex
    private final BitSet activeArcs; // if sorted is active
    // UNSORTED
    private final double[] costs;             // cost of the lex arc
    private final int[] p, rank;
    // CCtree
    private final int ccN;
    private final DirectedGraph ccTree;
    private final int[] ccTp;
    private final double[] ccTEdgeCost;
    private final LCAGraphManager lca;
    private int cctRoot;
    private final BitSet useful;
    private double maxTArc;
    private final int[][] map;
    private final double[][] repCosts;
    private final int[] fifo;

    //sort
    private final ArraySort sorter;
    private final IntComparator comparator;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public KruskalMSTGAC(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        activeArcs = new BitSet(n * n);
        rank = new int[n];
        costs = new double[n * n];
        sortedArcs = new int[n * n];
        p = new int[n];
        // CCtree
        ccN = 2 * n + 1;
        // backtrable
        ccTree = new DirectedGraph(ccN, SetType.LINKED_LIST, false);
        ccTEdgeCost = new double[ccN];
        ccTp = new int[n];
        useful = new BitSet(n);
        lca = new LCAGraphManager(ccN);
        map = new int[n][n];
        repCosts = new double[n][n];
        fifo = new int[n];
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

    private void sortArcs(double[][] costMatrix) {
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
        size /= 2; // recent change
        int idx = 0;
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j : nei) {
                assert i != j;
                if (i < j) {
                    sortedArcs[idx] = i * n + j;
                    costs[i * n + j] = costMatrix[i][j];
                    idx++;
                }
            }
        }
        assert idx == size;
        for (int i = n; i < ccN; i++) {
            ccTree.removeNode(i);
        }
        sorter.sort(sortedArcs, size, comparator);
        activeArcs.clear();
        activeArcs.set(0, size);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
        g = graph;
        ma = propHK.getMandatoryArcsList();
        sortArcs(costs);
        treeCost = 0;
        cctRoot = n - 1;
        int tSize = addMandatoryArcs();
        connectMST(tSize);
    }

    public void performPruning(double UB) throws ContradictionException {
        double delta = UB - treeCost;
        assert delta >= 0;
        prepareMandArcDetection();
        if (selectRelevantArcs(delta)) {
            lca.preprocess(cctRoot, ccTree);
            pruning(delta);
        }
    }

    private void prepareMandArcDetection() {
        // RECYCLING ccTp is used to model the compressed path
        ISet nei;
        for (int i = 0; i < n; i++) {
            ccTp[i] = -1;
        }
        useful.clear();
        useful.set(0);
        ccTp[0] = 0;
        int first = 0;
        int last = first;
        int k = 0;
        fifo[last++] = k;
        while (first < last) {
            k = fifo[first++];
            nei = Tree.getNeighborsOf(k);
            for (int s : nei) {
                if (ccTp[s] == -1) {
                    ccTp[s] = k;
                    map[s][k] = -1;
                    map[k][s] = -1;
                    if (!useful.get(s)) {
                        fifo[last++] = s;
                        useful.set(s);
                    }
                }
            }
        }
    }

    private void markTreeEdges(int[] next, int i, int j) {
        int rep = i * n + j;
        if (Tree.containsEdge(j, i)) {
            if (map[j][i] == -1) {
                map[j][i] = map[i][j] = rep;
            }
            return;
        }
        if (next[i] == next[j]) {
            if (map[i][next[i]] == -1) {
                map[i][next[i]] = map[next[i]][i] = rep;
            }
            if (map[j][next[j]] == -1) {
                map[j][next[j]] = map[next[j]][j] = rep;
            }
            return;
        }
        useful.clear();
        int meeting = j;
        int tmp;
        int a;
        for (a = i; a != next[a]; a = next[a]) {
            useful.set(a);
        }
        useful.set(a);
        while (!useful.get(meeting)) {
            meeting = next[meeting];
        }
        for (int b = j; b != meeting; ) {
            tmp = next[b];
            next[b] = meeting;
            if (map[b][tmp] == -1) {
                map[b][tmp] = map[tmp][b] = rep;
            }
            b = tmp;
        }
        for (a = i; a != meeting; ) {
            tmp = next[a];
            next[a] = meeting;
            if (map[a][tmp] == -1) {
                map[a][tmp] = map[tmp][a] = rep;
            }
            a = tmp;
        }
    }

    private boolean selectRelevantArcs(double delta) throws ContradictionException {
        // Trivially no inference
        int idx = activeArcs.nextSetBit(0);
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
        //contract ccTree
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

    private void pruning(double delta) throws ContradictionException {
        int i;
        for (int arc = activeArcs.nextSetBit(0); arc >= 0; arc = activeArcs.nextSetBit(arc + 1)) {
            i = sortedArcs[arc] / n;
            int j = sortedArcs[arc] % n;
            if (!Tree.containsEdge(i, j)) {
                repCosts[i][j] = costs[i * n + j] - ccTEdgeCost[lca.getLCA(i, j)];
                if (repCosts[i][j] > delta) {
                    activeArcs.clear(arc);
                    propHK.remove(i, j);
                } else {
                    markTreeEdges(ccTp, i, j);
                }
            }
        }
        ISet nei;
        for (i = 0; i < n; i++) {
            nei = Tree.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    if (map[i][j] != -1) {
                        repCosts[i][j] = costs[map[i][j]] - costs[i * n + j];
                        if (repCosts[i][j] > delta) {
                            propHK.enforce(i, j);
                        }
                    } else {
                        propHK.enforce(i, j);
                    }

//					repCost = costs[map[i][j]];
//					if(repCost-costs[i*n+j]>delta){
//						propHK.enforce(i,j);
//					}
//				}
                }
            }
        }
    }

    //***********************************************************************************
    // Kruskal's
    //***********************************************************************************

    private int addMandatoryArcs() throws ContradictionException {
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

    private void connectMST(int tSize) throws ContradictionException {
        int from, to, rFrom, rTo;
        int idx = activeArcs.nextSetBit(0);
        double minTArc = -propHK.getMinArcVal();
        maxTArc = propHK.getMinArcVal();
        double cost;
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

    private void updateCCTree(int rfrom, int rto, double cost) {
        cctRoot++;
        int newNode = cctRoot;
        ccTree.addNode(newNode);
        ccTree.addEdge(newNode, ccTp[rfrom]);
        ccTree.addEdge(newNode, ccTp[rto]);
        ccTp[rfrom] = newNode;
        ccTp[rto] = newNode;
        ccTEdgeCost[newNode] = cost;
    }

    private void linkUF(int x, int y) {
        if (rank[x] > rank[y]) {
            p[y] = p[x];
        } else {
            p[x] = p[y];
        }
        if (rank[x] == rank[y]) {
            rank[y]++;
        }
    }

    private int findUF(int i) {
        if (p[i] != i) {
            p[i] = findUF(p[i]);
        }
        return p[i];
    }

    public double getRepCost(int from, int to) {
        return repCosts[from][to];
    }

//	private int getLCA(int i, int j) {
//		BitSet marked = new BitSet(ccN);
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
