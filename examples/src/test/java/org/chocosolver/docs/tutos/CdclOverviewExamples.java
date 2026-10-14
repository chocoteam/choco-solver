/*
 * This file is part of examples, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.docs.tutos;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Examples shown in the user documentation, tutorial "A Lazy Clause Generation overview"
 * (docs/content/tutos/CDCL_overview.md).
 * The regions between {@code tag::name[]} and {@code end::name[]} are included in the page.
 *
 * @author Charles Prud'homme
 */
public class CdclOverviewExamples {

    // tag::data[]
    // Set up the problem data
    int numActivities = 32;  // Number of activities, including source (1) and sink (32)
    int numResources = 4;  // Number of resources
    int[] resourceCapacities = new int[]{10, 12, 4, 12};  // Resource capacities
    int[][] successors = new int[][]{
            {2, 3, 4}, {6, 11, 15}, {7, 8, 13}, {5, 9, 10}, {20}, {30}, {27}, {12, 19, 27},
            {14}, {16, 25}, {20, 26}, {14}, {17, 18}, {17}, {25}, {21, 22},
            {22}, {20, 22}, {24, 29}, {23, 25}, {28}, {23}, {24}, {30},
            {30}, {31}, {28}, {31}, {32}, {32}, {32}, {},
    };
    int[] durations = {0, 8, 4, 6, 3, 8, 5, 9, 2, 7, 9, 2, 6, 3, 9, 10, 6, 5, 3, 7, 2, 7, 2, 3, 3, 7, 8, 3, 7, 2, 2, 0};
    int[][] requirements = new int[][]{
            {0, 0, 0, 0}, {4, 0, 0, 0}, {10, 0, 0, 0}, {0, 0, 0, 3}, {3, 0, 0, 0}, {0, 0, 0, 8}, {4, 0, 0, 0}, {0, 1, 0, 0},
            {6, 0, 0, 0}, {0, 0, 0, 1}, {0, 5, 0, 0}, {0, 7, 0, 0}, {4, 0, 0, 0}, {0, 8, 0, 0}, {3, 0, 0, 0}, {0, 0, 0, 5},
            {0, 0, 0, 8}, {0, 0, 0, 7}, {0, 1, 0, 0}, {0, 10, 0, 0}, {0, 0, 0, 6}, {2, 0, 0, 0}, {3, 0, 0, 0}, {0, 9, 0, 0},
            {4, 0, 0, 0}, {0, 0, 4, 0}, {0, 0, 0, 7}, {0, 8, 0, 0}, {0, 7, 0, 0}, {0, 7, 0, 0}, {0, 0, 2, 0}, {0, 0, 0, 0},
    };
    // end::data[]

    // tag::model[]
    public Model rcpsp(boolean lcg) {
        // Create the Choco model, with or without LCG
        Model model = new Model("RCPSP", SettingsBuilder.init().setLCG(lcg));
        // Create the start time variables for each activity
        IntVar[] starts = model.intVarArray("S", numActivities, 0, 999);
        Task[] tasks = IntStream.range(0, numActivities)
                .mapToObj(i -> model.taskVar(starts[i], durations[i]))
                .toArray(Task[]::new);

        // Add capacity constraints
        for (int r = 0; r < numResources; r++) {
            List<Task> cTasks = new ArrayList<>();
            List<IntVar> cHeights = new ArrayList<>();
            for (int i = 0; i < numActivities; i++) {
                if (requirements[i][r] > 0) {
                    cTasks.add(tasks[i]);
                    cHeights.add(model.intVar(requirements[i][r]));
                }
            }
            model.cumulative(cTasks.toArray(new Task[0]),
                    cHeights.toArray(new IntVar[0]),
                    model.intVar(resourceCapacities[r])
            ).post();
        }

        // Add precedence constraints
        for (int i = 0; i < numActivities; i++) {
            for (int j : successors[i]) {
                tasks[i].getEnd().le(tasks[j - 1].getStart()).post();
            }
        }

        // Define the objective function
        IntVar makespan = model.intVar("makespan", 0, 999);
        model.max(makespan, starts).post();
        model.setObjective(Model.MINIMIZE, makespan);

        // Declare a static search strategy
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(starts));
        return model;
    }
    // end::model[]

    @Test(groups = "10s")
    public void withoutLCG() {
        // tag::withoutLCG[]
        Model model = rcpsp(false);
        while (model.getSolver().solve()) ;
        model.getSolver().printShortStatistics();
        // end::withoutLCG[]
        Assert.assertEquals(model.getSolver().getBestSolutionValue().intValue(), 43);
    }

    @Test(groups = "10s")
    public void withLCG() {
        // tag::withLCG[]
        Model model = rcpsp(true);
        while (model.getSolver().solve()) ;
        model.getSolver().printShortStatistics();
        // end::withLCG[]
        Assert.assertEquals(model.getSolver().getBestSolutionValue().intValue(), 43);
    }
}
