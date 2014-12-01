/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.nary.cumulative;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.sort.ArraySort;

import java.io.Serializable;
import java.util.Comparator;

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
	protected final int[] slb, sub;
	protected final int[] elb, eub;
	protected final int[] dlb;
	protected final int[] hlb;
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
	protected ArraySort<Event> sort;
	protected Comparator<Event> eventComparator;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public SweepCumulFilter(int n, Propagator cause){
		super(n,cause);
		map = new int[n];
		slb = new int[n];
		sub = new int[n];
		elb = new int[n];
		eub = new int[n];
		dlb = new int[n];
		hlb = new int[n];
		events = new Event[3*n];
		for(int i=0;i<3*n;i++){
			events[i] = new Event();
		}
		tasksToUSe = SetFactory.makeSwap(n, false);
		sort = new ArraySort<>(events.length,true,false);
		eventComparator = (e1, e2) -> {
            if(e1.date == e2.date){
                return e2.type-e1.type;
            }
            return e1.date - e2.date;
        };
	}

	//***********************************************************************************
	// GENERAL METHODS
	//***********************************************************************************

	@Override
	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
		// removing tasks with a duration lower bound equal to 0
		removeNullDurations(d, tasks);
		int nbT = tasksToUSe.getSize();
		// filtering start lower bounds
		boolean again;
		do{
			again = false;
			int i = 0;
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement()) {
				slb[i]=s[t].getLB();
				sub[i]=s[t].getUB();
				elb[i]=e[t].getLB();
				eub[i]=e[t].getUB();
				dlb[i]=d[t].getLB();
				hlb[i]=h[t].getLB();
				map[i] = t;
				i++;
			}
			while (sweep(capa, h, nbT)){
				again = true;
				if(!FIXPOINT)break;
			}
			pruneMin(s);
			// symmetric approach for the end upper bounds
			i = 0;
			for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement()) {
				slb[i]=-e[t].getUB()+1;
				sub[i]=-e[t].getLB()+1;
				elb[i]=-s[t].getUB()+1;
				eub[i]=-s[t].getLB()+1;
				i++;
			}
			while (sweep(capa, h, nbT)){
				again = true;
				if(!FIXPOINT)break;
			}
			pruneMax(e);
		}while(FIXPOINT && again);
	}

	protected void removeNullDurations(IntVar[] d, ISet tasks){
		tasksToUSe.clear();
		for(int t=tasks.getFirstElement();t>=0;t=tasks.getNextElement()) {
			if(d[t].getLB()>0){
				tasksToUSe.add(t);
			}
		}
	}

	protected void pruneMin(IntVar[] s) throws ContradictionException {
		int i = 0;
		for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement())
			s[t].updateLowerBound(slb[i++], aCause);
	}

	protected void pruneMax(IntVar[] e) throws ContradictionException {
		int i = 0;
		for(int t=tasksToUSe.getFirstElement();t>=0;t=tasksToUSe.getNextElement())
			e[t].updateUpperBound(1- slb[i++], aCause);
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
					if(currentDate< sub[index] || sub[index]>= elb[index]){
						// the current task should overlap the current event
						if(currentConso+ hlb[index]>capa) {
							// filter min start to next event
							slb[index]=nextDate;
							if(nextDate> sub[index]) {// early fail detection
								aCause.contradiction(capamax,"");
							}
							active = true;// perform fix point
							temp.add(index);
						}
						else if(nextDate< slb[index]+ dlb[index]) {
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
					currentConso += hlb[event.index];
					// filter the capa max LB from the compulsory part consumptions
					capamax.updateLowerBound(currentConso, aCause);
					break;
				case(ECP):
					currentConso -= hlb[event.index];
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
			if(slb[i]< sub[i]) {
				events[nbEvents++].set(PRU, i, slb[i]);
			}
			// a compulsory part exists
			if(sub[i] < elb[i]) {
				events[nbEvents++].set(SCP, i, sub[i]);
				events[nbEvents++].set(ECP, i, elb[i]);
			}
		}
	}

	//***********************************************************************************
	// DATA STRUCTURES
	//***********************************************************************************

	public static class Event implements Serializable{
		protected int type;
		protected int index;
		protected int date;

		protected void set(int t, int i, int d) {
			date = d;
			type = t;
			index= i;
		}
	}
}
