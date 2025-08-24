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

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.List;

/**
 * Class representing the Profile data structure described in <a href="https://doi.org/10.1007/978-3-319-23219-5_11">Gay, S., Hartert, R., and Schaus, P.: “Simple and Scalable Time-Table Filtering for the Cumulative Constraint”. In: Principles and Practice of Constraint Programming - 21st International Conference, CP 2015, Cork, Ireland, August 31 - September 4, 2015, Proceedings. Ed. by Gilles Pesant. Vol. 9255. Lecture Notes in Computer Science. Springer, 2015, pp. 149–157</a>.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
public class Profile {
    private final int[] timePoints;
    private final int[] heights;
    private final TIntArrayList[] indexesTask;
    private final EventPointSeries eventPointSeries;
    private int idx;

    /**
     * Instantiates a new Profile.
     *
     * @param nbTasks the maximum number of tasks the Profile will manage
     */
    public Profile(final int nbTasks) {
        idx = 0;
        timePoints = new int[2 * (nbTasks + 1)];
        heights = new int[2 * (nbTasks + 1)];
        indexesTask = new TIntArrayList[2 * (nbTasks + 1)];
        for (int i = 0; i < indexesTask.length; ++i) {
            indexesTask[i] = new TIntArrayList();
        }
        eventPointSeries = new EventPointSeries(nbTasks);
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
        return idx - 2;
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
     * Returns the indexes of the tasks whose compulsory part contributes to the rectangle at index j.
     *
     * @param j the index of the rectangle
     * @return the indexes of the tasks whose compulsory part contributes to the rectangle at index j
     */
    public TIntArrayList getIndexesTaskRectangle(final int j) {
        return indexesTask[j];
    }

    /**
     * Builds the profile for the tasks and their corresponding heights variables.
     *
     * @param tasks        the tasks
     * @param tasksHeights the heights variables of the tasks
     */
    public void buildProfile(final List<Task> tasks, final List<IntVar> tasksHeights) {
        clear();
        timePoints[idx] = Integer.MIN_VALUE;
        heights[idx] = 0;
        idx++;
        eventPointSeries.generateEvents(tasks);
        if (!eventPointSeries.isEmpty()) {
            int h = 0;
            final TIntArrayList list = new TIntArrayList();
            while (!eventPointSeries.isEmpty()) {
                timePoints[idx] = eventPointSeries.getTimeFirstEvent();
                while (!eventPointSeries.isEmpty() && eventPointSeries.getTimeFirstEvent() == timePoints[idx]) {
                    Event event = eventPointSeries.removeFirstEvent();
                    if (event.getType() == Event.SCP) {
                        list.add(event.getIndexTask());
                        h += tasksHeights.get(event.getIndexTask()).getLB();
                    } else {
                        list.remove(event.getIndexTask());
                        h -= tasksHeights.get(event.getIndexTask()).getLB();
                    }
                }
                heights[idx] = h;
                indexesTask[idx].clear();
                indexesTask[idx].addAll(list);
                idx++;
                assert h >= 0;
            }
            assert h == 0;
        }
        timePoints[idx] = Integer.MAX_VALUE;
        heights[idx] = 0;
        idx++;
    }

    /**
     * Finds and returns the index of the rectangle containing the time-point in parameter.
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
