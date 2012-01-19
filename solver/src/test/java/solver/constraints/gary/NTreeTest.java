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
import solver.Cause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import static org.testng.Assert.assertTrue;

public class NTreeTest {

	private static GraphType graphTypeEnv = GraphType.MATRIX;
	private static GraphType graphTypeKer = GraphType.MATRIX;

	public static void model(int n, int tmin, int tmax,int seed) {
		Solver s = new Solver();
		DirectedGraphVar g = new DirectedGraphVar(s, n, graphTypeEnv, graphTypeKer);
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				g.getEnvelopGraph().addArc(i, j);
			}
			try {
				g.enforceNode(i, Cause.Null);
			} catch (ContradictionException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		IntVar nTree = VariableFactory.bounded("NTREE ", tmin, tmax, s);
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(g,s);
		gc.addProperty(GraphProperty.K_ANTI_ARBORESCENCES,nTree);
		AbstractStrategy strategy = StrategyFactory.graphRandom(g,seed);

		s.post(gc);
		s.set(strategy);
		s.findAllSolutions();

		assertTrue(s.getMeasures().getFailCount()==0);
		assertTrue(s.getMeasures().getSolutionCount()>0);
	}

	@Test(groups = "10m")
	public static void debug() {
		for(int seed = 0;seed<5;seed++){
			for(int n=5;n<7;n++){
				for(int t1=1;t1<n;t1++){
					for(int t2=t1;t2<n;t2++){
						System.out.println("tree : n="+n+" nbTrees = ["+t1+","+t2+"]");
						model(n,t1,t2,seed);
					}
				}
			}
		}
	}

	@Test(groups = "30m")
	public static void testAllDataStructure(){
		for(GraphType ge:GraphType.ENVELOPE_TYPES){
			graphTypeEnv = ge;
			for(GraphType gk:GraphType.KERNEL_TYPES){
				graphTypeKer = gk;
				System.out.println("env:"+ge+" ker :"+gk);
				debug();
			}
		}
	}
}
