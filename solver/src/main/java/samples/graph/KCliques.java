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

import choco.kernel.common.util.tools.ArrayUtils;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphProperty;
import solver.constraints.gary.GraphRelation;
import solver.constraints.propagators.PropagatorPriority;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class KCliques extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private int k;
	private IntVar[] vars;
	private BoolVar[][] rel;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	
	public KCliques(int n, int k) {
		this.n = n;
		this.k = k;
		System.out.println(n+" : "+k);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		solver = new Solver();
		vars = VariableFactory.enumeratedArray("vars", n, 0, n-1, solver);
		rel = new BoolVar[n][n];

		vars[0] = VariableFactory.enumerated("v0", new int[]{2,5,42}, solver);
		for(int i=0; i<n; i++){
			for(int j=i; j<n; j++){
				rel[i][j]  = VariableFactory.bool("rel "+i+"_"+j, solver);
			}
		}
		for(int i=0; i<n; i++){
			for(int j=0; j<i; j++){
				rel[i][j]  = rel[j][i];
			}
		}

		GraphConstraint gc = new GraphConstraint(vars, rel, solver, PropagatorPriority.LINEAR, false, GraphRelation.EQUALITY);
		Constraint[] cstrs = new Constraint[]{gc};

		IntVar nv = VariableFactory.enumerated("n", n, n, solver);
		gc.addProperty(GraphProperty.K_LOOPS, nv);
		gc.addProperty(GraphProperty.K_CLIQUES, VariableFactory.bounded("N_CC", k, k, solver));
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.inputOrderMaxVal(ArrayUtils.flatten(rel), solver.getEnvironment());
		solver.set(strategy);
	}

	@Override
	public void solve() {
		Boolean status = solver.findSolution();
	}

	@Override
	public void prettyOut() {
		System.out.println(solver.getMeasures().getSolutionCount()+" sols");
		System.out.println(solver.getMeasures().getFailCount()+" fails");
		System.out.println(solver.getMeasures().getTimeCount()+" ms");
		for(int i=0;i<n;i++){
			System.out.println(vars[i]);
		}
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	public static void main(String[] args) {
		int n = 6;
		int k = 2;
		KCliques nc = new KCliques(n,k);
		nc.execute();
	}
}
