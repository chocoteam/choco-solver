/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.PropagatorEventType;

import java.util.List;

public class SchedulingUtils {
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

    private SchedulingUtils() {
    }
}
