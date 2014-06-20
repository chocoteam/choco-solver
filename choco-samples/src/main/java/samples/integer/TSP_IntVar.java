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

package samples.integer;

import samples.AbstractProblem;
import samples.graph.input.TSP_Utils;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.ICF;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.search.strategy.selectors.IntValueSelector;
import solver.search.strategy.strategy.FindAndProve;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.VariableFactory;

/**
 * Solves the Traveling Salesman Problem
 * parses TSP instances of the TSPLIB library
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 * proposes several optimization strategies
 * <p/>
 * Note that using the LKH heuristic as a pre-processing would speed up the resolution
 *
 * @author Jean-Guillaume Fages
 * @since June. 2014
 */
public class TSP_IntVar extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private String directory = "/Users/jfages07/github/In4Ga/benchRousseau";
	private String instance = "eil76";
	private int[][] costMatrix;
	private IntVar[] succOf;
	private IntVar totalCost;
	private boolean strongFilter = true;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		new TSP_IntVar().execute(args);
	}

	@Override
	public void createSolver() {
		solver = new Solver("solving the Traveling Salesman Problem");
	}

	@Override
	public void buildModel() {
		costMatrix = TSP_Utils.parseInstance(directory+"/"+instance+".tsp", 500);
		final int n = costMatrix.length;
		// variables
		totalCost = VariableFactory.bounded("obj", 0, 9999999, solver);
		succOf = VF.enumeratedArray("succOf",n,0,n-1,solver);
		// constraints (note that the strong filter applies only after a first solution has been found)
		solver.post(ICF.tsp(succOf, totalCost, costMatrix, strongFilter));
	}

	@Override
	public void configureSearch() {
		SMF.limitTime(solver, "30s");
		solver.set(new FindAndProve<IntVar>(
				succOf,
				// heuristic to find a good first solution
				ISF.custom(ISF.minDomainSize_var_selector(),cheapSuccSelector(), succOf),
				// heuristic to close the search tree
				ISF.minDom_LB(succOf)
		));
		solver.set(ISF.lastConflict(solver));
		solver.set(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), solver.getStrategy());
	}

	private IntValueSelector cheapSuccSelector(){
		return new IntValueSelector() {
			@Override
			public int selectValue(IntVar var) {
				// retrieve variable
				int i = -1;
				for(int k=0;k<succOf.length && i==-1;k++)
					if(succOf[k] == var)
						i = k;
				int bestSuc = -1;
				for(int j=var.getLB();j<=var.getUB();j=var.nextValue(j)){
					if(bestSuc == -1 || costMatrix[i][j] < costMatrix[i][bestSuc]){
						bestSuc = j;
					}
				}
				return bestSuc;
			}
		};
	}

	@Override
	public void solve() {
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
	}

	@Override
	public void prettyOut() {}
}