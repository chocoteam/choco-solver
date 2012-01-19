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

import choco.checker.DomainBuilder;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20 sept. 2010
 */
public class AllDifferentTest {

	public static void model(boolean simple, int n, int nbSol) {
		Solver s = new Solver();
		IEnvironment env = s.getEnvironment();

		IntVar[] vars = new IntVar[n];
		int i = 0;
		vars[0] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[1] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[2] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[3] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[4] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[5] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[6] = VariableFactory.enumerated("v_" + (i++), 1, n, s);
		vars[7] = VariableFactory.enumerated("v_" + (i), 1, n, s);

		List<Constraint> lcstrs = new ArrayList<Constraint>(10);
		IntVar[] allvars;
		if (simple) {
			allvars = vars;

			lcstrs.add(new AllDifferent(vars, s));
			for (i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					int k = j - i;
					lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], s));
					lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], k, s));
					lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], -k, s));
				}
			}
		} else {
			IntVar[] diag1 = new IntVar[n];
			IntVar[] diag2 = new IntVar[n];

			for (i = 0; i < n; i++) {
				diag1[i] = VariableFactory.enumerated("v_" + (i + 2 * n), -n, n, s);
				diag2[i] = VariableFactory.enumerated("v_" + (i + n), 1, 2 * n, s);
			}
			allvars = ArrayUtils.append(vars, diag1, diag2);

			lcstrs.add(new AllDifferent(vars, s));

			for (i = 0; i < n; i++) {
				lcstrs.add(ConstraintFactory.eq(diag1[i], vars[i], -i, s));
				lcstrs.add(ConstraintFactory.eq(diag2[i], vars[i], i, s));
			}
			lcstrs.add(new AllDifferent(diag1, s));
			lcstrs.add(new AllDifferent(diag2, s));

		}


		Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

		AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);

		s.post(cstrs);
		s.set(strategy);

		s.findAllSolutions();
		long sol = s.getMeasures().getSolutionCount();
		Assert.assertEquals(sol, nbSol, "nb sol incorrect");
	}


	@Test(groups = "10s")
	public void test1() {
		model(true, 8, 92);
		model(false, 8, 92);
	}

	@Test(groups = "1s")
	public void test2() {
		model(true, 8, 92);
	}

	@Test(groups = "1s")
	public void test3() {

		Solver s = new Solver();

		int n = 4;
		IntVar[] vars = new IntVar[n];
		vars[0] = VariableFactory.enumerated("v_0", new int[]{1, 6}, s);
		vars[1] = VariableFactory.enumerated("v_1", new int[]{1, 3}, s);
		vars[2] = VariableFactory.enumerated("v_2", new int[]{3, 5}, s);
		vars[3] = VariableFactory.enumerated("v_3", new int[]{1, 3, 5, 6}, s);


		List<Constraint> lcstrs = new ArrayList<Constraint>(10);
		List<Constraint> lcstrs1 = new ArrayList<Constraint>(1);
		List<Constraint> lcstrs2 = new ArrayList<Constraint>(10);

		lcstrs1.add(new AllDifferent(vars, s));
		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {
				int k = j - i;
				lcstrs2.add(ConstraintFactory.neq(vars[i], vars[j], -k, s));
				lcstrs2.add(ConstraintFactory.neq(vars[i], vars[j], k, s));
			}
		}
		lcstrs.addAll(lcstrs1);
		lcstrs.addAll(lcstrs2);

		Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

		s.post(cstrs);
		s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
		//        ChocoLogging.toSolution();
		s.findAllSolutions();
		long sol = s.getMeasures().getSolutionCount();
		Assert.assertEquals(sol, 1, "nb sol incorrect");

	}


	@Test(groups = "1s")
	public void test4() {

		Solver s = new Solver();

		int n = 5;
		IntVar[] vars = new IntVar[n];
		vars[0] = Views.fixed("v_0", 5, s);
		vars[1] = Views.fixed("v_1", 3, s);
		vars[2] = VariableFactory.bounded("v_2", 3, 4, s);
		vars[3] = VariableFactory.bounded("v_3", 2, 6, s);
		vars[4] = VariableFactory.bounded("v_4", 2, 6, s);


		List<Constraint> lcstrs = new ArrayList<Constraint>(10);

		lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.BC));

		Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

		s.post(cstrs);
		s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
		s.findAllSolutions();
		long sol = s.getMeasures().getSolutionCount();
		Assert.assertEquals(sol, 2, "nb sol incorrect");

	}


	@Test(groups = "1h")
	public void test6() {
		Random rand;
		for (int seed = 0; seed < 10; seed++) {
			rand = new Random(seed);
			for (double d = 0.25; d <= 1.0; d += 0.25) {
				for (int h = 0; h <= 1; h++) {
					for (int b = 0; b <= 1; b++) {
						int n = 1 + rand.nextInt(7);
						int[][] domains = DomainBuilder.buildFullDomains(n, 1, 2*n, rand, d, h == 0);

						Solver neqs = alldiffs(domains, 0, b == 0);
						neqs.findAllSolutions();

						Solver clique = alldiffs(domains, 1, b == 0);
						clique.findAllSolutions();
						Assert.assertEquals(clique.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect "+seed);
						Assert.assertEquals(clique.getMeasures().getNodeCount(), neqs.getMeasures().getNodeCount(), "nb nod incorrect"+ seed);

						Solver bc = alldiffs(domains, 2, b == 0);
						bc.findAllSolutions();
						Assert.assertEquals(bc.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect "+seed);
						Assert.assertTrue(bc.getMeasures().getNodeCount() <= neqs.getMeasures().getNodeCount(), "nb nod incorrect"+ seed);

						Solver ac = alldiffs(domains, 3, b == 0);
						ac.findAllSolutions();
						Assert.assertEquals(ac.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect "+seed);
						Assert.assertTrue(ac.getMeasures().getNodeCount() <= neqs.getMeasures().getNodeCount(), "nb nod incorrect"+ seed);

						Solver graph = alldiffs(domains, 4, b==0);
						graph.findAllSolutions();
						Assert.assertEquals(graph.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect "+seed);
						Assert.assertTrue(graph.getMeasures().getFailCount() == 0 || b==0, "gac failed"+ seed);
						Assert.assertTrue(graph.getMeasures().getNodeCount() == ac.getMeasures().getNodeCount(), "nb nod incorrect"+ seed);
						
						LoggerFactory.getLogger("test").info("{}ms - {}ms - {}ms - {}ms", new Object[]{
								neqs.getMeasures().getTimeCount(), clique.getMeasures().getTimeCount(),
								bc.getMeasures().getTimeCount(), ac.getMeasures().getTimeCount()});
					}
				}
			}
		}
	}


	protected Solver alldiffs(int[][] domains, int c, boolean bounded) {
		Solver s = new Solver();

		IntVar[] vars = new IntVar[domains.length];
		if (bounded) {
			for (int i = 0; i < domains.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
			}
		} else {
			for (int i = 0; i < domains.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
			}
		}

		List<Constraint> lcstrs = new ArrayList<Constraint>(10);

		switch (c) {
		case 0:
			for (int i = 0; i < vars.length - 1; i++) {
				for (int j = i + 1; j < vars.length; j++) {
					lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], s));
				}
			}
			break;
		case 1:
			lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.CLIQUE));
			break;
		case 2:
			lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.BC));
			break;
		case 3:
			lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.AC));
			break;
		case 4:
			lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.GRAPH));
			break;
		}

		Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

		s.post(cstrs);
		s.set(StrategyFactory.inputOrderMinVal(vars, s.getEnvironment()));
		return s;
	}

    @Test
    public void testXX(){
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[4];
        ts[0] = VariableFactory.enumerated("t0", new int[]{2,3,4}, solver);
        ts[1] = VariableFactory.enumerated("t1", new int[]{-3,-2,-1,1,2}, solver);
        ts[2] = VariableFactory.enumerated("t2", new int[]{-3,-2,-1,1,2,3}, solver);
        ts[3] = VariableFactory.enumerated("t3", new int[]{-3,-2,-1,1,2,3}, solver);

        try{
            solver.propagate();
            ts[0].removeValue(2, Cause.Null);
            ts[1].removeValue(2, Cause.Null);
            ts[0].removeValue(3, Cause.Null);
            ts[1].removeValue(1, Cause.Null);
            ts[2].removeValue(-3, Cause.Null);
            ts[2].removeValue(3, Cause.Null);
            ts[3].removeValue(-3, Cause.Null);
            ts[3].removeValue(3, Cause.Null);
            solver.propagate();
        }catch (ContradictionException ex){

        }
        System.out.printf("%s\n", solver.toString());



    }

}
