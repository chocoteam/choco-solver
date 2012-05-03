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

package solver.constraints.propagators.gary.trees;

import choco.annotations.PropAnn;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 *
 * Simple NoSubtour of Pesant when undirected graph
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropChainNoSubtour2 extends PropCycleNoSubtour {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Ensures that graph has no circuit, with Caseaux/Laburthe/Pesant algorithm
	 * runs in O(1) per instantiation event
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropChainNoSubtour2(UndirectedGraphVar graph, Constraint<UndirectedGraphVar,
			Propagator<UndirectedGraphVar>> constraint, Solver solver) {
		super(graph,constraint,solver);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int j;
		for(int i=0;i<n;i++){
			end[i].set(i);
			origin[i].set(i);
			size[i].set(1);
		}
		enforce(0,n-1);
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getKernelGraph().getSuccessorsOf(i);
			j = nei.getFirstElement();
			if(j!=-1 && i<j){
				enforce(i,j);
			}
			j = nei.getNextElement();
			if(j!=-1 && i<j){
				enforce(i,j);
			}
			if(nei.getNextElement()!=-1){
				contradiction(g,"");
			}
		}
	}
}
