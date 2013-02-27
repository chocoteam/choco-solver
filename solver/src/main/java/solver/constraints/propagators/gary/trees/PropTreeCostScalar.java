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
import common.util.objects.setDataStructures.ISet;
import common.util.procedure.PairProcedure;
import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Compute the cost of the graph by summing edge costs
 * - For minimization problem
 */
public class PropTreeCostScalar extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected UndirectedGraphVar g;
	private IGraphDeltaMonitor gdm;
	private PairProcedure edgeEnf, edgeRem;
    protected int n;
    protected IntVar sum;
    protected int[][] distMatrix;
	private IStateInt minSum,maxSum;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropTreeCostScalar(UndirectedGraphVar graph, IntVar obj, int[][] costMatrix) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR);
        g = graph;
        sum = obj;
        n = g.getEnvelopGraph().getNbNodes();
        distMatrix = costMatrix;
		minSum = environment.makeInt(0);
		maxSum = environment.makeInt(0);
		gdm = g.monitorDelta(this);
		edgeEnf = new PairProcedure() {
			@Override
			public void execute(int i, int j) throws ContradictionException {
				minSum.add(distMatrix[i][j]);
			}
		};
		edgeRem = new PairProcedure() {
			@Override
			public void execute(int i, int j) throws ContradictionException {
				maxSum.add(-distMatrix[i][j]);
			}
		};
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int min = 0;
        int max = 0;
        for (int i = 0; i < n; i++) {
            ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (i <= j) {
                    max += distMatrix[i][j];
                    if (g.getKernelGraph().edgeExists(i, j)) {
                        min += distMatrix[i][j];
                    }
                }
            }
        }
		minSum.set(min);
		maxSum.set(max);
        sum.updateLowerBound(min, aCause);
        sum.updateUpperBound(max, aCause);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
		gdm.forEachArc(edgeEnf, EventType.ENFORCEARC);
		gdm.forEachArc(edgeRem, EventType.REMOVEARC);
		gdm.unfreeze();
		sum.updateLowerBound(minSum.get(), aCause);
		sum.updateUpperBound(maxSum.get(), aCause);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
    }

    @Override
    public ESat isEntailed() {
		int min = 0;
		int max = 0;
		for (int i = 0; i < n; i++) {
			ISet nei = g.getEnvelopGraph().getNeighborsOf(i);
			for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
				if (i <= j) {
					max += distMatrix[i][j];
					if (g.getKernelGraph().edgeExists(i, j)) {
						min += distMatrix[i][j];
					}
				}
			}
		}
		if(min>sum.getUB() || max<sum.getLB()){
			return ESat.FALSE;
		}
		if(min == max){
			return ESat.TRUE;
		}else{
			return ESat.UNDEFINED;
		}
    }
}
