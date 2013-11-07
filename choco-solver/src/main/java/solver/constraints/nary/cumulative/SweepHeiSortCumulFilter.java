/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.cumulative;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.objects.setDataStructures.ISet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

	protected final ArrayList<Integer> sortedTasks;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SweepHeiSortCumulFilter(IntVar[] st, IntVar[] du, IntVar[] en, IntVar[] he, IntVar capa, Propagator cause){
		super(st,du,en,he,capa,cause);
		sortedTasks = new ArrayList<>(start_lb_copy.length);
	}

	//***********************************************************************************
	// GENERAL METHODS
	//***********************************************************************************

	@Override
	public void filter(ISet tasks) throws ContradictionException {
		int i = 0;
		sortedTasks.clear();
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			if(d[t].getLB()>0){
				map[i] = t;
				sortedTasks.add(i++);
			}
		}
		Collections.sort(sortedTasks, new TaskSorter());
		assert checkSort();
		super.filter(tasks);
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	protected boolean sweep(int nbT) throws ContradictionException {
		assert nbT==sortedTasks.size();
		generateMinEvents(nbT);
		if(nbEvents==0){
			return false;// might happen on randomly generated cases
		}
		sort.sort(events,nbEvents,eventComparator);
		//		Arrays.sort(events,0,nbEvents,eventComparator);
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
					int index = sortedTasks.get(i);
					// the current task cannot overlaps the current event
					if(currentConso+hei_lb_copy[index]>capa) {
						// task min start can be filtered (no mand part, overlaps envelope before smin+dmin
						if((currentDate<start_ub_copy[index] || start_ub_copy[index]>=end_lb_copy[index])
						&& start_lb_copy[index]<nextDate && currentDate < start_lb_copy[index]+dur_lb_copy[index]){
							// filter min start to next event
							start_lb_copy[index]=nextDate;
							if(nextDate>start_ub_copy[index]) {// early fail detection
								aCause.contradiction(s[map[index]],"");
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
				currentConso += hei_lb_copy[event.index];
				// filter the capa max LB from the compulsory part consumptions
				capamax.updateLowerBound(currentConso, aCause);
			}else {
				assert event.type==ECP;
				currentConso -= hei_lb_copy[event.index];
			}
		}
		return active;
	}

	protected void generateMinEvents(int nbT) {
		// no PRU events
		nbEvents = 0;
		for(int i=0; i<nbT; i++) {
			// a compulsory part exists
			if(start_ub_copy[i] < end_lb_copy[i]) {
				events[nbEvents++].set(SCP, i, start_ub_copy[i]);
				events[nbEvents++].set(ECP, i, end_lb_copy[i]);
			}
		}
	}

	//***********************************************************************************
	// DATA STRUCTURES
	//***********************************************************************************

	/**
	 * Sorts tasks to reduce sweep algorithm length:
	 * - first put tasks with an unfixed height,
	 * - then use a decreasing height ordering
	 */
	protected class TaskSorter implements Comparator<Integer>, Serializable {
		@Override
		public int compare(Integer o1, Integer o2) {
			// BEWARE: h[i].getLB() has not been copied into hei_lb_copy yet
			return h[map[o2]].getLB()-h[map[o1]].getLB();
		}
	}

	//***********************************************************************************
	// DEBUG ONLY
	//***********************************************************************************`

	protected boolean checkSort(){
		int nbT = sortedTasks.size();
		for(int i2=0; i2<nbT; i2++) {
			int idx1 = sortedTasks.get(i2);
			for(int i3=i2+1; i3<nbT; i3++) {
				int idx2 = sortedTasks.get(i3);
				assert h[map[idx1]].getLB()>=h[map[idx2]].getLB();
			}
		}
		return true;
	}
}