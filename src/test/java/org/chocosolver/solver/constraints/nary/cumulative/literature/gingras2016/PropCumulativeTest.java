/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.gingras2016;

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
public class PropCumulativeTest extends AbstractCumulativeTest {

    @Override
    public CumulativeFilter propagator(Task[] tasks, IntVar[] heights, IntVar capacity) {
        return new PropCumulative(tasks, heights, capacity, true, false);
    }

    @Test
    public void testOverloadCheck() {
        int[][] values = new int[][]{
                new int[]{0, 2, 2, 2, 4},
                new int[]{1, 3, 1, 2, 4},
                new int[]{1, 3, 1, 2, 4},
                new int[]{1, 3, 1, 2, 4}
        };
        int[] heights = new int[]{1, 2, 2, 2};
        int capacity = 2;

        Model model = new Model();
        Task[] tasks = buildTasks(values, model);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = model.intVar(heights[i]);
        }
        IntVar cap = model.intVar(capacity);
        PropCumulative prop = (PropCumulative) propagator(tasks, heightsVar, cap);
        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, heightsVar, cap, prop);

        try {
            prop.overloadCheck();
            Assert.fail();
        } catch(ContradictionException ex) {

        }
    }

}
