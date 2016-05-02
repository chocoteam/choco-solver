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

	public DefaultCumulFilter(int nbMaxTasks, Propagator cause) {
		super(nbMaxTasks, cause);
		nrj = Cumulative.Filter.NRJ.make(nbMaxTasks,aCause);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		boolean hInst = true;
		for (int t : tasks) {
			min = Math.min(min, s[t].getLB());
			max = Math.max(max, e[t].getUB());
			hInst &= h[t].isInstantiated();
		}
		if (max - min < tasks.getSize() * tasks.getSize()) {
			getTime().filter(s, d, e, h, capa, tasks);
		} else {
			getSweep().filter(s, d, e, h, capa, tasks);
			if (!hInst) {
				getHeights().filter(s, d, e, h, capa, tasks);
			}
		}
		nrj.filter(s, d, e, h, capa, tasks);
		// only propagated on less than 50 tasks (too costly otherwise)
		if (tasks.getSize() < 50) {
			if (capa.isInstantiatedTo(1)) {
				getDisjTaskInter().filter(s, d, e, h, capa, tasks);
			}
		}
	}

	//***********************************************************************************
	// Lazy creation (saves memory)
	//***********************************************************************************

	private CumulFilter getTime() {
		if(time==null)time = Cumulative.Filter.TIME.make(nbMaxTasks,aCause);
		return time;
	}

	private CumulFilter getSweep() {
		if(sweep==null)sweep = Cumulative.Filter.SWEEP.make(nbMaxTasks,aCause);
		return sweep;
	}

	private CumulFilter getHeights() {
		if(heights==null)heights = Cumulative.Filter.HEIGHTS.make(nbMaxTasks,aCause);
		return heights;
	}

	private CumulFilter getDisjTaskInter() {
		if(disjTaskInter==null)disjTaskInter = Cumulative.Filter.DISJUNCTIVE_TASK_INTERVAL.make(nbMaxTasks,aCause);
		return disjTaskInter;
	}
}
