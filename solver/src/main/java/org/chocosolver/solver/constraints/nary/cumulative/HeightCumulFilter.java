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

	public HeightCumulFilter(int n){
		super(n);
		FIXPOINT = false;
	}

	//***********************************************************************************
	// SWEEP ALGORITHM
	//***********************************************************************************

	// do not filter min and max (heights only)
	protected void pruneMin(IntVar[] s) throws ContradictionException {}
	protected void pruneMax(IntVar[] e) throws ContradictionException {}

	protected boolean sweep(IntVar capamax, IntVar[] h, int nbT, Propagator<IntVar> aCause) throws ContradictionException {
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
			if(event.type == SCP) {
				currentConso += hlb[event.index];
				// filter the capa max LB from the compulsory part consumptions
				capamax.updateLowerBound(currentConso, aCause);
				if (!h[map[event.index]].isInstantiated()) {
					tprune.add(event.index);
				}
			}else{
				assert event.type == ECP;
				currentConso -= hlb[event.index];
			}
		}
		return false;
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
