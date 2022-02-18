/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.chocosolver.solver.Model;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 07/03/2016.
 */
public class SatDecoratoTest {
    private Model model;
    private SatDecorator sat;
    private int a, b, c, d;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        model = new Model();
        sat = new SatDecorator(model);
        a = sat.newVariable();
        b = sat.newVariable();
        c = sat.newVariable();
        d = sat.newVariable();
    }

    @Test(groups = "1s")
    public void testCancelUntil() throws Exception {
        int ap = MiniSat.makeLiteral(a, true);
        int an = MiniSat.makeLiteral(a, false);
        int bp = MiniSat.makeLiteral(b, true);
        int cp = MiniSat.makeLiteral(c, true);
        int cn = MiniSat.makeLiteral(c, false);
        int dp = MiniSat.makeLiteral(d, true);
        sat.addClause(an, bp);
        sat.addClause(cn, dp);
        sat.propagateOneLiteral(ap);
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        sat.propagateOneLiteral(cp);
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.qhead_, 4);
        Assert.assertEquals(sat.trail_.size(), 4);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_.get(2), 5);
        Assert.assertEquals(sat.trail_.get(3), 7);
        Assert.assertEquals(sat.trail_markers_.size(), 2);
        Assert.assertEquals(sat.trailMarker(), 2);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        Assert.assertEquals(sat.trail_markers_.get(1), 2);
        sat.cancelUntil(1);
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        sat.cancelUntil(0);
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.trail_.size(), 0);
        Assert.assertEquals(sat.trail_markers_.size(), 0);
        Assert.assertEquals(sat.trailMarker(), 0);
    }

    @Test(groups = "1s")
    public void testValueVar() throws Exception {
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        sat.propagateOneLiteral(MiniSat.makeLiteral(c, true));
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lTrue);

        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        sat.propagateOneLiteral(MiniSat.makeLiteral(d, false));
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lFalse);
    }

    @Test(groups = "1s")
    public void testValueLit() throws Exception {
        int cp = MiniSat.makeLiteral(c, true);
        int cn = MiniSat.makeLiteral(c, false);
        int dp = MiniSat.makeLiteral(d, true);
        int dn = MiniSat.makeLiteral(d, false);

        Assert.assertEquals(sat.valueLit(cp), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueLit(cn), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueLit(dp), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueLit(dn), MiniSat.Boolean.lUndef);

        sat.propagateOneLiteral(MiniSat.makeLiteral(c, true));
        Assert.assertEquals(sat.valueLit(cp), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueLit(cn), MiniSat.Boolean.lFalse);

        sat.propagateOneLiteral(MiniSat.makeLiteral(d, false));
        Assert.assertEquals(sat.valueLit(dp), MiniSat.Boolean.lFalse);
        Assert.assertEquals(sat.valueLit(dn), MiniSat.Boolean.lTrue);
    }

    @Test(groups = "1s")
    public void testPropagateOneLiteral() throws Exception {
        int ap = MiniSat.makeLiteral(a, true);
        int an = MiniSat.makeLiteral(a, false);
        int bp = MiniSat.makeLiteral(b, true);
        int bn = MiniSat.makeLiteral(b, false);
        int cp = MiniSat.makeLiteral(c, true);
        int cn = MiniSat.makeLiteral(c, false);
        int dp = MiniSat.makeLiteral(d, true);
        int dn = MiniSat.makeLiteral(d, false);
        sat.addClause(an, bp);
        sat.addClause(cn, dp);
        sat.addClause(cp, dp);

        Assert.assertTrue(sat.propagateOneLiteral(ap));
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);

        Assert.assertFalse(sat.propagateOneLiteral(an));
        Assert.assertTrue(sat.propagateOneLiteral(bp));
        Assert.assertEquals(sat.valueVar(a), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.Boolean.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.Boolean.lUndef);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 2);
        Assert.assertEquals(sat.trailMarker(), 2);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        Assert.assertEquals(sat.trail_markers_.get(1), 2);
        Assert.assertFalse(sat.propagateOneLiteral(bn));

        sat.uncheckedEnqueue(dn);
        Assert.assertFalse(sat.propagateOneLiteral(cn));

    }

    @Test(groups = "1s")
    public void testPropagate() throws Exception {
        sat.addBoolOrArrayEqualTrue(a, b, c, d);
        sat.addBoolOrArrayEqualTrue(b, c, d);
        sat.addBoolOrArrayEqualTrue(a, c, d);
        Assert.assertEquals(sat.propagate(), MiniSat.CR_Undef);
        Assert.assertTrue(sat.propagateOneLiteral(MiniSat.makeLiteral(a, false)));
        Assert.assertTrue(sat.propagateOneLiteral(MiniSat.makeLiteral(b, false)));
        Assert.assertTrue(sat.propagateOneLiteral(MiniSat.makeLiteral(c, false)));
        Assert.assertFalse(sat.propagateOneLiteral(MiniSat.makeLiteral(d, false)));
        sat.cancelUntil(2);
        Assert.assertTrue(sat.propagateOneLiteral(MiniSat.makeLiteral(d, true)));
    }
}