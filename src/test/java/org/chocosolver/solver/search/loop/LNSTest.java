/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.Math.ceil;
import static org.chocosolver.solver.search.strategy.Search.domOverWDegSearch;
import static org.chocosolver.solver.search.strategy.Search.lastConflict;

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
                r.setLNS(new PropagationGuidedNeighborhood(objects, 100, 10, 123456L));
                break;
            case 3:
                r.setLNS(new SequenceNeighborhood(
                        new PropagationGuidedNeighborhood(objects, 100, 10, 123456L),
                        new ReversePropagationGuidedNeighborhood(objects, 100, 10, 123456L)
                ));
                break;
            case 4:
                r.setLNS(new SequenceNeighborhood(
                        new PropagationGuidedNeighborhood(objects, 100, 10, 123456L),
                        new ReversePropagationGuidedNeighborhood(objects, 100, 10, 123456L),
                        new RandomNeighborhood(objects, 200, 123456L)
                ));
                break;
            case 5:
                r.setLNS(new SequenceNeighborhood(
                        new ExplainingCut(model, 200, 123456L),
                        new RandomNeighborhood(objects, 200, 123456L)));
                break;
            case 6:
                r.setNoGoodRecordingFromRestarts();
                r.setLNS(new RandomNeighborhood(objects, 200, 123456L));
                break;
        }
        model.setObjective(Model.MAXIMIZE, power);
        int bw = 0, bp = 0;
        while(model.getSolver().solve()){
            bp = power.getValue();
            bw = scalar.getValue();
        }
        Assert.assertEquals(bp,8372);
        Assert.assertEquals(bw,1092);
    }

    @DataProvider(name = "lns")
    public Object[][] createData() {
        return new Object[][] {{0},{1},{2},{3},{4},{5}, {6}};
    }


    @Test(groups="10s", timeOut=300000, dataProvider = "lns")
    public void test1(int lns) {
        // opt: 8372
        knapsack20(lns);
    }

    @Test
    public void testTOTO(){
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
        while(r.solve()) {
            // store the solution, erase the previous one
            // so the last one is the best one.
            sol.record();
        }
        r.printShortStatistics();
        // let's start the reparation of the previous solution
        // so, reset the search
        r.reset();
        try{
            // object #17 cannot be set to a value greater than 50 (91 in the previous solution)
            objects[16].updateUpperBound(50, Cause.Null);
        }catch (ContradictionException e){}
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
        while(model.getSolver().solve()){
            bp = power.getValue();
            bw = scalar.getValue();
        }
        r.printShortStatistics();
        Assert.assertEquals(bp,6937);
        Assert.assertEquals(bw,1092);
    }

}
