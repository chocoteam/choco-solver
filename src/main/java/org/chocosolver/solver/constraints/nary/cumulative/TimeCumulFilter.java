/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

import java.util.Arrays;

/**
 * Time-based filtering (compute the profile over every point in time)
 * @author Jean-Guillaume Fages
 */
public class TimeCumulFilter extends CumulFilter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int[] time = new int[31];

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public TimeCumulFilter(int nbMaxTasks) {
		super(nbMaxTasks);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
		int min = Integer.MAX_VALUE / 2;
		int max = Integer.MIN_VALUE / 2;
		ISetIterator tIter = tasks.iterator();
		while (tIter.hasNext()){
			int i = tIter.nextInt();
			if (s[i].getUB() < e[i].getLB() && h[i].getLB() != 0) {
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
			int capaMax = capa.getUB();
			// fill mandatory parts and filter capacity
			int elb,hlb;
			int maxC=0;

			tIter = tasks.iterator();
			while (tIter.hasNext()){
				int i = tIter.nextInt();
				hlb = h[i].getLB();
				if (hlb != 0) {
					elb = e[i].getLB();
					for (int t = s[i].getUB(); t < elb; t++) {
						time[t - min] += hlb;
						maxC = Math.max(maxC, time[t - min]);
					}
				}
			}
			capa.updateLowerBound(maxC, aCause);
			// filter max height
			int minH;

			tIter = tasks.iterator();
			while (tIter.hasNext()){
				int i = tIter.nextInt();
				if(!h[i].isInstantiated()){
					minH = h[i].getUB();
					elb = Math.min(e[i].getLB(), max);
					hlb = h[i].getLB();
					for (int t = Math.max(s[i].getUB(), min); t < elb; t++) {
						minH = Math.min(minH,capaMax-(time[t-min]-hlb));
					}
					h[i].updateUpperBound(minH,aCause);
				}
			}
			for (int i : tasks) {
				if (h[i].getLB() > 0) {
					// filters
					if (s[i].getLB() + d[i].getLB() > min) {
						filterInf(s[i],e[i].getLB(),d[i].getLB(),h[i].getLB(), min, max, time, capaMax, aCause);
					}
					if (e[i].getUB() - d[i].getLB() < max) {
						filterSup(s[i].getUB(),e[i],d[i].getLB(),h[i].getLB(), min, max, time, capaMax, aCause);
					}
				}
			}
		}
	}

	protected void filterInf(IntVar start, int elb, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
		int nbOk = 0;
		int t = start.getLB();
		int newValue = t;
		if (min - t > 0) {
			nbOk = min - t;
			t = min;
		}
		boolean update = false;
		int sub = Math.min(start.getUB(), max);
		for (; t < sub; t++) {
			if (hlb + time[t - min] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					if (update) {
						start.updateLowerBound(newValue, aCause);
					}
					return;
				}
			} else {
				if(dlb==0 && t >= elb)return;
				nbOk = 0;
				newValue = t + 1;
				update = true;
			}
		}
		if (update) {
			start.updateLowerBound(newValue, aCause);
		}
	}

	protected void filterSup(int sub, IntVar end, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
		int nbOk = 0;
		int t = end.getUB();
		int newValue = t;
		if (t - max > 0) {
			nbOk = t - max;
			t = max;
		}
		boolean update = false;
		int elb = Math.max(end.getLB(), min);
		for (; t > elb; t--) {
			if (hlb + time[t - min - 1] <= capaMax) {
				nbOk++;
				if (nbOk == dlb) {
					if (update) {
						end.updateUpperBound(newValue, aCause);
					}
					return;
				}
			} else {
				if(dlb==0 && t <= sub)return;
				nbOk = 0;
				newValue = t - 1;
				update = true;
			}
		}
		if(update) {
			end.updateUpperBound(newValue, aCause);
		}
	}
}
