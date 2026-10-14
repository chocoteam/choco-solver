/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.solving;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainSticky;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, pages "Solving / Search strategies"
 * (docs/content/docs/Solving/Strategies.md) and "Advanced usages / Designing a search strategy"
 * (docs/content/docs/Advanced usages/Strategies.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the pages.
 *
 * @author Charles Prud'homme
 */
public class StrategiesExamples {

    private static IntVar[] model(Model model) {
        IntVar[] vars = model.intVarArray("x", 6, 0, 10);
        model.allDifferent(vars).post();
        IntVar sum = model.intVar("sum", 0, 60);
        model.sum(vars, "=", sum).post();
        model.setObjective(Model.MINIMIZE, sum);
        return vars;
    }

    @Test(groups = "1s")
    public void blackBox() {
        Model model = new Model();
        model(model);
        // tag::blackBox[]
        // a configuration for optimization problems (forCSP() for satisfaction problems),
        // applied to all the variables of the model
        BlackBoxConfigurator.forCOP().make(model);
        // end::blackBox[]
        Solver solver = model.getSolver();
        while (solver.solve()) ;
        Assert.assertEquals(solver.getBestSolutionValue().intValue(), 15);
    }

    @Test(groups = "1s")
    public void blackBoxCustom() {
        Model model = new Model();
        model(model);
        // tag::blackBoxCustom[]
        BlackBoxConfigurator.forCOP()
                // dom/wdeg on integer variables, smallest value first
                .setIntVarStrategy(vars -> Search.domOverWDegSearch(vars))
                // Luby restarts: cutoff 500, maximum 50,000 restarts, reset the cutoff on solutions
                .setRestartPolicy(SearchParams.Restart.LUBY, 500, 0., 50_000, true)
                .setNogoodOnRestart(true)
                .make(model);
        // end::blackBoxCustom[]
        Solver solver = model.getSolver();
        while (solver.solve()) ;
        Assert.assertEquals(solver.getBestSolutionValue().intValue(), 15);
    }

    @Test(groups = "1s")
    public void sticky() {
        Model model = new Model();
        IntVar[] vars = model(model);
        Solver solver = model.getSolver();
        // tag::sticky[]
        // try first the last value each variable took during propagation (phase saving),
        // or the smallest value of its domain if this value is no longer available
        IntDomainSticky sticky = new IntDomainSticky(vars, new IntDomainMin(), null);
        solver.setSearch(Search.intVarSearch(new FirstFail(model), sticky, vars));
        solver.setLubyRestart(500, new FailCounter(model, 500), 50_000);
        // end::sticky[]
        while (solver.solve()) ;
        Assert.assertEquals(solver.getBestSolutionValue().intValue(), 15);
    }
}
