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

package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphType;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.setDataStructures.linkedlist.Set_Std_2LinkedList;

import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 * Maintain incrementally the reduced graph G_R and SCC
 *  make G_R a hamiltonian path
 *  BEWARE REQUIRES A UNIQUE SOURCE AND A UNIQUE SINK
 *  O(n+m)
 *  User: Jean-Guillaume Fages
 *  Date: 17/11/2011
 * */
public class PropReducedGraphHamPath extends Propagator<DirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n; 					// number of nodes in G
	private DirectedGraphVar G;					// the graph variable
    GraphDeltaMonitor gdm;
	private IStateInt[] sccOf;		// SCC of each node
	private IStateInt[] sccFirst,sccNext; // nodes of each scc
	private ISet[] mates;		// arcs of G that fit with outgoing arcs of a node in G_R
	private IDirectedGraph G_R; 	// reduced graph
	private IStateInt n_R; 			// number of nodes G_R
	private PairProcedure arcRemoved;// incremental procedure
	private BitSet sccComputed;		// enable incrementation
	private StrongConnectivityFinder SCCfinder;

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
	public PropReducedGraphHamPath(DirectedGraphVar graph, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		G = graph;
        gdm = (GraphDeltaMonitor) G.monitorDelta(this);
		n = G.getEnvelopGraph().getNbNodes();
		n_R = environment.makeInt(0);
		G_R = new DirectedGraph(environment, n, GraphType.DOUBLE_LINKED_LIST,false);
		sccOf = new IStateInt[n];
		sccFirst = new IStateInt[n];
		sccNext  = new IStateInt[n];
		mates = new ISet[n];
		for (int i = 0; i < n; i++) {
			sccOf[i] = environment.makeInt(0);
			sccFirst[i] = environment.makeInt(-1);
			sccNext[i]  = environment.makeInt(-1);
			G_R.getActiveNodes().remove(i);
			mates[i] = new Set_Std_2LinkedList(environment);
		}
		arcRemoved = new RemArc(this);
		sccComputed = new BitSet(n);
		SCCfinder = new StrongConnectivityFinder(G.getEnvelopGraph());
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask+EventType.ENFORCEARC.mask;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			sccFirst[i].set(-1);
			sccNext[i].set(-1);
			mates[i].clear();
			G_R.getActiveNodes().remove(i);
		}
		SCCfinder.findAllSCC();
		int s = SCCfinder.getNbSCC();
		n_R.set(s);
		int j;
		for(int i=0;i<s;i++){
			G_R.getActiveNodes().add(i);
			j = SCCfinder.getSCCFirstNode(i);
			while(j!=-1){
				sccOf[j].set(i);
				addNode(i,j);
				j = SCCfinder.getNextNode(j);
			}
		}
		ISet succs;
		int x;
		for(int i=0;i<n;i++){
			x = sccOf[i].get();
			succs = G.getEnvelopGraph().getSuccessorsOf(i);
			for(j=succs.getFirstElement(); j>=0; j=succs.getNextElement()){
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
		if(first==-1 || last==-1 || first==last){
			contradiction(G, "");
		}
		if(visit(first, last)!=n_R.get()){
			contradiction(G, "");
		}
		int to,arc;
		for(int i=0;i<n;i++){
			to = G.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			x  = sccOf[i].get();
			if(to!=-1 && sccOf[to].get()!=x && mates[x].getSize()>1){
				arc = (i+1)*n+to;
				for(int a=mates[x].getFirstElement();a>=0;a=mates[x].getNextElement()){
					if(a!=arc){
						G.removeArc(a/n-1,a%n,this);
					}
				}
				mates[x].clear();
				mates[x].add(arc);
			}
		}
		gdm.unfreeze();
	}

	private void addNode(int scc, int node) {
		sccNext[node].set(sccFirst[scc].get());
		sccFirst[scc].set(node);
	}

	private int visit(int node, int last) throws ContradictionException {
		if(node==-1){
			contradiction(G,"G_R disconnected");
		}
		if(node==last){
			return 1;
		}
		int next = -1;
		ISet succs = G_R.getSuccessorsOf(node);
		for(int x=succs.getFirstElement(); x>=0; x=succs.getNextElement()){
			if(G_R.getPredecessorsOf(x).getSize()==1){
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
		if((mask & EventType.REMOVEARC.mask)!=0){
			gdm.freeze();
			gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
            gdm.unfreeze();
		}
		int to,x;
		for(int i=0;i<n;i++){
			to = G.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			x  = sccOf[i].get();
			if(to!=-1 && sccOf[to].get()!=x && mates[x].getSize()>1){
				int arc = (i+1)*n+to;
				for(int a=mates[x].getFirstElement();a>=0;a=mates[x].getNextElement()){
					if(a!=arc){
						G.removeArc(a/n-1,a%n,this);
					}
				}
				mates[x].clear();
				mates[x].add(arc);
			}
		}
	}

	@Override
	public ESat isEntailed() {
		if(G.instantiated()){
			int nr = 0;
			for(int i=0;i<n_R.get();i++){
				nr+=G_R.getSuccessorsOf(i).getSize();
			}
			if(nr==n_R.get()-1){
				return ESat.TRUE;
			}
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	public IStateInt getNSCC(){
		return n_R;
	}

	public ISet[] getOutArcs(){
		return mates;
	}

	public IStateInt[] getSCCOF() {
		return sccOf;
	}

	public IStateInt[] getSCCFirst() {
		return sccFirst;
	}

	public IStateInt[] getSCCNext() {
		return sccNext;
	}

	public IDirectedGraph getReducedGraph() {
		return G_R;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemArc implements PairProcedure{
		private Propagator p;
		private BitSet restriction;

		private RemArc(Propagator p){
			this.p = p;
			this.restriction = new BitSet(n);
		}
		@Override
		public void execute(int from, int to) throws ContradictionException {
			int x = sccOf[from].get();
			if (x==sccOf[to].get()){
				if(!sccComputed.get(x)){
					restriction.clear();
					int k = sccFirst[x].get();
					while(k!=-1){
						restriction.set(k);
						k = sccNext[k].get();
					}
					SCCfinder.findAllSCCOf(restriction);
					sccComputed.set(x);
					int ns = SCCfinder.getNbSCC();
					if(ns>1){
						int first = G_R.getPredecessorsOf(x).getFirstElement();
						int last  = G_R.getSuccessorsOf(x).getFirstElement();
						// SCC broken
						sccFirst[x].set(-1);
						mates[x].clear();
						G_R.removeArc(first,x);
						G_R.removeArc(x,last);
						// first scc
						int e = SCCfinder.getSCCFirstNode(0);
						while(e!=-1){
							addNode(x,e);
							e = SCCfinder.getNextNode(e);
						}
						// others
						int idx=n_R.get();
						int elem;
						for(int scc=1;scc<ns;scc++){
							sccFirst[idx].set(-1);
							mates[idx].clear();
							G_R.getActiveNodes().add(idx);
							e = SCCfinder.getSCCFirstNode(scc);
							while(e!=-1){
								addNode(idx,e);
								sccOf[e].set(idx);
								e = SCCfinder.getNextNode(e);
							}
							idx++;
						}
						n_R.set(idx);
						// link arcs
						int sccE;
						ISet nei;
						for(int scc=0;scc<ns;scc++){
							sccE = sccOf[SCCfinder.getSCCFirstNode(scc)].get();
							e = SCCfinder.getSCCFirstNode(scc);
							while(e!=-1){
								nei = G.getEnvelopGraph().getSuccessorsOf(e);
								for(int next=nei.getFirstElement(); next>=0; next=nei.getNextElement()){
									if(sccE!=sccOf[next].get()){
										G_R.addArc(sccE,sccOf[next].get());
										mates[sccE].add((e+1)*n+next);
									}
								}
								e = SCCfinder.getNextNode(e);
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
				mates[x].remove((from+1)*n+to);
				if(mates[x].getSize()==0){
					p.contradiction(G,"G_R disconnected");
				}
			}
		}
	}
}
