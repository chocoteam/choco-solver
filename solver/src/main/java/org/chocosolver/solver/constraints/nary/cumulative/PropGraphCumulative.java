/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.sort.ArraySort;

import java.util.BitSet;
import java.util.Comparator;

/**
 * Graph based cumulative
 * Maintains incrementally overlapping tasks
 * Performs energy checking and mandatory part based filtering
 * BEWARE : not idempotent, use two propagators to get the fix point
 *
 * @author Jean-Guillaume Fages
 * @since 31/01/13
 */
public class PropGraphCumulative extends PropCumulative {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraph g;
    private final ISet tasks;
    private final ISet toCompute;
    private long timestamp;
    private boolean full;
    private final boolean fast;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Graph-based cumulative propagator:
     * - only filters over subsets of overlapping tasks
     *
     * @param s       start 		variables
     * @param d       duration	variables
     * @param e       end			variables
     * @param h       height		variables
     * @param capa    capacity	variable
	 * @param fast    reduces the number of propagation (less filtering)
     * @param filters filtering algorithm to use
     */
    public PropGraphCumulative(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, boolean fast,
                               CumulFilter... filters) {
        super(s, d, e, h, capa, true, filters);
        this.g = new UndirectedGraph(model, n, SetType.BITSET, true);
        this.tasks = SetFactory.makeBipartiteSet(0);
        this.toCompute = SetFactory.makeBipartiteSet(0);
		this.fast = fast;
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
            if(full){
                filter(allTasks);
            }else {
                int count = 0;
                ISetIterator tcIt = toCompute.iterator();
                while (tcIt.hasNext()){
                    int i = tcIt.nextInt();
                    for (int j : g.getNeighborsOf(i)) {
                        if (disjoint(i, j)) {
                            g.removeEdge(i, j);
                        }
                    }
                    count += g.getNeighborsOf(i).size();
                    if(count >= 2*n)break;
                }
                if (count >= 2*n) {
                    filter(allTasks);
                } else {
                    ISetIterator iter = toCompute.iterator();
                    while (iter.hasNext()){
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
        if (varIdx < 4 * n) {
            int v = varIdx % n;
            if(h[v].getUB()==0 || d[v].getUB()==0){
                allTasks.remove(v);
                ISetIterator gIt = g.getNeighborsOf(v).iterator();
                while (gIt.hasNext()){
                    g.removeEdge(v,gIt.nextInt());
                }
            }else if(s[v].getUB()<e[v].getLB() || !fast){
                toCompute.add(v);
            }
        } else {
            updateMaxCapa();
            full = true;
        }
		forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    protected void filterAround(int taskIndex) throws ContradictionException {
        tasks.clear();
        tasks.add(taskIndex);
        ISetIterator env = g.getNeighborsOf(taskIndex).iterator();
        while (env.hasNext()) {
            tasks.add(env.nextInt());
        }
        filter(tasks);
    }

    private boolean disjoint(int i, int j) {
        return s[i].getLB() >= e[j].getUB() || s[j].getLB() >= e[i].getUB();
    }

    private void graphComputation() {
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
            events[i] = new Event();
            events[i].set(START, i, s[i].getLB());
            events[i + n] = new Event();
            events[i + n].set(END, i, e[i].getUB());
        }
        sort.sort(events, 2 * n, eventComparator);
        int timeIndex = 0;
        while (timeIndex < n * 2) {
            Event event = events[timeIndex++];
            switch (event.type) {
                case (START):
                    boolean eok = h[event.index].getUB()>0 && d[event.index].getUB()>0;
                    if(eok) {
                        for (int i = tprune.nextSetBit(0); i >= 0; i = tprune.nextSetBit(i + 1)) {
                            if(h[i].getUB()>0 && d[i].getUB()>0) {
                                g.addEdge(i, event.index);
                            }
                        }
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
