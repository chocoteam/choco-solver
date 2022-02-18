/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * STR2 Propagator for table constraints (only positive tuples)
 *
 * @author Guillaume Perez, Jean-Guillaume Fages (minor)
 * @since 26/07/2014
 */
public class PropTableStr2 extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int[][] table;
    private final Str2_var[] str2vars;
    private final ISet tuples;
    private final ArrayList<Str2_var> ssup;
    private final ArrayList<Str2_var> sval;
    private boolean firstProp = true;
    private final Tuples tuplesObject;
    private final int star;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropTableStr2(IntVar[] vars_, Tuples tuplesObject) {
        super(vars_, PropagatorPriority.LINEAR, false);
        this.table = tuplesObject.toMatrix();
        this.tuplesObject = tuplesObject;

        int size = 0;
        if (table.length > 0) {
            size = table[0].length;
        }
        str2vars = new Str2_var[size];
        int max = 0;
        for (int i = 0; i < size; i++) {
            str2vars[i] = new Str2_var(model.getEnvironment(), vars_[i], i);
            max = Math.max(max, vars_[i].getUB());
        }
        this.star = tuplesObject.allowUniversalValue() ? tuplesObject.getStarValue() : max + 1;
        tuples = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
        ssup = new ArrayList<>();
        sval = new ArrayList<>();
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (firstProp) {
            firstProp = false;
            model.getEnvironment().save(() -> firstProp = true);
            initialPropagate();
        }
        Filter();
    }

    @Override
    public ESat isEntailed() {
        if (firstProp) { // data structure not ready
            return tuplesObject.check(vars);
        } else {
            boolean hasSupport = false;
            for (int tuple : tuples) {
                if (is_tuple_supported(tuple)) {
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
    }

    @Override
    public String toString() {
        return "STR2 table constraint with " + vars.length + "vars and " + table.length + "tuples";
    }

    //***********************************************************************************
    // DEDICATED METHODS
    //***********************************************************************************

    private boolean is_tuple_supported(int tuple_index) {
        for (int i = 0; i < sval.size(); i++) {
            Str2_var v  = sval.get(i);
            if (table[tuple_index][v.index] != star &&
                    !v.var.contains(table[tuple_index][v.index])) {
                return false;
            }
        }
        return true;
    }

    private void initialPropagate() throws ContradictionException {
        for (int t = 0; t < table.length; t++) {
            tuples.add(t);
        }
        if (tuples.isEmpty()) {
            this.fails();
        }
    }

    private void Filter() throws ContradictionException {
        ssup.clear();
        sval.clear();
        for (int i = 0; i < str2vars.length; i++) {
            Str2_var tmp = str2vars[i];
            ssup.add(tmp);
            tmp.reset();
            if (tmp.last_size.get() != tmp.cnt) {
                sval.add(tmp);
                tmp.last_size.set(tmp.cnt);
            }
        }
        for (int tuple : tuples) {
            if (is_tuple_supported(tuple)) {
                for (int var = 0; var < ssup.size(); var++) {
                    Str2_var v = ssup.get(var);
                    int a = table[tuple][v.index];
                    if (a == star) {
                        v.cnt = 0;
                        ssup.set(var, ssup.get(ssup.size() - 1));
                        ssup.remove(ssup.size() - 1);
                        var--;
                    } else if (!v.ac.get(a - v.offset)) {
                        v.ac.set(a - v.offset);
                        if (--v.cnt == 0) {
                            ssup.set(var, ssup.get(ssup.size() - 1));
                            ssup.remove(ssup.size() - 1);
                            var--;
                        }
                    }
                }
            } else {
                tuples.remove(tuple);
            }
        }
        for (int i = 0; i < ssup.size(); i++) {
            ssup.get(i).remove_unsupported_value(this);
        }
    }

    /**
     * var class which will save local var information
     */
    private static class Str2_var {
        /**
         * original var
         */
        private final IntVar var;
        /**
         * index in the table
         */
        private final int index;

        private final IStateInt last_size;
        /**
         * Store consistent values
         */
        private final BitSet ac;
        /**
         * Current offset
         */
        private int offset;
        /**
         * Count the number of value to remove
         */
        private int cnt;

        /**
         * contains all the value of the variable
         */

        private Str2_var(IEnvironment env, IntVar var_, int index_) {
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

        private void remove_unsupported_value(ICause cause) throws ContradictionException {
            for (int val = var.getLB(); cnt > 0 && val <= var.getUB(); val = var.nextValue(val)) {
                if (!ac.get(val - offset)) {
                    var.removeValue(val, cause);
                    cnt--;
                }
            }
        }
    }
}
