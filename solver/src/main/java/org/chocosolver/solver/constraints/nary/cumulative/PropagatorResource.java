/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class offering useful services for Propagator implementing the Cumulative or Disjunctive constraints.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 17/06/2023
 */
public abstract class PropagatorResource extends Propagator<IntVar> {
    private static IntVar[] extractIntVars(Task[] tasks, IntVar[] heights, IntVar capacity, boolean filterOptionalTasks) {
        ArrayList<IntVar> list = new ArrayList<>();
        for (int i = 0; i < tasks.length; i++) {
            list.add(tasks[i].getStart());
            list.add(tasks[i].getDuration());
            list.add(tasks[i].getEnd());
            list.add(heights[i]);
        }
        list.add(capacity);
        for (int i = 0; i < tasks.length; i++) {
            if (filterOptionalTasks && !tasks[i].mustBePerformed() && tasks[i].mayBePerformed()) {
                list.add(((OptionalTask) tasks[i]).getPerformed());
            }
        }
        return list.toArray(new IntVar[0]);
    }

    /**
     * Returns true iff the combination of the task and the height variable can be considered as optional.
     *
     * @param task   a task
     * @param height its height variable in a propagator
     * @return true iff the combination of the task and the height variable can be considered as optional
     */
    public static boolean mayBePerformed(Task task, IntVar height) {
        return (height == null || height.getUB() > 0) && task.mayBePerformed();
    }

    /**
     * Returns true iff the combination of the task and the height variable must be considered as a performed task (not optional).
     *
     * @param task   a task
     * @param height its height variable in a propagator
     * @return true iff the combination of the task and the height variable must be considered as a performed task (not optional)
     */
    public static boolean mustBePerformed(Task task, IntVar height) {
        return (height == null || height.getLB() > 0) && task.mustBePerformed();
    }

    /**
     * Forces the task to be considered as optional. If the task must be performed, the height variable cannot be higher than 0. Otherwise, if the
     * task is optional, it is set as such. Else, a ContradictionException is thrown.
     *
     * @param task   a task
     * @param height a height variable
     * @param cause  the propagator that forces the task to be count as optional
     * @return true iff setting the task as optional has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterOptionalTask(Task task, IntVar height, ICause cause) throws ContradictionException {
        boolean isOpt = task instanceof OptionalTask;
        if (height != null && (!isOpt || task.mustBePerformed())) {
            // if the task is not optional or if it must be performed
            return height.updateUpperBound(0, cause);
        } else if (isOpt) {
            // if the task is optional and can be set as such
            return task.forceToBeOptional(cause);
        } else {
            ContradictionException ex = new ContradictionException();
            ex.set(cause, task.getStart(), "Task " + task + " should be set optional but it cannot be");
            throw ex;
        }
    }

    /**
     * Updates the earliest start time (est) of the task, considering its height variable. The height variable's upper bound is set to 0, or the task
     * can become optional if updating its est would empty the start variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param est    the est
     * @param cause  the propagator that filters the est
     * @return true iff updating the est has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterEst(Task task, IntVar height, int est, ICause cause) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && est > task.getLst()) {
            return height.updateUpperBound(0, cause);
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateEst(est, cause);
        } else {
            return false;
        }
    }

    /**
     * Updates the earliest start time (est) of the task, considering its height variable. The height variable's upper bound is set to 0, or the task
     * can become optional if updating its est would empty the start variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param est    the est
     * @param cause  the propagator that filters the est
     * @param reason the reason of the filtering
     * @return true iff updating the est has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterEst(Task task, IntVar height, int est, ICause cause, Reason reason) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && est > task.getLst()) {
            return height.updateUpperBound(0, cause, Reason.gather(reason, task.getStart().getMaxLit()));
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateEst(est, cause, reason);
        } else {
            return false;
        }
    }

    /**
     * Updates the latest start time (lst) of the task, considering its height variable. The height variable's upper bound is set to 0, or the task
     * can become optional if updating its lst would empty the start variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param lst    the lst
     * @param cause  the propagator that filters the est
     * @return true iff updating the lst has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterLst(Task task, IntVar height, int lst, ICause cause) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && lst < task.getEst()) {
            return height.updateUpperBound(0, cause);
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateLst(lst, cause);
        } else {
            return false;
        }
    }

    /**
     * Updates the latest start time (lst) of the task, considering its height variable. The height variable's upper bound is set to 0, or the task
     * can become optional if updating its lst would empty the start variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param lst    the lst
     * @param cause  the propagator that filters the est
     * @param reason the reason of the filtering
     * @return true iff updating the lst has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterLst(Task task, IntVar height, int lst, ICause cause, Reason reason) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && lst < task.getEst()) {
            return height.updateUpperBound(0, cause, Reason.gather(reason, task.getStart().getMinLit()));
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateLst(lst, cause, reason);
        } else {
            return false;
        }
    }

    /**
     * Updates the earliest completion time (ect) of the task, considering its height variable. The height variable's upper bound is set to 0, or the
     * task can become optional if updating its ect would empty the end variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param ect    the ect
     * @param cause  the propagator that filters the est
     * @return true iff updating the ect has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterEct(Task task, IntVar height, int ect, ICause cause) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && ect > task.getLct()) {
            return height.updateUpperBound(0, cause);
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateEct(ect, cause);
        } else {
            return false;
        }
    }

    /**
     * Updates the earliest completion time (ect) of the task, considering its height variable. The height variable's upper bound is set to 0, or the
     * task can become optional if updating its ect would empty the end variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param ect    the ect
     * @param cause  the propagator that filters the est
     * @param reason the reason of the filtering
     * @return true iff updating the ect has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterEct(Task task, IntVar height, int ect, ICause cause, Reason reason) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && ect > task.getLct()) {
            return height.updateUpperBound(0, cause, Reason.gather(reason, task.getEnd().getMaxLit()));
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateEct(ect, cause, reason);
        } else {
            return false;
        }
    }

    /**
     * Updates the latest completion time (lct) of the task, considering its height variable. The height variable's upper bound is set to 0, or the
     * task can become optional if updating its lct would empty the end variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param lct    the lct
     * @param cause  the propagator that filters the est
     * @return true iff updating the lct has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterLct(Task task, IntVar height, int lct, ICause cause) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && lct < task.getEct()) {
            return height.updateUpperBound(0, cause);
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateLct(lct, cause);
        } else {
            return false;
        }
    }

    /**
     * Updates the latest completion time (lct) of the task, considering its height variable. The height variable's upper bound is set to 0, or the
     * task can become optional if updating its lct would empty the end variable's domain.
     *
     * @param task   a task
     * @param height a height variable
     * @param lct    the lct
     * @param cause  the propagator that filters the est
     * @param reason the reason of the filtering
     * @return true iff updating the lct has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterLct(Task task, IntVar height, int lct, ICause cause, Reason reason) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() && lct < task.getEct()) {
            return height.updateUpperBound(0, cause, Reason.gather(reason, task.getEnd().getMinLit()));
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateLct(lct, cause, reason);
        } else {
            return false;
        }
    }

    /**
     * Updates the bounds of the task's duration variable, considering its height variable. The height variable's upper bound is set to 0, or the
     * task becomes optional if updating empties the duration variable's domain.
     *
     * @param task        a task
     * @param height      a height variable
     * @param minDuration the min duration
     * @param maxDuration the max duration
     * @param cause       the propagator that filters the est
     * @return true iff updating the bounds of the task's duration variable has filtered a variable
     * @throws ContradictionException an exception if a domain has been emptied
     */
    public static boolean filterDuration(Task task, IntVar height, int minDuration, int maxDuration, ICause cause) throws ContradictionException {
        if (height != null && height.getLB() == 0 && task.mustBePerformed() &&
                (maxDuration < task.getMinDuration() || task.getMaxDuration() < minDuration)) {
            return height.updateUpperBound(0, cause);
        } else if (height == null || height.getLB() > 0 || task instanceof OptionalTask) {
            return task.updateDuration(minDuration, maxDuration, cause);
        } else {
            return false;
        }
    }

    /**
     * Returns true iff the two tasks may be performed and they are intersecting, i.e. a.lst < b.ect && b.lst < a.ect.
     *
     * @param a a task
     * @param b another task
     * @return true iff the two tasks may be performed and they are intersecting
     */
    public static boolean intersect(Task a, Task b) {
        return a.mayBePerformed() && b.mayBePerformed() &&
                b.getLst() < a.getEct() && a.getLst() < b.getEct();
    }

    /**
     * Returns true iff either of the two tasks may not be performed or they are disjoint, i.e. a.est >= b.lct && b.est >= a.lct.
     *
     * @param a a task
     * @param b another task
     * @return true iff either of the two tasks may not be performed or they are disjoint
     */
    public static boolean disjoint(Task a, Task b) {
        return !a.mayBePerformed() || !b.mayBePerformed() || a.getEst() >= b.getLct() || b.getEst() >= a.getLct();
    }

    /**
     * Enforces the relation between start, duration and end variables of the tasks.
     *
     * @param tasks the tasks
     * @throws ContradictionException an exception thrown when enforcing task relation
     */
    public static void enforceTaskVariablesRelation(final List<Task> tasks) throws ContradictionException {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        }
    }

    /**
     * Returns the entailment status of the constraint (disjunctive or cumulative). More especially, it checks that : <ul>
     * <li>the relation start + duration = end is respected for all variables</li>
     * <li>if the constraint is a disjunctive one, no two tasks are performed and are intersecting</li>
     * <li>if the constraint is a cumulative one, the sum of tasks' height at each time point does not exceed the capacity</li>
     * </ul>
     *
     * @param isDisjunctive whether we are considering a disjunctive constraint (or a cumulative constraint if false)
     * @param tasks         the set of tasks
     * @param heights       the associated height variables
     * @param capacity      the capacity
     * @return the entailment status of the constraint
     */
    public static ESat isEntailed(boolean isDisjunctive, Task[] tasks, IntVar[] heights, IntVar capacity) {
        int n = tasks.length;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        // check start + duration = end
        for (int i = 0; i < n; i++) {
            if (tasks[i].mustBePerformed()) {
                min = Math.min(min, tasks[i].getLst());
                max = Math.max(max, tasks[i].getEct());
                if (tasks[i].mustBePerformed()
                        && (tasks[i].getEst() + tasks[i].getMinDuration() > tasks[i].getLct()
                        || tasks[i].getLst() + tasks[i].getMaxDuration() < tasks[i].getEct())) {
                    return ESat.FALSE;
                }
            }
        }
        // check capacity
        int maxLoad = 0;
        if (min <= max) {
            if (!isDisjunctive) {
                int[] consoMin = new int[max - min];
                for (int i = 0; i < n; i++) {
                    if (tasks[i].mustBePerformed()) {
                        for (int t = tasks[i].getLst(); t < tasks[i].getEct(); t++) {
                            consoMin[t - min] += heights[i].getLB();
                            if (consoMin[t - min] > capacity.getUB()) {
                                return ESat.FALSE;
                            }
                            maxLoad = Math.max(maxLoad, consoMin[t - min]);
                        }
                    }
                }
            } else {
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) {
                        if (
                                tasks[i].mustBePerformed() && heights[i].getLB() > 0 &&
                                        tasks[j].mustBePerformed() && heights[j].getLB() > 0 &&
                                        intersect(tasks[i], tasks[j])
                                        && tasks[i].getMinDuration() > 0 && tasks[j].getMinDuration() > 0
                        ) {
                            return ESat.FALSE;
                        }
                    }
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < n; i++) {
            if (
                    !tasks[i].getStart().isInstantiated() ||
                            !tasks[i].getDuration().isInstantiated() ||
                            !tasks[i].getEnd().isInstantiated() ||
                            !heights[i].isInstantiated() ||
                            tasks[i].mayBePerformed() && !tasks[i].mustBePerformed()
            ) {
                return ESat.UNDEFINED;
            }
        }
        if (!capacity.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        // capacity check entailed
        if (min <= max && maxLoad <= capacity.getLB()) {
            return ESat.TRUE;
        }
        // capacity not instantiated
        return ESat.UNDEFINED;
    }

    protected final boolean shouldRecomputePerformed;
    protected final boolean filterOptionalTasks;
    protected boolean isDisjunctive;
    protected Task[] tasks;
    protected IntVar[] heights;
    protected IntVar capacity;
    protected List<Integer> indexes;
    protected List<Task> performedTasks;
    protected List<Task> performedMirrorTasks;
    protected List<Task> performedAndOptionalTasks;
    protected List<Task> performedAndOptionalMirrorTasks;
    protected List<Integer> indexesWithOptional;
    protected List<IntVar> tasksHeights;
    protected List<IntVar> tasksHeightsWithOptional;

    protected PropagatorResource(boolean isDisjunctive, Task[] tasks, IntVar[] heights, IntVar capacity, PropagatorPriority prop) {
        this(isDisjunctive, tasks, heights, capacity, prop, false);
    }

    protected PropagatorResource(
            boolean isDisjunctive,
            Task[] tasks,
            IntVar[] heights,
            IntVar capacity,
            PropagatorPriority prop,
            boolean filterOptionalTasks
    ) {
        this(isDisjunctive, tasks, heights, capacity, prop, filterOptionalTasks, false);
    }

    protected PropagatorResource(
            boolean isDisjunctive,
            Task[] tasks,
            IntVar[] heights,
            IntVar capacity,
            PropagatorPriority prop,
            boolean filterOptionalTasks,
            boolean reactToFineEvt
    ) {
        super(extractIntVars(tasks, heights, capacity, filterOptionalTasks), prop, reactToFineEvt);

        this.isDisjunctive = isDisjunctive;
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;
        this.filterOptionalTasks = filterOptionalTasks;
        this.indexes = new ArrayList<>(tasks.length);
        this.performedTasks = new ArrayList<>(tasks.length);
        this.performedMirrorTasks = new ArrayList<>(tasks.length);
        this.tasksHeights = new ArrayList<>(tasks.length);
        if (filterOptionalTasks) {
            this.performedAndOptionalTasks = new ArrayList<>(tasks.length);
            this.performedAndOptionalMirrorTasks = new ArrayList<>(tasks.length);
            this.tasksHeightsWithOptional = new ArrayList<>(tasks.length);
            this.indexesWithOptional = new ArrayList<>(tasks.length);
        }

        boolean recompute = false;
        for (int i = 0; i < heights.length; i++) {
            if (heights[i].getLB() == 0 || tasks[i].mayBePerformed() && !tasks[i].mustBePerformed()) { // OptionalTask
                recompute = true;
                break;
            }
        }
        this.shouldRecomputePerformed = recompute;
    }

    @Override
    public int getPropagationConditions(int idx) {
        int n = tasks.length;
        if (idx == 4 * n) { // capacity variable
            return IntEventType.upperBoundAndInst();
        } else if (idx % 4 == 3 && idx < 4 * n) { // heights variables
            return IntEventType.lowerBoundAndInst();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    /**
     * Recomputes internal data structures useful for the propagator (should be overridden).
     */
    protected void recomputeDataStructure() {

    }

    /**
     * Enforces the relation between start, duration and end variables on all the tasks of the propagator (regardless their performance status).
     *
     * @throws ContradictionException an exception thrown when enforcing task relation
     */
    protected final void enforceTaskVariablesRelation() throws ContradictionException {
        for (int i = 0; i < tasks.length; i++) {
            tasks[i].propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        }
    }

    /**
     * Computes the collections of tasks regarding their performance status.
     */
    protected final void computeMustBePerformedTasks() {
        // TODO: should be shared by all propagators on a given resource to save memory and time
        if (shouldRecomputePerformed || model.getSolver().getNodeCount() == 0) {
            indexes.clear();
            performedTasks.clear();
            performedMirrorTasks.clear();
            tasksHeights.clear();
            if (filterOptionalTasks) {
                indexesWithOptional.clear();
                tasksHeightsWithOptional.clear();
                performedAndOptionalTasks.clear();
                performedAndOptionalMirrorTasks.clear();
            }
            for (int i = 0; i < tasks.length; i++) {
                if (mustBePerformed(tasks[i], heights[i])) {
                    indexes.add(performedTasks.size());
                    performedTasks.add(tasks[i]);
                    performedMirrorTasks.add(tasks[i].getMirror());
                    if (!isDisjunctive) {
                        tasksHeights.add(heights[i]);
                    }
                }
                if (filterOptionalTasks && mayBePerformed(tasks[i], heights[i])) {
                    indexesWithOptional.add(performedAndOptionalTasks.size());
                    performedAndOptionalTasks.add(tasks[i]);
                    performedAndOptionalMirrorTasks.add(tasks[i].getMirror());
                    tasksHeightsWithOptional.add(heights[i]);
                }
            }
            recomputeDataStructure();
        }
    }

    @Override
    public ESat isEntailed() {
        return isEntailed(isDisjunctive, tasks, heights, capacity);
    }
}

