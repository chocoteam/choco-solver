/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.lcg;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.solver.variables.impl.IntVarEagerLit;
import org.chocosolver.solver.variables.view.bool.BoolEqView;
import org.chocosolver.solver.variables.view.bool.BoolGeqView;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.variables.IntVar.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/11/2023
 */
public class LitVarTests {

    @Test(groups = "{1s,lcg}")
    public void testBoolVar0() {
        Model model = new Model("LCG testBoolVar0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar b = model.boolVar("b");
        Assert.assertEquals(b.satVar(), 2);

        Assert.assertEquals(b.getLit(-42, LR_EQ), 0);
        Assert.assertEquals(b.getLit(-1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(0, LR_EQ), 4);
        Assert.assertEquals(b.getLit(1, LR_EQ), 5);
        Assert.assertEquals(b.getLit(2, LR_EQ), 0);
        Assert.assertEquals(b.getLit(42, LR_EQ), 0);

        Assert.assertEquals(b.getLit(-42, LR_NE), 1);
        Assert.assertEquals(b.getLit(-1, LR_NE), 1);
        Assert.assertEquals(b.getLit(0, LR_NE), 5);
        Assert.assertEquals(b.getLit(1, LR_NE), 4);
        Assert.assertEquals(b.getLit(2, LR_NE), 1);
        Assert.assertEquals(b.getLit(42, LR_NE), 1);

        Assert.assertEquals(b.getLit(-42, LR_GE), 1);
        Assert.assertEquals(b.getLit(-1, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), b.getLit(1, LR_EQ));
        Assert.assertEquals(b.getLit(2, LR_GE), 0);
        Assert.assertEquals(b.getLit(42, LR_GE), 0);

        Assert.assertEquals(b.getLit(-42, LR_LE), 0);
        Assert.assertEquals(b.getLit(-1, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), b.getLit(0, LR_EQ));
        Assert.assertEquals(b.getLit(1, LR_LE), 1);
        Assert.assertEquals(b.getLit(2, LR_LE), 1);
        Assert.assertEquals(b.getLit(42, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), b.getLit(-1, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(2, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testBoolVar1() throws ContradictionException {
        Model model = new Model("LCG testBoolVar1",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar b = model.boolVar("b");
        Assert.assertTrue(b.setToTrue(Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), b.getLit(0, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(2, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testBoolVar2() throws ContradictionException {
        Model model = new Model("LCG testBoolVar2",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar b = model.boolVar("b");
        Assert.assertTrue(b.setToFalse(Cause.Null));

        Assert.assertEquals(b.getValLit(), 5);
        Assert.assertEquals(b.getMinLit(), b.getLit(-1, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(1, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testBoolVar3() {
        Model model = new Model("LCG testBoolVar0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar a = model.boolVar("a");
        BoolVar b = a.not();
        Assert.assertEquals(b.satVar(), -3);

        Assert.assertEquals(b.getLit(-42, LR_EQ), 0);
        Assert.assertEquals(b.getLit(-1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(0, LR_EQ), 5);
        Assert.assertEquals(b.getLit(1, LR_EQ), 4);
        Assert.assertEquals(b.getLit(2, LR_EQ), 0);
        Assert.assertEquals(b.getLit(42, LR_EQ), 0);

        Assert.assertEquals(b.getLit(-42, LR_NE), 1);
        Assert.assertEquals(b.getLit(-1, LR_NE), 1);
        Assert.assertEquals(b.getLit(0, LR_NE), 4);
        Assert.assertEquals(b.getLit(1, LR_NE), 5);
        Assert.assertEquals(b.getLit(2, LR_NE), 1);
        Assert.assertEquals(b.getLit(42, LR_NE), 1);

        Assert.assertEquals(b.getLit(-42, LR_GE), 1);
        Assert.assertEquals(b.getLit(-1, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), b.getLit(1, LR_EQ));
        Assert.assertEquals(b.getLit(2, LR_GE), 0);
        Assert.assertEquals(b.getLit(42, LR_GE), 0);

        Assert.assertEquals(b.getLit(-42, LR_LE), 0);
        Assert.assertEquals(b.getLit(-1, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), b.getLit(0, LR_EQ));
        Assert.assertEquals(b.getLit(1, LR_LE), 1);
        Assert.assertEquals(b.getLit(2, LR_LE), 1);
        Assert.assertEquals(b.getLit(42, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), b.getLit(-1, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(2, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testBoolVar4() throws ContradictionException {
        Model model = new Model("LCG testBoolVar1",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar a = model.boolVar("a");
        BoolVar b = a.not();
        Assert.assertTrue(b.setToTrue(Cause.Null));

        Assert.assertEquals(b.getValLit(), 5);
        Assert.assertEquals(b.getMinLit(), b.getLit(0, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(2, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testBoolVar5() throws ContradictionException {
        Model model = new Model("LCG testBoolVar2",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        BoolVar a = model.boolVar("a");
        BoolVar b = a.not();
        Assert.assertTrue(b.setToFalse(Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), b.getLit(-1, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(1, LR_GE));
    }

    @DataProvider(name = "range")
    private Object[][] range() {
        return new Object[][]{
                {1, 2},
                {11, 12},
                {-2, -1},
                {-12, -11},
                {0, 1},
                {-1, 0}
        };
    }

    @Test(groups = "{1s,lcg}", dataProvider = "range")
    public void testIntVarEL0(int l, int u) {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", l, u, model));

        Assert.assertEquals(b.getLit(l - 100, LR_EQ), 0);
        Assert.assertEquals(b.getLit(l - 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(l, LR_EQ), 5);
        Assert.assertEquals(b.getLit(u, LR_EQ), 7);
        Assert.assertEquals(b.getLit(u + 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(u + 100, LR_EQ), 0);

        Assert.assertEquals(b.getLit(l - 100, LR_NE), 1);
        Assert.assertEquals(b.getLit(l - 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(l, LR_NE), 4);
        Assert.assertEquals(b.getLit(u, LR_NE), 6);
        Assert.assertEquals(b.getLit(u + 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(u + 100, LR_NE), 1);

        Assert.assertEquals(b.getLit(l - 100, LR_GE), 1);
        Assert.assertEquals(b.getLit(l - 1, LR_GE), 1);
        Assert.assertEquals(b.getLit(l, LR_GE), 9);
        Assert.assertEquals(b.getLit(u, LR_GE), 11);
        Assert.assertEquals(b.getLit(u + 1, LR_GE), 0);
        Assert.assertEquals(b.getLit(u + 100, LR_GE), 0);

        Assert.assertEquals(b.getLit(l - 100, LR_LE), 0);
        Assert.assertEquals(b.getLit(l - 1, LR_LE), 0);
        Assert.assertEquals(b.getLit(l, LR_LE), 10);
        Assert.assertEquals(b.getLit(u, LR_LE), 12);
        Assert.assertEquals(b.getLit(u + 1, LR_LE), 1);
        Assert.assertEquals(b.getLit(u + 100, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
        Assert.assertNotEquals(b.getLit(l - 1, LR_LE), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertNotEquals(b.getLit(u + 1, LR_GE), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "range")
    public void testIntVarEL1(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL1",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", l, u, model));
        Assert.assertTrue(b.instantiateTo(u, Cause.Null));

        Assert.assertEquals(b.getValLit(), 6);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(u, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "range")
    public void testIntVarEL2(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", l, u, model));
        Assert.assertTrue(b.instantiateTo(l, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(l, LR_LE)));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarLL0() {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 3, true);
        // those fail
//        Assert.assertEquals(b.getEQLit(-42), 0);
//        Assert.assertEquals(b.getEQLit(0), 0);
//        Assert.assertEquals(b.getEQLit(1), 5);
//        Assert.assertEquals(b.getEQLit(2), 7);
//        Assert.assertEquals(b.getEQLit(3), 0);
//        Assert.assertEquals(b.getEQLit(42), 0);

//        Assert.assertEquals(b.getNELit(-42), 1);
//        Assert.assertEquals(b.getNELit(0), 1);
//        Assert.assertEquals(b.getNELit(1), 4);
//        Assert.assertEquals(b.getNELit(2), 6);
//        Assert.assertEquals(b.getNELit(3), 1);
//        Assert.assertEquals(b.getNELit(42), 1);

        Assert.assertEquals(b.getLit(-42, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), 1);
        Assert.assertEquals(b.getLit(2, LR_GE), 7);
        Assert.assertEquals(b.getLit(3, LR_GE), 9);
        Assert.assertEquals(b.getLit(4, LR_GE), 0);
        Assert.assertEquals(b.getLit(42, LR_GE), 0);

        Assert.assertEquals(b.getLit(-42, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), 0);
        Assert.assertEquals(b.getLit(1, LR_LE), 6);
        Assert.assertEquals(b.getLit(2, LR_LE), 8);
        Assert.assertEquals(b.getLit(3, LR_LE), 1);
        Assert.assertEquals(b.getLit(4, LR_LE), 1);
        Assert.assertEquals(b.getLit(42, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), 0);
        Assert.assertEquals(b.getMaxLit(), 3);
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarLL1() throws ContradictionException {
        Model model = new Model("LCG testIntVarEL1",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 3, true);
        Assert.assertTrue(b.instantiateTo(1, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), 0);
        Assert.assertEquals(b.getMaxLit(), 7);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(1, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(1, LR_LE)));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarLL2() throws ContradictionException {
        Model model = new Model("LCG testIntVarEL2",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 3, true);
        Assert.assertTrue(b.instantiateTo(2, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), 6);
        Assert.assertEquals(b.getMaxLit(), 9);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(2, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(2, LR_LE)));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarLL3() throws ContradictionException {
        Model model = new Model("LCG testIntVarEL3",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 3, true);
        Assert.assertTrue(b.instantiateTo(3, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), 6);
        Assert.assertEquals(b.getMaxLit(), 3);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(3, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), 3);
    }

    @DataProvider(name = "holes")
    private Object[][] holes() {
        return new Object[][]{
                {10, 100},
                {-100, -10},
                {0, 100},
                {-100, 0},
                {-100, 100},
        };
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL0(int l, int u) {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));

        Assert.assertEquals(b.getLit(l - 100, LR_EQ), 0);
        Assert.assertEquals(b.getLit(l - 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(l, LR_EQ), 5);
        Assert.assertEquals(b.getLit(l + 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(u - 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(u, LR_EQ), 7);
        Assert.assertEquals(b.getLit(u + 1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(u + 100, LR_EQ), 0);

        Assert.assertEquals(b.getLit(l - 100, LR_NE), 1);
        Assert.assertEquals(b.getLit(l - 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(l, LR_NE), 4);
        Assert.assertEquals(b.getLit(l + 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(u - 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(u, LR_NE), 6);
        Assert.assertEquals(b.getLit(u + 1, LR_NE), 1);
        Assert.assertEquals(b.getLit(u + 100, LR_NE), 1);

        Assert.assertEquals(b.getLit(l - 100, LR_GE), 1);
        Assert.assertEquals(b.getLit(l - 1, LR_GE), 1);
        Assert.assertEquals(b.getLit(l, LR_GE), 9);
        Assert.assertEquals(b.getLit(l + 1, LR_GE), 11);
        Assert.assertEquals(b.getLit(u - 1, LR_GE), 11);
        Assert.assertEquals(b.getLit(u, LR_GE), 11);
        Assert.assertEquals(b.getLit(u + 1, LR_GE), 0);
        Assert.assertEquals(b.getLit(u + 100, LR_GE), 0);

        Assert.assertEquals(b.getLit(l - 100, LR_LE), 0);
        Assert.assertEquals(b.getLit(l - 1, LR_LE), 0);
        Assert.assertEquals(b.getLit(l, LR_LE), 10);
        Assert.assertEquals(b.getLit(l + 1, LR_LE), 10);
        Assert.assertEquals(b.getLit(u - 1, LR_LE), 10);
        Assert.assertEquals(b.getLit(u, LR_LE), 12);
        Assert.assertEquals(b.getLit(u + 1, LR_LE), 1);
        Assert.assertEquals(b.getLit(u + 100, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
        Assert.assertNotEquals(b.getLit(l - 1, LR_LE), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertNotEquals(b.getLit(u + 1, LR_GE), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL1(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.instantiateTo(u, Cause.Null));

        Assert.assertEquals(b.getValLit(), 6);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(u, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL2(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.instantiateTo(l, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(l, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL4(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.updateLowerBound(l + (u - l) / 2, Cause.Null));

        Assert.assertEquals(b.getValLit(), 6);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(u, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL5(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.updateUpperBound(l + (u - l) / 2, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(l, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL6(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.removeValue(l, Cause.Null));

        Assert.assertEquals(b.getValLit(), 6);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(u, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(u, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL7(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertTrue(b.removeValue(u, Cause.Null));

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(l, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(l, LR_LE)));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testIntVarSL8(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = new IntVarEagerLit(new BitsetIntVarImpl("b", new int[]{l, u}, model));
        Assert.assertFalse(b.removeValue(l + (u - l) / 2, Cause.Null));
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testBoolEqView0(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testBoolEqView0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar x = new IntVarEagerLit(new BitsetIntVarImpl("b", l, u, model));
        int t = l + (u - l) / 2;
        BoolVar b = new BoolEqView<>(x, t);
        Assert.assertEquals(b.getLit(-1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(0, LR_EQ), x.getLit(t, LR_NE));
        Assert.assertEquals(b.getLit(1, LR_EQ), x.getLit(t, LR_EQ));
        Assert.assertEquals(b.getLit(2, LR_EQ), 0);

        Assert.assertEquals(b.getLit(-1, LR_NE), 1);
        Assert.assertEquals(b.getLit(0, LR_NE), x.getLit(t, LR_EQ));
        Assert.assertEquals(b.getLit(1, LR_NE), x.getLit(t, LR_NE));
        Assert.assertEquals(b.getLit(2, LR_NE), 1);

        Assert.assertEquals(b.getLit(-1, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), x.getLit(t, LR_EQ));
        Assert.assertEquals(b.getLit(2, LR_GE), 0);

        Assert.assertEquals(b.getLit(-1, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), x.getLit(t, LR_NE));
        Assert.assertEquals(b.getLit(1, LR_LE), 1);
        Assert.assertEquals(b.getLit(2, LR_LE), 1);
    }

    @Test(groups = "{1s,lcg}", dataProvider = "holes")
    public void testBoolGeqView0(int l, int u) throws ContradictionException {
        Model model = new Model("LCG testBoolLeqView0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar x = new IntVarEagerLit(new BitsetIntVarImpl("b", l, u, model));
        int t = l + (u - l) / 2;
        BoolVar b = new BoolGeqView<>(x, t);
        Assert.assertEquals(b.getLit(-1, LR_EQ), 0);
        Assert.assertEquals(b.getLit(0, LR_EQ), x.getLit(t - 1, LR_LE));
        Assert.assertEquals(b.getLit(1, LR_EQ), x.getLit(t, LR_GE));
        Assert.assertEquals(b.getLit(2, LR_EQ), 0);

        Assert.assertEquals(b.getLit(-1, LR_NE), 1);
        Assert.assertEquals(b.getLit(0, LR_NE), x.getLit(t, LR_GE));
        Assert.assertEquals(b.getLit(1, LR_NE), x.getLit(t - 1, LR_LE));
        Assert.assertEquals(b.getLit(2, LR_NE), 1);

        Assert.assertEquals(b.getLit(-1, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), x.getLit(t, LR_GE));
        Assert.assertEquals(b.getLit(2, LR_GE), 0);

        Assert.assertEquals(b.getLit(-1, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), x.getLit(t - 1, LR_LE));
        Assert.assertEquals(b.getLit(1, LR_LE), 1);
        Assert.assertEquals(b.getLit(2, LR_LE), 1);
    }


}
