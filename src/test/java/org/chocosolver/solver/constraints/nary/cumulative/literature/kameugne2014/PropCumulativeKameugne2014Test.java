/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.kameugne2014;

import org.chocosolver.solver.constraints.nary.cumulative.literature.AbstractCumulativeTest;
import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.constraints.nary.cumulative.literature.PropagatorCumulative;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public class PropCumulativeKameugne2014Test extends AbstractCumulativeTest {

    @Test(groups="1s", timeOut=60000)
    public void CumulativeNotFirstTest() {
        int[][] values = new int[][]{
                new int[]{0, 1, 2, 2, 3},
                new int[]{0, 1, 2, 2, 3},
                new int[]{0, 2, 1, 1, 3},
                new int[]{0, 3, 2, 2, 5}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);

        IntVar[] heights = model.intVarArray(4, 1, 1);
        IntVar capacity = model.intVar(2);

        PropCumulativeKameugne2014 prop = new PropCumulativeKameugne2014(tasks, heights, capacity, true);
        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, heights, capacity, prop);

        try {
            Assert.assertTrue(prop.notFirst());
        } catch(ContradictionException ex) {
            Assert.fail();
        }

        int[] afterProp = new int[]{0, 0, 0, 1};
        Assert.assertTrue(AbstractCumulativeTest.checkProp(tasks, afterProp));
    }

    public CumulativeFilter propagator(Task[] tasks, IntVar[] heights, IntVar capacity) {
        return new PropCumulativeKameugne2014(tasks, heights, capacity, true);
    }

}
