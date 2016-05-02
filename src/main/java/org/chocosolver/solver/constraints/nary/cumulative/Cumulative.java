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

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Cumulative constraint
 * @author Jean-Guillaume Fages
 * @since 22/10/13
 */
public class Cumulative extends Constraint {

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Cumulative constraint
	 *
	 * @param tasks			task variables (embed start, duration and end variables)
	 * @param heights		height variables (represent the consumption of each task on the resource)
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
		super("Cumulative",createPropagators(tasks, heights, capacity, graphBased, filters));
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	private static Propagator[] createPropagators(Task[] tasks, IntVar[] heights, IntVar capa, boolean graphBased, Filter... filters){
		int n = tasks.length;
		assert n==heights.length && n > 0;
		IntVar[] vars = extract(tasks,heights,capa);
		IntVar[] s = Arrays.copyOfRange(vars,0,n);
		IntVar[] d = Arrays.copyOfRange(vars,n,2*n);
		IntVar[] e = Arrays.copyOfRange(vars,2*n,3*n);
		IntVar[] h = Arrays.copyOfRange(vars,3*n,4*n);
		// propagators are posted twice, to achieve fixpoint
		if(graphBased){
			return new Propagator[]{
					new PropGraphCumulative(s,d,e,h,capa,false, filters),
					new PropGraphCumulative(s,d,e,h,capa,true, filters)
			};
		}else{
			return new Propagator[]{
					new PropCumulative(s,d,e,h,capa, filters),
					new PropCumulative(s,d,e,h,capa, filters)
			};
		}
	}

	public static IntVar[] extract(Task[] tasks, IntVar[] heights, IntVar capa){
		int n = tasks.length;
		IntVar[] starts = new IntVar[n];
		IntVar[] durations = new IntVar[n];
		IntVar[] ends = new IntVar[n];
		for (int i = 0; i < n; i++) {
			starts[i] = tasks[i].getStart();
			durations[i] = tasks[i].getDuration();
			ends[i] = tasks[i].getEnd();
		}
		return ArrayUtils.append(starts,durations,ends,heights,new IntVar[]{capa});
	}

	//***********************************************************************************
	// FILTERING ALGORITHMS
	//***********************************************************************************

	/**
	 * Filtering algorithms for Cumulative constraint
	 */
	public enum Filter {
		/**
		 * filters height variables only (sweep-based algorithm)
		 * idempotent (on the given set of variables only)
		 */
		HEIGHTS{
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new HeightCumulFilter(n,cause);
			}
		},
		/**
		 * time-table algorithm based on each point in time
		 * not idempotent
		 */
		TIME{
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new TimeCumulFilter(n,cause);
			}
		},
		/**
		 * time-table algorithm based on a sweep line
		 * idempotent (on the given set of variables only)
		 */
		SWEEP{
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new SweepCumulFilter(n,cause);
			}
		},
		/**
		 * time-table algorithm based on a sweep line
		 * idempotent (on the given set of variables only)
		 */
		SWEEP_HEI_SORT {
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new SweepHeiSortCumulFilter(n,cause);
			}
		},
		/**
		 * energetic reasoning to filter
		 * not idempotent
		 * not enough to ensure correctness (only an additional filtering)
		 */
		NRJ{
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new NRJCumulFilter(n,cause);
			}
		},
		/**
		 * energetic reasoning to filter disjunctive constraint
		 * Only propagated on variable subsets of size < 30
		 * not idempotent
		 * not enough to ensure correctness (only an additional filtering)
		 */
		DISJUNCTIVE_TASK_INTERVAL {
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new DisjunctiveTaskIntervalFilter(n,cause);
			}
		},
		/**
		 * Combines above filters as a black-box
		 * not idempotent
		 */
		DEFAULT {
			public CumulFilter make(int n, Propagator<IntVar> cause){
				return new DefaultCumulFilter(n,cause);
			}
		};

		/**
		 * Create an instance of the filtering algorithm
		 * @param n		maximum number of tasks
		 * @param cause	cause of filtering
		 * @return an instance of the filtering algorithm
		 */
		public abstract CumulFilter make(int n, Propagator<IntVar> cause);
	}
}
