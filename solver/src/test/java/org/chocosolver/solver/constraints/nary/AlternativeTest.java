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
import static org.testng.Assert.*;

public class AlternativeTest {
    @Test(groups="1s", timeOut=60000)
    public void testPerformed() {
        int n = 100;
        Model model = new Model();
        int start = 0;
        int duration = 10;
        OptionalTask[] tasks = new OptionalTask[n];
        BoolVar[] allPerformedTasks = new BoolVar[n + 1];
        for (int i = 0; i < tasks.length; i++) {
            IntVar s = model.intVar("start[" + i + "]", start + i);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            allPerformedTasks[i] = performed;
            tasks[i] = model.taskVar(s, duration, performed);
        }
        IntVar s = model.intVar("start_alter", start, start + tasks.length - 1);
        BoolVar performed = model.boolVar("performed_alter");
        allPerformedTasks[n] = performed;
        OptionalTask alter = model.taskVar(s, duration, performed);
        model.alternative(alter, tasks).post();
        model.getSolver().setSearch(Search.randomSearch(allPerformedTasks, 0));
        while (model.getSolver().solve())
            ;
        assertEquals(model.getSolver().getSolutionCount(), n + 1);
    }

    @Test(groups="1s", timeOut=60000)
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
        IntVar start = model.intVar("start_alter", est, lst);
        BoolVar performed = model.boolVar("performed_alter");
        OptionalTask alter = model.taskVar(start, duration, performed);
        model.alternative(alter, tasks).post();

        try {
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                testBounds(tasks[i].getStart(), 0, 10);
                assertTrue(tasks[i].mayBePerformed());
                assertFalse(tasks[i].mustBePerformed());
            }
            testBounds(alter.getStart(), 0, 10);
            assertTrue(alter.mayBePerformed());
            assertFalse(alter.mustBePerformed());

            // alter.est = 2 and tasks[9] not performed and tasks[i].lct = 18 for i != 4
            // Expected filtering : tasks[i].est = 2 for all i
            alter.updateEst(2, Cause.Null);
            tasks[9].forceToBeUnperformed(Cause.Null);
            for (int i = 0; i < tasks.length; i++) {
                if (i != 4) {
                    tasks[i].updateLct(18, Cause.Null);
                }
            }
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 4) {
                    testBounds(tasks[i].getStart(), 2, 10);
                } else if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 9) {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(alter.getStart(), 2, 10);
            assertTrue(alter.mayBePerformed());
            assertFalse(alter.mustBePerformed());

            // tasks[4] not performed
            // Expected filtering : alter.lct = 8 (because it is the LCT of all remaining tasks)
            tasks[4].forceToBeUnperformed(Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 4 || i == 9) {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(alter.getStart(), 2, 8);
            assertTrue(alter.mayBePerformed());
            assertFalse(alter.mustBePerformed());

            // alter is performed
            // Expected filtering : None
            alter.forceToBePerformed(Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 4 || i == 9) {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(alter.getStart(), 2, 8);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());

            // tasks[3].est = 5
            // Expected filtering : None
            tasks[3].updateEst(5, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 3) {
                    testBounds(tasks[i].getStart(), 5, 8);
                } else if (tasks[i].mayBePerformed()) {
                    testBounds(tasks[i].getStart(), 2, 8);
                }
                if (i == 4 || i == 9) {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(alter.getStart(), 2, 8);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());

            // tasks[3] is performed
            // Expected filtering : tasks[i] not performed for i != 3 and alter.est = 5 (since it is the EST of tasks[3])
            tasks[3].forceToBePerformed(Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 3) {
                    testBounds(tasks[i].getStart(), 5, 8);
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                } else {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(alter.getStart(), 5, 8);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());

            // tasks[3].est = 7 and alter.lct = 17
            // Expected filtering : tasks[3] and alter are performed and instantiated to start = 7 and end = 17
            tasks[3].updateEst(7, Cause.Null);
            alter.updateLct(17, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i != 3) {
                    assertFalse(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
            }
            testBounds(tasks[3].getStart(), 7, 7);
            assertTrue(tasks[3].mayBePerformed());
            assertTrue(tasks[3].mustBePerformed());
            testBounds(alter.getStart(), 7, 7);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
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
        IntVar start = model.intVar("start_alter", est, lst);
        BoolVar performed = model.boolVar("performed_alter");
        OptionalTask alter = model.taskVar(start, duration, performed);
        IntVar varN = model.intVar("varN", 0, 7);
        model.alternative(alter, tasks, varN).post();

        try {
            // Expected filtering : varN = [2..7] as tasks[0] and tasks[5] are performed, and alter is performed
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5) {
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
                testBounds(tasks[i].getStart(), 0, 10);
            }
            testBounds(alter.getStart(), 0, 10);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
            testBounds(varN, 2, 7);

            // tasks[0].est = 2 and tasks[5].lct = 18
            // Expected filtering : tasks[i].est = 2 and tasks[i].lct = 18 for all i, alter.est = 2 and alter.lct = 18
            tasks[0].updateEst(2, Cause.Null);
            tasks[5].updateLct(18, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5) {
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                }
                testBounds(tasks[i].getStart(), 2, 8);
            }
            testBounds(alter.getStart(), 2, 8);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
            testBounds(varN, 2, 7);

            // tasks[1] and tasks[2] are performed, tasks[i].est = 4 and tasks[i].lct = 16 for i in {3, 4, 6, 7, 8, 9}
            // Expected filtering : still tasks[i].est = 2 and tasks[i].lct = 18 for i in {0, 1, 2, 5} as well as alter, and varN = [4..7]
            tasks[1].forceToBePerformed(Cause.Null);
            tasks[2].forceToBePerformed(Cause.Null);
            for (int i = 0; i < tasks.length; i++) {
                if (!tasks[i].mustBePerformed()) {
                    tasks[i].updateEst(4, Cause.Null);
                    tasks[i].updateLct(16, Cause.Null);
                }
            }
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5 || i == 1 || i == 2) {
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 2, 8);
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 4, 6);
                }
            }
            testBounds(alter.getStart(), 2, 8);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
            testBounds(varN, 4, 7);

            // varN.lb = 5
            // Expected filtering : tasks[i].est = 4 and tasks[i].lct = 16 for all i, as well as alter, as varN >= 5 so at least one of optional task will be performed
            varN.updateLowerBound(5, Cause.Null);
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5 || i == 1 || i == 2) {
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 4, 6);
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 4, 6);
                }
            }
            testBounds(alter.getStart(), 4, 6);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
            testBounds(varN, 5, 7);
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testFilter3() {
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
        IntVar start = model.intVar("start_alter", est, lst);
        BoolVar performed = model.boolVar("performed_alter");
        OptionalTask alter = model.taskVar(start, duration, performed);
        IntVar varN = model.intVar("varN", 5, 7);
        model.alternative(alter, tasks, varN).post();

        try {
            tasks[0].updateEst(2, Cause.Null);
            tasks[5].updateLct(18, Cause.Null);
            tasks[1].forceToBePerformed(Cause.Null);
            tasks[2].forceToBePerformed(Cause.Null);
            for (int i = 0; i < tasks.length; i++) {
                if (!tasks[i].mustBePerformed()) {
                    tasks[i].updateEst(4, Cause.Null);
                    tasks[i].updateLct(16, Cause.Null);
                }
            }
            model.getSolver().propagate();
            for (int i = 0; i < tasks.length; i++) {
                if (i == 0 || i == 5 || i == 1 || i == 2) {
                    assertTrue(tasks[i].mayBePerformed());
                    assertTrue(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 4, 6);
                } else {
                    assertTrue(tasks[i].mayBePerformed());
                    assertFalse(tasks[i].mustBePerformed());
                    testBounds(tasks[i].getStart(), 4, 6);
                }
            }
            testBounds(alter.getStart(), 4, 6);
            assertTrue(alter.mayBePerformed());
            assertTrue(alter.mustBePerformed());
            testBounds(varN, 5, 7);
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testFilterDuration() {
        Model model = new Model();
        final int est = 0;
        final int lst = 20;
        final int cases = 3;
        // Case 0 : alter and tasks have duration variables with enumerated domains
        // Case 1 : alter and tasks have duration variables with enumerated domains, except for one task that has a duration variable with bounded domain
        // Case 2 : alter has duration variable with bounded domain and tasks have duration variables with enumerated domains
        final int[] durations = new int[]{3, 4, 7, 8};
        final Task[][] tasks = new Task[cases][10];
        final Task[] alters = new Task[cases];
        final int idx = (int) (System.currentTimeMillis() % 10);
        for (int j = 0; j < cases; j++) {
            for (int i = 0; i < tasks[j].length; i++) {
                final IntVar start = model.intVar("start[" + j + "][" + i + "]", est, lst);
                final IntVar duration;
                if (j == 1 && i == idx) {
                    duration = model.intVar("duration[" + j + "][" + i + "]", 3, 8, true);
                    model.arithm(duration, "!=", 5).post();
                    model.arithm(duration, "!=", 6).post();
                } else {
                    duration = model.intVar("duration[" + j + "][" + i + "]", durations);
                }
                final BoolVar performed = model.boolVar("performed[" + j + "][" + i + "]");
                tasks[j][i] = model.taskVar(start, duration, performed);
            }
            final IntVar start = model.intVar("start_alter[" + j + "]", est, lst);
            final BoolVar performed = model.boolVar("performed_alter[" + j + "]");
            final IntVar duration = model.intVar("duration[" + j + "]", 1, 10, j == 2);
            alters[j] = model.taskVar(start, duration, performed);
            model.alternative(alters[j], tasks[j], model.intVar(1)).post();
            model.arithm(alters[j].getDuration(), "!=", 7).post();
        }

        try {
            model.getSolver().propagate();
            for (int j = 0; j < cases; j++) {
                for (int i = 0; i < tasks[j].length; i++) {
                    assertTrue(tasks[j][i].getDuration().contains(3));
                    assertTrue(tasks[j][i].getDuration().contains(4));
                    if (j == 2) {
                        assertFalse(tasks[j][i].getDuration().contains(5));
                        assertFalse(tasks[j][i].getDuration().contains(6));
                        assertTrue(tasks[j][i].getDuration().contains(7));
                    } else if (j == 1 && i == idx) {
                        assertTrue(tasks[j][i].getDuration().contains(5));
                        assertTrue(tasks[j][i].getDuration().contains(6));
                        assertTrue(tasks[j][i].getDuration().contains(7));
                    } else {
                        assertFalse(tasks[j][i].getDuration().contains(5));
                        assertFalse(tasks[j][i].getDuration().contains(6));
                        assertFalse(tasks[j][i].getDuration().contains(7));
                    }
                    assertTrue(tasks[j][i].getDuration().contains(8));
                    assertTrue(tasks[j][i].mayBePerformed());
                    assertFalse(tasks[j][i].mustBePerformed());
                }
                assertTrue(alters[j].getDuration().contains(3));
                assertTrue(alters[j].getDuration().contains(4));
                if (j == 2) {
                    assertTrue(alters[j].getDuration().contains(5));
                    assertTrue(alters[j].getDuration().contains(6));
                    assertTrue(alters[j].getDuration().contains(7));
                } else if (j == 1) {
                    assertTrue(alters[j].getDuration().contains(5));
                    assertTrue(alters[j].getDuration().contains(6));
                    assertFalse(alters[j].getDuration().contains(7));
                } else {
                    assertFalse(alters[j].getDuration().contains(5));
                    assertFalse(alters[j].getDuration().contains(6));
                    assertFalse(alters[j].getDuration().contains(7));
                }
                assertTrue(alters[j].getDuration().contains(8));
                assertTrue(alters[j].mayBePerformed());
                assertFalse(alters[j].mustBePerformed());
            }
        } catch (ContradictionException ex) {
            fail();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAlternativeSolve() {
        Model model = new Model();
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", i, i);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, 10, performed);
        }
        Task alter = model.taskVar(model.intVar("alter_start", -10, 20), model.intVar("alter_duration", 0, 30), model.boolVar("alter_performed"));
        model.alternative(alter, tasks).post();
        model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{alter})));
        while (model.getSolver().solve()) {
            for (int i = 0; i < tasks.length; ++i) {
                if (alter.mustBePerformed() && tasks[i].mayBePerformed()) {
                    testBounds(tasks[i], alter.getEst(), alter.getLst(), alter.getEct(), alter.getLct());
                } else {
                    Assert.assertFalse(tasks[i].mayBePerformed());
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 11);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAlternativeSolve2() {
        Model model = new Model();
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; i++) {
            IntVar start = model.intVar("start[" + i + "]", 1, 10);
            IntVar duration = model.intVar("duration[" + i + "]", 1, 10);
            BoolVar performed = model.boolVar("performed[" + i + "]");
            tasks[i] = model.taskVar(start, duration, performed);
        }
        Task alter = model.taskVar(model.intVar("alter_start", -10, 20), model.intVar("alter_duration", 0, 30), model.boolVar("alter_performed"));
        model.alternative(alter, tasks).post();
        model.getSolver().setSearch(model.smallest(ArrayUtils.append(tasks, new Task[]{alter})));
        while (model.getSolver().solve()) {
            for (int i = 0; i < tasks.length; ++i) {
                if (alter.mustBePerformed() && tasks[i].mayBePerformed()) {
                    testBounds(tasks[i], alter.getEst(), alter.getLst(), alter.getEct(), alter.getLct());
                } else {
                    Assert.assertFalse(tasks[i].mayBePerformed());
                }
            }
        }
        // 1001 solutions : 1 for not-performed, and 10 starts x 10 durations x 10 performed possibilities
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1001);
    }

    private static boolean isAlternativeFeasible(boolean lcg, int[][] assignment, int maxN) {
        Model model = lcg ? new Model(SettingsBuilder.dev().setLCG(true)) : new Model();
        int nbTasks = assignment.length - 1;
        OptionalTask[] tasks = new OptionalTask[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            tasks[i] = new OptionalTask(model.intVar("s" + i, 0, 2), model.intVar("d" + i, 1, 2),
                    model.intVar("e" + i, 1, 4), model.boolVar("p" + i));
            TestUtils.fixTask(model, tasks[i], assignment[i]);
        }
        OptionalTask alter = new OptionalTask(model.intVar("as", 0, 2), model.intVar("ad", 1, 2),
                model.intVar("ae", 1, 4), model.boolVar("ap"));
        TestUtils.fixTask(model, alter, assignment[nbTasks]);
        model.alternative(alter, tasks, model.intVar("n", 0, maxN)).post();
        return model.getSolver().solve();
    }

    /**
     * Under LCG, alternative is turned into clauses: for each way to assign the tasks and the alternative,
     * it must be feasible if and only if it is for the propagator.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testLcgDecompositionSameSolutions() {
        for (int n = 1; n <= 2; n++) {
            for (int maxN : new int[]{1, n}) {
                for (int[][] assignment : TestUtils.taskAssignments(n + 1)) {
                    Assert.assertEquals(isAlternativeFeasible(true, assignment, maxN),
                            isAlternativeFeasible(false, assignment, maxN),
                            Arrays.deepToString(assignment) + ", maxN = " + maxN);
                }
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testUnperformedAlternativeCreators() {
        Model model = new Model();
        Task[] tasks = new Task[10];
        for (int i = 0; i < tasks.length; ++i) {
            tasks[i] = model.taskVar(model.intVar(i), model.intVar(i), model.boolVar(false));
        }

        Task[] alters = new Task[]{
            model.alternative(new Task[0]),
            model.alternative(tasks, false),
            model.alternative(tasks, true)
        };
        for (int i = 0; i < alters.length; ++i) {
            Assert.assertNotNull(alters[i]);
            Assert.assertFalse(alters[i].mayBePerformed());
            Assert.assertFalse(alters[i].mustBePerformed());
            Assert.assertTrue(alters[i].isPassive());
        }
    }
}
