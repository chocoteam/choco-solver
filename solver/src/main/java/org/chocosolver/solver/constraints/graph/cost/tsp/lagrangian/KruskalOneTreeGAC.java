/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp.lagrangian;

import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.constraints.graph.cost.trees.lagrangian.KruskalMSTFinder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class KruskalOneTreeGAC extends KruskalMSTFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int min1, min2;
    private final int[][] map;
    private final double[][] marginalCosts;
    private final int[] fifo;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public KruskalOneTreeGAC(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        map = new int[n][n];
        marginalCosts = new double[n][n];
        fifo = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
        super.computeMST(costs, graph);
        add0Node();
    }

    public void performPruning(double UB) throws ContradictionException {
        double delta = UB - treeCost;
        if (delta < 0) {
            throw new UnsupportedOperationException("mst>ub");
        }
        prepareMandArcDetection();
        if (selectRelevantArcs(delta)) {
            lca.preprocess(cctRoot, ccTree);
            pruning(0, delta);
        }
    }

    protected void sortArcs() {
        int size = 0;
        Tree.getNeighborsOf(0).clear();
        for (int i = 1; i < n; i++) {
            p[i] = i;
            rank[i] = 0;
            ccTp[i] = i;
            Tree.getNeighborsOf(i).clear();
            ccTree.removeNode(i);
            ccTree.addNode(i);
            size += g.getNeighborsOf(i).size();
        }
        size -= g.getNeighborsOf(0).size();
        assert size % 2 == 0;
        size /= 2;
        ISet nei;
        int idx = 0;
        for (int i = 1; i < n; i++) {
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

    protected void pruning(int fi, double delta) throws ContradictionException {
        ISet nei = g.getNeighborsOf(0);
        for (int i : nei) {
            if (i != min1 && i != min2 && distMatrix[0][i] - distMatrix[0][min2] > delta) {
                propHK.remove(0, i);
            }
        }

        for (int arc = activeArcs.nextSetBit(0); arc >= 0; arc = activeArcs.nextSetBit(arc + 1)) {
            int i = sortedArcs[arc] / n;
            int j = sortedArcs[arc] % n;
            if (!Tree.containsEdge(i, j)) {
                marginalCosts[i][j] = costs[i * n + j] - ccTEdgeCost[lca.getLCA(i, j)];
                if (marginalCosts[i][j] > delta) {
                    activeArcs.clear(arc);
                    propHK.remove(i, j);
                } else {
                    markTreeEdges(ccTp, i, j);
                }
            }
        }
        for (int i = 1; i < n; i++) {
            nei = Tree.getNeighborsOf(i);
            for (int j : nei) {
                if (i < j)
                    if (map[i][j] == -1 || costs[map[i][j]] - costs[i * n + j] > delta) {
                        propHK.enforce(i, j);
                    } else {
                        marginalCosts[i][j] = costs[map[i][j]] - costs[i * n + j];
                    }
//				if(j!=0 && costs[map[i][j]]-costs[i*n+j]>delta){
//					propHK.enforce(i,j);
//				}
//				if(i<j && map[i][j]==-1){
//					propHK.enforce(i,j);
//				}
            }
        }
    }

    protected boolean selectRelevantArcs(double delta) throws ContradictionException {
        return selectAndCompress(delta);
    }

    protected boolean selectAndCompress(double delta) throws ContradictionException {
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

    //***********************************************************************************
    // Kruskal's
    //***********************************************************************************

    @Override
    protected int addMandatoryArcs() throws ContradictionException {
        int from, to, rFrom, rTo, arc;
        int tSize = 0;
        double val = propHK.getMinArcVal();
        for (int i = ma.size() - 1; i >= 0; i--) {
            arc = ma.get(i);
            from = arc / n;
            to = arc % n;
            if (from != 0 && to != 0) {
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
        while (tSize < n - 2) {
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

    private void add0Node() throws ContradictionException {
        ISet nei = g.getNeighborsOf(0);
        min1 = -1;
        min2 = -1;
        boolean b1 = false, b2 = false;
        for (int j : nei) {
            if (!b1) {
                if (min1 == -1) {
                    min1 = j;
                }
                if (distMatrix[0][j] < distMatrix[0][min1]) {
                    min2 = min1;
                    min1 = j;
                }
                if (propHK.isMandatory(0, j)) {
                    if (min1 != j) {
                        min2 = min1;
                    }
                    min1 = j;
                    b1 = true;
                }
            }
            if (min1 != j && !b2) {
                if (min2 == -1 || distMatrix[0][j] < distMatrix[0][min2]) {
                    min2 = j;
                }
                if (propHK.isMandatory(0, j)) {
                    min2 = j;
                    b2 = true;
                }
            }
        }
        if (min1 == min2) {
            throw new UnsupportedOperationException();
        }
        if (min1 == -1 || min2 == -1) {
            propHK.contradiction();
        }
        if (!propHK.isMandatory(0, min1)) {
            maxTArc = Math.max(maxTArc, distMatrix[0][min1]);
        }
        if (!propHK.isMandatory(0, min2)) {
            maxTArc = Math.max(maxTArc, distMatrix[0][min2]);
        }
        Tree.addEdge(0, min1);
        Tree.addEdge(0, min2);
        treeCost += distMatrix[0][min1] + distMatrix[0][min2];
    }

    //***********************************************************************************
    // Detecting mandatory arcs
    //***********************************************************************************

    protected void prepareMandArcDetection() {
        // RECYCLING ccTp is used to model the compressed path
        ISet nei;
        for (int i = 0; i < n; i++) {
            ccTp[i] = -1;
        }
        useful.clear();
        useful.set(0);
        int k = 1;
        useful.set(k);
        ccTp[k] = k;
        int first = 0;
        int last = first;
        fifo[last++] = k;
        while (first < last) {
            k = fifo[first++];
            nei = Tree.getNeighborsOf(k);
            for (int s : nei) {
                if (ccTp[s] == -1) {
                    ccTp[s] = k;
                    map[s][k] = map[k][s] = -1;
                    if (!useful.get(s)) {
                        fifo[last++] = s;
                        useful.set(s);
                    }
                }
            }
        }
    }

    protected void markTreeEdges(int[] next, int i, int j) {
        int rep = i * n + j;
        if (i == 0) {
            throw new UnsupportedOperationException();
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

    public double getRepCost(int from, int to) {
        if (from > to) {
            return getRepCost(to, from);//to check
        }
        if (from == 0) {
            return 0;
        }
        return marginalCosts[from][to];
//		if(map[from][to]==-1){
//			System.out.println(map[to][from]);
//			System.exit(0);
//		}
//		return costs[map[from][to]]-costs[from*n+to];
    }

//	public double getMarginalCost(int from, int to){
//		if(from>to){
//			return getRepCost(to,from);//to check
//		}
//		if(from==0){
//			return 0;
//		}
//		return costs[from*n+to]-ccTEdgeCost[lca.getLCA(from,to)];
//	}
}
