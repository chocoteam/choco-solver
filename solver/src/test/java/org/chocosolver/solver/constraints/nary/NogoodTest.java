/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.nary.sat.PropSat;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.search.restart.MonotonicCutoff;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.SetDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class NogoodTest {

    @DataProvider
    public Object[][] intCombinations() {
        return Providers.merge(
                new Object[][]{{2, 2, 9, 17},
                        {3, 2, 27, 54}, {3, 3, 64, 133},
                        {4, 2, 81, 175}, {4, 3, 256, 558}, {4, 4, 625, 1396},
                        {5, 3, 1024, 2469}, {5, 4, 3125, 6729}, {5, 5, 7776, 16057},
                },
                new Object[][]{
                        {true, false},
                        {false, false},
                        {false, true}
                },
                IntStream.range(29091981, 29091981 + 20).mapToObj(i -> new Object[]{i}).toArray(Object[][]::new));
    }

//    @Test(dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
//    public void test11(boolean ngSAT) {
//        test2(5, 5, 7776, 16057, false, ngSAT, 29091981);
//    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "intCombinations")
    public void test1(int nbvars, int ub, int nbsols, int nbbcks, boolean ngSAT, boolean minimize, long seed) {
        final Model model = new Model(SettingsBuilder.init()
                .setNogoodFromRestartWithSAT(ngSAT)
                .setNogoodFromRestartMinimize(minimize));
        IntVar[] vars = model.intVarArray("vars", nbvars, 0, ub, false);
        Solver solver = model.getSolver();
        solver.setSearch(randomSearch(vars, seed));
        solver.setNoGoodRecordingFromRestarts();
        solver.setRestarts(new BacktrackCounter(model, 0), new MonotonicCutoff(30), 100);
//        solver.showDecisions(() -> solver.getNodeCount()+"-"+solver.getFailCount()+"-"+solver.getRestartCount()+"::"+ Arrays.toString(vars));
        while (solver.solve()) ;
        assertEquals(solver.getSolutionCount(), nbsols);
        if (seed == 29091981L) {
            assertEquals(model.getSolver().getBackTrackCount(), nbbcks);
        }
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "intCombinations")
    public void test2(int nbvars, int ub, int nbsols, int nbbcks, boolean ngSAT, boolean minimize, long seed) {
        final Model model = new Model(SettingsBuilder.init()
                .setNogoodFromRestartWithSAT(ngSAT)
                .setNogoodFromRestartMinimize(minimize));
        IntVar[] vars = model.intVarArray("vars", nbvars, 0, ub, false);
        Solver solver = model.getSolver();
        solver.setSearch(new FullyRandom(vars, seed));
        solver.setNoGoodRecordingFromRestarts();
        solver.setRestarts(new BacktrackCounter(model, 0), new MonotonicCutoff(30), 1000);
        solver.limitTime(2000);
//        solver.showDecisions(() -> solver.getNodeCount()+"-"+solver.getFailCount()+"-"+solver.getRestartCount()+"::"+ Arrays.toString(vars));
        while (solver.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), nbsols);
//        if(seed == 29091981L) {
//            assertEquals(model.getSolver().getBackTrackCount(), nbbcks);
//        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3a() {
        Model model = new Model("nogoods");
        IntVar x = model.intVar("x", -1, 1, false);
        IntVar y = model.intVar("y", 0, 2, false);
        IntVar z = model.intVar("z", 1, 5, false);
        PropSat sat = model.getMinisat().getPropSat();
        sat.initialize();


        TIntList ng = new TIntArrayList();
        ng.add(MiniSat.makeLiteral(sat.makeIntEq(x, 1), false));
        ng.add(MiniSat.makeLiteral(sat.makeIntEq(y, 1), false));
        ng.add(MiniSat.makeLiteral(sat.makeIntLe(z, 3), true));
        sat.addClause(ng);
        Solver solver = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 43);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3b() {
        Model model = new Model("nogoods");
        IntVar x = model.intVar("x", 0, 1, false);
        IntVar y = model.intVar("y", 1, 2, false);
        IntVar z = model.intVar("z", 2, 3, false);
        PropSat sat = model.getMinisat().getPropSat();
        sat.initialize();


        TIntList ng = new TIntArrayList();
        ng.add(MiniSat.makeLiteral(sat.makeIntEq(x, 1), false));
        ng.add(MiniSat.makeLiteral(sat.makeIntEq(y, 1), false));
        ng.add(MiniSat.makeLiteral(sat.makeIntLe(z, 3), true));
        sat.addClause(ng);
        Solver solver = model.getSolver();
        solver.showSolutions();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() throws ContradictionException {
        Model chocoModel = new Model("ASSIST");

        IntVar v1 = chocoModel.intVar("v1", 0, 3, false);
        IntVar v2 = chocoModel.intVar("v2", 0, 3, false);
        IntVar v3 = chocoModel.intVar("v3", 0, 1, false);

        chocoModel.getSolver().propagate();

        /* Default case - make sure only solutions differing in the v1, v2 values are returned */
        chocoModel.getSolver().setNoGoodRecordingFromSolutions(v1, v2);

        /* Adding a constraint */
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(0, 1, 0);
        tuples.add(1, 1, 1);
        tuples.add(1, 2, 1);
        tuples.add(2, 2, 0);
        tuples.add(3, 3, 0);

        IntVar[] varArray = {v1, v2, v3};
        chocoModel.table(varArray, tuples, "GAC3rm+").post();

        /* Setting the optional case - show me only solutions that differ in v1 value */
        chocoModel.getSolver().setNoGoodRecordingFromSolutions(v2);

        chocoModel.getSolver().findAllSolutions();
        Assert.assertEquals(chocoModel.getSolver().getSolutionCount(), 4);
    }

    @DataProvider
    public Object[][] setCombinations() {
        return Providers.merge(
                new Object[][]{{2, 2, 4, 7},
                        {3, 2, 8, 15}, {3, 3, 64, 133},
                        {4, 2, 16, 31}, {4, 3, 256, 527}, {4, 4, 4096, 8218},
                        {5, 3, 1024, 2066}, {5, 4, 32768, 65571}, //{5, 5, 1048576, 2097201},
                },
                new Object[][]{
                        {true, false},
                        {false, false},
                        {false, true}
                },
                IntStream.range(29091981, 29091981 + 20).mapToObj(i -> new Object[]{i}).toArray(Object[][]::new));
    }

    @Test(dataProviderClass = Providers.class, dataProvider = "trueOrFalse")
    public void test55(boolean ngSAT) {
        test5(3, 3, 64, 133, ngSAT, false, 29091990);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "setCombinations")
    public void test5(int nbvars, int ub, int nbsols, int nbbcks, boolean ngSAT, boolean minimize, long seed) {
        final Model model = new Model(SettingsBuilder.init()
                .setNogoodFromRestartWithSAT(ngSAT)
                .setNogoodFromRestartMinimize(minimize));
        SetVar[] vars = model.setVarArray("vars", nbvars, new int[]{}, IntStream.range(1, ub).toArray());
        Solver solver = model.getSolver();
        solver.setNoGoodRecordingFromRestarts();
        solver.setSearch(Search.setVarSearch(new Random<>(seed), new SetDomainMin(), true, vars));
        solver.setRestarts(new BacktrackCounter(model, 0), new MonotonicCutoff(30), 3);
//        solver.showDecisions(() -> solver.getNodeCount()+"-"+solver.getFailCount()+"-"+solver.getRestartCount()+"::"+ Arrays.toString(vars));
        while (solver.solve()) ;
        assertEquals(solver.getSolutionCount(), nbsols);
        if (seed == 29091981) {
            assertEquals(solver.getBackTrackCount(), nbbcks);
        }
    }


}
