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
package solver.constraints.propagators.nary.globalcardinality;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Bound Global cardinality : Given an array of variables vars, an array of variables card to represent the cardinalities, the constraint ensures that the number of occurences
 * of the value i among the variables is equal to card[i].
 * this constraint enforces :
 * - Bound Consistency over vars regarding the lower and upper bounds of cards
 * - maintain the upperbound of card by counting the number of variables in which each value
 * can occur
 * - maintain the lowerbound of card by counting the number of variables instantiated to a value
 * - enforce card[0] + ... + card[m] = n (n = the number of variables, m = number of values)
 * <br/>
 *
 * @author Hadrien Cambazard, Charles Prud'homme
 * @since 15/06/11
 */
public class PropBoundGlobalCardinality extends Propagator<IntVar> {

    private final int[] treelinks; // Tree links
    private final int[] d; // Diffs between critical capacities
    private final int[] h; // Hall interval links
    private final int[] bounds;

    private final int[] stableInterval;
    private final int[] potentialStableSets;
    private final int[] newMin;

    private final int[] minOccurrences, maxOccurrences;

    int offset = 0;

    private int nbBounds;
    final int nbVars;  //number of variables (without the cardinalities variables)
    final IntVar[] card;

    final Interval[] minsorted;
    final Interval[] maxsorted;

    final PartialSum l;
    final PartialSum u;

    private final int firstValue;
    final int range;

    //desynchornized copy of domains to make sure we properly counting
    //the number of variables that still have value i in their domain
    //(table val_maxOcc)
    final IStateInt[] val_maxOcc;
    final IStateInt[] val_minOcc;

    protected IntProcedure rem_proc;

    static IntVar[] makeVarTable(IntVar[] vars, IntVar[] card) {
        if (card != null) {
            IntVar[] allvars = new IntVar[vars.length + card.length];
            System.arraycopy(vars, 0, allvars, 0, vars.length);
            System.arraycopy(card, 0, allvars, vars.length, card.length);
            return allvars;
        } else {
            return vars;
        }
    }

    public PropBoundGlobalCardinality(IntVar[] vars, IntVar[] card, int firstCardValue, int lastCardValue, Solver solver,
                                      Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(makeVarTable(vars, card), solver, intVarPropagatorConstraint, PropagatorPriority.LINEAR, false);
        this.card = card;
        this.range = lastCardValue - firstCardValue + 1;
        int n = vars.length;
        this.nbVars = n;
        treelinks = new int[2 * n + 2];
        d = new int[2 * n + 2];
        h = new int[2 * n + 2];
        bounds = new int[2 * n + 2];
        stableInterval = new int[2 * n + 2];
        potentialStableSets = new int[2 * n + 2];
        newMin = new int[n];

        final Interval[] intervals = new Interval[n];
        minsorted = new Interval[n];
        maxsorted = new Interval[n];
        for (int i = 0; i < nbVars; i++) {
            intervals[i] = new Interval();
            intervals[i].var = vars[i];
            intervals[i].idx = i;
            minsorted[i] = intervals[i];
            maxsorted[i] = intervals[i];
        }
        this.offset = firstCardValue;
        this.firstValue = firstCardValue;
        val_maxOcc = new IStateInt[range];
        val_minOcc = new IStateInt[range];
        for (int i = 0; i < range; i++) {
            val_maxOcc[i] = environment.makeInt(0);
            val_minOcc[i] = environment.makeInt(0);
        }

        rem_proc = new RemProc(this);
        l = new PartialSum(firstValue, range);
        u = new PartialSum(firstValue, range);
        minOccurrences = new int[range];
        maxOccurrences = new int[range];
    }

    int getMaxOcc(int i) {
        return card[i].getUB();
    }

    int getMinOcc(int i) {
        return card[i].getLB();
    }

    @Override
    public int getPropagationConditions() {
        return EventType.CUSTOM_PROPAGATION.mask + EventType.FULL_PROPAGATION.mask;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    /**
     * Build internal structure of the propagator, if necessary
     *
     * @throws solver.exception.ContradictionException
     *          if initialisation encounters a contradiction
     */
    protected void initialize() throws ContradictionException {
        int j = 0;
        for(; j < nbVars; j++){
            vars[j].updateLowerBound(offset, this);
            vars[j].updateUpperBound(offset + vars.length - nbVars - 1, this, false);
        }
        for(;j < vars.length; j++){
            vars[j].updateLowerBound(0, this);
            vars[j].updateUpperBound(nbVars, this, false);
        }
        initBackDataStruct();
        initCard();
        for (int i = 0; i < vars.length; i++) {
            IntVar var = vars[i];
            if (var.instantiated()) {
                // if a value has been instantiated to its max number of occurrences
                // remove it from all variables
                if (i < nbVars) {
                    int val = vars[i].getValue();
                    filterBCOnInst(val);
                } else {
                    filterBCOnInst(i - nbVars + offset);
                }
            }
        }
        if (directInconsistentCount())
            this.contradiction(null, "inconsistent");
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if((evtmask & EventType.FULL_PROPAGATION.mask) !=0){
            initialize();
        }
        filter();
    }

    void filter() throws ContradictionException {
        propagateSumCard();
        dynamicInitOfPartialSum();
        sortIt();

        // The variable domains must be inside the domain defined by
        // the lower bounds (l) and the upper bounds (u).
        assert (l.minValue() == u.minValue());
        assert (l.maxValue() == u.maxValue());
        assert (l.minValue() <= minsorted[0].var.getLB());
        assert (maxsorted[nbVars - 1].var.getUB() <= u.maxValue());
        assert (!directInconsistentCount());

        // Checks if there are values that must be assigned before the
        // smallest interval or after the last interval. If this is
        // the case, there is no solution to the problem
        // This is not an optimization since
        // filterLower{Min,Max} and
        // filterUpper{Min,Max} do not check for this case.
        if ((l.sum(l.minValue(), minsorted[0].var.getLB() - 1) > 0) ||
                (l.sum(maxsorted[nbVars - 1].var.getUB() + 1, l.maxValue()) > 0)) {
            this.contradiction(null, "inconsistent");
        }
        filterLowerMax();
        filterLowerMin();
        filterUpperMax();
        filterUpperMin();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            int val = vars[idx].getValue();
            forcePropagate(EventType.CUSTOM_PROPAGATION);
            // if a value has been instantiated to its max number of occurrences
            // remove it from all variables
            if (idx < nbVars) {
                //update lower bounds of cardinalities
                val_minOcc[val - offset].add(1);
                card[val - offset].updateLowerBound(val_minOcc[val - offset].get(), this);
                filterBCOnInst(val);
            } else {
                filterBCOnInst(idx - nbVars + offset);
            }
        } else {
            if (EventType.isInclow(mask)) {
                forcePropagate(EventType.CUSTOM_PROPAGATION);
                if (idx < nbVars) {
                    if (!vars[idx].hasEnumeratedDomain()) {
                        filterBCOnInf(idx);
                    }
                }
            }
            if (EventType.isDecupp(mask)) {
                forcePropagate(EventType.CUSTOM_PROPAGATION);
                if (idx < nbVars) {
                    if (!vars[idx].hasEnumeratedDomain()) {
                        filterBCOnSup(idx);
                    }
                }
            }
        }
        if (idx < nbVars) {
            eventRecorder.getDeltaMonitor(vars[idx]).forEach(rem_proc, EventType.REMOVE);
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            return this.constraint.isSatisfied();
        }
        return ESat.UNDEFINED;
    }

    /////////// ////////////////////////////////////////////////////////////////////////////////////////////////////////
    static class Interval {
        int minrank, maxrank;
        IntVar var;
        int idx;
    }

    /**
     * A class to deal with partial sum data structure adapted to
     * the filterLower{Min,Max} and filterUpper{Min,Max} functions.
     * Two elements before and after the element list will be added with a weight of 1
     */
    static final class PartialSum {
        private int[] sum;
        private int[] ds;
        private int firstValue, lastValue, range;


        public PartialSum(int firstValue, int count) {
            this.range = count;
            this.sum = new int[count + 5];
            this.ds = new int[count + 5];
            this.firstValue = firstValue - 3;
            this.lastValue = firstValue + count + 1;
        }

        public void compute(int[] elt) {
            sum[0] = 0;
            sum[1] = 1;
            sum[2] = 2;
            int i, j;
            for (i = 2; i < range + 2; i++) {
                sum[i + 1] = sum[i] + elt[i - 2];
            }
            sum[i + 1] = sum[i] + 1;
            sum[i + 2] = sum[i + 1] + 1;

            i = range + 3;
            for (j = i + 1; i > 0; ) {
                while (sum[i] == sum[i - 1]) {
                    ds[i--] = j;
                }
                j = ds[j] = i--;
            }
            ds[j] = 0;
        }

        public int sum(int from, int to) {
            if (from <= to) {
                return sum[to - firstValue] - sum[from - firstValue - 1];
            } else {
                return sum[to - firstValue - 1] - sum[from - firstValue];
            }
        }

        public int minValue() {
            return firstValue + 3;
        }

        public int maxValue() {
            return lastValue - 2;
        }

        public int skipNonNullElementsRight(int value) {
            value -= firstValue;
            return (ds[value] < value ? value : ds[value]) + firstValue;
        }

        public int skipNonNullElementsLeft(int value) {
            value -= firstValue;
            return (ds[value] > value ? ds[ds[value]] : value) + firstValue;
        }
    }


    static enum SORT implements Comparator<Interval> {
        MAX {
            @Override
            public int compare(Interval o1, Interval o2) {
                return o1.var.getUB() - o2.var.getUB();
            }
        },
        MIN {
            @Override
            public int compare(Interval o1, Interval o2) {
                return o1.var.getLB() - o2.var.getLB();
            }
        },
        ;
    }

    void sortIt() {
        Arrays.sort(minsorted, SORT.MIN);
        Arrays.sort(maxsorted, SORT.MAX);

        int min = minsorted[0].var.getLB();
        int max = maxsorted[0].var.getUB() + 1;
        int last = l.firstValue + 1; //change here compared to the boundalldiff
        int nb = 0;
        bounds[0] = last;

        int i = 0, j = 0;
        while (true) {
            if (i < nbVars && min <= max) {
                if (min != last) {
                    bounds[++nb] = last = min;
                }
                minsorted[i].minrank = nb;
                if (++i < nbVars) {
                    min = minsorted[i].var.getLB();
                }
            } else {
                if (max != last) {
                    bounds[++nb] = last = max;
                }
                maxsorted[j].maxrank = nb;
                if (++j == nbVars) {
                    break;
                }
                max = maxsorted[j].var.getUB() + 1;
            }
        }

        this.nbBounds = nb;
        bounds[nb + 1] = u.lastValue + 1; //change here compared to the boundalldiff
    }

    void pathset(int[] tab, int start, int end, int to) {
        int next = start;
        int prev = next;

        while (prev != end) {
            next = tab[prev];
            tab[prev] = to;
            prev = next;
        }
    }

    int pathmin(int[] tab, int i) {
        while (tab[i] < i) {
            i = tab[i];
        }
        return i;
    }

    int pathmax(int[] tab, int i) {
        while (tab[i] > i) {
            i = tab[i];
        }
        return i;
    }

    /**
     * Shrink the lower bounds for the max occurences
     *
     * @throws ContradictionException
     */
    void filterLowerMax() throws ContradictionException {
        int i, j, w, x, y, z;

        for (i = 1; i <= nbBounds + 1; i++) {
            treelinks[i] = h[i] = i - 1;
            d[i] = u.sum(bounds[i - 1], bounds[i] - 1);
        }
        for (i = 0; i < nbVars; i++) { // visit intervals in increasing max order
            // get interval bounds
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            j = treelinks[z = pathmax(treelinks, x + 1)];
            if (--d[z] == 0) {
                treelinks[z = pathmax(treelinks, treelinks[z] = z + 1)] = j;
            }
            pathset(treelinks, x + 1, z, z);
            if (d[z] < u.sum(bounds[y], bounds[z] - 1)) {
                this.contradiction(null, "inconsistent");
            }
            if (h[x] > x) {
                w = pathmax(h, h[x]);
//                updateLowerBound(maxsorted[i].var, bounds[w], maxsorted[i].idx);
                maxsorted[i].var.updateLowerBound(bounds[w], this); //CPRU not idempotent
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[y], bounds[z] - 1)) {
                pathset(h, h[y], j - 1, y); // mark hall interval
                h[y] = j - 1; //("hall interval [%d,%d)\n",bounds[j],bounds[y]);
            }
        }
    }

    /**
     * Shrink the upper bounds for the max occurences
     *
     * @throws ContradictionException
     */
    void filterUpperMax() throws ContradictionException {
        int i, j, w, x, y, z;

        for (i = 0; i <= nbBounds; i++) {
            d[i] = u.sum(bounds[i], bounds[treelinks[i] = h[i] = i + 1] - 1);
        }
        for (i = nbVars; --i >= 0; ) { // visit intervals in decreasing min order
            // get interval bounds
            x = minsorted[i].maxrank;
            y = minsorted[i].minrank;
            j = treelinks[z = pathmin(treelinks, x - 1)];
            if (--d[z] == 0) {
                treelinks[z = pathmin(treelinks, treelinks[z] = z - 1)] = j;
            }
            pathset(treelinks, x - 1, z, z);
            if (d[z] < u.sum(bounds[z], bounds[y] - 1)) {
                this.contradiction(null, "inconsistent");
            }
            if (h[x] < x) {
                w = pathmin(h, h[x]);
//                updateUpperBound(minsorted[i].var, bounds[w] - 1, minsorted[i].idx);
                minsorted[i].var.updateUpperBound(bounds[w] - 1, this, false);
                pathset(h, x, w, w);
            }
            if (d[z] == u.sum(bounds[z], bounds[y] - 1)) {
                pathset(h, h[y], j + 1, y);
                h[y] = j + 1;
            }
        }
    }

    /*
      * Shrink the lower bounds for the min occurrences model.
      * called as: filterLowerMin(t, d, h, stableInterval, potentialStableSets, newMin);
      */
    void filterLowerMin() throws ContradictionException {
        int i, j, w, x, y, z, v;

        for (w = i = nbBounds + 1; i > 0; i--) {
            potentialStableSets[i] = stableInterval[i] = i - 1;
            d[i] = l.sum(bounds[i - 1], bounds[i] - 1);
            // If the capacity between both bounds is zero, we have
            // an unstable set between these two bounds.
            if (d[i] == 0) {
                h[i - 1] = w;
            } else {
                w = h[w] = i - 1;
            }
        }

        for (i = w = nbBounds + 1; i >= 0; i--) {
            if (d[i] == 0) {
                treelinks[i] = w;
            } else {
                w = treelinks[w] = i;
            }
        }

        for (i = 0; i < nbVars; i++) { // visit intervals in increasing max order
            // Get interval bounds
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            j = treelinks[z = pathmax(treelinks, x + 1)];
            if (z != x + 1) {
                // if bounds[z] - 1 belongs to a stable set,
                // [bounds[x], bounds[z]) is a sub set of this stable set
                v = potentialStableSets[w = pathmax(potentialStableSets, x + 1)];
                pathset(potentialStableSets, x + 1, w, w); // path compression
                w = y < z ? y : z;
                pathset(potentialStableSets, potentialStableSets[w], v, w);
                potentialStableSets[w] = v;
            }

            if (d[z] <= l.sum(bounds[y], bounds[z] - 1)) {
                // (potentialStableSets[y], y] is a stable set
                w = pathmax(stableInterval, potentialStableSets[y]);
                pathset(stableInterval, potentialStableSets[y], w, w); // Path compression
                pathset(stableInterval, stableInterval[y], v = stableInterval[w], y);
                stableInterval[y] = v;
            } else {
                // Decrease the capacity between the two bounds
                if (--d[z] == 0) {
                    treelinks[z = pathmax(treelinks, treelinks[z] = z + 1)] = j;
                }

                // If the lower bound belongs to an unstable or a stable set,
                // remind the new value we might assigned to the lower bound
                // in case the variable doesn't belong to a stable set.
                if (h[x] > x) {
                    w = newMin[i] = pathmax(h, x);
                    pathset(h, x, w, w); // path compression
                } else {
                    newMin[i] = x; // Do not shrink the variable
                }

                // If an unstable set is discovered
                if (d[z] == l.sum(bounds[y], bounds[z] - 1)) {
                    if (h[y] > y) {
                        y = h[y]; // Equivalent to pathmax since the path is fully compressed
                    }
                    pathset(h, h[y], j - 1, y); // mark the new unstable set
                    h[y] = j - 1;
                }
            }
            pathset(treelinks, x + 1, z, z); // path compression
        }

        // If there is a failure set
        if (h[nbBounds] != 0) {
            this.contradiction(null, "inconsistent");
        }

        // Perform path compression over all elements in
        // the stable interval data structure. This data
        // structure will no longer be modified and will be
        // accessed n or 2n times. Therefore, we can afford
        // a linear time compression.
        for (i = nbBounds + 1; i > 0; i--) {
            if (stableInterval[i] > i) {
                stableInterval[i] = w;
            } else {
                w = i;
            }
        }

        // For all variables that are not a subset of a stable set, shrink the lower bound
        for (i = nbVars - 1; i >= 0; i--) {
            x = maxsorted[i].minrank;
            y = maxsorted[i].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
//                updateLowerBound(maxsorted[i].var, l.skipNonNullElementsRight(bounds[newMin[i]]), maxsorted[i].idx);
                maxsorted[i].var.updateLowerBound(l.skipNonNullElementsRight(bounds[newMin[i]]), this);
            }
        }
    }

    /*
    * Shrink the upper bounds for the min occurrences model.
    * called as: filterUpperMin(t, d, h, stableInterval, newMin);
    */
    void filterUpperMin() throws ContradictionException {
        int i, w = 0, n = nbVars;
        for (i = 0; i <= nbBounds; i++) {
            d[i] = l.sum(bounds[i], bounds[i + 1] - 1);
            if (d[i] == 0) {
                treelinks[i] = w;
            } else {
                w = treelinks[w] = i;
            }
        }
        treelinks[w] = i;
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
            // Get interval bounds
            int x = minsorted[i].maxrank;
            int y = minsorted[i].minrank;

            // Solve the lower bound model
            int z = pathmin(treelinks, x - 1);
            int j = treelinks[z];

            // If the variable is not in a discovered stable set
            // Possible optimization: Use the array stableInterval to perform this palm
            if (d[z] > l.sum(bounds[z], bounds[y] - 1)) {
                if (--d[z] == 0) {
                    treelinks[z] = z - 1;
                    z = pathmin(treelinks, treelinks[z]);
                    treelinks[z] = j;
                }
                if (h[x] < x) {
                    w = pathmin(h, h[x]);
                    newMin[i] = w;       // re-use the table newMin to store the max
                    pathset(h, x, w, w); // path compression
                } else {
                    newMin[i] = x;
                }
                if (d[z] == l.sum(bounds[z], bounds[y] - 1)) {
                    if (h[y] < y) {
                        y = h[y];
                    }
                    pathset(h, h[y], j + 1, y);
                    h[y] = j + 1;
                }
            }
            pathset(treelinks, x - 1, z, z);
        }
        // For all variables that are not subsets of a stable set, shrink the upper bound
        for (i = n - 1; i >= 0; i--) {
            int x = minsorted[i].minrank;
            int y = minsorted[i].maxrank;
            if ((stableInterval[x] <= x) || (y > stableInterval[x])) {
//                updateUpperBound(minsorted[i].var, l.skipNonNullElementsLeft(bounds[newMin[i]] - 1), minsorted[i].idx);
                minsorted[i].var.updateUpperBound(l.skipNonNullElementsLeft(bounds[newMin[i]] - 1), this, false);
            }
        }

    }

    final void initBackDataStruct() throws ContradictionException {
        for (int i = 0; i < range; i++) {
            for (int j = 0; j < nbVars; j++) {
                if (vars[j].contains(i + offset)) {
                    val_maxOcc[i].add(1);
                }
                if (vars[j].instantiatedTo(i + offset)) {
                    val_minOcc[i].add(1);
                }
            }
        }
    }

    void initCard() throws ContradictionException {
        for (int i = 0; i < range; i++) {
            if (val_maxOcc[i].get() == 0) {
                card[i].instantiateTo(0, this);
            } else {
                card[i].updateLowerBound(val_minOcc[i].get(), this);
            }
        }
    }

    boolean directInconsistentCount() {
        for (int i = 0; i < range; i++) {
            if (val_maxOcc[i].get() < card[i].getLB() ||
                    val_minOcc[i].get() > card[i].getUB())
                return true;
        }
        return false;
    }

    final void dynamicInitOfPartialSum() {
        for (int i = 0; i < range; i++) {
            maxOccurrences[i] = card[i].getUB();
            minOccurrences[i] = card[i].getLB();
        }
        l.compute(minOccurrences);
        u.compute(maxOccurrences);
    }

    //in case of bound variables, the bound has to be checked
    final void filterBCOnInf(int i) throws ContradictionException {
        int inf = vars[i].getLB();
        int nbInf = val_minOcc[inf - offset].get();
        if (vars[i].instantiatedTo(inf)) {
            nbInf--;
        }
        if (nbInf == getMaxOcc(inf - offset)) {
            vars[i].updateLowerBound(inf + 1, this);//CPRU not idempotent
        }
    }

    //in case of bound variables, the bound has to be checked
    final void filterBCOnSup(int i) throws ContradictionException {
        int sup = vars[i].getUB();
        int nbSup = val_minOcc[sup - offset].get();
        if (vars[i].instantiatedTo(sup)) {
            nbSup--;
        }
        if (nbSup == getMaxOcc(sup - offset)) {
            vars[i].updateUpperBound(sup - 1, this, true);//CPRU not idempotent
        }
    }

    /**
     * Enforce simple occurrences reasonnings on value val
     * no need to reason on the number of possible (instead of sure) values
     * as this will be done as part of the BC on vars
     *
     * @param val
     * @throws ContradictionException
     */
    void filterBCOnInst(int val) throws ContradictionException {
        int nbvalsure = val_minOcc[val - offset].get();
        if (nbvalsure > getMaxOcc(val - offset)) {
            this.contradiction(null, "inconsistent");
        } else if (nbvalsure == getMaxOcc(val - offset)) {
            for (int j = 0; j < nbVars; j++) {
                if (!vars[j].instantiatedTo(val)) {
                    vars[j].removeValue(val, this);//CPRU not idempotent because data structure is maintained in awakeOnX methods
                }
            }
        }
    }

    /**
     * Enforce sum of the cardinalities = nbVariable
     *
     * @throws ContradictionException
     */
    void propagateSumCard() throws ContradictionException {
        boolean fixpoint = true;
        while (fixpoint) {
            fixpoint = false;
            int lb = 0;
            int ub = 0;
            for (int i = 0; i < range; i++) {
                lb += card[i].getLB();
                ub += card[i].getUB();

            }
            for (int i = 0; i < range; i++) {
                fixpoint |= card[i].updateUpperBound(nbVars - (lb - card[i].getLB()), this, false);
                fixpoint |= card[i].updateLowerBound(nbVars - (ub - card[i].getUB()), this);
            }
        }
    }

    private static class RemProc implements IntProcedure {

        private final PropBoundGlobalCardinality p;

        public RemProc(PropBoundGlobalCardinality p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            int o = p.offset;
            p.val_maxOcc[i - o].add(-1);
            p.card[i - o].updateUpperBound(p.val_maxOcc[i - o].get(), this.p, true);//CPRU not idempotent
        }
    }

}
