/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphOperations.coupling.BipartiteMaxCardMatching;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 * Main propagator for AllDifferent constraint
 * Uses Regin algorithm
 * Runs in O(m.rac(n)) worst case time
 * <p/>
 * Use incrementality for current matching and strongly connected components
 * but sometimes needs to recomputed everything from scratch
 * <p/>
 * BEWARE : pretty heavy and not so good in practice (especially because of domain restoring)
 *
 * @author Jean-Guillaume Fages
 * @param <V>
 */
public class PropAllDiffGraph2<V extends Variable> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n,n2;
	private DirectedGraphVar g;
	private DirectedGraph digraph;
	private int[] matching,nodeSCC;
	private Solver solver;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAllDiffGraph2(DirectedGraphVar graph, Solver sol, Constraint constraint) {
		super((V[]) new Variable[]{graph}, sol, constraint, PropagatorPriority.QUADRATIC);
		this.solver = sol;
		n = graph.getEnvelopGraph().getNbNodes();
		n2=2*n;
		g = graph;
		matching = new int[n2];
		nodeSCC = new int[n2];
		digraph = new DirectedGraph(n2, GraphType.LINKED_LIST);
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void buildDigraph() throws ContradictionException {
		for (int i = 0; i < n2; i++) {
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
			matching[i] = -1;
		}
		int size,j;
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			size = nei.neighborhoodSize();
			if(size==1){
				j = nei.getFirstElement()+n;
				digraph.addArc(j,i);
				matching[i] = j;
				matching[j] = i;
			}else{
				for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					digraph.addArc(i,j+n);
				}
			}
		}
	}

	private void buildSCC() {
		ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(digraph);
		int scc = 0;
		for (TIntArrayList in : allSCC) {
			for (int i = 0; i < in.size(); i++) {
				nodeSCC[in.get(i)] = scc;
			}
			scc++;
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void repairMatching() throws ContradictionException {
		BitSet iterable = new BitSet(n2);
		int[] A = new int[n];
		int i;
		for (i=0;i<n;i++) {
			A[i] = i;
		}
		BitSet free = new BitSet(n);
		for (i=0;i<n2;i++) {
			iterable.set(i);
			free.set(i, matching[i] == -1);
		}
		BipartiteMaxCardMatching.maxCardBipartiteMatching_HK(digraph, A, iterable, free, n);
		int p;
		for (i=0;i<n;i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			matching[i] = p;
			if(p!=-1){
				matching[p] = i;
			}
		}
	}

	//***********************************************************************************
	// PRUNING
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		buildDigraph();
		repairMatching();
		buildSCC();
		filter();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	private void filter() throws ContradictionException {
		INeighbors succ;
		int j;
		for (int node = 0;node<n;node++) {
			if (g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize() == 1) {
				j = g.getKernelGraph().getSuccessorsOf(node).getFirstElement()+n;
				if (matching[node] != j || matching[j] != node) {
					this.contradiction(g, "");
				}
			}else if (g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize() > 1) {
				this.contradiction(g, "");
			}else {
				succ = g.getEnvelopGraph().getSuccessorsOf(node);
				for (j = succ.getFirstElement(); j >= 0; j = succ.getNextElement()) {
					if (nodeSCC[node] != nodeSCC[j+n]) {
						if (matching[node] == j+n && matching[j+n] == node) {
							g.enforceArc(node, j, this);
						} else {
							g.removeArc(node, j, this);
						}
					}
				}
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		if(!g.instantiated()){
			return  ESat.UNDEFINED;
		}
		BitSet b = new BitSet(n);
		int next;
		for(int i=0;i<n;i++){
			next = g.getEnvelopGraph().getSuccessorsOf(i).getFirstElement();
			if(next!=-1){
				if(b.get(next)){
					return ESat.FALSE;
				}else{
					b.set(next);
				}
			}
		}
		return ESat.TRUE;
	}
}
