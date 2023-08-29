/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Cause;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

    @Test(groups = "1s", timeOut = 60000)
    public void testInstZero() {
        Model m = new Model();
        IntVar x = m.intVar(0);
        IntVar y = m.intVar(-5, 5, false);
        IntVar z = m.intVar(-5, 5, true);
        m.pow(y, 3, x).post();
        m.pow(z, 4, x).post();

        try {
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        assertTrue(y.isInstantiatedTo(0));
        assertTrue(z.isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testInst() {
        Model m = new Model();
        IntVar x1 = m.intVar(16);
        IntVar y1 = m.intVar(-5, 5, false);
        IntVar z1 = m.intVar(-5, 5, true);
        m.pow(y1, 4, x1).post();
        m.pow(z1, 4, x1).post();

        IntVar x2 = m.intVar(-50, 50, false);
        IntVar y2 = m.intVar(-50, 50, true);
        IntVar z2 = m.intVar(-2);
        m.pow(z2, 4, x2).post();
        m.pow(z2, 4, y2).post();

        IntVar x3 = m.intVar(8);
        IntVar y3 = m.intVar(-5, 5, false);
        IntVar z3 = m.intVar(-5, 5, true);
        m.pow(y3, 3, x3).post();
        m.pow(z3, 3, x3).post();

        IntVar x4 = m.intVar(-50, 50, false);
        IntVar y4 = m.intVar(-50, 50, true);
        IntVar z4 = m.intVar(-2);
        m.pow(z4, 3, x4).post();
        m.pow(z4, 3, y4).post();

        try {
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }

        assertEquals(y1.getLB(), -2);
        assertEquals(y1.getUB(), 2);
        assertEquals(y1.getDomainSize(), 2);
        assertEquals(z1.getLB(), -2);
        assertEquals(z1.getUB(), 2);
        assertEquals(z1.getDomainSize(), 5);

        assertTrue(x2.isInstantiatedTo(16));
        assertTrue(y2.isInstantiatedTo(16));

        assertTrue(y3.isInstantiatedTo(2));
        assertTrue(z3.isInstantiatedTo(2));

        assertTrue(x4.isInstantiatedTo(-8));
        assertTrue(y4.isInstantiatedTo(-8));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPropHoles() {
        Model m = new Model();

        IntVar x = m.intVar(-257, 257, false);
        IntVar y = m.intVar(-4, 4, false);
        m.pow(y, 4, x).post();

        IntVar x2 = m.intVar(0, 126, false);
        IntVar y2 = m.intVar(-2, 4, false);
        m.pow(y2, 3, x2).post();

        try {
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }

        assertEquals(x.getLB(), 0);
        assertEquals(x.getUB(), 256);
        for (int j : new int[]{0, 1, 16, 81, 256}) {
            assertTrue(x.contains(j));
        }
        assertEquals(x.getDomainSize(), 5);
        assertEquals(y.getLB(), -4);
        assertEquals(y.getUB(), 4);
        assertEquals(y.getDomainSize(), 9);

        assertEquals(x2.getLB(), 0);
        assertEquals(x2.getUB(), 64);
        for (int j : new int[]{0, 1, 8, 27, 64}) {
            assertTrue(x2.contains(j));
        }
        assertEquals(x2.getDomainSize(), 5);
        assertEquals(y2.getLB(), 0);
        assertEquals(y2.getUB(), 4);
        assertEquals(y2.getDomainSize(), 5);

        try {
            x.removeValue(16, Cause.Null);
            y.removeValue(1, Cause.Null);
            x2.removeValue(27, Cause.Null);
            y2.removeValue(1, Cause.Null);
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        assertEquals(x.getLB(), 0);
        assertEquals(x.getUB(), 256);
        for (int j : new int[]{0, 1, 81, 256}) {
            assertTrue(x.contains(j));
        }
        assertEquals(x.getDomainSize(), 4);
        assertEquals(y.getLB(), -4);
        assertEquals(y.getUB(), 4);
        assertEquals(y.getDomainSize(), 6);

        assertEquals(x2.getLB(), 0);
        assertEquals(x2.getUB(), 64);
        for (int j : new int[]{0, 8, 64}) {
            assertTrue(x2.contains(j));
        }
        assertEquals(x2.getDomainSize(), 3);
        assertEquals(y2.getLB(), 0);
        assertEquals(y2.getUB(), 4);
        assertEquals(y2.getDomainSize(), 3);
        for (int j : new int[]{0, 2, 4}) {
            assertTrue(y2.contains(j));
        }

        try {
            y.removeValue(-1, Cause.Null);
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        assertEquals(x.getLB(), 0);
        assertEquals(x.getUB(), 256);
        for (int j : new int[]{0, 81, 256}) {
            assertTrue(x.contains(j));
        }
        assertEquals(x.getDomainSize(), 3);
        assertEquals(y.getLB(), -4);
        assertEquals(y.getUB(), 4);
        assertEquals(y.getDomainSize(), 5);
    }

    @DataProvider
    public static Object[][] domainType() {
        return new Object[][]{{true, true}, {true, false}, {false, true}, {false, false}};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "domainType")
    public void testPropBoundsOdd(boolean[] boundedDomains) {
        Model m = new Model();
        IntVar x = m.intVar(-9, 65, boundedDomains[0]);
        IntVar y = m.intVar(-5, 5, boundedDomains[1]);
        m.pow(y, 3, x).post();

        IntVar x2 = m.intVar(-65, -9, boundedDomains[0]);
        IntVar y2 = m.intVar(-5, 5, boundedDomains[1]);
        m.pow(y2, 3, x2).post();

        IntVar x3 = m.intVar(9, 65, boundedDomains[0]);
        IntVar y3 = m.intVar(-5, 5, boundedDomains[1]);
        m.pow(y3, 3, x3).post();

        IntVar[][] varsToCheck = new IntVar[][]{{x, y}, {x2, y2}, {x3, y3}};
        int[][] expectedLb = new int[][]{{-8, -2}, {-64, -4}, {27, 3}};
        int[][] expectedUb = new int[][]{{64, 4}, {-27, -3}, {64, 4}};
        int[][] domainSizes = new int[][]{{7, 7}, {2, 2}, {2, 2}};

        try {
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }

        int size;
        for (int i = 0; i < varsToCheck.length; i++) {
            for (int j = 0; j < varsToCheck[i].length; j++) {
                assertEquals(varsToCheck[i][j].getLB(), expectedLb[i][j]);
                assertEquals(varsToCheck[i][j].getUB(), expectedUb[i][j]);
                if (boundedDomains[j]) {
                    size = varsToCheck[i][j].getUB() - varsToCheck[i][j].getLB() + 1;
                } else {
                    size = domainSizes[i][j];
                }
                assertEquals(varsToCheck[i][j].getDomainSize(), size);
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "domainType")
    public void testPropBoundsEven(boolean[] boundedDomains) {
        Model m = new Model();
        IntVar x = m.intVar(-15, 257, boundedDomains[0]);
        IntVar y = m.intVar(-5, 5, boundedDomains[1]);
        m.pow(y, 4, x).post();

        IntVar x2 = m.intVar(15, 257, boundedDomains[0]);
        IntVar y2 = m.intVar(-5, 5, boundedDomains[1]);
        m.pow(y2, 4, x2).post();

        IntVar x3 = m.intVar(15, 257, boundedDomains[0]);
        IntVar y3 = m.intVar(0, 5, boundedDomains[1]);
        m.pow(y3, 4, x3).post();

        IntVar[][] varsToCheck = new IntVar[][]{{x, y}, {x2, y2}, {x3, y3}};
        int[][] expectedLb = new int[][]{{0, -4}, {16, -4}, {16, 2}};
        int[][] expectedUb = new int[][]{{256, 4}, {256, 4}, {256, 4}};
        int[][] domainSizes = new int[][]{{5, 9}, {3, 6}, {3, 3}};

        try {
            m.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }

        int size;
        for (int i = 0; i < varsToCheck.length; i++) {
            for (int j = 0; j < varsToCheck[i].length; j++) {
                assertEquals(varsToCheck[i][j].getLB(), expectedLb[i][j]);
                assertEquals(varsToCheck[i][j].getUB(), expectedUb[i][j]);
                if (boundedDomains[j]) {
                    size = varsToCheck[i][j].getUB() - varsToCheck[i][j].getLB() + 1;
                } else {
                    size = domainSizes[i][j];
                }
                assertEquals(varsToCheck[i][j].getDomainSize(), size);
            }
        }
    }
}
