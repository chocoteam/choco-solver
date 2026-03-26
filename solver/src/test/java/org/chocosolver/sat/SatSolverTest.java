/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import gnu.trove.list.array.TIntArrayList;
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
public class SatSolverTest {

    private MiniSat sat;
    private int a, b, c, d;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        sat = new MiniSat(false, 0);
        a = sat.newVariable();
        b = sat.newVariable();
        c = sat.newVariable();
        d = sat.newVariable();
    }

    @Test(groups = "1s")
    public void testNewVariable() {
        Assert.assertEquals(a, 0);
        Assert.assertEquals(b, 1);
        Assert.assertEquals(c, 2);
        Assert.assertEquals(d, 3);
        Assert.assertEquals(MiniSat.makeLiteral(a, true), 1);
        Assert.assertEquals(MiniSat.makeLiteral(a, false), 0);
        Assert.assertEquals(MiniSat.makeLiteral(b, true), 3);
        Assert.assertEquals(MiniSat.makeLiteral(b, false), 2);
        Assert.assertEquals(MiniSat.makeLiteral(c, true), 5);
        Assert.assertEquals(MiniSat.makeLiteral(c, false), 4);
        Assert.assertEquals(MiniSat.makeLiteral(d, true), 7);
        Assert.assertEquals(MiniSat.makeLiteral(d, false), 6);
    }

    @Test(groups = "1s")
    public void testAddClause() {
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(c), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(d), MiniSat.lUndef);

        Assert.assertTrue(sat.addClause(MiniSat.makeLiteral(a, true)));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lTrue);
        Assert.assertTrue(sat.addClause(MiniSat.makeLiteral(b, false)));
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lFalse);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.clauses.size(), 0);
    }

    @Test(groups = "1s")
    public void testLearnClause() {

    }

    @Test(groups = "1s")
    public void testAddClause1() {
        int ap = MiniSat.makeLiteral(a, true);
        int bp = MiniSat.makeLiteral(b, true);
        Assert.assertTrue(sat.addClause(ap, bp));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.clauses.size(), 1);
        Assert.assertEquals(sat.qhead_, 0);
    }

    @Test(groups = "1s")
    public void testAddClause2() {
        int ap = MiniSat.makeLiteral(a, true);
        int bp = MiniSat.makeLiteral(b, true);
        int cp = MiniSat.makeLiteral(c, true);
        Assert.assertTrue(sat.addClause(ap, bp, cp));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(c), MiniSat.lUndef);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.clauses.size(), 1);
    }

    @Test(groups = "1s")
    public void testAddClause3() {
        int ap = MiniSat.makeLiteral(a, true);
        int bp = MiniSat.makeLiteral(b, true);
        int cp = MiniSat.makeLiteral(c, true);
        int dp = MiniSat.makeLiteral(d, true);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{ap, bp, cp, dp})));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(c), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(d), MiniSat.lUndef);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.clauses.size(), 1);
    }

    @Test(groups = "1s")
    public void testAddClause4() {
        int a1 = MiniSat.makeLiteral(a, true);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{a1, a1, a1, a1})));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lTrue);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(c), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(d), MiniSat.lUndef);
        Assert.assertEquals(sat.qhead_, 1);
        Assert.assertEquals(sat.clauses.size(), 0);
    }

    @Test(groups = "1s")
    public void testAddClause5() {
        int a1 = MiniSat.makeLiteral(a, true);
        int a2 = MiniSat.makeLiteral(a, false);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{a1, a2})));
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(b), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(c), MiniSat.lUndef);
        Assert.assertEquals(sat.assignment_.get(d), MiniSat.lUndef);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.clauses.size(), 0);
    }

    @Test(groups = "1s")
    public void testAddClause6() {
        int ap = MiniSat.makeLiteral(a, true);
        sat.uncheckedEnqueue(ap);
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lTrue);
        int an = MiniSat.makeLiteral(a, false);
        Assert.assertFalse(sat.addClause(an));
        sat.propagate();
        Assert.assertEquals(sat.assignment_.get(a), MiniSat.lTrue);
        Assert.assertEquals(sat.qhead_, 1);
        Assert.assertFalse(sat.ok_);
    }

    @Test(groups = "1s")
    public void testInitPropagator() {
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        //Assert.assertEquals(sat.touched_variables_.size(), 1);
        //Assert.assertEquals(sat.touched_variables_.size(), 0);
    }
    

    @Test(groups = "1s")
    public void testSign() {
        Assert.assertTrue(MiniSat.sgn(3));
        Assert.assertTrue(MiniSat.sgn(1));
        Assert.assertFalse(MiniSat.sgn(0));
        Assert.assertFalse(MiniSat.sgn(2));

        int ta = MiniSat.makeLiteral(a, true);
        Assert.assertEquals(ta, 1);
        Assert.assertTrue(MiniSat.sgn(ta));

        int fa = MiniSat.makeLiteral(a, false);
        Assert.assertEquals(fa, 0);
        Assert.assertFalse(MiniSat.sgn(fa));
    }


    @Test(groups = "1s")
    public void testNumvars() {
        Assert.assertEquals(sat.num_vars_, 4);
    }

    @Test(groups = "1s")
    public void testAddClause_() {
        sat.addClause(new int[]{a, b}, new int[]{c, d});
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddTrue() {
        sat.addTrue(a);
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddFalse() {
        sat.addFalse(a);
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolEq() {
        sat.addBoolEq(a, b);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolLe1() {
        sat.addBoolLe(a, b);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolLe2() {
        sat.addBoolLe(a, b);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddBoolLe3() {
        sat.addBoolLe(a, b);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(b, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolLt() {
        sat.addBoolLt(a, b);
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolNot() {
        sat.addBoolNot(a, b);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolOrArrayEqVar1() {
        sat.addBoolOrArrayEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(d, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolOrArrayEqVar2() {
        sat.addBoolOrArrayEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolAndArrayEqVar1() {
        sat.addBoolAndArrayEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(d, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolAndArrayEqVar2() {
        sat.addBoolAndArrayEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(d, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolAndArrayEqVar3() {
        sat.addBoolAndArrayEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(b, true));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolOrEqVar1() {
        sat.addBoolOrEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolOrEqVar2() {
        sat.addBoolOrEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolOrEqVar3() {
        sat.addBoolOrEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(b, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lFalse);
    }

    @Test(groups = "1s")
    public void testAddBoolAndEqVar() {
        sat.addBoolAndEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolXorEqVar() {
        sat.addBoolXorEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolIsEqVar() {
        sat.addBoolIsEqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolIsNeqVar() {
        sat.addBoolIsNeqVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolIsLeVar() {
        sat.addBoolIsLeVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolIsLtVar() {
        sat.addBoolIsLtVar(a, b, c);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.uncheckedEnqueue(MiniSat.makeLiteral(c, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lTrue);
    }

    @Test(groups = "1s")
    public void testAddBoolOrArrayEqualTrue() {
        sat.addBoolOrArrayEqualTrue(a, b, c, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, true));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lTrue);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddBoolAndArrayEqualFalse() {
        sat.addBoolAndArrayEqualFalse(a, b, c, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddAtMostOne() {
        sat.addAtMostOne(a, b, c, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddAtMostNMinusOne() {
        sat.addAtMostNMinusOne(a, b, c, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddSumBoolArrayGreaterEqVar() {
        sat.addSumBoolArrayGreaterEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddMaxBoolArrayLessEqVar() {
        sat.addMaxBoolArrayLessEqVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }

    @Test(groups = "1s")
    public void testAddSumBoolArrayLessEqVar() {
        sat.addSumBoolArrayLessEqKVar(new int[]{a, b, c}, d);
        sat.uncheckedEnqueue(MiniSat.makeLiteral(a, false));
        sat.propagate();
        Assert.assertEquals(sat.valueVar(a), MiniSat.lFalse);
        Assert.assertEquals(sat.valueVar(b), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(c), MiniSat.lUndef);
        Assert.assertEquals(sat.valueVar(d), MiniSat.lUndef);
    }
}