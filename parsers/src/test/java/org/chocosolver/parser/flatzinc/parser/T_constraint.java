/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_constraint extends GrammarTest {

    Model mSolver;
    Datas map;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        mSolver = new Model();
        map = new Datas();
    }

    @Test(groups = "1s")
    public void test1() throws IOException {
        map.register("x", mSolver.intVar("x", 0, 2, true));
        Flatzinc4Parser fp = parser("constraint int_le(0,x); % 0<= x\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        map.register("x", mSolver.intVar("x", 0, 2, true));
        map.register("y", mSolver.intVar("y", 0, 2, true));
        Flatzinc4Parser fp = parser("constraint int_lt(x,y); % x <= y\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
        Assert.assertTrue(c instanceof Arithmetic);
    }


    @Test(groups = "1s")
    public void test3() throws IOException {
        map.register("x", mSolver.intVar("x", 0, 2, true));
        map.register("y", mSolver.intVar("y", 0, 2, true));
        Flatzinc4Parser fp = parser("constraint int_lin_eq([2,3],[x,y],10); % 0<= x\n", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
//		// not even true (can be Arithmetic or Scalar)
//        Assert.assertTrue(c instanceof Sum);
    }

    @Test(groups = "1s")
    public void test4() throws IOException {
        map.register("q", mSolver.intVarArray("q", 2, 0, 2, true));
        Flatzinc4Parser fp = parser("constraint int_lin_eq([ 1, -1 ], [ q[1], q[2] ], -1);", mSolver, map);
        fp.constraint();
        Assert.assertEquals(mSolver.getCstrs().length, 1);
        Constraint c = mSolver.getCstrs()[0];
//		// not even true (can be Arithmetic or Scalar)
//        Assert.assertTrue(c instanceof Sum);
    }


}
