/**
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary.alldifferent.algo;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.sort.ArraySort;

import java.io.Serializable;
import java.util.Comparator;

public class AlgoAllDiffBC {

	int[] t; // Tree links
	int[] d; // Diffs between critical capacities
	int[] h; // Hall interval links
	int[] bounds;

	int nbBounds;

	Interval[] intervals;
	Interval[] minsorted;
	Interval[] maxsorted;

	private Propagator aCause;
	private IntVar[] vars;

	ArraySort sorter;

	public AlgoAllDiffBC(Propagator cause) {
		this.aCause = cause;
	}

	public void reset(IntVar[] variables) {
		this.vars = variables;
		int n = vars.length;
		if(intervals==null || intervals.length<n){
			t = new int[2 * n + 2];
			d = new int[2 * n + 2];
			h = new int[2 * n + 2];
			bounds = new int[2 * n + 2];
			intervals = new Interval[n];
			minsorted = new Interval[n];
			maxsorted = new Interval[n];
			for (int i = 0; i < n; i++) {
				intervals[i] = new Interval();
			}
			sorter = new ArraySort(n,true,false);
		}
		for (int i = 0; i < n; i++) {
			Interval interval = intervals[i];
			interval.idx = i;
			interval.var = vars[i];
			minsorted[i] = interval;
			maxsorted[i] = interval;
		}
	}

	//****************************************************************************************************************//
	//****************************************************************************************************************//
	//****************************************************************************************************************//

	static enum SORT implements Comparator<Interval> {
		MAX {
			@Override
			public final int compare(Interval o1, Interval o2) {
				return o1.ub - o2.ub;
			}
		},
		MIN {
			@Override
			public final int compare(Interval o1, Interval o2) {
				return o1.lb - o2.lb;
			}
		},;
	}

	public void filter() throws ContradictionException {
		boolean again;
		do {
			again = false;
			sortIt();
			again |= filterLower();
			again |= filterUpper();
		} while (again);
	}

	protected void sortIt() {
		int n = vars.length;
		IntVar vt;
		for (int i = 0; i < n; i++) {
			vt = intervals[i].var;
			intervals[i].lb = vt.getLB();
			intervals[i].ub = vt.getUB();
		}
		sorter.sort(minsorted,n,SORT.MIN);
		sorter.sort(maxsorted,n,SORT.MAX);
		int min = minsorted[0].lb;
		int max = maxsorted[0].ub + 1;
		int last = min - 2;
		int nb = 0;
		bounds[0] = last;
		int i = 0, j = 0;
		while (true) {
			if (i < this.vars.length && min <= max) {
				if (min != last) {
					bounds[++nb] = last = min;
				}
				minsorted[i].minrank = nb;
				if (++i < this.vars.length) {
					min = minsorted[i].lb;
				}
			} else {
				if (max != last) {
					bounds[++nb] = last = max;
				}
				maxsorted[j].maxrank = nb;
				if (++j == this.vars.length) {
					break;
				}
				max = maxsorted[j].ub + 1;
			}
		}
		this.nbBounds = nb;
		bounds[nb + 1] = bounds[nb] + 2;
	}

	protected void pathset(int[] tab, int start, int end, int to) {
		int next = start;
		int prev = next;
		while (prev != end) {
			next = tab[prev];
			tab[prev] = to;
			prev = next;
		}
	}

	protected int pathmin(int[] tab, int i) {
		while (tab[i] < i) {
			i = tab[i];
		}
		return i;
	}

	protected int pathmax(int[] tab, int i) {
		while (tab[i] > i) {
			i = tab[i];
		}
		return i;
	}

	protected boolean filterLower() throws ContradictionException {
		boolean filter = false;
		for (int i = 1; i <= nbBounds + 1; i++) {
			t[i] = h[i] = i - 1;
			d[i] = bounds[i] - bounds[i - 1];
		}
		for (int i = 0; i < this.vars.length; i++) {
			int x = maxsorted[i].minrank;
			int y = maxsorted[i].maxrank;
			int z = pathmax(t, x + 1);
			int j = t[z];

			if (--d[z] == 0) {
				t[z] = z + 1;
				z = pathmax(t, t[z]);
				t[z] = j;
			}
			pathset(t, x + 1, z, z);
			if (d[z] < bounds[z] - bounds[y]) {
				aCause.contradiction(null, "");
			}
			if (h[x] > x) {
				int w = pathmax(h, h[x]);
				if (maxsorted[i].var.updateLowerBound(bounds[w], aCause)) {
					filter |= true;
					maxsorted[i].lb = maxsorted[i].var.getLB();//bounds[w];
				}
				pathset(h, x, w, w);
			}
			if (d[z] == bounds[z] - bounds[y]) {
				pathset(h, h[y], j - 1, y);
				h[y] = j - 1;
			}
		}
		return filter;
	}

	protected boolean filterUpper() throws ContradictionException {
		boolean filter = false;
		for (int i = 0; i <= nbBounds; i++) {
			t[i] = h[i] = i + 1;
			d[i] = bounds[i + 1] - bounds[i];
		}
		for (int i = this.vars.length - 1; i >= 0; i--) {
			int x = minsorted[i].maxrank;
			int y = minsorted[i].minrank;
			int z = pathmin(t, x - 1);
			int j = t[z];
			if (--d[z] == 0) {
				t[z] = z - 1;
				z = pathmin(t, t[z]);
				t[z] = j;
			}
			pathset(t, x - 1, z, z);
			if (d[z] < bounds[y] - bounds[z]) {
				aCause.contradiction(null, "");
			}
			if (h[x] < x) {
				int w = pathmin(h, h[x]);
				if (minsorted[i].var.updateUpperBound(bounds[w] - 1, aCause)) {
					filter |= true;
					minsorted[i].ub = minsorted[i].var.getUB();//bounds[w] - 1;
				}
				pathset(h, x, w, w);
			}
			if (d[z] == bounds[y] - bounds[z]) {
				pathset(h, h[y], j + 1, y);
				h[y] = j + 1;
			}
		}
		return filter;
	}

	private static class Interval implements Serializable {
		int minrank, maxrank;
		IntVar var;
		int idx;
		int lb, ub;
	}
}