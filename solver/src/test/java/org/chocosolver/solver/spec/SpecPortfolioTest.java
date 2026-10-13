/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests of {@link ParallelPortfolio#of(ModelSpec, List)}.
 *
 * @author Charles Prud'homme
 */
public class SpecPortfolioTest {

    private static final int[] WEIGHTS = {3, 4, 5, 8, 9, 11, 13};
    private static final int[] VALUES = {4, 5, 7, 10, 11, 15, 17};
    private static final int CAPACITY = 30;

    /**
     * A small knapsack, to maximize.
     *
     * @return the occurrences
     */
    private static IntVar[] knapsack(Model m) {
        IntVar[] occ = m.intVarArray("o", WEIGHTS.length, 0, 3);
        IntVar weight = m.intVar("w", 0, CAPACITY);
        IntVar value = m.intVar("v", 0, 200);
        m.knapsack(occ, weight, value, WEIGHTS, VALUES).post();
        m.setObjective(Model.MAXIMIZE, value);
        return occ;
    }

    /**
     * @return the optimum, computed sequentially
     */
    private static int optimum() {
        Model m = new Model();
        knapsack(m);
        Solution best = m.getSolver().findOptimalSolution((IntVar) m.getObjective(), Model.MAXIMIZE);
        return best.getIntVal((IntVar) m.getObjective());
    }

    private static void assertOptimal(ParallelPortfolio portfolio, RecordingModel master, IntVar[] occ) {
        int best = -1;
        while (portfolio.solve()) {
            best = portfolio.getBestModel().getSolver().getBestSolutionValue().intValue();
            // each solution can be read from the master
            SpecSolution sol = portfolio.getBestSpecSolution();
            Solution inMaster = sol.toSolution(master.getRecorder());
            int weight = 0;
            int value = 0;
            for (int i = 0; i < occ.length; i++) {
                weight += WEIGHTS[i] * inMaster.getIntVal(occ[i]);
                value += VALUES[i] * inMaster.getIntVal(occ[i]);
            }
            Assert.assertTrue(weight <= CAPACITY);
            Assert.assertEquals(value, best);
            Assert.assertEquals(inMaster.getIntVal((IntVar) master.getObjective()), best);
        }
        Assert.assertEquals(best, optimum());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCarbonCopies() {
        RecordingModel master = new RecordingModel("knapsack");
        IntVar[] occ = knapsack(master);
        ParallelPortfolio portfolio = ParallelPortfolio.of(master.snapshot(), 4);
        List<Model> models = portfolio.getModels();
        Assert.assertEquals(models.size(), 4);
        Set<String> names = new HashSet<>();
        for (Model m : models) {
            Assert.assertNotSame(m, master);
            Assert.assertNotNull(portfolio.resolverOf(m));
            names.add(m.getName());
        }
        Assert.assertEquals(names.size(), 4, "the models are named after their rank");
        assertOptimal(portfolio, master, occ);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testVariants() {
        RecordingModel master = new RecordingModel("knapsack");
        IntVar[] occ = knapsack(master);
        int[] ids = master.getRecorder().idsOf(occ);
        SearchDecl search = SearchDecl.intVarSearch(
                new SearchParams.VarSelConf(SearchParams.VariableSelection.DOM, Integer.MAX_VALUE),
                new SearchParams.ValSelConf(SearchParams.ValueSelection.MAX, false, 1, false), ids);
        ParallelPortfolio portfolio = ParallelPortfolio.of(master.snapshot(), List.of(
                Variant.IDENTITY,
                Variant.seed(17),
                Variant.search(search),
                Variant.settings(SettingsBuilder.init().setEnableSAT(true).build())));
        Assert.assertEquals(portfolio.getModels().size(), 4);
        Assert.assertEquals(portfolio.getModels().get(1).getSeed(), 17L);
        assertOptimal(portfolio, master, occ);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testManualPortfolioHasNoResolver() {
        ParallelPortfolio portfolio = new ParallelPortfolio();
        Model m = new Model();
        knapsack(m);
        portfolio.addModel(m);
        Assert.assertNull(portfolio.resolverOf(m));
        Assert.assertTrue(portfolio.solve());
        Assert.assertThrows(SolverException.class, portfolio::getBestSpecSolution);
        Assert.assertNotNull(portfolio.getBestSolution());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testInvalidVariantIsReported() {
        RecordingModel master = new RecordingModel("knapsack");
        knapsack(master);
        ModelSpec spec = master.snapshot();
        // a variant which breaks the replay: the error of the instantiation is reported
        Variant broken = Variant.mapCalls(c -> c.method().name().equals("knapsack")
                ? new Step.Call(c.out(), FactoryMethod.of("allDifferent", IntVar[].class, String.class),
                new Arg[]{c.args()[0], new Arg.Const("UNKNOWN")}) : c);
        Assert.assertThrows(SolverException.class, () -> ParallelPortfolio.of(spec, List.of(Variant.IDENTITY, broken)));
    }
}
