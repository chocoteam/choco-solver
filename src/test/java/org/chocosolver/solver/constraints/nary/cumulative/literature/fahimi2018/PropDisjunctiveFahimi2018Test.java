/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.fahimi2018;

import org.chocosolver.solver.constraints.nary.cumulative.literature.AbstractCumulativeTest;
import org.chocosolver.solver.constraints.nary.cumulative.literature.AbstractDisjunctiveTest;
import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.constraints.nary.cumulative.literature.PropagatorCumulative;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public class PropDisjunctiveFahimi2018Test extends AbstractDisjunctiveTest {

    @Test
    public void DetectablePrecedencesTest() {
        int[][] values = new int[][]{
                new int[]{0, 15, 4, 4, 19},
                new int[]{2, 13, 9, 11, 22},
                new int[]{9, 23, 7, 16, 30},
                new int[]{12, 14, 6, 18, 20}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);

        PropDisjunctiveFahimi2018 prop = new PropDisjunctiveFahimi2018(tasks, false, false, true);
        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, prop);

        try {
            Assert.assertTrue(prop.edgeFinding());
        } catch(ContradictionException ex) {
            Assert.fail();
        }

        int[] afterProp = new int[]{0, 2, 19, 13};
        Assert.assertTrue(AbstractCumulativeTest.checkProp(tasks, afterProp));
    }

    @Test
    public void TimeTableTest() {
        int[][] values = new int[][]{
                new int[]{0, 1, 4, 4, 5},
                new int[]{1, 5, 1, 2, 6}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);

        PropDisjunctiveFahimi2018 prop = new PropDisjunctiveFahimi2018(tasks, false, true, false);
        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, prop);

        try {
            Assert.assertTrue(prop.timeTable());
        } catch(ContradictionException ex) {
            Assert.fail();
        }

        int[] afterProp = new int[]{0, 4};
        Assert.assertTrue(AbstractCumulativeTest.checkProp(tasks, afterProp));
    }

    @Test(expectedExceptions = ContradictionException.class)
    public void OverloadCheckTest() throws ContradictionException {
        int[][] values = new int[][]{
                new int[]{0, 1, 3, 3, 4},
                new int[]{1, 2, 3, 4, 5}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);

        PropDisjunctiveFahimi2018 prop = new PropDisjunctiveFahimi2018(tasks, true, false, false);
        PropagatorCumulative propagatorCumulative = new PropagatorCumulative(tasks, prop);

        prop.overloadCheck();
    }

    public CumulativeFilter propagator(Task[] tasks) {
        return new PropDisjunctiveFahimi2018(tasks, true, true, true);
    }
}
