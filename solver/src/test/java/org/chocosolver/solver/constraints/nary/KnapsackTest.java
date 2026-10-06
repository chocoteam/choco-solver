/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.knapsack.PropKnapsack;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.Largest;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.MathUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.ceil;

/**
 * @author Jean-Guillaume FAGES (cosling)
 * @since 05/04/2017.
 */
public class KnapsackTest {
    int N = 50;
    int M = 5;
    int[] c = new int[]{560, 1125, 300, 620, 2100, 431, 68, 328, 47, 122, 322, 196, 41, 25, 425, 4260, 416, 115, 82, 22, 631, 132, 420, 86, 42, 103, 215, 81, 91, 26, 49, 420, 316, 72, 71, 49, 108, 116, 90, 738, 1811, 430, 3060, 215, 58, 296, 620, 418, 47, 81};
    int[] b = new int[]{800, 650, 550, 550, 650};
    int[][] a = new int[][]{
            {40, 91, 10, 30, 160, 20, 3, 12, 3, 18, 9, 25, 1, 1, 10, 280, 10, 8, 1, 1, 49, 8, 21, 6, 1, 5, 10, 8, 2, 1, 0, 10, 42, 6, 4, 8, 0, 10, 1, 40, 86, 11, 120, 8, 3, 32, 28, 13, 2, 4},
            {16, 92, 41, 16, 150, 23, 4, 18, 6, 0, 12, 8, 2, 1, 0, 200, 20, 6, 2, 1, 70, 9, 22, 4, 1, 5, 10, 6, 4, 0, 4, 12, 8, 4, 3, 0, 10, 0, 6, 28, 93, 9, 30, 22, 0, 36, 45, 13, 2, 2},
            {38, 39, 32, 71, 80, 26, 5, 40, 8, 12, 30, 15, 0, 1, 23, 100, 0, 20, 3, 0, 40, 6, 8, 0, 6, 4, 22, 4, 6, 1, 5, 14, 8, 2, 8, 0, 20, 0, 0, 6, 12, 6, 80, 13, 6, 22, 14, 0, 1, 2},
            {8, 71, 30, 60, 200, 18, 6, 30, 4, 8, 31, 6, 3, 0, 18, 60, 21, 4, 0, 2, 32, 15, 31, 2, 2, 7, 8, 2, 8, 0, 2, 8, 6, 7, 1, 0, 0, 20, 8, 14, 20, 2, 40, 6, 1, 14, 20, 12, 0, 1},
            {38, 52, 30, 42, 170, 9, 7, 20, 0, 3, 21, 4, 1, 2, 14, 310, 8, 4, 6, 1, 18, 15, 38, 10, 4, 8, 6, 0, 0, 3, 0, 10, 6, 1, 3, 0, 3, 5, 4, 0, 30, 12, 16, 18, 3, 16, 22, 30, 4, 0}
    };

    @Test(groups = "10s", timeOut = 60000)
    public void knapsackTest() {
        Model m = new Model();
        BoolVar[] x = m.boolVarArray(N);
        IntVar[] bVar = new IntVar[M];
        for (int i = 0; i < M; i++) {
            bVar[i] = m.intVar(0, b[i]);
        }
        IntVar objective = m.intVar(0, N * MathUtils.max(c));
        m.setObjective(Model.MAXIMIZE, objective);
        m.scalar(x, c, "=", objective).post();
        for (int i = 0; i < M; i++) {
            m.knapsack(x, bVar[i], objective, a[i], c).post();
        }
        Solver s = m.getSolver();
        IntVar[] xCost = new IntVar[N];
        for (int i = 0; i < N; i++) xCost[i] = m.mul(x[i], c[i]);
        s.setSearch(Search.intVarSearch(new Largest(), new IntDomainMax(), xCost));
        while (s.solve()) ;
        s.printShortStatistics();
        Assert.assertEquals(16537, s.getBestSolutionValue());
    }

    @Test(groups = "10s", timeOut = 60000)
    public void knapsackTestBestValue() {
        Model m = new Model();
        BoolVar[] x = m.boolVarArray(N);
        IntVar[] bVar = new IntVar[M];
        for (int i = 0; i < M; i++) {
            bVar[i] = m.intVar(0, b[i]);
        }
        IntVar objective = m.intVar(0, N * MathUtils.max(c));
        m.setObjective(Model.MAXIMIZE, objective);
        m.scalar(x, c, "=", objective).post();
        for (int i = 0; i < M; i++) {
            m.knapsack(x, bVar[i], objective, a[i], c).post();
        }
        Solver s = m.getSolver();
        IntVar[] xCost = new IntVar[N];
        for (int i = 0; i < N; i++) xCost[i] = m.mul(x[i], c[i]);
        s.setSearch(Search.intVarSearch(new Largest(), new IntDomainBest(), xCost));
        while (s.solve()) ;
        s.printShortStatistics();
        Assert.assertEquals(16537, s.getBestSolutionValue());
    }

    @Test(groups = "10s", timeOut = 60000)
    public void knapsackTest3() {
        for (int seed = 0; seed < 200; seed++) {
            Model m = new Model();
            int[] es = {1, 8, 4, 7, 3};
            int[] ws = {1, 4, 3, 5, 2};
            IntVar capa = m.intVar("capa", 0, 15);
            IntVar power = m.intVar("power", 0, 999);
            IntVar[] occs = new IntVar[5];
            for (int i = 0; i < 5; i++) {
                occs[i] = m.intVar("o" + i, 0, (int) Math.ceil(15. / ws[i]));
            }
            m.knapsack(occs, capa, power, ws, es).post();
            m.setObjective(true, power);
            m.getSolver().setSearch(Search.randomSearch(occs, seed));
            int p = 0;
            while (m.getSolver().solve()) {
                p = power.getValue();
            }
            Assert.assertEquals(p, 28);
        }

    }

    @Test(groups = "10s", timeOut = 60000)
    public void knapsackTest4() {
        for (int seed = 0; seed < 200; seed++) {
            Model m = new Model();
            IntVar[] occs = m.boolVarArray("o", 35);
            int[] es = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 8, 8, 8, 4, 4, 4, 4, 4, 7, 7, 7, 3, 3, 3, 3, 3, 3, 3, 3};
            int[] ws = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 3, 3, 3, 3, 3, 5, 5, 5, 2, 2, 2, 2, 2, 2, 2, 2};
            IntVar capa = m.intVar("capa", 0, 15);
            IntVar power = m.intVar("power", 0, 999);
            m.knapsack(occs, capa, power, ws, es).post();
            m.setObjective(true, power);
            m.getSolver().setSearch(Search.randomSearch(occs, seed));
            int p = 0;
            while (m.getSolver().solve()) {
                p = power.getValue();
            }
            Assert.assertEquals(p, 28);
        }
    }

    @Test(groups = "10s", timeOut = 600000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "20", "1"})
    public void testIssue1231Example1(int seed) {
        // Example 1: a valid solution is removed
        // Two assignments are feasible: [0,1,0] -> weight=1, profit=2 and [1,1,0] -> weight=2, profit=3
        Model model = new Model();
        IntVar[] occurrences = model.intVarArray("item", 3, 0, 1);
        IntVar weight = model.intVar("weight", 0, 2);
        IntVar profit = model.intVar("profit", 0, 6);

        model.knapsack(
                occurrences,
                weight,
                profit,
                new int[]{1, 1, 3},
                new int[]{1, 2, 3}
        ).post();

        model.arithm(occurrences[1], "=", 1).post();
        model.arithm(occurrences[2], "=", 0).post();
        model.arithm(profit, ">=", 2).post();
        Solver solver = model.getSolver();

        try{
            solver.propagate();
            Assert.assertEquals(occurrences[0].getLB(), 0);
            Assert.assertEquals(occurrences[0].getUB(), 1);
            Assert.assertEquals(occurrences[1].getLB(), 1);
            Assert.assertEquals(occurrences[2].getUB(), 0);
        }catch (ContradictionException cex){
            Assert.fail();
        }

        solver.setSearch(new FullyRandom(model.retrieveIntVars(true), seed));
        List<int[]> solutions = new ArrayList<>();
        while (solver.solve()) {
            solutions.add(new int[]{occurrences[0].getValue(), occurrences[1].getValue(), occurrences[2].getValue()});
        }

        // Both solutions should be found
        Assert.assertEquals(solutions.size(), 2, "Expected 2 solutions, but found: " + solutions.size());

        // Check that both expected solutions are present
        boolean found010 = false;
        boolean found110 = false;
        for (int[] sol : solutions) {
            if (sol[0] == 0 && sol[1] == 1 && sol[2] == 0) {
                found010 = true;
            }
            if (sol[0] == 1 && sol[1] == 1 && sol[2] == 0) {
                found110 = true;
            }
        }
        Assert.assertTrue(found010, "Solution [0,1,0] not found");
        Assert.assertTrue(found110, "Solution [1,1,0] not found");
    }

    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "100", "1"})
    public void testIssue1231Example2(int seed) {
        // Example 2: wrong optimum with a profit lower bound
        // Using the same instance as Example 1, set profit as the objective
        Model model = new Model();
        IntVar[] occurrences = model.intVarArray("item", 3, 0, 1);
        IntVar weight = model.intVar("weight", 0, 2);
        IntVar profit = model.intVar("profit", 0, 6);

        model.knapsack(
                occurrences,
                weight,
                profit,
                new int[]{1, 1, 3},
                new int[]{1, 2, 3}
        ).post();

        model.arithm(profit, ">=", 2).post();

        model.setObjective(Model.MAXIMIZE, profit);
        Solver solver = model.getSolver();
        solver.setSearch(new FullyRandom(model.retrieveIntVars(true), seed));
        int best = Integer.MIN_VALUE;
        while (solver.solve()) {
            best = profit.getValue();
        }

        // The exact optimum is 3 with [1,1,0]
        Assert.assertEquals(best, 3, "Expected optimum 3, but got: " + best);
    }

    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "100", "1"})
    public void testIssue1231Example3(int seed) {
        // Example 3: satisfiable model reported UNSAT
        Model model = new Model();
        IntVar[] occurrences = model.intVarArray("item", 3, 0, 1);
        IntVar weight = model.intVar("weight", 0, 17);
        IntVar profit = model.intVar("profit", 0, 29);

        model.knapsack(
                occurrences,
                weight,
                profit,
                new int[]{16, 1, 25},
                new int[]{6, 3, 20}
        ).post();

        model.arithm(profit, ">=", 9).post();

        Solver solver = model.getSolver();
        solver.setSearch(new FullyRandom(model.retrieveIntVars(true), seed));
        boolean solutionFound = false;
        int[] foundSolution = null;

        while (solver.solve()) {
            solutionFound = true;
            foundSolution = new int[]{occurrences[0].getValue(), occurrences[1].getValue(), occurrences[2].getValue()};
        }

        Assert.assertTrue(solutionFound, "Expected to find solution [1,1,0] but no solution found");
        if (foundSolution != null) {
            Assert.assertEquals(foundSolution[0], 1, "Expected occurrences[0]=1");
            Assert.assertEquals(foundSolution[1], 1, "Expected occurrences[1]=1");
            Assert.assertEquals(foundSolution[2], 0, "Expected occurrences[2]=0");
        }
    }


    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "100", "1"})
    public void testIssue1231Example3ProfitThresholds(int seed) {
        // Additional test: different profit thresholds for Example 3 instance
        // Expected solution counts: 0->4, 1->3, 2->3, 3->3, 4->2, 5->2, 6->2, 7->1, 8->1, 9->1, 10->0
        int[] weights = {16, 1, 25};
        int[] profits = {6, 3, 20};
        int[] expectedCounts = {4, 3, 3, 3, 2, 2, 2, 1, 1, 1, 0};

        for (int minProfit = 0; minProfit <= 10; minProfit++) {
            Model model = new Model();
            IntVar[] occurrences = model.intVarArray("item", 3, 0, 1);
            IntVar weight = model.intVar("weight", 0, 17);
            IntVar profit = model.intVar("profit", 0, 29);

            model.knapsack(occurrences, weight, profit, weights, profits).post();
            model.arithm(profit, ">=", minProfit).post();

            Solver solver = model.getSolver();
            solver.setSearch(new FullyRandom(model.retrieveIntVars(true), seed));
            int solutionCount = 0;
            while (solver.solve()) {
                solutionCount++;
            }

            int expectedCount = expectedCounts[minProfit];
            Assert.assertEquals(solutionCount, expectedCount,
                    "For minProfit=" + minProfit + ", expected " + expectedCount + " solutions, but found " + solutionCount);
        }
    }

    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "20", "1"})
    public void testIssue1231Example4(int seed) {
        // Example 4: wrong optimum without an external profit bound
        Model model = new Model();
        IntVar[] occurrences = model.intVarArray("item", 5, 0, 1);
        IntVar weight = model.intVar("weight", 0, 14);
        IntVar profit = model.intVar("profit", 0, 25);

        model.knapsack(
                occurrences,
                weight,
                profit,
                new int[]{1, 6, 7, 9, 5},
                new int[]{1, 6, 8, 9, 1}
        ).post();

        model.setObjective(Model.MAXIMIZE, profit);
        model.getSolver().setSearch(Search.randomSearch(occurrences, seed));

        int best = Integer.MIN_VALUE;
        while (model.getSolver().solve()) {
            best = profit.getValue();
        }

        // The exact optimum is 15 with [1,1,1,0,0]
        Assert.assertEquals(best, 15, "Expected optimum 15, but got: " + best);
    }

    // ====================================================================================
    // COMPARISON TESTS: With vs Without PropKnapsackKatriel01
    // ====================================================================================

    /**
     * Creates a knapsack constraint manually with only PropKnapsack (without PropKnapsackKatriel01)
     */
    private Constraint createKnapsackWithoutKatriel(Model model, IntVar[] occurrences, IntVar weightSum, IntVar energySum,
                                                    int[] weights, int[] energies) {
        Constraint scalar1 = model.scalar(occurrences, weights, "=", weightSum);
        scalar1.ignore();
        Constraint scalar2 = model.scalar(occurrences, energies, "=", energySum);
        scalar2.ignore();

        return new Constraint(
                "KNAPSACK",
                ArrayUtils.append(
                        scalar1.getPropagators(),
                        scalar2.getPropagators(),
                        new Propagator[]{new PropKnapsack(occurrences, weightSum, energySum, weights, energies)}
                )
        );
    }

    /**
     * Generates a random knapsack instance
     */
    private void generateKnapsackInstance(Random random, int n, int[] weights, int[] energies, int maxWeight, int maxEnergy) {
        for (int i = 0; i < n; i++) {
            weights[i] = random.nextInt(maxWeight) + 1;  // Weight between 1 and maxWeight
            energies[i] = random.nextInt(maxEnergy) + 1;  // Profit between 1 and maxEnergy
        }
    }

    @DataProvider
    public Object[][] instances() {
        final int N_ITEMS = 10;
        final int MAX_WEIGHT = 20;
        final int MAX_ENERGY = 50;
        final int MAX_CAPACITY = 50;

        int nbInstances = 20;
        int seeds = 20;
        Object[][] results = new Object[(nbInstances + 1) * seeds][];
        int k = 0;
        for (int j = 0; j < seeds; j++) {
            results[k++] = new Object[]{3, 2, 6, 2, new int[]{1, 1, 3}, new int[]{1, 2, 3}, j};
        }

        for (int i = 1; i < nbInstances + 1; i++) {
            Random random = new Random(i);
            int[] weights = new int[N_ITEMS];
            int[] energies = new int[N_ITEMS];
            generateKnapsackInstance(random, N_ITEMS, weights, energies, MAX_WEIGHT, MAX_ENERGY);
            for (int j = 0; j < seeds; j++) {
                results[k++] = new Object[]{N_ITEMS, MAX_ENERGY / 2, Arrays.stream(energies).sum(), MAX_CAPACITY / 3, weights, energies, j};
            }
        }
        return results;
    }


    @Test(groups = "10s", timeOut = 60000, dataProvider = "instances")
    public void testKnapsackComparisonWithSeed(int N_ITEMS, int MIN_ENERGY, int MAX_ENERGY, int MAX_WEIGHT, int[] weights, int[] energies, int seed) {
        // Test comparing solutions with and without PropKnapsackKatriel01 using a specific seed

        // ========================================================================
        // Model WITH PropKnapsackKatriel01
        // ========================================================================
        Model modelWithKatriel = new Model();
        IntVar[] occWithKatriel = modelWithKatriel.intVarArray("occ", N_ITEMS, 0, 1);
        IntVar weightWithKatriel = modelWithKatriel.intVar("weight", 0, MAX_WEIGHT);
        IntVar profitWithKatriel = modelWithKatriel.intVar("profit", 0, N_ITEMS * MAX_ENERGY);

        modelWithKatriel.knapsack(occWithKatriel, weightWithKatriel, profitWithKatriel, weights, energies).post();
        modelWithKatriel.arithm(profitWithKatriel, ">=", MIN_ENERGY).post();
        modelWithKatriel.setObjective(Model.MAXIMIZE, profitWithKatriel);

        // ========================================================================
        // Model WITHOUT PropKnapsackKatriel01 (only PropKnapsack)
        // ========================================================================
        Model modelWithoutKatriel = new Model();
        IntVar[] occWithoutKatriel = modelWithoutKatriel.intVarArray("occ", N_ITEMS, 0, 1);
        IntVar weightWithoutKatriel = modelWithoutKatriel.intVar("weight", 0, MAX_WEIGHT);
        IntVar profitWithoutKatriel = modelWithoutKatriel.intVar("profit", 0, N_ITEMS * MAX_ENERGY);

        createKnapsackWithoutKatriel(modelWithoutKatriel, occWithoutKatriel, weightWithoutKatriel,
                profitWithoutKatriel, weights, energies).post();
        modelWithoutKatriel.arithm(profitWithoutKatriel, ">=", MIN_ENERGY).post();
        modelWithoutKatriel.setObjective(Model.MAXIMIZE, profitWithoutKatriel);

        // ========================================================================
        // Solving and comparison
        // ========================================================================
        Solver solverWithKatriel = modelWithKatriel.getSolver();
        Solver solverWithoutKatriel = modelWithoutKatriel.getSolver();

        // Use the same search strategy for both
        solverWithKatriel.setSearch(new FullyRandom(modelWithKatriel.retrieveIntVars(true), seed));
        solverWithoutKatriel.setSearch(new FullyRandom(modelWithoutKatriel.retrieveIntVars(true), seed));

        List<int[]> solutionsWithKatriel = new ArrayList<>();
        int bestProfitWithKatriel = Integer.MIN_VALUE;
        while (solverWithKatriel.solve()) {
            int[] sol = new int[occWithKatriel.length];
            for (int i = 0; i < occWithKatriel.length; i++) {
                sol[i] = occWithKatriel[i].getValue();
            }
            solutionsWithKatriel.add(sol);
            bestProfitWithKatriel = Math.max(bestProfitWithKatriel, profitWithKatriel.getValue());
        }

        List<int[]> solutionsWithoutKatriel = new ArrayList<>();
        int bestProfitWithoutKatriel = Integer.MIN_VALUE;
        while (solverWithoutKatriel.solve()) {
            int[] sol = new int[occWithoutKatriel.length];
            for (int i = 0; i < occWithoutKatriel.length; i++) {
                sol[i] = occWithoutKatriel[i].getValue();
            }
            solutionsWithoutKatriel.add(sol);
            bestProfitWithoutKatriel = Math.max(bestProfitWithoutKatriel, profitWithoutKatriel.getValue());
        }

        // ========================================================================
        // Assertions
        // ========================================================================
        String msg = String.format("Seed=%d, Items=%d, Weights=%s, Energies=%s\n",
                seed, N_ITEMS, java.util.Arrays.toString(weights), java.util.Arrays.toString(energies));

        // 1. Best profit must be identical
        Assert.assertEquals(bestProfitWithKatriel, bestProfitWithoutKatriel,
                msg + "Best profit differs: with Katriel=" + bestProfitWithKatriel +
                        ", without Katriel=" + bestProfitWithoutKatriel);

        // 2. Each solution found with Katriel must be valid (and vice versa)
        for (int[] sol : solutionsWithKatriel) {
            int totalWeight = 0;
            int totalProfit = 0;
            for (int i = 0; i < N_ITEMS; i++) {
                totalWeight += sol[i] * weights[i];
                totalProfit += sol[i] * energies[i];
            }
            Assert.assertTrue(totalWeight <= MAX_WEIGHT,
                    msg + "Invalid solution with Katriel: weight=" + totalWeight + " > " + MAX_WEIGHT);
        }

        for (int[] sol : solutionsWithoutKatriel) {
            int totalWeight = 0;
            int totalProfit = 0;
            for (int i = 0; i < N_ITEMS; i++) {
                totalWeight += sol[i] * weights[i];
                totalProfit += sol[i] * energies[i];
            }
            Assert.assertTrue(totalWeight <= MAX_WEIGHT,
                    msg + "Invalid solution without Katriel: weight=" + totalWeight + " > " + MAX_WEIGHT);
        }
    }

    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"10", "100", "1"})
    public void testKnapsackComparisonWithProfitConstraint(int seed) {
        // Test comparing solutions with and without PropKnapsackKatriel01 using profit constraint
        final int N_ITEMS = 8;
        final int MAX_WEIGHT = 20;
        final int MAX_ENERGY = 40;
        final int MAX_CAPACITY = 50;

        Random random = new Random(seed);
        int[] weights = new int[N_ITEMS];
        int[] energies = new int[N_ITEMS];
        generateKnapsackInstance(random, N_ITEMS, weights, energies, MAX_WEIGHT, MAX_ENERGY);

        // Compute maximum possible profit
        int maxPossibleProfit = 0;
        for (int e : energies) maxPossibleProfit += e;
        // Minimum profit constraint: between 30% and 70% of max profit
        int minProfit = (int) (0.3 * maxPossibleProfit);

        // Model WITH Katriel
        Model modelWith = new Model();
        IntVar[] occWith = modelWith.intVarArray("occ", N_ITEMS, 0, 1);
        IntVar weightWith = modelWith.intVar("weight", 0, MAX_CAPACITY);
        IntVar profitWith = modelWith.intVar("profit", 0, maxPossibleProfit);
        modelWith.knapsack(occWith, weightWith, profitWith, weights, energies).post();
        modelWith.arithm(weightWith, "<=", MAX_CAPACITY).post();
        modelWith.arithm(profitWith, ">=", minProfit).post();

        // Model WITHOUT Katriel
        Model modelWithout = new Model();
        IntVar[] occWithout = modelWithout.intVarArray("occ", N_ITEMS, 0, 1);
        IntVar weightWithout = modelWithout.intVar("weight", 0, MAX_CAPACITY);
        IntVar profitWithout = modelWithout.intVar("profit", 0, maxPossibleProfit);
        createKnapsackWithoutKatriel(modelWithout, occWithout, weightWithout, profitWithout, weights, energies).post();
        modelWithout.arithm(weightWithout, "<=", MAX_CAPACITY).post();
        modelWithout.arithm(profitWithout, ">=", minProfit).post();

        Solver solverWith = modelWith.getSolver();
        Solver solverWithout = modelWithout.getSolver();

        List<int[]> solutionsWith = new ArrayList<>();
        int bestWith = Integer.MIN_VALUE;
        while (solverWith.solve()) {
            int[] sol = new int[occWith.length];
            for (int i = 0; i < occWith.length; i++) sol[i] = occWith[i].getValue();
            solutionsWith.add(sol);
            bestWith = Math.max(bestWith, profitWith.getValue());
        }

        List<int[]> solutionsWithout = new ArrayList<>();
        int bestWithout = Integer.MIN_VALUE;
        while (solverWithout.solve()) {
            int[] sol = new int[occWithout.length];
            for (int i = 0; i < occWithout.length; i++) sol[i] = occWithout[i].getValue();
            solutionsWithout.add(sol);
            bestWithout = Math.max(bestWithout, profitWithout.getValue());
        }

        String errorMsg = String.format("Seed=%d, MinProfit=%d, Weights=%s, Energies=%s\n",
                seed, minProfit, java.util.Arrays.toString(weights), java.util.Arrays.toString(energies));

        Assert.assertEquals(solutionsWith.size(), solutionsWithout.size(),
                errorMsg + "Solution count mismatch: with=" + solutionsWith.size() + ", without=" + solutionsWithout.size());
        Assert.assertEquals(bestWith, bestWithout,
                errorMsg + "Best profit mismatch: with=" + bestWith + ", without=" + bestWithout);
    }

    @Test(groups = "10s", timeOut = 60000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"50"})
    public void testKnapsackComparisonMultipleSeeds(int dummy) {
        // Test with multiple seeds to cover more cases
        final int N_SEEDS = 20;
        final int N_ITEMS = 8;
        final int MAX_WEIGHT = 15;
        final int MAX_ENERGY = 30;
        final int MAX_CAPACITY = 40;

        for (int seed = 0; seed < N_SEEDS; seed++) {
            Random random = new Random(seed * 12345 + dummy);  // Varier les seeds
            int[] weights = new int[N_ITEMS];
            int[] energies = new int[N_ITEMS];
            generateKnapsackInstance(random, N_ITEMS, weights, energies, MAX_WEIGHT, MAX_ENERGY);

            // Modèle avec Katriel
            Model modelWith = new Model();
            IntVar[] occWith = modelWith.intVarArray("occ", N_ITEMS, 0, 1);
            IntVar weightWith = modelWith.intVar("weight", 0, MAX_CAPACITY);
            IntVar profitWith = modelWith.intVar("profit", 0, N_ITEMS * MAX_ENERGY);
            modelWith.knapsack(occWith, weightWith, profitWith, weights, energies).post();
            modelWith.arithm(weightWith, "<=", MAX_CAPACITY).post();

            // Modèle sans Katriel
            Model modelWithout = new Model();
            IntVar[] occWithout = modelWithout.intVarArray("occ", N_ITEMS, 0, 1);
            IntVar weightWithout = modelWithout.intVar("weight", 0, MAX_CAPACITY);
            IntVar profitWithout = modelWithout.intVar("profit", 0, N_ITEMS * MAX_ENERGY);
            createKnapsackWithoutKatriel(modelWithout, occWithout, weightWithout, profitWithout, weights, energies).post();
            modelWithout.arithm(weightWithout, "<=", MAX_CAPACITY).post();

            Solver solverWith = modelWith.getSolver();
            Solver solverWithout = modelWithout.getSolver();

            List<int[]> solutionsWith = new ArrayList<>();
            int bestWith = Integer.MIN_VALUE;
            while (solverWith.solve()) {
                int[] sol = new int[occWith.length];
                for (int i = 0; i < occWith.length; i++) {
                    sol[i] = occWith[i].getValue();
                }
                solutionsWith.add(sol);
                bestWith = Math.max(bestWith, profitWith.getValue());
            }

            List<int[]> solutionsWithout = new ArrayList<>();
            int bestWithout = Integer.MIN_VALUE;
            while (solverWithout.solve()) {
                int[] sol = new int[occWithout.length];
                for (int i = 0; i < occWithout.length; i++) {
                    sol[i] = occWithout[i].getValue();
                }
                solutionsWithout.add(sol);
                bestWithout = Math.max(bestWithout, profitWithout.getValue());
            }

            String errorMsg = String.format("Seed=%d, Weights=%s, Energies=%s\n",
                    seed, java.util.Arrays.toString(weights), java.util.Arrays.toString(energies));

            Assert.assertEquals(solutionsWith.size(), solutionsWithout.size(),
                    errorMsg + "Solution count mismatch: with=" + solutionsWith.size() + ", without=" + solutionsWithout.size());
            Assert.assertEquals(bestWith, bestWithout,
                    errorMsg + "Best profit mismatch: with=" + bestWith + ", without=" + bestWithout);
        }
    }

    @Test(groups = "10s", timeOut = 600000, dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"1", "20", "1"})
    public void testIssue1231Example5(int seed) {
        int[] capacities = {99, 1101};
        int[] volumes = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energies = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 49, 43, 36, 68, 92, 33, 84, 90};

        Model model = new Model();
        int nos = 20;
        // occurrence of each item
        IntVar[] objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = model.intVar("o_" + (i + 1), 0, (int) ceil(capacities[1]*1. / volumes[i]), true);
        }
        final IntVar profit = model.intVar("power", 0, 8415, true);
        IntVar capacity = model.intVar("weight", capacities[0], capacities[1], true);
        model.scalar(objects, volumes, "=", capacity).post();
        model.scalar(objects, energies, "=", profit).post();
        model.knapsack(objects, capacity, profit, volumes, energies).post();

        model.arithm(profit, ">=", 5293).post();
        // 19:0, 8:0, 1:88, 13:0, 16:3, 14:0, 4:0, 12:0, 18:0,
        model.arithm(objects[19], "=", 0).post();
        model.arithm(objects[8], "=", 0).post();
        model.arithm(objects[1], "=", 88).post();
        Solver solver = model.getSolver();

        try{
            solver.propagate();
            Assert.fail();
        }catch (ContradictionException cex){
        }

        solver.setSearch(new FullyRandom(model.retrieveIntVars(true), seed));
        solver.findAllSolutions();

        // Both solutions should be found
        Assert.assertEquals(solver.getSolutionCount(), 0,
                "Expected 0 solution, but found: " + solver.getSolutionCount());

    }
}
