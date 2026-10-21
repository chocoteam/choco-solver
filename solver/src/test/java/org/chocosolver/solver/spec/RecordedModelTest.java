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
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * The simple usage: {@link Model#record(String)}, {@link Model#duplicate()} and
 * {@link ParallelPortfolio#of(Model, int)}, without knowing anything about specs.
 *
 * @author Charles Prud'homme
 */
public class RecordedModelTest {

    private static IntVar[] queens(Model model, int n) {
        IntVar[] q = model.intVarArray("q", n, 1, n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(q[i], "!=", q[j]).post();
                model.arithm(q[i], "!=", q[j], "+", j - i).post();
                model.arithm(q[i], "!=", q[j], "-", j - i).post();
            }
        }
        return q;
    }

    private static void assertQueens(Solution s, IntVar[] q) {
        for (int i = 0; i < q.length - 1; i++) {
            for (int j = i + 1; j < q.length; j++) {
                int a = s.getIntVal(q[i]);
                int b = s.getIntVal(q[j]);
                Assert.assertTrue(a != b && a != b + j - i && a != b - j + i);
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDuplicate() {
        Model model = Model.record("queens");
        queens(model, 8);

        Model copy = model.duplicate();
        Assert.assertNotSame(copy, model);
        Assert.assertEquals(copy.getNbVars(), model.getNbVars());
        Assert.assertEquals(copy.getNbCstrs(), model.getNbCstrs());
        Assert.assertEquals(copy.getSolver().findAllSolutions().size(), 92);
        // the original is untouched, and can be duplicated again
        Assert.assertEquals(model.duplicate().getSolver().findAllSolutions().size(), 92);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 92);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPortfolio() {
        Model model = Model.record("queens");
        IntVar[] q = queens(model, 10);

        ParallelPortfolio portfolio = ParallelPortfolio.of(model, 4);
        Assert.assertEquals(portfolio.getModels().size(), 4);
        Assert.assertSame(portfolio.getModels().get(0), model, "the original model is the first worker");
        Assert.assertTrue(portfolio.solve());
        // the solution is expressed with the variables of the original model
        assertQueens(portfolio.getBestSolution(), q);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOptimizationPortfolio() {
        Model model = Model.record("knapsack");
        IntVar[] occ = model.intVarArray("o", 3, 0, 5);
        IntVar value = model.intVar("v", 0, 100);
        model.knapsack(occ, model.intVar("w", 0, 10), value, new int[]{2, 3, 4}, new int[]{3, 5, 7}).post();
        model.setObjective(Model.MAXIMIZE, value);

        ParallelPortfolio portfolio = ParallelPortfolio.of(model, List.of(Variant.IDENTITY, Variant.seed(3)));
        Assert.assertEquals(portfolio.getModels().size(), 3, "the original model and one copy per variant");
        int best = -1;
        while (portfolio.solve()) {
            Solution s = portfolio.getBestSolution();
            best = s.getIntVal(value);
            Assert.assertEquals(best, 3 * s.getIntVal(occ[0]) + 5 * s.getIntVal(occ[1]) + 7 * s.getIntVal(occ[2]));
        }
        Assert.assertEquals(best, 17);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testOriginalSearchIsKept() {
        Model model = Model.record("queens");
        IntVar[] q = queens(model, 8);
        AbstractStrategy<IntVar> search = Search.inputOrderUBSearch(q);
        model.getSolver().setSearch(search);
        ParallelPortfolio portfolio = ParallelPortfolio.of(model, 2);
        Assert.assertTrue(portfolio.solve());
        Assert.assertSame(model.getSolver().getSearch(), search, "the search declared on the original model is kept");
        assertQueens(portfolio.getBestSolution(), q);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSolvedModelCannotBeDuplicated() {
        Model model = Model.record("queens");
        queens(model, 4);
        Assert.assertNotNull(model.duplicate());
        Assert.assertTrue(model.getSolver().solve());
        // once its resolution has started, the model cannot be duplicated anymore
        Assert.assertThrows(SolverException.class, model::duplicate);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPlainModelCannotBeDuplicated() {
        Model model = Model.create("plain");
        queens(model, 4);
        SolverException e = Assert.expectThrows(SolverException.class, model::duplicate);
        Assert.assertTrue(e.getMessage().contains("Model.record"), e.getMessage());
        Assert.assertThrows(SolverException.class, () -> ParallelPortfolio.of(model, 2));
    }
}
