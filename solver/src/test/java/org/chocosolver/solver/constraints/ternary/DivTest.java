/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.ESat.TRUE;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class DivTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return vy != 0 && vz == vx / vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.div(vars[0], vars[1], vars[2]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model model = new Model();
        IntVar i = model.intVar("i", 0, 2, false);
        model.div(i, model.boolVar(true), model.boolVar(false)).getOpposite().post();
//        SMF.log(solver, true, false);
        while (model.getSolver().solve()) ;
    }

    @Test(groups="10s", timeOut=60000)
    public void testJL2() {
        for (int i = 0; i < 100000; i++) {
            final Model s = new Model();
            IntVar a = s.intVar("a", new int[]{0, 2, 3, 4});
            IntVar b = s.intVar("b", new int[]{-1, 1, 3, 4});
            IntVar c = s.intVar("c", new int[]{-3, 1, 4});
            s.div(a, b, c).post();
            Solver r = s.getSolver();
            r.setSearch(randomSearch(new IntVar[]{a, b, c}, i));
            //SMF.log(s, true, true);
            r.plugMonitor((IMonitorSolution) () -> {
                if (!TRUE.equals(r.isSatisfied())) {
                    throw new Error(s.toString());
                }
            });
            while (s.getSolver().solve()) ;
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testTP1(){
        final Model s = new Model();
        IntVar a = s.intVar("a", 0, 525000);
        IntVar b = s.intVar("b", 0, 5000);
        IntVar c = s.intVar("c", 0, 525000);
        s.div(a, b, c).post();
        Solver r = s.getSolver();
        r.solve();
        Assert.assertEquals(r.getSolutionCount(), 1);
    }

    @Test(groups = "1s", timeOut = 2000)
    public void testSlow() {
        Model model = new Model();

        // Compute lost potential
        int deadline = 50460;
        IntVar jobPackageStart = model.intVar(0, 879840);
        int cost = 72000; // if <10 000, very fast
        int period = 103680;

        model.arithm(jobPackageStart, "<=", deadline).post();

        ArExpression deviation = model.intVar(deadline).sub(jobPackageStart);
        deviation = deviation.div(60);
        period = (int) Math.round(period / 60.0);

        // Warning : PropDivXYZ very slow (initial propagation)
        IntVar potential = deviation.mul(cost).div(period).intVar(); // if we remove .mul(cost) or .div(period), very
        // fast

        Solver solver = model.getSolver();
        Solution sol = solver.findOptimalSolution(potential, false);
        //		very slow :
        //			Building time : 14,074s
        //			Resolution time : 32,093s
        //			Time to best solution : 32,084s
    }


    @Test
    public void testPropag() {
        Model m = new Model();
        IntVar duration = m.intVar("duration", -100, 214);
        duration.ge(0).post(); // doit être >= 0 mais pas évident via domaine initial
        IntVar dmul = duration.mul(4000).intVar(); // donc toujours >= 0

        IntVar div = dmul.div(60).intVar(); // on divise un truc >=0 par 60, ça devrait être >= 0 après propag initiale

        try {
            m.getSolver().propagate();
        } catch (ContradictionException e) {
            throw new RuntimeException(e);
        }
        System.out.println(duration);
        Assert.assertTrue(duration.getLB() >= 0);
        System.out.println("dmul " + dmul);
        Assert.assertTrue(dmul.getLB() >= 0);
        System.out.println("div " + div);
        Assert.assertTrue(div.getLB() >= 0);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "divSolve")
    public void testSolve(int[][] domains, int nbSolutions) {
        Model model = new Model();
        IntVar x = model.intVar("x", domains[0]);
        IntVar y = model.intVar("y", domains[1]);
        IntVar z = model.intVar("z", domains[2]);
        model.div(x, y, z).post();
        while (model.getSolver().solve()) {
            // Finding all solutions to the model
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), nbSolutions);
    }

    @DataProvider(name = "divSolve")
    private static Object[][] divSolve() {
        return new Object[][]{
            new Object[]{new int[][]{{-2, 1}, {-2, 0}, {0, 1}}, 2},
            new Object[]{new int[][]{{-2, 1}, {2}, {-1, 0}}, 2},
            new Object[]{new int[][]{{0, 1, 2}, {-1, 0, 1}, {-1, 0, 1}}, 4},
            new Object[]{new int[][]{{-1, 0, 1, 2}, {-2, -1, 0, 1}, {-2, 0, 1, 2}}, 9}
        };
    }
}
