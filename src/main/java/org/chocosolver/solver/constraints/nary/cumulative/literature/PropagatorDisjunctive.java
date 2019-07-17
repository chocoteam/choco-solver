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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * Cumulative propagator
 * Encapsulates filtering algorithm for the cumulative and disjunctive constraints.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropagatorDisjunctive extends Propagator<IntVar> {
    private Task[] tasks;
    private DisjunctiveFilter[] filters;
    private boolean atLeastOneTimeTabling;

    public PropagatorDisjunctive(Task[] tasks, DisjunctiveFilter... filters) {
        super(extractIntVars(tasks), PropagatorPriority.QUADRATIC, false);

        this.tasks = tasks;
        this.filters = filters;
        this.atLeastOneTimeTabling = false;

        for(DisjunctiveFilter filter : filters) {
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
            for(DisjunctiveFilter filter : filters) {
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
        // check disjunctive
        if (min <= max) {
            int[] consoMin = new int[max - min];
            for (int i = 0; i < n; i++) {
                for (int t = tasks[i].getStart().getUB(); t < tasks[i].getEnd().getLB(); t++) {
                    consoMin[t - min] += 1;
                    if (consoMin[t - min] > 1) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < vars.length - 1; i++) {
            if (!vars[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }
}
