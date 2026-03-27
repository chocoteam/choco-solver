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

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Arrays;
import java.util.BitSet;

import static org.chocosolver.solver.constraints.nary.cumulative.SchedulingUtils.mustBePerformed;

/**
 * Class representing the Profile data structure described in <a href="https://doi.org/10.1007/978-3-319-23219-5_11">Gay, S., Hartert, R., and Schaus, P.: “Simple and Scalable Time-Table Filtering for the Cumulative Constraint”. In: Principles and Practice of Constraint Programming - 21st International Conference, CP 2015, Cork, Ireland, August 31 - September 4, 2015, Proceedings. Ed. by Gilles Pesant. Vol. 9255. Lecture Notes in Computer Science. Springer, 2015, pp. 149–157</a>.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
public class Profile {
    private final int[] timePoints;
    private final int[] heights;
    private final BitSet[] indexesTask;
    private final EventPointSeries eventPointSeries;
    private final BitSet list;
    private int idx;
    private int[] time;
    private int min;
    private int max;

    /**
     * Instantiates a new Profile.
     *
     * @param nbTasks the maximum number of tasks the Profile will manage
     */
    public Profile(final int nbTasks) {
        idx = 0;
        timePoints = new int[2 * (nbTasks + 1)];
        heights = new int[2 * (nbTasks + 1)];
        indexesTask = new BitSet[2 * (nbTasks + 1)];
        for (int i = 0; i < indexesTask.length; ++i) {
            indexesTask[i] = new BitSet();
        }
        eventPointSeries = new EventPointSeries(nbTasks);
        list = new BitSet(nbTasks);
        time = new int[31];
    }

    /**
     * Clears the profile.
     */
    public void clear() {
        idx = 0;
    }

    /**
     * Returns the size of the profile, <i>i.e.</i> the number of rectangles.
     *
     * @return the size of the profile
     */
    public int size() {
        return idx - 1;
    }

    /**
     * Returns the start of the rectangle at index j.
     *
     * @param j the index of the rectangle
     * @return the start of the rectangle at index j
     */
    public int getStartRectangle(final int j) {
        return timePoints[j];
    }

    /**
     * Returns the end of the rectangle at index j.
     *
     * @param j the index of the rectangle
     * @return the end of the rectangle at index j
     */
    public int getEndRectangle(final int j) {
        return timePoints[j + 1];
    }

    /**
     * Returns the height of the rectangle at index j.
     *
     * @param j the index of the rectangle
     * @return the height of the rectangle at index j
     */
    public int getHeightRectangle(final int j) {
        return heights[j];
    }


    /**
     * Builds the profile for the tasks and their corresponding heights variables.
     *
     * @param tasks        the tasks
     * @param tasksHeights the heights variables of the tasks
     * @param activeTasks  the indexes of tasks to consider
     */
    public void buildProfile(final Task[] tasks, final IntVar[] tasksHeights, final IStateBitSet activeTasks) {
        clear();
        timePoints[idx] = Integer.MIN_VALUE;
        heights[idx] = 0;
        idx++;
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            min = Math.min(min, tasks[i].getEst());
            max = Math.max(max, tasks[i].getLct());
        }
        if (!tasks[0].getStart().getModel().getSolver().isLCG()
                && max - min < activeTasks.cardinality() * activeTasks.cardinality()) {
            buildProfileNaive(tasks, tasksHeights, activeTasks);
        } else {
            buildProfileSweep(tasks, tasksHeights, activeTasks);
        }
        timePoints[idx] = Integer.MAX_VALUE;
        heights[idx] = 0;
        idx++;
    }

    /**
     * Builds the profile for the tasks and their corresponding heights variables, using a sweep algorithm.
     *
     * @param tasks        the tasks
     * @param tasksHeights the heights variables of the tasks
     * @param activeTasks  the indexes of tasks to consider
     */
    private void buildProfileSweep(final Task[] tasks, final IntVar[] tasksHeights, final IStateBitSet activeTasks) {
        eventPointSeries.generateEvents(tasks, tasksHeights, activeTasks);
        if (!eventPointSeries.isEmpty()) {
            int h = 0;
            list.clear();
            final boolean lcg = tasksHeights[0].getModel().getSolver().isLCG();
            while (!eventPointSeries.isEmpty()) {
                timePoints[idx] = eventPointSeries.getTimeFirstEvent();
                while (!eventPointSeries.isEmpty() && eventPointSeries.getTimeFirstEvent() == timePoints[idx]) {
                    Event event = eventPointSeries.removeFirstEvent();
                    if (event.getType() == Event.SCP) {
                        if (lcg) {
                            list.set(event.getIndexTask());
                        }
                        h += tasksHeights[event.getIndexTask()].getLB();
                    } else {
                        if (lcg) {
                            list.clear(event.getIndexTask());
                        }
                        h -= tasksHeights[event.getIndexTask()].getLB();
                    }
                }
                if (lcg || h != heights[idx - 1]) {
                    heights[idx] = h;
                    if (lcg) {
                        indexesTask[idx].clear();
                        if (!list.isEmpty()) indexesTask[idx].or(list);
                    }
                    idx++;
                }
                assert h >= 0;
            }
            assert h == 0;
        }
    }

    /**
     * Builds the profile for the tasks and their corresponding heights variables, using a naive approach.
     *
     * @param tasks        the tasks
     * @param tasksHeights the heights variables of the tasks
     * @param activeTasks  the indexes of tasks to consider
     */
    private void buildProfileNaive(final Task[] tasks, final IntVar[] tasksHeights, final IStateBitSet activeTasks) {
        min = Integer.MAX_VALUE / 2;
        max = Integer.MIN_VALUE / 2;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (tasks[i].hasCompulsoryPart() && mustBePerformed(tasks[i], tasksHeights[i])) {
                min = Math.min(min, tasks[i].getLst());
                max = Math.max(max, tasks[i].getEct());
            }
        }
        if (min < max) {
            if (max - min > time.length) {
                time = new int[max - min];
            } else {
                Arrays.fill(time, 0, max - min, 0);
            }
            fillTime(tasks, tasksHeights, activeTasks);
            fillRectangles();
        }
    }

    /**
     * Fills the time array, representing the resource consumption at a given time point, from the compulsory part of
     * tasks that must be performed.
     *
     * @param tasks        the tasks
     * @param tasksHeights the heights variables
     * @param activeTasks  the indexes of tasks to consider
     */
    private void fillTime(final Task[] tasks, final IntVar[] tasksHeights, final IStateBitSet activeTasks) {
        // Fill mandatory parts
        int ect;
        int hlb;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (tasks[i].hasCompulsoryPart() && mustBePerformed(tasks[i], tasksHeights[i])) {
                ect = tasks[i].getEct();
                hlb = tasksHeights[i].getLB();
                for (int t = tasks[i].getLst(); t < ect; t++) {
                    time[t - min] += hlb;
                }
            }
        }
    }

    /**
     * Fills the indexesTask lists of tasks' indexes contributing to the rectangle k of the profile.
     *
     * @return the list of indexes of tasks contributing to the rectangle k
     */
    public BitSet fillList(int k) {
        return indexesTask[k];
    }

    /**
     * Fills the rectangles data structures from the time array. Should be called after the
     * {@link #fillTime(Task[], IntVar[], IStateBitSet)} function.
     */
    private void fillRectangles() {
        int t = min;
        int currentHeight = time[t - min];
        timePoints[idx] = t;
        heights[idx] = currentHeight;
        idx++;
        while (t < max) {
            while (t < max && time[t - min] == currentHeight) {
                ++t;
            }
            if (t < max) {
                currentHeight = time[t - min];
            } else {
                currentHeight = 0;
            }
            timePoints[idx] = t;
            heights[idx] = currentHeight;
            idx++;
        }
    }

    /**
     * Finds and returns the index of the rectangle containing the time-point in parameter.
     * Runs in logarithmic time in the number of rectangles.
     *
     * @param time the time-point
     * @return the index of the rectangle containing the time-point
     */
    public int find(final int time) {
        int i1 = 0;
        int i2 = size();
        while (i1 < i2) {
            int im = (i1 + i2) / 2;
            if (timePoints[im] <= time && time < timePoints[im + 1]) {
                i1 = im;
                i2 = im;
            } else if (timePoints[im] < time) {
                i1 = im + 1;
            } else if (timePoints[im] > time) {
                i2 = im - 1;
            }
        }
        return i1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Profile[");
        if (size() > 0) {
            for (int i = 0; i < size(); i++) {
                sb.append("<").append(timePoints[i]).append(",").append(timePoints[i + 1]).append(",").append(heights[i]).append(">");
                if (i < size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("<").append(timePoints[size()]).append(",").append(Integer.MAX_VALUE).append(",").append(0).append(">");
        } else {
            sb.append("<").append(Integer.MIN_VALUE).append(",").append(Integer.MAX_VALUE).append(",").append(0).append(">");
        }
        sb.append("]");
        return sb.toString();
    }
}
