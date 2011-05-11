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

package solver.constraints.gary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.PropNLoops;
import solver.constraints.propagators.gary.PropNSuccs;
import solver.constraints.propagators.gary.PropNTree;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphTools;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;

import gnu.trove.TIntArrayList;

import java.util.LinkedList;

public class NTree<V extends Variable> extends Constraint<V, Propagator<V>>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nTree;
	public static int filteringCounter=0;
	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Create a constraint for tree partitioning graph
	 * @param graph the graph variable (directed)
	 * @param nTree the expected number of trees (IntVar)
	 * @param solver 
	 * @param storeThreshold
	 */
	public NTree(DirectedGraphVar graph, IntVar nTree, Solver solver, PropagatorPriority storeThreshold) {
		super((V[]) new Variable[]{graph,nTree}, solver, storeThreshold);
		setPropagators(
				new PropNSuccs(graph, solver.getEnvironment(), this, storeThreshold, true, 1),
				new PropNLoops(graph, nTree, solver.getEnvironment(), this, storeThreshold, true),
				new PropNTree(graph, nTree,solver.getEnvironment(),this, storeThreshold,true));
		this.g = graph;
		this.nTree = nTree;
	}


	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public ESat isSatisfied() {
		DirectedGraphVar g = (DirectedGraphVar) vars[0];
		int n = g.getEnvelopGraph().getNbNodes();
		IntVar nTree = (IntVar) vars[1];
		int MINTREE = calcMinTree();
		int MAXTREE = calcMaxTree();
		if (nTree.getLB()<=MAXTREE && nTree.getUB()>=MINTREE){
			ActiveNodesIterator<IActiveNodes> nodeIter = g.getEnvelopGraph().getActiveNodes().iterator();
			int node;
			DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());
			while (nodeIter.hasNext()){
				node = nodeIter.next();
				if (g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>1){
					return ESat.FALSE;
				}
				AbstractNeighborsIterator<INeighbors> sucIter = g.getEnvelopGraph().successorsIteratorOf(node);
				int suc;
				while (sucIter.hasNext()){
					suc = sucIter.next();
					Grs.addArc(suc, node);
					if(suc==node){
						Grs.addArc(node, n);
						Grs.addArc(n, node);
					}
				}
			}
			int[] numDFS = GraphTools.performDFS(n, Grs);
			boolean rootFound = false;
			for(int i:numDFS){
				if(rootFound && i==0)return ESat.FALSE;
				if(i==0)rootFound = true;
			}
		}else{
			return ESat.FALSE;
		}
		if(g.instantiated()){
			return ESat.TRUE;
		}else{
			return ESat.UNDEFINED;
		}
	}
	
	private int calcMaxTree() {
		int ct = 0;
		ActiveNodesIterator<IActiveNodes> nodeIter = g.getEnvelopGraph().activeNodesIterator();
		int node;
		while(nodeIter.hasNext()){
			node = nodeIter.next();
			if (g.getEnvelopGraph().arcExists(node, node)){
				ct++;
			}
		}
		return ct;
	}
	
	private int calcMinTree() {
		int n = g.getEnvelopGraph().getNbNodes();
		LinkedList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(g.getEnvelopGraph());
		int[] sccOf = new int[n];
		int sccNum = 0;
		int node;
		for (TIntArrayList scc:allSCC){
			for(int x=0;x<scc.size();x++){
				sccOf[scc.get(x)] = sccNum;
			}
			sccNum++;
		}
		LinkedList<TIntArrayList> sinks = new LinkedList<TIntArrayList>();
		boolean looksSink = true;
		int suc;
		AbstractNeighborsIterator<INeighbors> succIter;
		for (TIntArrayList scc:allSCC){
			looksSink = true;
			for(int x=0;x<scc.size();x++){
				node = scc.get(x);
				succIter = g.getEnvelopGraph().successorsIteratorOf(node);
				while(looksSink && succIter.hasNext()){
					suc = succIter.next();
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
			}
		}
		return sinks.size();
	}
}
