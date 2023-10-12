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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.util.ESat.TRUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/06/13
 */
public class EqTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        Model s = new Model();
        IntVar two1 = s.intVar(2);
        IntVar two2 = s.intVar(2);
        s.arithm(two1, "=", two2).post();
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }


    @Test(groups = "1s", timeOut = 60000)
    public void test2() {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "-", two, "=", 1).post();
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "=", two, "+", 1).post();
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }

    @Test(groups = "1s")
    public void testJBS() {
        Model s = new Model();
        IntVar three = s.intVar("X", 0, 2000);
        IntVar two = s.intVar("Y", 0, 2000);
        IntVar one = s.intVar("Z", 0, 2000);
        s.arithm(s.offset(three, 3), "+", s.intView(1, two, -2), "=", 1000).post();
        s.arithm(s.offset(one, 10), "+", s.intView(1, two, -8), "=", 999).post();
        s.ifThen(three.ge(10).boolVar(),s.mod(two, 2, 1));
        s.getSolver().findAllSolutions();
        assertEquals(503, s.getSolver().getSolutionCount());
    }
}
