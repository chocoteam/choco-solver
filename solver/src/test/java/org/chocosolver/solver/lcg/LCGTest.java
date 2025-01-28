/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.lcg;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.chocosolver.util.tools.ArrayUtils.flatten;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
//@Ignore
public class LCGTest {

    @DataProvider
    public static Object[][] conf() {
        return new Object[][]{
                // bounder or not, view or not
                {false, false},
                {true, false},
                {false, true},
                {true, true}
        };
    }

    @DataProvider
    public static Object[][] seed() {
        long current = 0;//System.currentTimeMillis();
        return IntStream.range(0, 20)
                .mapToObj(i -> new Object[]{i > 0 ? i + current : i})
                .toArray(Object[][]::new);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////  NQUEEN  //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @DataProvider
    public static Object[][] nqueen() {
        Object[][] params = {
                {2, 0}, {3, 0}, {4, 2}, {5, 10},
                {6, 4}, {7, 40}, {8, 92},
                {9, 352}};
        return Providers.merge(conf(), params, seed());
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "nqueen", timeOut = 60000)
    public void testNQueenOneSolution(boolean bounded, boolean view, int n, int s, long seed) {
        Model model = new Model("LCG Queens sat",
                Settings.init().setLCG(true));
        IntVar[] vars = model.intVarArray("Q", n, 1, n, bounded);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(vars[i], "!=", vars[j]).post();
                if (view) {
                    model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
                    model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                } else {
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], j - i)).post();
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], -j + i)).post();
                }
            }
        }

        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(vars));
        } else {
            solver.setSearch(Search.randomSearch(vars, seed));
        }
        if (s > 0) {
            Assert.assertTrue(solver.solve());
        } else {
            Assert.assertFalse(solver.solve());
        }
    }

    @Test(groups = {"10s", "lcg"}, dataProvider = "nqueen", timeOut = 60000)
    public void testNQueenAllSolutions(boolean bounded, boolean view, int n, int s, long seed) {
        Model model = new Model("LCG Queens enum",
                Settings.init().setLCG(true));
        IntVar[] vars = model.intVarArray("Q", n, 1, n, bounded);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(vars[i], "!=", vars[j]).post();
                if (view) {
                    model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
                    model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                } else {
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], j - i)).post();
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], -j + i)).post();
                }
            }
        }
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(vars));
        } else {
            solver.setSearch(Search.randomSearch(vars, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), s, "seed: " + seed);
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "nqueen", timeOut = 60000)
    public void testNQueenOptim(boolean bounded, boolean view, int n, int s, long seed) {
        Model model = new Model("LCG Queens opt",
                Settings.init()
                        .setLCG(true)
        );
        IntVar[] vars = model.intVarArray("Q", n, 1, n + 1, bounded); // <= one more column

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(vars[i], "!=", vars[j]).post();
                if (view) {
                    model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
                    model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                } else {
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], j - i)).post();
                    model.arithm(vars[i], "!=", model.intView(1, vars[j], -j + i)).post();
                }
            }
        }
        model.setObjective(Model.MAXIMIZE, vars[0]);
        Solver solver = model.getSolver();
//        solver.showDecisions(0);
//        solver.showSolutions(vars);
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(vars));
        } else {
            solver.setSearch(Search.randomSearch(vars, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), n + 1);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////  GOLOMB  //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @DataProvider
    public static Object[][] alldiff() {
        return new Object[][]{
                {"FC"}, //can be ignored, since it already evaluated by other tests
                {"BC"},
                {"AC"},
                {"DEFAULT"},
        };
    }

    @DataProvider
    public static Object[][] golomb() {
        Object[][] sizes = {
                {2, 1}, {3, 3}, {4, 6}, {5, 11}, {6, 17}, {7, 25}
        };
        return Providers.merge(conf(), sizes, alldiff(), seed());
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "golomb", timeOut = 60000)
    public void testGolombRuler(boolean bounded, boolean view, int m, int o, String a, long seed) {
        Model model = new Model("LCG Golomb ruler",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[] ticks = model.intVarArray("a", m, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, bounded);
        model.arithm(ticks[0], "=", 0).post();
        for (int i = 0; i < m - 1; i++) {
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
        }
        IntVar[] diffs = model.intVarArray("d", (m * m - m) / 2, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, bounded);
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                model.arithm(ticks[j], "-", ticks[i], "=", diffs[k]).post();
                model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
                int t = (m - 1 - j + i) * (m - j + i);
                if (view) {
                    model.arithm(diffs[k], "<=", model.intView(1, ticks[m - 1], -t / 2)).post();
                } else {
                    model.arithm(diffs[k], "<=", ticks[m - 1], "-", t / 2).post();
                }
            }
        }
        model.allDifferent(diffs, a).post();
        // break symetries
        if (m > 2) {
            model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
        }
        model.setObjective(Model.MINIMIZE, ticks[m - 1]);
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(ticks));
        } else {
            solver.setSearch(Search.randomSearch(ticks, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), o);
    }

    @DataProvider
    public static Object[][] golomb2() {
        Object[][] sizes = {
                {2, 1}, {3, 3}, {4, 6}, {5, 11}, {6, 17}, {7, 25}, {8, 34}, //{9, 44}, {10, 55}, {11, 72}, {12, 85}, {13, 106},
        };
        return Providers.merge(conf(), sizes, alldiff(), seed());
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "golomb2", timeOut = 60000)
    public void testGolombRuler2(boolean bounded, boolean view, int m, int o, String a, long seed) {
        int[] length = new int[]{0, 0, 1, 3, 6, 11, 17, 25, 34, 44, 55, 72, 85, 106, 127};
        Model model = new Model("LCG Golomb ruler",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[] ticks = new IntVar[m];
        ticks[0] = model.intVar("a0", 0);
        for (int i = 1; i < m; i++) {
            ticks[i] = model.intVar("a" + i, length[i + 1], m * m, bounded);
        }
        IntVar[] diffs = new IntVar[((m - 1) * (m - 1) - (m - 1)) / 2];

        List<IntVar> d = new ArrayList<>();
        for (int j = 1; j < m; j++) {
            if (view) {
                d.add(model.intView(1, ticks[j], -1));
            } else {
                d.add(ticks[j]);
            }
            model.arithm(ticks[j], ">=", length[j + 1]).post();
        }
        for (int i = 1, k = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                diffs[k] = model.intVar("d[" + i + "," + j + "]", length[j - i + 1], m * m, bounded);
                //model.arithm(ticks[j], "-", ticks[i], "=", diffs[k]).post();
                model.sum(new IntVar[]{ticks[i], diffs[k]}, "=", ticks[j]).post();
                if (view) {
                    d.add(model.intView(1, diffs[k], -1));
                } else {
                    d.add(diffs[k]);
                }
            }
        }
        model.allDifferent(d.toArray(new IntVar[0]), a).post();

        model.setObjective(Model.MINIMIZE, ticks[m - 1]);
        Solver solver = model.getSolver();

        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(ticks));
        } else {
            solver.setSearch(Search.randomSearch(ticks, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), o);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////  MILP  ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @DataProvider
    public static Object[][] milp() {
        return Providers.merge(conf(), seed());
    }

    private Constraint scalarWithViews(IntVar[] vars, int[] coeffs, String op, int cste, boolean view) {
        if (view) {
            IntVar[] vvars = new IntVar[vars.length];
            for (int i = 0; i < vars.length; i++) {
                vvars[i] = vars[i].getModel().intView(coeffs[i], vars[i], 0);
            }
            return vars[0].getModel().sum(vvars, op, cste);
        } else {
            return vars[0].getModel().scalar(vars, coeffs, op, cste);
        }
    }

    @Test(groups = {"10s", "lcg"}, dataProvider = "milp", timeOut = 60000)
    public void testMILP(boolean bounded, boolean view, long seed) {
        Model model = new Model("LCG MILP",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        // Variables
        IntVar[] x = model.intVarArray("x", 8, 0, 100, bounded);

        // Objective function
        IntVar objective = model.intVar("Z", -1000, 1000);
        model.scalar(
                new IntVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{3, 2, -5, 4, 2, -1, 7, -6},
                "=",
                objective
        ).post();
        // Constraints
        model.sum(x, "<=", 20).post();
        scalarWithViews(
                new IntVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{2, -1, 3, 4, -1, 2, 3, -2},
                ">=",
                15,
                view
        ).post();
        scalarWithViews(
                new IntVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{1, 2, 3, -1, 1, 1, 4, 2},//, -1, 3},
                "<=",
                10,
                view
        ).post();


        // Solve the problem
        model.setObjective(Model.MAXIMIZE, objective);
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(x));
        } else {
            solver.setSearch(Search.randomSearch(x, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), 98);

    }

    @Test(groups = {"10s", "lcg"}, dataProvider = "milp", timeOut = 60000)
    public void testMILPBin(boolean bounded, boolean view, long seed) {
        Model model = new Model("LCG MILP bin",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        // Variables
        BoolVar[] x = model.boolVarArray("x", 8);

        // Objective function
        IntVar objective = model.intVar("Z", -1000, 1000);
        model.scalar(
                new BoolVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{3, 2, -5, 4, 2, -1, 7, -6},
                "=",
                objective
        ).post();
        // Constraints
        model.sum(x, "<=", 20).post();
        scalarWithViews(
                new BoolVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{2, -1, 3, 4, -1, 2, 3, -2},
                ">=",
                3,
                view
        ).post();
        scalarWithViews(
                new BoolVar[]{x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7]},
                new int[]{1, 2, 3, -1, 1, 1, 4, 2},
                "<=",
                4,
                view
        ).post();


        // Solve the problem
        model.setObjective(Model.MAXIMIZE, objective);
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(x));
        } else {
            solver.setSearch(Search.randomSearch(x, seed));
        }
        //solver.showDecisions(()->"");
        //solver.showSolutions(x);
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestSolutionValue(), 14);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////  SCHUR LEMMA //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @DataProvider
    public static Object[][] schurlemma() {
        Object[][] params = {
                {12, 3, 114},
                {13, 3, 18},
                {14, 3, 0}
        };
        return Providers.merge(conf(), params, seed());
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "schurlemma", timeOut = 60000)
    public void testSchurLemma(boolean bounded, boolean clause, int balls, int boxes, int nbsol, long seed) {
        Model model = new Model("LCG Schur Lemma",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar[][] M = model.boolVarMatrix("b", balls, boxes); // M_ij is true iff ball i is in box j

        for (int i = 0; i < balls; i++) {
            if (clause) {
                model.addClausesBoolOrArrayEqualTrue(M[i]);
                model.addClausesAtMostOne(M[i]);
            } else {
                if (bounded) {
                    model.sum(M[i], ">=", 1).post();
                    model.sum(M[i], "<=", 1).post();
                } else {
                    model.sum(M[i], "=", 1).post();
                }
            }
        }

        for (int i = 0; i < boxes; i++) {
            for (int x = 1; x <= balls; x++) {
                for (int y = 1; y <= balls; y++) {
                    for (int z = 1; z <= balls; z++) {
                        if (x + y == z) {
                            if (clause) {
                                model.addClausesAtMostNMinusOne(new BoolVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]});
                            } else {
                                if (bounded) {
                                    model.sum(new BoolVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, ">=", 0).post();
                                    model.sum(new BoolVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, "<=", 2).post();
                                } else {
                                    IntVar target = model.intVar("sum", 0, 2, bounded);
                                    model.sum(new BoolVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, "=", target).post();
                                }
                            }
                        }
                    }
                }
            }
        }
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(flatten(M)));
        } else {
            solver.setSearch(Search.randomSearch(flatten(M), seed));
        }
        //solver.showDecisions(()->"");
        //solver.showSolutions(flatten(M));
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), nbsol);
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "schurlemma", timeOut = 60000)
    public void testSchurLemmaInt(boolean bounded, boolean clause, int balls, int boxes, int nbsol, long seed) {
        Model model = new Model("LCG Schur Lemma",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[][] M = new IntVar[balls][boxes];
        for (int i = 0; i < balls; i++) {
            for (int j = 0; j < boxes; j++) {
                M[i][j] = model.intView(1, model.intVar("b_" + i + "_" + j, 1, 2), -1); // M_ij is true iff ball i is in box j
            }
        }

        for (int i = 0; i < balls; i++) {
            model.sum(M[i], ">=", 1).post();
            model.sum(M[i], "<=", 1).post();
        }

        for (int i = 0; i < boxes; i++) {
            for (int x = 1; x <= balls; x++) {
                for (int y = 1; y <= balls; y++) {
                    for (int z = 1; z <= balls; z++) {
                        if (x + y == z) {
                            if (bounded) {
                                IntVar target = model.intVar("sum", 0, 2, bounded);
                                model.sum(new IntVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, "<=", target).post();
                                model.sum(new IntVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, ">=", target).post();
                            } else {
                                model.sum(new IntVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, ">=", 0).post();
                                model.sum(new IntVar[]{M[x - 1][i], M[y - 1][i], M[z - 1][i]}, "<=", 2).post();
                            }
                        }
                    }
                }
            }
        }
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(flatten(M)));
        } else {
            solver.setSearch(Search.randomSearch(flatten(M), seed));
        }
        //solver.showDecisions(()->"");
        //solver.showSolutions(flatten(M));
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), nbsol);
    }


    @Test(groups = {"1s", "lcg"}, timeOut = 60000)
    public void testBoolVars() {
        Model model = new Model("LCG Bin Neq 1",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[] bs = model.boolVarArray("b", 3);
        model.arithm(bs[0], "!=", bs[1]).post();
        model.arithm(bs[0], "!=", bs[2]).post();
        model.arithm(bs[1], "!=", bs[2]).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(bs));
        Assert.assertFalse(solver.solve());
    }

    @Test(groups = {"1s", "lcg"}, timeOut = 60000)
    public void testBoolVars2() {
        Model model = new Model("LCG Bin Neq 2",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[] bs = model.boolVarArray("b", 3);
        model.arithm(bs[0], "!=", bs[1]).post();
        model.arithm(bs[0], "=", bs[2]).post();
        model.arithm(bs[1], "=", bs[2]).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(bs));
        Assert.assertFalse(solver.solve());
    }

    @Test(groups = {"1s", "lcg"}, timeOut = 60000)
    public void testBoolVars3() {
        Model model = new Model("LCG MILP",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar[] bs = model.boolVarArray("b", 3);
        model.addClausesBoolAndEqVar(bs[0], bs[1], bs[2].not());
        model.addClausesBoolAndEqVar(bs[0], bs[1], bs[2]);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(bs));
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////  REIFICATION //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups = {"1s", "lcg"}, dataProvider = "seed", timeOut = 60000)
    public void testEq(long seed) {
        Model model = new Model("LCG Reification",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar[] vs = model.intVarArray("v", 2, 0, 3);
        BoolVar b = model.boolVar("b");
        IntVar[] vars = ArrayUtils.append(vs, new IntVar[]{b});
        model.arithm(vs[0], "=", vs[1]).reifyWith(b);
        model.arithm(vs[0], "!=", vs[1]).post();
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(vars));
        } else {
            solver.setSearch(Search.randomSearch(vars, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), 12);
    }

    @Test(groups = {"1s", "lcg"}, timeOut = 60000, dataProvider = "seed")
    public void testReif(long seed) {
        Model model = new Model("LCG BACP",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        int n = 100;
        IntVar cp = model.intVar("cp", 1, n, false);
        BoolVar[] bv = model.boolVarArray("b1", n);
        IntVar[] vars = ArrayUtils.append(bv, new IntVar[]{cp});
        for (int i = 1; i <= n; i++) {
            model.ifThenElse(bv[i - 1],
                    model.arithm(cp, "=", i),
                    model.arithm(cp, "!=", i));
//            model.reifyXeqC(cp, i, bv[i - 1]);
        }

        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(vars));
        } else {
            solver.setSearch(Search.randomSearch(vars, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), n);

    }

    @DataProvider
    public static Object[][] view() {
        return Providers.merge(Providers.trueOrFalse(), seed());
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "view")
    public void testIsEq(boolean view, long seed) {
        Model model = new Model("LCG isEq",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        int t = 2;
        IntVar x = model.intVar("x", 1, 4);
        BoolVar b1;
        if (view) {
            b1 = model.isEq(x, t);
        } else {
            b1 = model.boolVar();
            model.reifyXeqC(x, t, b1);
        }
        model.addClauseTrue(b1);
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(x, b1));
        } else {
            solver.setSearch(Search.randomSearch(new IntVar[]{x, b1}, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), 1);
    }

    @Test(groups = {"1s", "lcg"}, dataProvider = "view")
    public void testIsGeq(boolean view, long seed) {
        Model model = new Model("LCG isGeq",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        int t = 2;
        IntVar x = model.intVar("x", 0, 4);
        BoolVar b1;
        if (view) {
            b1 = model.isGeq(x, t);
        } else {
            b1 = model.boolVar();
            model.reifyXgtC(x, t - 1, b1);
        }
        model.addClauseTrue(b1);
        Solver solver = model.getSolver();
        if (seed == 0) {
            solver.setSearch(Search.inputOrderLBSearch(x, b1));
        } else {
            solver.setSearch(Search.randomSearch(new IntVar[]{x, b1}, seed));
        }
        while (solver.solve()) ;
        Assert.assertEquals(solver.getSolutionCount(), 3);
    }
}
