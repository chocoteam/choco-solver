/*
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

package solver.constraints.propagators.nary.alldifferent;

import choco.annotations.PropAnn;
import common.ESat;
import memory.IStateInt;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.io.Serializable;
import java.util.Comparator;

import static choco.annotations.PropAnn.Status.*;

/**
 * Based on: </br>
 * "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
 * A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
 * <br/>
 *
 * @author Hadrien Cambazard, Charles Prud'homme
 * @revision 04/03/12 : change sort
 * @since 07/02/11
 */
@PropAnn(tested = {BENCHMARK, CORRECTION, CONSISTENCY})
public class PropAllDiffBC extends Propagator<IntVar> {

    //TODO: minsorted et maxsorted => LinkedList
    //TODO: maintenir sortIt incrementalement

    int[] t; // Tree links
    int[] d; // Diffs between critical capacities
    int[] h; // Hall interval links
    int[] bounds;

    int nbBounds;

    Interval[] intervals;
    Interval[] minsorted;
    Interval[] maxsorted;

    int[] instantiatedValues;
    IStateInt ivIdx;

    public boolean infBoundModified = true;
    public boolean supBoundModified = true;

    public PropAllDiffBC(IntVar[] vars) {
        super(vars, PropagatorPriority.LINEAR, true);
        int n = vars.length;

        t = new int[2 * n + 2];
        d = new int[2 * n + 2];
        h = new int[2 * n + 2];
        bounds = new int[2 * n + 2];

        intervals = new Interval[n];
        minsorted = new Interval[n];
        maxsorted = new Interval[n];

        int idx = 0;
        instantiatedValues = new int[n];
        for (int i = 0; i < vars.length; i++) {
            Interval interval = new Interval();
            interval.var = vars[i];
            interval.idx = i;
            intervals[i] = interval;
            minsorted[i] = interval;
            maxsorted[i] = interval;
            if (vars[i].instantiated()) {
                instantiatedValues[idx++] = vars[i].getValue();
            }
        }
        ivIdx = environment.makeInt(idx);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    /**
     * Build internal structure of the propagator, if necessary
     *
     * @throws solver.exception.ContradictionException
     *          if initialisation encounters a contradiction
     */
    protected void initialize() throws ContradictionException {
        int left, right;
        for (int j = 0; j < vars.length; j++) {
            left = right = Integer.MIN_VALUE;
            for (int i = 0; i < j; i++) {
                if (vars[i].instantiated()) {
                    int val = vars[i].getValue();
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[j].removeInterval(left, right, aCause);
                        left = right = val;
                    }
                }
            }
            for (int i = j + 1; i < vars.length; i++) {
                if (vars[i].instantiated()) {
                    int val = vars[i].getValue();
                    if (val == right + 1) {
                        right = val;
                    } else {
                        vars[j].removeInterval(left, right, aCause);
                        left = right = val;
                    }
                }
            }
            vars[j].removeInterval(left, right, aCause);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            initialize();
        }
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            awakeOnInst(varIdx);
        } else if (EventType.isInclow(mask) && EventType.isDecupp(mask)) {
            infBoundModified = supBoundModified = true;
            if (!vars[varIdx].hasEnumeratedDomain()) {
                awakeOnBound(varIdx);
            }
        } else if (EventType.isInclow(mask)) {
            infBoundModified = true;
            if (!vars[varIdx].hasEnumeratedDomain()) {
                awakeOnInf(varIdx);
            }
        } else if (EventType.isDecupp(mask)) {
            supBoundModified = true;
            if (!vars[varIdx].hasEnumeratedDomain()) {
                awakeOnSup(varIdx);
            }
        }
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        if (this.isCompletelyInstantiated()) {
            for (int i = 0; i < vars.length; i++) {
                for (int j = i + 1; j < vars.length; j++) {
                    if (vars[i].getValue() == vars[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropAllDiffBC(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }

    //****************************************************************************************************************//
    //****************************************************************************************************************//
    //****************************************************************************************************************//

    protected void awakeOnBound(int i) throws ContradictionException {
        infBoundModified = supBoundModified = true;
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == vars[i].getLB()) {
                    vars[i].updateLowerBound(val + 1, aCause);
                }
                if (val == vars[i].getUB()) {
                    vars[i].updateUpperBound(val - 1, aCause);
                }
            }
        }
    }

    protected void awakeOnInf(int i) throws ContradictionException {
        infBoundModified = true;
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == vars[i].getLB()) {
                    vars[i].updateLowerBound(val + 1, aCause);
                }
            }
        }
    }

    protected void awakeOnSup(int i) throws ContradictionException {
        supBoundModified = true;
        for (int j = 0; j < vars.length; j++) {
            if (j != i && vars[j].instantiated()) {
                int val = vars[j].getValue();
                if (val == vars[i].getUB()) {
                    vars[i].updateUpperBound(val - 1, aCause);
                }
            }
        }
    }

    protected void awakeOnInst(int i) throws ContradictionException {   // Propagation classique
        infBoundModified = true;
        supBoundModified = true;
        int val = vars[i].getValue();
        for (int j = 0; j < vars.length; j++) {
            if (j != i) {
                vars[j].removeValue(val, aCause);
            }
        }
    }

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

    private void filter() throws ContradictionException {
        if (infBoundModified || supBoundModified) {
            initSort();
            do {
                sortIt();
                infBoundModified = filterLower();
                supBoundModified = filterUpper();
            } while (infBoundModified || supBoundModified);
        }
    }


    protected void initSort() {
        IntVar vt;
        for (int i = 0; i < vars.length; i++) {
            vt = intervals[i].var;
            intervals[i].lb = vt.getLB();
            intervals[i].ub = vt.getUB();
        }
    }

    /**
     * appears to be more efficient than Arrays.sort() because
     * it does not clone the array before sorting it,
     * but "simply" copy it into a temporary one -- intervals
     */
    private void _sort() {
        int n = vars.length;
        System.arraycopy(minsorted, 0, intervals, 0, n);
        mergeSort(intervals, minsorted, 0, n, SORT.MIN);
        System.arraycopy(maxsorted, 0, intervals, 0, n);
        mergeSort(intervals, maxsorted, 0, n, SORT.MAX);
    }

    protected void sortIt() {
        _sort();

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
                this.contradiction(null, "");
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
                this.contradiction(null, "");
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

    ///////////////////

    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset into src corresponding to low in dest
     */
    private static void mergeSort(Interval[] src,
                                  Interval[] dest,
                                  int low, int high,
                                  Comparator c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && c.compare(dest[j - 1], dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, c);
        mergeSort(dest, src, mid, high, c);

        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && c.compare(src[p], src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    private static void swap(Interval[] x, int a, int b) {
        Interval t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}