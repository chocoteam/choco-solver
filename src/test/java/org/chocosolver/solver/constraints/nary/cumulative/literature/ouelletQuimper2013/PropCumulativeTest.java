/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.ouelletQuimper2013;

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
        return new PropCumulative(tasks, heights, capacity, true, true, false);
    }

    @Test
    public void testFilterTimeTabling() throws ContradictionException {
        int[][] values = new int[][]{
                new int[]{0, 6, 6, 6, 12}, // A
                new int[]{1, 4, 3, 4, 7}, // B
                new int[]{2, 4, 2, 4, 6}, // C
                new int[]{3, 6, 4, 7, 10} // D
        };
        int[] heights = new int[]{2, 2, 1, 1};
        int capacity = 3;

        Model model = new Model();
        Task[] tasks = buildTasks(values, model);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = model.intVar(heights[i]);
        }
        IntVar cap = model.intVar(capacity);
        PropCumulative prop = new PropCumulative(tasks, heightsVar, cap, true, true, true);
        PropCumulative propOpp = new PropCumulative(buildOpposite(tasks), heightsVar, cap, true, true, true);

        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, heightsVar, cap, prop, propOpp);

        prop.timeTableExtendedEdgeFinding();
        Assert.assertEquals(tasks[0].getStart().getLB(), 3);

        propOpp.timeTable();
        Assert.assertEquals(tasks[1].getEnd().getUB(), 6);
    }
}
