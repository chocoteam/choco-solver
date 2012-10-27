/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary.arborescences;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.setDataStructures.SetType;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.util.BitSet;
import java.util.LinkedList;

/**
 * Arborescence constraint (simplification from tree constraint)
 * Use naive implementation in O(n.m) for testing
 * */
public class PropArborescence_NaiveForm extends Propagator<DirectedGraphVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int source;
	int n;
	LinkedList<Integer> list;
	BitSet visited;
	DirectedGraph domTrans;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**Ensures that graph is an arborescence rooted in node source
	 * naive form: O(n.m)
	 * @param graph
	 * @param source root of the arborescence
	 * @param constraint
	 * @param solver
	 * */
	public PropArborescence_NaiveForm(DirectedGraphVar graph, int source, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.QUADRATIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		this.source = source;
		list = new LinkedList<Integer>();
		visited = new BitSet(n);
		domTrans= new DirectedGraph(n, SetType.BOOL_ARRAY,false);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private boolean allReachableFrom(int x,DirectedGraph g) {
		list.clear();
		visited.clear();
		list.add(x);
		ISet env;
		visited.set(x);
		while(!list.isEmpty()){
			x = list.removeFirst();
			env = g.getSuccessorsOf(x);
			for(int suc=env.getFirstElement(); suc>=0; suc=env.getNextElement()){
				if(!visited.get(suc)){
					visited.set(suc);
					list.addLast(suc);
				}
			}
		}
		return visited.nextSetBit(0)>=0;
	}

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            g.enforceNode(i, aCause);
            g.removeArc(i, i, aCause);
        }
        structuralPruning();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        structuralPruning();
    }

	private void structuralPruning() throws ContradictionException {
		ISet succ;
		for(int i=0;i<n;i++){
			domTrans.getSuccessorsOf(i).clear();
			domTrans.getPredecessorsOf(i).clear();
		}
		for(int i=0;i<n;i++){
			DirectedGraph dig = new DirectedGraph(n, SetType.LINKED_LIST,false);
			for(int j=0; j<n; j++){
				if(j!=i){
					succ = g.getEnvelopGraph().getSuccessorsOf(j);
					for(int k=succ.getFirstElement();k>=0;k=succ.getNextElement()){
						dig.addArc(j,k);
					}
				}
			}
			allReachableFrom(source,dig);
			for(int z=visited.nextClearBit(0);z<n;z=visited.nextClearBit(z+1)){
				domTrans.addArc(i,z);
			}
		}
		for(int i=0;i<n;i++){
			succ = domTrans.getSuccessorsOf(i);
			for(int k=succ.getFirstElement();k>=0;k=succ.getNextElement()){
				g.removeArc(k,i,this);
			}
		}
	}

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask;
    }

	@Override
	public ESat isEntailed() {
		if(isCompletelyInstantiated()){
			try{
				structuralPruning();
			}catch (Exception e){
				return ESat.FALSE;
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}