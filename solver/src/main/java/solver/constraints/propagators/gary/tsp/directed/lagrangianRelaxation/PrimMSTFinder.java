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

package solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation;

import memory.setDataStructures.ISet;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.constraints.propagators.gary.tsp.specificHeaps.FastSimpleHeap;
import solver.constraints.propagators.gary.tsp.specificHeaps.ISimpleHeap;
import solver.exception.ContradictionException;
import solver.variables.graph.DirectedGraph;

import java.util.BitSet;

public class PrimMSTFinder extends AbstractMSTFinder {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    double[][] costs;
    ISimpleHeap heap;
    BitSet inTree;
    int tSize;
    double minVal;
    double maxTArc;
    int[] mate;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PrimMSTFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
        heap = new FastSimpleHeap(nbNodes);
        inTree = new BitSet(n);
        mate = new int[n * 2];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void computeMST(double[][] costs, DirectedGraph graph) throws ContradictionException {
        g = graph;
        for (int i = 0; i < n; i++) {
            Tree.getSuccessorsOf(i).clear();
            Tree.getPredecessorsOf(i).clear();
        }
        this.costs = costs;
        heap.clear();
        inTree.clear();
        treeCost = 0;
        tSize = 0;
        prim();
    }

    private void prim() throws ContradictionException {
        minVal = propHK.getMinArcVal();
        if (FILTER) {
            maxTArc = minVal;
        }
        addNode(0);
        int from, to;
        while (tSize < n - 1 && !heap.isEmpty()) {
            to = heap.removeFirstElement();
            from = mate[to];
            addArc(from, to);
        }
        if (tSize != n - 1) {
            propHK.contradiction();
        }
    }

    private void addArc(int from, int to) throws ContradictionException {
        if (from < n) {
            if (Tree.arcExists(to, from)) {
                return;
            }
            Tree.addArc(from, to);
            treeCost += costs[from][to];
            if (FILTER) {
                if (!propHK.isMandatory(from, to)) {
                    maxTArc = Math.max(maxTArc, costs[from][to]);
                }
            }
        } else {
            from -= n;
            if (Tree.arcExists(from, to)) {
                return;
            }
            Tree.addArc(to, from);
            treeCost += costs[to][from];
            if (FILTER) {
                if (!propHK.isMandatory(to, from)) {
                    maxTArc = Math.max(maxTArc, costs[to][from]);
                }
            }
        }
        tSize++;
        addNode(to);
    }

    private void addNode(int i) {
        if (!inTree.get(i)) {
            inTree.set(i);
            ISet nei = g.getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (!inTree.get(j)) {
                    if (propHK.isMandatory(i, j)) {
                        heap.addOrUpdateElement(j, Integer.MIN_VALUE);
                        mate[j] = i;
                    } else {
                        if (heap.addOrUpdateElement(j, costs[i][j])) {
                            mate[j] = i;
                        }
                    }
                }
            }
            nei = g.getPredecessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (!inTree.get(j)) {
                    if (propHK.isMandatory(j, i)) {
                        if (heap.addOrUpdateElement(j, Integer.MIN_VALUE)) {
                            mate[j] = i + n;
                        }
                    } else {
                        if (heap.addOrUpdateElement(j, costs[j][i])) {
                            mate[j] = i + n;
                        }
                    }
                }
            }
        }
    }

    public void performPruning(double UB) throws ContradictionException {
        if (FILTER) {
            double delta = UB - treeCost;
            ISet nei;
            for (int i = 0; i < n; i++) {
                nei = g.getSuccessorsOf(i);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if ((!Tree.arcExists(i, j)) && costs[i][j] - maxTArc > delta) {
                        propHK.remove(i, j);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("bound computation only, no filtering!");
        }
    }

    public double getRepCost(int from, int to) {
        return 0;//costs[from][to];// approximation of course
    }
}
