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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.*;

import java.util.BitSet;
import java.util.LinkedList;

/**
 * Arborescence constraint (simplification from tree constraint)
 * */
public class PropArborescence<V extends GraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int source;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Ensures that graph is an arborescence rooted in node source
	 * @param graph
	 * @param source root of the arborescence
	 * @param constraint
	 * @param solver
	 * */
	public PropArborescence(DirectedGraphVar graph, int source, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new GraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		this.source = source;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private boolean allReachableFrom(int x) {
		int n = g.getEnvelopGraph().getNbNodes();
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(x);
		INeighbors env;
		BitSet visited = new BitSet(n);
		visited.set(x);
		while(!list.isEmpty()){
			x = list.removeFirst();
			env = g.getEnvelopGraph().getSuccessorsOf(x);
			for(int suc=env.getFirstElement(); suc>=0; suc=env.getNextElement()){
				if(!visited.get(suc)){
					visited.set(suc);
					list.addLast(suc);
				}
			}
		}
		return visited.nextClearBit(0)>=n;
	}

	private void filtering() throws ContradictionException{
		if(allReachableFrom(source)){
			structuralPruning();
		}else{
			this.contradiction(g, "infeasible");
		}
	}

	@Override
	public void propagate() throws ContradictionException {
		filtering();
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		filtering();
	}

	private void structuralPruning() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		//dominators
		FlowGraphManager flowGM = new FlowGraphManager(source, g.getEnvelopGraph(),true);
		DirectedGraph dominatorGraph = new DirectedGraph(n, GraphType.LINKED_LIST);
		for (int node=0; node<n; node++){
			if(node!=source)dominatorGraph.addArc(flowGM.getImmediateDominatorsOf(node), node);
		}
		//PREPROCESSING
		int[] in = new int[n];
		int[] out = new int[n];
		int[] father = new int[n];
		INeighbors[] successors = new INeighbors[n];
		for (int i=0; i<n; i++){
			father[i] = -1;
			successors[i] = dominatorGraph.getSuccessorsOf(i);
		}
		int time = 0;
		int currentNode = source;
		father[currentNode] = currentNode;
		in[currentNode] = 0;
		int nextNode;
		boolean first = true;
		boolean finished = false;
		while(!finished){
			if(first){
				nextNode = successors[currentNode].getFirstElement();
				first = false;
			}else{
				nextNode = successors[currentNode].getNextElement();
			}
			if(nextNode<0){
				time++;
				out[currentNode] = time;
				if(currentNode==source){
					finished = true;
					break;
				}
				first = false;
				currentNode = father[currentNode];
			}else{
				if (father[nextNode]==-1) {
					time++;
					in[nextNode] = time;
					father[nextNode] = currentNode;
					currentNode = nextNode;
					first = true;
				}
			}
		}
		//END_PREPROCESSING
		//queries
		INeighbors nei;
		for (int node=0; node<n; node++){
			nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int suc = nei.getFirstElement(); suc>=0; suc = nei.getNextElement()){
				//--- STANDART PRUNING
				if(node !=suc){
					if (in[node]>in[suc] && out[node]<out[suc]){
						g.removeArc(node, suc, this,false);
					}
					// arc-dominator detection (redundant with 1-pred propagator
//					else if(in[node]<in[suc] && out[node]>out[suc]){
//						INeighbors preds = g.getEnvelopGraph().getPredecessorsOf(suc);
//						boolean arcDominator = true;
//						for(int p=preds.getFirstElement(); p>=0; p=preds.getNextElement()){
//							if(p!=node){
//								arcDominator &= (in[p]>in[suc] && out[p]<out[suc]);
//							}
//						}
//						if (arcDominator){
//							g.enforceArc(node,suc,this);
//						}
//					}
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
