/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.solving;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Solving / Multi-thread resolution"
 * (docs/content/docs/Solving/Portfolio.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the pages.
 *
 * @author Charles Prud'homme
 */
public class PortfolioExamples {

    private static Model makeModel(int id) {
        Model model = new Model("model #" + id);
        IntVar[] x = model.intVarArray("x", 5, 0, 9);
        model.allDifferent(x).post();
        model.sum(x, "=", 20).post();
        return model;
    }

    @Test(groups = "1s")
    public void unalterable() {
        ParallelPortfolio portfolio = new ParallelPortfolio();
        // tag::unalterable[]
        for (int s = 0; s < 4; s++) {
            Model model = makeModel(s);
            IntVar[] vars = model.retrieveIntVars(true);
            // a different search strategy for each model
            model.getSolver().setSearch(s % 2 == 0
                    ? Search.inputOrderLBSearch(vars)
                    : Search.inputOrderUBSearch(vars));
            // 'true, true': the search strategy is kept and the model is reliable
            portfolio.addModel(model, true, true);
        }
        // end::unalterable[]
        AbstractStrategy<?>[] declared = portfolio.getModels().stream()
                .map(m -> m.getSolver().getSearch())
                .toArray(AbstractStrategy[]::new);
        Assert.assertTrue(portfolio.solve());
        for (int s = 0; s < 4; s++) {
            Assert.assertSame(portfolio.getModels().get(s).getSolver().getSearch(), declared[s]);
        }
    }
}
