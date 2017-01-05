/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.ESat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class TaskTest {

    private Model model;
    private Task task;
    private IntVar start;
    private IntVar duration;
    private IntVar end;


    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
        start = model.intVar(0, 5);
        end = model.intVar(5, 10);
        duration = model.intVar(0, 10);
        task = new Task(start, duration, end);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDecreaseDuration() throws ContradictionException {
        checkVariable(duration, 0, 10);
        start.removeValue(0, Cause.Null);
        checkVariable(duration, 0, 9);

        end.removeValue(10, Cause.Null);
        checkVariable(duration, 0, 8);
    }


    @Test(groups = "1s", timeOut=60000)
    public void testIncreaseDuration() throws ContradictionException {
        start.removeInterval(4, 5, Cause.Null);
        checkVariable(duration, 2, 10);

        end.removeValue(5, Cause.Null);
        end.removeValue(6, Cause.Null);
        end.removeValue(7, Cause.Null);
        checkVariable(duration, 5, 10);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testBadDomainFilteringOK() throws ContradictionException {
        Task task = new Task(end, duration, start);
        System.out.println(task);
        task.ensureBoundConsistency();
        checkVariable(duration, 0, 0);
        checkVariable(start, 5, 5);
        checkVariable(end, 5, 5);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testBadDomainFilteringKO() throws ContradictionException {
        IntVar start = model.intVar(5, 6);
        IntVar end = model.intVar(1, 2);
        task = new Task(start, duration, end);
        task.ensureBoundConsistency();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testUpdateStart() throws ContradictionException {
        duration.removeValue(10, Cause.Null);
        // we don't know which bound is updated
        checkVariable(start, 0, 5);
        checkVariable(end, 5, 10);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValueSameVariable() throws ContradictionException {
        IntVar start = model.intVar(-5, 0);
        IntVar durationAndEnd = model.intVar(10);
        new Task(start, durationAndEnd, durationAndEnd);
        start.removeValue(0, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNegativeDuration() throws ContradictionException {
        IntVar start = model.intVar(0);
        IntVar end = model.intVar(-5);
        IntVar duration = model.intVar(-5);
        Task task = new Task(start, duration, end);
        task.ensureBoundConsistency();
        // TODO: 21/03/2016 is it possible ?
        System.out.println("odd behaviour: " + task);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCumulativeConstraintKO() {
        Task[] tasks = new Task[2];
        IntVar[] heights = model.intVarArray(2, 50, 75);
        tasks[0] = new Task(model.intVar(0), model.intVar(20), model.intVar(20));
        tasks[1] = new Task(model.intVar(19), model.intVar(5), model.intVar(24));
        model.cumulative(tasks, heights, model.intVar(75)).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCumulativeConstraint() {
        Task[] tasks = new Task[3];
        IntVar[] heights = model.intVarArray(3, 73, 75);
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new Task(model.intVar(0, 5, true), model.intVar(1, 5, true), model.intVar(0, 5, true));
        }
        model.cumulative(tasks, heights, model.intVar(74, 75)).post();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            if (collide(tasks[0], tasks[1]) || collide(tasks[1], tasks[2]) || collide(tasks[0], tasks[2])) {
                System.err.println("Tasks overlapping :");
                for (Task task : tasks) {
                    System.err.println(task);
                }
                fail("Tasks overlapping");
            }
        }
        assertTrue(nbSol > 0);
    }

    /**
     * Detect tasks overlapping between two tasks
     * @param one first task
     * @param two second check
     * @return if an overlapping exists between the tasks
     */
    private boolean collide(Task one, Task two) {
        return one.getStart().getValue() < two.getEnd().getValue() &&
                one.getEnd().getValue() > two.getStart().getValue();
    }

    private void checkVariable(IntVar var, int lb, int ub) {
        assertEquals(var.getLB(), lb);
        assertEquals(var.getUB(), ub);
    }


}

