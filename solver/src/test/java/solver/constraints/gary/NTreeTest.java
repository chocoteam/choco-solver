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

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class NTreeTest {

	@Test
	public static void model(int n, int tmin, int tmax) {
		Solver s = new Solver();
		DirectedGraphVar g = new DirectedGraphVar(s, n, GraphType.MATRIX, GraphType.MATRIX);
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				g.getEnvelopGraph().addArc(i, j);
			}
		}
		IntVar nTree = VariableFactory.enumerated("NTREE ", tmin,tmax, s);
		Constraint[] cstrs = new Constraint[]{new NTree(g,nTree, s, PropagatorPriority.LINEAR)};
		AbstractStrategy strategy = StrategyFactory.graphLexico(g);
		
		s.post(cstrs);
		s.set(strategy);
		s.findAllSolutions();
		
		if(s.getMeasures().getBackTrackCount()>0){
			throw new UnsupportedOperationException("error (no GAC)");
		}
	}
	
	@Test
	public static void debug() {
//		for(int s=0;s<3;s++){
//			for(int n=100;n<500;n*=2){
//				for(int t1=1;t1<n;t1*=2){
//					for(int t2=t1;t2<n;t2*=2){
//						model(n,t1,t2);
//					}
//				}
//			}
//		}
		model(10,11,12);
	}
}
