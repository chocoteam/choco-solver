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
import java.util.Arrays;
import java.util.Collections;

/**
 * Filtering (sweep-based) algorithm to filter task maximum heights
 * @author Jean-Guillaume Fages, Thierry Petit
 * @since 16/10/13
 */
public class HeightCumulFilter extends SweepCumulFilter {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public HeightCumulFilter(IntVar[] st, IntVar[] du, IntVar[] en, IntVar[] he, IntVar capa, Propagator cause){
		super(st,du,en,he,capa,cause);
		FIXPOINT = false;
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	// do not filter min and max (heights only)
	protected void pruneMin(boolean min) throws ContradictionException {}

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
					assert (start_ub_copy[index]<=currentDate && currentDate < end_lb_copy[index]);
					// filter consumption variable from remaining capacity
					h[map[index]].updateUpperBound(capa-(currentConso-hei_lb_copy[index]),aCause);
					if(nextDate<end_lb_copy[index]) {
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
					currentConso += hei_lb_copy[event.index];
					// filter the capa max LB from the compulsory part consumptions
					capamax.updateLowerBound(currentConso, aCause);
					if(!h[map[event.index]].instantiated()){
						tprune.add(event.index);
					}
					break;
				case(ECP):
					currentConso -= hei_lb_copy[event.index];
					break;
			}
		}
		return active;
	}

	protected void generateMinEvents(int nbT) {
		nbEvents = 0;
		for(int i=0; i<nbT; i++) {
			// a compulsory part exists
			if(start_ub_copy[i] < end_lb_copy[i]) {
				events[nbEvents++] = useEvent(SCP, i, start_ub_copy[i]);
				events[nbEvents++] = useEvent(ECP, i, end_lb_copy[i]);
			}
		}
	}

	//***********************************************************************************
	// DEBUG ONLY
	//***********************************************************************************

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
			int minH;
			for (int i = tasks.getFirstElement(); i >= 0; i = tasks.getNextElement()) {
				if(!h[i].instantiated()){
					minH = h[i].getUB();
					elb = e[i].getLB();
					hlb = h[i].getLB();
					for (int t = s[i].getUB(); t < elb; t++) {
						minH = Math.min(minH,capaMax-(time[t-min]-hlb));
					}
					if(crashOnFiltering && h[i].getUB()>minH){
						throw new UnsupportedOperationException();
					}
					h[i].updateUpperBound(minH,aCause);
				}
			}
		}
		return true;
	}
}