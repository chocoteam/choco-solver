/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/*
@author Arthur Godet <arth.godet@gmail.com>
@since 25/09/2020
*/

package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.sort.ArraySort;

import java.util.Comparator;
import java.util.List;

public class EventPointSeries {
    protected Event[] eventsArray;
    protected int nbEvents;
    protected int timeIndex;
    protected Comparator<Event> comparator = Event::compareTo;
    protected ArraySort<Event> sort;

    public EventPointSeries(int nbTasks, int nbMaxEventsPerTask) {
        eventsArray = new Event[nbMaxEventsPerTask * nbTasks];
        for (int i = 0; i < eventsArray.length; i++) {
            eventsArray[i] = new Event();
        }
        sort = new ArraySort<>(eventsArray.length, true, false);
    }

    public boolean isEmpty() {
        return timeIndex >= nbEvents;
    }

    public int size() {
        return nbEvents - timeIndex;
    }

    public void generateEvents(List<Task> tasks, boolean generatePREvents, boolean generateCCPEvents, boolean mergeScpAndCcpEvents) {
        nbEvents = 0;
        timeIndex = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.mustBePerformed()) {
                if (generatePREvents) {
                    // start min can be filtered
                    if (!task.getStart().isInstantiated()) {
                        eventsArray[nbEvents++].set(Event.PR, i, task.getEst());
                    }
                }
                if (mergeScpAndCcpEvents) {
                    eventsArray[nbEvents++].set(Event.SCP, i, task.getLst());
                    if (task.getLst() < task.getEct()) {
                        eventsArray[nbEvents++].set(Event.ECP, i, task.getEct());
                    }
                } else {
                    // a compulsory part exists
                    if (task.getLst() < task.getEct()) {
                        eventsArray[nbEvents++].set(Event.SCP, i, task.getLst());
                        eventsArray[nbEvents++].set(Event.ECP, i, task.getEct());
                    } else if (generateCCPEvents) { // conditional compulsory part
                        eventsArray[nbEvents++].set(Event.CCP, i, task.getLst());
                    }
                }
            }
        }
        if (!isEmpty()) {
            sort.sort(eventsArray, nbEvents, comparator);
        }
    }

    public Event getEvent() {
        return eventsArray[timeIndex];
    }

    public Event removeEvent() {
        return eventsArray[timeIndex++];
    }

    public void swap(int index1, int index2) {
        Event tmp = eventsArray[index1];
        eventsArray[index1] = eventsArray[index2];
        eventsArray[index2] = tmp;
    }

    public void addEvent(int type, int idxTask, int date) {
        int pos = nbEvents;
        eventsArray[nbEvents++].set(type, idxTask, date);
        while (timeIndex <= pos - 1 && comparator.compare(eventsArray[pos - 1], eventsArray[pos]) > 0) {
            swap(pos - 1, pos);
            pos--;
        }
    }

    public void updateEvent(int type, int idxTask, Updater updater) {
        int pos = timeIndex;
        while (pos < nbEvents && (eventsArray[pos].type != type || eventsArray[pos].indexTask != idxTask)) {
            pos++;
        }
        if (pos < nbEvents) {
            updater.update(eventsArray[pos]);
            while (pos < nbEvents - 1 && comparator.compare(eventsArray[pos], eventsArray[pos + 1]) > 0) {
                swap(pos, pos + 1);
                pos++;
            }
        }
    }

    public void updateCompulsoryPartEvents(int idxTask, Task task) {
        if (task.getLst() < task.getEct()) {
            updateEvent(Event.SCP, idxTask, e -> e.date = task.getLst());
            updateEvent(Event.ECP, idxTask, e -> e.date = task.getEct());
        }
    }

    public int getNextDate() {
        int pos = timeIndex;
        int date = eventsArray[pos].date;
        while (pos < nbEvents && eventsArray[pos].date == date) {
            pos++;
        }
        if (pos < nbEvents) {
            return eventsArray[pos].date;
        } else {
            return date;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EventPointSeries[");
        for (int pos = timeIndex; pos < nbEvents; pos++) {
            sb.append(eventsArray[pos]);
            if (pos < nbEvents - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @FunctionalInterface
    public interface Updater {
        void update(Event e);
    }
}
