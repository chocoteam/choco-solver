/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Examples shown in the user documentation, page "Modeling / Building expressions on integer variables"
 * (docs/content/docs/Modeling/IntExpressions.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class IntExpressionsExamples {

    @Test(groups = "1s")
    public void arithmetic() {
        Model model = new Model();
        // tag::arithmetic[]
        IntVar x = model.intVar("x", 1, 5);
        IntVar y = model.intVar("y", 1, 5);
        // z = min((x + 5) % 3, y^2)
        IntVar z = x.add(5).mod(3).min(y.pow(2)).intVar();
        // end::arithmetic[]
        List<Solution> sols = model.getSolver().findAllSolutions();
        Assert.assertEquals(sols.size(), 25);
        for (Solution s : sols) {
            int vx = s.getIntVal(x);
            int vy = s.getIntVal(y);
            Assert.assertEquals(s.getIntVal(z), Math.min((vx + 5) % 3, vy * vy));
        }
    }

    @Test(groups = "1s")
    public void division() {
        Model model = new Model();
        IntVar x = model.intVar("x", -7);
        IntVar d = x.div(2).intVar();
        Assert.assertTrue(model.getSolver().solve());
        // rounding towards 0
        Assert.assertEquals(d.getValue(), -3);
    }

    @Test(groups = "1s")
    public void decompose() {
        Model model = new Model();
        // tag::decompose[]
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        x.ge(y).decompose().post();
        // end::decompose[]
        checkGe(model, x, y);
    }

    @Test(groups = "1s")
    public void extension() {
        Model model = new Model();
        // tag::extension[]
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        x.ge(y).extension().post();
        // end::extension[]
        checkGe(model, x, y);
    }

    private static void checkGe(Model model, IntVar x, IntVar y) {
        List<Solution> sols = model.getSolver().findAllSolutions();
        Assert.assertEquals(sols.size(), 21);
        for (Solution s : sols) {
            Assert.assertTrue(s.getIntVal(x) >= s.getIntVal(y));
        }
    }

    @Test(groups = "1s")
    public void boolVar() {
        Model model = new Model();
        // tag::boolVar[]
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        BoolVar b = x.gt(y).boolVar();
        // end::boolVar[]
        List<Solution> sols = model.getSolver().findAllSolutions();
        Assert.assertEquals(sols.size(), 36);
        for (Solution s : sols) {
            Assert.assertEquals(s.getIntVal(b) == 1, s.getIntVal(x) > s.getIntVal(y));
        }
    }

    @Test(groups = "1s")
    public void logical() {
        Model model = new Model();
        // tag::logical[]
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", 0, 5);
        // (x = y + 1) ==> (x + 2 <= 6)
        x.eq(y.add(1)).imp(x.add(2).le(6)).post();
        // end::logical[]
        List<Solution> sols = model.getSolver().findAllSolutions();
        // only (x, y) = (5, 4) is forbidden
        Assert.assertEquals(sols.size(), 35);
        for (Solution s : sols) {
            int vx = s.getIntVal(x);
            int vy = s.getIntVal(y);
            Assert.assertTrue(vx != vy + 1 || vx + 2 <= 6);
        }
    }
}
