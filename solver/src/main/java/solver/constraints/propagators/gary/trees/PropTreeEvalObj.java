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

import common.ESat;
import memory.setDataStructures.ISet;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Compute the cost of the graph by summing edge costs
 * - For minimization problem
 */
public class PropTreeEvalObj extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar g;
    protected int n;
    protected IntVar sum;
    protected int[][] distMatrix;
    protected int[] lowestUnused;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropTreeEvalObj(UndirectedGraphVar graph, IntVar obj, int[][] costMatrix) {
        super(new Variable[]{graph, obj}, PropagatorPriority.LINEAR);
        g = graph;
        sum = obj;
        n = g.getEnvelopGraph().getNbNodes();
        distMatrix = costMatrix;
        lowestUnused = new int[n];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        int ce = 0;
        int ck = 0;
        int minCost = 1000000;
        int maxCost = 0;
        for (int i = 0; i < n; i++) {
            ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i < j) {
                    ce++;
                    max += distMatrix[i][j];
                    if (g.getKernelGraph().edgeExists(i, j)) {
                        min += distMatrix[i][j];
                        ck++;
                    } else {
                        if (distMatrix[i][j] > maxCost) {
                            maxCost = distMatrix[i][j];
                        }
                        if (distMatrix[i][j] < minCost) {
                            minCost = distMatrix[i][j];
                        }
                    }
                }
            }
        }
        int max2 = min + (n - 1 - ck) * maxCost;
        min += (n - 1 - ck) * minCost;
        sum.updateLowerBound(min, aCause);
        sum.updateUpperBound(max, aCause);
        sum.updateUpperBound(max2, aCause);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // not implemented
    }

    protected void filter(int minSum) throws ContradictionException {
        ISet succs;
        int delta = sum.getUB() - minSum;
        for (int i = 0; i < n; i++) {
            succs = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
                if (i < j && !g.getKernelGraph().edgeExists(i, j)) {
                    if (distMatrix[i][j] - lowestUnused[i] > delta) {
                        g.removeArc(i, j, aCause);
                    }
                }
            }
        }
    }
}
