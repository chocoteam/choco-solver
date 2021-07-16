/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.objective.ParetoOptimizer;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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

    @Test(groups="1s", timeOut=60000)
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
        s.getSolver().setSearch(Search.domOverWDegSearch(occurrences), Search.inputOrderLBSearch(totalProfit_1,totalProfit_2));
        // --- solve
        ParetoOptimizer pareto = new ParetoOptimizer(Model.MAXIMIZE,new IntVar[]{totalProfit_1,totalProfit_2});
        s.getSolver().plugMonitor(pareto);
        while(s.getSolver().solve());
        System.out.println("Pareto Front:");
        for(Solution sol:pareto.getParetoFront()){
            System.out.println(sol.getIntVal(totalProfit_1)+" // "+sol.getIntVal(totalProfit_2));
        }
    }
}
