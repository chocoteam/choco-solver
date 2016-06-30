/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
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
    private ISet tasks, toCompute;
    private long timestamp;
    private boolean full, fast;

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
                               Cumulative.Filter... filters) {
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
                for (int i : toCompute) {
                    for (int j : g.getNeighOf(i)) {
                        if (disjoint(i, j)) {
                            g.removeEdge(i, j);
                        }
                    }
                    count += g.getNeighOf(i).size();
                    if(count >= 2*n)break;
                }
                if (count >= 2*n) {
                    filter(allTasks);
                } else {
                    for (int i : toCompute) {
                        filterAround(i);
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
                for(int j:g.getNeighOf(v)){
                    g.removeEdge(v,j);
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
        ISet env = g.getNeighOf(taskIndex);
        for (int i : env) {
            tasks.add(i);
        }
        filter(tasks);
    }

    private boolean disjoint(int i, int j) {
        return s[i].getLB() >= e[j].getUB() || s[j].getLB() >= e[i].getUB();
    }

    private void graphComputation() {
        for (int i = 0; i < n; i++) {
            g.getNeighOf(i).clear();
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
