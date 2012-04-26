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
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.gary.constraintSpecific.PropNLoopsTree;
import solver.constraints.propagators.gary.constraintSpecific.PropNTree;
import solver.constraints.propagators.gary.degree.PropAtLeastNSuccessors;
import solver.constraints.propagators.gary.degree.PropAtMostNSuccessors;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphTools;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import java.util.ArrayList;
import java.util.LinkedList;

/**Constraint for tree partitioning an anti-arborscence
 * In the modelization a root is a loop
 * GAC ensured in O(alpha.m) worst case time (cf. paper Revisiting the tree constraint)
 * where alpha is the inverse of ackermann function
 * 
 * BEWARE the case where some nodes do not belong to the solution has not been tested
 * 
 * @author Jean-Guillaume Fages
 *
 * @param <V> 
 */
public class NTree<V extends Variable> extends Constraint<V, Propagator<V>>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nTree;
	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Create a constraint for tree partitioning graph
	 * @param graph the graph variable (directed)
	 * @param nTree the expected number of trees (IntVar)
	 * @param solver 
	 */
	public NTree(DirectedGraphVar graph, IntVar nTree, Solver solver) {
		super((V[]) new Variable[]{graph,nTree}, solver);
//		setPropagators(
////			new PropNSuccs(graph, solver, this, 1),
//				new PropNLoopsTree(graph, nTree, solver, this),
//				new PropNTree(graph, nTree,solver,this));
		setPropagators(
				(Propagator) new PropAtLeastNSuccessors(graph, 1, this, solver),
				(Propagator) new PropAtMostNSuccessors(graph, 1, this, solver),
				new PropNLoopsTree(graph, nTree, solver, this),
				new PropNTree(graph, nTree,solver,this));
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
		INeighbors nei;
		if (nTree.getLB()<=MAXTREE && nTree.getUB()>=MINTREE){
			IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
			DirectedGraph Grs = new DirectedGraph(n+1, g.getEnvelopGraph().getType());
			for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
				if (g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>1){
					return ESat.FALSE;
				}
				nei = g.getEnvelopGraph().getSuccessorsOf(node);
				for(int suc=nei.getFirstElement(); suc>=0; suc=nei.getNextElement()){
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
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if (g.getEnvelopGraph().arcExists(node, node)){
				ct++;
			}
		}
		return ct;
	}
	
	private int calcMinTree() {
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
		LinkedList<TIntArrayList> sinks = new LinkedList<TIntArrayList>();
		boolean looksSink = true;
		INeighbors nei;
		for (TIntArrayList scc:allSCC){
			looksSink = true;
			for(int x=0;x<scc.size();x++){
				node = scc.get(x);
				nei = g.getEnvelopGraph().getSuccessorsOf(node);
				for(int suc=nei.getFirstElement(); suc>=0 && looksSink; suc=nei.getNextElement()){
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

    @Override
    public HeuristicVal getIterator(String name, V var) {
        throw new UnsupportedOperationException("NTree does not provide such a service");
    }
}
