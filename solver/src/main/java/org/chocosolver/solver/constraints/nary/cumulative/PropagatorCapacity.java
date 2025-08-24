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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.ISchedulingFactory;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Propagator used to filter the capacity variable and the height variables of tasks in a Cumulative constraint.
 * More particularly, it assures that the following equation :
 * <p>
 * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
 * <p>
 * Il also assures that all heights variables and capacity variable are positive.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 17/06/2023
 */
@Explained
public class PropagatorCapacity extends Propagator<IntVar> {
    private final IStateInt lastCapaMax;
    private final Task[] tasks;
    private final IntVar[] heights;
    private final IntVar capacity;

    public PropagatorCapacity(final Task[] tasks, final IntVar[] heights, final IntVar capacity) {
        super(ArrayUtils.append(ISchedulingFactory.extractDurationVars(tasks), heights, new IntVar[]{capacity}), PropagatorPriority.LINEAR, true);
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;
        lastCapaMax = model.getEnvironment().makeInt(capacity.getUB() + 1);
    }

    @Override
    public int getPropagationConditions(int idx) {
        if (idx == 2 * tasks.length) { // capacity variable
            return IntEventType.upperBoundAndInst();
        } else {
            return IntEventType.lowerBoundAndInst();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 2 * tasks.length) { // capacity variable
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        } else {
            propagateTask(idxVarInProp % tasks.length);
        }
    }

    /**
     * Propagates the capacity constraint for the task at index i. More particularly, it enforces the following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propagateTask(final int i) throws ContradictionException {
        if (lcg()) {
            propTaskExplained(i);
        } else {
            propTask(i);
        }
    }

    /**
     * Propagates the capacity constraint for the task at index i. More particularly, it enforces the following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propTask(final int i) throws ContradictionException {
        if (capacity.getUB() < heights[i].getLB()) {
            if (tasks[i].mustBePerformed()) {
                tasks[i].updateDuration(0, 0, this);
            } else {
                tasks[i].forceToBeOptional(this);
            }
        } else if (tasks[i].mustBePerformed() && tasks[i].getMinDuration() > 0) {
            heights[i].updateUpperBound(capacity.getUB(), this);
            capacity.updateLowerBound(heights[i].getLB(), this);
        }
    }

    /**
     * Propagates the capacity constraint for the task at index i with explanations. More particularly, it enforces the
     * following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propTaskExplained(final int i) throws ContradictionException {
        if (capacity.getUB() < heights[i].getLB()) {
            if (tasks[i].mustBePerformed()) {
                final Reason reason = tasks[i] instanceof OptionalTask
                        ? Reason.r(heights[i].getGELit(capacity.getUB()), ((OptionalTask) tasks[i]).getPerformed().getMinLit())
                        : Reason.r(heights[i].getGELit(capacity.getUB()));
                tasks[i].updateDuration(0, 0, this, reason);
            } else {
                tasks[i].forceToBeOptional(this, Reason.r(heights[i].getGELit(capacity.getUB())));
            }
        } else if (tasks[i].mustBePerformed() && tasks[i].getMinDuration() > 0) {
            final int durationLit = tasks[i].getDuration().getGELit(1);
            final Reason reasonHeight;
            final Reason reasonCapacity;
            if (tasks[i] instanceof OptionalTask) {
                final int performedLit = ((OptionalTask) tasks[i]).getPerformed().getMinLit();
                reasonHeight = Reason.r(capacity.getMaxLit(), durationLit, performedLit);
                reasonCapacity = Reason.r(heights[i].getMinLit(), durationLit, performedLit);
            } else {
                reasonHeight = Reason.r(capacity.getMaxLit(), durationLit);
                reasonCapacity = Reason.r(heights[i].getMinLit(), durationLit);
            }
            heights[i].updateUpperBound(capacity.getUB(), this, reasonHeight);
            capacity.updateLowerBound(heights[i].getLB(), this, reasonCapacity);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            capacity.updateLowerBound(0, this);
            for (int i = 0; i < tasks.length; i++) {
                heights[i].updateLowerBound(0, this);
            }
        }
        final int capaMax = capacity.getUB();
        if (lastCapaMax.get() != capaMax) {
            // No need to run the following instructions if the maximum capacity has not changed since last run
            lastCapaMax.set(capaMax);
            boolean shouldPassivate = true;
            for (int i = 0; i < tasks.length; i++) {
                propagateTask(i);
                if (tasks[i].mayBePerformed() && isUndefined(i)) {
                    // the equation is not entailed or is undefined for this task
                    shouldPassivate = false;
                }
            }
            if (shouldPassivate) {
                setPassive();
            }
        }
    }

    /**
     * Returns true iff the status of the task at index i is undefined, <i>i.e.</i> its duration can be strictly
     * greater than 0 and its height higher than the capacity.
     *
     * @param i the index of the task
     * @return true iff the status of the task at index i is undefined
     */
    private boolean isUndefined(final int i) {
        return tasks[i].getMaxDuration() > 0 && capacity.getLB() < heights[i].getUB();
    }

    @Override
    public ESat isEntailed() {
        boolean undefined = false;
        for (int i = 0; i < tasks.length; ++i) {
            final boolean undef = isUndefined(i);
            if (tasks[i].mustBePerformed()) {
                if (tasks[i].getMinDuration() > 0 && heights[i].getLB() > capacity.getUB()) {
                    return ESat.FALSE;
                } else if (undef) {
                    undefined = true;
                }
            } else if (tasks[i].mayBePerformed() && undef) {
                undefined = true;
            }
        }
        return undefined ? ESat.UNDEFINED : ESat.TRUE;
    }
}
