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
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.sort.ArraySort;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropGraphCumulative extends PropagatorCumulative {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraph g;
    private final List<Task> tasksToFilter;
    private final List<IntVar> heightsToFilter;
    private final ISet toCompute;
    private long timestamp;
    private boolean full;
    private final boolean fast;
    private final IStateInt lastCapaMax;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Graph-based cumulative propagator:
     * - only filters over subsets of overlapping tasks
     *
     * @param tasks    tasks variables
     * @param heights  height variables
     * @param capacity capacity variable
     * @param fast     reduces the number of propagation (less filtering)
     */
    public PropGraphCumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean fast) {
        super(tasks, heights, capacity, PropagatorPriority.QUADRATIC, true, true);
        this.g = new UndirectedGraph(model, tasks.length, SetType.BITSET, true);
        this.tasksToFilter = new ArrayList<>(tasks.length);
        this.heightsToFilter = new ArrayList<>(tasks.length);
        this.toCompute = SetFactory.makeBipartiteSet(0);
        this.fast = fast;
        lastCapaMax = model.getEnvironment().makeInt(capacity.getUB() + 1);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            super.propagate(evtmask);
            graphComputation();
        } else {
            computeMustBePerformedTasks();
            if (full) {
                filter(performedAndOptionalTasks, tasksHeightsWithOptional, !fast);
            } else {
                int count = 0;
                ISetIterator tcIt = toCompute.iterator();
                while (tcIt.hasNext()) {
                    int i = tcIt.nextInt();
                    ISetIterator it = g.getNeighborsOf(i).iterator();
                    while (it.hasNext()) {
                        int j = it.nextInt();
                        if (disjoint(tasks[i], tasks[j])) {
                            g.removeEdge(i, j);
                        }
                    }
                    count += g.getNeighborsOf(i).size();
                    if (count >= 2 * tasks.length) {
                        break;
                    }
                }
                if (count >= 2 * tasks.length) {
                    filter(performedAndOptionalTasks, tasksHeightsWithOptional, !fast);
                } else {
                    ISetIterator iter = toCompute.iterator();
                    while (iter.hasNext()) {
                        filterAround(iter.nextInt());
                    }
                }
            }
        }
        toCompute.clear();
        full = false;
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (timestamp != model.getEnvironment().getTimeStamp()) {
            timestamp = model.getEnvironment().getTimeStamp();
            toCompute.clear();
            full = false;
        }
        if (varIdx < 4 * tasks.length) {
            int v = varIdx / 4;
            if (!shouldConsiderTask(v)) {
                ISetIterator gIt = g.getNeighborsOf(v).iterator();
                while (gIt.hasNext()) {
                    g.removeEdge(v, gIt.nextInt());
                }
            } else if (tasks[v].hasCompulsoryPart() || !fast) {
                toCompute.add(v);
            }
        } else {
            full = true;
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    private void filterAround(int taskIndex) throws ContradictionException {
        tasksToFilter.clear();
        heightsToFilter.clear();
        tasksToFilter.add(tasks[taskIndex]);
        heightsToFilter.add(heights[taskIndex]);
        ISetIterator env = g.getNeighborsOf(taskIndex).iterator();
        while (env.hasNext()) {
            int neighborIndex = env.nextInt();
            tasksToFilter.add(tasks[neighborIndex]);
            heightsToFilter.add(heights[neighborIndex]);
        }
        filter(tasksToFilter, heightsToFilter, !fast);
    }

    private boolean shouldConsiderTask(final int indexTask) {
        return tasks[indexTask].mayBePerformed() && tasks[indexTask].getMaxDuration() > 0 && heights[indexTask].getUB() > 0;
    }

    private void graphComputation() {
        final int n = tasks.length;
        for (int i = 0; i < n; i++) {
            g.getNeighborsOf(i).clear();
        }
        Event[] events = new Event[2 * n];
        ArraySort<Event> sort = new ArraySort<>(events.length, true, false);
        Comparator<Event> eventComparator = (e1, e2) -> {
            if (e1.date == e2.date) {
                return e1.type - e2.type;
            }
            return e1.date - e2.date;
        };
        BitSet tprune = new BitSet(n);
        for (int i = 0; i < n; i++) {
            if (shouldConsiderTask(i)) {
                events[i] = new Event();
                events[i].set(START, i, tasks[i].getEst());
                events[i + n] = new Event();
                events[i + n].set(END, i, tasks[i].getLct());
            }
        }
        sort.sort(events, 2 * n, eventComparator);
        int timeIndex = 0;
        while (timeIndex < n * 2) {
            Event event = events[timeIndex++];
            switch (event.type) {
                case (START):
                    for (int i = tprune.nextSetBit(0); i >= 0; i = tprune.nextSetBit(i + 1)) {
                        g.addEdge(i, event.index);
                    }
                    tprune.set(event.index);
                    break;
                case (END):
                    tprune.clear(event.index);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    private static class Event {
        protected int type;
        protected int index;
        protected int date;

        protected void set(int t, int i, int d) {
            date = d;
            type = t;
            index = i;
        }
    }

    private final static int START = 1, END = 2;
}
