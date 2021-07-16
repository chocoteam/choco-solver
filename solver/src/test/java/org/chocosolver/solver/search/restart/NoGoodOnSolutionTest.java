/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume Fages
 * @since 17/12/14
 * Created by IntelliJ IDEA.
 */
public class NoGoodOnSolutionTest {

    final static int N = 15;
    final static int Z = 175;
    final static int NB_SOLS = 5;
    final static int MAX_NB_SOLS = 10;

    public static Model makeProblem() {
        int n = N;
        Random rd = new Random(0);
        int[][] costs = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costs[i][j] = rd.nextInt(100);
            }
        }
        Model s = new Model();
        IntVar z = s.intVar("z", Z, Z + 10, true);
        IntVar[] vars = s.intVarArray("x", n, 0, n - 1, false);
        IntVar[] costOf = new IntVar[n];
        for (int i = 0; i < n; i++) {
            costOf[i] = s.intVar("costOf(" + i + ")", costs[i]);
        }
        for (int i = 0; i < n; i++) {
            s.element(costOf[i], costs[i], vars[i]).post();
        }
        s.sum(costOf, "=", z).post();
        s.circuit(vars).post();
        s.getSolver().setSearch(randomSearch(vars, 0));
        s.getSolver().limitSolution(MAX_NB_SOLS);
        return s;
    }

    @Test(groups="1s", timeOut=60000)
    public void testNormal() {
        // no restarts (ok)
        Model s = makeProblem();
        while (s.getSolver().solve()) ;

        assertTrue(s.getSolver().getSolutionCount() == NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRoS() {
        // restarts on solutions (infinite loop)
        Model s = makeProblem();
        s.getSolver().setRestartOnSolutions();
        while (s.getSolver().solve()) ;

        assertTrue(s.getSolver().getSolutionCount() == MAX_NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRoSNG() {
        // restarts on solutions with no goods on solutions (ok)
        Model s = makeProblem();
        s.getSolver().setNoGoodRecordingFromSolutions(s.retrieveIntVars(true));
        s.getSolver().setRestartOnSolutions();
        while (s.getSolver().solve()) ;

        assertTrue(s.getSolver().getSolutionCount() == NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testA() {
        // restarts on solutions and on fails (at activity presolve only) (loop infinitely)
        Model s = makeProblem();
        Solver r = s.getSolver();
        r.setSearch(activityBasedSearch(s.retrieveIntVars(true)));
        while (s.getSolver().solve()) ;

        assertTrue(r.getMeasures().getSolutionCount() == MAX_NB_SOLS);
    }

    @Test(groups="10s", timeOut=60000)
    public void testANG() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model s = makeProblem();
        Solver r = s.getSolver();
        r.setNoGoodRecordingFromSolutions(s.retrieveIntVars(true));
        r.setSearch(activityBasedSearch(s.retrieveIntVars(true)));
        while (s.getSolver().solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), NB_SOLS);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(model.retrieveIntVars(true));
        model.getSolver().setSearch(randomSearch(vars, 0));

        model.getSolver().setRestartOnSolutions();
        while (model.getSolver().solve()) ;

        assertTrue(model.getSolver().getSolutionCount() == 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ2() {
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(model.retrieveIntVars(false));
        model.getSolver().setNoGoodRecordingFromRestarts();
        model.getSolver().setSearch(randomSearch(vars, 0));
        model.getSolver().setRestartOnSolutions();
        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getSolutionCount(), 92);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ3() { //issue 327
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(vars[0]);

        model.getSolver().setSearch(inputOrderLBSearch(vars));
        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNQ4() { //issue 327
        // restarts on solutions and on fails with restarts on solutions (ok)
        Model model = new Model();
        int n = 8;
        IntVar[] vars = model.intVarArray("Q", n, 1, n, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = model.arithm(vars[i], "!=", vars[j]);
                neq.post();
                model.arithm(vars[i], "!=", vars[j], "+", -k).post();
                model.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        model.getSolver().setNoGoodRecordingFromSolutions(vars[0], vars[1]);

        model.getSolver().setSearch(inputOrderLBSearch(vars));

        while (model.getSolver().solve()) ;

        assertEquals(model.getSolver().getSolutionCount(), 36);
    }
}
