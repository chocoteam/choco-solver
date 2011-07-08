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
import gnu.trove.TIntArrayList;
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
import solver.variables.graph.GraphTools;
import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.FlowGraphManager;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

public class PropNTree<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nTree;
	int minTree = 0;
	private LinkedList<TIntArrayList> sinks;
	private LinkedList<TIntArrayList> nonSinks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNTree(DirectedGraphVar graph, IntVar nT,Solver solver,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion) {
		super((V[]) new Variable[]{graph,nT}, solver, constraint, priority, reactOnPromotion);
		g = graph;
		nTree = nT;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private boolean checkFeasibility() {
		int n = g.getEnvelopGraph().getNbNodes();
		computeSinks();
		int MINTREE = minTree;
		int MAXTREE = calcMaxTree();
		INeighbors nei;
		if (nTree.getLB()<=MAXTREE && nTree.getUB()>=MINTREE){
			IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
			DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());//ATENTION TYPE
			for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
				if (g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>1){
					return false;
				}
				nei = g.getEnvelopGraph().getSuccessorsOf(node);
				for(int suc=nei.getFirstElement(); suc>=0; suc = nei.getNextElement()){
					Grs.addArc(suc, node);
					if(suc==node){
						Grs.addArc(node, n);
						Grs.addArc(n, node);
					}
				}
			}
			int[] numDFS = GraphTools.performDFS(n, Grs);
//			boolean rootFound = false;
			for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
				if(numDFS[node]==0){
					return false;
				}
			}
//			for(int i:numDFS){
//				if(rootFound && i==0){
//					System.out.println("relou");
//					return false;
//				}
//				if(i==0)rootFound = true;
//			}
		}else{
			return false;
		}
		return true;
	}

	private int calcMaxTree() {
		int ct = 0;
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
			if (g.getEnvelopGraph().arcExists(node, node)){
				ct++;
			}
		}
		return ct;
	}

	private void filtering() throws ContradictionException{
		computeSinks();
		//1) Bound pruning
		minTreePruning(); // MAXTREE pruning is done by PropNLoops
		//2) structural pruning
		structuralPruning();
	}

	@Override
	public void propagate() throws ContradictionException {
		if(!checkFeasibility()){
			this.contradiction(g, "infeasible");
		}else{
//			structuralPruning();
			filtering();
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			if(!checkFeasibility()){
				this.contradiction(g, "infeasible");
			}else{
				filtering();
			}
		}
	}

	private void structuralPruning() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());
		INeighbors nei;
		for (int node = 0; node<n; node++) {
			if(env.isActive(node)){
				nei = g.getEnvelopGraph().getSuccessorsOf(node);
				for(int suc = nei.getFirstElement() ; suc>=0; suc = nei.getNextElement()){
					Grs.addArc(suc, node);
					if(suc==node){
						Grs.addArc(node, n);
						Grs.addArc(n, node);
					}
				}
			} else{ // enables to manage deleted nodes
				Grs.addArc(node, n);
				Grs.addArc(n, node);
			}
		}
		//dominators
		FlowGraphManager flowGM = new FlowGraphManager(n, Grs); 
		//LCA preprocessing
		DirectedGraph dominatorGraph = new DirectedGraph(n+1, GraphType.SPARSE);
		for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
			dominatorGraph.addArc(flowGM.getImmediateDominatorsOf(node), node);
		}
		//PREPROCESSING
		int[] in = new int[n+1];
		int[] out = new int[n+1];
		int[] father = new int[n+1];
		INeighbors[] successors = new INeighbors[n+1];
		BitSet notFirsts = new BitSet(n+1);
		for (int i=0; i<n+1; i++){
			father[i] = -1;
			successors[i] = dominatorGraph.getSuccessorsOf(i);
		}
		int time = 0;
		int currentNode = n;
		int nextNode;
		father[n] = n;
		in[n] = 0;
		boolean notFinished = true;
		while(notFinished){
			if(notFirsts.get(currentNode)){
				nextNode = successors[currentNode].getNextElement();
			}else{
				notFirsts.set(currentNode);
				nextNode = successors[currentNode].getFirstElement();
			}
			if(nextNode<0){
				time++;
				out[currentNode] = time;
				if(currentNode==n){
					notFinished = false;
					break;
				}
				currentNode = father[currentNode];
			}else{
				if (father[nextNode]==-1) {
					time++;
					in[nextNode] = time;
					father[nextNode] = currentNode;
					currentNode = nextNode;
				}
			}
		}
		time++;
		out[n] = time;
		//END_PREPROCESSING
		//queries
		for (int node=env.getFirstElement();node>=0;node=env.getNextElement()){
			nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int suc = nei.getFirstElement(); suc>=0; suc = nei.getNextElement()){
				//--- STANDART PRUNING
				if (node != suc && in[suc]>in[node] && out[suc]<out[node]){
					g.removeArc(node, suc, this);
				}
			}
		}
	}

	private void minTreePruning() throws ContradictionException {
		nTree.updateLowerBound(minTree, this);
		if (nTree.getUB()==minTree){
			int node;
			for (TIntArrayList scc:nonSinks){
				for(int x=0;x<scc.size();x++){
					node = scc.get(x);
					if(g.getEnvelopGraph().arcExists(node, node)){
						g.removeArc(node, node, this);
					}
				}
			}
		}
	}

	private void computeSinks() {
		int n = g.getEnvelopGraph().getNbNodes();
		ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(g.getEnvelopGraph());
		int[] sccOf = new int[n];
		int sccNum = 0;
		int node;
		for (TIntArrayList scc:allSCC){
			for(int x=0;x<scc.size();x++){
				sccOf[scc.get(x)] = sccNum;
			}
			sccNum++;
		}
		sinks = new LinkedList<TIntArrayList>();
		nonSinks = new LinkedList<TIntArrayList>();
		boolean looksSink = true;
		INeighbors nei;
		for (TIntArrayList scc:allSCC){
			looksSink = true;
			for(int x=0;x<scc.size();x++){
				node = scc.get(x);
				nei = g.getEnvelopGraph().getSuccessorsOf(node);
				for(int suc = nei.getFirstElement(); suc>=0 && looksSink; suc = nei.getNextElement()){
					if (sccOf[suc]!=sccOf[node]){
						looksSink = false;
					}
				}
				if(!looksSink){
					x = scc.size();
				}
			}
			if(looksSink){
				sinks.add(scc);
			}else{
				nonSinks.add(scc);
			}
		}
		minTree = sinks.size();
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.REMOVENODE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
