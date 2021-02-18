/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import static java.lang.Math.ceil;
import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.solver.search.strategy.Search.lastConflict;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.lns.neighbors.PropagationGuidedNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.ReversePropagationGuidedNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.SequenceNeighborhood;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveLNS;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class LNSTest {

    private void knapsack20(final int lns) {
        int[] capacities = {99, 1101};
        int[] volumes = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energies = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 49, 43, 36, 68, 92, 33, 84, 90};

        Model model = new Model();
        int nos = 20;
        // occurrence of each item
        IntVar[] objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = model.intVar("o_" + (i + 1), 0, (int) ceil(capacities[1] / volumes[i]), true);
        }
        final IntVar power = model.intVar("power", 0, 99999, true);
        IntVar scalar = model.intVar("weight", capacities[0], capacities[1], true);
        model.scalar(objects, volumes, "=", scalar).post();
        model.scalar(objects, energies, "=", power).post();
        model.knapsack(objects, scalar, power, volumes, energies).post();

        Solver r = model.getSolver();
        r.setSearch(lastConflict(domOverWDegSearch(objects)));
        r.limitTime(900);
        switch (lns) {
            case 0:
                break;
            case 1:
                r.setLNS(new RandomNeighborhood(objects, 200, 123456L));
                break;
            case 2:
                r.setLNS(new PropagationGuidedNeighborhood(objects, 40, 10, 123456L));
                break;
            case 3:
                r.setLNS(new ReversePropagationGuidedNeighborhood(objects, 40, 10, 123456L));
                break;
            case 4:
                r.setLNS(new SequenceNeighborhood(
                        new PropagationGuidedNeighborhood(objects, 25, 10, 123456L),
                        new ReversePropagationGuidedNeighborhood(objects, 25, 10, 123456L)
                ));
                break;
            case 5:
                r.setLNS(new SequenceNeighborhood(
                        new PropagationGuidedNeighborhood(objects, 25, 10, 123456L),
                        new ReversePropagationGuidedNeighborhood(objects, 25, 10, 123456L),
                        new RandomNeighborhood(objects, 200, 123456L)
                ));
                break;
            case 6:
                r.setNoGoodRecordingFromRestarts();
                r.setLNS(new RandomNeighborhood(objects, 200, 123456L));
                break;
        }
        model.setObjective(Model.MAXIMIZE, power);
        int bw = 0, bp = 0;
        while (model.getSolver().solve()) {
            bp = power.getValue();
            bw = scalar.getValue();
        }
        Assert.assertEquals(bp, 8372);
        Assert.assertEquals(bw, 1092);
    }

    @DataProvider(name = "lns")
    public Object[][] createData() {
        return new Object[][]{{0}, {1}, {2}, {3}, {4}, {5}, {6}};
    }


    @Test(groups = "10s", timeOut = 60000, dataProvider = "lns")
    public void test1(int lns) {
        // opt: 8372
        knapsack20(lns);
    }

    @Test(groups="1s", timeOut=60000)
    public void testTOTO() {
        // First, the model: here a simple knapsack pb ...
        int[] capacities = {99, 1101};
        int[] volumes = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energies = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 49, 43, 36, 68, 92, 33, 84, 90};

        Model model = new Model();
        int nos = 20;
        // occurrence of each item
        IntVar[] objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = model.intVar("o_" + (i + 1), 0, (int) ceil(capacities[1] / volumes[i]), true);
        }
        final IntVar power = model.intVar("power", 0, 99999, true);
        IntVar scalar = model.intVar("weight", capacities[0], capacities[1], true);
        model.scalar(objects, volumes, "=", scalar).post();
        model.scalar(objects, energies, "=", power).post();
        model.knapsack(objects, scalar, power, volumes, energies).post();
        model.setObjective(Model.MAXIMIZE, power);
        // ... end of modelling

        Solver r = model.getSolver();
        // prepare solution recording, only based on 'objects'
        Solution sol = new Solution(model, objects);
        while (r.solve()) {
            // store the solution, erase the previous one
            // so the last one is the best one.
            sol.record();
        }
        r.printShortStatistics();
        // let's start the reparation of the previous solution
        // so, reset the search
        r.reset();
        try {
            // object #17 cannot be set to a value greater than 50 (91 in the previous solution)
            objects[16].updateUpperBound(50, Cause.Null);
        } catch (ContradictionException e) {
        }
        // declaring a neighborhood for LNS, here a random one
        RandomNeighborhood rnd = new RandomNeighborhood(objects, 1, 0);
        // initialize it with the previous (best) solution
        rnd.loadFromSolution(sol);
        // declare LNS
        r.setLNS(rnd);
        // limit search
        r.limitNode(3000);
        // find the new best solution
        int bw = 0, bp = 0;
        while (model.getSolver().solve()) {
            bp = power.getValue();
            bw = scalar.getValue();
        }
        r.printShortStatistics();
        Assert.assertEquals(bp, 6937);
        Assert.assertEquals(bw, 1092);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLoadSolution() {
        // First, the model: here a simple knapsack pb ...
        int[] capacities = {99, 1101};
        int[] volumes = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energies = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 49, 43, 36, 68, 92, 33, 84, 90};

        Model model = new Model();
        int nos = 20;
        // occurrence of each item
        IntVar[] objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = model.intVar("o_" + (i + 1), 0, (int) ceil(capacities[1] / volumes[i]), true);
        }
        final IntVar power = model.intVar("power", 0, 99999, true);
        IntVar scalar = model.intVar("weight", capacities[0], capacities[1], true);
        model.scalar(objects, volumes, "=", scalar).post();
        model.scalar(objects, energies, "=", power).post();
        model.knapsack(objects, scalar, power, volumes, energies).post();
        // ... end of modelling

        Solver r = model.getSolver();
        Solution sol = r.findSolution();
        r.printShortStatistics();
        // let's start the reparation of the previous solution
        // so, reset the search
        r.reset();
        model.setObjective(Model.MAXIMIZE, power);
        // declaring a neighborhood for LNS, here a random one
        RandomNeighborhood rnd = new RandomNeighborhood(objects, 1, 0);
        // initialize it with the previous (best) solution
        // declare LNS
        r.setLNS(rnd, sol);
        r.limitTime("2s");
        // find the new best solution
        int bw = 0, bp = 0;
        while (r.solve()) {
            bp = power.getValue();
            bw = scalar.getValue();
        }
        r.printShortStatistics();
        Assert.assertEquals(bp, 8372);
        Assert.assertEquals(bw, 1092);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPN1() {
        // Instance copied from meb-inst-18-09.eprime-param

        int nodes = 18;

        int initialNode = 15;

        //  Indexed by parent, child.  Should be symmetric.
        int[][] linkCosts = {
                {0, 0, 0, 104, 0, 52, 0, 0, 0, 0, 0, 0, 0, 2, 0, 20, 0, 73},
                {0, 0, 0, 0, 152, 0, 1, 0, 7, 0, 17, 11, 0, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 104, 0, 0, 1, 0, 1, 0, 0, 20, 0, 1, 0, 0, 0},
                {104, 0, 0, 0, 0, 5, 151, 0, 100, 0, 191, 0, 0, 0, 0, 8, 0, 0},
                {0, 152, 104, 0, 0, 0, 191, 46, 140, 197, 0, 75, 0, 0, 106, 0, 104, 0},
                {52, 0, 0, 5, 0, 0, 0, 0, 105, 0, 0, 0, 0, 70, 0, 1, 0, 0},
                {0, 1, 0, 151, 191, 0, 0, 0, 1, 0, 11, 46, 0, 0, 0, 0, 14, 0},
                {0, 0, 1, 0, 46, 0, 0, 0, 0, 15, 0, 0, 77, 0, 6, 0, 0, 0},
                {0, 7, 0, 100, 140, 105, 1, 0, 0, 0, 36, 77, 0, 0, 0, 197, 33, 0},
                {0, 0, 1, 0, 197, 0, 0, 15, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0},
                {0, 17, 0, 191, 0, 0, 11, 0, 36, 0, 0, 144, 0, 0, 0, 0, 60, 0},
                {0, 11, 0, 0, 75, 0, 46, 0, 77, 0, 144, 0, 0, 0, 0, 0, 1, 0},
                {0, 0, 20, 0, 0, 0, 0, 77, 0, 2, 0, 0, 0, 0, 6, 0, 0, 0},
                {2, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 0, 0, 0, 34, 0, 0},
                {0, 0, 1, 0, 106, 0, 0, 6, 0, 1, 0, 0, 6, 0, 0, 0, 0, 0},
                {20, 0, 0, 8, 0, 1, 0, 0, 197, 0, 0, 0, 0, 34, 0, 0, 0, 0},
                {0, 1, 0, 0, 104, 0, 14, 0, 33, 0, 60, 1, 0, 0, 0, 0, 0, 0},
                {73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };

        int maxLinkCost = 0;
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (linkCosts[i][j] > maxLinkCost) maxLinkCost = linkCosts[i][j];
            }
        }

        assert initialNode >= 0 && initialNode < nodes;
        assert linkCosts.length == nodes;
        assert linkCosts[0].length == nodes;

        Model model = new Model("MEB");

        IntVar[] parents = model.intVarArray("parents", nodes, 0, nodes - 1);   // indexed by child node
        IntVar[] depths = model.intVarArray("depths", nodes, 0, nodes - 1);    // indexed by node
        IntVar[] cost = model.intVarArray("cost", nodes, 1, maxLinkCost);
        IntVar optVar = model.intVar("optVar", 0, maxLinkCost * nodes);

        model.arithm(parents[initialNode], "=", initialNode).post();
        model.arithm(cost[initialNode], "=", 1).post();

        for (int child = 0; child < nodes; child++) {
            if (child != initialNode) {
                model.arithm(parents[child], "!=", child).post();

                // Assuming linkCosts is symmetric, i.e. can swap parent/child indexes
                // Index into linkCosts using the parent and store result in 'cost'
                model.element(cost[child], linkCosts[child], parents[child], 0).post();
                //  Constraint !=0 is implicit in domain of 'cost'.

                IntVar depthpar = model.intVar("depthpar" + child, 1, nodes);
                model.element(depthpar, depths, parents[child], 0).post();

                model.arithm(depths[child], ">", depthpar).post();
            }
        }

        IntVar[] costmaxs = model.intVarArray("costmaxs", nodes, 0, maxLinkCost);
        for (int parent = 0; parent < nodes; parent++) {
            // Take the max of transmitting to its children

            IntVar[] costmaxpart = model.intVarArray("costmaxpart[" + parent + "]", nodes, 0, maxLinkCost);

            for (int poschild = 0; poschild < nodes; poschild++) {
                // parents[poschild]!=parent, then costmaxpart will be 0.
                // Otherwise, costmaxpart will be the linkCost of parent to child.
                IntVar reifvar = model.arithm(parents[poschild], "=", parent).reify();
                model.arithm(reifvar, "*", model.intVar(linkCosts[parent][poschild]), "=", costmaxpart[poschild]).post();
            }
            model.max(costmaxs[parent], costmaxpart).post();
        }

        int[] coeffs = new int[nodes];
        for (int i = 0; i < nodes; i++) coeffs[i] = 1;
        model.scalar(costmaxs, coeffs, "=", optVar).post();

        model.setObjective(Model.MINIMIZE, optVar);

        ////////////////////////////////////////////////////////////////////////////

        //  Collect decision variables into one array.
        IntVar[] decvars = new IntVar[4 * nodes];
        for (int i = 0; i < nodes; i++) {
            decvars[i] = parents[i];
            decvars[i + nodes] = depths[i];
            decvars[i + (2 * nodes)] = cost[i];
            decvars[i + (3 * nodes)] = costmaxs[i];
        }
        //decvars[4*nodes]=optVar;


        // Set up basic search for first sol.
        Move basicsearch = new MoveBinaryDFS(new DomOverWDeg(decvars, 992634, new IntDomainMin()));
        Solver solver = model.getSolver();
        solver.setMove(basicsearch);

        solver.solve();

        //   Type of LNS neighbourhood -- propagation-guided LNS.
//        INeighbor in=INeighborFactory.propagationGuided(decvars);
        INeighbor in = INeighborFactory.blackBox(decvars);

        in.init(); // Should this be necessary?

        //   Type of search within LNS neighbourhoods
        Move innersearch = new MoveBinaryDFS(new DomOverWDeg(decvars, 0L, new IntDomainMin()));

        MoveLNS lns = new MoveLNS(innersearch, in, new BacktrackCounter(model, 50));

        solver.setMove(lns);
        solver.limitNode(20000);
        while (solver.solve()) ;
        Assert.assertEquals(solver.getObjectiveManager().getBestUB(), 318);
    }


    // --- LNS on a set var

    @Test(groups="10s", timeOut=60000)
    public void testKnapsackSet20() {
        int obj1 = testKnapsackSet(20, false);
        int obj2 = testKnapsackSet(20, true);
        Assert.assertTrue(obj1 == obj2);
    }

    @Test(groups="10s", timeOut=60000)
    public void testKnapsackSet30() {
        int obj1 = testKnapsackSet(30, false);
        int obj2 = testKnapsackSet(30, true);
        Assert.assertTrue(obj1==obj2); // on this data set LNS improves results
    }

    @Test(groups="60s", timeOut=60000)
    public void testKnapsackSet50() {
        int obj1 = testKnapsackSet(50, false);
        int obj2 = testKnapsackSet(50, true);
        Assert.assertTrue(obj1<obj2); // on this data set LNS improves results
    }

    private static int testKnapsackSet(int nos, boolean lns) {
        int[] capacities = {99, 1101};
        int[] volumesDef = {54, 12, 47, 33, 30, 65, 56, 57, 91, 88, 77, 99, 29, 23, 39, 86, 12, 85, 22, 64};
        int[] energiesDef = {38, 57, 69, 90, 79, 89, 28, 70, 38, 71, 46, 41, 36, 68, 92, 33, 84, 90};
        int[] volumes = new int[nos];
        int[] energies = new int[nos];
        for(int i=0;i<nos;i++){
            volumes[i] = volumesDef[i%volumesDef.length];
            energies[i] = energiesDef[i%energiesDef.length];
        }

        Model m = new Model();
        // occurrence of each item
        SetVar in = m.setVar(new int[0], ArrayUtils.array(0,nos));
        final IntVar power = m.intVar("power", 0, 99999, true);
        final IntVar weight = m.intVar("weight", capacities[0], capacities[1], true);
        m.sumElements(in, volumes, 0, weight).post();
        m.sumElements(in, energies, 0, power).post();

        Solver s = m.getSolver();
        s.setSearch(Search.setVarSearch(new InputOrder<>(m), new SetDomainMin(), false, in));
        if(lns) s.setLNS(INeighborFactory.setVarRandom(in));
        s.limitTime("5s");

        //r.limitTime("10s");
        m.setObjective(Model.MAXIMIZE, power);
        int bp = 0;
        while (m.getSolver().solve()) {
            bp = power.getValue();
        }

        return bp;
    }
}
