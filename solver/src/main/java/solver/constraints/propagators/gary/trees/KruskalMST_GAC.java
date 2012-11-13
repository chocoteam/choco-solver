/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary.trees;

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.exception.ContradictionException;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.graphOperations.dominance.LCAGraphManager;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import solver.variables.setDataStructures.ISet;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.LinkedList;

public class KruskalMST_GAC extends AbstractTreeFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected TIntArrayList ma;     //mandatory arcs (i,j) <-> i*n+j
    // indexes are sorted
    protected int[] sortedArcs;   // from sorted to lex
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
    protected int cctRoot;
    protected BitSet useful;
    protected double minTArc, maxTArc;
    protected int[][] map;
    protected double[][] repCosts;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public KruskalMST_GAC(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        activeArcs = new BitSet(n * n);
        rank = new int[n];
        costs = new double[n * n];
        sortedArcs = new int[n * n];
        p = new int[n];
        // CCtree
        ccN = 2 * n + 1;
        // backtrable
        ccTree = new DirectedGraph(ccN, GraphType.LINKED_LIST, false);
        ccTEdgeCost = new double[ccN];
        ccTp = new int[n];
        useful = new BitSet(n);
        lca = new LCAGraphManager(ccN);
        map = new int[n][n];
        repCosts = new double[n][n];
    }

    protected void sortArcs(double[][] costMatrix) {
        Comparator<Integer> comp = new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                if (costs[i1] < costs[i2]) {
                    return -1;
                }
                if (costs[i1] > costs[i2]) {
                    return 1;
                }
                return 0;
            }
        };
        int size = 0;
        for (int i = 0; i < n; i++) {
            p[i] = i;
            rank[i] = 0;
            ccTp[i] = i;
            Tree.getPredecessorsOf(i).clear();
            Tree.getSuccessorsOf(i).clear();
            ccTree.desactivateNode(i);
            ccTree.activateNode(i);
            size += g.getSuccessorsOf(i).getSize();
        }
        Integer[] integers = new Integer[size];
        int idx = 0;
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                integers[idx] = i * n + j;
                costs[i * n + j] = costMatrix[i][j];
                idx++;
            }
        }
        for (int i = n; i < ccN; i++) {
            ccTree.desactivateNode(i);
        }
        Arrays.sort(integers, comp);
        int v;
        activeArcs.clear();
        activeArcs.set(0, size);
        for (idx = 0; idx < size; idx++) {
            v = integers[idx];
            sortedArcs[idx] = v;
        }
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
        if (delta < 0) {
            throw new UnsupportedOperationException("mst>ub");
        }
        prepareMandArcDetection();
        if (selectRelevantArcs(delta)) {
            lca.preprocess(cctRoot, ccTree);
            pruning(delta);
        }
    }

    protected void prepareMandArcDetection() {
        // RECYCLING ccTp is used to model the compressed path
        ISet nei;
        for (int i = 0; i < n; i++) {
            ccTp[i] = -1;
        }
        int k = 0;
        useful.clear();
        useful.set(0);
        ccTp[0] = 0;
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.add(k);
        while (!list.isEmpty()) {
            k = list.removeFirst();
            nei = Tree.getSuccessorsOf(k);
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                if (ccTp[s] == -1) {
                    ccTp[s] = k;
                    map[s][k] = -1;
                    map[k][s] = -1;
                    if (!useful.get(s)) {
                        list.addLast(s);
                        useful.set(s);
                    }
                }
            }
            nei = Tree.getPredecessorsOf(k);
            for (int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()) {
                if (ccTp[s] == -1) {
                    ccTp[s] = k;
                    map[s][k] = -1;
                    map[k][s] = -1;
                    if (!useful.get(s)) {
                        list.addLast(s);
                        useful.set(s);
                    }
                }
            }
        }
    }

    protected void markTreeEdges(int[] next, int i, int j) {
        int rep = i * n + j;
        if (Tree.arcExists(j, i)) {
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
        for (; !useful.get(meeting); meeting = next[meeting]) {
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

    protected boolean selectRelevantArcs(double delta) throws ContradictionException {
        // Trivially no inference
        int idx = activeArcs.nextSetBit(0);
        // Maybe interesting
        while (idx >= 0 && costs[sortedArcs[idx]] - maxTArc <= delta) {
            idx = activeArcs.nextSetBit(idx + 1);
        }
        // Trivially infeasible arcs
        while (idx >= 0) {
            if (!Tree.arcExists(sortedArcs[idx] / n, sortedArcs[idx] % n)) {
                propHK.remove(sortedArcs[idx] / n, sortedArcs[idx] % n);
                activeArcs.clear(idx);
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
        //contract ccTree
        cctRoot++;
        int newNode = cctRoot;
        ccTree.activateNode(newNode);
        ccTEdgeCost[newNode] = propHK.getMinArcVal();
        for (int i = ccTree.getActiveNodes().getFirstElement(); i >= 0; i = ccTree.getActiveNodes().getNextElement()) {
            if (ccTree.getPredecessorsOf(i).getFirstElement() == -1) {
                if (i != cctRoot) {
                    ccTree.addArc(cctRoot, i);
                }
            }
        }
        return true;
    }

    protected void pruning(double delta) throws ContradictionException {
        int i, j;
        for (int arc = activeArcs.nextSetBit(0); arc >= 0; arc = activeArcs.nextSetBit(arc + 1)) {
            i = sortedArcs[arc] / n;
            j = sortedArcs[arc] % n;
            if (!Tree.arcExists(i, j)) {
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
            nei = Tree.getSuccessorsOf(i);
            for (j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
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
            rFrom = FIND(from);
            rTo = FIND(to);
            if (rFrom != rTo) {
                LINK(rFrom, rTo);
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

    protected void connectMST(int tSize) throws ContradictionException {
        int from, to, rFrom, rTo;
        int idx = activeArcs.nextSetBit(0);
        minTArc = -propHK.getMinArcVal();
        maxTArc = propHK.getMinArcVal();
        double cost;
        while (tSize < n - 1) {
            if (idx < 0) {
                propHK.contradiction();
            }
            from = sortedArcs[idx] / n;
            to = sortedArcs[idx] % n;
            rFrom = FIND(from);
            rTo = FIND(to);
            if (rFrom != rTo) {
                LINK(rFrom, rTo);
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
        ccTree.activateNode(newNode);
        ccTree.addArc(newNode, ccTp[rfrom]);
        ccTree.addArc(newNode, ccTp[rto]);
        ccTp[rfrom] = newNode;
        ccTp[rto] = newNode;
        ccTEdgeCost[newNode] = cost;
    }

    protected void LINK(int x, int y) {
        if (rank[x] > rank[y]) {
            p[y] = p[x];
        } else {
            p[x] = p[y];
        }
        if (rank[x] == rank[y]) {
            rank[y]++;
        }
    }

    protected int FIND(int i) {
        if (p[i] != i) {
            p[i] = FIND(p[i]);
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
//		int p = ccTree.getPredecessorsOf(i).getFirstElement();
//		while(p!=-1){
//			marked.set(p);
//			p = ccTree.getPredecessorsOf(p).getFirstElement();
//		}
//		p = ccTree.getPredecessorsOf(j).getFirstElement();
//		while(p!=-1 && !marked.get(p)){
//			p = ccTree.getPredecessorsOf(p).getFirstElement();
//		}
//		return p;
//	}
}
