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
import org.testng.Assert;
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
        b.setToTrue(Cause.Null);

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
        b.setToFalse(Cause.Null);

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
        Assert.assertEquals(b.satVar(), -2);

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
        b.setToTrue(Cause.Null);

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
        b.setToFalse(Cause.Null);

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), b.getLit(-1, LR_LE));
        Assert.assertEquals(b.getMaxLit(), b.getLit(1, LR_GE));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarEL0() {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 2);

        Assert.assertEquals(b.getLit(-42, LR_EQ), 0);
        Assert.assertEquals(b.getLit(0, LR_EQ), 0);
        Assert.assertEquals(b.getLit(1, LR_EQ), 5);
        Assert.assertEquals(b.getLit(2, LR_EQ), 7);
        Assert.assertEquals(b.getLit(3, LR_EQ), 0);
        Assert.assertEquals(b.getLit(42, LR_EQ), 0);

        Assert.assertEquals(b.getLit(-42, LR_NE), 1);
        Assert.assertEquals(b.getLit(0, LR_NE), 1);
        Assert.assertEquals(b.getLit(1, LR_NE), 4);
        Assert.assertEquals(b.getLit(2, LR_NE), 6);
        Assert.assertEquals(b.getLit(3, LR_NE), 1);
        Assert.assertEquals(b.getLit(42, LR_NE), 1);

        Assert.assertEquals(b.getLit(-42, LR_GE), 1);
        Assert.assertEquals(b.getLit(0, LR_GE), 1);
        Assert.assertEquals(b.getLit(1, LR_GE), 9);
        Assert.assertEquals(b.getLit(2, LR_GE), 11);
        Assert.assertEquals(b.getLit(3, LR_GE), 0);
        Assert.assertEquals(b.getLit(42, LR_GE), 0);

        Assert.assertEquals(b.getLit(-42, LR_LE), 0);
        Assert.assertEquals(b.getLit(0, LR_LE), 0);
        Assert.assertEquals(b.getLit(1, LR_LE), 10);
        Assert.assertEquals(b.getLit(2, LR_LE), 12);
        Assert.assertEquals(b.getLit(3, LR_LE), 1);
        Assert.assertEquals(b.getLit(42, LR_LE), 1);

        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(1, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(2, LR_LE)));
        Assert.assertNotEquals(b.getLit(0, LR_LE), MiniSat.neg(b.getLit(1, LR_GE)));
        Assert.assertNotEquals(b.getLit(3, LR_GE), MiniSat.neg(b.getLit(2, LR_LE)));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarEL1() throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 2);
        b.instantiateTo(2, Cause.Null);

        Assert.assertEquals(b.getValLit(), 6);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(2, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(2, LR_LE)));
    }

    @Test(groups = "{1s,lcg}")
    public void testIntVarEL2() throws ContradictionException {
        Model model = new Model("LCG testIntVarEL0",
                Settings.init().setLCG(true)
                        .setWarnUser(true));
        IntVar b = model.intVar("a", 1, 2);
        b.instantiateTo(1, Cause.Null);

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(1, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), MiniSat.neg(b.getLit(1, LR_LE)));
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
        b.instantiateTo(1, Cause.Null);

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
        b.instantiateTo(2, Cause.Null);

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
        b.instantiateTo(3, Cause.Null);

        Assert.assertEquals(b.getValLit(), 4);
        Assert.assertEquals(b.getMinLit(), 6);
        Assert.assertEquals(b.getMaxLit(), 3);
        Assert.assertEquals(b.getMinLit(), MiniSat.neg(b.getLit(3, LR_GE)));
        Assert.assertEquals(b.getMaxLit(), 3);
    }

}
