/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;

/**
 * Created by cprudhom on 18/02/15.
 * Project: choco.
 */
public class ParetoTest {

    int bestProfit1 = 0;

    //******************************************************************
    // MAIN
    //******************************************************************

    @Test(groups = "1s", timeOut = 60000)
    public void testPareto() {
        runKnapsackPareto(30, "10;1;2;5", "5;3;7;4", "2;5;11;3");
        Assert.assertTrue(bestProfit1 > 60);
    }

    private void runKnapsackPareto(final int capacity, final String... items) {
        int[] nbItems = new int[items.length];
        int[] weights = new int[items.length];
        int[] profits_1 = new int[items.length];
        int[] profits_2 = new int[items.length];

        int maxProfit_1 = 0;
        int maxProfit_2 = 0;
        for (int it = 0; it < items.length; it++) {
            String item = items[it];
            item = item.trim();
            final String[] itemData = item.split(";");
            nbItems[it] = parseInt(itemData[0]);
            weights[it] = parseInt(itemData[1]);
            profits_1[it] = parseInt(itemData[2]);
            profits_2[it] = parseInt(itemData[3]);
            maxProfit_1 += nbItems[it] * profits_1[it];
            maxProfit_2 += nbItems[it] * profits_2[it];
        }

        Model s = new Model("Knapsack");
        // --- Creates decision variables
        IntVar[] occurrences = new IntVar[nbItems.length];
        for (int i = 0; i < nbItems.length; i++) {
            occurrences[i] = s.intVar("occurrences_" + i, 0, nbItems[i], true);
        }
        IntVar totalWeight = s.intVar("totalWeight", 0, capacity, true);
        IntVar totalProfit_1 = s.intVar("totalProfit_1", 0, maxProfit_1, true);
        IntVar totalProfit_2 = s.intVar("totalProfit_2", 0, maxProfit_2, true);

        // --- Posts constraints
        s.knapsack(occurrences, totalWeight, totalProfit_1, weights, profits_1).post();
        s.knapsack(occurrences, totalWeight, totalProfit_2, weights, profits_2).post();
        // --- Monitor
        s.getSolver().plugMonitor((IMonitorSolution) () -> bestProfit1 = max(bestProfit1, totalProfit_1.getValue()));
        // --- Search
        s.getSolver().setSearch(Search.domOverWDegSearch(occurrences), Search.inputOrderLBSearch(totalProfit_1, totalProfit_2));
        // --- solve
        //ParetoMaximizer pareto = new ParetoMaximizer(Model.MAXIMIZE,new IntVar[]{totalProfit_1,totalProfit_2});
        //s.getSolver().plugMonitor(pareto);
        s.getSolver().showShortStatistics();
        List<Solution> front = s.getSolver().findParetoFront(new IntVar[]{totalProfit_1, totalProfit_2}, Model.MAXIMIZE);
        System.out.println("Pareto Front:");
        for (Solution sol : front) {
            System.out.println(sol.getIntVal(totalProfit_1) + " // " + sol.getIntVal(totalProfit_2));
        }
    }

    @Test(groups = "10s")
    public void testMOQAP() {
        runMOQAP();
    }

    private void runMOQAP() {
        Model m = new Model();
        int n = 10;
        int[][] w1 = {
                {0, 0, 132, 0, 2558, 667, 0, 572, 1, 200},
                {0, 0, 990, 140, 9445, 1397, 7, 0, 100, 0},
                {132, 990, 0, 7, 40, 2213, 0, 1, 0, 0},
                {0, 140, 7, 0, 58, 0, 1, 0, 4, 70},
                {2558, 9445, 40, 58, 0, 3, 0, 0, 0, 0},
                {667, 1397, 2213, 0, 3, 0, 139, 3, 5169, 101},
                {0, 7, 0, 1, 0, 139, 0, 0, 5659, 0},
                {572, 0, 1, 0, 0, 3, 0, 0, 3388, 1982},
                {1, 100, 0, 4, 0, 5169, 5659, 3388, 0, 1023},
                {200, 0, 0, 70, 0, 101, 0, 1982, 1023, 0}
        };
        int[][] w2 = {
                {0, 1, 0, 5379, 0, 0, 1, 0, 329, 856},
                {1, 0, 2029, 531, 15, 80, 197, 17, 274, 241},
                {0, 2029, 0, 1605, 0, 194, 0, 2, 4723, 0},
                {5379, 531, 1605, 0, 24, 68, 0, 0, 4847, 2205},
                {0, 15, 0, 24, 0, 1355, 5124, 1610, 0, 0},
                {0, 80, 194, 68, 1355, 0, 549, 0, 151, 2},
                {1, 197, 0, 0, 5124, 549, 0, 5955, 0, 0},
                {0, 17, 2, 0, 1610, 0, 5955, 0, 553, 710},
                {329, 274, 4723, 4847, 0, 151, 0, 553, 0, 758},
                {856, 241, 0, 2205, 0, 2, 0, 710, 758, 0},
        };
        int[][] d = new int[][]{
                {0, 3, 60, 62, 155, 29, 47, 78, 83, 102},
                {3, 0, 57, 61, 152, 27, 44, 74, 80, 99},
                {60, 57, 0, 58, 95, 47, 32, 19, 25, 48},
                {62, 61, 58, 0, 141, 33, 27, 61, 61, 65},
                {155, 152, 95, 141, 0, 142, 123, 82, 80, 80},
                {29, 27, 47, 33, 142, 0, 21, 60, 63, 78},
                {47, 44, 32, 27, 123, 21, 0, 41, 43, 57},
                {78, 74, 19, 61, 82, 60, 41, 0, 7, 31},
                {83, 80, 25, 61, 80, 63, 43, 7, 0, 23},
                {102, 99, 48, 65, 80, 78, 57, 31, 23, 0}
        };
        IntVar[] x = m.intVarArray("X", n, 1, n);
        // limitations for test purpose
        x[0].eq(1).post();
        IntVar[][] dist = m.intVarMatrix("dist", n, n, 0, 200);
        IntVar obj1 = m.intVar("O1", 0, 9_999_999);
        IntVar obj2 = m.intVar("O2", 0, 9_999_999);

        m.allDifferent(x).post();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m.element(dist[i][j], d, x[i], 1, x[j], 1);
            }
        }
        m.scalar(ArrayUtils.flatten(dist), ArrayUtils.flatten(w1), "=", obj1).post();
        m.scalar(ArrayUtils.flatten(dist), ArrayUtils.flatten(w2), "=", obj2).post();

        m.getSolver().setSearch(Search.inputOrderLBSearch(x));
        List<Solution> front = m.getSolver().findParetoFront(new IntVar[]{m.intMinusView(obj1), m.intMinusView(obj2)}, Model.MAXIMIZE);
        Assert.assertEquals(26, front.size());
        Assert.assertEquals(233, m.getSolver().getSolutionCount());
        Assert.assertEquals(95208, m.getSolver().getNodeCount());
    }
}
