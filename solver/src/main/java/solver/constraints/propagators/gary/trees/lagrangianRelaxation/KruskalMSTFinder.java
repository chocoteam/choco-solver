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

package solver.constraints.propagators.gary.trees.lagrangianRelaxation;

import gnu.trove.list.array.TIntArrayList;
import common.util.objects.graphs.DirectedGraph;
import common.util.objects.graphs.UndirectedGraph;
import common.util.graphOperations.dominance.LCAGraphManager;
import common.util.objects.setDataStructures.ISet;
import common.util.objects.setDataStructures.SetType;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.exception.ContradictionException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

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
    }

    //***********************************************************************************
    // FIND MST
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
            Tree.getNeighborsOf(i).clear();
            ccTree.desactivateNode(i);
            ccTree.activateNode(i);
            size += g.getNeighborsOf(i).getSize();
        }
        if (size % 2 != 0) {
            throw new UnsupportedOperationException();
        }
        size /= 2;
        ISet nei;
        Integer[] integers = new Integer[size];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            nei = g.getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    integers[idx] = i * n + j;
                    costs[i * n + j] = distMatrix[i][j];
                    idx++;
                }
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
            indexOfArc[v / n][v % n] = idx;
        }
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    public void performPruning(double UB) throws ContradictionException {
        double delta = UB - treeCost;
        if (delta < 0) {
            throw new UnsupportedOperationException("mst>ub");
        }
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
            if (!Tree.edgeExists(sortedArcs[idx] / n, sortedArcs[idx] % n)) {
                propHK.remove(sortedArcs[idx] / n, sortedArcs[idx] % n);
                activeArcs.clear(idx);
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
        ccTree.addArc(cctRoot, 0);
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
            if (!Tree.edgeExists(sortedArcs[idx] / n, sortedArcs[idx] % n)) {
                propHK.remove(sortedArcs[idx] / n, sortedArcs[idx] % n);
                activeArcs.clear(idx);
            }
            idx = activeArcs.nextSetBit(idx + 1);
        }
        if (useful.cardinality() == 0) {
            return false;
        }
        //contract ccTree
        int p, s;
        for (int i = useful.nextClearBit(0); i < n; i = useful.nextClearBit(i + 1)) {
            ccTree.desactivateNode(i);
        }
        for (int i = ccTree.getActiveNodes().getFirstElement(); i >= 0; i = ccTree.getActiveNodes().getNextElement()) {
            s = ccTree.getSuccessorsOf(i).getFirstElement();
            if (s == -1) {
                if (i >= n) {
                    ccTree.desactivateNode(i);
                }
            } else if (ccTree.getSuccessorsOf(i).getNextElement() == -1) {
                p = ccTree.getPredecessorsOf(i).getFirstElement();
                ccTree.desactivateNode(i);
                if (p != -1) {
                    ccTree.addArc(p, s);
                }
            }
        }
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

    protected void pruning(int fi, double delta) throws ContradictionException {
        int i, j;
        double repCost;
        for (int arc = activeArcs.nextSetBit(fi); arc >= 0; arc = activeArcs.nextSetBit(arc + 1)) {
            i = sortedArcs[arc] / n;
            j = sortedArcs[arc] % n;
            if (!Tree.edgeExists(i, j)) {
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

//	BitSet marked = new BitSet(n*2);
//	private int getLCA(int i, int j) {
//		marked.clear();
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
