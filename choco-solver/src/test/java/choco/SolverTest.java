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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.SolverException;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import java.text.MessageFormat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 juil. 2010
 */
public class SolverTest {

	final static int[] capacites = {0, 34};
	final static int[] energies = {6, 4, 3};
	final static int[] volumes = {7, 5, 2};
	final static int[] nbOmax = {4, 6, 17};
	final static int n = 3;

	public static Solver knapsack() {
		Solver s = new Solver();
		IntVar power = VariableFactory.enumerated("v_" + n, 0, 999999, s);
		IntVar[] objects = new IntVar[n];
		for (int i = 0; i < n; i++) {
			objects[i] = VariableFactory.enumerated("v_" + i, 0, nbOmax[i], s);
		}
		s.post(IntConstraintFactory.scalar(objects, volumes, VariableFactory.bounded("capa", capacites[0], capacites[1], s)));
		s.post(IntConstraintFactory.scalar(objects, energies, power));
		s.set(IntStrategyFactory.inputOrder_InDomainMin(objects));
		return s;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final int ONE = 0, NEXT = 1, ALL = 2, OPT = 3;

	public static void conf(Solver s, int... is) throws SolverException {
		for (int i : is) {
			switch (i) {
				case ONE:
					s.findSolution();
					break;
				case NEXT:
					s.nextSolution();
					break;
				case ALL:
					s.findAllSolutions();
					break;
				case OPT:
					s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, (IntVar) s.getVar(0));
					break;
				default:
					Assert.fail("unknonw case");
					break;
			}
		}
	}

	@Test(groups = "10s")
	public void testRight() {
		boolean alive = true;
		int cas = 0;
		while (alive) {
			cas++;
			Solver s = knapsack();
			try {
				switch (cas) {
					case 1:
						conf(s, ONE);
						break;
					case 2:
						conf(s, ONE, NEXT);
						break;
					case 3:
						conf(s, ONE, NEXT, NEXT);
						break;
					case 4:
						conf(s, ALL);
						break;
					case 5:
						conf(s, OPT);
						break;
					case 6:
						conf(s, ALL, NEXT);
						break;
					case 7:
						conf(s, OPT, NEXT);
						break;
					default:
						alive = false;

				}
			} catch (SolverException ingored) {
				Assert.fail(MessageFormat.format("Fail on {0}", cas));
			}

		}
	}

	@Test(groups = "10s")
	public void testWrong() {
		boolean alive = true;
		int cas = 0;
		while (alive) {
			cas++;
			Solver s = knapsack();
			try {
				switch (cas) {
					case 1:
						conf(s, ONE, ONE);
						break;
					case 2:
						conf(s, ONE, ALL);
						break;
					case 3:
						conf(s, ONE, OPT);
						break;
					case 4:
						conf(s, NEXT);
						break;
					case 5:
						conf(s, ALL, ONE);
						break;
					case 6:
						conf(s, ALL, ALL);
						break;
					case 7:
						conf(s, ALL, OPT);
						break;
					case 8:
						conf(s, OPT, ONE);
						break;
					case 9:
						conf(s, OPT, ALL);
						break;
					case 10:
						conf(s, OPT, OPT);
						break;
					default:
						alive = false;
						throw new SolverException("to stop ^^");

				}
				Assert.fail("Fail on " + cas);

			} catch (SolverException ignored) {
			}

		}
	}

}
