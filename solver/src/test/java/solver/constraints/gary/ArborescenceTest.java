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
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence_NaiveForm;
import solver.constraints.propagators.gary.tsp.directed.PropOnePredBut;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import static org.testng.Assert.assertEquals;

public class ArborescenceTest {

	private static GraphType graphTypeEnv = GraphType.MATRIX;
	private static GraphType graphTypeKer = GraphType.MATRIX;

	public static Solver model(int n, int seed, boolean naive, boolean simple, long nbMaxSols) {
		Solver s = new Solver();
		DirectedGraphVar g = new DirectedGraphVar(s, n, graphTypeEnv, graphTypeKer);
		for(int i=0;i<n;i++){
			for(int j=1;j<n;j++){
				g.getEnvelopGraph().addArc(i, j);
			}
		}
		GraphConstraint gc = GraphConstraintFactory.makeConstraint(g,s);
		gc.addAdHocProp(new PropOnePredBut(g,0,gc,s));
		if(naive){
			gc.addAdHocProp(new PropArborescence_NaiveForm(g,0,gc,s));
		}else{
			gc.addAdHocProp(new PropArborescence(g,0,gc,s,simple));
		}
		AbstractStrategy strategy = StrategyFactory.graphRandom(g,seed);
		s.post(gc);
		s.set(strategy);
		if(nbMaxSols>0){
			s.getSearchLoop().getLimitsBox().setSolutionLimit(nbMaxSols);
		}
		s.findAllSolutions();
		return s;
	}

	@Test(groups = "10s")
	public static void smallTrees() {
		for(int s=0;s<3;s++){
			for(int n=3;n<8;n++){
				System.out.println("Test n="+n+", with seed="+s);
				Solver naive = model(n,s,true,false,-1);
				Solver efficientA = model(n,s,false,true,-1);
				Solver efficientN = model(n,s,false,false,-1);
				System.out.println(naive.getMeasures().getSolutionCount()+" sols");
				assertEquals(naive.getMeasures().getFailCount(),0);
				assertEquals(naive.getMeasures().getSolutionCount(),efficientA.getMeasures().getSolutionCount());
				assertEquals(naive.getMeasures().getFailCount(),efficientA.getMeasures().getFailCount());
				assertEquals(naive.getMeasures().getSolutionCount(),efficientN.getMeasures().getSolutionCount());
				assertEquals(naive.getMeasures().getFailCount(),efficientN.getMeasures().getFailCount());
			}
		}
	}

	@Test(groups = "10S")
	public static void bigTrees() {
		for(int s=0;s<3;s++){
			int n = 60;
			System.out.println("Test n="+n+", with seed="+s);
			Solver naive = model(n,s,true,false,10);
			Solver efficientA = model(n,s,false,true,10);
			Solver efficientN = model(n,s,false,false,10);
			System.out.println(naive.getMeasures().getSolutionCount()+" sols");
			assertEquals(naive.getMeasures().getFailCount(),0);
			assertEquals(naive.getMeasures().getSolutionCount(), efficientA.getMeasures().getSolutionCount());
			assertEquals(naive.getMeasures().getFailCount(),efficientA.getMeasures().getFailCount());
			assertEquals(naive.getMeasures().getSolutionCount(),efficientN.getMeasures().getSolutionCount());
			assertEquals(naive.getMeasures().getFailCount(),efficientN.getMeasures().getFailCount());
		}
	}

	@Test(groups = "30s")
	public static void testAllDataStructure(){
		for(GraphType ge:GraphType.ENVELOPE_TYPES){
			graphTypeEnv = ge;
			for(GraphType gk:GraphType.KERNEL_TYPES){
				graphTypeKer = gk;
				System.out.println("env:"+ge+" ker :"+gk);
				smallTrees();
			}
		}
	}
}
