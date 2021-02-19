/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
 * @since 13/10/2020
 */
public class IntDomainMedianTest {

    @Test(groups = "1s")
    public void test1() throws ContradictionException {
        Model model = new Model();
        IntVar var = model.intVar(new int[]{0,3,7,9,44});
        IntDomainMedian med = new IntDomainMedian();
        Assert.assertEquals(med.selectValue(var), 7);
        var.updateUpperBound(10, Cause.Null);
        Assert.assertEquals(med.selectValue(var), 3);
    }

}