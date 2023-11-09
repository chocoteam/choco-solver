/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;
import org.chocosolver.util.tools.MathUtils;

public class AlgoAllDiffBC {

    private int[] t; // Tree links
    private int[] d; // Diffs between critical capacities
    private int[] h; // Hall interval links
    private int[] bounds;

    private int nbBounds;

    private Interval[] intervals;
    private int[] minsorted;
    private int[] maxsorted;

    private IntComparator minComp;
    private IntComparator maxComp;


    private final Propagator<?> aCause;
    private IntVar[] vars;

    private ArraySort<Interval> sorter;

    public AlgoAllDiffBC(Propagator<?> cause) {
        this.aCause = cause;
    }

    public void reset(IntVar[] variables) {
        this.vars = variables;
        int n = vars.length;
        if (intervals == null || intervals.length < n) {
            t = new int[2 * n + 2];
            d = new int[2 * n + 2];
            h = new int[2 * n + 2];
            bounds = new int[2 * n + 2];
            intervals = new Interval[n];
            minsorted = new int[n];
            maxsorted = new int[n];
            for (int i = 0; i < n; i++) {
                intervals[i] = new Interval();
            }
            sorter = new ArraySort<>(n, false, true);
        }
        for (int i = 0; i < n; i++) {
            minsorted[i] = i;
            maxsorted[i] = i;
        }
        minComp = (i1, i2) -> MathUtils.safeSubstract(intervals[i1].lb, intervals[i2].lb);
        maxComp = (i1, i2) -> MathUtils.safeSubstract(intervals[i1].ub, intervals[i2].ub);
    }

    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    // returns true iff at least one bound update has been done
    public boolean filter() throws ContradictionException {
        boolean hasFiltered = false;
        sortIt();
        filterLower();
        filterUpper();
        return hasFiltered;
    }

    private void sortIt() {
        int n = vars.length;
        IntVar vt;
        for (int i = 0; i < n; i++) {
            vt = vars[i];
            intervals[i].lb = vt.getLB();
            intervals[i].ub = vt.getUB() + 1;
        }
        sorter.sort(minsorted, n, minComp);
        sorter.sort(maxsorted, n, maxComp);
        int min = intervals[minsorted[0]].lb;
        int max = intervals[maxsorted[0]].ub;
        int last = min - 2;
        int nb = 0;
        bounds[0] = last;
        int i = 0, j = 0;
        while (true) {
            if (i < this.vars.length && min <= max) {
                if (min != last) {
                    bounds[++nb] = last = min;
                }
                intervals[minsorted[i]].minrank = nb;
                if (++i < this.vars.length) {
                    min = intervals[minsorted[i]].lb;
                }
            } else {
                if (max != last) {
                    bounds[++nb] = last = max;
                }
                intervals[maxsorted[j]].maxrank = nb;
                if (++j == this.vars.length) {
                    break;
                }
                max = intervals[maxsorted[j]].ub;
            }
        }
        this.nbBounds = nb;
        bounds[nb + 1] = bounds[nb] + 2;
    }

    private void pathset(int[] tab, int start, int end, int to) {
        int next = start;
        int prev = next;
        while (prev != end) {
            next = tab[prev];
            tab[prev] = to;
            prev = next;
        }
    }

    private int pathmin(int[] tab, int i) {
        while (tab[i] < i) {
            i = tab[i];
        }
        return i;
    }

    private int pathmax(int[] tab, int i) {
        while (tab[i] > i) {
            i = tab[i];
        }
        return i;
    }

    private boolean filterLower() throws ContradictionException {
        boolean filter = false;
        for (int i = 1; i <= nbBounds + 1; i++) {
            t[i] = h[i] = i - 1;
            d[i] = bounds[i] - bounds[i - 1];
        }
        for (int i = 0; i < this.vars.length; i++) {
            int minrank = intervals[maxsorted[i]].minrank;
            int y = intervals[maxsorted[i]].maxrank;
            int z = pathmax(t, minrank + 1);
            int j = t[z];

            if (--d[z] == 0) {
                t[z] = z + 1;
                z = pathmax(t, t[z]);
                t[z] = j;
            }
            pathset(t, minrank + 1, z, z);
            if (d[z] < bounds[z] - bounds[y]) {
                aCause.fails();
            }
            if (h[minrank] > minrank) {
                int maxrank = pathmax(h, h[minrank]);
                int hall_max = bounds[maxrank];
                if (vars[maxsorted[i]].updateLowerBound(hall_max, aCause)) {
                    filter = true;
                    intervals[maxsorted[i]].lb = hall_max;//bounds[maxrank];
                }
                pathset(h, minrank, maxrank, maxrank);
            }
            if (d[z] == bounds[z] - bounds[y]) {
                pathset(h, h[y], j - 1, y);
                h[y] = j - 1;
            }
        }
        return filter;
    }

    private boolean filterUpper() throws ContradictionException {
        boolean filter = false;
        for (int i = 0; i <= nbBounds; i++) {
            t[i] = h[i] = i + 1;
            d[i] = bounds[i + 1] - bounds[i];
        }
        for (int i = this.vars.length - 1; i >= 0; i--) {
            int maxrank = intervals[minsorted[i]].maxrank;
            int minrank = intervals[minsorted[i]].minrank;
            int z = pathmin(t, maxrank - 1);
            int j = t[z];
            if (--d[z] == 0) {
                t[z] = z - 1;
                z = pathmin(t, t[z]);
                t[z] = j;
            }
            pathset(t, maxrank - 1, z, z);
            if (d[z] < bounds[minrank] - bounds[z]) {
                aCause.fails();
            }
            if (h[maxrank] < maxrank) {
                int w = pathmin(h, h[maxrank]);
                int hall_min = bounds[w];
                if (vars[minsorted[i]].updateUpperBound(hall_min - 1, aCause)) {
                    filter = true;
                    intervals[minsorted[i]].ub = hall_min;//bounds[w] - 1;
                }
                pathset(h, maxrank, w, w);
            }
            if (d[z] == bounds[minrank] - bounds[z]) {
                pathset(h, h[minrank], j + 1, minrank);
                h[minrank] = j + 1;
            }
        }
        return filter;
    }

    private static class Interval {
        private int minrank, maxrank;
        private int lb, ub;
    }
}
