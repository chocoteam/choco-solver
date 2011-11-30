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

package solver.constraints.propagators.gary.tsp;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.S64BitSet;
import gnu.trove.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.LCAGraphManager;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import java.util.*;

public class PropBST<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	private IntVar obj;
	int n,ccN;
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
	private int[] ccTp, ccTGedge;
	private int treeCost;
	private LCAGraphManager lca;
	private int fromInterest, cctRoot;
	private BitSet useful;
	private IntProcedure arcRemoved;
	private int minTArc,maxTArc;
	private IStateInt nR;
	private INeighbors[] outArcs;
	private IStateInt[] sccOf;
	private int[] minCostOutArcs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropBST(DirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,cost}, solver, constraint, PropagatorPriority.CUBIC);
		g = graph;
		obj = cost;
		n = g.getEnvelopGraph().getNbNodes();
		ccN = 2*n-1;
		// backtrable
		activeArcs = new S64BitSet(environment,n*n);
		Tree = new UndirectedGraph(n,GraphType.LINKED_LIST);
		ccTree = new DirectedGraph(ccN,GraphType.LINKED_LIST);
		ccTGedge = new int[ccN];
		// not backtrable
		p = new int[n];
		rank = new int[n];
		ccTp = new int[n];
		minCostOutArcs = new int[n];
		//trivially not backtrable
		useful = new BitSet(n);
		costs = new int[n*n];
		arcRemoved = new RemArc();
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

	public void setRGStructure(IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		this.nR = nR;
		this.sccOf = sccOf;
		this.outArcs = outArcs;
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
	public void propagate() throws ContradictionException {
		for(int x=nR.get()-1;x>=0;x--){
			minCostOutArcs[x] = -1;
			for(int a=outArcs[x].getFirstElement();a>=0;a=outArcs[x].getNextElement()){
				if(minCostOutArcs[x]==-1 || costs[minCostOutArcs[x]]>costs[a-n]){
					minCostOutArcs[x] = a-n;
				}
			}
		}
		INIT();
		computeMST();
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if(request instanceof GraphRequest){
			GraphRequest gr = (GraphRequest) request;
			IntDelta d = g.getDelta().getArcRemovalDelta();
			d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
		}
		propagate();
	}

	private void pruning(int fi) throws ContradictionException {
		int[] next = new int[n];
		for(int i=0;i<n;i++){
			next[i] = -1;
		}
		int k=0;
		next[0]=0;
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(k);
		INeighbors nei;
		while(!list.isEmpty()){
			k = list.removeFirst();
			nei = Tree.getNeighborsOf(k);
			for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(next[s]==-1){
					next[s] = k;
					list.addFirst(s);
				}
			}
		}
		TIntIntHashMap map = new TIntIntHashMap();

		int repCost,i,j;
		int delta = obj.getUB()-treeCost;
		for(int arc=activeArcs.nextSetBit(fi); arc>=0; arc=activeArcs.nextSetBit(arc+1)){
			i = sortedArcs[arc]/n;
			j = sortedArcs[arc]%n;
			if(!Tree.arcExists(i,j)){
				if(sccOf[i].get()==sccOf[j].get()){
					repCost = costs[ccTGedge[lca.getLCA(i,j)]];
					truc(map,next,i,j, costs[arc]);
				}else{
					repCost = costs[minCostOutArcs[sccOf[i].get()]];
				}
				if(repCost + costs[i*n+j] > delta){
					g.removeArc(i,j,this,false);
					activeArcs.clear(arc);
				}
			}
			else if(activeArcs.get(indexOfArc[j][i]) && costs[i*n+j]-costs[j*n+i] > delta) {
				g.removeArc(i,j,this,false);
				activeArcs.clear(arc);
			}
		}
		for(i=0;i<n;i++){
			nei = Tree.getNeighborsOf(i);
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(Tree.arcExists(i,j) && !activeArcs.get(indexOfArc[j][i])){
					if(sccOf[i].get()==sccOf[j].get()){
						repCost = map.get(i*n+j);
						if(repCost-costs[i*n+j]>delta){
							g.enforceArc(i,j,this,false);
						}
					}
				}
			}
		}
	}

	private void truc(TIntIntHashMap map, int[] next, int i, int j, int cost) {
		BitSet visited = new BitSet(n);
		int meeting = j;
		int tmp;
		int a;
		for(a=i;a!=next[a];a=next[a]){
			visited.set(a);
		}
		visited.set(a);
		for(;!visited.get(meeting);meeting=next[meeting]){}
		for(int b=j;b!=meeting;){
			tmp = next[b];
			next[b] = meeting;
			map.put(b*n+tmp,cost);
			map.put(tmp*n+b,cost);
			b = tmp;
		}
		for(a=i;a!=meeting;){
			tmp = next[a];
			next[a] = meeting;
			map.put(a*n+tmp,cost);
			map.put(tmp*n+a,cost);
			a = tmp;
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
		treeCost = 0;
		// add mandatory arcs first
		cctRoot = n-1;
		int tSize = addMandatoryArcs();
		if(tSize==n-1){
			return;// problem solved
		}
		tSize += addOutArcs();
		if(tSize==n-1){
			return;// problem solved
		}
		// finish the MST with other arcs
		connectMST(tSize);
		// bound
		obj.updateLowerBound(treeCost, this,false);
		int delta = obj.getUB()-treeCost;
		// select relevant arcs
		if(selectRelevantArcs(delta)){
			lca.preprocess(cctRoot, ccTree);
			pruning(fromInterest);
		}
	}

	private int addMandatoryArcs() throws ContradictionException {
		int rFrom,rTo,next;
		int tSize = 0;
		for(int i=0; i<n;i++){
			next = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			if(next!=-1){
				rFrom = FIND(i, p);
				rTo   = FIND(next, p);
				if(rFrom != rTo){
					LINK(rFrom,rTo,rank,p);
					Tree.addEdge(i, next);
					updateCCTree(rFrom, rTo, i,next);
					treeCost += costs[i*n+next];
					tSize++;
				}else{
					contradiction(g,"cycle");
				}
			}
		}
		return tSize;
	}

	private int addOutArcs() throws ContradictionException {
		int rFrom,rTo, from,to;
		int tSize = 0;
		INeighbors nei;
		int minArc;
		for(int i=nR.get();i>=0;i--){
			nei = outArcs[i];
			if(nei.neighborhoodSize()>1){
				minArc = minCostOutArcs[i];
				from = minArc/n;
				to   = minArc%n;
				rFrom = FIND(from, p);
				rTo   = FIND(to, p);
				if(rFrom != rTo){
					LINK(rFrom, rTo, rank, p);
					Tree.addEdge(from, to);
					treeCost += costs[minArc];
					tSize++;
				}else{
					throw new UnsupportedOperationException("strange...");
				}
			}
		}
		return tSize;
	}

	private void connectMST(int tSize) throws ContradictionException {
		int from,to,rFrom,rTo;
		maxTArc = 0;
		minTArc = -1;
		int idx = activeArcs.nextSetBit(0);
		int cost;
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
				treeCost += cost;
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
	}

	private boolean selectRelevantArcs(int delta) throws ContradictionException {
		// Trivially no inference
		int idx = activeArcs.nextSetBit(0);
		while(idx>=0 && costs[sortedArcs[idx]]-minTArc <= delta){
			idx = activeArcs.nextSetBit(idx+1);
		}
		if(idx==-1){
			return false;
		}
		fromInterest = idx;
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
				g.removeArc(sortedArcs[idx]/n, sortedArcs[idx]%n, this,false);
				activeArcs.clear(idx);
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
		if(useful.cardinality()==0){
			return false;
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
				}
			}
		}
		cctRoot = -1;
		for(int i=ccTree.getActiveNodes().getFirstElement();i>=0;i=ccTree.getActiveNodes().getNextElement()){
			p = ccTree.getPredecessorsOf(i).getFirstElement();
			if(p==-1){
				if(cctRoot==-1){
					cctRoot = i;
				}else{
					ccTree.addArc(cctRoot,i);
				}
			}
		}
		return true;
	}

	private void updateCCTree(int rfrom, int rto, int from, int to) {
		cctRoot++;
		int newNode = cctRoot;
		ccTree.activateNode(newNode);
		ccTree.addArc(newNode,ccTp[rfrom]);
		ccTree.addArc(newNode,ccTp[rto]);
		ccTp[rfrom] = newNode;
		ccTp[rto] = newNode;
		ccTGedge[newNode] = from*n+to;
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
			activeArcs.clear(indexOfArc[i/n-1][i%n]);
		}
	}
}
