/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.fahimi2018;

import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Comparator;

/**
 * Cumulative constraint filtering algorithms described in the following paper :
 * Fahimi, H., Ouellet, Y., Quimper, C.-G.: Linear-Time Filtering Algorithms for the Disjunctive Constraint and a Quadratic Filtering Algorithm for the Cumulative Not-First Not-Last. Constraints 23(3), pages 272–293 (2018). https://doi.org/10.1007/s10601-018-9282-9
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropCumulative extends CumulativeFilter {
    private Timeline overloadTimeline;
    private Timeline notFirstTimeline;

    public PropCumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean overloadCheck, boolean notFirst) {
        super(tasks, heights, capacity);
        this.overloadCheck = overloadCheck;
        this.notFirst = notFirst;

        overloadTimeline = new Timeline(tasks, heights, capacity);
        notFirstTimeline = new Timeline(tasks, heights, capacity);
    }

    @Override
    public void overloadCheck() throws ContradictionException {
        overloadTimeline.setCapacity(capacity.getUB());
        overloadTimeline.initializeTimeline();
        indexes.sort(Comparator.comparingInt(i -> tasks[i].getEnd().getUB()));
        for(int i : indexes) {
            overloadTimeline.scheduleTask(i);
            if(overloadTimeline.earliestCompletionTime()>capacity.getUB()*tasks[i].getEnd().getUB()) {
                aCause.fails();
            }
        }
    }

    @Override
    public boolean notFirst() throws ContradictionException {
        boolean hasFiltered = false;
        indexes.sort(Comparator.comparingInt(i -> tasks[i].getEnd().getUB()));
        for(int i : indexes) {
            if(!tasks[i].getStart().isInstantiated()) {
                int minEct = Integer.MAX_VALUE;
                notFirstTimeline.setCapacity(capacity.getUB()-heights[i].getLB());
                notFirstTimeline.initializeTimeline();
                for(int j : indexes) {
                    if(i!=j && tasks[i].getStart().getLB()<tasks[j].getEnd().getLB()) {
                        notFirstTimeline.scheduleTask(j);
                        minEct = Math.min(minEct, tasks[j].getEnd().getLB());
                        int lctj = tasks[j].getEnd().getUB();
                        if(notFirstTimeline.earliestCompletionTime()>capacity.getUB()*lctj-heights[i].getLB()*Math.min(tasks[i].getEnd().getLB(), lctj)) {
                            hasFiltered |= tasks[i].getStart().updateLowerBound(minEct, aCause);
                            break;
                        }
                    }
                }
            }
        }
        return hasFiltered;
    }

}
