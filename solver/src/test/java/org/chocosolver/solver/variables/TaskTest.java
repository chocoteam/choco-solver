/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;
import org.chocosolver.solver.variables.view.integer.IntAffineView;
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

    @Test(groups = "1s", timeOut = 60000)
    public void testDecreaseDuration() throws ContradictionException {
        checkVariable(duration, 0, 10);
        start.removeValue(0, Cause.Null);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        checkVariable(duration, 0, 9);

        end.removeValue(10, Cause.Null);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        checkVariable(duration, 0, 8);
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testIncreaseDuration() throws ContradictionException {
        start.removeInterval(4, 5, Cause.Null);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        checkVariable(duration, 2, 10);

        end.removeValue(5, Cause.Null);
        end.removeValue(6, Cause.Null);
        end.removeValue(7, Cause.Null);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        checkVariable(duration, 5, 10);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBadDomainFilteringOK() throws ContradictionException {
        Task task = new Task(end, duration, start);
        System.out.println(task);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        checkVariable(duration, 0, 0);
        checkVariable(start, 5, 5);
        checkVariable(end, 5, 5);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testBadDomainFilteringKO() throws ContradictionException {
        IntVar start = model.intVar(5, 6);
        IntVar end = model.intVar(1, 2);
        task = new Task(start, duration, end);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testUpdateStart() throws ContradictionException {
        duration.removeValue(10, Cause.Null);
        // we don't know which bound is updated
        checkVariable(start, 0, 5);
        checkVariable(end, 5, 10);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValueSameVariable() throws ContradictionException {
        IntVar start = model.intVar(-5, 0);
        IntVar durationAndEnd = model.intVar(10);
        Task task = new Task(start, durationAndEnd, durationAndEnd);
        start.removeValue(0, Cause.Null);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void testNegativeDuration() throws ContradictionException {
        IntVar start = model.intVar(0);
        IntVar end = model.intVar(-5);
        IntVar duration = model.intVar(-5);
        Task task = new Task(start, duration, end);
        task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        // TODO: 21/03/2016 is it possible ?
        System.out.println("odd behaviour: " + task);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCumulativeConstraintKO() {
        Task[] tasks = new Task[2];
        IntVar[] heights = model.intVarArray(2, 50, 75);
        tasks[0] = new Task(model.intVar(0), model.intVar(20), model.intVar(20));
        tasks[1] = new Task(model.intVar(19), model.intVar(5), model.intVar(24));
        model.cumulative(tasks, heights, model.intVar(75)).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
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
     *
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
        return t1.getEst() == t2.getEst() && t1.getLst() == t2.getLst()
                && t1.getDuration().getLB() == t2.getDuration().getLB() && t1.getDuration().getUB() == t2.getDuration().getUB()
                && t1.getEct() == t2.getEct() && t1.getLct() == t2.getLct();
    }

    @FunctionalInterface
    private interface TaskCreator {
        Task create();
    }

    private static Task[] createTasks(TaskCreator[] creators) {
        Task[] tasks = new Task[creators.length];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = creators[i].create();
        }
        return tasks;
    }

    @FunctionalInterface
    private interface TaskTester {
        boolean test(Task task);
    }

    private void testTaskVars(Task[] tasks, boolean shouldHaveMonitor, TaskTester[] testers) {
        for (int i = 0; i < tasks.length; i++) {
            Assert.assertEquals(!tasks[i].isPassive(), shouldHaveMonitor);
            if (testers != null) {
                for (int j = 0; j < testers.length; j++) {
                    Assert.assertTrue(testers[j].test(tasks[i]));
                }
            }
            if (tasks[i] instanceof OptionalTask) {
                Assert.assertTrue(tasks[i].mayBePerformed());
                Assert.assertFalse(tasks[i].mustBePerformed());
            } else {
                Assert.assertTrue(tasks[i].mayBePerformed());
                Assert.assertTrue(tasks[i].mustBePerformed());
            }
            if (i > 0) {
                Assert.assertTrue(sameTaskVars(tasks[0], tasks[i]));
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTaskConstructors() {
        Model m = new Model();
        int d = 2;
        boolean shouldHaveMonitor = false;

        // Task(Model m, int est, int lst, int d, int ect, int lct)
        TaskCreator[] creators = new TaskCreator[]{
                () -> new Task(m, 1, 10, 2, 0, 10),
                () -> new OptionalTask(m, 1, 10, 2, 0, 10),
                () -> new OptionalTask(m, 1, 10, 2, 0, 10, m.boolVar()),
                () -> m.taskVar(1, 10, 2, 0, 10),
                () -> m.taskVar(1, 10, 2, 0, 10, false),
                () -> m.taskVar(1, 10, 2, 0, 10, true),
                () -> m.taskVar(1, 10, 2, 0, 10, m.boolVar())
        };
        TaskTester[] testers = new TaskTester[]{
                t -> t.getEnd() instanceof IntAffineView,
                t -> t.getEst() == 1,
                t -> t.getLst() == 8,
                t -> t.getEct() == 3,
                t -> t.getLct() == 10
        };
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        shouldHaveMonitor = false;
        creators = new TaskCreator[]{
                () -> new Task(m, 0, 10, 2, 2, 12),
                () -> new OptionalTask(m, 0, 10, 2, 2, 12),
                () -> new OptionalTask(m, 0, 10, 2, 2, 12, m.boolVar()),
                () -> m.taskVar(0, 10, 2, 2, 12),
                () -> m.taskVar(0, 10, 2, 2, 12, false),
                () -> m.taskVar(0, 10, 2, 2, 12, true),
                () -> m.taskVar(0, 10, 2, 2, 12, m.boolVar())
        };
        testers = new TaskTester[]{
                t -> t.getEnd() instanceof IntAffineView,
                t -> t.getEst() == 0,
                t -> t.getLst() == 10,
                t -> t.getEct() == 2,
                t -> t.getLct() == 12
        };
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        // Task(IntVar s, int d)
        shouldHaveMonitor = false;
        creators = new TaskCreator[]{
                () -> new Task(m.intVar(0, 10), d),
                () -> new OptionalTask(m.intVar(0, 10), d),
                () -> new OptionalTask(m.intVar(0, 10), d, m.boolVar()),
                () -> m.taskVar(m.intVar(0, 10), d),
                () -> m.taskVar(m.intVar(0, 10), d, false),
                () -> m.taskVar(m.intVar(0, 10), d, true),
                () -> m.taskVar(m.intVar(0, 10), d, m.boolVar())
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        // Task(IntVar s, int d, IntVar e)
        shouldHaveMonitor = true;
        creators = new TaskCreator[]{
                () -> new Task(m.intVar(0, 10), d, m.intVar(0, 10)),
                () -> new OptionalTask(m.intVar(0, 10), d, m.intVar(0, 10)),
                () -> new OptionalTask(m.intVar(0, 10), d, m.intVar(0, 10), m.boolVar()),
                () -> m.taskVar(m.intVar(0, 10), d, m.intVar(0, 10)),
                () -> m.taskVar(m.intVar(0, 10), d, m.intVar(0, 10), false),
                () -> m.taskVar(m.intVar(0, 10), d, m.intVar(0, 10), true),
                () -> m.taskVar(m.intVar(0, 10), d, m.intVar(0, 10), m.boolVar())
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        shouldHaveMonitor = false;
        creators = new TaskCreator[]{
                () -> {IntVar s = m.intVar(0, 10); return new Task(s, d, m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, d, m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, d, m.offset(s, d), m.boolVar());},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, d, m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, d, m.offset(s, d), false);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, d, m.offset(s, d), true);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, d, m.offset(s, d), m.boolVar());}
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        // Task(IntVar s, IntVar d, IntVar e)
        shouldHaveMonitor = true;
        creators = new TaskCreator[]{
                () -> new Task(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10)),
                () -> new OptionalTask(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10)),
                () -> new OptionalTask(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10), m.boolVar()),
                () -> m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10)),
                () -> m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10), false),
                () -> m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10), true),
                () -> m.taskVar(m.intVar(0, 10), m.intVar(1, 5), m.intVar(0, 10), m.boolVar())
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        shouldHaveMonitor = false;
        creators = new TaskCreator[]{
                () -> {IntVar s = m.intVar(0, 10); return new Task(s, m.intVar(d), m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(d), m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(d), m.offset(s, d), m.boolVar());},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(d), m.offset(s, d));},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(d), m.offset(s, d), false);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(d), m.offset(s, d), true);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(d), m.offset(s, d), m.boolVar());}
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        // taskVar(IntVar s, IntVar d)
        shouldHaveMonitor = true;
        creators = new TaskCreator[]{
                () -> {IntVar s = m.intVar(0, 10); return new Task(s, m.intVar(1, 5));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(1, 5));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(1, 5), m.boolVar());},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(1, 5));},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(1, 5), false);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(1, 5), true);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(1, 5), m.boolVar());}
        };
        testers = null;
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);

        shouldHaveMonitor = false;
        creators = new TaskCreator[]{
                () -> {IntVar s = m.intVar(0, 10); return new Task(s, m.intVar(2));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(2));},
                () -> {IntVar s = m.intVar(0, 10); return new OptionalTask(s, m.intVar(2), m.boolVar());},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(2));},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(2), false);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(2), true);},
                () -> {IntVar s = m.intVar(0, 10); return m.taskVar(s, m.intVar(2), m.boolVar());}
        };
        testers = new TaskTester[]{
                t -> t.getEnd() instanceof IntAffineView
        };
        testTaskVars(createTasks(creators), shouldHaveMonitor, testers);
    }

    @Test(groups = "1s")
    public void testMonitor1() {
        Model model = new Model();
        IntVar first = model.intVar("first", new int[]{1, 2, 3, 4, 5});
        IntVar dur = model.intVar("dur", new int[]{1, 2, 4, 5});
        IntVar last = model.intVar("last", 5, 6);
        IntVar IV390 = model.intVar("IV390", 6);
        model.arithm(first, "+", dur, "=", last).post();
        new Task(first, dur, IV390);
        Solver s = model.getSolver();
        s.setSearch(Search.inputOrderLBSearch(last));  // <- for the issue
        Assert.assertTrue(s.solve());
    }

    @Test(groups = "1s")
    public void testMonitor2() {
        Model model = new Model();
        IntVar first = new BitsetIntVarImpl("first", new int[]{1, 2, 3, 4, 5}, model);
        IntVar dur = new BitsetIntVarImpl("dur", new int[]{1, 2, 4, 5}, model);
        IntVar last = model.intVar("last", 5, 6);
        IntVar IV390 = model.intVar("IV390", 6);
        model.arithm(first, "+", dur, "=", last).post();
        new Task(first, dur, IV390);
        Solver s = model.getSolver();
        s.setSearch(Search.inputOrderLBSearch(last));  // <- for the issue
        Assert.assertTrue(s.solve());
    }

    @Test(groups = "1s")
    public void testMonitorAndView() {
        Model model = new Model();
        IntVar first = model.offset(model.intVar("first", new int[]{1, 2, 3, 4, 5}), 2);
        IntVar dur = model.offset(model.intVar("dur", new int[]{1, 2, 4, 5}), 2);
        IntVar last = model.offset(model.intVar("last", 5, 6), 2);
        IntVar IV390 = model.offset(model.intVar("IV390", 6), 2);
        model.arithm(first, "+", dur, "=", last).post();
        new Task(first, dur, IV390);
        Solver s = model.getSolver();
        s.setSearch(Search.inputOrderLBSearch(last));  // <- for the issue
        Assert.assertTrue(s.solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testScalarAndTaskBug1() {
        int[] coeffs = new int[]{3, -2,-1,-2};
        IntVar[] vars = new IntVar[4];
        vars[0] = model.intVar(8, 9);
        vars[1] = model.intVar(2, 4);
        vars[2] = model.intVar(new int[]{1,2,4,5,6});
        vars[3] = model.intVar(3, 5);
        model.scalar(vars, coeffs, "<=", 1).post();
        IntVar ee = model.intVar(4, 9);
        new Task(vars[3], vars[2], ee);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }
}