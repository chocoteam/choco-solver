/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.view.integer.IntMinusView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexandre LEBRUN
 */
public class IntVarIntMinusViewTest extends IntVarTest {


    private IntVar original;

    @BeforeMethod(alwaysRun = true)
    @Override
    public void setup() {
        Model model = new Model();
        original = model.intVar(-4, -1);
        var = model.intMinusView(original);
    }

    @Override
    protected void domainIn(int lb, int ub) {
        super.domainIn(lb, ub);
        assertEquals(var.getLB(), -original.getUB());
        assertEquals(var.getUB(), -original.getLB());
        assertEquals(var.getDomainSize(), original.getDomainSize());
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testUpdateInfeasBounds() throws Exception {
        setup();
        IntMinusView mv = (IntMinusView) var;
        mv.updateBounds(3,2, Cause.Null);
    }
}
