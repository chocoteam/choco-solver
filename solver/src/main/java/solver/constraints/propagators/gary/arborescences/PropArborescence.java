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
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.dominance.AbstractLengauerTarjanDominatorsFinder;
import solver.variables.graph.graphOperations.dominance.AlphaDominatorsFinder;
import solver.variables.graph.graphOperations.dominance.SimpleDominatorsFinder;

/**
 * Arborescence constraint (simplification from tree constraint)
 * based on dominators
 * Uses simple LT algorithm which runs in O(m.log(n)) worst case time
 * but very efficient in practice
 */
public class PropArborescence extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	// flow graph
	DirectedGraphVar g;
	// source that reaches other nodes
	int source;
	// number of nodes
	int n;
	// dominators finder that contains the dominator tree
	AbstractLengauerTarjanDominatorsFinder domFinder;
	ISet[] successors;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

	/**
	 * @PropAnn(tested = {BENCHMARK,CORRECTION})
	 * Ensures that graph is an arborescence rooted in node source
	 * @param graph
	 * @param source root of the arborescence
	 * @param constraint
	 * @param solver
	 * */
	public PropArborescence(DirectedGraphVar graph, int source, Constraint constraint, Solver solver, boolean simple) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.QUADRATIC);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		this.source = source;
		successors = new ISet[n];
		if(simple){
			domFinder = new SimpleDominatorsFinder(source, g.getEnvelopGraph());
		}else{
			domFinder = new AlphaDominatorsFinder(source, g.getEnvelopGraph());
		}
	}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            g.enforceNode(i, aCause);
            g.removeArc(i, i, aCause);
            g.removeArc(i, source, aCause);
        }
        structuralPruning();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        structuralPruning();
    }

	private void structuralPruning() throws ContradictionException {
		if(domFinder.findDominators()){
			ISet nei;
			for (int x=0; x<n; x++){
				nei = g.getEnvelopGraph().getSuccessorsOf(x);
				for(int y = nei.getFirstElement(); y>=0; y = nei.getNextElement()){
					//--- STANDART PRUNING
					if(domFinder.isDomminatedBy(x,y)){
						g.removeArc(x,y,this);
					}
					// ENFORCE ARC-DOMINATORS (redondant)
				}
			}
		}else{
			contradiction(g,"the source cannot reach all nodes");
		}
	}

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            try {
                structuralPruning();
            } catch (Exception e) {
                return ESat.FALSE;
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
