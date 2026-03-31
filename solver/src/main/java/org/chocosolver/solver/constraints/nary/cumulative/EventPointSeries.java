/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 25/09/2020
*/

package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.sort.ArraySort;

import java.util.Comparator;
import java.util.List;

import static org.chocosolver.solver.constraints.nary.cumulative.SchedulingUtils.mustBePerformed;

/**
 * Class representing a series of {@link Event}.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
public class EventPointSeries {
    /**
     * The array of events.
     */
    private final Event[] eventsArray;
    /**
     * The index of the last event in the series.
     */
    private int nbEvents;
    /**
     * The index of the first event in the series.
     */
    private int indexFirstEvent;
    /**
     * The comparator used on events.
     */
    private final Comparator<Event> comparator = Event::compareTo;
    /**
     * Used to sort the array of events.
     */
    private final ArraySort<Event> sort;

    /**
     * Instantiates a new EventPointSeries.
     *
     * @param nbTasks the maximum number of tasks the EventPointSeries will manage
     */
    public EventPointSeries(int nbTasks) {
        eventsArray = new Event[2 * nbTasks];
        for (int i = 0; i < eventsArray.length; i++) {
            eventsArray[i] = new Event();
        }
        sort = new ArraySort<>(eventsArray.length, true, false);
    }

    /**
     * Returns true iff the series is empty.
     *
     * @return true iff the series is empty
     */
    public boolean isEmpty() {
        return indexFirstEvent >= nbEvents;
    }

    /**
     * Returns the size of the series.
     *
     * @return the size of the series
     */
    public int size() {
        return nbEvents - indexFirstEvent;
    }

    /**
     * Generates SCP and ECP events (start and end of compulsory parts) of the tasks and sorts them.
     *
     * @param tasks the tasks for which SCP and ECP events should be generated
     * @param heights the height variables of the tasks
     */
    public void generateEvents(final List<Task> tasks, final List<IntVar> heights) {
        nbEvents = 0;
        indexFirstEvent = 0;
        for (int i = 0; i < tasks.size(); i++) {
            final Task task = tasks.get(i);
            if (mustBePerformed(task, heights.get(i)) && task.hasCompulsoryPart()) {
                eventsArray[nbEvents++].set(Event.SCP, i, task.getLst());
                eventsArray[nbEvents++].set(Event.ECP, i, task.getEct());
            }
        }
        if (!isEmpty()) {
            sort.sort(eventsArray, nbEvents, comparator);
        }
    }

    /**
     * Generates SCP and ECP events (start and end of compulsory parts) of the tasks and sorts them.
     *
     * @param tasks the tasks for which SCP and ECP events should be generated
     * @param heights the height variables of the tasks
     * @param activeTasks the indexes of tasks to consider
     */
    public void generateEvents(final Task[] tasks, final IntVar[] heights, final IStateBitSet activeTasks) {
        nbEvents = 0;
        indexFirstEvent = 0;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            final Task task = tasks[i];
            if (mustBePerformed(task, heights[i]) && task.hasCompulsoryPart()) {
                eventsArray[nbEvents++].set(Event.SCP, i, task.getLst());
                eventsArray[nbEvents++].set(Event.ECP, i, task.getEct());
            }
        }
        if (!isEmpty()) {
            sort.sort(eventsArray, nbEvents, comparator);
        }
    }

    /**
     * Returns the time of the first event in the series.
     *
     * @return the time of the first event in the series
     */
    public int getTimeFirstEvent() {
        return eventsArray[indexFirstEvent].getTime();
    }

    /**
     * Removes and returns the first event in the series.
     *
     * @return the first event in the series
     */
    public Event removeFirstEvent() {
        return eventsArray[indexFirstEvent++];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EventPointSeries[");
        for (int pos = indexFirstEvent; pos < nbEvents; pos++) {
            sb.append(eventsArray[pos]);
            if (pos < nbEvents - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
