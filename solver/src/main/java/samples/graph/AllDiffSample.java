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
package samples.graph;

import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.AllDiff;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.PropAllDiff;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.BitSet;

public class AllDiffSample extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int n;
	int sizeFirstSet;
	UndirectedGraphVar g;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public AllDiffSample(){
		super();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		sizeFirstSet = 7;
		n = sizeFirstSet*2+2;
		solver = new Solver();
		BitSet[] data = new BitSet[n];
		for(int i=0;i<n;i++){
			data[i] = new BitSet(n);
		}
		for(int i=0;i<sizeFirstSet;i++){
			for(int j=sizeFirstSet;j<n;j++){
				data[i].set(j);
				data[j].set(i);
			}
		}
		g = VariableFactory.undirectedGraph("", data, GraphType.SPARSE, GraphType.SPARSE, solver);
		Constraint[] cstrs = new Constraint[]{new AllDiff(g,sizeFirstSet, solver, PropagatorPriority.LINEAR)};
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.randomArcs(g);
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.findAllSolutions();
	}

	@Override
	public void prettyOut() {
		System.out.println("allDiff stuff : "+PropAllDiff.duration+" ms");
	}

	public static void main(String[] args) {
		int time = 0;
		for(int i = 0; i<10; i++){
			AllDiffSample ads = new AllDiffSample();
			ads.execute();
//			Logger log = LoggerFactory.getLogger("bench");
//			log.info(Constant.WELCOME_TITLE);
//			log.info(Constant.WELCOME_VERSION);
//			log.info("* Sample library: executing {}.java ... \n", "truc");
//			Solver solver = new Solver();
//			int n = 7;
//			IntVar[] vars = VariableFactory.enumeratedArray("", n, 0,n+1, solver);
//			Constraint[] cstrs = new Constraint[]{new AllDifferent(vars, solver, Type.AC)};
//			solver.post(cstrs);
//			solver.set(StrategyFactory.random(vars, solver.getEnvironment()));
////			solver.set(StrategyFactory.inputOrderIncDomain(vars, solver.getEnvironment()));
//			solver.findAllSolutions();
//			log.info("[STATISTICS {}]", solver.getMeasures().toOneLineString());
			time += ads.solver.getMeasures().getTimeCount();
		}
		System.out.println("total time : "+time);
	}
}
