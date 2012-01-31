
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
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.tsp;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
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
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.BitSet;

/**
 * Each node but "but" has only one successor
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropSeparator<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	TIntArrayList mis;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** All nodes of the graph but "but" have only one successor
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropSeparator(DirectedGraphVar graph, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		mis = new TIntArrayList();
//		throw new UnsupportedOperationException("ca marche pas trop");
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		findMIS();
		minimizeSep();
		crashAndPerform();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		int s = 0;
		for(int i=0;i<n;i++){
			s+=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
		}
		if(s>3*n)return;
//		System.out.println("a");
		propagate(0);
//		System.out.println("b");
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private void findMIS() {
		UndirectedGraph dg = new UndirectedGraph(n, GraphType.MATRIX);
		INeighbors suc;
		for(int i=0;i<n;i++){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=0;j>=0;j=suc.getNextElement()){
				dg.addEdge(i, j);
			}
		}
		mis.clear();
		IActiveNodes nodes = dg.getActiveNodes();
		int next;
		while(!nodes.isEmpty()){
			next = nodes.getFirstElement();
			for(int i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
				if(dg.getNeighborsOf(i).neighborhoodSize()<dg.getNeighborsOf(next).neighborhoodSize()){
					next = i;
				}
			}
			if(next == -1){
				throw new UnsupportedOperationException();
			}
			mis.add(next);
			suc = dg.getNeighborsOf(next);
			for(int i=suc.getFirstElement();i>=0;i=suc.getNextElement()){
				dg.desactivateNode(i);
			}
			dg.desactivateNode(next);
		}
	}
	private void minimizeSep() throws ContradictionException {
		BitSet S = new BitSet(n);
		BitSet Sman = new BitSet(n);
		S.set(0, n);
		for(int i=0;i<mis.size();i++){
			S.clear(mis.get(i));
		}
		boolean again = true;
		INeighbors suc;
		do{
			again = true;
			while(again){
				while(again){
					again = false;
					for(int i=S.nextSetBit(0);i>=0;i=S.nextSetBit(i+1)){
						if(!Sman.get(i)){
							int nb = 0;
							suc = g.getEnvelopGraph().getNeighborsOf(i);
							for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
								if(!S.get(j)){
									nb++;
									if(nb>=2){
										Sman.set(i);
										again = true;
										break;
									}
								}
							}
							if(nb==1){
								S.clear(i);
								again = true;
							}
						}
					}
				}
				for(int i=S.nextSetBit(0);i>=0;i=S.nextSetBit(i+1)){
					if(!Sman.get(i)){
						S.clear(i);
						again = true;
						break;
					}
				}
			}
			int nbInS = S.cardinality();
			int nbSman= Sman.cardinality();
			if(nbInS!=nbSman){
				if(S.get(0)){
					Sman.set(0);
				}
				if(S.get(n-1)){
					Sman.set(n-1);
				}
				nbSman = Sman.cardinality();
				if(nbInS!=nbSman){
					System.out.println(S);
					System.out.println(Sman);
					throw new UnsupportedOperationException();
				}
			}
			UndirectedGraph dg = new UndirectedGraph(n, GraphType.MATRIX);
			for(int i=0;i<n;i++){
				if(!S.get(i)){
					suc = g.getEnvelopGraph().getSuccessorsOf(i);
					for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
						if(!S.get(j)){
							dg.addEdge(i, j);
						}
					}
				}
			}
			for(int j=S.nextSetBit(0);j>=0;j=S.nextSetBit(j+1)){
				dg.desactivateNode(j);
			}
			int nbCC  = ConnectivityFinder.findCCOf(dg).size();
			if(nbCC>nbInS+1){
				System.out.println("\nFAILFILAAILAIIALIFLI\n");
				contradiction(g,"");
//				throw new UnsupportedOperationException();
			}
			if(nbCC==nbInS+1){
				for(int i=S.nextSetBit(0);i>=0;i=S.nextSetBit(i+1)){
					suc = g.getEnvelopGraph().getSuccessorsOf(i);
					for(int j=suc.getFirstElement();j>=0;j=suc.getNextElement()){
						if(S.get(j)){
							System.out.println("\nREOMREOMROERMEMROVRRRR\n");
							g.removeArc(i,j,this);
						}
					}
				}
			}
			S.clear(S.nextSetBit(0));
			Sman.clear();
		}while(S.cardinality()>0);
	}
	private void crashAndPerform() {

	}
}
