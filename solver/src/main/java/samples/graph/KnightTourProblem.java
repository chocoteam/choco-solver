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

import choco.kernel.ESat;
import choco.kernel.common.util.PoolManager;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetType;
import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import samples.sandbox.graph.input.HCP_Utils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.*;
import solver.variables.graph.GraphVar;
import solver.variables.graph.UndirectedGraphVar;

import java.util.ArrayList;

/**
 * Solves the Knight's Tour Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class KnightTourProblem extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	@Option(name = "-tl", usage = "time limit.", required = false)
	private long limit = 60000;
	@Option(name = "-l", usage = "Board length.", required = false)
	private int boardLength = 50;
	@Option(name = "-open", usage = "Open tour (path instead of cycle).", required = false)
	private boolean closedTour = false;

	private UndirectedGraphVar graph;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		new KnightTourProblem().execute(args);
	}

	@Override
	public void createSolver() {
		solver = new Solver("solving the knight's tour problem with graph variables");
	}

	@Override
	public void buildModel() {
		boolean[][] matrix;
		if(closedTour){
			matrix = HCP_Utils.generateKingTourInstance(boardLength);
		}else{
			matrix = HCP_Utils.generateOpenKingTourInstance(boardLength);
		}
		int n = matrix.length;
		// variables
		graph = new UndirectedGraphVar("G",solver, n, SetType.LINKED_LIST, SetType.LINKED_LIST, true);
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
		solver.set(GraphStrategyFactory.graphStrategy(graph, null, new MinNeigh(graph), GraphStrategy.NodeArcPriority.ARCS));
//		solver.getSearchLoop().getLimitsBox().setTimeLimit(limit);
		SearchMonitorFactory.log(solver, true, false);
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

	private static class MinNeigh extends ArcStrategy {
		int n;

		public MinNeigh(GraphVar graphVar) {
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
				sizi = g.getEnvelopGraph().getSuccessorsOf(i).getSize() - g.getKernelGraph().getSuccessorsOf(i).getSize();
				if (sizi < size && sizi > 0) {
					from = i;
					size = sizi;
				}
			}
			if (from == -1) {
				System.out.println("over");
				return false;
			}
			suc = g.getEnvelopGraph().getSuccessorsOf(from);
//			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
//				if(!g.getKernelGraph().arcExists(from,j)){
//					this.from = from;
//					this.to = j;
//					return true;
//				}
//			}
			this.from = from;
			to = 2 * n;
			for (int j = suc.getFirstElement(); j >= 0; j = suc.getNextElement()) {
				if (!g.getKernelGraph().arcExists(from, j)) {
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

	private static class MinNeighBool extends AbstractStrategy<BoolVar> {
		int n;
		PoolManager<FastDecision> pool;
		ArrayList<BoolVar>[] gl;

		public MinNeighBool(BoolVar[] vars, ArrayList<BoolVar>[] gl) {
			super(vars);
			n = gl.length;
			this.gl = gl;
			pool = new PoolManager<FastDecision>();
		}

		@Override
		public void init() {
		}

		@Override
		public Decision getDecision() {
			int from = -1;
			int size = n + 1;
			int sizi;
			for (int i = 0; i < n; i++) {
				sizi = 0;
				ArrayList<BoolVar> l = gl[i];
				for (BoolVar bv : l) {
					if (bv.getUB() == 1 && bv.getLB() == 0) {
						sizi++;
					}
				}
				if (sizi < size && sizi > 0) {
					from = i;
					size = sizi;
				}
			}
			if (from == -1) {
				System.out.println("over");
				return null;
			}
			ArrayList<BoolVar> l = gl[from];
			for (BoolVar bv : l) {
				if (bv.getUB() == 1 && bv.getLB() == 0) {
					FastDecision dec = pool.getE();
					if (dec == null) {
						dec = new FastDecision(pool);
					}
					dec.set(bv, 1, DecisionOperator.int_eq);
					return dec;
				}
			}
			throw new UnsupportedOperationException();
		}
	}

	//***********************************************************************************
	// PROPAGATORS
	//***********************************************************************************

	private static class PropBoolNoSubtour extends Propagator<BoolVar> {

		//***********************************************************************************
		// VARIABLES
		//***********************************************************************************

		protected BoolVar[][] g;
		protected int n;
		int[] mapping;
		protected IStateInt[] e1, e2, size;

		//***********************************************************************************
		// CONSTRUCTORS
		//***********************************************************************************

		/**
		 * Ensures that graph has no circuit, with Caseaux/Laburthe/Pesant algorithm
		 * runs in O(1) per instantiation event
		 *
		 * @param mapping
		 * @param graph
		 * @param constraint
		 * @param solver
		 */
		public PropBoolNoSubtour(int[] mapping, BoolVar[] decVars, BoolVar[][] graph, Constraint constraint, Solver solver) {
			super(decVars, solver, constraint, PropagatorPriority.UNARY, true);
			g = graph;
			this.mapping = mapping;
			this.n = graph.length;
			e1 = new IStateInt[n];
			size = new IStateInt[n];
			e2 = new IStateInt[n];
			for (int i = 0; i < n; i++) {
				e1[i] = environment.makeInt(i);
				size[i] = environment.makeInt(1);
				e2[i] = environment.makeInt(i);
			}
		}

		//***********************************************************************************
		// METHODS
		//***********************************************************************************

		@Override
		public void propagate(int evtmask) throws ContradictionException {
			for (int i = 0; i < n; i++) {
				e1[i].set(i);
				e2[i].set(i);
				size[i].set(1);
			}
			int s = vars.length;
			for (int k = 0; k < s; k++) {
				if (vars[k].getLB() == 1) {
					int i = mapping[k] / n;
					int j = mapping[k] % n;
					enforce(i, j);
				}
			}
		}

		@Override
		public void propagate(int idxVarInProp, int mask) throws ContradictionException {
			if (vars[idxVarInProp].getLB() == 1) {
				int i = mapping[idxVarInProp] / n;
				int j = mapping[idxVarInProp] % n;
				enforce(i, j);
			}
		}

		@Override
		public int getPropagationConditions(int vIdx) {
			return EventType.INSTANTIATE.mask + EventType.REMOVE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
		}

		@Override
		public ESat isEntailed() {
			return ESat.TRUE; //not implemented
		}

		private void enforce(int i, int j) throws ContradictionException {
			int ext1 = getExt(i);
			int ext2 = getExt(j);
			int t = size[ext1].get() + size[ext2].get();
			setExt(ext1, ext2);
			setExt(ext2, ext1);
			size[ext1].set(t);
			size[ext2].set(t);
			if (t > 2 && t <= n)
				if (t < n) {
					if (g[ext1][ext2] != null)
						g[ext1][ext2].setToFalse(aCause);
				} else if (t == n) {
					g[ext1][ext2].setToTrue(aCause);
				}
		}

		private int getExt(int i) {
			return (e1[i].get() == i) ? e2[i].get() : e1[i].get();
		}

		private void setExt(int i, int ext) {
			if (e1[i].get() == i) {
				e2[i].set(ext);
			} else {
				e1[i].set(ext);
			}
		}
	}

}