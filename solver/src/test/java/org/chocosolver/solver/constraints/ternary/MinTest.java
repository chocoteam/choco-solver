/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/07/12
 */
public class MinTest extends AbstractTernaryTest {

    @Override
    protected int validTuple(int vx, int vy, int vz) {
        return (vx == Math.min(vy, vz)) ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model model) {
        return model.min(vars[0], vars[1], vars[2]);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[4];
        IntVar min = model.intVar(-5);
        X[0] = model.intVar(-3);
        X[1] = model.intVar("1", -4, -3, true);
        X[2] = model.intVar("2", -5, -2, true);
        X[3] = model.intVar(-3);

        model.min(min, X).post();
        model.getSolver().propagate();
        assertEquals(X[2].getUB(), -5);
    }
}
