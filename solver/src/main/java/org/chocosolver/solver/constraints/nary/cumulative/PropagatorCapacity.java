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
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class PropagatorCapacity extends Propagator<IntVar> {
    private static IntVar[] extractDurationVars(Task[] tasks) {
        IntVar[] durations = new IntVar[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            durations[i] = tasks[i].getDuration();
        }
        return durations;
    }

    protected final IStateInt lastCapaMax;
    private final Task[] tasks;
    private final IntVar[] heights;
    private final IntVar capacity;

    public PropagatorCapacity(Task[] tasks, IntVar[] heights, IntVar capacity) {
        super(ArrayUtils.append(extractDurationVars(tasks), heights, new IntVar[]{capacity}), PropagatorPriority.LINEAR, true);
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

    private void propagateTask(int i) throws ContradictionException {
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

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            capacity.updateLowerBound(0, this);
            for (int i = 0; i < tasks.length; i++) {
                heights[i].updateLowerBound(0, this);
            }
        }
        int capaMax = capacity.getUB();
        if (lastCapaMax.get() != capaMax) {
            lastCapaMax.set(capaMax);
            int sumHeights = 0;
            for (int i = 0; i < tasks.length; i++) {
                propagateTask(i);
                if (tasks[i].mayBePerformed()) {
                    sumHeights += heights[i].getUB();
                }
            }
            if (sumHeights <= capacity.getLB()) {
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return PropagatorResource.isEntailed(false, tasks, heights, capacity);
    }
}
