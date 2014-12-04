/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22 nov. 2010
 */
public class ClauseTest {

    @Test(groups = "1m")
    public void test1() {
        int nSol = 1;
        for (int n = 1; n < 16; n++) {
            for (int i = 0; i <= n; i++) {
                Solver s = new Solver();

				final BoolVar[] bsource = new BoolVar[n];
				final BoolVar[] bs = new BoolVar[n];

				for (int j = 0; j < n; j++) {
					bsource[j] = VariableFactory.bool("b" + j, s);
				}

				for (int j = 0; j < n; j++) {
					if (j >= i) {
						bs[j] = bsource[j].not();
					}else{
						bs[j] = bsource[j];
					}
				}

                LogOp or = LogOp.or(bs);
                LoggerFactory.getLogger("test").info(or.toString());
                SatFactory.addClauses(or, s);
                s.set(IntStrategyFactory.lexico_LB(bs));

				s.findAllSolutions();
                long sol = s.getMeasures().getSolutionCount();
                Assert.assertEquals(sol, nSol);
            }
            nSol = nSol * 2 + 1;
        }
    }

    @Test(groups = "1s")
    public void testBothAnd() {
        Solver s = new Solver();

        BoolVar[] bs = new BoolVar[1];
        bs[0] = VariableFactory.bool("to be", s);

        LogOp and = LogOp.and(bs[0], bs[0].not());

        SatFactory.addClauses(and, s);
        s.set(IntStrategyFactory.lexico_LB(bs));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 0);
    }

    @Test(groups = "1s")
    public void testBothOr() {
        Solver s = new Solver();

        BoolVar b = VariableFactory.bool("to be", s);

        LogOp or = LogOp.or(b, b.not());

        SatFactory.addClauses(or, s);

        BoolVar[] bs = new BoolVar[]{b};
        s.set(IntStrategyFactory.lexico_LB(bs));
//        SMF.log(s, true, true);
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2);
    }


    @Test(groups = "1s")
    public void test2() {
        Solver solver = new Solver();
        BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
        LogOp tree = LogOp.or(bvars[0], bvars[1]);
        SatFactory.addClauses(tree, solver);

        try {
            solver.propagate();
            bvars[1].instantiateTo(0, Cause.Null);
            bvars[0].instantiateTo(1, Cause.Null);
            solver.propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups = "1s")
    public void test30() {
        Solver solver = new Solver();
        BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
        LogOp tree = LogOp.or(bvars[0], bvars[1]);
        SatFactory.addClauses(tree, solver);

        try {
            solver.propagate();
            bvars[1].instantiateTo(1, Cause.Null);
            bvars[0].instantiateTo(0, Cause.Null);
            solver.propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups = "1s")
    public void test31() {
        Solver solver = new Solver();
        BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not());
        SatFactory.addClauses(tree, solver);

        try {
            solver.propagate();
            bvars[0].instantiateTo(0, Cause.Null);
            solver.propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(bvars[1].isInstantiatedTo(0));
    }

    @Test(groups = "1s")
    public void test32() {
        Solver solver = new Solver();
        BoolVar[] bvars = VariableFactory.boolArray("b", 2, solver);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not());
        SatFactory.addClauses(tree, solver);

        try {
            solver.propagate();
            bvars[1].instantiateTo(1, Cause.Null);
            solver.propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertTrue(bvars[0].isInstantiatedTo(1));
    }

    @Test(groups = "1s")
    public void test33() {
        Solver solver = new Solver();
        BoolVar[] bvars = VariableFactory.boolArray("b", 3, solver);
        LogOp tree = LogOp.or(bvars[0], bvars[1].not(), bvars[2].not());
        SatFactory.addClauses(tree, solver);

        try {
            solver.propagate();
            bvars[0].instantiateTo(0, Cause.Null);
            bvars[2].instantiateTo(0, Cause.Null);
            bvars[1].instantiateTo(1, Cause.Null);
            solver.propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }

    @Test(groups = "1s")
    public void test4() {
        for (int seed = 0; seed < 2000; seed++) {
            long n1, n2;
            {
                Solver solver = new Solver();
                BoolVar[] bvars = VariableFactory.boolArray("b", 3, solver);
                LogOp tree = LogOp.ifOnlyIf(
                        LogOp.and(bvars[1], bvars[2]),
                        bvars[0]);
                SatFactory.addClauses(tree, solver);

                solver.set(IntStrategyFactory.random_bound(bvars, seed));
                solver.findAllSolutions();
                n1 = solver.getMeasures().getSolutionCount();
            }
            {
                Solver solver = new Solver();
                BoolVar[] bvars = VariableFactory.boolArray("b", 3, solver);
                solver.post(IntConstraintFactory.times(bvars[1], bvars[2], bvars[0]));

                solver.set(IntStrategyFactory.random_bound(bvars, seed));
                solver.findAllSolutions();
                n2 = solver.getMeasures().getSolutionCount();
            }
            Assert.assertEquals(n2, n1, String.format("seed: %d", seed));
        }

    }

    @Test(groups = "10s")
    public void test5() {
        Random rand = new Random();
        for (int seed = 0; seed < 20000; seed++) {
            rand.setSeed(seed);
            int n1 = rand.nextInt(3);
            int n2 = rand.nextInt(3);
            while (n1 == n2) {
                n2 = rand.nextInt(3);
            }
            boolean b1 = rand.nextBoolean();
            boolean b2 = rand.nextBoolean();
            boolean s1, s2;
            {
                Solver solver = new Solver();
                BoolVar[] bvars = VariableFactory.boolArray("b", 3, solver);
                LogOp tree = LogOp.ifOnlyIf(
                        LogOp.and(bvars[1], bvars[2]),
                        bvars[0]);
                SatFactory.addClauses(tree, solver);
                try {
                    solver.propagate();
                    bvars[n1].instantiateTo(b1 ? 1 : 0, Cause.Null);
                    bvars[n2].instantiateTo(b2 ? 1 : 0, Cause.Null);
                    s1 = true;
                } catch (ContradictionException cex) {
                    s1 = false;
                }
            }
            {
                Solver solver = new Solver();
                BoolVar[] bvars = VariableFactory.boolArray("b", 3, solver);
                solver.post(IntConstraintFactory.times(bvars[1], bvars[2], bvars[0]));
                try {
                    solver.propagate();
                    bvars[n1].instantiateTo(b1 ? 1 : 0, Cause.Null);
                    bvars[n2].instantiateTo(b2 ? 1 : 0, Cause.Null);
                    s2 = true;
                } catch (ContradictionException cex) {
                    s2 = false;
                }
            }
            Assert.assertEquals(s2, s1, String.format("seed: %d", seed));
        }

    }


}
