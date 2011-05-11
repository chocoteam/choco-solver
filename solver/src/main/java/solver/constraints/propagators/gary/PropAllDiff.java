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
package solver.constraints.propagators.gary;

import gnu.trove.TIntArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.gary.AllDiff;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphOperations.coupling.BipartiteMaxCardMatching;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class PropAllDiff<V extends Variable> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public static long duration;
	
	private int n;
	private int sizeFirstSet;
	private UndirectedGraphVar g;
	private StoredDirectedGraph digraph;
	private IStateInt[] storedMatching;
	private int[] matching;
	private Solver solver;
	private IStateInt[] nodeSCCref;
	private IStateInt[] nodeSCCnext;
	private IntProcedure maintain_matching;
	private IntProcedure maintain_scc;
	private boolean matchingZgueg;
	private long nbBks;
	private BitSet repairedSCC;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAllDiff(UndirectedGraphVar graph, int sizeFirstSet, Solver sol, AllDiff<V> constraint, PropagatorPriority storeThreshold, boolean b) {
		super((V[]) new Variable[]{graph}, sol.getEnvironment(), constraint, storeThreshold, b);
		this.sizeFirstSet = sizeFirstSet;
		this.solver = sol;
		n = graph.getEnvelopGraph().getNbNodes();
		g = graph;
		repairedSCC = new BitSet(n);
		matching = new int[n]; 
		storedMatching = new IStateInt[n]; 
		nodeSCCref  = new IStateInt[n+1];
		nodeSCCnext  = new IStateInt[n+1];
		for (int i=0;i<n; i++){
			matching[i] = -1;
			storedMatching[i] = environment.makeInt(-1);
		}
		for (int i=0;i<n+1; i++){
			nodeSCCref[i] = environment.makeInt(-1);
			nodeSCCnext[i] = environment.makeInt(-1);
		}
		digraph = new StoredDirectedGraph(solver.getEnvironment(), n+1,g.getEnvelopGraph().getType());
		maintain_matching = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				int from = i/n-1;
				int to   = i%n;
				digraph.removeEdge(from, to);
				if(matching[from]==to && matching[to] == from){
					matching[from] = -1;
					matching[to] = -1;
					matchingZgueg = true;
				}
			}
		};
		final BitSet bitSCC = new BitSet(n+1);
		maintain_scc = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				int from = i/n-1;
				int to   = i%n;
				int oldSCC = nodeSCCref[from].get();
				if(oldSCC==nodeSCCref[to].get() && !repairedSCC.get(oldSCC)){
					bitSCC.clear();
					int nodeI = oldSCC;
					while (nodeI!=-1){
						bitSCC.set(nodeI);
						nodeI = nodeSCCnext[nodeI].get();
					}
					LinkedList<TIntArrayList> allscc = StrongConnectivityFinder.findAllSCCOf(digraph, bitSCC);
					int size = allscc.size();
					if(size==1)return;
					int node,origin,first;
					for(TIntArrayList scc:allscc){
						origin = scc.get(0);
						repairedSCC.set(origin);
						nodeSCCref[origin].set(origin);
						first = origin;
						for(i=1;i<scc.size();i++){
							node = scc.get(i);
							nodeSCCref[node].set(origin);
							nodeSCCnext[first].set(node);
							first = node;
						}
						nodeSCCnext[first].set(-1);
					}
				}
			}
		};
	}


	//***********************************************************************************
	// BUILD
	//***********************************************************************************

	private void buildDigraph() {
		for(int i=0; i<n;i++){
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		int mate;
		int pot;
		AbstractNeighborsIterator<INeighbors> iter;
		ActiveNodesIterator<IActiveNodes> niter = g.getEnvelopGraph().activeNodesIterator();
		int i;
		while(niter.hasNext()){
			i = niter.next();
			if(i<sizeFirstSet){
				mate = matching[i];
				if(mate!=-1){
					digraph.addArc(mate, i);
				}
				iter = g.getEnvelopGraph().neighborsIteratorOf(i);
				while(iter.hasNext()){
					pot = iter.next();
					if(pot!=mate){
						digraph.addArc(i, pot);
					}
				}
			}else{
				if(matching[i]!=-1){
					digraph.addArc(n, i);
				}else{
					digraph.addArc(i, n);
				}
			}
		}
	}

	private void buildSCC() {
		LinkedList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(digraph);
		int origin,nodeI,nodeIPlus1;
		for(TIntArrayList in:allSCC){
			origin = in.get(0);
			nodeI = origin;
			nodeSCCref[origin].set(origin);
			for(int i=1;i<in.size();i++){
				nodeIPlus1 = in.get(i);
				nodeSCCref[nodeIPlus1].set(origin);
				nodeSCCnext[nodeI].set(nodeIPlus1);
				nodeI = nodeIPlus1;
			}
			nodeSCCnext[nodeI].set(-1);
		}
	}

	//***********************************************************************************
	// MATCHING      
	//***********************************************************************************

	private void repairMatching(){
		BitSet iterable = new BitSet(n+1);
		ActiveNodesIterator<IActiveNodes> niter = this.g.getEnvelopGraph().activeNodesIterator();
		int node;
		while(niter.hasNext()){
			node = niter.next();
				if(g.getKernelGraph().getNeighborhoodSize(node)==0){
					iterable.set(node);
				}else if(matching[node]==-1){
					iterable.set(node);
				}
		}
		int[] A = new int[sizeFirstSet];
		int i=0;
		for(node = iterable.nextSetBit(0); node>=0 && node<sizeFirstSet; node = iterable.nextSetBit(node+1)){
			A[i++] = node;
		}
		BitSet free = new BitSet(n);
		for (i=iterable.nextSetBit(0);i>=0;i=iterable.nextSetBit(i+1)){
			free.set(i,matching[i]==-1);
		}
		BipartiteMaxCardMatching.maxCardBipartiteMatching_HK(digraph, A,iterable,free, sizeFirstSet);
		int size;
		int mate;
		INeighbors succs;
		for (i=iterable.nextSetBit(sizeFirstSet);i>=0;i=iterable.nextSetBit(i+1)){
			digraph.removeArc(i, n);
			succs = digraph.getSuccessorsOf(i);
			size = succs.neighborhoodSize();
			if(size==1){
				mate = succs.getFirstElement();
				matching[i]=mate;
				matching[mate]=i;
				digraph.addArc(n, i);
			}else if(size==0){
				matching[i]=-1;
				digraph.addArc(i, n);
				digraph.removeArc(n, i);
			}else{
				throw new UnsupportedOperationException("not a matching");
			}
		}
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		for(int i=0;i<n;i++){
			matching[i] = -1;
			storedMatching[i].set(-1);
		}
		buildDigraph();
		repairMatching();
		buildSCC();
		filter();
		for(int i=0;i<n;i++){
			storedMatching[i].set(matching[i]);
		}
		nbBks = solver.getMeasures().getBackTrackCount();
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if(nbBks != solver.getMeasures().getBackTrackCount()){
//			int j;
//			for(int i=0;i<sizeFirstSet;i++){
//				j = digraph.getPredecessorsOf(i).getFirstElement();
//				if(matching[i]!=j){
//					matching[i]=j;
//					matching[j]=i;
//				}
//			}
			for(int i=0;i<n;i++){
				matching[i]=storedMatching[i].get();
			}
		}
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
			// maintient du COUPLAGE et des SCC
			matchingZgueg = false;
			d.forEach(maintain_matching, gv.fromArcRemoval(), gv.toArcRemoval());
			if(matchingZgueg){
				repairMatching();
				buildSCC();
				filter();
			}else{
				long time = System.currentTimeMillis();
				repairedSCC.clear();
				d.forEach(maintain_scc, gv.fromArcRemoval(), gv.toArcRemoval());
				duration += (System.currentTimeMillis()-time);
				filter();
			}
		}
		nbBks = solver.getMeasures().getBackTrackCount();
		for(int i=0;i<n;i++){
			storedMatching[i].set(matching[i]);
		}
	}

	private void filter() throws ContradictionException {
		ActiveNodesIterator<IActiveNodes> niter = g.getEnvelopGraph().activeNodesIterator();
		int i;
		AbstractNeighborsIterator<INeighbors> iter;
		int j;
		while(niter.hasNext()){
			i=niter.next();
			if(i>=sizeFirstSet)return;
			if(g.getKernelGraph().getNeighborhoodSize(i)==0){
				iter = g.getEnvelopGraph().neighborsIteratorOf(i);
				while(iter.hasNext()){
					j = iter.next();
					if(nodeSCCref[i].get()!=nodeSCCref[j].get()){
						if(matching[i]==j&&matching[j]==i){
							g.enforceArc(i, j, this);
						}else{
							g.removeArc(i, j, this);
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
		return ESat.UNDEFINED;
	}
}
