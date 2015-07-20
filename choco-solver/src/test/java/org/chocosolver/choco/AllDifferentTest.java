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

import org.chocosolver.choco.checker.DomainBuilder;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;
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
            vars[i] = VariableFactory.enumerated("v_" + i, 1, n, s);
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
                diag1[i] = VariableFactory.enumerated("v_" + (i + 2 * n), -n, n, s);
                diag2[i] = VariableFactory.enumerated("v_" + (i + n), 1, 2 * n, s);
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


    @Test(groups = "1s")
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


    @Test(groups = "1s")
    public void test4() {

        Solver s = new Solver();

        int n = 5;
        IntVar[] vars = new IntVar[n];
        vars[0] = VariableFactory.fixed("v_0", 5, s);
        vars[1] = VariableFactory.fixed("v_1", 3, s);
        vars[2] = VariableFactory.bounded("v_2", 3, 4, s);
        vars[3] = VariableFactory.bounded("v_3", 2, 6, s);
        vars[4] = VariableFactory.bounded("v_4", 2, 6, s);


        List<Constraint> lcstrs = new ArrayList<>(10);

        lcstrs.add(IntConstraintFactory.alldifferent(vars, "BC"));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));
        s.findAllSolutions();
        long sol = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sol, 2, "nb sol incorrect");

    }


    @Test(groups = "verylong")
    public void test6() {
        Random rand;
        for (int seed = 0; seed < 4; seed++) {
            rand = new Random(seed);
            for (double d = 0.25; d <= 1.0; d += 0.25) {
                for (int h = 0; h <= 1; h++) {
                    for (int b = 0; b <= 1; b++) {
                        int n = 1 + rand.nextInt(3);
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
                        Assert.assertTrue(ac.getMeasures().getFailCount() == 0 || b == 0, "nb nod incorrect" + seed);

                        LoggerFactory.getLogger("test").info("{}ms - {}ms - {}ms - {}ms", neqs.getMeasures().getTimeCount(), clique.getMeasures().getTimeCount(),
                                bc.getMeasures().getTimeCount(), ac.getMeasures().getTimeCount());
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

    @Test(groups = "1s")
    public void testXX() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[4];
        ts[0] = VariableFactory.enumerated("t0", new int[]{2, 3, 4}, solver);
        ts[1] = VariableFactory.enumerated("t1", new int[]{-3, -2, -1, 1, 2}, solver);
        ts[2] = VariableFactory.enumerated("t2", new int[]{-3, -2, -1, 1, 2, 3}, solver);
        ts[3] = VariableFactory.enumerated("t3", new int[]{-3, -2, -1, 1, 2, 3}, solver);

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

    @Test(groups = "1s")
    public void testXXX() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = VariableFactory.enumerated("t0", 2, 2, solver);
        ts[1] = VariableFactory.enumerated("t1", 1, 3, solver);
        ts[2] = VariableFactory.enumerated("t2", 1, 3, solver);

        solver.post(ICF.alldifferent(ts, "BC"));

        solver.propagate();
        Assert.assertEquals(ts[1].getDomainSize(), 2);
        Assert.assertEquals(ts[2].getDomainSize(), 2);
    }

    @Test(groups = "1s")
    public void testB() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = VariableFactory.bounded("t0", 2, 4, solver);
        ts[1] = VariableFactory.bounded("t1", 1, 3, solver);
        ts[2] = VariableFactory.bounded("t2", 1, 3, solver);

        solver.post(ICF.alldifferent(ts, "FC"));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 10);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 19);
    }

    @Test(groups = "1s")
    public void testE() {
        Solver solver = new Solver();
        IntVar[] ts = new IntVar[3];
        ts[0] = VariableFactory.enumerated("t0", 2, 4, solver);
        ts[1] = VariableFactory.enumerated("t1", 1, 3, solver);
        ts[2] = VariableFactory.enumerated("t2", 1, 3, solver);

        solver.post(ICF.alldifferent(ts, "FC"));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 10);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 19);
    }

    @Test(groups = "1s")
    public void testB1() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[32];
        X[0] = VF.enumerated("V0", new int[]{-1, 19, 24, 25}, solver);
        X[1] = VF.enumerated("V1", new int[]{-13, 1, 12}, solver);
        X[2] = VF.enumerated("V2", new int[]{-9, -8, 9, 19}, solver);
        X[3] = VF.enumerated("V3", new int[]{6}, solver);
        X[4] = VF.enumerated("V4", new int[]{-3, 4, 11, 12}, solver);
        X[5] = VF.enumerated("V5", new int[]{-6, 25}, solver);
        X[6] = VF.enumerated("V6", new int[]{7, 12, 21}, solver);
        X[7] = VF.enumerated("V7", new int[]{4, 7, 11, 12}, solver);
        X[8] = VF.enumerated("V8", new int[]{-8, -4, 0, 21}, solver);
        X[9] = VF.enumerated("V9", new int[]{-3, 12}, solver);
        X[10] = VF.enumerated("X10", new int[]{0}, solver);
        X[11] = VF.enumerated("X11", new int[]{-15, -3, 3}, solver);
        X[12] = VF.enumerated("X12", new int[]{-5, 3, 21, 24}, solver);
        X[13] = VF.enumerated("X13", new int[]{3}, solver);
        X[14] = VF.enumerated("X14", new int[]{-16, 13, 16}, solver);
        X[15] = VF.enumerated("X15", new int[]{-14, -12, 0, 20}, solver);
        X[16] = VF.enumerated("X16", new int[]{-9, 11}, solver);
        X[17] = VF.enumerated("X17", new int[]{-15, 13}, solver);
        X[18] = VF.enumerated("X18", new int[]{-12, -4, 21}, solver);
        X[19] = VF.enumerated("X19", new int[]{-1}, solver);
        X[20] = VF.enumerated("X20", new int[]{2, 11, 14}, solver);
        X[21] = VF.enumerated("X21", new int[]{-9, 7, 21}, solver);
        X[22] = VF.enumerated("X22", new int[]{-16, 10, 15}, solver);
        X[23] = VF.enumerated("X23", new int[]{20, 24}, solver);
        X[24] = VF.enumerated("X24", new int[]{23}, solver);
        X[25] = VF.enumerated("X25", new int[]{-7, 5}, solver);
        X[26] = VF.enumerated("X26", new int[]{-2, 1, 10, 12}, solver);
        X[27] = VF.enumerated("X27", new int[]{-16, -6, 12, 15}, solver);
        X[28] = VF.enumerated("X28", new int[]{-9}, solver);
        X[29] = VF.enumerated("X29", new int[]{-6, -4}, solver);
        X[30] = VF.enumerated("X30", new int[]{-15, -2, -1, 3}, solver);
        X[31] = VF.enumerated("X31", new int[]{1, 10, 14}, solver);

        solver.post(ICF.alldifferent(X, "AC"));
        solver.propagate();
        Assert.assertEquals(X[14].getUB(), 16);
        Assert.assertEquals(X[14].getLB(), -16);
        Assert.assertEquals(X[14].getDomainSize(), 2);
    }
}
