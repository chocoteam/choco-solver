/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Cumulative propagator
 * Encapsulates filtering algorithm for the cumulative and disjunctive constraints.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropagatorCumulative extends Propagator<IntVar> {
    private Task[] tasks;
    private IntVar[] heights;
    private IntVar capacity;
    private CumulativeFilter[] filters;
    private boolean atLeastOneTimeTabling;

    public PropagatorCumulative(Task[] tasks, CumulativeFilter... filters) {
        this(tasks, tasks[0].getStart().getModel().intVarArray(tasks.length, 1, 1), tasks[0].getStart().getModel().intVar(1), filters);
    }

    public PropagatorCumulative(Task[] tasks, IntVar[] heights, IntVar capacity, CumulativeFilter... filters) {
        super(ArrayUtils.append(extractIntVars(tasks), heights, new IntVar[]{capacity}), PropagatorPriority.VERY_SLOW, false);
        if(tasks.length != heights.length) {
            throw new SolverException("Arrays of task and heights should have the same size in cumulative constraint.");
        }

        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;
        this.filters = filters;
        this.atLeastOneTimeTabling = false;

        for(CumulativeFilter filter : filters) {
            filter.setPropagator(this);
            atLeastOneTimeTabling |= (filter.timeTable || filter.timeTableExtendedEdgeFinding);
        }
    }

    private static IntVar[] extractIntVars(Task[] tasks) {
        IntVar[] array = new IntVar[tasks.length*3];
        for(int i = 0; i<tasks.length; i++) {
            array[3*i] = tasks[i].getStart();
            array[3*i+1] = tasks[i].getDuration();
            array[3*i+2] = tasks[i].getEnd();
        }
        return array;
    }

    @Override
    public int getPropagationConditions(int idx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasFiltered;
        do {
            hasFiltered = false;
            for(CumulativeFilter filter : filters) {
                hasFiltered |= filter.propagate();
            }
        } while(hasFiltered);

        // ensures that timetabling check is applied even if no such algorithm is contained in the propagator
        if(!atLeastOneTimeTabling && isEntailed() == ESat.FALSE) {
            this.fails();
        }
    }

    @Override
    public ESat isEntailed() {
        int n = tasks.length;
        int min = tasks[0].getStart().getUB();
        int max = tasks[0].getEnd().getLB();
        // check start + duration = end
        for (int i = 0; i < n; i++) {
            min = Math.min(min, tasks[i].getStart().getUB());
            max = Math.max(max, tasks[i].getEnd().getLB());
            if (tasks[i].getStart().getLB() + tasks[i].getDuration().getLB() > tasks[i].getEnd().getUB()
                    || tasks[i].getStart().getUB() + tasks[i].getDuration().getUB() < tasks[i].getEnd().getLB()) {
                return ESat.FALSE;
            }
        }
        // check capacity
        int maxLoad = 0;
        if (min <= max) {
            int capamax = capacity.getUB();
            int[] consoMin = new int[max - min];
            for (int i = 0; i < n; i++) {
                for (int t = tasks[i].getStart().getUB(); t < tasks[i].getEnd().getLB(); t++) {
                    consoMin[t - min] += heights[i].getLB();
                    if (consoMin[t - min] > capamax) {
                        return ESat.FALSE;
                    }
                    maxLoad = Math.max(maxLoad, consoMin[t - min]);
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < vars.length - 1; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        assert min <= max;
        // capacity check entailed
        if (maxLoad <= capacity.getLB()) {
            return ESat.TRUE;
        }
        // capacity not instantiated
        return ESat.UNDEFINED;
    }
}
