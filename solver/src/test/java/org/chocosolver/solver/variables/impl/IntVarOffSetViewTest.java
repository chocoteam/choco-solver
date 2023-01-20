/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexandre LEBRUN
 */
public class IntVarOffSetViewTest extends IntVarTest {


    private IntVar original;

    @BeforeMethod(alwaysRun = true)
    @Override
    public void setup() {
        Model model = new Model();
        original = model.intVar(0, 3);
        var = model.intOffsetView(original, 1);
    }

    @Override
    protected void domainIn(int lb, int ub) {
        super.domainIn(lb, ub);
        assertEquals(var.getLB(), original.getLB() + 1);
        assertEquals(var.getUB(), original.getUB() + 1);
        assertEquals(var.getDomainSize(), original.getDomainSize());
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdateInfeasBounds1() throws Exception {
        setup();
        var.updateBounds(4, 1, Cause.Null);
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdateInfeasBounds2() throws Exception {
        setup();
        var.updateBounds(5, 4, Cause.Null);
    }


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdateInfeasBounds3() throws Exception {
        setup();
        var.updateBounds(1, -1, Cause.Null);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testUpdateInfeasBounds4() throws Exception {
        setup();
        var.updateBounds(5, 0, Cause.Null);
    }
}
