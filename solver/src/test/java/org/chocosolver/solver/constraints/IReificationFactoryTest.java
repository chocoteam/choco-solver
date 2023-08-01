/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 14/11/2018.
 */
public class IReificationFactoryTest {

    @Test(groups = "1s")
    public void testReifyXeqC() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        BoolVar B = m.boolVar();
        m.reifyXeqC(X, 3, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.isInstantiatedTo(3));
        }
    }

    @Test(groups = "1s")
    public void testReifyXneC() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        BoolVar B = m.boolVar();
        m.reifyXneC(X, 3, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1) | X.isInstantiatedTo(3));
        }
    }

    @DataProvider(name = "two")
    public Object[][] domains() {
        return new int[][][]{
                {{0, 5}, {0, 5}},
                {{0, 5}, {3, 3}},
                {{3, 3}, {0, 5}},
        };
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXeqY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXeqY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() == Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXneY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXneY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() != Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXeqYC(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXeqYC(X, Y, 1, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() == Y.getValue() + 1,
                    B.getValue() + " : " + X.getValue() + " = " + Y.getValue() + "+1");
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXneYC(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXneYC(X, Y, 1, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() != Y.getValue() + 1);
        }
    }

    @Test(groups = "1s")
    public void testReifyXltC() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        BoolVar B = m.boolVar();
        m.reifyXltC(X, 3, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() < 3);
        }
    }

    @Test(groups = "1s")
    public void testReifyXgtC() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        BoolVar B = m.boolVar();
        m.reifyXgtC(X, 3, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() > 3);
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXltY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXltY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() < Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXgtY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXgtY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() > Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXleY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXleY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() <= Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXgeY(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXgeY(X, Y, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() >= Y.getValue());
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXltYC(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXltYC(X, Y, 1, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() < Y.getValue() + 1);
        }
    }

    @Test(groups = "1s", dataProvider = "two")
    public void testReifyXgtYC(int[] xx, int[] yy) {
        Model m = new Model();
        IntVar X = m.intVar(xx[0], xx[1]);
        IntVar Y = m.intVar(yy[0], yy[1]);
        BoolVar B = m.boolVar();
        m.reifyXgtYC(X, Y, 1, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | X.getValue() > Y.getValue() + 1);
        }
    }

    @Test(groups = "1s")
    public void testReifyXinS() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        IntIterableRangeSet S = new IntIterableRangeSet(2, 3);
        BoolVar B = m.boolVar();
        m.reifyXinS(X, S, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | S.contains(X.getValue()));
        }
    }

    @Test(groups = "1s")
    public void testReifyXnotinS() {
        Model m = new Model();
        IntVar X = m.intVar(0, 5);
        IntIterableRangeSet S = new IntIterableRangeSet(2, 3);
        BoolVar B = m.boolVar();
        m.reifyXnotinS(X, S, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0) | !S.contains(X.getValue()));
        }
    }

    @Test(groups = "1s")
    public void testReifyXgtY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXgtY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXgtY2() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXgtYC(X, X, 2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXgtY3() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXgtYC(X, X, -2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXgeY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXgeY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXltY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXltY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXltY2() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXltYC(X, X, 2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXltY3() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXltYC(X, X, -2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXleY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXleY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXeqY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXeqY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXeqY2() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXeqYC(X, X, 2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXeqY3() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXeqYC(X, X, -2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXneY1() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXneY(X, X, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(0));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXneY2() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXneYC(X, X, 2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testReifyXneY3() {
        Model m = new Model();
        IntVar X = m.intVar(0, 3);
        BoolVar B = m.boolVar();
        m.reifyXneYC(X, X, -2, B);
        while (m.getSolver().solve()) {
            Assert.assertTrue(B.isInstantiatedTo(1));
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), 4);
    }
}