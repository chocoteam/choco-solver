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

package samples.graph;

import memory.setDataStructures.ISet;
import memory.setDataStructures.SetType;
import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import samples.graph.input.HCP_Utils;
import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Solves the Hamiltonian Cycle Problem
 * <p/>
 * Uses graph variables and a light but fast filtering
 * Parses HCP instances of the TSPLIB:
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class HamiltonianCycleProblem extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	@Option(name = "-tl", usage = "time limit.", required = false)
	private long limit = 10000;
	@Option(name = "-inst", usage = "TSPLIB HCP Instance file path.", required = false)
	private String instancePath = "/Users/jfages07/Documents/code/ALL_hcp/alb5000.hcp";
	// graph variable expected to form a Hamiltonian Cycle
	private UndirectedGraphVar graph;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		new HamiltonianCycleProblem().execute(args);
	}

	@Override
	public void createSolver() {
		solver = new Solver("solving the Hamiltonian Cycle Problem");
	}

	@Override
	public void buildModel() {
		boolean[][] matrix = HCP_Utils.parseTSPLIBInstance(instancePath);
		int n = matrix.length;
		// variables (use linked lists because the graph is sparse)
		graph = new UndirectedGraphVar("G", solver, n, SetType.LINKED_LIST, SetType.LINKED_LIST, true);
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (matrix[i][j]) {
					graph.getEnvelopGraph().addEdge(i, j);
				}
			}
		}
		// constraints
		solver.post(GraphConstraintFactory.hamiltonianCycle(graph));
	}

	@Override
	public void configureSearch() {
		// basically branch on sparse areas of the graph
		solver.set(GraphStrategyFactory.graphStrategy(graph, null, new MinNeigh(graph), GraphStrategy.NodeArcPriority.ARCS));
		SearchMonitorFactory.limitTime(solver,limit);
		SearchMonitorFactory.log(solver, false, false);
	}

	@Override
	public void configureEngine() {}

	@Override
	public void solve() {
		solver.findSolution();
	}

	@Override
	public void prettyOut() {}

	//***********************************************************************************
	// HEURISTICS
	//***********************************************************************************

	private static class MinNeigh extends ArcStrategy<UndirectedGraphVar> {
		int n;

		public MinNeigh(UndirectedGraphVar graphVar) {
			super(graphVar);
			n = graphVar.getEnvelopGraph().getNbNodes();
		}

		@Override
		public boolean computeNextArc() {
			ISet suc;
			int from = -1;
			int size = n + 1;
			int sizi;
			for (int i = 0; i < n; i++) {
				sizi = g.getEnvelopGraph().getNeighborsOf(i).getSize() - g.getKernelGraph().getNeighborsOf(i).getSize();
				if (sizi < size && sizi > 0) {
					from = i;
					size = sizi;
				}
			}
			if (from == -1) {
				return false;
			}
			suc = g.getEnvelopGraph().getNeighborsOf(from);
			this.from = from;
			to = 2 * n;
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if (!g.getKernelGraph().edgeExists(from, j)) {
					if (j < to) {
						to = j;
					}
				}
			}
			if (to == 2 * n) {
				throw new UnsupportedOperationException();
			}
			return true;
		}
	}
}