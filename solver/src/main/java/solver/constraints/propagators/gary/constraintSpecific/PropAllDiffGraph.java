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
import gnu.trove.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphOperations.coupling.BipartiteMaxCardMatching;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.ArrayList;
import java.util.BitSet;

/**Main propagator for AllDifferent constraint
 * Uses Regin algorithm
 * Runs in O(m.rac(n)) worst case time
 * 
 * Use incrementality for current matching and strongly connected components
 * but sometimes needs to recomputed everything from scratch
 * 
 * BEWARE : pretty heavy and not so good in practice (especially because of domain restoring)
 * 
 * @author Jean-Guillaume Fages
 *
 * @param <V>
 */
public class PropAllDiffGraph<V extends Variable> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

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
	private boolean obsoleteMatching;
	private long nbBks;
	private BitSet repairedSCC;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAllDiffGraph(UndirectedGraphVar graph, int sizeFirstSet, Solver sol, Constraint constraint, PropagatorPriority storeThreshold, boolean b) {
		super((V[]) new Variable[]{graph}, sol, constraint, storeThreshold, b);
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
		digraph = new StoredDirectedGraph(solver.getEnvironment(), n+1,GraphType.SPARSE);
		maintain_matching = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				int from = i/n-1;
				int to   = i%n;
				digraph.removeEdge(from, to);
				if(matching[from]==to && matching[to] == from){
					matching[from] = -1;
					matching[to] = -1;
					obsoleteMatching = true;
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
					ArrayList<TIntArrayList> allscc = StrongConnectivityFinder.findAllSCCOf(digraph, bitSCC);
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
	// Initialization
	//***********************************************************************************

	private void buildDigraph() {
		for(int i=0; i<n;i++){
			digraph.getSuccessorsOf(i).clear();
			digraph.getPredecessorsOf(i).clear();
		}
		int mate;
		int pot;
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		INeighbors nei;
		for (int i = act.getFirstElement(); i>=0; i = act.getNextElement()) {
			if(i<sizeFirstSet){
				mate = matching[i];
				if(mate!=-1){
					digraph.addArc(mate, i);
				}
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				for(pot = nei.getFirstElement(); pot>=0; pot = nei.getNextElement()){
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
		ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(digraph);
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

	private void repairMatching() throws ContradictionException{
		BitSet iterable = new BitSet(n+1);
		IActiveNodes act = this.g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(g.getKernelGraph().getNeighborhoodSize(node)==0 || matching[node]==-1){// BEWARE should be good but brought mistakes once (before some bug corrections)
				iterable.set(node);
			}
		}
		int[] A = new int[sizeFirstSet];
		int i=0;
		for(int node = iterable.nextSetBit(0); node>=0 && node<sizeFirstSet; node = iterable.nextSetBit(node+1)){
			A[i++] = node;
		}
		BitSet free = new BitSet(n);
		for (i=iterable.nextSetBit(0);i>=0;i=iterable.nextSetBit(i+1)){
			free.set(i,matching[i]==-1);
		}
		BipartiteMaxCardMatching.maxCardBipartiteMatching_HK(digraph, A,iterable,free, sizeFirstSet);
		int size;
		int mate;
		INeighbors preds;
		for (i=iterable.nextSetBit(0);i>=0;i=iterable.nextSetBit(i+1)){
			if(i<sizeFirstSet){
				preds = digraph.getPredecessorsOf(i);
				size = preds.neighborhoodSize();
				if(size==1){
					mate = preds.getFirstElement();
					if(matching[i]!=mate){
						matching[i]=mate;
						matching[mate]=i;
					}
					if(digraph.addArc(n, mate)){
						digraph.removeArc(mate,n);
					}
					iterable.clear(mate);
				}else{
					this.contradiction(g, "");
				}
			}else{
				if(matching[i]!=-1){
					matching[i]=-1;
				}
				if(digraph.addArc(i, n)){
					digraph.removeArc(n, i);
				}
			}
		}
	}

	//***********************************************************************************
	// PRUNING
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
			for(int i=0;i<n;i++){
				matching[i]=storedMatching[i].get();
			}
			nbBks = solver.getMeasures().getBackTrackCount();
		}
		if(mask!=EventType.REMOVEARC.mask)return;
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
			// maintient du COUPLAGE et des SCC
			obsoleteMatching = false;
			d.forEach(maintain_matching, gv.fromArcRemoval(), gv.toArcRemoval());
			if(obsoleteMatching){
				repairMatching();
				buildSCC();
				filter();
				for(int i=0;i<n;i++){
					storedMatching[i].set(matching[i]);
				}
			}else{
				repairedSCC.clear();
				d.forEach(maintain_scc, gv.fromArcRemoval(), gv.toArcRemoval());
				filter();
			}
		}
	}

	private void filter() throws ContradictionException {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		INeighbors nei;
		int j;
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(node>=sizeFirstSet)return;
			if(g.getKernelGraph().getNeighborhoodSize(node)==0){
				nei = g.getEnvelopGraph().getNeighborsOf(node);
				for(j = nei.getFirstElement(); j>=0; j = nei.getNextElement()){
					if(nodeSCCref[node].get()!=nodeSCCref[j].get()){
						if(matching[node]==j&&matching[j]==node){
							g.enforceArc(node, j, this);
						}else{
							g.removeArc(node, j, this);
						}
					}
				}
			}
		}
		act = g.getKernelGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(node>=sizeFirstSet)return;
			if(g.getKernelGraph().getNeighborhoodSize(node)==1){
				j = g.getKernelGraph().getNeighborsOf(node).getFirstElement();
				if(matching[node]!=j || matching[j]!=node){
					this.contradiction(g, "");
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
		if(g.instantiated()){
			return ESat.TRUE;
		}else{
			return ESat.UNDEFINED;
		}
	}
}
