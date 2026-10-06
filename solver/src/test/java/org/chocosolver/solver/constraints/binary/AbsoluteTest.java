/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/08/2026
 */
public class AbsoluteTest extends AbstractBinaryTest {

    @Override
    protected int validTuple(int vx, int vy) {
        return Math.abs(vy) == vx ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return new Constraint(ConstraintsName.ABSOLUTE, new PropAbsolute(vars[0], vars[1]));
    }

    @Test(groups = "1s", timeOut = 30_000, dataProviderClass = Providers.class, dataProvider = "random")
    @Providers.Arguments(values = {"1", "50", "1"})
    public void testReification(int seed) {
        Model model = new Model();

        IntVar base = model.intVar("base", new int[]{-2, 0});
        IntVar result = model.intVar("result", new int[]{0, 1});
        BoolVar truth = make(new IntVar[]{result, base}, model).reify();

        // Make the issue deterministic and independent of the default search strategy.
        model.getSolver().setSearch(
                new FullyRandom(new IntVar[]{base, result, truth}, seed)
        );

        while (model.getSolver().solve()) {
            Assert.assertTrue(check(base.getValue(), result.getValue(), truth.getValue()));
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    private boolean check(int b, int r, int t) {
        return (Math.abs(b) == r) == (t == 1);
    }

    @DataProvider
    public Object[][] combinations(){
        return new Object[][]{
                {new int[]{0,1}, ESat.UNDEFINED},
                {new int[]{0}, ESat.TRUE},
                {new int[]{1, 2}, ESat.FALSE},
        };
    }

    @Test(groups = "1s", dataProvider = "combinations")
    public void testXY(int[] dom, ESat expected){
        Model model = new Model();

        IntVar X = model.intVar("X", 0);
        IntVar Y = model.intVar("result", dom);
        Constraint c = make(new IntVar[]{X, Y}, model);
        Assert.assertEquals(c.getPropagator(0).isEntailed(), expected);
    }
}
