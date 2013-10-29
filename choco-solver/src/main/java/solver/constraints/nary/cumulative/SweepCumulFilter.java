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

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.PoolManager;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Basic implementation of Sweep-based Time-Table for cumulative
 * @author Thierry Petit, Jean-Guillaume Fages
 * @since 16/10/13
 */
public class SweepCumulFilter extends CumulFilter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// variable bound copies (for both performance and convenience)
	protected final int[] start_lb_copy,start_ub_copy;
	protected final int[] end_lb_copy,end_ub_copy;
	protected final int[] dur_lb_copy;
	protected final int[] hei_lb_copy;
	// sweep events: Prune / StartCompulsoryPart / EndCompulsoryPart
	protected final static int PRU = 1, SCP = 2,ECP = 3;
	protected final Event[] events;
	protected int nbEvents;
	// map to deal with subsets of variables
	protected final int[] map;
	// > 0 duration subset
	protected final ISet tasksToUSe;
	protected boolean FIXPOINT = true;
	protected TIntArrayList temp = new TIntArrayList();
	protected TIntArrayList tprune = new TIntArrayList();
	protected PoolManager<Event> pool = new PoolManager<>();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SweepCumulFilter(IntVar[] st, IntVar[] du, IntVar[] en, IntVar[] he, IntVar capa, Propagator cause){
		super(st,du,en,he,capa,cause);
		int n = st.length;
		this.map = new int[n];
		this.start_lb_copy = new int[n];
		this.start_ub_copy = new int[n];
		this.end_lb_copy = new int[n];
		this.end_ub_copy = new int[n];
		this.dur_lb_copy = new int[n];
		this.hei_lb_copy = new int[n];
		this.events = new Event[3*n];
		this.tasksToUSe = SetFactory.makeSwap(st.length, false);
	}

	//***********************************************************************************
	// GENERAL METHODS
	//***********************************************************************************

	@Override
	public void filter(ISet tasks) throws ContradictionException {
		// removing tasks with a duration lower bound equal to 0
		removeNullDurations(tasks);
		int nbT = tasksToUSe.getSize();
		// filtering start lower bounds
		boolean again;
		do{
			again = false;
			int i = 0;
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement()) {
				this.start_lb_copy[i]=this.s[t].getLB();
				this.start_ub_copy[i]=this.s[t].getUB();
				this.end_lb_copy[i]=this.e[t].getLB();
				this.end_ub_copy[i]=this.e[t].getUB();
				this.dur_lb_copy[i]=this.d[t].getLB();
				this.hei_lb_copy[i]=this.h[t].getLB();
				this.map[i] = t;
				i++;
			}
			while (sweep(nbT)){
				again = true;
				if(!FIXPOINT)break;
			}
			pruneMin(true);
			// symmetric approach for the end upper bounds
			i = 0;
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement()) {
				this.start_lb_copy[i]=-this.e[t].getUB()+1;
				this.start_ub_copy[i]=-this.e[t].getLB()+1;
				this.end_lb_copy[i]=-this.s[t].getUB()+1;
				this.end_ub_copy[i]=-this.s[t].getLB()+1;
				i++;
			}
			while (sweep(nbT)){
				again = true;
				if(!FIXPOINT)break;
			}
			pruneMin(false);
		}while(FIXPOINT && again);
		// debug mode : should not be able to filter more with the time-based filter
		assert filterTime(true, tasksToUSe);
	}

	protected void removeNullDurations(ISet tasks){
		tasksToUSe.clear();
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			if(d[t].getLB()>0){
				tasksToUSe.add(t);
			}
		}
	}

	protected void pruneMin(boolean min) throws ContradictionException {
		int i = 0;
		if(min)
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement())
				s[t].updateLowerBound(start_lb_copy[i++], aCause);
		else
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement())
				e[t].updateUpperBound(1-start_lb_copy[i++], aCause);
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	protected boolean sweep(int nbT) throws ContradictionException {
		generateMinEvents(nbT);
		if(nbEvents==0){
			return false;// might happen on randomly generated cases
		}
		Arrays.sort(events,0,nbEvents,eventComparator);
		int timeIndex = 0;
		int currentDate = events[timeIndex].date;
		tprune.resetQuick();
		int capa = capamax.getUB();
		int currentConso = 0;
		boolean active = false;
		while(timeIndex<nbEvents){
			// see next event
			int nextDate = events[timeIndex].date;
			// pruning
			if(currentDate<nextDate) {
				assert currentConso<=capa;
				temp.resetQuick();
				for(int i=tprune.size()-1;i>=0;i--){
					int index = tprune.get(i);
					// task min start might be filtered
					if(currentDate<start_ub_copy[index] || start_ub_copy[index]>=end_lb_copy[index]){
						// the current task should overlap the current event
						if(currentConso+hei_lb_copy[index]>capa) {
							// filter min start to next event
							start_lb_copy[index]=nextDate;
							if(nextDate>start_ub_copy[index]) {// early fail detection
								aCause.contradiction(s[map[index]],"");
							}
							active = true;// perform fix point
							temp.add(index);
						}
						else if(nextDate<start_lb_copy[index]+dur_lb_copy[index]) {
							temp.add(index);
						}
					}
				}
				tprune.resetQuick();
				for(int i=temp.size()-1;i>=0;i--){
					tprune.add(temp.getQuick(i));
				}
			}
			// handle the current event
			Event event = events[timeIndex++];
			currentDate = event.date;
			switch(event.type) {
				case(SCP):
					currentConso += hei_lb_copy[event.index];
					// filter the capa max LB from the compulsory part consumptions
					capamax.updateLowerBound(currentConso, aCause);
					break;
				case(ECP):
					currentConso -= hei_lb_copy[event.index];
					break;
				case(PRU):
					tprune.add(event.index);
					break;
			}
		}
		return active;
	}

	protected void generateMinEvents(int nbT) {
		nbEvents = 0;
		for(int i=0; i<nbT; i++) {
			// start min or height max can be filtered
			if(start_lb_copy[i]<start_ub_copy[i]) {
				events[nbEvents++] = useEvent(PRU, i, start_lb_copy[i]);
			}
			// a compulsory part exists
			if(start_ub_copy[i] < end_lb_copy[i]) {
				events[nbEvents++] = useEvent(SCP, i, start_ub_copy[i]);
				events[nbEvents++] = useEvent(ECP, i, end_lb_copy[i]);
			}
		}
	}

	//***********************************************************************************
	// DATA STRUCTURES
	//***********************************************************************************

	protected class Event implements Serializable{
		protected int type;
		protected int index;
		protected int date;
	}

	protected Event useEvent(int type, int i, int date) {
		Event e = pool.getE();
		if(e==null){
			e = new Event();
		}
		e.date = date;
		e.type = type;
		e.index= i;
		return e;
	}

	protected final static Comparator<Event> eventComparator = new Comparator<Event>(){
		@Override
		public int compare(Event o1, Event o2) {
			if(o1.date == o2.date){
				return o2.type-o1.type;
			}
			return o1.date - o2.date;
		}
	};

	//***********************************************************************************
	// DEBUG ONLY
	//***********************************************************************************

	protected int[] time = new int[31];
	protected boolean filterTime(boolean crashOnFiltering, ISet tasks) throws ContradictionException {
		int min = Integer.MAX_VALUE / 2;
		int max = Integer.MIN_VALUE / 2;
		for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
			if (s[i].getUB() < e[i].getLB()) {
				min = Math.min(min, s[i].getUB());
				max = Math.max(max, e[i].getLB());
			}
		}
		if (min < max) {
			if(max-min>time.length){
				time = new int[max-min];
			}
			else{
				Arrays.fill(time, 0, max - min, 0);
			}
			int capaMax = capamax.getUB();
			// fill mandatory parts and filter capacity
			int elb,hlb;
			int maxC=0;
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				elb = e[i].getLB();
				hlb = h[i].getLB();
				for (int t = s[i].getUB(); t < elb; t++) {
					time[t - min] += hlb;
					maxC = Math.max(maxC,time[t - min]);
				}
			}
			if(crashOnFiltering && capamax.getLB()<maxC){
				throw new UnsupportedOperationException();
			}
			capamax.updateLowerBound(maxC, aCause);
			// filter max height
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				if (d[i].getLB() > 0 && h[i].getLB() > 0) {
					// filters
					if (s[i].getLB() + d[i].getLB() > min) {
						filterInf(crashOnFiltering,i, min, max, time, capaMax);
					}
					if (e[i].getUB() - d[i].getLB() < max) {
						filterSup(crashOnFiltering,i, min, max, time, capaMax);
					}
				}
			}
		}
		return true;
	}
	protected void filterInf(boolean crashOnFiltering,int i, int min, int max, int[] time, int capaMax) throws ContradictionException {
		int nbOk = 0;
		int dlb = d[i].getLB();
		int hlb = h[i].getLB();
		int sub = s[i].getUB();
		for (int t = s[i].getLB(); t < sub; t++) {
			if (t < min || t >= max || hlb + time[t - min] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					return;
				}
			} else {
				if(crashOnFiltering)throw new UnsupportedOperationException();
				nbOk = 0;
				s[i].updateLowerBound(t + 1, aCause);
			}
		}
	}
	protected void filterSup(boolean crashOnFiltering, int i, int min, int max, int[] time, int capaMax) throws ContradictionException {
		int nbOk = 0;
		int dlb = d[i].getLB();
		int hlb = h[i].getLB();
		int elb = e[i].getLB();
		for (int t = e[i].getUB(); t > elb; t--) {
			if (t - 1 < min || t - 1 >= max || hlb + time[t - min - 1] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					return;
				}
			} else {
				if(crashOnFiltering)throw new UnsupportedOperationException();
				nbOk = 0;
				e[i].updateUpperBound(t - 1, aCause);
			}
		}
	}
}