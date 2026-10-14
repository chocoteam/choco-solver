/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.modeling;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Modeling / Constraints"
 * (docs/content/docs/Modeling/Constraints.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class ConstraintsExamples {

    @Test(groups = "1s")
    public void post() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("x", 3, 0, 2);
        // tag::post[]
        model.allDifferent(vars).post();
        // end::post[]
        Assert.assertTrue(model.getSolver().solve());
    }

    @Test(groups = "1s")
    public void reify() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 5);
        Constraint constraint = model.arithm(x, ">", 2);
        // tag::reify[]
        BoolVar b = constraint.reify();
        // end::reify[]
        Assert.assertSame(constraint.reify(), b);
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(b.getValue() == 1, x.getValue() > 2);
    }

    @Test(groups = "1s")
    public void reifyWith() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 5);
        Constraint constraint = model.arithm(x, ">", 2);
        // tag::reifyWith[]
        BoolVar b = model.boolVar();
        constraint.reifyWith(b);
        // end::reifyWith[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(b.getValue() == 1, x.getValue() > 2);
    }

    @Test(groups = "1s")
    public void ifThen() {
        Model model = new Model();
        IntVar x = model.intVar("x", -5, 5);
        IntVar y = model.intVar("y", 0, 50);
        // tag::ifThen[]
        model.ifThen(
                model.arithm(x, "<", 0),
                model.arithm(y, ">", 42)
        );
        // end::ifThen[]
        model.arithm(x, "=", -1).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(y.getValue() > 42);
    }

    @Test(groups = "1s")
    public void reifyTwice() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 5);
        Constraint constraint = model.arithm(x, ">", 2);
        // tag::reifyTwice[]
        BoolVar b1 = model.boolVar();
        BoolVar b2 = model.boolVar();
        constraint.reifyWith(b1);
        constraint.reifyWith(b2); // posts b1 = b2
        // end::reifyTwice[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(b1.getValue(), b2.getValue());
    }
}
