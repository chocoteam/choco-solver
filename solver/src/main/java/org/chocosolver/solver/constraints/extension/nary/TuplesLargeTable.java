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

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class TuplesLargeTable extends LargeRelation {

    /**
     * the number of dimensions of the considered tuples
     */
    private final int n;

    /**
     * The consistency matrix
     */
    private final TIntObjectHashMap<TIntSet> tables;

    /**
     * lower bound of each variable
     */
    private final int[] lowerbounds;

    /**
     * upper bound of each variable
     */
    private final int[] upperbounds;

    private final boolean feasible;

    /**
     * in order to speed up the computation of the index of a tuple in the table, blocks[i] stores
     * the product of the size of variables j with j < i.
     */
    private final long[] blocks;

    public TuplesLargeTable(Tuples tuples, IntVar[] vars) {
        n = vars.length;
        lowerbounds = new int[n];
        upperbounds = new int[n];
        feasible = tuples.isFeasible();

        blocks = new long[n];
        long totalSize = 1;
        for (int i = 0; i < n; i++) {
            blocks[i] = totalSize;
            lowerbounds[i] = vars[i].getLB();
            upperbounds[i] = vars[i].getUB();
            totalSize *= upperbounds[i] - lowerbounds[i] + 1;
            if (totalSize < 0) { // to prevent from integer overflow
                totalSize = -1;
            }
        }
        if ((totalSize / Integer.MAX_VALUE) + 1 < 0 || (totalSize / Integer.MAX_VALUE) + 1 > Integer.MAX_VALUE)
            throw new SolverException("Tuples required too much memory ...");

        tables = new TIntObjectHashMap<>();
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, vars)) {
                setTuple(tuple);
            }
        }
    }

    public boolean checkTuple(int[] tuple) {
        long address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            if ((tuple[i] < lowerbounds[i]) || (tuple[i] > upperbounds[i])) {
                return false;
            }
            address += (tuple[i] - lowerbounds[i]) * blocks[i];
        }
        int a = (int) (address % Integer.MAX_VALUE);
        int t = (int) (address / Integer.MAX_VALUE);
        TIntSet ts = tables.get(t);
        return ts != null && ts.contains(a);
    }

    public boolean isConsistent(int[] tuple) {
        return checkTuple(tuple) == feasible;
    }

    private void setTuple(int[] tuple) {
        long address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            address += (tuple[i] - lowerbounds[i]) * blocks[i];
        }
        int a = (int) (address % Integer.MAX_VALUE);
        int t = (int) (address / Integer.MAX_VALUE);
        TIntSet ts = tables.get(t);
        if (ts == null) {
            ts = new TIntHashSet();
            tables.put(t, ts);
        }
        ts.add(a);
    }

    @Override
    public Tuples convert() {
        Tuples tuples = new Tuples(feasible);
        int[] tt = new int[lowerbounds.length];
        for (TIntSet set : tables.valueCollection()) {
            for (int add : set.toArray()) {
                for (int i = (n - 1); i >= 0; i--) {
                    long t = add / blocks[i];
                    tt[i] = (int) (t + lowerbounds[i]);
                    add -= t;
                }
            }
            tuples.add(tt);
        }
        return tuples;
    }
}
