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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < n; i++) {
            vars[i] = s.intVar("v_" + i, 1, n, false);
        }
        s.post(IntConstraintFactory.alldifferent(vars, "BC"));
        if (simple) {

            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    int k = j - i;
                    s.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j]));
                    s.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
                    s.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                }
            }
        } else {
            IntVar[] diag1 = new IntVar[n];
            IntVar[] diag2 = new IntVar[n];

            for (int i = 0; i < n; i++) {
                diag1[i] = s.intVar("v_" + (i + 2 * n), -n, n, false);
                diag2[i] = s.intVar("v_" + (i + n), 1, 2 * n, false);
            }
            for (int i = 0; i < n; i++) {
                s.post(IntConstraintFactory.arithm(diag1[i], "=", vars[i], "-", i));
                s.post(IntConstraintFactory.arithm(diag2[i], "=", vars[i], "+", i));
            }
            s.post(IntConstraintFactory.alldifferent(diag1, "BC"));
            s.post(IntConstraintFactory.alldifferent(diag2, "BC"));
        }
        AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
        s.set(strategy);
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, nbSol, "nb sol incorrect");
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        model(true, 8, 92);
        model(false, 8, 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        model(true, 8, 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {

        Solver s = new Solver();

        int n = 4;
        IntVar[] vars = new IntVar[n];
        vars[0] = s.intVar("v_0", new int[]{1, 6});
        vars[1] = s.intVar("v_1", new int[]{1, 3});
        vars[2] = s.intVar("v_2", new int[]{3, 5});
        vars[3] = s.intVar("v_3", new int[]{1, 3, 5, 6});


        List<Constraint> lcstrs = new ArrayList<>(10);
        List<Constraint> lcstrs1 = new ArrayList<>(1);
        List<Constraint> lcstrs2 = new ArrayList<>(10);

        lcstrs1.add(IntConstraintFactory.alldifferent(vars, "BC"));
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                lcstrs2.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                lcstrs2.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        lcstrs.addAll(lcstrs1);
        lcstrs.addAll(lcstrs2);

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));
        //        ChocoLogging.toSolution();
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 1, "nb sol incorrect");

    }


    @Test(groups="1s", timeOut=60000)
    public void test4() {

        Solver s = new Solver();

        int n = 5;
        IntVar[] vars = new IntVar[n];
        vars[0] = s.intVar("v_0", 5);
        vars[1] = s.intVar("v_1", 3);
        vars[2] = s.intVar("v_2", 3, 4, true);
        vars[3] = s.intVar("v_3", 2, 6, true);
        vars[4] = s.intVar("v_4", 2, 6, true);


        List<Constraint> lcstrs = new ArrayList<>(10);

        lcstrs.add(IntConstraintFactory.alldifferent(vars, "BC"));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2, "nb sol incorrect");

    }


    @Test(groups="10s", timeOut=60000)
    public void test6() {
        Random rand;
        for (int seed = 0; seed < 10; seed++) {
            rand = new Random(seed);
            for (double d = 0.25; d <= 1.0; d += 0.25) {
                for (int h = 0; h <= 1; h++) {
                    for (int b = 0; b <= 1; b++) {
                        int n = 1 + rand.nextInt(5);
                        int[][] domains = DomainBuilder.buildFullDomains(n, 1, 2 * n, rand, d, h == 0);

                        Solver neqs = alldiffs(domains, 0, b == 0);
                        neqs.findAllSolutions();

                        Solver clique = alldiffs(domains, 1, b == 0);
                        clique.findAllSolutions();
                        Assert.assertEquals(clique.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect " + seed);
                        Assert.assertEquals(clique.getMeasures().getNodeCount(), neqs.getMeasures().getNodeCount(), "nb nod incorrect" + seed);

                        Solver bc = alldiffs(domains, 2, b == 0);
                        bc.findAllSolutions();
                        Assert.assertEquals(bc.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect " + seed);
                        Assert.assertTrue(bc.getMeasures().getNodeCount() <= neqs.getMeasures().getNodeCount(), "nb nod incorrect" + seed);

                        Solver ac = alldiffs(domains, 3, b == 0);
                        ac.findAllSolutions();
                        Assert.assertEquals(ac.getMeasures().getSolutionCount(), neqs.getMeasures().getSolutionCount(), "nb sol incorrect " + seed);
                        Assert.assertTrue(ac.getMeasures().getNodeCount() <= neqs.getMeasures().getNodeCount(), "nb nod incorrect" + seed);
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
                vars[i] = s.intVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
            }
        } else {
            for (int i = 0; i < domains.length; i++) {
                vars[i] = s.intVar("v_" + i, domains[i]);
            }
        }

        List<Constraint> lcstrs = new ArrayList<>(10);

        switch (c) {
            case 0:
                for (int i = 0; i < vars.length - 1; i++) {
                    for (int j = i + 1; j < vars.length; j++) {
                        lcstrs.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j]));
                    }
                }
                break;
            case 1:
                lcstrs.add(ICF.alldifferent(vars, "NEQS"));
                break;
            case 2:
                lcstrs.add(ICF.alldifferent(vars, "BC"));
                break;
            case 3:
                lcstrs.add(ICF.alldifferent(vars, "AC"));
                break;
        }

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));
        return s;
    }

    @Test(groups="1s", timeOut=60000)
    public void testXX() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[4];
        ts[0] = solver.intVar("t0", new int[]{2, 3, 4});
        ts[1] = solver.intVar("t1", new int[]{-3, -2, -1, 1, 2});
        ts[2] = solver.intVar("t2", new int[]{-3, -2, -1, 1, 2, 3});
        ts[3] = solver.intVar("t3", new int[]{-3, -2, -1, 1, 2, 3});

        try {
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
        } catch (ContradictionException ignored) {

        }
//        System.out.printf("%s\n", solver.toString());


    }

    @Test(groups="1s", timeOut=60000)
    public void testXXX() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = solver.intVar("t0", 2, 2, false);
        ts[1] = solver.intVar("t1", 1, 3, false);
        ts[2] = solver.intVar("t2", 1, 3, false);

        solver.post(ICF.alldifferent(ts, "BC"));

        solver.propagate();
        Assert.assertEquals(ts[1].getDomainSize(), 2);
        Assert.assertEquals(ts[2].getDomainSize(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = solver.intVar("t0", 2, 4, true);
        ts[1] = solver.intVar("t1", 1, 3, true);
        ts[2] = solver.intVar("t2", 1, 3, true);

        solver.post(ICF.alldifferent(ts, "FC"));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 10);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 23);
    }

    @Test(groups="1s", timeOut=60000)
    public void testE() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = solver.intVar("t0", 2, 4, false);
        ts[1] = solver.intVar("t1", 1, 3, false);
        ts[2] = solver.intVar("t2", 1, 3, false);

        solver.post(ICF.alldifferent(ts, "FC"));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 10);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 19);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[32];
        X[0] = solver.intVar("V0", new int[]{-1, 19, 24, 25});
        X[1] = solver.intVar("V1", new int[]{-13, 1, 12});
        X[2] = solver.intVar("V2", new int[]{-9, -8, 9, 19});
        X[3] = solver.intVar("V3", new int[]{6});
        X[4] = solver.intVar("V4", new int[]{-3, 4, 11, 12});
        X[5] = solver.intVar("V5", new int[]{-6, 25});
        X[6] = solver.intVar("V6", new int[]{7, 12, 21});
        X[7] = solver.intVar("V7", new int[]{4, 7, 11, 12});
        X[8] = solver.intVar("V8", new int[]{-8, -4, 0, 21});
        X[9] = solver.intVar("V9", new int[]{-3, 12});
        X[10] = solver.intVar("X10", new int[]{0});
        X[11] = solver.intVar("X11", new int[]{-15, -3, 3});
        X[12] = solver.intVar("X12", new int[]{-5, 3, 21, 24});
        X[13] = solver.intVar("X13", new int[]{3});
        X[14] = solver.intVar("X14", new int[]{-16, 13, 16});
        X[15] = solver.intVar("X15", new int[]{-14, -12, 0, 20});
        X[16] = solver.intVar("X16", new int[]{-9, 11});
        X[17] = solver.intVar("X17", new int[]{-15, 13});
        X[18] = solver.intVar("X18", new int[]{-12, -4, 21});
        X[19] = solver.intVar("X19", new int[]{-1});
        X[20] = solver.intVar("X20", new int[]{2, 11, 14});
        X[21] = solver.intVar("X21", new int[]{-9, 7, 21});
        X[22] = solver.intVar("X22", new int[]{-16, 10, 15});
        X[23] = solver.intVar("X23", new int[]{20, 24});
        X[24] = solver.intVar("X24", new int[]{23});
        X[25] = solver.intVar("X25", new int[]{-7, 5});
        X[26] = solver.intVar("X26", new int[]{-2, 1, 10, 12});
        X[27] = solver.intVar("X27", new int[]{-16, -6, 12, 15});
        X[28] = solver.intVar("X28", new int[]{-9});
        X[29] = solver.intVar("X29", new int[]{-6, -4});
        X[30] = solver.intVar("X30", new int[]{-15, -2, -1, 3});
        X[31] = solver.intVar("X31", new int[]{1, 10, 14});

        solver.post(ICF.alldifferent(X, "AC"));
        solver.propagate();
        Assert.assertEquals(X[14].getUB(), 16);
        Assert.assertEquals(X[14].getLB(), -16);
        Assert.assertEquals(X[14].getDomainSize(), 2);
    }
}
