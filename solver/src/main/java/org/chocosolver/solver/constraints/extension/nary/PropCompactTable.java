/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.IStateLong;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.view.IntView;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.VariableUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Propagator for table constraint based on "Compact-Table: Efficiently Filtering Table Constraints
 * with Reversible Sparse Bit-Sets" Only for feasible Tuples
 *
 * @author Jean-Guillaume FAGES
 * @author Charles Prud'homme
 * @since 28/04/2016
 */
@Explained(ignored = true, comment = "Turned into clauses")
public class PropCompactTable extends Propagator<IntVar> {

    /**
     * This interface is used to create a support structure for the table constraint.
     */
    protected interface ISupport {

        /**
         * Factory method to create a support structure.
         * The choice of the support structure depends on the range and the number of values.
         * @param range the range of the variable, that is the difference between the upper and lower bounds.
         * @param nValues the number of values in the domain of the variable.
         * @param nWords the number of words in the bitset.
         * @return a support structure.
         */
        static ISupport make(int range, int nValues, int nWords){
            if(range > 256 && range >= 1.1 * nValues){
                // If the variable seems sparse, we use a sparse support structure
                return new SparseSupport(nValues, nWords);
            } else {
                // If the variable seems dense, we use a dense support structure
                return new DenseSupport(range, nWords);
            }
        }

        /**
         * Get the support for a given value.
         * @param val the value for which we want the support.
         * @return the support for the given value.
         */
        long[] get(int val);
    }

    /**
     * Dense support structure.
     * It is used when the variable is dense, that is when the range is equal to the number of values.
     * In that case, we use a simple array of long arrays to store the supports.
     */
    protected final static class DenseSupport implements ISupport {
        private final long[][] supports;

        public DenseSupport(int range, int nWords) {
            this.supports = new long[range][nWords];
        }

        @Override
        public long[] get(int val) {
            return supports[val];
        }
    }

    /**
     * Sparse support structure.
     * It is used when the variable is sparse, that is when the range is greater than the number of values.
     * In that case, we use a hash map to store the supports.
     */
    protected final static class SparseSupport implements ISupport {
        private final TIntObjectHashMap<long[]> map;
        private final int nWords;

        public SparseSupport(int nValues, int nWords) {
            this.nWords = nWords;
            this.map = new TIntObjectHashMap<>(nValues, 1.1f, -1);
        }

        @Override
        public long[] get(int val) {
            long[] m = map.get(val);
            if(m == null){
                m = new long[nWords];
                map.put(val, m);
            }
            return m;
        }
    }

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    RSparseBitSet currTable;
    protected Tuples tuples; // only for checker
    protected ISupport[] supports;
    int[][] residues;
    protected int[] offset;
    protected IIntDeltaMonitor[] monitors;
    protected final UnaryIntProcedure<Integer> onValRem;
    protected final boolean uniqueness;
    protected final boolean invariant;
    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * This method ensures that among the variables, there is no duplicate variable nor
     * views of scope's variables .
     *
     * @param vars scope
     * @return true if all variables are unique and not views of each other
     */
    private static boolean uniqueness(IntVar[] vars) {
        HashSet<IntVar> set = new LinkedHashSet<>();
        for (IntVar var : vars) {
            if (VariableUtils.isView(var)) {
                //noinspection unchecked
                IntView<IntVar> view = (IntView<IntVar>) var;
                var = view.getVariables()[0];
            }
            if (set.contains(var)) {
                return false;
            }
            set.add(var);
        }
        return true;
    }

    /**
     * Create a propagator for table constraint Only for feasible Tuples
     *
     * @param vars   scope
     * @param tuples list of feasible tuples
     */
    public PropCompactTable(IntVar[] vars, Tuples tuples) {
        super(vars, vars.length <= 3 ? PropagatorPriority.get(vars.length) : PropagatorPriority.QUADRATIC, true, tuples.allowUniversalValue());
        this.tuples = tuples;
        this.currTable = new RSparseBitSet(model.getEnvironment(), this.tuples.nbTuples());
        computeSupports(tuples);
        monitors = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            monitors[i] = vars[i].monitorDelta(this);
        }
        onValRem = makeProcedure();
        uniqueness = uniqueness(vars);
        invariant = uniqueness && !tuples.allowUniversalValue();
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    protected UnaryIntProcedure<Integer> makeProcedure() {
        //noinspection Convert2Diamond
        return new UnaryIntProcedure<Integer>() {
            int var, off;

            @Override
            public UnaryIntProcedure<Integer> set(Integer o) {
                var = o;
                off = offset[var];
                return this;
            }

            @Override
            public void execute(int i) {
                currTable.addToMask((supports[var].get(i - off)));
            }
        };
    }

    protected void computeSupports(Tuples tuples) {
        int n = vars.length;
        offset = new int[n];
        supports = new ISupport[n];
        residues = new int[n][];
        long[] tmp;
        for (int i = 0; i < n; i++) {
            int lb = vars[i].getLB();
            int ub = vars[i].getUB();
            offset[i] = lb;
            supports[i] = ISupport.make(vars[i].getRange(), vars[i].getDomainSize(), currTable.words.length);
            residues[i] = new int[ub - lb + 1];
        }
        int wI = 0;
        byte bI = 0;
        top:
        for (int ti = 0; ti < tuples.nbTuples(); ti++) {
            int[] tuple = tuples.get(ti);
            for (int i = 0; i < tuple.length; i++) {
                if (!vars[i].contains(tuple[i])) {
                    continue top;
                }
            }
            for (int i = 0; i < tuple.length; i++) {
                tmp = supports[i].get(tuple[i] - offset[i]);
                tmp[wI] |= 1L << (bI);
            }
            if (++bI > 63) {
                bI = 0;
                wI++;
            }
        }
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < vars.length; i++) {
                currTable.clearMask();
                int ub = vars[i].getUB();
                for (int v = vars[i].getLB(); v <= ub; v = vars[i].nextValue(v)) {
                    currTable.addToMask(supports[i].get(v - offset[i]));
                }
                currTable.intersectWithMask();
            }
            for (int i = 0; i < vars.length; i++) {
                monitors[i].startMonitoring();
            }
        }
        filterDomains();
    }

    @Override
    public void propagate(int vIdx, int mask) throws ContradictionException {
        currTable.clearMask();
        if (vars[vIdx].isInstantiated()) {
            currTable.addToMask(supports[vIdx].get(vars[vIdx].getValue() - offset[vIdx]));
        } else {
            if (vars[vIdx].getDomainSize() > monitors[vIdx].sizeApproximation()) {
                monitors[vIdx].forEachRemVal(onValRem.set(vIdx));
                currTable.reverseMask();
            } else {
                int ub = vars[vIdx].getUB();
                for (int v = vars[vIdx].getLB(); v <= ub; v = vars[vIdx].nextValue(v)) {
                    currTable.addToMask(supports[vIdx].get(v - offset[vIdx]));
                }
            }
        }
        currTable.intersectWithMask();
        if (currTable.isEmpty()) { // fail as soon as possible
            fails();
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    protected void filterDomains() throws ContradictionException {
        long count = currTable.nb1s();
        if (count == 0) { // Invariant 3.4  (Invariant 7.8 for CT*)
            fails();
        } else {
            // --> does not work with views
            if (invariant && count == VariableUtils.domainCardinality(vars)) { // Invariant 3.3, only for positive table
                setPassive();
                return;
            }
            for (int i = 0; i < vars.length; i++) {
                if (uniqueness && vars[i].isInstantiated()) continue; // --> does not work with views
                if (vars[i].hasEnumeratedDomain()) {
                    enumFilter(i);
                } else {
                    boundFilter(i);
                }
            }
        }
    }

    private void boundFilter(int i) throws ContradictionException {
        int lb = vars[i].getLB();
        int ub = vars[i].getUB();
        for (int v = lb; v <= ub; v++) {
            int index = residues[i][v - offset[i]];
            if ((currTable.words[index].get() & supports[i].get(v - offset[i])[index]) == 0L) {
                index = currTable.intersectIndex(supports[i].get(v - offset[i]));
                if (index == -1) {
                    lb++;
                } else {
                    residues[i][v - offset[i]] = index;
                    break;
                }
            } else {
                break;
            }
        }
        vars[i].updateLowerBound(lb, this);
        for (int v = ub; v >= lb; v--) {
            int index = residues[i][v - offset[i]];
            if ((currTable.words[index].get() & supports[i].get(v - offset[i])[index]) == 0L) {
                index = currTable.intersectIndex(supports[i].get(v - offset[i]));
                if (index == -1) {
                    ub--;
                } else {
                    residues[i][v - offset[i]] = index;
                    break;
                }
            } else {
                break;
            }
        }
        vars[i].updateUpperBound(ub, this);
    }

    private void enumFilter(int i) throws ContradictionException {
        int ub = vars[i].getUB();
        for (int v = vars[i].getLB(); v <= ub; v = vars[i].nextValue(v)) {
            int index = residues[i][v - offset[i]];
            if ((currTable.words[index].get() & supports[i].get(v - offset[i])[index]) == 0L) {
                index = currTable.intersectIndex(supports[i].get(v - offset[i]));
                if (index == -1) {
                    vars[i].removeValue(v, this);
                } else {
                    residues[i][v - offset[i]] = index;
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        // TODO optim : check current according to currTable?
        return tuples.check(vars);
    }

//***********************************************************************************
// RSparseBitSet
//***********************************************************************************

    /**
     * A reversible sparse bitset.
     *
     * @implNote An equivalent implementation of this class but relying on IStateBitSet was evaluated
     * but the bottleneck was the calls to nextSetBit and nextClearBit which are called a lot in
     * the #clearMask and #intersectWithMask methods.
     */

    protected static class RSparseBitSet {
        protected IStateLong[] words;
        private final int[] index;
        private final IStateInt limit;
        private final long[] mask;

        protected RSparseBitSet(IEnvironment environment, int nbBits) {
            int nw = nbBits / 64;
            if (nw * 64 < nbBits) nw++;
            index = new int[nw];
            mask = new long[nw];
            limit = environment.makeInt(nw - 1);
            words = new IStateLong[nw];
            for (int i = 0; i < nw; i++) {
                index[i] = i;
                words[i] = environment.makeLong(-1L);
            }
        }

        protected boolean isEmpty() {
            return limit.get() == -1;
        }

        protected void clearMask() {
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                mask[offset] = 0L;
            }
        }

        protected void reverseMask() {
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                mask[offset] = ~mask[offset];
            }
        }

        protected void addToMask(long[] wordsToAdd) {
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                mask[offset] = mask[offset] | wordsToAdd[offset];
            }
        }

        protected void intersectWithMask() {
            int l = this.limit.get();
            for (int i = l; i >= 0; i--) {
                int offset = index[i];
                long wo = words[offset].get();
                long w = wo & mask[offset];
                if (wo != w) {
                    words[offset].set(w);
                    if (w == 0L) {
                        index[i] = index[l];
                        index[l] = offset;
                        l--;
                    }
                }
            }
            limit.set(l);
        }

        protected void intersectWithNotMask() {
            int l = this.limit.get();
            for (int i = l; i >= 0; i--) {
                int offset = index[i];
                long wo = words[offset].get();
                long w = wo & ~mask[offset];
                if (wo != w) {
                    words[offset].set(w);
                    if (w == 0L) {
                        index[i] = index[l];
                        index[l] = offset;
                        l--;
                    }
                }
            }
            this.limit.set(l);
        }

        protected int intersectIndex(long[] m) {
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                if ((words[offset].get() & m[offset]) != 0L) {
                    return offset;
                }
            }
            return -1;
        }

        protected int nb1s() {
            int cnt = 0;
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                cnt += Long.bitCount(words[offset].get());
            }
            return cnt;
        }

        protected long nb1s(long[] m) {
            long cnt = 0;
            for (int i = limit.get(); i >= 0; i--) {
                int offset = index[i];
                cnt += Long.bitCount(words[offset].get() & m[offset]);
            }
            return cnt;
        }
    }
}