/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import static org.testng.Assert.assertEquals;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.annotations.Test;

/**
 * 
 * @author Arnaud Malapert
 * @since 10/30/2017
 *
 */
public class DistanceEQTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return Math.abs(vx-vy) ==  vz ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.distance(vars[0], vars[1], "=", vars[2]);
    }
    
    public final static void assertDomainIn(IntVar v, int lb, int ub) {
    	assertEquals(v.getLB(), lb, "Invalid lower bound");
    	assertEquals(v.getUB(), ub, "Invalid upper bound");
    }
    
    @Test(groups="1s", timeOut=60000)
    public void test2() throws ContradictionException {
    	Model model = new Model();
    	IntVar x = model.intVar("x", 0, 10); 
    	IntVar y = model.intVar("y", 12, 20);
    	IntVar z = model.intVar("z", 0, 5);
    	Constraint ct = model.distance(x, y, "=", z);
    	model.post(ct);
    	model.getSolver().propagate();
    	assertDomainIn(x, 7, 10);
    	assertDomainIn(y, 12, 15);
    	assertDomainIn(z, 2, 5);
    }
    
}
