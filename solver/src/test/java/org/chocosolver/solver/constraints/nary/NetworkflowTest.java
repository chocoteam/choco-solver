/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/01/2022
 */
public class NetworkflowTest {

    @Test(groups = "1s")
    public void testNFC1() {
        Model model = new Model();
        int[] starts = {5, 5, 5, 5, 1, 1, 2, 2, 3, 3, 4, 4, 1, 2, 3, 4};
        int[] ends = {1, 2, 3, 4, 2, 3, 1, 4, 1, 4, 2, 3, 6, 6, 6, 6};
        int[] balance = {0, 0, 0, 0, 450, -450};
        int[] weight = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0};
        IntVar[] flow = new IntVar[16];
        for (int i = 0; i < 4; i++) {
            flow[i] = model.intVar(new int[]{0, 450});
        }
        for (int i = 4; i < 12; i++) {
            flow[i] = model.intVar(0, 350);
        }
        for (int i = 12; i < 16; i++) {
            flow[i] = model.intVar(new int[]{0, 450});
        }
        IntVar cost = model.intVar(0, 10000);
        model.costFlow(starts, ends, balance, weight, flow, cost, 1);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        solver.showShortStatistics();
        solver.findOptimalSolution(cost, false);
        Assert.assertEquals(solver.getBestSolutionValue(), 0);
        Assert.assertEquals(solver.getSolutionCount(), 1);
        Assert.assertEquals(solver.getNodeCount(), 9);
    }

    @Test(groups = "1s")
    public void testNFC2() {
        Model model = new Model();
        int[] starts = {5, 5, 5, 5, 1, 1, 2, 2, 3, 3, 4, 4, 1, 2, 3, 4};
        int[] ends = {1, 2, 3, 4, 2, 3, 1, 4, 1, 4, 2, 3, 6, 6, 6, 6};
        int[] balance = {0, 0, 0, 0, 450, -450};
        int[] weight = {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0};
        IntVar[] flow = new IntVar[16];
        flow[0] = model.intVar(450);
        flow[1] = model.intVar(0);
        flow[2] = model.intVar(0);
        flow[3] = model.intVar(0);
        flow[4] = model.intVar(145, 305);
        flow[5] = model.intVar(145, 305);
        flow[6] = model.intVar(0, 160);
        flow[7] = model.intVar(0, 305);
        flow[8] = model.intVar(0, 160);
        flow[9] = model.intVar(0, 305);
        flow[10] = model.intVar(0, 305);
        flow[11] = model.intVar(0, 305);
        flow[12] = model.intVar(0);
        flow[13] = model.intVar(0, 450);
        flow[14] = model.intVar(0, 450);
        flow[15] = model.intVar(0, 450);
        IntVar cost = model.intVar(0, 10000);
        model.costFlow(starts, ends, balance, weight, flow, cost, 1);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        solver.showShortStatistics();
        solver.findOptimalSolution(cost, false);
        Assert.assertEquals(solver.getBestSolutionValue(), 450);
        Assert.assertEquals(solver.getSolutionCount(), 1);
        Assert.assertEquals(solver.getNodeCount(), 4);
    }


    @Test(groups = "1s")
    public void testNFC3() {
        Model model = new Model();
        int[] starts = {0, 0, 1, 1, 2};
        int[] ends = {1, 2, 2, 3, 3};
        int[] balance = {3, 2, -3, -2};
        int[] weight = {1, 1, 1, 1, 1};
        IntVar[] flow = new IntVar[5];
        int i = -1;
        flow[++i] = model.intVar("c" + i, 1, 3);
        flow[++i] = model.intVar("c" + i, 2, 4);
        flow[++i] = model.intVar("c" + i, 0, 3);
        flow[++i] = model.intVar("c" + i, 0, 1);
        flow[++i] = model.intVar("c" + i, 2, 10);
        IntVar cost = model.intVar(0, 100);
        model.costFlow(starts, ends, balance, weight, flow, cost, 0);
        model.setObjective(false, cost);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        solver.showShortStatistics();
        while (solver.solve()) {
            //System.out.printf("%s%n", Arrays.toString(Stream.of(flow).map(IntVar::getValue).toArray()));
        }
        Assert.assertEquals(solver.getSolutionCount(), 1);
        Assert.assertEquals(solver.getNodeCount(), 1);
    }

    @Test(groups = "1s")
    public void testNFC4() {
        Model model = new Model();
        int[] starts = {0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7};
        int[] ends = {1, 2, 3, 4, 5, 6, 5, 6, 5, 6, 6, 7, 8, 8, 8, 8, 8};
        int[] balance = {4, 0, 0, 0, 0, 0, 0, 0, -4};
        int[] weight = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0};
        IntVar[] flow = new IntVar[17];
        for (int i = 0; i < 14; i++) {
            flow[i] = model.intVar("c" + i, 0, 1);
        }
        flow[14] = model.intVar("c14", 0, 2);
        flow[15] = model.intVar("c15", 0, 3);
        flow[16] = model.intVar("c16", 0, 1);
        IntVar cost = model.intVar(0, 10);
        model.costFlow(starts, ends, balance, weight, flow, cost, 0);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        solver.showShortStatistics();
        //solver.findOptimalSolution(cost, false);
        while (solver.solve()) {
            //System.out.printf("%d / %d -- ", cost.getValue(), Arrays.stream(flow).mapToInt(IntVar::getValue).sum());
            //System.out.printf("%s%n", Arrays.toString(Stream.of(flow).map(IntVar::getValue).toArray()));
        }
        Assert.assertEquals(solver.getSolutionCount(), 56);
        Assert.assertEquals(solver.getNodeCount(), 111);
    }

    @Test(groups = "1s")
    public void testMCMFORtools1() {
        int[] startNodes = new int[]{0, 0, 0, 1, 1, 2, 2, 3, 3};
        int[] endNodes = new int[]{1, 2, 3, 2, 4, 3, 4, 2, 4};
        int[] capacities = new int[]{20, 30, 10, 40, 30, 10, 20, 5, 20};

        // Define an array of supplies at each node.
        int[] supplies = new int[]{60, 0, 0, 0, -60};

        Model model = new Model();
        IntVar[] flow = new IntVar[startNodes.length];
        for (int i = 0; i < startNodes.length; i++) {
            flow[i] = model.intVar(String.format("f(%d,%d)", startNodes[i], endNodes[i]), 0, capacities[i]);
        }
        model.costFlow(startNodes, endNodes, supplies, IntStream.range(0, startNodes.length).map(i -> 0).toArray(),
                flow, model.intVar(0), 0);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        if (solver.solve()) {
            /*System.out.println("Maximum Flow: " + maxFlow.getValue());
            System.out.println();
            System.out.println(" Edge   Flow / Capacity");
            for (int i = 0; i < startNodes.length; ++i) {
                System.out.println(startNodes[i] + " -> " + endNodes[i] + "  "
                        + flow[i].getValue() + "  / " + capacities[i]);
            }
            System.out.println();
            System.out.println();*/
        }
        Assert.assertEquals(solver.getSolutionCount(), 1);
        Assert.assertEquals(solver.getNodeCount(), 1);
    }

    @Test(groups = "1s")
    public void testMCMFORtools2() {
        int[] startNodes = new int[]{0, 0, 1, 1, 1, 2, 2, 3, 4};
        int[] endNodes = new int[]{1, 2, 2, 3, 4, 3, 4, 4, 2};
        int[] capacities = new int[]{15, 8, 20, 4, 10, 15, 4, 20, 5};
        int[] unitCosts = new int[]{4, 4, 2, 2, 6, 1, 3, 2, 3};

        // Define an array of supplies at each node.
        int[] supplies = new int[]{20, 0, 0, -5, -15};

        Model model = new Model();
        IntVar[] flow = new IntVar[startNodes.length];
        for (int i = 0; i < startNodes.length; i++) {
            flow[i] = model.intVar(String.format("f(%d,%d)", startNodes[i], endNodes[i]), 0, capacities[i]);
        }
        IntVar cost = model.intVar(0, 1000);
        model.costFlow(startNodes, endNodes, supplies, unitCosts, flow, cost, 0);
        model.setObjective(false, cost);
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(flow));
        while (solver.solve()) {
            /*System.out.println("Minimum cost: " + cost.getValue());
            System.out.println();
            System.out.println(" Edge   Flow / Capacity  Cost");
            for (int i = 0; i < startNodes.length; ++i) {
                long acost = (long) flow[i].getValue() * unitCosts[i];
                System.out.println(startNodes[i] + " -> " + endNodes[i] + "  "
                        + flow[i].getValue() + "  / " + capacities[i] + "       " + acost);
            }
            System.out.println();
            System.out.println();*/
        }
        Assert.assertEquals(solver.getBestSolutionValue(), 150);
        Assert.assertEquals(solver.getSolutionCount(), 11);
        Assert.assertEquals(solver.getNodeCount(), 33);
    }
}
