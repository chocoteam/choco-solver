/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Jean-Guillaume Fages
 */
public class SquareTest {

    @Test(groups="1s", timeOut=60000)
    public void testCst() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(2);
        m.square(x,y).post();
        while(m.getSolver().solve());
        assertEquals(m.getSolver().getSolutionCount(),1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCstNeg() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(-2);
        m.square(x,y).post();
        while(m.getSolver().solve());
        assertEquals(m.getSolver().getSolutionCount(),1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSimple() {
        Model m = new Model();
        IntVar x = m.intVar(4);
        IntVar y = m.intVar(-2,2);
        m.square(x,y).post();
        while(m.getSolver().solve());
        assertEquals(m.getSolver().getSolutionCount(),2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCstFail() {
        Model m = new Model();
        IntVar x = m.intVar(2);
        IntVar y = m.intVar(2);
        m.square(x,y).post();
        while(m.getSolver().solve());
        assertEquals(m.getSolver().getSolutionCount(),0);
    }

    @Test(groups="1s", timeOut=60000)
    public void test() {
        Model m = new Model();
        IntVar x = m.intVar(0,4);
        IntVar y = m.intVar(-2,2);
        m.square(x,y).post();
        while(m.getSolver().solve());
        assertEquals(m.getSolver().getSolutionCount(),5);
    }
}
