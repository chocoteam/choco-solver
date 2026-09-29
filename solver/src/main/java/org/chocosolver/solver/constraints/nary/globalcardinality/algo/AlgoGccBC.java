/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality.algo;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;
import org.chocosolver.util.tools.MathUtils;

/**
 * Bound-consistency algorithm for the global cardinality constraint (GCC).
 * <p>
 * Based on: C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and S.B. Sadjad.
 * "An efficient bounds consistency algorithm for the global cardinality constraint." CP-2003.
 *
 * @author Charles Prud'homme
 */
public class AlgoGccBC {

    private final Propagator<?> aCause;
    private IntVar[] vars;
    private int n;

    // Tree/diff/hall-interval links, shared across the four sub-filters of a single pass,
    // exactly as in the reference implementation.
    private int[] t;
    private int[] d;
    private int[] h;
    private int[] bounds;
    private int[] stableInterval;
    private int[] potentialStableSets;
    private int[] newMin;

    private int nbBounds;

    private Interval[] intervals;
    private int[] minsorted;
    private int[] maxsorted;

    private IntComparator minComp;
    private IntComparator maxComp;
    private ArraySort<Interval> sorter;

    public AlgoGccBC(Propagator<?> cause) {
        this.aCause = cause;
    }

    public void reset(IntVar[] variables) {
        this.vars = variables;
        this.n = vars.length;
        if (intervals == null || intervals.length < n) {
            t = new int[2 * n + 2];
            d = new int[2 * n + 2];
            h = new int[2 * n + 2];
            bounds = new int[2 * n + 2];
            stableInterval = new int[2 * n + 2];
            potentialStableSets = new int[2 * n + 2];
            newMin = new int[n];
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

    /**
     * Enforces bound-consistency on {@code vars} given dense minimum/maximum occurrence bounds
     * over the contiguous range {@code [firstValue, firstValue + minOcc.length - 1]}.
     *
     * @param minOcc     minimum number of occurrences, per value, dense on the range
     * @param maxOcc     maximum number of occurrences, per value, dense on the range
     * @param firstValue first value of the range covered by {@code minOcc}/{@code maxOcc}
     * @return {@code true} iff at least one bound update has been done
     */
    public boolean filter(int[] minOcc, int[] maxOcc, int firstValue) throws ContradictionException {
        int range = minOcc.length;
        PartialSum l = new PartialSum(firstValue, range, minOcc);
        PartialSum u = new PartialSum(firstValue, range, maxOcc);
        boolean hasFiltered = false;
        boolean again;
        do {
            sortIt(firstValue, range);
            int lowLb = vars[minsorted[0]].getLB();
            int highUb = vars[maxsorted[n - 1]].getUB();
            if (l.sum(l.minValue(), lowLb - 1) > 0 || l.sum(highUb + 1, l.maxValue()) > 0) {
                aCause.fails();
            }
            again = filterLowerMax(u);
            again |= filterLowerMin(l);
            again |= filterUpperMax(u);
            again |= filterUpperMin(l);
            hasFiltered |= again;
        } while (again);
        return hasFiltered;
    }

    private void sortIt(int firstValue, int range) {
        for (int i = 0; i < n; i++) {
            intervals[i].lb = vars[i].getLB();
            intervals[i].ub = vars[i].getUB() + 1;
        }
        sorter.sort(minsorted, n, minComp);
        sorter.sort(maxsorted, n, maxComp);

        int min = intervals[minsorted[0]].lb;
        int max = intervals[maxsorted[0]].ub;
        int last = firstValue - 2;
        int nb = 0;
        bounds[0] = last;

        int i = 0;
        int j = 0;
        while (true) {
            if (i < n && min <= max) {
                if (min != last) {
                    bounds[++nb] = last = min;
                }
                intervals[minsorted[i]].minrank = nb;
                if (++i < n) {
                    min = intervals[minsorted[i]].lb;
                }
            } else {
                if (max != last) {
                    bounds[++nb] = last = max;
                }
                intervals[maxsorted[j]].maxrank = nb;
                if (++j == n) {
                    break;
                }
                max = intervals[maxsorted[j]].ub;
            }
        }
        this.nbBounds = nb;
        bounds[nb + 1] = firstValue + range + 2;
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

    /**
     * Shrinks the lower bounds so that no value exceeds its maximum number of occurrences.
     */
    private boolean filterLowerMax(PartialSum u) throws ContradictionException {
        boolean filter = false;
        for (int i = 1; i <= nbBounds + 1; i++) {
            t[i] = h[i] = i - 1;
            d[i] = u.sum(bounds[i - 1], bounds[i] - 1);
            // A slot whose capacity is ALREADY zero here (e.g. a run of consecutive values with
            // maxOcc == 0) must be redirected to its upward neighbour right now, exactly as the
            // main loop does when a capacity reaches zero through a decrement. Otherwise, pathmax
            // can land on it, "--d[z] == 0" is never true (d[z] goes from 0 to -1, skipping the
            // transition) and t[] is left inconsistent: pathset may then loop forever, or the
            // Hall-interval check that should fail is silently skipped.
            if (d[i] == 0) {
                t[i] = i + 1;
            }
        }
        for (int i = 0; i < n; i++) { // visit intervals in increasing max order
            int idx = maxsorted[i];
            int x = intervals[idx].minrank;
            int y = intervals[idx].maxrank;
            int z = pathmax(t, x + 1);
            int j = t[z];
            if (--d[z] == 0) {
                t[z] = z + 1;
                z = pathmax(t, t[z]);
                t[z] = j;
            }
            pathset(t, x + 1, z, z);
            if (d[z] < u.sum(bounds[y], bounds[z] - 1)) {
                aCause.fails();
            }
            if (h[x] > x) {
                int w = pathmax(h, h[x]);
                int hallMax = bounds[w];
                if (vars[idx].updateLowerBound(hallMax, aCause)) {
                    filter = true;
                    intervals[idx].lb = hallMax;
                }
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[y], bounds[z] - 1)) {
                pathset(h, h[y], j - 1, y);
                h[y] = j - 1;
            }
        }
        return filter;
    }

    /**
     * Shrinks the upper bounds so that no value exceeds its maximum number of occurrences.
     */
    private boolean filterUpperMax(PartialSum u) throws ContradictionException {
        boolean filter = false;
        for (int i = 0; i <= nbBounds; i++) {
            t[i] = h[i] = i + 1;
            d[i] = u.sum(bounds[i], bounds[i + 1] - 1);
            // Mirror of the zero-capacity redirection in filterLowerMax: here the climb goes
            // towards lower indices (pathmin), so an already-exhausted slot points downward.
            if (d[i] == 0) {
                t[i] = i - 1;
            }
        }
        for (int i = n - 1; i >= 0; i--) { // visit intervals in decreasing min order
            int idx = minsorted[i];
            int x = intervals[idx].maxrank;
            int y = intervals[idx].minrank;
            int z = pathmin(t, x - 1);
            int j = t[z];
            if (--d[z] == 0) {
                t[z] = z - 1;
                z = pathmin(t, t[z]);
                t[z] = j;
            }
            pathset(t, x - 1, z, z);
            if (d[z] < u.sum(bounds[z], bounds[y] - 1)) {
                aCause.fails();
            }
            if (h[x] < x) {
                int w = pathmin(h, h[x]);
                int hallMin = bounds[w];
                if (vars[idx].updateUpperBound(hallMin - 1, aCause)) {
                    filter = true;
                    intervals[idx].ub = hallMin;
                }
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[z], bounds[y] - 1)) {
                pathset(h, h[y], j + 1, y);
                h[y] = j + 1;
            }
        }
        return filter;
    }

    /**
     * Shrinks the lower bounds so that every value reaches its minimum number of occurrences.
     */
    private boolean filterLowerMin(PartialSum l) throws ContradictionException {
        boolean filter = false;
        int i;
        int j;
        int w;
        int x;
        int y;
        int z;
        int v;

        for (w = i = nbBounds + 1; i > 0; i--) {
            potentialStableSets[i] = stableInterval[i] = i - 1;
            d[i] = l.sum(bounds[i - 1], bounds[i] - 1);
            // If the capacity between both bounds is zero, we have an unstable set between them.
            if (d[i] == 0) {
                h[i - 1] = w;
            } else {
                w = h[w] = i - 1;
            }
        }

        for (i = w = nbBounds + 1; i >= 0; i--) {
            if (d[i] == 0) {
                t[i] = w;
            } else {
                w = t[w] = i;
            }
        }

        for (i = 0; i < n; i++) { // visit intervals in increasing max order
            int idx = maxsorted[i];
            x = intervals[idx].minrank;
            y = intervals[idx].maxrank;
            j = t[z = pathmax(t, x + 1)];
            if (z != x + 1) {
                // [bounds[x], bounds[z]) is a subset of a stable set
                v = potentialStableSets[w = pathmax(potentialStableSets, x + 1)];
                pathset(potentialStableSets, x + 1, w, w); // path compression
                w = Math.min(y, z);
                pathset(potentialStableSets, potentialStableSets[w], v, w);
                potentialStableSets[w] = v;
            }

            if (d[z] <= l.sum(bounds[y], bounds[z] - 1)) {
                // (potentialStableSets[y], y] is a stable set
                w = pathmax(stableInterval, potentialStableSets[y]);
                pathset(stableInterval, potentialStableSets[y], w, w); // path compression
                pathset(stableInterval, stableInterval[y], v = stableInterval[w], y);
                stableInterval[y] = v;
            } else {
                // decrease the capacity between the two bounds
                if (--d[z] == 0) {
                    t[z] = z + 1;
                    z = pathmax(t, t[z]);
                    t[z] = j;
                }
                // remind the new value the variable might get, in case it is not in a stable set
                if (h[x] > x) {
                    w = newMin[i] = pathmax(h, x);
                    pathset(h, x, w, w); // path compression
                } else {
                    newMin[i] = x; // do not shrink the variable
                }
                // if an unstable set is discovered
                if (d[z] == l.sum(bounds[y], bounds[z] - 1)) {
                    if (h[y] > y) {
                        y = h[y]; // equivalent to pathmax since the path is fully compressed
                    }
                    pathset(h, h[y], j - 1, y); // mark the new unstable set
                    h[y] = j - 1;
                }
            }
            pathset(t, x + 1, z, z); // path compression
        }

        // if there is a failure set
        if (h[nbBounds] != 0) {
            aCause.fails();
        }

        // path compression over all elements of the stable interval structure (linear, done once)
        for (i = nbBounds + 1; i > 0; i--) {
            if (stableInterval[i] > i) {
                stableInterval[i] = w;
            } else {
                w = i;
            }
        }

        // for all variables that are not a subset of a stable set, shrink the lower bound
        for (i = n - 1; i >= 0; i--) {
            int idx = maxsorted[i];
            x = intervals[idx].minrank;
            y = intervals[idx].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
                int newLb = l.skipNonNullElementsRight(bounds[newMin[i]]);
                if (vars[idx].updateLowerBound(newLb, aCause)) {
                    filter = true;
                    intervals[idx].lb = newLb;
                }
            }
        }
        return filter;
    }

    /**
     * Shrinks the upper bounds so that every value reaches its minimum number of occurrences.
     * Relies on {@code stableInterval}, as computed by the last call to {@link #filterLowerMin}.
     */
    private boolean filterUpperMin(PartialSum l) throws ContradictionException {
        boolean filter = false;
        int w = 0;
        int i;
        for (i = 0; i <= nbBounds; i++) {
            d[i] = l.sum(bounds[i], bounds[i + 1] - 1);
            if (d[i] == 0) {
                t[i] = w;
            } else {
                w = t[w] = i;
            }
        }
        t[w] = i;
        w = 0;
        for (i = 1; i <= nbBounds; i++) {
            if (d[i - 1] == 0) {
                h[i] = w;
            } else {
                w = h[w] = i;
            }
        }
        h[w] = i;
        for (i = n - 1; i >= 0; i--) { // visit intervals in decreasing min order
            int idx = minsorted[i];
            int x = intervals[idx].maxrank;
            int y = intervals[idx].minrank;

            int z = pathmin(t, x - 1);
            int j = t[z];

            // if the variable is not in a discovered stable set
            if (d[z] > l.sum(bounds[z], bounds[y] - 1)) {
                if (--d[z] == 0) {
                    t[z] = z - 1;
                    z = pathmin(t, t[z]);
                    t[z] = j;
                }
                int newMax;
                if (h[x] < x) {
                    w = pathmin(h, h[x]);
                    newMax = w;
                    pathset(h, x, w, w); // path compression
                } else {
                    newMax = x;
                }
                newMin[i] = newMax;
                if (d[z] == l.sum(bounds[z], bounds[y] - 1)) {
                    if (h[y] < y) {
                        y = h[y];
                    }
                    pathset(h, h[y], j + 1, y);
                    h[y] = j + 1;
                }
            }
            pathset(t, x - 1, z, z);
        }
        // for all variables that are not subsets of a stable set, shrink the upper bound
        for (i = n - 1; i >= 0; i--) {
            int idx = minsorted[i];
            int x = intervals[idx].minrank;
            int y = intervals[idx].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
                int newUb = l.skipNonNullElementsLeft(bounds[newMin[i]] - 1);
                if (vars[idx].updateUpperBound(newUb, aCause)) {
                    filter = true;
                    intervals[idx].ub = newUb + 1;
                }
            }
        }
        return filter;
    }

    private static final class Interval {
        private int minrank;
        private int maxrank;
        private int lb;
        private int ub; // exclusive, i.e. getUB() + 1
    }

    /**
     * Partial-sum data structure adapted to {@code filterLower{Min,Max}}/{@code filterUpper{Min,Max}}:
     * sums an array of per-value occurrence bounds over any sub-range in O(1), with two sentinel
     * elements of weight 1 added on each side.
     */
    private static final class PartialSum {
        private final int[] sum;
        private final int[] ds;
        private final int firstValue;
        private final int lastValue;

        private PartialSum(int firstValue, int count, int[] elt) {
            this.sum = new int[count + 5];
            this.firstValue = firstValue - 3;
            this.lastValue = firstValue + count + 1;
            sum[0] = 0;
            sum[1] = 1;
            sum[2] = 2;
            int i;
            int j;
            for (i = 2; i < count + 2; i++) {
                sum[i + 1] = sum[i] + elt[i - 2];
            }
            sum[i + 1] = sum[i] + 1;
            sum[i + 2] = sum[i + 1] + 1;
            ds = new int[count + 5];
            i = count + 3;
            for (j = i + 1; i > 0; ) {
                while (sum[i] == sum[i - 1]) {
                    ds[i--] = j;
                }
                j = ds[j] = i--;
            }
            ds[j] = 0;
        }

        private int sum(int from, int to) {
            if (from <= to) {
                return sum[to - firstValue] - sum[from - firstValue - 1];
            } else {
                return sum[to - firstValue - 1] - sum[from - firstValue];
            }
        }

        private int minValue() {
            return firstValue + 3;
        }

        private int maxValue() {
            return lastValue - 2;
        }

        private int skipNonNullElementsRight(int value) {
            value -= firstValue;
            return (Math.max(ds[value], value)) + firstValue;
        }

        private int skipNonNullElementsLeft(int value) {
            value -= firstValue;
            return (ds[value] > value ? ds[ds[value]] : value) + firstValue;
        }
    }
}
