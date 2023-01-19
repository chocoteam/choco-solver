/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/02/2022
 */
public class PowTest extends AbstractBinaryTest {
    private int TEST_VALUE = 5; // TODO How to change this dependency to use AbstractBinaryTest

    @DataProvider
    public static Object[][] even() {
        return new Object[][]{{2}, {4}, {6}, {8}, {10}};
    }

    @DataProvider
    public static Object[][] odd() {
        return new Object[][]{{1}, {3}, {5}, {7}, {9}};
    }

    @Override
    protected int validTuple(int vx, int vy) {
        return Math.pow(vx, TEST_VALUE) == vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model s) {
        return s.pow(vars[0], TEST_VALUE, vars[1]);
    }

    @Test(groups = "1s", dataProvider = "even")
    public void testEven(int exp) {
        TEST_VALUE = exp;
        super.test1();
        super.test2();
    }

    @Test(groups = "1s", dataProvider = "odd")
    public void testOdd(int exp) {
        TEST_VALUE = exp;
        super.test1();
        super.test2();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJL() {
        Model s = new Model();
        IntVar dividend = s.intVar("dividend", 2, 3, false);
        int divisor = 1;
        IntVar remainder = s.intVar("remainder", 1, 2, false);
        s.pow(dividend, divisor, remainder).getOpposite().post();
        Solver r = s.getSolver();
        r.setSearch(inputOrderLBSearch(dividend, remainder));
        s.getSolver().solve();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJT1() {
        Model model = new Model("model");
        IntVar a = model.intVar("a", 2, 6);
        int b = 2;
        IntVar c = model.intVar("c", 5, 30);
        model.pow(a, b, c).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testJT2() {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar a = model.intVar("a", 2, 6);
        int b = 2;
        IntVar c = model.intVar("c", 5, 30);
        model.pow(a, b, c).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMod2Var() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0, 9);
        IntVar z = model.intVar("z", 0, 9);
        model.pow(x, 2, z).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 4);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMod2Var1() {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", 0, 9);
        IntVar z = model.intVar("z", 0, 9);
        model.pow(x, 2, z).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 4);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMod2VarNegValues() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.intVar("z", -5, 5);
        model.pow(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 3);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMod2VarNegValues2() throws ContradictionException {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", -5, 5);
        IntVar z = model.intVar("z", -5, 5);
        model.pow(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertEquals(z.getDomainSize(), 3);
        Assert.assertEquals(x.getDomainSize(), 3);
        model.getSolver().showSolutions();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMod2VarsZeroPow() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0, 9);
        IntVar y = model.intVar("y", 0, 9);
        model.pow(x, 0, y).post();
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMod2VarsZeroPow2() {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", 0, 9);
        IntVar y = model.intVar("y", 0, 9);
        model.pow(x, 0, y).post();
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMod2VarsNegPow() {
        Model model = new Model("model");
        IntVar x = model.intVar("x", -9, 9);
        IntVar y = model.intVar("y", -9, 9);
        model.pow(x, -2, y).post();
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMod2VarsNegPow2() {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", -9, 9);
        IntVar y = model.intVar("y", -9, 9);
        model.pow(x, -2, y).post();
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testMod2VarsBig() {
        Model model = new Model("model", Settings.init().setEnableTableSubstitution(false));
        IntVar x = model.intVar("x", 2, 60);
        IntVar y = model.intVar("y", 0, 999_999);
        model.pow(x, 7, y).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 6);
    }
}
