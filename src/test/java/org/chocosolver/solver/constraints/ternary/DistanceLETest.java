/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
public class DistanceLETest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return Math.abs(vx-vy) <=  vz ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.distance2(vars[0], vars[1], "<=", vars[2]);
    }
     
    @Test(groups="1s", timeOut=60000)
    public void test2() throws ContradictionException {
    	Model model = new Model();
    	IntVar x = model.intVar("x", -10, 0); 
    	IntVar y = model.intVar("y", -20, -10);
    	IntVar z = model.intVar("z", -5, 5);
    	Constraint ct = model.distance2(x, y, "<=", z);
    	model.post(ct);
    	model.getSolver().propagate();
    	assertDomainIn(x, -10, -5);
    	assertDomainIn(y, -15,-10);
    	assertDomainIn(z, 0, 5);
    }
    
}
