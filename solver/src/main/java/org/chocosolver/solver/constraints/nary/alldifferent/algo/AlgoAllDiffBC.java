/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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

import java.util.Comparator;
import org.chocosolver.util.tools.MathUtils;

public class AlgoAllDiffBC {

    private int[] t; // Tree links
    private int[] d; // Diffs between critical capacities
    private int[] h; // Hall interval links
    private int[] bounds;

    private int nbBounds;

    private Interval[] intervals, minsorted, maxsorted;

    private Propagator aCause;
    private IntVar[] vars;

    private ArraySort<Interval> sorter;

    public AlgoAllDiffBC(Propagator cause) {
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
            minsorted = new Interval[n];
            maxsorted = new Interval[n];
            for (int i = 0; i < n; i++) {
                intervals[i] = new Interval();
            }
            sorter = new ArraySort<>(n, true, false);
        }
        for (int i = 0; i < n; i++) {
            Interval interval = intervals[i];
            interval.var = vars[i];
            minsorted[i] = interval;
            maxsorted[i] = interval;
        }
    }

    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    private enum SORT implements Comparator<Interval> {
        MAX {
            @Override
            public final int compare(Interval o1, Interval o2) {
                return MathUtils.safeSubstract(o1.ub, o2.ub);
            }
        },
        MIN {
            @Override
            public final int compare(Interval o1, Interval o2) {
                return MathUtils.safeSubstract(o1.lb,o2.lb);
            }
        },;
    }

    public void filter() throws ContradictionException {
        boolean again;
        do {
            sortIt();
            again = filterLower();
            again |= filterUpper();
        } while (again);
    }

    private void sortIt() {
        int n = vars.length;
        IntVar vt;
        for (int i = 0; i < n; i++) {
            vt = intervals[i].var;
            intervals[i].lb = vt.getLB();
            intervals[i].ub = vt.getUB();
        }
        sorter.sort(minsorted, n, SORT.MIN);
        sorter.sort(maxsorted, n, SORT.MAX);
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
                aCause.fails();
            }
            if (h[x] > x) {
                int w = pathmax(h, h[x]);
                if (maxsorted[i].var.updateLowerBound(bounds[w], aCause)) {
                    filter = true;
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

    private boolean filterUpper() throws ContradictionException {
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
                aCause.fails();
            }
            if (h[x] < x) {
                int w = pathmin(h, h[x]);
                if (minsorted[i].var.updateUpperBound(bounds[w] - 1, aCause)) {
                    filter = true;
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

    private static class Interval  {
        private int minrank, maxrank;
        private IntVar var;
        private int lb, ub;
    }
}
