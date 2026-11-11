/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class TestUtils {
    public static int[] getDomain(IntVar variable) {
        int[] values = new int[variable.getDomainSize()];
        int i = 0;
        for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
            values[i++] = v;
        }
        return values;
    }

    public static void testBounds(IntVar variable, int lb, int ub) {
        assertEquals(variable.getLB(), lb);
        assertEquals(variable.getUB(), ub);
    }

    public static void testBounds(Task task, int est, int lst, int ect, int lct) {
        assertEquals(task.getEst(), est);
        assertEquals(task.getLst(), lst);
        assertEquals(task.getEct(), ect);
        assertEquals(task.getLct(), lct);
    }

    /**
     * Builds all the ways to assign {@code nbTasks} tasks: either unperformed ({@code null}),
     * or performed with a given start in [0,2], a given duration in [1,2], and the corresponding end.
     */
    public static List<int[][]> taskAssignments(int nbTasks) {
        List<int[]> options = new ArrayList<>();
        options.add(null);
        for (int s = 0; s <= 2; s++) {
            for (int d = 1; d <= 2; d++) {
                options.add(new int[]{s, d, s + d});
            }
        }
        List<int[][]> assignments = new ArrayList<>();
        assignments.add(new int[0][]);
        for (int i = 0; i < nbTasks; i++) {
            List<int[][]> next = new ArrayList<>();
            for (int[][] partial : assignments) {
                for (int[] option : options) {
                    int[][] extended = Arrays.copyOf(partial, partial.length + 1);
                    extended[partial.length] = option;
                    next.add(extended);
                }
            }
            assignments = next;
        }
        return assignments;
    }

    /**
     * Fixes an optional task to be unperformed ({@code null}), or performed with the given start, duration and end.
     */
    public static void fixTask(Model model, OptionalTask task, int[] assignment) {
        if (assignment == null) {
            model.arithm(task.getPerformed(), "=", 0).post();
        } else {
            model.arithm(task.getPerformed(), "=", 1).post();
            model.arithm(task.getStart(), "=", assignment[0]).post();
            model.arithm(task.getDuration(), "=", assignment[1]).post();
            model.arithm(task.getEnd(), "=", assignment[2]).post();
        }
    }
}
