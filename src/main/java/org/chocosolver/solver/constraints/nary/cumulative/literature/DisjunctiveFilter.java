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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.sort.ArraySort;

/**
 * Abstract class for cumulative constraint filtering algorithms.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public abstract class DisjunctiveFilter {
    protected Task[] tasks;
    protected Propagator<IntVar> aCause;

    protected int[] indexes;
    protected ArraySort arraySort;

    protected boolean overloadCheck;
    protected boolean timeTable;
    protected boolean edgeFinding;
    protected boolean extendedEdgeFinding;
    protected boolean timeTableExtendedEdgeFinding;
    protected boolean notFirst;
    protected boolean energeticReasoning;

    /**
     * Declares a class that contains filtering algorithms for the disjunctive constraint
     *
     * @param tasks			task variables (embed start, duration and end variables)
     */
    public DisjunctiveFilter(Task[] tasks) {
        this.tasks = tasks;

        this.indexes = new int[tasks.length];
        for(int i = 0; i<tasks.length; i++) {
            indexes[i] = i;
        }
        arraySort = new ArraySort(indexes.length, false, true);
    }

    public void setPropagator(Propagator<IntVar> aCause) {
        this.aCause = aCause;
    }

    /**
     * Propagates all the filtering algorithms contained in the class
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean propagate() throws ContradictionException {
        boolean hasFiltered = false;

        if(overloadCheck) {
            overloadCheck();
        }

        if(timeTableExtendedEdgeFinding) {
            hasFiltered = timeTableExtendedEdgeFinding();
        } else {
            if(timeTable) {
                hasFiltered = timeTable();
            }
            if(extendedEdgeFinding) {
                hasFiltered |= extendedEdgeFinding();
            }
            else if(edgeFinding) {
                hasFiltered |= edgeFinding();
            }
        }

        if(notFirst) {
            hasFiltered |= notFirst();
        }

        if(energeticReasoning) {
            hasFiltered |= energeticReasoning();
        }

        return hasFiltered;
    }

    /**
     * Applies Overload checking
     */
    public void overloadCheck() throws ContradictionException {

    }

    /**
     * Applies Timetabling filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean timeTable() throws ContradictionException {
        return false;
    }

    /**
     * Applies not-first/not-last filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean notFirst() throws ContradictionException {
        return false;
    }

    /**
     * Applies energetic reasoning filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean energeticReasoning() throws ContradictionException {
        return false;
    }

    /**
     * Applies Edge-Finding filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean edgeFinding() throws ContradictionException {
        return false;
    }

    /**
     * Applies Extended Edge-Finding filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean extendedEdgeFinding() throws ContradictionException {
        return false;
    }

    /**
     * Applies Timetable-Extended-Edge-Finding filtering
     *
     * @return <i>true</i> if at least one of the filtering algorithm has filtered some values.
     */
    public boolean timeTableExtendedEdgeFinding() throws ContradictionException {
        return false;
    }

}
