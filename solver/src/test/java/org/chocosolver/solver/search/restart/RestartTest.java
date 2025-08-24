/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.*;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    protected static Model buildQ(int n) {
        Model model = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = model.intVar("Q_" + i, 1, n, false);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setSearch(inputOrderLBSearch(vars));
        return model;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGeometricalRestart1() {
        Model model = buildQ(4);
        model.getSolver().setGeometricalRestart(2, 1.1, new NodeCounter(model, 2), 2);
        while (model.getSolver().solve()) ;
        // not 2, because of restart, that found twice the same solution
        assertEquals(model.getSolver().getSolutionCount(), 2);
        assertEquals(model.getSolver().getRestartCount(), 2);
        assertEquals(model.getSolver().getNodeCount(), 12);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLubyRestart1() {
        Model model = buildQ(4);
        model.getSolver().setLubyRestart(2, new NodeCounter(model, 2), 2);
        while (model.getSolver().solve()) ;
        // not 2, because of restart, that found twice the same solution
        assertEquals(model.getSolver().getSolutionCount(), 2);
        assertEquals(model.getSolver().getRestartCount(), 2);
        assertEquals(model.getSolver().getNodeCount(), 11);
    }


    public final static int[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

    public final static int[] LUBY_3 = {1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9, 27};

    public final static int[] LUBY_4 = {1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16, 64
    };

    public final static int[] GEOMETRIC_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

    @Test(groups = "10s", timeOut = 60000)
    public void test1() {

        for (int j = 1; j < 5; j++) {
            int n = 200;
            Model model = new Model("Test");
            IntVar[] X = model.intVarArray("X", n, 1, n, false);
            IntVar[] Y = model.intVarArray("Y", n, n + 1, 2 * (n + 1), false);
            model.allDifferent(X).post();
            for (int i = 0; i < n; i++) {
                model.arithm(Y[i], "=", X[i], "+", n).post();
            }
            model.getSolver().setRestartOnSolutions();
            model.getSolver().setSearch(inputOrderLBSearch(X));
            model.getSolver().limitSolution(100);
            while (model.getSolver().solve()) ;
            //System.out.printf("%d - %.3fms \n", n, solver.getTimeCount());
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testGeometricalRestart2() {
        Model model = buildQ(8);
        model.getSolver().setGeometricalRestart(10, 1.2, new FailCounter(model, 10), 2);
        while (model.getSolver().solve()) ;
        // not 2, because of restart, that found twice the same solution
//        Assert.assertEquals(solver.getSolutionCount(), 92);
        assertEquals(model.getSolver().getRestartCount(), 2);
    }

    @Test(groups = "1s")
    public void testGolombRuler() {
        Model model = ProblemMaker.makeGolombRuler(10);
        IntVar[] ticks = (IntVar[]) model.getHook("ticks");
        Solver solver = model.getSolver();
        solver.setGeometricalRestart(10, 1.05, new FailCounter(model, 2), 2);
        solver.setNoGoodRecordingFromRestarts();
        solver.setSearch(
                Search.generatePartialAssignment(ticks, 5, false,
                        domOverWDegSearch(ticks)
                )
        );
        while (solver.solve()) ;
        assertEquals(solver.getRestartCount(), 2);
        assertEquals(solver.getSolutionCount(), 10);
    }

    @Test(groups = "lcg", dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"0", "20", "1"})
    public void testGolombRulerWithLCG(long seed) {
        Model model = ProblemMaker.makeGolombRuler(8, true);
        IntVar[] ticks = (IntVar[]) model.getHook("ticks");
        Solver solver = model.getSolver();
        solver.setGeometricalRestart(10, 1.05, new NodeCounter(model, 2), 2);
        solver.setSearch(randomSearch(ticks, seed));
        while (solver.solve()) ;
        assertEquals(solver.getBestSolutionValue(), 34);
    }

    @Test(groups = "1s")
    public void testGolombRulerWithReset() {
        Model model = ProblemMaker.makeGolombRuler(8);
        IntVar[] ticks = (IntVar[]) model.getHook("ticks");
        Solver solver = model.getSolver();
        solver.setGeometricalRestart(10, 1.05, new FailCounter(model, 2), 2);
        solver.setNoGoodRecordingFromRestarts();
        solver.setSearch(domOverWDegSearch(ticks));
        while (solver.solve()) ;
        assertEquals(solver.getRestartCount(), 2);
        assertEquals(solver.getSolutionCount(), 3);
        assertEquals(solver.getObjectiveManager().getBestSolutionValue(), 34);
        solver.hardReset();
        while (solver.solve()) ;
        assertEquals(solver.getRestartCount(), 0);
        assertEquals(solver.getSolutionCount(), 10);
        assertEquals(solver.getObjectiveManager().getBestSolutionValue(), 34);
    }

    @Test(groups = "1s")
    public void testClausesWithReset() {
        Model model = new Model("quinn.cnf");
        // p cnf 16 18
        BoolVar[] bs = model.boolVarArray("b", 16);
        //  1    2  0
        model.addClauses(new BoolVar[]{bs[0], bs[1]}, new BoolVar[0]);
        // -2   -4  0
        model.addClauses(new BoolVar[]{bs[1].not(), bs[3].not()}, new BoolVar[0]);
        //  3    4  0
        model.addClauses(new BoolVar[]{bs[2], bs[3]}, new BoolVar[0]);
        // -4   -5  0
        model.addClauses(new BoolVar[]{bs[3].not(), bs[4].not()}, new BoolVar[0]);
        //  5   -6  0
        model.addClauses(new BoolVar[]{bs[4], bs[5].not()}, new BoolVar[0]);
        //  6   -7  0
        model.addClauses(new BoolVar[]{bs[5], bs[6].not()}, new BoolVar[0]);
        //  6    7  0
        model.addClauses(new BoolVar[]{bs[5], bs[6]}, new BoolVar[0]);
        //  7  -16  0
        model.addClauses(new BoolVar[]{bs[6], bs[15].not()}, new BoolVar[0]);
        //  8   -9  0
        model.addClauses(new BoolVar[]{bs[7], bs[8].not()}, new BoolVar[0]);
        // -8  -14  0
        model.addClauses(new BoolVar[]{bs[7].not(), bs[13].not()}, new BoolVar[0]);
        //  9   10  0
        model.addClauses(new BoolVar[]{bs[8], bs[9]}, new BoolVar[0]);
        //  9  -10  0
        model.addClauses(new BoolVar[]{bs[8], bs[9].not()}, new BoolVar[0]);
        //-10  -11  0
        model.addClauses(new BoolVar[]{bs[9].not(), bs[10].not()}, new BoolVar[0]);
        // 10   12  0
        model.addClauses(new BoolVar[]{bs[9], bs[11]}, new BoolVar[0]);
        // 11   12  0
        model.addClauses(new BoolVar[]{bs[10], bs[11]}, new BoolVar[0]);
        // 13   14  0
        model.addClauses(new BoolVar[]{bs[12], bs[13]}, new BoolVar[0]);
        // 14  -15  0
        model.addClauses(new BoolVar[]{bs[13], bs[14].not()}, new BoolVar[0]);
        // 15   16  0
        model.addClauses(new BoolVar[]{bs[14], bs[15]}, new BoolVar[0]);
        Solver solver = model.getSolver();
        solver.setRestartOnSolutions();
        solver.setNoGoodRecordingFromRestarts();
        solver.setNoGoodRecordingFromSolutions(bs);
        solver.setSearch(inputOrderLBSearch(bs));
        while (solver.solve()) ;
        assertEquals(solver.getRestartCount(), 9);
        assertEquals(solver.getSolutionCount(), 9);

        solver.hardReset();
        while (solver.solve()) ;
        assertEquals(solver.getRestartCount(), 0);
        assertEquals(solver.getSolutionCount(), 9);

    }
}
