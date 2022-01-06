/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous.relational;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/2020
 */
public class PropEquationTest {

    @Test(groups = "1s")
    public void test1() {
        Model model = new Model();
        RealVar alpha = model.realVar("alpha", -Math.PI, Math.PI, 1.0e-6);

        alpha.cos().sub(alpha.sin()).eq(0.).equation().post();

        Solver solver = model.getSolver();
        solver.limitSolution(3);
        while (solver.solve()) {
            Assert.assertTrue(Math.abs(Math.cos(alpha.getLB()) - Math.sin(alpha.getLB())) < 1e-6);
        }
        Assert.assertTrue(solver.getSolutionCount() >= 2);
    }

    @Test(groups = "1s")
    public void test2() {
        Model model = new Model();

        RealVar alpha = model.realVar("alpha", -5.5 * Math.PI, -1.5 * Math.PI, 1.0e-6);
        alpha.cos().eq(1.0).equation().post();

        Solver solver = model.getSolver();
        solver.limitSolution(3);
        while (solver.solve()) {
            Assert.assertTrue(Math.cos(alpha.getLB())- 1.0 < 1e-6);
            Assert.assertTrue(Math.cos(alpha.getUB())- 1.0 < 1e-6);
        }
        Assert.assertTrue(solver.getSolutionCount() >= 2);
    }
}
