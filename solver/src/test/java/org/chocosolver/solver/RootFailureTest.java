/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Calling solve() again after a failure at the root must keep returning false (issue #1281).
 *
 * @author Charles Prud'homme
 */
public class RootFailureTest {

    @DataProvider(name = "lcg")
    public Object[][] lcg() {
        return new Object[][]{{false}, {true}};
    }

    @Test(groups = "1s", dataProvider = "lcg")
    public void testInitialPropagationWithCut(boolean lcg) {
        Model model = new Model(SettingsBuilder.init().setLCG(lcg).build());
        IntVar value = model.intVar("value", 0, 1);
        model.arithm(value, "=", 0).post();
        model.setObjective(Model.MAXIMIZE, value);
        Solver solver = model.getSolver();
        solver.onReceivingExternalCut(0);
        Assert.assertFalse(solver.solve());
        Assert.assertFalse(solver.solve());
        Assert.assertFalse(solver.solve());
    }

    @Test(groups = "1s")
    public void testInitialPropagationSatisfaction() {
        Model model = new Model(SettingsBuilder.prod().build());
        IntVar value = model.intVar("value", 0, 2);
        model.arithm(value, "=", 0).post();
        model.arithm(value, "=", 2).post();
        Solver solver = model.getSolver();
        Assert.assertFalse(solver.solve());
        Assert.assertFalse(solver.solve());
    }

    @Test(groups = "1s")
    public void testResetAfterRootFailure() {
        Model model = new Model();
        IntVar value = model.intVar("value", 0, 1);
        model.arithm(value, "=", 0).post();
        model.setObjective(Model.MAXIMIZE, value);
        Solver solver = model.getSolver();
        solver.onReceivingExternalCut(0);
        Assert.assertFalse(solver.solve());
        solver.reset();
        Assert.assertTrue(solver.solve());
        Assert.assertEquals(value.getValue(), 0);
    }

    @Test(groups = "1s", dataProvider = "lcg")
    public void testRestartFailureAfterLastSolution(boolean lcg) {
        Model model = new Model(SettingsBuilder.init().setLCG(lcg).build());
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        IntVar obj = model.intVar("obj", 0, 10);
        model.arithm(x, "+", y, "<=", 5).post();
        model.arithm(x, "+", y, "=", obj).post();
        model.setObjective(Model.MAXIMIZE, obj);
        Solver solver = model.getSolver();
        solver.setRestartOnSolutions();
        while (solver.solve()) { }
        Assert.assertEquals(solver.getBestSolutionValue().intValue(), 5);
        Assert.assertFalse(solver.solve());
        Assert.assertFalse(solver.solve());
    }

    @Test(groups = "1s", dataProvider = "lcg")
    public void testRestartFailureSmallModel(boolean lcg) {
        Model model = new Model(SettingsBuilder.init().setLCG(lcg).build());
        IntVar value = model.intVar("value", 0, 2);
        model.arithm(value, "<=", 1).post();
        model.setObjective(Model.MAXIMIZE, value);
        Solver solver = model.getSolver();
        solver.setRestartOnSolutions();
        while (solver.solve()) { }
        Assert.assertFalse(solver.solve());
        Assert.assertFalse(solver.solve());
    }
}
