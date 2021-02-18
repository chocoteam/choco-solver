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

import static org.chocosolver.solver.constraints.ternary.DistanceEQTest.assertDomainIn;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

/**
 * @author Arnaud Malapert
 * @since 10/30/2017
 *
 */
public class DistanceGTTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return Math.abs(vx-vy) >  vz ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.distance(vars[0], vars[1], ">", vars[2]);
    }
     
    @Test(groups="1s", timeOut=60000)
    public void test2() throws ContradictionException {
    	Model model = new Model();
    	IntVar x = model.intVar("x", -1, 5); 
    	IntVar y = model.intVar("y", -3, 3);
    	IntVar z = model.intVar("z", 5, 10);
    	Constraint ct = model.distance(x, y, ">", z);
    	model.post(ct);
    	model.getSolver().propagate();
    	assertDomainIn(x, 3, 5);
    	assertDomainIn(y, -3, -1);
    	assertDomainIn(z, 5, 7);
    }
    
}
