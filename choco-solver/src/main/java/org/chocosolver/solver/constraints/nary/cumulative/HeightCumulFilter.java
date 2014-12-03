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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

/**
 * Filtering (sweep-based) algorithm to filter task maximum heights
 * @author Jean-Guillaume Fages, Thierry Petit
 * @since 16/10/13
 */
public class HeightCumulFilter extends SweepCumulFilter {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HeightCumulFilter(int n, Propagator cause){
		super(n,cause);
		FIXPOINT = false;
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	// do not filter min and max (heights only)
	protected void pruneMin(IntVar[] s) throws ContradictionException {}
	protected void pruneMax(IntVar[] e) throws ContradictionException {}

	protected boolean sweep(IntVar capamax, IntVar[] h, int nbT) throws ContradictionException {
		generateMinEvents(nbT);
		if(nbEvents==0){
			return false;// might happen on randomly generated cases
		}
		sort.sort(events,nbEvents,eventComparator);
		//Arrays.sort(events,0,nbEvents,eventComparator);
		int timeIndex = 0;
		int currentDate = events[timeIndex].date;
		tprune.resetQuick();
		int capa = capamax.getUB();
		int currentConso = 0;
		boolean active = false;
		while(timeIndex<nbEvents) {
			// see next event
			int nextDate = events[timeIndex].date;
			// pruning
			if(currentDate<nextDate) {
				assert currentConso<=capa;
				temp.resetQuick();
				for(int i=tprune.size()-1;i>=0;i--){
					int index = tprune.get(i);
					// the envelope overlaps the event
					assert (sub[index]<=currentDate && currentDate < elb[index]);
					// filter consumption variable from remaining capacity
					h[map[index]].updateUpperBound(capa-(currentConso- hlb[index]),aCause);
					if(nextDate< elb[index]) {
						temp.add(index);
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
					if(!h[map[event.index]].isInstantiated()){
						tprune.add(event.index);
					}
					break;
				case(ECP):
					currentConso -= hlb[event.index];
					break;
			}
		}
		return active;
	}

	protected void generateMinEvents(int nbT) {
		nbEvents = 0;
		for(int i=0; i<nbT; i++) {
			// a compulsory part exists
			if(sub[i] < elb[i]) {
				events[nbEvents++].set(SCP, i, sub[i]);
				events[nbEvents++].set(ECP, i, elb[i]);
			}
		}
	}
}
