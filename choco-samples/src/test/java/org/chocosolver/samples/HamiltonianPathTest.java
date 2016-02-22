/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.samples.graph.input.GraphGenerator;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.PoolManager;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;
import static org.testng.Assert.assertTrue;

/**
 * Find a Hamiltonian path in a sparse graph with incremental algorithm
 * test the correctness of fine event recorders
 *
 * @author Jean-Guillaume Fages
 */
public class HamiltonianPathTest {

	private final static long TIME_LIMIT = 1000;

	@Test(groups="5m", timeOut=300000)
	public static void test() {
		int[] sizes = new int[]{20, 40};
		long s;
		int[] nbVoisins = new int[]{3, 5, 10};
		boolean[][] matrix;
		for (int n : sizes) {
			for (int nb : nbVoisins) {
				for (int ks = 0; ks < 20; ks++) {
					s = System.currentTimeMillis();
					System.out.println("n:" + n + " nbVoisins:" + nb + " s:" + s);
					GraphGenerator gg = new GraphGenerator(n, s, GraphGenerator.InitialProperty.HamiltonianCircuit);
					matrix = transformMatrix(gg.neighborBasedGenerator(nb));
					testInt(matrix, s, true, false);
					testInt(matrix, s, false, false);
					testInt(matrix, s, true, true);
					testInt(matrix, s, false, true);
				}
			}
		}
	}

	private static void testInt(boolean[][] matrix, long seed, boolean rd, boolean enumerated) {
		Model model = new Model();
		int n = matrix.length;
		// build model
		IntVar[] succ = new IntVar[n];
		int offset = -5;
		TIntArrayList l = new TIntArrayList();
		for (int i = 0; i < n - 1; i++) {
			l.clear();
			for (int j = 0; j < n; j++) {
				if (matrix[i][j]) {
					l.add(j + offset);
				}
			}
			if (l.isEmpty()) throw new UnsupportedOperationException();
			if (enumerated) {
				succ[i] = model.intVar("suc", l.toArray());
			} else {
				succ[i] = model.intVar("suc", offset, n + offset, true);
				model.member(succ[i], l.toArray()).post();
			}
		}
		succ[n - 1] = model.intVar(n + offset);
		model.path(succ, model.intVar(offset), model.intVar(n - 1 + offset), offset).post();
		// configure solver
		if (rd) {
			model.getSolver().set(randomSearch(succ, seed));
		} else {
			model.getSolver().set(new ConstructorIntHeur(succ, offset));
		}
		model.getSolver().limitTime(TIME_LIMIT);
		model.solve();
		IMeasures mes = model.getSolver().getMeasures();
		// the problem has at least one solution
		assertTrue(mes.getSolutionCount() == 1 || model.getSolver().isStopCriterionMet(),
				"sol count:" + mes.getSolutionCount() + ", has reached limit: " + model.getSolver().isStopCriterionMet());
	}

	private static boolean[][] transformMatrix(boolean[][] m) {
		int n = m.length + 1;
		boolean[][] matrix = new boolean[n][n];
		for (int i = 0; i < n - 1; i++) {
			System.arraycopy(m[i], 1, matrix[i], 1, n - 1 - 1);
			matrix[i][n - 1] = m[i][0];
		}
		return matrix;
	}

	private static class ConstructorIntHeur extends AbstractStrategy<IntVar> {
		int n, offset;
		PoolManager<IntDecision> pool;

		public ConstructorIntHeur(IntVar[] v, int off) {
			super(v);
			offset = off;
			n = v.length;
			pool = new PoolManager<>();
		}

		@Override
		public Decision<IntVar> getDecision() {
			int x = 0;
			while (vars[x].isInstantiated()) {
				x = vars[x].getValue()-offset;
				if(x==vars.length){
					return null;
				}
			}
			IntDecision d = pool.getE();
			if(d==null)d=new IntDecision(pool);
			d.set(vars[x], vars[x].getLB(), DecisionOperator.int_eq);
			return d;
		}
	}
}
