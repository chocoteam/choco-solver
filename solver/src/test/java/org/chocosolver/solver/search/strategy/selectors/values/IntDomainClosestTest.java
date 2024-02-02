/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/07/2023
 */
public class IntDomainClosestTest {

    @Test(timeOut = 60000, groups = "1s", expectedExceptions = ContradictionException.class)
    public void testSelectValue1() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", new int[]{-6, -2, 0, 3, 5});
        int[] order = {0, -2, 3, 5, -6};
        IntDomainClosest sel = new IntDomainClosest();
        int i = 0;
        while (x.getDomainSize() > 0) {
            int val = sel.selectValue(x);
            Assert.assertEquals(val, order[i++]);
            x.removeValue(val, Cause.Null);
        }
    }

    @Test(timeOut = 60000, groups = "1s", expectedExceptions = ContradictionException.class)
    public void testSelectValue2() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", new int[]{-6, -2, 0, 3, 5});
        int[] order = {3, 0, 5, -2, -6};
        IntDomainClosest sel = new IntDomainClosest(2);
        int i = 0;
        while (x.getDomainSize() > 0) {
            int val = sel.selectValue(x);
            Assert.assertEquals(val, order[i++]);
            x.removeValue(val, Cause.Null);
        }
    }

}