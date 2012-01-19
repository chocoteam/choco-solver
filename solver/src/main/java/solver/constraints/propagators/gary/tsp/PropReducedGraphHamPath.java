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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 16/11/11
 * Time: 10:42
 */

package solver.constraints.propagators.gary.tsp;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredDoubleIntLinkedList;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredIntLinkedList;
import java.util.ArrayList;
import java.util.BitSet;

/** Maintain incrementally the reduced graph G_R and SCC
 *  make G_R a hamiltonian path
 *  BEWARE REQUIRES A UNIQUE SOURCE AND A UNIQUE SINK
 *  O(n+m)
 *  User: Jean-Guillaume Fages
 *  Date: 17/11/2011
 * */
public class PropReducedGraphHamPath<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n; 					// number of nodes in G
	private V G;					// the graph variable
	private IStateInt[] sccOf;		// SCC of each node
	private INeighbors[] nodesOf;	// nodes of each SCC (CAN BE REMOVED)
	private INeighbors[] mates;		// arcs of G that fit with outgoing arcs of a node in G_R
	private IDirectedGraph G_R; 	// reduced graph
	private IStateInt n_R; 			// number of nodes G_R
	private RemArc arcRemoved;		// incremental procedure
	private BitSet sccComputed;		// enable incrementation


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Maintain incrementally the reduced graph and strongly connected components of a directed graph variable
	 * Ensures that the reduced graph is a Hamiltonian path
	 * BEWARE REQUIRES A UNIQUE SOURCE AND A UNIQUE SINK
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropReducedGraphHamPath(V graph, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		G = graph;
		n = G.getEnvelopGraph().getNbNodes();
		n_R = environment.makeInt(0);
		G_R = new StoredDirectedGraph(environment, n, GraphType.DOUBLE_LINKED_LIST);
		sccOf = new IStateInt[n];
		nodesOf = new INeighbors[n];
		mates = new INeighbors[n];
		for (int i = 0; i < n; i++) {
			sccOf[i] = environment.makeInt(0);
			nodesOf[i] = new StoredIntLinkedList(environment);
			G_R.getActiveNodes().desactivate(i);
			mates[i] = new StoredDoubleIntLinkedList(environment);
		}
		arcRemoved = new RemArc(this);
		sccComputed = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(G.getEnvelopGraph());
		int s = allSCC.size();
		n_R.set(s);
		int elem;
		TIntArrayList list;
		for(int i=0;i<s;i++){
			list = allSCC.get(i);
			nodesOf[i].clear();
			mates[i].clear();
			G_R.getActiveNodes().desactivate(i);
			G_R.getActiveNodes().activate(i);
			for(int j=list.size()-1;j>=0;j--){
				elem = list.get(j);
				sccOf[elem].set(i);
				nodesOf[i].add(elem);
			}
		}
		INeighbors succs;
		int x;
		for(int i=0;i<n;i++){
			x = sccOf[i].get();
			succs = G.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=succs.getFirstElement(); j>=0; j=succs.getNextElement()){
				if(x!=sccOf[j].get()){
					G_R.addArc(x,sccOf[j].get());
					mates[x].add((i+1)*n+j);
				}
			}
		}
		int first = -1;
		int last  = -1;
		for(int i=0;i<s;i++){
			if(G_R.getPredecessorsOf(i).isEmpty()){
				first = i;
			}
			if(G_R.getSuccessorsOf(i).isEmpty()){
				last = i;
			}
		}
		if(visit(first, last)!=n_R.get()){
			contradiction(G, "");
		}
	}

	private int visit(int node, int last) throws ContradictionException {
		if(node==-1){
			contradiction(G,"G_R disconnected");
		}
		if(node==last){
			return 1;
		}
		int next = -1;
		INeighbors succs = G_R.getSuccessorsOf(node);
		for(int x=succs.getFirstElement(); x>=0; x=succs.getNextElement()){
			if(G_R.getPredecessorsOf(x).neighborhoodSize()==1){
				if(next!=-1){
					return 0;
				}
				next = x;
			}else{
				G_R.removeArc(node,x);
			}
		}
		succs = mates[node];
		int from,to;
		for(int e=succs.getFirstElement(); e>=0;e=succs.getNextElement()){
			to = e%n;
			if(sccOf[to].get()!=next){
				from = e/n-1;
				G.removeArc(from,to,this);
				mates[node].remove(e);
			}
		}
		return visit(next,last)+1;
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		sccComputed.clear();
        eventRecorder.getDeltaMonitor(G).forEach(arcRemoved, EventType.REMOVEARC);
	}

	@Override
	public ESat isEntailed() {
		if(G.instantiated()){
			int nr = 0;
			for(int i=0;i<n_R.get();i++){
				nr+=G_R.getSuccessorsOf(i).neighborhoodSize();
			}
			if(nr==n_R.get()-1){
				return ESat.TRUE;
			}
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemArc implements IntProcedure{
		private GraphPropagator p;
		private BitSet restriction;

		private RemArc(GraphPropagator p){
			this.p = p;
			this.restriction = new BitSet(n);
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to = i%n;
			int x = sccOf[from].get();
			if (x==sccOf[to].get()){
				if(!sccComputed.get(x)){
					restriction.clear();
					for(int k=nodesOf[x].getFirstElement(); k>=0; k=nodesOf[x].getNextElement()){
						restriction.set(k);
					}
					// piste1 algo faux
					ArrayList<TIntArrayList> newSCC = StrongConnectivityFinder.findAllSCCOf(G.getEnvelopGraph(), restriction);
					sccComputed.set(x);
					int ns = newSCC.size();
					if(ns>1){
						int first = G_R.getPredecessorsOf(x).getFirstElement();
						int last  = G_R.getSuccessorsOf(x).getFirstElement();
						// SCC broken
						nodesOf[x].clear();
						mates[x].clear();
						G_R.removeArc(first,x);
						G_R.removeArc(x,last);
						// first scc
						for(int e=0; e<newSCC.get(0).size();e++){
							nodesOf[x].add(newSCC.get(0).get(e));
						}
						// others
						int idx=n_R.get();
						int elem;
						for(int scc=1;scc<ns;scc++){
							nodesOf[idx].clear();
							mates[idx].clear();
							G_R.getActiveNodes().activate(idx);
							for(int e=0; e<newSCC.get(scc).size();e++){
								elem = newSCC.get(scc).get(e);
								nodesOf[idx].add(elem);
								sccOf[elem].set(idx);
							}
							idx++;
						}
						n_R.set(idx);
						// link arcs
						int e,sccE;
						INeighbors nei;
						for(int scc=0;scc<ns;scc++){
							for(int k=newSCC.get(scc).size()-1;k>=0;k--){
								e = newSCC.get(scc).get(k);
								sccE = sccOf[e].get();
								nei = G.getEnvelopGraph().getSuccessorsOf(e);
								for(int next=nei.getFirstElement(); next>=0; next=nei.getNextElement()){
									if(sccE!=sccOf[next].get()){
										G_R.addArc(sccE,sccOf[next].get());
										mates[sccE].add((e+1)*n+next);
									}
								}
							}
						}
						for(int arc=mates[first].getFirstElement();arc>=0;arc=mates[first].getNextElement()){
							G_R.addArc(first,sccOf[arc%n].get());
						}
						// filter
						if(visit(first,last)!=ns+2){
							p.contradiction(G,"no Hamiltonian path");
						}
					}
				}
			}else{
				int y = sccOf[to].get();
				mates[x].remove(i);
				if(mates[x].neighborhoodSize()==0){
					p.contradiction(G,"G_R disconnected");
				}
			}
		}
	}
}