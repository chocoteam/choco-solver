/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/06/2021
 */
public class ImplicationTest {

    private Model imply(boolean dec, int seed) {
        Model model = new Model();
        IntVar x = model.intVar("X", 0, 1);
        IntVar y = model.intVar("Y", 0, 1);
        BoolVar r = model.boolVar("r");
        Constraint c = x.eq(y).decompose();
        if (dec) {
            BoolVar r2 = c.reify();
            r2.imp(r).post();
        } else {
            c.implies(r);
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(new IntVar[]{x, y, r}, seed));
        return model;
    }

    @Test(groups = "1s")
    public void test1() {
        for (int i = 0; i < 40; i++) {
            Model m1 = imply(false, i);
            Model m2 = imply(false, i);
            m1.getSolver().findAllSolutions();
            m2.getSolver().findAllSolutions();
            Assert.assertEquals(m1.getSolver().getSolutionCount(), 6);
            Assert.assertEquals(m2.getSolver().getSolutionCount(), 6);
            Assert.assertEquals(m2.getSolver().getNodeCount(), m1.getSolver().getNodeCount());
        }
    }


    private Model implied(boolean dec, int seed) {
        Model model = new Model(SettingsBuilder.init().setWarnUser(false));
        IntVar x = model.intVar("X", 0, 4);
        IntVar y = model.intVar("Y", 0, 4);
        BoolVar r = model.boolVar("r");
        Constraint c = x.eq(y).decompose();
        if (dec) {
            BoolVar r2 = c.reify();
            r.imp(r2).post();
        } else {
            c.impliedBy(r);
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(new IntVar[]{x, y, r}, seed));
        return model;
    }

    @Test(groups = "1s")
    public void test2() {
        for (int i = 3; i < 40; i++) {
            Model m1 = implied(false, i);
            Model m2 = implied(false, i);
            m1.getSolver().findAllSolutions();
            m2.getSolver().findAllSolutions();
            Assert.assertEquals(m1.getSolver().getSolutionCount(), 30);
            Assert.assertEquals(m2.getSolver().getSolutionCount(), 30);
            Assert.assertEquals(m2.getSolver().getNodeCount(), m1.getSolver().getNodeCount());
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNames() {
        // #1087: an implication is not named as a reification
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 3);
        BoolVar b = model.boolVar("b");
        model.arithm(x, "=", 1).impliedBy(b);
        model.arithm(x, ">", 0).reifyWith(model.boolVar("r"));
        Assert.assertEquals(model.getCstrs()[0].getName(), ConstraintsName.IMPLIEDCONSTRAINT);
        Assert.assertEquals(model.getCstrs()[1].getName(), ConstraintsName.REIFICATIONCONSTRAINT);
    }
}
