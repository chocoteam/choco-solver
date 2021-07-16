/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Default filtering for cumulative
 * @author Jean-Guillaume Fages
 */
public class DefaultCumulFilter extends CumulFilter {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private CumulFilter time, sweep, nrj, heights, disjTaskInter;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DefaultCumulFilter(int nbMaxTasks) {
		super(nbMaxTasks);
		nrj = Cumulative.Filter.NRJ.make(nbMaxTasks);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		boolean hInst = true;
		ISetIterator tIter = tasks.iterator();
		while (tIter.hasNext()){
			int t = tIter.nextInt();
			min = Math.min(min, s[t].getLB());
			max = Math.max(max, e[t].getUB());
			hInst &= h[t].isInstantiated();
		}
		if (max - min < tasks.size() * tasks.size()) {
			getTime().filter(s, d, e, h, capa, tasks, aCause);
		} else {
			getSweep().filter(s, d, e, h, capa, tasks, aCause);
			if (!hInst) {
				getHeights().filter(s, d, e, h, capa, tasks, aCause);
			}
		}
		nrj.filter(s, d, e, h, capa, tasks, aCause);
		// only propagated on less than 50 tasks (too costly otherwise)
		if (tasks.size() < 50) {
			if (capa.isInstantiatedTo(1)) {
				getDisjTaskInter().filter(s, d, e, h, capa, tasks, aCause);
			}
		}
	}

	//***********************************************************************************
	// Lazy creation (saves memory)
	//***********************************************************************************

	private CumulFilter getTime() {
		if(time==null)time = Cumulative.Filter.TIME.make(nbMaxTasks);
		return time;
	}

	private CumulFilter getSweep() {
		if(sweep==null)sweep = Cumulative.Filter.SWEEP.make(nbMaxTasks);
		return sweep;
	}

	private CumulFilter getHeights() {
		if(heights==null)heights = Cumulative.Filter.HEIGHTS.make(nbMaxTasks);
		return heights;
	}

	private CumulFilter getDisjTaskInter() {
		if(disjTaskInter==null)disjTaskInter = Cumulative.Filter.DISJUNCTIVE_TASK_INTERVAL.make(nbMaxTasks);
		return disjTaskInter;
	}
}
