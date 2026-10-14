/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.advanced;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.MiniSatSolver;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Examples shown in the user documentation, page "Advanced usages / SAT"
 * (docs/content/docs/Advanced usages/SAT.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class SatExamples {

    @Test(groups = "1s")
    public void miniSat() {
        // tag::miniSat[]
        MiniSatSolver sat = new MiniSatSolver();
        int a = sat.newVariable();
        int b = sat.newVariable();
        // a or not b
        sat.addClause(MiniSat.makeLiteral(a), MiniSat.neg(MiniSat.makeLiteral(b)));
        ESat ret = sat.solve();
        // end::miniSat[]
        Assert.assertEquals(ret, ESat.TRUE);
    }

    @Test(groups = "1s")
    public void bindBool() {
        // tag::bindBool[]
        Model model = new Model();
        BoolVar a = model.boolVar("a");
        BoolVar b = model.boolVar("b");
        int sa = a.satVar();
        int sb = b.satVar();
        // end::bindBool[]
        // tag::clause[]
        // a is true implies b is true (a is false or b is true)
        model.addClause(model.neg(sa), model.lit(sb));
        // end::clause[]
        model.arithm(a, "=", 1).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertEquals(b.getValue(), 1);
    }

    @Test(groups = "1s")
    public void bindInt() {
        // tag::bindInt[]
        Model model = new Model();
        IntVar c = model.intVar("c", 1, 3);
        IntVar d = model.intVar("d", 2, 4);
        int sc = c.eq(2).boolVar().satVar();
        int sd = d.le(3).boolVar().satVar();
        // c = 2 implies d <= 3
        model.addClause(model.neg(sc), model.lit(sd));
        // end::bindInt[]
        model.arithm(c, "=", 2).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(d.getValue() <= 3);
    }

    @Test(groups = "1s")
    public void literalizer() {
        // tag::literalizer[]
        Model model = new Model();
        SetVar e = model.setVar("e", new int[]{}, new int[]{1, 2, 3});
        // link the membership of the value 2 to the set e
        int se = model.satVar(e, new Literalizer.SetInLit(e, 2));
        // then declare clauses, e.g. 2 must be in e
        model.addClause(model.lit(se));
        // end::literalizer[]
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertTrue(e.getValue().contains(2));
    }
}
