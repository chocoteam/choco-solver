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

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.variables.IntVar;
import solver.variables.Task;
import util.ESat;
import util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Cumulative constraint
 * @author Jean-Guillaume Fages
 * @since 22/10/13
 */
public class Cumulative extends Constraint<IntVar,Propagator<IntVar>> {

	//***********************************************************************************
	// VARIABLE
	//***********************************************************************************

	protected final int n;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Default cumulative constraint
	 * uses SWEEP + NRJ filters
	 *
	 * @param tasks		task variables (embed start, duration and end variables)
	 * @param heights	height variables (represent the consomation of each task on the resource)
	 * @param capacity	maximal capacity of the resource (same at each point in time)
	 */
	public Cumulative(Task[] tasks, IntVar[] heights, IntVar capacity) {
		this(tasks,heights,capacity, true, Filter.SWEEP, Filter.NRJ);
	}

	/**
	 * Cumulative constraint
	 *
	 * @param tasks			task variables (embed start, duration and end variables)
	 * @param heights		height variables (represent the consomation of each task on the resource)
	 * @param capacity		maximal capacity of the resource (same at each point in time)
	 * @param graphBased	parameter indicating how to filter:
	 *                         - TRUE:	applies on subset of overlapping tasks
	 *                         - FALSE:	applies on all tasks
	 * @param filters			Filtering algorithm to use:
	 *                         - TIME: filters time-table from considering each point in time
	 *                         (efficient in practice as long as the time horizon is not too high)
	 *                         - SWEEP: filters time-table with a sweep-based algorithm
	 *                         - NRJ: greedy energy-based filter.
	 *                         BEWARE: should not be used alone, use it in addition to either SWEEP or TIME.
	 *
	 */
	public Cumulative(Task[] tasks, IntVar[] heights, IntVar capacity, boolean graphBased, Filter... filters) {
		super(extract(tasks,heights,capacity), capacity.getSolver());
		this.n = tasks.length;
		assert n==heights.length && n > 0;
		IntVar[] s = Arrays.copyOfRange(vars,0,n);
		IntVar[] d = Arrays.copyOfRange(vars,n,2*n);
		IntVar[] e = Arrays.copyOfRange(vars,2*n,3*n);
		IntVar[] h = Arrays.copyOfRange(vars,3*n,4*n);
		if(graphBased){
			setPropagators(
					new PropGraphCumulative(s,d,e,h,capacity,true, filters),
					new PropGraphCumulative(s,d,e,h,capacity,false, filters)
			);
		}else{
			setPropagators(
					new PropFullCumulative(s,d,e,h,capacity,true, filters),
					new PropFullCumulative(s,d,e,h,capacity,false, filters)
			);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static IntVar[] extract(Task[] tasks, IntVar[] heights, IntVar capacity){
		int n = tasks.length;
		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		for (int i = 0; i < n; i++) {
			starts[i] = tasks[i].getStart();
			durations[i] = tasks[i].getDuration();
			ends[i] = tasks[i].getEnd();
		}
		return ArrayUtils.append(starts,durations,ends,heights,new IntVar[]{capacity});
	}

	@Override
	public ESat isEntailed() {
		int min = vars[0].getUB();
		int max = vars[2*n].getLB();
		// check start + duration = end
		for (int i = 0; i < n; i++) {
			min = Math.min(min, vars[i].getUB());
			max = Math.max(max, vars[i+2*n].getLB());
			if(vars[i].getLB()+vars[i+n].getLB()>vars[i+2*n].getUB()
					|| vars[i].getUB()+vars[i+n].getUB()<vars[i+2*n].getLB()){
				return ESat.FALSE;
			}
		}
		// check capacity
		int maxLoad = 0;
		if(min <= max){
			int capamax = vars[4*n].getUB();
			int[] consoMin = new int[max - min];
			for (int i = 0; i < n; i++) {
				for (int t = vars[i].getUB(); t < vars[i+2*n].getLB(); t++) {
					consoMin[t - min] += vars[i+3*n].getLB();
					if (consoMin[t - min] > capamax) {
						return ESat.FALSE;
					}
					maxLoad = Math.max(maxLoad,consoMin[t-min]);
				}
			}
		}
		// check variables are instantiated
		for (int i = 0; i < vars.length-1; i++) {
			if(!vars[i].instantiated()){
				return ESat.UNDEFINED;
			}
		}
		assert min<=max;
		// capacity check entailed
		if(maxLoad<=vars[4*n].getLB()){
			return ESat.TRUE;
		}
		// capacity not instantiated
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Cumulative(");
		for (int i = 0; i < n; i++) {
			sb.append("[" + vars[i].toString());
			sb.append("," + vars[i + n].toString());
			sb.append("," + vars[i + 2 * n].toString());
			sb.append("," + vars[i + 3 * n].toString() + "],");
		}
		sb.append(vars[4*n].toString()+")");
		return sb.toString();
	}

	//***********************************************************************************
	// FILTERING ALGORITHMS
	//***********************************************************************************

	/**
	 * Filtering algorithms for Cumulative constraint
	 */
	public static enum Filter {
		/**
		 * time-table algorithm based on each point in time
		 * not idempotent
		 */
		TIME{
			public CumulFilter make(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, Propagator<IntVar> cause){
				return new TimeCumulFilter(s,d,e,h,capa,cause);
			}
		},
		/**
		 * time-table algorithm based on a sweep line
		 * idempotent (on the given set of variables only)
		 */
		SWEEP{
			public CumulFilter make(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, Propagator<IntVar> cause){
				return new SweepCumulFilter(s,d,e,h,capa,cause);
			}
		},
		/**
		 * energetic reasoning to filter
		 * not idempotent
		 * not enough to ensure correctness (only an additional filtering)
		 */
		NRJ{
			public CumulFilter make(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, Propagator<IntVar> cause){
				return new NRJCumulFilter(s,d,e,h,capa,cause);
			}
		};

		public abstract CumulFilter make(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, Propagator<IntVar> cause);
	}
}
