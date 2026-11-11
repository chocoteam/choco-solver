/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.TestUtils;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.chocosolver.solver.TestUtils.testBounds;
import static org.testng.Assert.fail;

public class SpanTest {
    @Test(groups = "1s", timeOut = 60000)
    public void testFilter() {
        Model model = new Model();
        int est = 0;
        int lst = 10;
        int duration = 10;
        OptionalTask[] tasks = new OptionalTask[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", est, lst);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, duration, performed);
        }
        IntVar start = model.intVar("start_span", est, lst);
        IntVar dur = model.intVar("duration_span", duration, lst + duration - est);
        BoolVar performed = model.boolVar("performed_span");
        OptionalTask span = model.taskVar(start, dur, performed);
        model.span(span, tasks).post();

        try {
            // Expected filtering : None
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                testBounds(tasks[i].getStart(), 0, 10);
                Assert.assertTrue(tasks[i].mayBePerformed());
                Assert.assertFalse(tasks[i].mustBePerformed());
            }
            testBounds(span.getStart(), 0, 10);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertFalse(span.mustBePerformed());

            // span.est = 2 and (tasks[i].lct = 18 and tasks[i] is performed) for all i != 4
            // Expected filtering : tasks[i].est = 2 for all i, and span is performed
            span.updateEst(2, Cause.Null);
            for (int i = 0; i < tasks.length; i++) {
                if (i != 4) {
                    tasks[i].updateLct(18, Cause.Null);
                    tasks[i].forceToBePerformed(Cause.Null);
                }
            }
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 4) {
                    testBounds(tasks[i].getStart(), 2, 10);
                } else if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 4) {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                } else {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                }
            }
            testBounds(span.getStart(), 2, 10);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());

            // tasks[4]is not performed
            // Expected filtering : span.lct = 18 (since tasks[4] is not performed and it was the only one with lct > 18)
            tasks[4].forceToBeUnperformed(Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 4) {
                    Assert.assertFalse(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                } else {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                }
            }
            testBounds(span.getStart(), 2, 8);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());

            // tasks[1].start is instantiated to 2 and tasks[2].end is instantiated to 18
            // Expected filtering : span.start is instantiated to 2 and span.end is instantiated to 18
            tasks[1].instantiateStartAt(2, Cause.Null);
            tasks[2].instantiateEndAt(18, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].mayBePerformed()) {
                    if (i == 1) {
                        testBounds(tasks[i].getStart(), 2, 2);
                    } else if (i == 2) {
                        testBounds(tasks[i].getStart(), 8, 8);
                    } else {
                        testBounds(tasks[i].getStart(), 2, 8);
                    }
                }
                if (i == 4) {
                    Assert.assertFalse(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                } else {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                }
            }
            testBounds(span.getStart(), 2, 2);
            testBounds(span.getEnd(), 18, 18);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testFilter2() {
        Model model = new Model();
        int est = 0;
        int lst = 10;
        int duration = 10;
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", est, lst);
            if (i % 5 == 0) {
                tasks[i] = model.taskVar(start, duration);
            } else {
                BoolVar performed = model.boolVar("performed[" + i + "]");
                tasks[i] = model.taskVar(start, duration, performed);
            }
        }
        IntVar start = model.intVar("start_span", est, lst);
        IntVar dur = model.intVar("duration_span", duration, lst + duration - est);
        BoolVar performed = model.boolVar("performed_span");
        OptionalTask span = model.taskVar(start, dur, performed);
        model.span(span, tasks).post();

        try {
            // Expected filtering : span is performed as tasks[0] and tasks[5] are performed
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5) {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                } else {
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                }
                testBounds(tasks[i].getStart(), 0, 10);
            }
            testBounds(span.getStart(), 0, 10);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());

            // tasks[0].est = 2 and tasks[0].lct = 18 and tasks[5].est = 2 and tasks[5].lct = 18
            // Expected filtering : None
            tasks[0].updateEst(2, Cause.Null);
            tasks[0].updateLct(18, Cause.Null);
            tasks[5].updateEst(2, Cause.Null);
            tasks[5].updateLct(18, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5) {
                    testBounds(tasks[i].getStart(), 2, 8);
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                } else {
                    testBounds(tasks[i].getStart(), 0, 10);
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(span.getStart(), 0, 10);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());

            // tasks[i] is not performed for all i != 0 and i != 5
            // Expected filtering : span.est = 2 and span.lct = 18
            for (int i = 0; i < tasks.length; i++) {
                if (!tasks[i].mustBePerformed()) {
                    tasks[i].forceToBeUnperformed(Cause.Null);
                }
            }
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5) {
                    testBounds(tasks[i].getStart(), 2, 8);
                    Assert.assertTrue(tasks[i].mayBePerformed());
                    Assert.assertTrue(tasks[i].mustBePerformed());
                } else {
                    testBounds(tasks[i].getStart(), 0, 10);
                    Assert.assertFalse(tasks[i].mayBePerformed());
                    Assert.assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(span.getStart(), 2, 8);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertTrue(span.mustBePerformed());
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testFilter3() {
        Model model = new Model();
        int est = 0;
        int lst = 10;
        int duration = 10;
        OptionalTask[] tasks = new OptionalTask[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", est, lst);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, duration, performed);
        }
        IntVar start = model.intVar("start_span", est, lst);
        IntVar dur = model.intVar("duration_span", duration, lst + duration - est);
        BoolVar performed = model.boolVar("performed_span");
        OptionalTask span = model.taskVar(start, dur, performed);
        model.span(span, tasks).post();

        try {
            // Expected filtering : None
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                testBounds(tasks[i].getStart(), 0, 10);
                Assert.assertTrue(tasks[i].mayBePerformed());
                Assert.assertFalse(tasks[i].mustBePerformed());
            }
            testBounds(span.getStart(), 0, 10);
            Assert.assertTrue(span.mayBePerformed());
            Assert.assertFalse(span.mustBePerformed());

            // span is not performed
            // Expected filtering : tasks[i] is not performed for all i
            span.forceToBeUnperformed(Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                testBounds(tasks[i].getStart(), 0, 10);
                Assert.assertFalse(tasks[i].mayBePerformed());
                Assert.assertFalse(tasks[i].mustBePerformed());
            }
            testBounds(span.getStart(), 0, 10);
            Assert.assertFalse(span.mayBePerformed());
            Assert.assertFalse(span.mustBePerformed());
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSpanCreation() {
        Model model = new Model();
        int est = 0;
        int lst = 10;
        int duration = 10;
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", est + i, lst + i);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, duration, performed);
        }
        Task span = model.span(tasks);
        testBounds(span, 0, 29, 0, 29);
        Assert.assertTrue(span.mayBePerformed());
        Assert.assertFalse(span.mustBePerformed());

        Task[] tasks2 = ArrayUtils.append(tasks, new Task[]{new Task(model.intVar(-5, 20), duration)});
        Task span2 = model.span(tasks2);
        testBounds(span2, -5, 30, -5, 30);
        Assert.assertTrue(span2.mayBePerformed());
        Assert.assertTrue(span2.mustBePerformed());

        Task[] tasks3 = ArrayUtils.append(tasks, new Task[]{new OptionalTask(model.intVar(-5, 20), duration, model.boolVar(false))});
        Task span3 = model.span(tasks3);
        testBounds(span3, 0, 29, 0, 29);
        Assert.assertTrue(span3.mayBePerformed());
        Assert.assertFalse(span3.mustBePerformed());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSpanSolve() {
        int n = 10;
        Model model = new Model();
        Task[] tasks = new Task[n];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", i, i);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, 10, performed);
        }
        Task span = model.taskVar(model.intVar("span_start", -10, 20), model.intVar("span_duration", 0, 30), model.boolVar("span_performed"));
        model.span(span, tasks).post();
        model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{span})));
        while (model.getSolver().solve()) {
            int minEst = Integer.MAX_VALUE;
            int maxLct = Integer.MIN_VALUE;
            for (int i = 0; i < tasks.length; ++i) {
                if (tasks[i].mustBePerformed()) {
                    minEst = Math.min(minEst, tasks[i].getEst());
                    maxLct = Math.max(maxLct, tasks[i].getLct());
                }
            }
            if (minEst > maxLct) {
                Assert.assertFalse(span.mayBePerformed());
            } else {
                Assert.assertTrue(span.mustBePerformed());
                testBounds(span, minEst, minEst, maxLct, maxLct);
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), (int) Math.pow(2, n));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSpanSolve2() {
        Model model = new Model();
        Task[] tasks = new Task[3];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", 1, 5);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, 10, performed);
        }
        Task span = model.taskVar(model.intVar("span_start", -10, 20), model.intVar("span_duration", 0, 30), model.boolVar("span_performed"));
        model.span(span, tasks).post();
        model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{span})));
        while (model.getSolver().solve()) {
            int minEst = Integer.MAX_VALUE;
            int maxLct = Integer.MIN_VALUE;
            for (int i = 0; i < tasks.length; ++i) {
                if (tasks[i].mustBePerformed()) {
                    minEst = Math.min(minEst, tasks[i].getEst());
                    maxLct = Math.max(maxLct, tasks[i].getLct());
                }
            }
            if (minEst > maxLct) {
                Assert.assertFalse(span.mayBePerformed());
            } else {
                Assert.assertTrue(span.mustBePerformed());
                testBounds(span, minEst, minEst, maxLct, maxLct);
            }
        }
        // With span performed and with 3 optional task variables, we have the following number of solutions :
        //      span_duration = 10 : 7 solutions
        //      span_duration = 11 : 12 solutions
        //      span_duration = 12 : 18 solutions
        //      span_duration = 13 : 24 solutions
        //      span_duration = 14 : 30 solutions
        // And we have the following possibilities :
        //      span_duration = 10 : 5 possibilities
        //      span_duration = 11 : 4 possibilities
        //      span_duration = 12 : 3 possibilities
        //      span_duration = 13 : 2 possibilities
        //      span_duration = 14 : 1 possibilities
        // Therefore, there are 5*7 + 4*12 + 3*18 + 2*24 + 1*30 = 215 solutions where span is performed.
        // So we have 216 solutions when considering the one where span is not performed in addition.
        Assert.assertEquals(model.getSolver().getSolutionCount(), 216);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSpanSolve3() {
        int n = 10;
        Model model = new Model();
        Task[] tasks = new Task[n];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", i, i);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, 10, performed);
        }
        Task span = model.taskVar(model.intVar("span_start", -10, 20), model.intVar("span_duration", 0, 30), model.boolVar("span_performed"));
        IntVar varN = model.intVar("varN", 0, 3);
        model.span(span, tasks, varN).post();
        model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{span})));
        while (model.getSolver().solve()) {
            int minEst = Integer.MAX_VALUE;
            int maxLct = Integer.MIN_VALUE;
            for (int i = 0; i < tasks.length; ++i) {
                if (tasks[i].mustBePerformed()) {
                    minEst = Math.min(minEst, tasks[i].getEst());
                    maxLct = Math.max(maxLct, tasks[i].getLct());
                }
            }
            if (minEst > maxLct) {
                Assert.assertFalse(span.mayBePerformed());
            } else {
                Assert.assertTrue(span.mustBePerformed());
                testBounds(span, minEst, minEst, maxLct, maxLct);
            }
        }
        // Nb solutions = 176 = 1 (unperformed) + (1 among 10) + (2 among 10) + (3 among 10)
        Assert.assertEquals(model.getSolver().getSolutionCount(), 176);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSpanSolveReification() {
        for (int n = 1; n < 10; ++n) {
            Model model = new Model();
            Task[] tasks = new Task[n];
            for (int i = 0; i < tasks.length; i++) {
                IntVar start = model.intVar("start[" + i + "]", i, i);
                BoolVar performed = model.boolVar("performed[" + i + "]");
                tasks[i] = model.taskVar(start, 10, performed);
            }
            Task span = model.taskVar(model.intVar("span_start", -2, 10), model.intVar("span_duration", 0, 20), model.boolVar("span_performed"));
            BoolVar reified = model.span(span, tasks).reify();
            model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{span})), Search.inputOrderLBSearch(reified));
            while (model.getSolver().solve()) {
                if (reified.getValue() == 1) {
                    int minEst = Integer.MAX_VALUE;
                    int maxLct = Integer.MIN_VALUE;
                    for (int i = 0; i < tasks.length; ++i) {
                        if (tasks[i].mustBePerformed()) {
                            minEst = Math.min(minEst, tasks[i].getEst());
                            maxLct = Math.max(maxLct, tasks[i].getLct());
                        }
                    }
                    if (minEst > maxLct) {
                        Assert.assertFalse(span.mayBePerformed());
                    } else {
                        Assert.assertTrue(span.mustBePerformed());
                        testBounds(span, minEst, minEst, maxLct, maxLct);
                    }
                }
            }
            // 2^n solutions when reified = true
            // XXXXX solutions when reified = false : (|dom(alter_start)| x |dom(alter_duration)| x |dom(alter_end)| + 1) x |dom(performed[0])| x ... x |dom(performed[n-1])|
            Assert.assertEquals(model.getSolver().getSolutionCount(), 9010L * ((int) Math.pow(2, n)) + ((int) Math.pow(2, n)));
        }
    }

    private static boolean isSpanFeasible(boolean lcg, int[][] assignment) {
        Model model = lcg ? new Model(SettingsBuilder.dev().setLCG(true)) : new Model();
        int nbTasks = assignment.length - 1;
        OptionalTask[] tasks = new OptionalTask[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            tasks[i] = new OptionalTask(model.intVar("s" + i, 0, 2), model.intVar("d" + i, 1, 2),
                    model.intVar("e" + i, 1, 4), model.boolVar("p" + i));
            TestUtils.fixTask(model, tasks[i], assignment[i]);
        }
        OptionalTask span = new OptionalTask(model.intVar("ss", 0, 2), model.intVar("sd", 0, 4),
                model.intVar("se", 0, 4), model.boolVar("sp"));
        TestUtils.fixTask(model, span, assignment[nbTasks]);
        model.span(span, tasks).post();
        return model.getSolver().solve();
    }

    /**
     * Under LCG, span is turned into clauses: for each way to assign the tasks and the span,
     * it must be feasible if and only if it is for the propagator.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testLcgDecompositionSameSolutions() {
        for (int n = 1; n <= 2; n++) {
            for (int[][] assignment : TestUtils.taskAssignments(n + 1)) {
                Assert.assertEquals(isSpanFeasible(true, assignment), isSpanFeasible(false, assignment),
                        Arrays.deepToString(assignment));
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLcgDecompositionNoTask() {
        Model model = new Model(SettingsBuilder.dev().setLCG(true));
        OptionalTask span = model.taskVar(model.intVar(0, 3), 1, model.boolVar());
        model.span(span, new Task[0]).post();
        Assert.assertTrue(model.getSolver().solve());
        Assert.assertFalse(span.mayBePerformed() && span.mustBePerformed());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnperformedSpanCreators() {
        Model model = new Model();
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; ++i) {
            tasks[i] = model.taskVar(model.intVar(i), model.intVar(i), model.boolVar(false));
        }

        Task[] spans = new Task[]{
            model.span(new Task[0]),
            model.span(tasks)
        };
        for (int i = 0; i < spans.length; ++i) {
            Assert.assertNotNull(spans[i]);
            Assert.assertFalse(spans[i].mayBePerformed());
            Assert.assertFalse(spans[i].mustBePerformed());
            Assert.assertTrue(spans[i].isPassive());
        }
    }
}
