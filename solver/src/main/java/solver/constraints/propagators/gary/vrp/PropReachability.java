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

package solver.constraints.propagators.gary.vrp;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.util.BitSet;

public class PropReachability extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	int nbTrucks;
	int[] stack;
	BitSet inStack;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropReachability(DirectedGraphVar graph, int nbTrucks,
							Constraint constraint, Solver solver) {
		super(new Variable[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.nbTrucks = nbTrucks;
		this.n = g.getEnvelopGraph().getNbNodes();
		stack = new int[n];
		inStack = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int node;
		int size = 0;
		int idx  = 0;
		inStack.clear();
		for(int i=0;i<2*nbTrucks;i+=2){
			stack[size++] = i;
			inStack.set(i);
		}
		INeighbors nei;
		while(idx!=size){
			node = stack[idx++];
			nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!inStack.get(j)){
					stack[size++] = j;
					inStack.set(j);
				}
			}
		}
		if(inStack.cardinality()!=n){
			contradiction(g,"");
		}
		size = 0;
		idx  = 0;
		inStack.clear();
		for(int i=1;i<2*nbTrucks;i+=2){
			stack[size++] = i;
			inStack.set(i);
		}
		while(idx!=size){
			node = stack[idx++];
			nei = g.getEnvelopGraph().getPredecessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!inStack.get(j)){
					stack[size++] = j;
					inStack.set(j);
				}
			}
		}
		if(inStack.cardinality()!=n){
			contradiction(g,"");
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}


//	LinkedList<TIntArrayList> ccs = ConnectivityFinder.findCCOf(graph.getEnvelopGraph());
//				if(ccs.size()>nbTrucks){
//					contradiction(graph,"");
//				}
//				int node;
//				for(TIntArrayList cc:ccs){
//					boolean start = false;
//					boolean end = false;
//					for(int i=cc.size()-1;i>=0;i--){
//						node = cc.get(i);
//						if(node<nbTrucks*2){
//							if(node%2==0){
//								start = true;
//							}else{
//								end   = true;
//							}
//						}
//					}
//					if(!(start && end)){
//						contradiction(graph, "");
//					}
//				}
}
