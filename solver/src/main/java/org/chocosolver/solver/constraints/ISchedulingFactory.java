/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.alternative.PropAlternative;
import org.chocosolver.solver.constraints.nary.cumulative.PropagatorCumulative;
import org.chocosolver.solver.constraints.nary.span.PropSpan;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.SetTimes;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Interface to make and declare everything useful for scheduling problems (Task objects, constraints, search heuristics, etc.)
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 17/08/2021
 */
public interface ISchedulingFactory extends ISelf<Model> {
    static Propagator<IntVar>[] tasksToBePosted(Task[] tasks) {
        return Arrays.stream(tasks).filter(p -> !p.isPassive()).toArray(Propagator[]::new);
    }

    static Propagator<IntVar>[] tasksToBePosted(Task[] tasks, Task task) {
        return tasksToBePosted(ArrayUtils.append(tasks, new Task[]{task}));
    }

    static Task[] tasksForConstraint(Task[] tasks) {
        return Arrays.stream(tasks).filter(Task::mayBePerformed).toArray(Task[]::new);
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// ALTERNATIVE ////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a task that is the alternative over the tasks in parameters. Either : <ul>
     *     <li>the alternative task is not performed if none of the tasks are performed</li>
     *     <li>the alternative task is performed if exactly one task is performed and their starts and ends are the same</li>
     * </ul>
     *
     * @param tasks the tasks to create an alternative over
     * @return the alternative task
     */
    default Task alternative(Task[] tasks) {
        return alternative(tasks, false);
    }

    /**
     * Creates a task that is the alternative over the tasks in parameters. Either : <ul>
     *     <li>the alternative task is not performed if none of the tasks are performed</li>
     *     <li>the alternative task is performed and, for all performed tasks, their starts and ends are the same as the one of the alternative task</li>
     * </ul>
     *
     * @param tasks the tasks to create an alternative over
     * @param allowSeveralTasks whether to allow several tasks to be performed and have the same start and end as the alternative task
     * @return the alternative task
     */
    default Task alternative(Task[] tasks, boolean allowSeveralTasks) {
        int est = Integer.MAX_VALUE;
        int lct = Integer.MIN_VALUE;
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = Integer.MIN_VALUE;
        boolean mustBePerformed = false;
        for (int i = 0; i < tasks.length; ++i) {
            if (tasks[i].mayBePerformed()) {
                est = Math.min(est, tasks[i].getEst());
                lct = Math.max(lct, tasks[i].getLct());
                minDuration = Math.min(minDuration, tasks[i].getMinDuration());
                maxDuration = Math.max(maxDuration, tasks[i].getMaxDuration());
                mustBePerformed |= tasks[i].mustBePerformed();
            }
        }
        if (est == Integer.MAX_VALUE) { // either tasks.length == 0 or none of tasks may be performed
            return ref().taskVar(ref().intVar(0), ref().intVar(0), ref().boolVar(false));
        }
        IntVar start = ref().intVar(est, lct - maxDuration);
        IntVar duration = ref().intVar(minDuration, maxDuration);
        IntVar end = ref().intVar(est + minDuration, lct);
        Task alter;
        if (mustBePerformed) {
            alter = new Task(start, duration, end);
        } else {
            alter = new OptionalTask(start, duration, end, ref().boolVar());
        }
        if (allowSeveralTasks) {
            IntVar varN = ref().intVar(0, tasks.length);
            ref().alternative(alter, tasks, varN).post();
        } else {
            ref().alternative(alter, tasks).post();
        }
        return alter;
    }

    /**
     * Creates an alternative constraint such that alternative(alter, tasks) is true iff <ul>
     *    <li>either alter is not performed and none of tasks are performed</li>
     *    <li>either alter is performed and exactly one of the tasks is performed and has same start and end as alter</li>
     *  </ul>
     *
     * @param alter the alternative task
     * @param tasks the tasks
     * @return an alternative constraint
     */
    default Constraint alternative(Task alter, Task[] tasks) {
        return alternative(alter, tasks, ref().intVar(0, 1));
    }

    /**
     * Creates an alternative constraint such that alternative(alter, tasks, varN) is true iff <ul>
     *    <li>either alter is not performed and none of tasks are performed and varN = 0</li>
     *    <li>either alter is performed and exactly varN elements of tasks are performed and all performed tasks have same start and end as alter</li>
     *  </ul>
     *
     * @param alter the alternative task
     * @param tasks the tasks
     * @param varN the number of tasks that are performed when alter is performed
     * @return an alternative constraint
     */
    default Constraint alternative(Task alter, Task[] tasks, IntVar varN) {
        Task[] tasksForConstraint = tasksForConstraint(tasks);
        if (ref().getSolver().isLCG()) {
            if (ref().getSettings().warnUser()) {
                ref().getSolver().log().white().println(
                        "Warning: alternative constraint is turned into clauses (due to LCG).");
            }
            ref().alternativeDec(alter, tasksForConstraint, varN);
            return ref().voidConstraint();
        }
        return Constraint.merge(
            ConstraintsName.ALTERNATIVE,
            new Constraint(
                ConstraintsName.ALTERNATIVE,
                ArrayUtils.append(
                    tasksToBePosted(tasks, alter),
                    new Propagator[]{new PropAlternative(alter, tasksForConstraint, varN)}
                )
            ),
            ref().sum(Arrays.stream(tasksForConstraint).map(Task::getPerformed).toArray(BoolVar[]::new), "=", varN),
            ref().sum(Arrays.stream(tasksForConstraint).map(Task::getPerformed).toArray(BoolVar[]::new), ">=", alter.getPerformed())
        );
    }

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
    default Constraint cumulative(final List<Task> tasks, final List<IntVar> heights, final IntVar capacity) {
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
    default Constraint cumulative(final Task[] tasks, final IntVar[] heights, final IntVar capacity) {
        return cumulative(tasks, heights, capacity, false, false);
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
     * @param energyNaive whether to apply the naive energy filtering
     * @param disjunctiveEnergyNaive whether to apply the naive disjunctive energy filtering
     * @return a cumulative constraint
     */
    default Constraint cumulative(
            final Task[] tasks,
            final IntVar[] heights,
            final IntVar capacity,
            final boolean energyNaive,
            final boolean disjunctiveEnergyNaive
    ) {
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
        Propagator<IntVar> prop =
            new PropagatorCumulative(keptTasks, keptHeights, capacity, energyNaive, disjunctiveEnergyNaive);
        return new Constraint(
            ConstraintsName.CUMULATIVE,
            ArrayUtils.append(
                tasksToBePosted(tasks),
                new Propagator[]{prop}
            )
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
    default Constraint cumulative(final IntVar[] starts, final int[] durations, final int[] heights, final int capacity) {
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
            tasks[i] = ref().taskVar(starts[i], d[i], e[i]);
        }
        return cumulative(tasks, h, ref().intVar(capacity));
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// SEARCH ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    static IntVar[] extractVars(final Task[] tasks, final Function<Task, IntVar> function) {
        return Arrays.stream(tasks).map(function).toArray(IntVar[]::new);
    }

    static IntVar[] extractStartVars(final Task[] tasks) {
        return extractVars(tasks, Task::getStart);
    }

    static IntVar[] extractDurationVars(final Task[] tasks) {
        return extractVars(tasks, Task::getDuration);
    }

    static IntVar[] extractEndVars(final Task[] tasks) {
        return extractVars(tasks, Task::getEnd);
    }

    static BoolVar[] extractPerformedVars(final Task[] tasks) {
        return Arrays.stream(tasks).map(Task::getPerformed).toArray(BoolVar[]::new);
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
    default IntStrategy setTimes(final Task[] tasks) {
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
    default boolean before(final Task task1, final Task task2, final ArbitrationRule rule) {
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
    default IntStrategy smallest(final Task[] tasks) {
        return smallest(tasks, ArbitrationRule.MIN_ECT);
    }

    default IntStrategy smallest(final Task[] tasks, final ArbitrationRule arbitrationRule) {
        List<IntVar> vars = new ArrayList<>();
        for (int i = 0; i < tasks.length; ++i) {
            if (tasks[i] instanceof OptionalTask optionalTask) {
                vars.add(optionalTask.getPerformed());
            }
            vars.add(tasks[i].getStart());
            vars.add(tasks[i].getDuration());
            vars.add(tasks[i].getEnd());
        }
        return Search.intVarSearch(
                variables -> {
                    int idx = -1;
                    for (int i = 0; i < tasks.length; i++) {
                        if (tasks[i].mayBePerformed() && !tasks[i].isInstantiated()) {
                            if (idx == -1
                                || tasks[i].getEst() < tasks[idx].getEst()
                                || tasks[i].getEst() == tasks[idx].getEst() && before(tasks[i], tasks[idx], arbitrationRule)
                            ) {
                                idx = i;
                            }
                        }
                    }
                    if (idx == -1) {
                        return null;
                    } else {
                        return nextUninstantiatedVariable(tasks[idx]);
                    }
                },
                new IntDomainMin(),
                vars.toArray(IntVar[]::new)
        );
    }

    private static IntVar nextUninstantiatedVariable(Task task) {
        if (!task.mustBePerformed()) {
            return task.getPerformed();
        } else if (!task.isStartInstantiated()) {
            return task.getStart();
        } else if (!task.isEndInstantiated()) {
            return task.getEnd();
        } else if (!task.isDurationInstantiated()) {
            return task.getDuration();
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// SPAN ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a task that is the span over the tasks in parameters. Either : <ul>
     *     <li>the span task is not performed if none of the tasks are performed</li>
     *     <li>the span task is performed if at least one task is performed and the span task starts when the earliest performed task starts and the span task ends when the latest performed task ends</li>
     * </ul>
     *
     * @param tasks the tasks to create a span over
     * @return the span task
     */
    default Task span(Task[] tasks) {
        int est = Integer.MAX_VALUE;
        int lct = Integer.MIN_VALUE;
        boolean mustBePerformed = false;
        for (int i = 0; i < tasks.length; ++i) {
            if (tasks[i].mayBePerformed()) {
                est = Math.min(est, tasks[i].getEst());
                lct = Math.max(lct, tasks[i].getLct());
                mustBePerformed |= tasks[i].mustBePerformed();
            }
        }
        if (est == Integer.MAX_VALUE) { // either tasks.length == 0 or none of tasks may be performed
            return ref().taskVar(ref().intVar(0), ref().intVar(0), ref().boolVar(false));
        }
        IntVar start = ref().intVar(est, lct);
        IntVar duration = ref().intVar(0, lct - est);
        IntVar end = ref().intVar(est, lct);
        Task span;
        if (mustBePerformed) {
            span = new Task(start, duration, end);
        } else {
            span = new OptionalTask(start, duration, end, ref().boolVar());
        }
        ref().span(span, tasks).post();
        return span;
    }

    /**
     * Creates a span constraint such that span(span, tasks) is true iff <ul>
     *    <li>either span is not performed and none of tasks are performed</li>
     *    <li>either span is performed and at least one of tasks is performed and span starts when the earliest performed task starts and span ends when the latest performed task ends</li>
     *  </ul>
     *
     * @param span the span task
     * @param tasks the tasks
     * @return a span constraint
     */
    default Constraint span(Task span, Task[] tasks) {
        Task[] tasksConstraint = Arrays.stream(tasks).filter(Task::mayBePerformed).toArray(Task[]::new);
        return span(span, tasksConstraint, ref().intVar(0, tasksConstraint.length));
    }

    /**
     * Creates a span constraint such that span(span, tasks, varN) is true iff <ul>
     *    <li>either span is not performed and none of tasks are performed and varN = 0</li>
     *    <li>either span is performed and span starts when the earliest performed task starts and span ends when the latest performed task ends and exactly varN tasks are performed</li>
     *  </ul>
     *
     * @param span the span task
     * @param tasks the tasks
     * @param varN the number of tasks that are performed when span is performed
     * @return a span constraint
     */
    default Constraint span(Task span, Task[] tasks, IntVar varN) {
        Task[] tasksForConstraint = tasksForConstraint(tasks);
        if (ref().getSolver().isLCG()) {
            if (ref().getSettings().warnUser()) {
                ref().getSolver().log().white().println(
                        "Warning: span constraint is turned into clauses (due to LCG).");
            }
            ref().spanDec(span, tasksForConstraint, varN);
            return ref().voidConstraint();
        }
        return Constraint.merge(
            ConstraintsName.SPAN,
            new Constraint(
                ConstraintsName.SPAN,
                ArrayUtils.append(
                    tasksToBePosted(tasks, span),
                    new Propagator[]{new PropSpan(span, tasksForConstraint, varN)}
                )
            ),
            ref().sum(Arrays.stream(tasksForConstraint).map(Task::getPerformed).toArray(BoolVar[]::new), "=", varN),
            ref().sum(Arrays.stream(tasksForConstraint).map(Task::getPerformed).toArray(BoolVar[]::new), ">=", span.getPerformed())
        );
    }
}
