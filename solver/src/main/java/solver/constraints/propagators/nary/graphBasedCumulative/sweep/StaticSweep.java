/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.graphBasedCumulative.sweep;

import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.dataStructures.Event;
import solver.constraints.propagators.nary.scheduling.dataStructures.IncHeapEvents;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

public class StaticSweep {

	Propagator prop;
    private DynamicSweepMin sweepMin;
    private DynamicSweepMax sweepMax;
    private final int aggregateMode;
    private int[] mapping;
	private int nbTasks;
	private IntVar[] vars;
	private int capa;

    // results
    int nbTasksInFilteringAlgo;
    int nbEventsToAdd;
	private ICause aCause;


	public StaticSweep(IntVar[] vars, int capa, Propagator prop, int aggregatedProfile, ICause aCause) {
        this.vars = vars;
		this.prop = prop;
		this.aCause = aCause;
		this.capa = capa;
        this.aggregateMode = aggregatedProfile;
		this.nbTasks = vars.length/4;
        this.nbTasksInFilteringAlgo = nbTasks;
        this.nbEventsToAdd = 0;
    }


    /**
     * Copy bounds of rtasks into arrays.
     * Build an aggregated cumulative profile with instantiated tasks.
     */
    public int[][] copyAndAggregate(int[] ls, int[] us, int[] ld, int[] le, int[] ue, int[] h) {
        nbEventsToAdd = 0;
        mapping = new int[nbTasks];
        if (aggregateMode != 0) {
            int copyIdx = 0;
            IncHeapEvents hEvents = new IncHeapEvents(2 * nbTasks);
            int[][] eventsToAdd = null;
            for (int is = 0, id = nbTasks, ie = 2 * nbTasks, ih = 3 * nbTasks; is < nbTasks; is++, id++, ie++, ih++) {
                if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated() || !vars[ih].instantiated()) {
                    ls[copyIdx] = vars[is].getLB();
                    us[copyIdx] = vars[is].getUB();
                    ld[copyIdx] = vars[id].getLB();
                    le[copyIdx] = vars[ie].getLB();
                    ue[copyIdx] = vars[ie].getUB();
                    h[copyIdx] = vars[ih].getLB();
                    mapping[copyIdx] = is;
                    copyIdx++;
                } else {
                    hEvents.add(vars[is].getUB(), is, Event.FSCP, vars[ih].getValue());
                    hEvents.add(vars[ie].getLB(), is, Event.FECP, -vars[ih].getValue());
                }
            }
            nbTasksInFilteringAlgo = copyIdx;
            // build the aggregated profile
            if (!hEvents.isEmpty()) { // there are instantiated tasks
                eventsToAdd = new int[2][hEvents.nbEvents()];
                int evtIdx = 0;
                int[] curEvt = hEvents.peek();
                int delta, date, height, prevHeight;
                delta = curEvt[0];
                date = delta;
                prevHeight = 0;
                height = 0;
                while (!hEvents.isEmpty()) {
                    if (date != delta) {
                        if (prevHeight != height) { // variation of the consumption
                            eventsToAdd[0][evtIdx] = delta; // event date
                            eventsToAdd[1][evtIdx] = prevHeight - height; // decrement
                            evtIdx++;
                            prevHeight = height;
                        }
                        delta = date;
                    }
                    curEvt = hEvents.poll();
                    height += curEvt[3];
                    if (!hEvents.isEmpty()) date = hEvents.peekDate();
                }
                // creer le dernier !
                eventsToAdd[0][evtIdx] = date;
                eventsToAdd[1][evtIdx] = prevHeight - height;
                nbEventsToAdd = evtIdx + 1;
            }
            return eventsToAdd;
        } else {
            for (int is = 0, id = nbTasks, ie = 2 * nbTasks, ih = 3 * nbTasks; is < nbTasks; is++, id++, ie++, ih++) {
                ls[is] = vars[is].getLB();
                us[is] = vars[is].getUB();
                ld[is] = vars[id].getLB();
                le[is] = vars[ie].getLB();
                ue[is] = vars[ie].getUB();
                h[is] = vars[ih].getLB();
                mapping[is] = is;
            }
            return null;
        }
    }

    public final void mainLoop() throws ContradictionException {
        int state = 0;
        int[][] eventsToAdd;
        boolean succeed = false;
        boolean res, max;
        int[] ls = new int[nbTasks];
        int[] us = new int[nbTasks];
        int[] ld = new int[nbTasks];
        int[] le = new int[nbTasks];
        int[] ue = new int[nbTasks];
        int[] h = new int[nbTasks];
        do {
            for (int is = 0; is < nbTasks; is++) {
                vars[is].notifyMonitors(null, null);
            }
            // copy variable bounds to arrays
            eventsToAdd = copyAndAggregate(ls, us, ld, le, ue, h); // the number of AP events is stored in nbEventsToAdd
            // ===== NORMAL MODE =====
                this.sweepMin = new DynamicSweepMin(nbTasks, prop, nbTasksInFilteringAlgo, capa, ls, us, ld, le, ue, h);
                this.sweepMax = new DynamicSweepMax(nbTasks, prop, nbTasksInFilteringAlgo, capa, ls, us, ld, le, ue, h);
                res = sweepMin.sweepMin(eventsToAdd, nbEventsToAdd);
                res = sweepMax.sweepMax(eventsToAdd, nbEventsToAdd);
                max = false;

                while (res) {
                    if (max) {
                        if (sweepMin.isSweepMaxNeeded()) { // check if sweep max should be run.
                            res = sweepMax.sweepMax(eventsToAdd, nbEventsToAdd);
                        } else {
                            res = false;
                            assert (false == sweepMax.sweepMax(eventsToAdd, nbEventsToAdd));
                        }
                    } else {
                        res = sweepMin.sweepMin(eventsToAdd, nbEventsToAdd);
                    }
                    max = !max;
                }

            state = 0;
            // update variables.
            for (int is = 0; is < nbTasksInFilteringAlgo; is++) {
                vars[mapping[is]].updateLowerBound(ls[is], aCause);
                vars[mapping[is]].updateUpperBound(us[is], aCause);
                vars[mapping[is] + nbTasks].updateLowerBound(ld[is], aCause);
                vars[mapping[is] + 2 * nbTasks].updateLowerBound(le[is], aCause);
                vars[mapping[is] + 2 * nbTasks].updateUpperBound(ue[is], aCause);
                state = state + (us[is] - ls[is]) + (ue[is] - le[is]) + ld[is];
            }
            // check !
            for (int is = 0; is < nbTasksInFilteringAlgo; is++) {
                state = state - (vars[mapping[is]].getUB() - vars[mapping[is]].getLB())
                        - (vars[mapping[is] + 2 * nbTasks].getUB() - vars[mapping[is] + 2 * nbTasks].getLB())
                        - vars[mapping[is] + nbTasks].getLB();
            }
        } while (state != 0);
    }
}
