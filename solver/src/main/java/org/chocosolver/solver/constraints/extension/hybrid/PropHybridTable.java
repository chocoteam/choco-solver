/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.hybrid;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * This class implements an adaptation of the filtering algorithm from:
 * "Extending Compact-Table to Basic Smart Tables",
 * H. Verhaeghe and C. Lecoutre and Y. Deville and P. Schauss, CP-17.
 * to STR2. It deals with smart/hybrid tuples.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/03/2023
 */
public class PropHybridTable extends Propagator<IntVar> {

    private final HybridTuples.ISupportable[][] table;
    private final StrHVar[] str2vars;
    private final ISet activeTuples;
    private final BitSet ssup;
    private final BitSet sval;
    private boolean firstProp = true;

    public PropHybridTable(IntVar[] vars, HybridTuples tuples) {
        super(vars, PropagatorPriority.QUADRATIC, false);
        this.table = tuples.toArray();
        int nbVars = vars.length;
        str2vars = new StrHVar[nbVars];
        for (int i = 0; i < nbVars; i++) {
            str2vars[i] = new StrHVar(model.getEnvironment(), vars[i], i);
        }
        activeTuples = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        ssup = new BitSet(vars.length);
        sval = new BitSet(vars.length);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (firstProp) {
            firstProp = false;
            model.getEnvironment().save(() -> firstProp = true);
            initialPropagate();
        }
        filter();
    }

    @Override
    public ESat isEntailed() {
        boolean hasSupport = false;
        for (int i = 0; i < table.length && !hasSupport; i++) {
            if (isTupleSupported(i)) {
                hasSupport = true;
            }
        }
        if (hasSupport) {
            if (isCompletelyInstantiated()) {
                return ESat.TRUE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.FALSE;
        }
    }

    @Override
    public String toString() {
        return "STR2 hybrid table constraint with " + vars.length + " vars and " + table.length + " tuples";
    }

    //***********************************************************************************
    // DEDICATED METHODS
    //***********************************************************************************

    private boolean isTupleSupported(int tuple_index) {
        for (int i = sval.nextSetBit(0); i > -1; i = sval.nextSetBit(i + 1)) {
            StrHVar v = str2vars[i];
            if (!table[tuple_index][v.index].satisfiable(str2vars, v.index)) {
                return false;
            }
        }
        return true;
    }

    private void initialPropagate() throws ContradictionException {
        for (int t = 0; t < table.length; t++) {
            activeTuples.add(t);
        }
        if (activeTuples.isEmpty()) {
            this.fails();
        }
    }

    private void filter() throws ContradictionException {
        ssup.clear();
        sval.clear();
        for (int i = 0; i < str2vars.length; i++) {
            StrHVar tmp = str2vars[i];
            ssup.set(i);
            tmp.reset();
            if (tmp.last_size.get() != tmp.cnt) {
                // to get variables modified since last call
                sval.set(i);
                tmp.last_size.set(tmp.cnt);
            }
        }
        boolean loop;
        do {
            loop = false;
            for (int tidx : activeTuples/*.toArray()*/) {
                if (isTupleSupported(tidx)) {
                    for (int i = ssup.nextSetBit(0); i > -1; i = ssup.nextSetBit(i + 1)) {
                        HybridTuples.ISupportable exp = table[tidx][i];
                        exp.support(str2vars, i);
                        if (str2vars[i].cnt == 0) {
                            ssup.clear(i);
                        }
                    }
                } else {
                    activeTuples.remove(tidx);
                }
            }
            sval.clear();
            for (int i = 0; i < str2vars.length; i++) {
                if (str2vars[i].cnt > 0) {
                    if (str2vars[i].removeUnsupportedValue(this)) {
                        loop = true;
                        sval.set(i);
                        str2vars[i].last_size.set(str2vars[i].cnt);
                    }
                }
                ssup.set(i);
            }
        } while (loop);
    }

    /**
     * Class that maintains, for a variable, the supported values
     */
    public static class StrHVar {
        /**
         * original var
         */
        final IntVar var;
        /**
         * index in the table
         */
        private final int index;

        private final IStateInt last_size;
        /**
         * Store consistent values
         */
        final BitSet ac;
        /**
         * Current offset
         */
        private int offset;
        /**
         * Count the number of value to remove
         */
        int cnt;

        StrHVar(IEnvironment env, IntVar var_, int index_) {
            var = var_;
            last_size = env.makeInt(0);
            index = index_;
            ac = new BitSet();
        }

        private void reset() {
            ac.clear();
            offset = var.getLB();
            cnt = var.getDomainSize();
        }

        private boolean removeUnsupportedValue(ICause cause) throws ContradictionException {
            boolean filter = false;
            if (var.hasEnumeratedDomain()) {
                for (int val = var.getLB(); cnt > 0 && val <= var.getUB(); val = var.nextValue(val)) {
                    if (!ac.get(val - offset)) {
                        filter |= var.removeValue(val, cause);
                        cnt--;
                    }
                }
            } else {
                int val = var.getLB();
                while (cnt > 0 && val <= var.getUB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            filter = true;
                            cnt--;
                        } else break;
                    }
                    val = var.nextValue(val);
                }
                val = var.getUB();
                while (cnt > 0 && val >= var.getLB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            filter = true;
                            cnt--;
                        } else break;
                    }
                    val = var.previousValue(val);
                }
            }
            return filter;
        }

        /**
         * Set support for <i>value</i>
         *
         * @param value a value
         */
        public void support(int value) {
            value -= offset;
            if (!ac.get(value)) {
                ac.set(value);
                cnt--;
            }
        }

        /**
         * Set support for all values.
         */
        public void supportAll() {
            cnt = 0;
        }

        @Override
        public String toString() {
            return var.getName();
        }
    }
}
