/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling / Constraints over real variables"
 * (docs/content/docs/Modeling/RealConstraints.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 * The examples of the "ibex" group require Ibex to be installed.
 *
 * @author Charles Prud'homme
 */
public class RealConstraintsExamples {

    @Test(groups = "ibex", timeOut = 60000)
    public void realVar() {
        Model model = new Model();
        // tag::realVar[]
        double p = 0.01d;
        RealVar x = model.realVar("x", 1, 5, p);
        RealVar y = model.realVar("y", 1, 5, p);
        // z = x^(y-2)
        RealVar z = x.pow(y.sub(2)).realVar(p);
        // end::realVar[]
        Assert.assertTrue(model.getSolver().solve());
        double expected = Math.pow(x.getLB(), y.getLB() - 2);
        Assert.assertEquals(z.getLB(), expected, 10 * p);
    }

    @Test(groups = "1s")
    public void equation() {
        Model model = new Model();
        // tag::equation[]
        double p = 0.01d;
        RealVar x = model.realVar("x", 1, 5, p);
        RealVar y = model.realVar("y", 1, 5, p);
        // x / (y-2) <= 1.5
        x.div(y.sub(2)).le(1.5).equation().post();
        // end::equation[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void ibex() {
        Model model = new Model();
        // tag::ibex[]
        double p = 0.01d;
        RealVar x = model.realVar("x", 1, 5, p);
        RealVar y = model.realVar("y", 3, 5, p);
        // x / (y-2) >= 1.6
        x.div(y.sub(2)).ge(1.6).ibex(p).post();
        // end::ibex[]
        Assert.assertTrue(model.getSolver().solve());
        double vy = (y.getLB() + y.getUB()) / 2;
        double vx = (x.getLB() + x.getUB()) / 2;
        Assert.assertTrue(vx / (vy - 2) >= 1.6 - 10 * p);
    }

    @Test(groups = "1s")
    public void intEq() {
        Model model = new Model();
        // tag::intEq[]
        IntVar foo = model.intVar("foo", new int[]{0, 15, 20});
        RealVar bar = model.realVar("bar", 0, 20, 1e-5);
        model.eq(bar, foo).post();
        // end::intEq[]
        int n = 0;
        while (model.getSolver().solve()) {
            Assert.assertEquals(bar.getLB(), foo.getValue(), 1e-5);
            n++;
        }
        Assert.assertEquals(n, 3);
    }

    @Test(groups = "1s")
    public void element() {
        Model model = new Model();
        // tag::element[]
        RealVar value = model.realVar("V", 0., 10., 1.e-4);
        IntVar index = model.intVar("I", 0, 5);
        double[] values = new double[]{-1., .8, Math.PI, 12.};
        model.element(value, values, index).post();
        // end::element[]
        int n = 0;
        while (model.getSolver().solve()) {
            Assert.assertEquals(value.getLB(), values[index.getValue()], 1.e-4);
            n++;
        }
        // only .8 and PI are in [0, 10]
        Assert.assertEquals(n, 2);
    }

    @Test(groups = "1s")
    public void scalar() {
        Model model = new Model();
        // tag::scalar[]
        double[] coeffs = new double[]{1, 5, 7, 8};
        RealVar[] vars = model.realVarArray(4, 1., 6., .1);
        model.scalar(vars, coeffs, "=", 35).post();
        // end::scalar[]
        Assert.assertTrue(model.getSolver().solve());
        double sum = 0;
        for (int i = 0; i < vars.length; i++) {
            sum += coeffs[i] * vars[i].getLB();
        }
        Assert.assertEquals(sum, 35, 21 * .1);
    }
}
