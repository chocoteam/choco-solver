/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.view.integer.IntOffsetView;
import org.chocosolver.util.ESat;
import org.testng.Assert;
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
        return one.getStart().getValue() < two.getEnd().getValue() && one.getEnd().getValue() > two.getStart().getValue();
    }

    private void checkVariable(IntVar var, int lb, int ub) {
        assertEquals(var.getLB(), lb);
        assertEquals(var.getUB(), ub);
    }

    ///////////////////////////////////////////////////////////////
    /////////////////    Task constructors test    ////////////////
    ///////////////////////////////////////////////////////////////

    private static boolean sameTaskVars(Task t1, Task t2) {
        return t1.getStart().getLB() == t2.getStart().getLB() && t1.getStart().getUB() == t2.getStart().getUB()
            && t1.getDuration().getLB() == t2.getDuration().getLB() && t1.getDuration().getUB() == t2.getDuration().getUB()
            && t1.getEnd().getLB() == t2.getEnd().getLB() && t1.getEnd().getUB() == t2.getEnd().getUB();
    }

    private static boolean hasTaskMonitor(Task task) {
        return task.getMonitor() != null;
    }

    private void specificToTask(Model m) {
        // Task(Model model, int est, int lst, int d, int ect, int lct)
        Task t1 = new Task(m, 0, 10, 2, 0, 10);
        Assert.assertTrue(hasTaskMonitor(t1));
        Task t2 = new Task(m, 0, 10, 2, 2, 12);
        Assert.assertFalse(hasTaskMonitor(t2));
    }

    private void specificToIVariableFactory(Model m) {
        // taskVar(IntVar s, IntVar d)
        IntVar s = m.intVar(0, 10);
        IntVar d = m.intVar(1,5);
        Task t1 = m.taskVar(s, d);
        Assert.assertTrue(hasTaskMonitor(t1));

        Task t2 = m.taskVar(s, m.intVar(2));
        Assert.assertTrue(t2.getEnd() instanceof IntOffsetView);
        Assert.assertFalse(hasTaskMonitor(t2));
    }

    private void inCommon(Model m) {
        int d = 2;

        // Task(IntVar s, int d)
        Task t1 = new Task(m.intVar(0, 10), d);
        Task t2 = m.taskVar(m.intVar(0, 10), d);
        Assert.assertTrue(sameTaskVars(t1, t2));
        Assert.assertFalse(hasTaskMonitor(t1));
        Assert.assertFalse(hasTaskMonitor(t2));

        // Task(IntVar s, int d, IntVar e)
        Task t3 = new Task(m.intVar(0, 10), d, m.intVar(0, 10));
        Task t4 = m.taskVar(m.intVar(0, 10), d, m.intVar(0, 10));
        Assert.assertTrue(sameTaskVars(t3, t4));
        Assert.assertTrue(hasTaskMonitor(t3));
        Assert.assertTrue(hasTaskMonitor(t4));

        IntVar s5 = m.intVar(0, 10);
        Task t5 = new Task(s5, d, m.intOffsetView(s5, d));
        IntVar s6 = m.intVar(0, 10);
        Task t6 = m.taskVar(s6, d, m.intOffsetView(s6, d));
        Assert.assertTrue(sameTaskVars(t5, t6));
        Assert.assertFalse(hasTaskMonitor(t5));
        Assert.assertFalse(hasTaskMonitor(t6));

        // Task(IntVar s, IntVar d, IntVar e)
        Task t7 = new Task(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10));
        Task t8 = m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10));
        Assert.assertTrue(sameTaskVars(t7, t8));
        Assert.assertTrue(hasTaskMonitor(t7));
        Assert.assertTrue(hasTaskMonitor(t8));

        IntVar s9 = m.intVar(0, 10);
        Task t9 = new Task(s9, m.intVar(d), m.intOffsetView(s9, d));
        IntVar s10 = m.intVar(0, 10);
        Task t10 = m.taskVar(s10, m.intVar(d), m.intOffsetView(s10, d));
        Assert.assertTrue(sameTaskVars(t9, t10));
        Assert.assertFalse(hasTaskMonitor(t9));
        Assert.assertFalse(hasTaskMonitor(t10));

        // Task(IntVar s, IntVar d, IntVar e, boolean declareMonitor)
        Task t11 = new Task(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10));
        Task t12 = m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10));
        Assert.assertTrue(sameTaskVars(t11, t12));
        Assert.assertTrue(hasTaskMonitor(t11));
        Assert.assertTrue(hasTaskMonitor(t12));

        IntVar s13 = m.intVar(0, 10);
        Task t13 = new Task(s13, m.intVar(d), m.intOffsetView(s13, d));
        IntVar s14 = m.intVar(0, 10);
        Task t14 = m.taskVar(s14, m.intVar(d), m.intOffsetView(s14, d));
        Assert.assertTrue(sameTaskVars(t13, t14));
        Assert.assertFalse(hasTaskMonitor(t13));
        Assert.assertFalse(hasTaskMonitor(t14));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testingTaskConstructors() {
        Model m = new Model();

        /* Task specific constructors */
        specificToTask(m);
        /* Constructing methods in common between Task and IVariableFactory */
        inCommon(m);
        /* IVariableFactory specific constructors */
        specificToIVariableFactory(m);
    }

}

