package samples.parallel; /**
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

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.PoolManager;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.PropSymmetricHeldKarp;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.strategy.assignments.GraphAssignment;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.graph.GraphDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 21/05/12
 * Time: 14:27
 */

public class Fragment {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

//	private static final int TIMELIMIT = 10000;
	//model
	private int[] fragToReal,outputFragment;
	private int[][] distMatrix;
	private int n,ub,outputCost;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public Fragment(int size){
		super();
		n = size+1;
		distMatrix = new int[n][n];
		fragToReal = new int[n-1];
		outputFragment = new int[n-1];
	}

	//***********************************************************************************
	// INITIALIZATION
	//***********************************************************************************

	public void set(int[] frag, int[][] bigMatrix){
		fragToReal = frag;
		ub = 0;
		for(int i=0;i<n-1;i++){
			outputFragment[i] = fragToReal[i];
			for(int j=i+1;j<n-1;j++){
				distMatrix[j][i] = distMatrix[i][j] = bigMatrix[fragToReal[i]][fragToReal[j]];
			}
			if(i<n-2){
				ub += bigMatrix[frag[i]][frag[i+1]];
			}
		}
		outputCost = ub;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void solve(){
		final Solver solver = new Solver();
		// variables
		final IntVar totalCost = VariableFactory.bounded("obj", 0, ub, solver);
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		for(int i=0;i<n;i++){
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n-1;j++){
				undi.getEnvelopGraph().addEdge(i,j);
			}
		}
		undi.getEnvelopGraph().addEdge(0,n-1);
		undi.getEnvelopGraph().addEdge(n-2,n-1);
		undi.getKernelGraph().addEdge(0,n-1);
		undi.getKernelGraph().addEdge(n-2,n-1);
		// constraints
		final Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
		gc.addPropagators(new PropAtLeastNNeighbors(undi, 2, gc, solver));
		gc.addPropagators(new PropAtMostNNeighbors(undi, 2, gc, solver));
		gc.addPropagators(new PropCycleEvalObj(undi, totalCost, distMatrix, gc, solver));
		gc.addPropagators(PropSymmetricHeldKarp.oneTreeBasedRelaxation(undi, totalCost, distMatrix, gc, solver));
		solver.post(gc);
		// config
		solver.set(new FragSearch(undi));
//		solver.set(StrategyFactory.graphTSP(undi,TSP_heuristics.enf_sparse,null));
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
//		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		// resolution
//		solver.findSolution();
//		outputCost = totalCost.getValue();
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,totalCost);
		//output
		if(solver.getMeasures().getSolutionCount()==0||!undi.instantiated()){
			throw new UnsupportedOperationException("SOL#"+solver.getMeasures().getSolutionCount());
		}
		outputCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		if(outputCost>ub){
			throw new UnsupportedOperationException(outputCost+">"+ub);
		}
		int x = 0;
		INeighbors nei = undi.getEnvelopGraph().getSuccessorsOf(x);
		int y = nei.getFirstElement();
		if(y==n-1){
			y = nei.getNextElement();
		}
		int tmp;
		for(int i=0;i<n-1;i++){
			outputFragment[i] = fragToReal[x];
			tmp = x;
			x = y;
			nei = undi.getEnvelopGraph().getSuccessorsOf(x);
			y = nei.getFirstElement();
			if(y==tmp){
				y = nei.getNextElement();
			}
		}
		if(outputFragment[0]!=fragToReal[0] || outputFragment[n-2]!=fragToReal[n-2]){
			throw new UnsupportedOperationException();
		}
		Parallelized_LNS.jobFinished();
	}

	public void lazyStart() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				solve();
			}
		});
		t.start();
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public int[] getInputFragment() {
		return fragToReal;
	}

	public int[] getOutputFragment() {
		return outputFragment;
	}

	public int getOutputCost() {
		return outputCost;
	}

	//***********************************************************************************
	// SEARCH
	//***********************************************************************************

	private class FragSearch extends AbstractStrategy {
		UndirectedGraphVar g;
		int n, currentNode;
		PoolManager<GraphDecision> pool;
		private int[] e;

		FragSearch(UndirectedGraphVar g){
			super(new GraphVar[]{g});
			this.g = g;
			this.n = g.getEnvelopGraph().getNbNodes();
			pool = new PoolManager<GraphDecision>();
		}
		@Override
		public void init() {
			e = new int[n];
			currentNode = -1;
		}
		@Override
		public Decision getDecision() {
			if(g.instantiated()){
				return null;
			}
			if (currentNode==-1 || g.getEnvelopGraph().getSuccessorsOf(currentNode).neighborhoodSize()==2){
				currentNode = getNextSparseNode(g,n);
			}
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(currentNode);
			int maxE = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!g.getKernelGraph().arcExists(currentNode,j)){
					if(maxE == -1 || e[maxE]<e[j]){
						maxE=j;
					}
				}
			}
			if(maxE==-1){
				throw new UnsupportedOperationException();
			}
			GraphDecision fd = pool.getE();
			if(fd==null){
				fd = new GraphDecision(pool);
			}
			fd.setArc(g,currentNode, maxE, GraphAssignment.graph_enforcer);
			return fd;
		}

		private int getNextSparseNode(UndirectedGraphVar g, int n) {
			int s = n;
			int si;
			for (int i = 0; i < n; i++) {
				si = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
				if(si<s && si>2){
					s = si;
				}
			}
			INeighbors nei;
			for (int i = 0; i < n; i++) {
				e[i] = 0;
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				for(int j=0;j<n;j++){
					if(g.getEnvelopGraph().getSuccessorsOf(j).neighborhoodSize()==s){
						e[i]++;
					}
				}
			}
			int bestScore = -1;
			int score;
			int node = -1;
			for (int i = 0; i < n; i++) {
				if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()==s){
					nei = g.getEnvelopGraph().getSuccessorsOf(i);
					score = 0;
					for(int j=0;j<n;j++){
						score += e[j];
					}
					if(score>bestScore){
						bestScore = score;
						node = i;
					}
				}
			}
			if(node == -1){
				throw new UnsupportedOperationException();
			}
			return node;
		}
	}
}
