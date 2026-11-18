/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.Solution;
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
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/03/2023
 */
public class MILPTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testFeasible01() {
        // 29.3-7
        double[] c = {-1, -1, -1};
        double[][] A = {
                {-2, -7.5, -3},
                {-20, -5, -10}
        };
        double[] b = {-10000, -30000};
        BitSet integers = new BitSet();
        integers.set(0, 3);
        MILP lp = new MILP(A, b, c, integers, new BitSet());
        Assert.assertEquals(lp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.objective(), -2250, 1e-8);
        Assert.assertEquals(lp.value(0), 1250, 1e-8);
        Assert.assertEquals(lp.value(1), 1000, 1e-8);
        Assert.assertEquals(lp.value(2), 0, 1e-8);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testFeasible2() {
        // 29.5-5
        double[] c = {1, 3};
        double[][] A = {
                {1, -1},
                {-1, -1},
                {-1, 4}
        };
        double[] b = {8, -3, 2};
        BitSet integers = new BitSet();
        integers.set(0, 2);
        MILP lp = new MILP(A, b, c, integers, new BitSet());
        Assert.assertEquals(lp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(lp.value(0), 11., 1e-8);
        Assert.assertEquals(lp.value(1), 3., 1e-8);
        Assert.assertEquals(lp.objective(), 20., 1e-8);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testFeasible3() {
        // https://en.wikipedia.org/wiki/Branch_and_bound
        MILP milp = new MILP();
        milp.makeIntegers(2);
        milp.addLeq(new double[]{1, 1}, 50);
        milp.addLeq(new double[]{4, 7}, 280);
        //milp.addLeq(new double[]{1, 0}, 23);
        milp.setObjective(true, new double[]{5., 6.});
        Assert.assertEquals(milp.branchAndBound(), LinearProgram.Status.FEASIBLE);
        Assert.assertEquals(milp.value(0), 24., 1e-8);
        Assert.assertEquals(milp.value(1), 26., 1e-8);
        Assert.assertEquals(milp.objective(), 276., 1e-8);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDiffn1() {
        MILP milp = new MILP(false);
        // 7 taches
        milp.makeVariables(7);  // starting time
        milp.makeBooleans(21); // ti avant tj ou non
        int[] d = new int[]{60, 274, 6, 30, 45, 30, 30};
        // fixed tasks
        milp.addEq(0, 1., 33);
        milp.addEq(1, 1., 116);
        milp.addEq(2, 1., 494);

        // free tasks
        milp.addLeq(3, 1., 464);
        milp.addLeq(4, 1., 484);
        milp.addLeq(5, 1., 499);
        milp.addLeq(6, 1., 499);
        // diffn
        double M = 9999.;
        for (int i = 0, k = 7; i < 7; i++) {
            int _i = i;
            for (int j = i + 1; j < 7; j++, k++) {
                int _j = j;
                int _k = k;
                // ti + di <= tj + M.(1-y)
                //noinspection Convert2Diamond
                milp.addLeq(new HashMap<Integer, Double>() {{
                    put(_i, 1.);
                    put(_j, -1.);
                    put(_k, M);
                }}, -d[i] + M);
                // tj + dj <= ti + M.y
                //noinspection Convert2Diamond
                milp.addLeq(new HashMap<Integer, Double>() {{
                    put(_j, 1.);
                    put(_i, -1.);
                    put(_k, -M);
                }}, -d[j]);
            }
        }
        double[] c = new double[28];
        for (int i = 7; i < 28; i++) {
            c[i] = 1.;
        }
        milp.setObjective(true, c);
        LinearProgram.Status status = milp.branchAndBound();
//        if (status.equals(LinearProgram.Status.FEASIBLE)) {
//            for (int i = 0; i < 7; i++) {
//                System.out.printf("task %d starts at %.2f, for %d\n", i, milp.value(i), d[i]);
//            }
//            for (int k = 7; k < 28; k++) {
//                System.out.printf("b_%d %.4f\n", k, milp.value(k));
//            }
//            System.out.printf("Cost=: %.4f", milp.objective());
//        } else {
//            System.out.printf("%s\n", status);
//        }
        Assert.assertEquals(status, LinearProgram.Status.INFEASIBLE);
    }

    @DataProvider
    public Object[][] triangle() {
        return new Object[][]{
                {3, 4, 10},
                {4, 6, 20},
                {5, 8, 25},
                {6, 10, 50},
                {7, 12, 400},
        };
    }

    @DataProvider
    public Object[][] algo() {
        return new Object[][]{
                {ILinearRelaxation.Solver.OJALGO},
        };
    }

    @DataProvider
    public Object[][] allTriangular() {
        return Providers.merge(triangle(), algo());
    }

    @Test(groups = "1s", dataProvider = "allTriangular")//, timeOut = 10000)
    public void testTriangular(int n, int opt, int maxNodes, ILinearRelaxation.Solver linrel) {
        Model model = new Model("Triangular");
        BoolVar[][] heart = model.boolVarMatrix("heart", n, n);
        IntVar obj = model.intVar("obj", 1, n * n);

        List<IntVar> objFun = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
                // collect heart[i][j] for the objective
                objFun.add(heart[i][j]);
                for (int k = 0; k < n - i; k++) {
                    for (int m = 0; m <= k - 1; m++) {
                        model.sum(new IntVar[]{heart[i + m][j], heart[i + k][j + m], heart[i + k - m][j + k - m]}, "<=", 2).post();
                    }
                }
            }
            for (int j = i + 1; j < n; j++) {
                model.arithm(heart[i][j], "=", 0).post();
            }
        }
        model.sum(objFun.toArray(new IntVar[0]), "=", obj).post();
        new Constraint("Simplex", new PropSimplex(model, linrel)).post();

        Solver solver = model.getSolver();
//        solver.showDecisions(()-> ""+solver.getNodeCount());
        solver.limitNode(1_000_000L);
        solver.setSearch(Search.inputOrderUBSearch(ArrayUtils.flatten(heart)));
        solver.showShortStatistics();
        solver.findOptimalSolution(obj, true);
        Assert.assertEquals(solver.getBestSolutionValue(), opt);
        long actualNodes = solver.getMeasures().getNodeCount();
        Assert.assertTrue(actualNodes <= maxNodes,
                "node count " + actualNodes + " exceeds upper bound " + maxNodes);
    }

    @Test(dataProvider = "algo", groups = "1s", timeOut = 1000000)
    public void test111(ILinearRelaxation.Solver linrel){
        Model model = new Model(SettingsBuilder.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        IntVar z = model.intVar("z", 0, 5);
        model.scalar(new IntVar[]{x, y, z}, new int[]{3, 2, 1}, "<=", 10).post();
        model.scalar(new IntVar[]{x, y, z}, new int[]{2, 5, 3}, "<=", 15).post();
        IntVar obj = model.intVar("obj", 0, 100);
        model.scalar(new IntVar[]{x, y, z}, new int[]{2, 3, 4}, "=", obj).post();

        new Constraint("Simplex", new PropSimplex(model, linrel)).post();
        model.setObjective(Model.MAXIMIZE, obj);
        Solver solver = model.getSolver();
//        solver.showDecisions(0);
        solver.setSearch(Search.inputOrderUBSearch(x,y, z));
        solver.showShortStatistics();
        Solution sol = solver.findOptimalSolution(obj, true);
        Assert.assertEquals(sol.getIntVal(obj),20);
    }

}