/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;

/**
 * Alternative implementation of Sweep-based Time-Table for cumulative
 * The set of variables to be pruned is sorted by decreasing heights, not time
 * @author Jean-Guillaume Fages, Thierry Petit
 * @since 16/10/13
 */
public class SweepHeiSortCumulFilter extends SweepCumulFilter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final int[] sortedTasks;
	private final ArraySort taskSorter;
	private final IntComparator comparator;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SweepHeiSortCumulFilter(int n, Propagator cause){
		super(n,cause);
		sortedTasks = new int[n];
		taskSorter = new ArraySort(n,false,true);
		comparator = (i1, i2) -> hlb[map[i2]]-hlb[map[i1]];
	}

	//***********************************************************************************
	// GENERAL METHODS
	//***********************************************************************************

	@Override
	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
		int size = 0;
		for(int t:tasks) {
			if(d[t].getLB()>0){
				map[size] = t;
				sortedTasks[size] = size;
				hlb[t] = h[t].getLB();
				size++;
			}
		}
		taskSorter.sort(sortedTasks,size,comparator);
		assert checkSort(h,size);
		super.filter(s,d,e,h,capa,tasks);
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	protected boolean sweep(IntVar capamax, IntVar[] h, int nbT) throws ContradictionException {
		generateMinEvents(nbT);
		if(nbEvents==0){
			return false;// might happen on randomly generated cases
		}
		sort.sort(events,nbEvents,eventComparator);
		int timeIndex = 0;
		int currentDate = events[timeIndex].date;
		int capa = capamax.getUB();
		int currentConso = 0;
		boolean active = false;
		while(timeIndex<nbEvents) {
			// see next event
			int nextDate = events[timeIndex].date;
			assert nextDate>=currentDate;
			// pruning
			if(currentDate<nextDate) {
				assert currentConso<=capa;
				for(int i=0; i<nbT; i++) {
					int index = sortedTasks[i];
					// the current task cannot overlaps the current event
					if(currentConso+ hlb[index]>capa) {
						// task min start can be filtered (no mand part, overlaps envelope before smin+dmin
						if((currentDate< sub[index] || sub[index]>= elb[index])
								&& slb[index]<nextDate && currentDate < slb[index]+ dlb[index]){
							// filter min start to next event
							slb[index]=nextDate;
							if(nextDate> sub[index]) {// early fail detection
								aCause.contradiction(capamax,"");
							}
							active = true;// perform fix point
						}
					}else{
						// remaining tasks have a (fixed) smaller height => no more filtering can occur
						break;
					}
				}
			}
			// handle the current event
			Event event = events[timeIndex++];
			currentDate = event.date;
			if(event.type==SCP){
				assert (events[timeIndex].date>event.date||events[timeIndex].type==SCP);
				currentConso += hlb[event.index];
				// filter the capa max LB from the compulsory part consumptions
				capamax.updateLowerBound(currentConso, aCause);
			}else {
				assert event.type==ECP;
				currentConso -= hlb[event.index];
			}
		}
		return active;
	}

	protected void generateMinEvents(int nbT) {
		// no PRU events
		nbEvents = 0;
		for(int i=0; i<nbT; i++) {
			// a compulsory part exists
			if(sub[i] < elb[i]) {
				events[nbEvents++].set(SCP, i, sub[i]);
				events[nbEvents++].set(ECP, i, elb[i]);
			}
		}
	}

	//***********************************************************************************
	// DEBUG ONLY
	//***********************************************************************************`

	protected boolean checkSort(IntVar[] h, int nbT){
		for(int i2=0; i2<nbT; i2++) {
			int idx1 = sortedTasks[i2];
			for(int i3=i2+1; i3<nbT; i3++) {
				int idx2 = sortedTasks[i3];
				assert h[map[idx1]].getLB()>=h[map[idx2]].getLB();
			}
		}
		return true;
	}
}
