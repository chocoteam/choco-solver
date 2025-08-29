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

/**
 * The class Event is used to represent a meaningful time event inside an {@link EventPointSeries}.
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
public class Event implements Comparable<Event> {
    /**
     * A constant representing the SCP event, which is the start of the compulsory part of a task.
     */
    public static final int SCP = 0;
    /**
     * A constant representing the ECP event, which is the end of the compulsory part of a task.
     */
    public static final int ECP = 1;

    /**
     * The type of the event (should be SCP or ECP).
     */
    private int type;
    /**
     * The index of the task in a mapping array or list.
     */
    private int indexTask;
    /**
     * The time-point of the event.
     */
    private int time;

    /**
     * Instantiates a new Event.
     */
    public Event() {
        // Nothing to instantiate here, it will be done in the set() function
    }

    /**
     * Set the event.
     *
     * @param type      the type of the event (should be SCP or ECP)
     * @param indexTask the index of the task in a mapping array or list
     * @param time      the time-point of the event
     */
    public void set(int type, int indexTask, int time) {
        this.type = type;
        this.indexTask = indexTask;
        this.time = time;
    }

    /**
     * Returns the type of the event.
     *
     * @return the type of the event
     */
    public int getType() {
        return this.type;
    }

    /**
     * Returns the index of the task in the mapping array or list.
     *
     * @return the index of the task in the mapping array or list
     */
    public int getIndexTask() {
        return this.indexTask;
    }

    /**
     * Return the time-point of the event.
     *
     * @return the time-point of the event
     */
    public int getTime() {
        return this.time;
    }

    @Override
    public int compareTo(Event o) {
        if (this.time == o.time) {
            return -(this.type - o.type);
        }
        return this.time - o.time;
    }

    @Override
    public String toString() {
        String typeStr;
        switch (this.type) {
            case SCP:
                typeStr = "SCP";
                break;
            case ECP:
                typeStr = "ECP";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return "Event<" + typeStr + "," + indexTask + "," + time + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            Event event = (Event) obj;
            return this.type == event.type && this.time == event.time && this.indexTask == event.indexTask;
        }
        return false;
    }
}
