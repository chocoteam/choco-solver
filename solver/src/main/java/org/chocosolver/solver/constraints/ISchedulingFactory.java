/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import java.util.*;
import java.util.function.Function;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cumulative.PropagatorCapacity;
import org.chocosolver.solver.constraints.nary.cumulative.PropagatorCumulative;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetTimes;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/**
 * Interface to make and declare everything useful for scheduling problems (Task objects, constraints, search heuristics, etc.)
 *
 * @author Arthur GODET <arth.godet@gmail.com>
 * @since 17/08/2021
 */
public interface ISchedulingFactory extends ISelf<Model> {
    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// CUMULATIVE /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * heights should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks    Task objects containing start, duration and end variables
     * @param heights  integer variables representing the resource consumption of each task
     * @param capacity integer variable representing the resource capacity
     * @return a cumulative constraint
     */
    default Constraint cumulative(List<Task> tasks, List<IntVar> heights, IntVar capacity) {
        if (tasks.size() != heights.size()) {
            throw new SolverException("Tasks and heights arrays should have same size");
        }
        Task[] tasksArray = new Task[tasks.size()];
        IntVar[] heightsArray = new IntVar[tasks.size()];
        for (int i = 0; i < tasks.size(); ++i) {
            tasksArray[i] = tasks.get(i);
            heightsArray[i] = heights.get(i);
        }
        return cumulative(tasksArray, heightsArray, capacity);
    }

    /**
     * Creates a cumulative constraint: Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * heights should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param tasks    Task objects containing start, duration and end variables
     * @param heights  integer variables representing the resource consumption of each task
     * @param capacity integer variable representing the resource capacity
     * @return a cumulative constraint
     */
    default Constraint cumulative(Task[] tasks, IntVar[] heights, IntVar capacity) {
        if (tasks.length != heights.length) {
            throw new SolverException("Tasks and heights arrays should have same size");
        }
        // keep only tasks that can have impact on resource consumption
        final List<Task> tasksToKeep = new ArrayList<>();
        final List<IntVar> heightsToKeep = new ArrayList<>();
        for (int i = 0; i < heights.length; i++) {
            if (heights[i].getUB() > 0 && tasks[i].getMaxDuration() > 0 && tasks[i].mayBePerformed()) {
                tasksToKeep.add(tasks[i]);
                heightsToKeep.add(heights[i]);
            }
        }
        if (tasksToKeep.isEmpty()) {
            return ref().arithm(capacity, ">=", 0);
        }
        final Task[] keptTasks = new Task[tasksToKeep.size()];
        final IntVar[] keptHeights = new IntVar[tasksToKeep.size()];
        for (int i = 0; i < tasksToKeep.size(); ++i) {
            keptTasks[i] = tasksToKeep.get(i);
            keptHeights[i] = heightsToKeep.get(i);
        }
        return new Constraint(
                ConstraintsName.CUMULATIVE,
                new PropagatorCumulative(keptTasks, keptHeights, capacity),
                new PropagatorCapacity(keptTasks, keptHeights, capacity)
        );
    }

    /**
     * Creates a cumulative constraint:
     * Enforces that at each point in time,
     * the cumulated height of the set of tasks that overlap that point
     * does not exceed a given limit.
     * <p>
     * Task duration and height should be >= 0
     * Discards tasks whose duration or height is equal to zero
     *
     * @param starts    starting time of each task
     * @param durations processing time of each task
     * @param heights   resource consumption of each task
     * @param capacity  resource capacity
     */
    default Constraint cumulative(IntVar[] starts, int[] durations, int[] heights, int capacity) {
        int n = starts.length;
        final IntVar[] d = new IntVar[n];
        final IntVar[] h = new IntVar[n];
        final IntVar[] e = new IntVar[n];
        Task[] tasks = new Task[n];
        for (int i = 0; i < n; i++) {
            d[i] = ref().intVar(durations[i]);
            h[i] = ref().intVar(heights[i]);
            e[i] = ref().intVar(starts[i].getName() + "_e",
                    starts[i].getLB() + durations[i],
                    starts[i].getUB() + durations[i],
                    true);
            tasks[i] = new Task(starts[i], d[i], e[i]);
        }
        return cumulative(tasks, h, ref().intVar(capacity));
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// SEARCH ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    static IntVar[] extractVars(Task[] tasks, Function<Task, IntVar> function) {
        return Arrays.stream(tasks).map(function).toArray(IntVar[]::new);
    }

    static IntVar[] extractStartVars(Task[] tasks) {
        return extractVars(tasks, Task::getStart);
    }

    static IntVar[] extractDurationVars(Task[] tasks) {
        return extractVars(tasks, Task::getDuration);
    }

    static IntVar[] extractEndVars(Task[] tasks) {
        return extractVars(tasks, Task::getEnd);
    }

    /**
     * Search for scheduling problems described in the following paper :
     * Godard, D., Laborie, P., Nuijten, W.: Randomized large neighborhood search for cumulative scheduling. In: Biundo, S., Myers, K.L., Rajan, K. (eds.) Proceedings of the Fifteenth International Conference on Automated Planning and Scheduling(ICAPS 2005), June 5-10 2005, Monterey, California, USA. pp. 81–89. AAAI (2005),http://www.aaai.org/Library/ICAPS/2005/icaps05-009.php
     * <p>
     * Beware that the search is not complete in general, as stated in the paper.
     *
     * @param tasks the tasks
     * @return the strategy
     */
    default IntStrategy setTimes(Task[] tasks) {
        SetTimes setTimes = new SetTimes(tasks);
        ref().post(new Constraint("SET_TIMES", setTimes));
        return Search.intVarSearch(setTimes, new IntDomainMin(), extractStartVars(tasks));
    }

    enum ArbitrationRule {
        MIN_EST, MAX_EST, MIN_LST, MAX_LST, MIN_ECT, MAX_ECT, MIN_LCT, MAX_LCT
    }

    /**
     * Returns true if the first task is before the second task according to the rule.
     * If the rule is MIN_EST(0), then returns task1.est < task2.est
     * If the rule is MAX_EST(1), then returns task1.est > task2.est
     * If the rule is MIN_LST(2), then returns task1.lst < task2.lst
     * If the rule is MAX_LST(3), then returns task1.lst > task2.lst
     * If the rule is MIN_ECT(4), then returns task1.eet < task2.eet
     * If the rule is MAX_ECT(5), then returns task1.eet > task2.eet
     * If the rule is MIN_LCT(6), then returns task1.let < task2.let
     * If the rule is MAX_LCT(7), then returns task1.let > task2.let
     *
     * @param task1 the first task
     * @param task2 the second task
     * @param rule  the rule
     * @return true iff task1 is before task2 according to the rule
     */
    default boolean before(Task task1, Task task2, ArbitrationRule rule) {
        switch (rule) {
            case MIN_EST:
                return task1.getStart().getLB() < task2.getStart().getLB();
            case MAX_EST:
                return task1.getStart().getLB() > task2.getStart().getLB();
            case MIN_LST:
                return task1.getStart().getUB() < task2.getStart().getUB();
            case MAX_LST:
                return task1.getStart().getUB() > task2.getStart().getUB();
            case MIN_ECT:
                return task1.getEnd().getLB() < task2.getEnd().getLB();
            case MAX_ECT:
                return task1.getEnd().getLB() > task2.getEnd().getLB();
            case MIN_LCT:
                return task1.getEnd().getUB() < task2.getEnd().getUB();
            case MAX_LCT:
                return task1.getEnd().getUB() > task2.getEnd().getUB();
            default:
                throw new UnsupportedOperationException("Task comparison should be either MIN_EST(0), MAX_EST(1), MIN_LST(2), MAX_LST(3), MIN_EET(4), MAX_EET(5), MIN_LET(6) or MAX_LET(7).");
        }
    }

    /**
     * Builds a strategy that selects the task's start with the smallest lower bound (in case of equality, it selects the task with the smallest end's lower bound).
     *
     * @param tasks the tasks
     * @return the strategy
     */
    default IntStrategy smallest(Task[] tasks) {
        return smallest(tasks, ArbitrationRule.MIN_ECT);
    }

    default IntStrategy smallest(Task[] tasks, ArbitrationRule arbitrationRule) {
        IntVar[] starts = extractStartVars(tasks);
        return Search.intVarSearch(
                variables -> {
                    int idx = -1;
                    for (int i = 0; i < tasks.length; i++) {
                        if (!starts[i].isInstantiated()) {
                            if (idx == -1
                                    || starts[i].getLB() < starts[idx].getLB()
                                    || starts[i].getLB() == starts[idx].getLB() && before(tasks[i], tasks[idx], arbitrationRule)
                            ) {
                                idx = i;
                            }
                        }
                    }
                    return idx == -1 ? null : starts[idx];
                },
                new IntDomainMin(),
                starts
        );
    }
}
