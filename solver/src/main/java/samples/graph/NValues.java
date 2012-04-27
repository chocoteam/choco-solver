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
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.gary.GraphProperty;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class NValues extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private int k;
	private IntVar[] vars;
	private UndirectedGraphVar g;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	
	public NValues(int n, int k) {
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
		vars[0] = VariableFactory.enumerated("vars_0", new int[]{2}, solver);
		vars[1] = VariableFactory.enumerated("vars_1", new int[]{3}, solver);
		vars[2] = VariableFactory.enumerated("vars_2", new int[]{2}, solver);
		vars[3] = VariableFactory.enumerated("vars_3", new int[]{2,3}, solver);
		IntVar nv = VariableFactory.enumerated("n", 4,4, solver);
		IntVar nVal = VariableFactory.bounded("N_CC", k,k, solver);
		GraphConstraint gc = GraphConstraintFactory.nIntegers(vars, nVal, solver);
		g = (UndirectedGraphVar) gc.getGraph();
		gc.addProperty(GraphProperty.K_NODES, nv);
		Constraint[] cstrs = new Constraint[]{gc};
		solver.post(cstrs);
	}

	@Override
	public void configureSearch() {
		AbstractStrategy strategy = StrategyFactory.graphLexico(g);
		solver.set(strategy);
	}

    @Override
    public void configureEngine() {
    }

    @Override
	public void solve() {
		SearchMonitorFactory.log(solver, false, false);
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
		System.out.println(g.instantiated());
		System.out.println("env"+g.getEnvelopGraph());
		System.out.println("ker "+g.getKernelGraph());
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	public static void main(String[] args) {
		int n = 10;
		int k = 3;
		NValues nc = new NValues(n,k);
		nc.execute();
	}
}
