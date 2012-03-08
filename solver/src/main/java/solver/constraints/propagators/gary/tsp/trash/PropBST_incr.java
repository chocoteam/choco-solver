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

package solver.constraints.propagators.gary.tsp.trash;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.S64BitSet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.measure.IMeasures;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.LCAGraphManager;
import solver.variables.graph.undirectedGraph.StoredUndirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

public class PropBST_incr<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	private IntVar obj;
	int n,ccN,nCCT;
	// indexes are sorted
	int[] sortedArcs;   // from sorted to lex
	int[][] indexOfArc; // from lex (i,j) to sorted (i+1)*n+j
	private IStateBitSet activeArcs; // if sorted is active
	// UNSORTED
	private int[] costs;			 // cost of the lex arc
	// Kruskal
	int[] p, rank;
	private UndirectedGraph Tree;
	// CC stuff
	private DirectedGraph ccTree;
	private int[] ccTp;
	private IStateInt[] ccTGedge;
	private IStateInt treeCost;
	private boolean treeBroken;
	private boolean newEnf;
	private LCAGraphManager lca;
	private IStateInt fromInterest, cctRoot;
	private BitSet useful;
	private IntProcedure arcEnforced,arcRemoved;
	private long wIndex;
	private IMeasures mesures;
	private IStateInt minTArc,maxTArc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropBST_incr(DirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.QUADRATIC);
		g = graph;
		obj = cost;
		mesures = solver.getMeasures();
		n = g.getEnvelopGraph().getNbNodes();
		ccN = 2*n-1;
		// backtrable
		activeArcs = new S64BitSet(environment,n*n);
		Tree = new StoredUndirectedGraph(environment,n,GraphType.LINKED_LIST);
		ccTree = new StoredDirectedGraph(environment,ccN,GraphType.LINKED_LIST);
		ccTGedge = new IStateInt[ccN];
		fromInterest = environment.makeInt();
		cctRoot = environment.makeInt();
		minTArc = environment.makeInt();
		maxTArc = environment.makeInt();
		treeCost= environment.makeInt();
		for(int i=0;i<ccN;i++){
			ccTGedge[i] = environment.makeInt();
		}
		// not backtrable
		p = new int[n];
		rank = new int[n];
		ccTp = new int[n];
		//trivially not backtrable
		useful = new BitSet(n);
		costs = new int[n*n];
		arcRemoved = new RemArc();
		arcEnforced= new EnfArc();
		sortedArcs = new int[n*n];
		indexOfArc = new int[n][n];
		lca = new LCAGraphManager(ccN);
		sortArcs(costMatrix);
	}

	private void sortArcs(int[][] costMatrix){
		Integer[] integers = new Integer[n*n];
		Comparator<Integer> comp = new Comparator<Integer>(){
			@Override
			public int compare(Integer i1, Integer i2) {
				return costs[i1]-costs[i2];
			}
		};
		int idx=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<n; j++){
				integers[idx]=i*n+j;
				costs[i*n+j] = costMatrix[i][j];
				idx++;
			}
		}
		Arrays.sort(integers,comp);
		int v;
		for(idx = 0; idx<sortedArcs.length; idx++){
			v = integers[idx];
			sortedArcs[idx] = v;
			indexOfArc[v/n][v%n] = idx;
			if(g.getEnvelopGraph().arcExists(v/n,v%n)){
				activeArcs.set(idx);
			}
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INIT();
		computeMST();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if((vars[idxVarInProp].getTypeAndKind() & Variable.GRAPH)!=0) {
			treeBroken = false;
			eventRecorder.getDeltaMonitor(this, g).forEach(arcRemoved, EventType.REMOVEARC);
			if(treeBroken){
				propagate(EventType.FULL_PROPAGATION.mask); // RECOMPUTE from oldT
			}else{
				newEnf = false;
				eventRecorder.getDeltaMonitor(this, g).forEach(arcEnforced, EventType.ENFORCEARC);
				if(newEnf){
					propagate(EventType.FULL_PROPAGATION.mask);
				}else{
					int delta = obj.getUB()-treeCost.get();
					selectRelevantArcs(delta);
					lca.preprocess(cctRoot.get(), ccTree);
					pruning(fromInterest.get());
				}
			}
		}else{
			propagate(EventType.FULL_PROPAGATION.mask);
		}
	}



	private void pruning(int fi) throws ContradictionException {
		int repCost,i,j;
		int treeCost = this.treeCost.get();
		for(int arc=activeArcs.nextSetBit(fi); arc>=0; arc=activeArcs.nextSetBit(arc+1)){
			i = sortedArcs[arc]/n;
			j = sortedArcs[arc]%n;
			if(!Tree.arcExists(i,j)){
				repCost = costs[ccTGedge[lca.getLCA(i,j)].get()];
				if(treeCost - repCost + costs[i*n+j] > obj.getUB()){
					g.removeArc(i,j,this);
					activeArcs.clear(arc);
				}
			}
			else if(activeArcs.get(indexOfArc[j][i]) && treeCost-costs[j*n+i]+costs[i*n+j] > obj.getUB()) {
				g.removeArc(i,j,this);
				activeArcs.clear(arc);
			}
		}
	}

	private void INIT(){
		for(int i=0; i<n; i++){
			p[i] = i;
			rank[i] = 0;
			ccTree.desactivateNode(i);
			ccTree.activateNode(i);
			Tree.desactivateNode(i);
			Tree.activateNode(i);
			ccTp[i] = i;
		}
		for(int i=n; i<ccN; i++){
			ccTree.desactivateNode(i);
		}
	}

	//***********************************************************************************
	// Kruskal's
	//***********************************************************************************

	private void computeMST() throws ContradictionException {
		treeCost.set(0);
		// add mandatory arcs first
		nCCT = n-1;
		int tSize = addMandatoryArcs();
		if(tSize==n-1){
			return;// problem solved
		}
		// finish the MST with other arcs
		int idx = connectMST(tSize);
		cctRoot.set(nCCT);
		// bound
		obj.updateLowerBound(treeCost.get(), this);
		int delta = obj.getUB()-treeCost.get();
		// select relevant arcs
		selectRelevantArcs(delta);
		lca.preprocess(cctRoot.get(), ccTree);
		pruning(fromInterest.get());
	}

	private int addMandatoryArcs() throws ContradictionException {
		int rFrom,rTo,next;
		int tSize = 0;
		int cost = 0;
		for(int i=0; i<n;i++){
			next = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			if(next!=-1){
				rFrom = FIND(i, p);
				rTo   = FIND(next, p);
				if(rFrom != rTo){
					LINK(rFrom,rTo,rank,p);
					Tree.addEdge(i, next);
					updateCCTree(rFrom, rTo, i,next);
					cost += costs[i*n+next];
					tSize++;
				}else{
					contradiction(g,"cycle");
				}
			}
		}
		treeCost.add(cost);
		return tSize;
	}

	private int connectMST(int tSize) throws ContradictionException {
		int from,to,rFrom,rTo;
		int maxTArc = 0;
		int minTArc = -1;
		int idx = activeArcs.nextSetBit(0);
		int cost;
		int tc = 0;
		while(tSize < n-1){
			if(idx<0){
				contradiction(g, "disconnected");
			}
			from = sortedArcs[idx]/n;
			to = sortedArcs[idx]%n;
			rFrom = FIND(from, p);
			rTo   = FIND(to, p);
			if(rFrom != rTo){
				LINK(rFrom,rTo,rank,p);
				Tree.addEdge(from, to);
				updateCCTree(rFrom, rTo, from, to);
				cost = costs[sortedArcs[idx]];
				tc += cost;
				if (maxTArc < cost){
					maxTArc = cost;
				}
				if (minTArc == -1 || minTArc > cost){
					minTArc = cost;
				}
				tSize++;
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
		this.treeCost.add(tc);
		this.minTArc.set(minTArc);
		this.maxTArc.set(maxTArc);
		return idx;
	}

	private void selectRelevantArcs(int delta) throws ContradictionException {
		// Trivially no inference
		int idx = activeArcs.nextSetBit(0);
		int minTArc = this.minTArc.get();
		int maxTArc = this.maxTArc.get();
		while(idx>=0 && costs[sortedArcs[idx]]-minTArc <= delta){
			idx = activeArcs.nextSetBit(idx+1);
		}
		if(idx==-1){
			return;
		}
		fromInterest.set(idx);
		// Maybe interesting
		useful.clear();
		while(idx>=0 && costs[sortedArcs[idx]]-maxTArc <= delta){
			useful.set(sortedArcs[idx]/n);
			useful.set(sortedArcs[idx]%n);
			idx = activeArcs.nextSetBit(idx+1);
		}
		// Trivially infeasible arcs
		while(idx>=0){
			if(!Tree.arcExists(sortedArcs[idx]/n, sortedArcs[idx]%n)){
				g.removeArc(sortedArcs[idx]/n, sortedArcs[idx]%n, this);
				activeArcs.clear(idx);
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
		if(useful.cardinality()==0){
			return;
		}
		// contract ccTree
		int p,s;
		for(int i=useful.nextClearBit(0);i<n;i=useful.nextClearBit(i+1)){
			ccTree.desactivateNode(i);
		}
		for(int i=ccTree.getActiveNodes().getFirstElement();i>=0;i=ccTree.getActiveNodes().getNextElement()){
			s = ccTree.getSuccessorsOf(i).getFirstElement();
			if(s==-1){
				if(i>=n){
					ccTree.desactivateNode(i);
				}
			}else if (ccTree.getSuccessorsOf(i).getNextElement()==-1){
				p = ccTree.getPredecessorsOf(i).getFirstElement();
				ccTree.desactivateNode(i);
				if(p!=-1){
					ccTree.addArc(p,s);
				}else{
					cctRoot.set(s);
				}
			}
		}
	}

	private void updateCCTree(int rfrom, int rto, int from, int to) {
		nCCT++;
		int newNode = nCCT;
		ccTree.activateNode(newNode);
		ccTree.addArc(newNode,ccTp[rfrom]);
		ccTree.addArc(newNode,ccTp[rto]);
		ccTp[rfrom] = newNode;
		ccTp[rto] = newNode;
		ccTGedge[newNode].set(from*n+to);
	}

	private void LINK(int x, int y, int[] rank, int[] p) {
		if(rank[x]>rank[y]){
			p[y] = x;
		}else{
			p[x] = y;
		}
		if(rank[x] == rank[y]){
			rank[y]++;
		}
	}

	private int FIND(int i, int[] p) {
		if(p[i]!=i){
			p[i] = FIND(p[i],p);
		}
		return p[i];
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemArc implements IntProcedure{

		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			activeArcs.clear(indexOfArc[from][to]);
			if(Tree.arcExists(from,to) && !activeArcs.get(indexOfArc[to][from])){
				treeBroken = true;
			}
		}
	}

	private class EnfArc implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(!Tree.arcExists(from,to)){
				newEnf = true;
//				int toRem = lca.getLCA(from,to);
//				treeCost += costs[indexOfArc[from][to]] - costs[indexOfArc[toRem/n][toRem%n]];
//				int ancestor = ccTree.getPredecessorsOf(toRem).getFirstElement();
//				int ci = ccTree.getSuccessorsOf(toRem).getFirstElement();
//				int cj = ccTree.getSuccessorsOf(toRem).getNextElement();
//				int cci = costs[ccTGedge[ci]];
//				int ccj = costs[ccTGedge[cj]];
//				if(cci<ccj){
//					if(ancestor!=-1){
//						ccTree.addArc(ancestor,cj);
//					}
//					ccTree.addArc(cj,ci);
//					ccTree.desactivateNode(toRem);
//				}else{
//					if(ancestor!=-1){
//						ccTree.addArc(ancestor,ci);
//					}
//					ccTree.addArc(ci,cj);
//					ccTree.desactivateNode(toRem);
//				}
			}
		}
	}
}
