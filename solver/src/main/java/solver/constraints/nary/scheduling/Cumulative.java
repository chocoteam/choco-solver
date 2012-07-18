package solver.constraints.nary.scheduling;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.scheduling.PropDynamicSweep;
import solver.variables.IntVar;

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
public class Cumulative extends Constraint<IntVar, Propagator<IntVar>> {
	
	private int nbTasks;
	private int limit;
	
	public static enum Type {
		SWEEP, DYNAMIC_SWEEP, EDGE_FINDING, TASK_INTERVALS, GREEDY
	}
	
	public Cumulative(IntVar[] starts, IntVar[] durations, IntVar[] ends, IntVar[] heights, int limit, Solver solver) {
		super(ArrayUtils.append(starts,durations,ends,heights), solver);
		assert(starts.length == durations.length && starts.length == ends.length && starts.length == heights.length);
		this.nbTasks = starts.length;
		this.limit = limit;
		setPropagators(new PropDynamicSweep(nbTasks, limit, vars, solver, this, 0, 1));
	}

	public Cumulative(IntVar[] starts, IntVar[] durations, IntVar[] ends, IntVar[] heights, int limit, Solver solver, Type type) {
		super(ArrayUtils.append(starts,durations,ends,heights), solver);
		assert(starts.length == durations.length && starts.length == ends.length && starts.length == heights.length);
		this.nbTasks = starts.length;
		this.limit = limit;
		switch (type) {
			case DYNAMIC_SWEEP : setPropagators(new PropDynamicSweep(nbTasks, limit, vars, solver, this, 0, 1)); break;
			case GREEDY : setPropagators(new PropDynamicSweep(nbTasks, limit, vars, solver, this, 1, 0)); break; // TODO Agregation des profils ˆ mettre par defaut ?
			default: setPropagators(new PropDynamicSweep(nbTasks, limit, vars, solver, this, 0, 1));
		}
	}
	
	
	
		

	@Override
	public ESat isSatisfied(){
		int minStart = Integer.MAX_VALUE;
		int maxEnd = Integer.MIN_VALUE;
		// compute min start and max end
		for(int is=0, id=nbTasks, ie=2*nbTasks, ih=3*nbTasks;is<this.nbTasks;is++,id++,ie++) { // is = start index, id = duration index, ie = end index
			if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated() || !vars[ih].instantiated()) return ESat.UNDEFINED;
			if (vars[is].getValue() < minStart) minStart = vars[is].getValue();
			if (vars[ie].getValue() > maxEnd) maxEnd = vars[ie].getValue();
		}
		int sumHeight;
		// scan the time axis and check the height
		for(int i=minStart;i<=maxEnd;i++) {
			sumHeight = 0;
			for(int is=0, ie=2*nbTasks, ih=3*nbTasks;is<this.nbTasks;is++,ie++,ih++) {
				if ( i >= vars[is].getValue() && i < vars[ie].getValue() ) sumHeight += vars[ih].getValue(); 
			}
			if (sumHeight > limit) return ESat.FALSE;
		}
		return ESat.TRUE;
	}
	
	public int nbTasks() {
		return nbTasks;
	}
	
	public int limit() {
		return limit;
	}
	
}
