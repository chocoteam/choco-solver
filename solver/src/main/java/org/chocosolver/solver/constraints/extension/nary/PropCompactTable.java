/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

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

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    RSparseBitSet currTable;
    protected Tuples tuples; // only for checker
    protected long[][][] supports;
    int[][] residues;
    protected int[] offset;
    protected IIntDeltaMonitor[] monitors;
    protected final UnaryIntProcedure<Integer> onValRem;
    protected final boolean uniqueness;

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
        super(vars, PropagatorPriority.QUADRATIC, true, true);
        this.tuples = tuples;
        this.currTable = new RSparseBitSet(model.getEnvironment(), this.tuples.nbTuples());
        computeSupports(tuples);
        monitors = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            monitors[i] = vars[i].monitorDelta(this);
        }
        onValRem = makeProcedure();
        uniqueness = uniqueness(vars);
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
                currTable.addToMask((supports[var][i - off]));
            }
        };
    }

    protected void computeSupports(Tuples tuples) {
        int n = vars.length;
        offset = new int[n];
        supports = new long[n][][];
        residues = new int[n][];
        long[] tmp;
        for (int i = 0; i < n; i++) {
            int lb = vars[i].getLB();
            int ub = vars[i].getUB();
            offset[i] = lb;
            supports[i] = new long[ub - lb + 1][currTable.words.length];
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
                tmp = supports[i][tuple[i] - offset[i]];
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
                    currTable.addToMask(supports[i][v - offset[i]]);
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
            currTable.addToMask(supports[vIdx][vars[vIdx].getValue() - offset[vIdx]]);
        } else if (vars[vIdx].getDomainSize() > monitors[vIdx].sizeApproximation()) {
            monitors[vIdx].forEachRemVal(onValRem.set(vIdx));
            currTable.reverseMask();
        } else {
            int ub = vars[vIdx].getUB();
            for (int v = vars[vIdx].getLB(); v <= ub; v = vars[vIdx].nextValue(v)) {
                currTable.addToMask(supports[vIdx][v - offset[vIdx]]);
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
        if (count == 0) { // Invariant 3.4
            fails();
        } else {
            long initthreshold = VariableUtils.domainCardinality(vars);
            if (uniqueness && count == initthreshold) { // Invariant 3.3
                // --> does not work with views
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
            if ((currTable.words[index].get() & supports[i][v - offset[i]][index]) == 0L) {
                index = currTable.intersectIndex(supports[i][v - offset[i]]);
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
            if ((currTable.words[index].get() & supports[i][v - offset[i]][index]) == 0L) {
                index = currTable.intersectIndex(supports[i][v - offset[i]]);
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
            if ((currTable.words[index].get() & supports[i][v - offset[i]][index]) == 0L) {
                index = currTable.intersectIndex(supports[i][v - offset[i]]);
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
                long w = words[offset].get() & ~mask[offset];
                if (words[offset].get() != w) {
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

        protected int intersectIndex(long[] m) {
            int i = this.limit.get();
            while (i >= 0) {
                int offset = index[i];
                if ((words[offset].get() & m[offset]) != 0L) {
                    return offset;
                }
                i--;
            }
            return -1;
        }

        protected int nb1s() {
            int cnt = 0;
            int i = this.limit.get();
            while (i >= 0) {
                int offset = index[i];
                cnt += Long.bitCount(words[offset].get());
                i--;
            }
            return cnt;
        }

        protected long nb1s(long[] m) {
            long cnt = 0;
            int i = limit.get();
            while (i >= 0) {
                int offset = index[i];
                cnt += Long.bitCount(words[offset].get() & m[offset]);
                i--;
            }
            return cnt;
        }
    }
}