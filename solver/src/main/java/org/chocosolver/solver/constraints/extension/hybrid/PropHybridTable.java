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

import java.util.ArrayList;
import java.util.BitSet;

/**
 * This class implements an adaptation of the filtering algorithm from:
 * "Extending Compact-Table to Basic Smart Tables",
 * H. Verhaeghe and C. Lecoutre and Y. Deville and P. Schauss, CP-17.
 * to STR2. It deals with smart/hybrid tuples.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/02/2023
 */
public class PropHybridTable extends Propagator<IntVar> {

    private final HReExpression[][] table;
    private final StrHVar[] str2vars;
    private final ISet activeTuples;
    private final ArrayList<StrHVar> ssup;
    private final ArrayList<StrHVar> sval;
    private boolean firstProp = true;

    public PropHybridTable(IntVar[] vars, HybridTuples tuples) {
        super(vars, PropagatorPriority.QUADRATIC, false);
        this.table = tuples.toArray();
        int size = 0;
        if (table.length > 0) {
            size = table[0].length;
        }
        str2vars = new StrHVar[size];
        for (int i = 0; i < size; i++) {
            str2vars[i] = new StrHVar(model.getEnvironment(), vars[i], i);
        }
        activeTuples = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        ssup = new ArrayList<>();
        sval = new ArrayList<>();
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

    StrHVar getSVar(int a) {
        return str2vars[a];
    }

    //***********************************************************************************
    // DEDICATED METHODS
    //***********************************************************************************

    private boolean isTupleSupported(int tuple_index) {
        for (int i = 0; i < sval.size(); i++) {
            StrHVar v = sval.get(i);
            if (!table[tuple_index][v.index].canBeSatisfied(this, v.index)) {
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
            ssup.add(tmp);
            tmp.reset();
            if (tmp.last_size.get() != tmp.cnt) {
                sval.add(tmp);
                tmp.last_size.set(tmp.cnt);
            }
        }
        for (int tidx : activeTuples.toArray()) { //TODO REMOVE
            if (isTupleSupported(tidx)) {
                for (int var = 0; var < ssup.size(); var++) {
                    StrHVar v = ssup.get(var);
                    HReExpression exp = table[tidx][v.index];
                    exp.supportFor(this, v.index);
                    if (v.cnt == 0) {
                        ssup.set(var, ssup.get(ssup.size() - 1));
                        ssup.remove(ssup.size() - 1);
                        var--;
                    }
                }
            } else {
                activeTuples.remove(tidx);
            }
        }
        for (int i = 0; i < ssup.size(); i++) {
            ssup.get(i).removeUnsupportedValue(this);
        }
    }

    /**
     * var class which will save local var information
     */
    static class StrHVar {
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

        /**
         * contains all the value of the variable
         */

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

        private void removeUnsupportedValue(ICause cause) throws ContradictionException {
            if (var.hasEnumeratedDomain()) {
                for (int val = var.getLB(); cnt > 0 && val <= var.getUB(); val = var.nextValue(val)) {
                    if (!ac.get(val - offset)) {
                        var.removeValue(val, cause);
                        cnt--;
                    }
                }
            } else {
                int val = var.getLB();
                while (cnt > 0 && val <= var.getUB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            cnt--;
                        } else break;
                    }
                    val = var.nextValue(val);
                }
                val = var.getUB();
                while (cnt > 0 && val >= var.getLB()) {
                    if (!ac.get(val - offset)) {
                        if (var.removeValue(val, cause)) {
                            cnt--;
                        } else break;
                    }
                    val = var.previousValue(val);
                }
            }

        }

        /**
         * Set support for <i>value</i>
         *
         * @param value a value
         */
        public void supportFor(int value) {
            value -= offset;
            if (!ac.get(value)) {
                ac.set(value);
                cnt--;
            }
        }

        /**
         * Set support for all values.
         */
        public void supportForAll() {
            cnt = 0;
        }
    }
}
