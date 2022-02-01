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

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class TuplesTable extends LargeRelation {

    /**
     * the number of dimensions of the considered tuples
     */
    private final int n;
    /**
     * The consistency matrix
     */
    private final BitSet table;

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
     * in order to speed up the computation of the index of a tuple
     * in the table, blocks[i] stores the product of the size of variables j with j < i.
     */
    private final int[] blocks;

    public TuplesTable(Tuples tuples, IntVar[] vars) {
        n = vars.length;
        lowerbounds = new int[n];
        upperbounds = new int[n];
        feasible = tuples.isFeasible();

        int totalSize = 1;
        blocks = new int[n];
        for (int i = 0; i < n; i++) {
            blocks[i] = totalSize;
            lowerbounds[i] = vars[i].getLB();
            upperbounds[i] = vars[i].getUB();
            totalSize *= upperbounds[i] - lowerbounds[i] + 1;
        }
        if (totalSize < 0 || (totalSize / 8 > 50 * 1024 * 1024)) {
            throw new SolverException("Tuples required over 50Mo of memory...");
        }
        table = new BitSet(totalSize);
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, vars)) {
                setTuple(tuple);
            }
        }
    }

    public boolean checkTuple(int[] tuple) {
        int address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            if ((tuple[i] < lowerbounds[i]) || (tuple[i] > upperbounds[i])) {
                return false;
            }
            address += (tuple[i] - lowerbounds[i]) * blocks[i];
        }
        return table.get(address);
    }

    public boolean isConsistent(int[] tuple) {
        return checkTuple(tuple) == feasible;
    }

    private void setTuple(int[] tuple) {
        int address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            address += (tuple[i] - lowerbounds[i]) * blocks[i];
        }
        table.set(address);
    }

    @Override
    public Tuples convert() {
        Tuples tuples = new Tuples(feasible);
        int[] tt = new int[upperbounds.length];
        for(int add = table.nextSetBit(0); add > -1; add = table.nextSetBit(add + 1)){
            int address = add;
            for (int i = (n - 1); i >= 0; i--) {
                int t = address / blocks[i];
                tt[i] = t + lowerbounds[i];
                address -= (t * blocks[i]);
            }
            tuples.add(tt);
        }
        return tuples;
    }
}
