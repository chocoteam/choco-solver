/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/04/2014
 */
class CouplesBitSetTable extends BinRelation {

    /**
     * table[0][i] gives the supports of value i of variable 0
     * table[1][i] gives the supports of value i of variable 1
     */
    private final BitSet[][] table;

    /**
     * first value of x, and y
     */
    private final int[] offsets;

    /**
     * Create a tuple list for AC3bit+rm
     *
     * @param tuples list of tuples
     */
    public CouplesBitSetTable(Tuples tuples, IntVar var1, IntVar var2) {
        offsets = new int[]{var1.getLB(), var2.getLB()};
        int range1 = var1.getUB() - offsets[0] + 1;
        int range2 = var2.getUB() - offsets[1] + 1;
        this.table = new BitSet[2][];
        this.table[0] = new BitSet[range1];
        boolean feasible = tuples.isFeasible();
        for (int i = 0; i < range1; i++) {
            table[0][i] = new BitSet(range2);
            if (!feasible) table[0][i].set(0, range2);
        }
        this.table[1] = new BitSet[range2];
        for (int i = 0; i < range2; i++) {
            table[1][i] = new BitSet(range1);
            if (!feasible) table[1][i].set(0, range1);
        }

        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
//            setCouple(tuple[0], tuple[1]);
            if (var1.contains(tuple[0]) && var2.contains(tuple[1])) {
                if (feasible) {
                    table[0][tuple[0] - offsets[0]].set(tuple[1] - offsets[1]);
                    table[1][tuple[1] - offsets[1]].set(tuple[0] - offsets[0]);
                } else {
                    table[0][tuple[0] - offsets[0]].clear(tuple[1] - offsets[1]);
                    table[1][tuple[1] - offsets[1]].clear(tuple[0] - offsets[0]);
                }
            }
        }
    }

    public boolean isConsistent(int x, int y) {
        return table[0][x - offsets[0]].get(y - offsets[1]);
    }

    public boolean checkCouple(int x, int y) {
        return table[0][x - offsets[0]].get(y - offsets[1]);
    }

    private int lastVar;
    private IntVar lastV;
    private int lastLB;
    private int lastUB;
    private BitSet[] lastTable;
    private int lastOffset;
    private int lastO;
    private int[] lastIndex;
    private int indexOffset;

    /**
     * Optimized check that reuses cached data to determine if `val` is unsupported.
     */
    private boolean checkUnsupportedValueOptimized(int val) {
        BitSet tableForVal = lastTable[val - lastOffset];
        int i = lastLB;

        while (i <= lastUB) {
            if (tableForVal.get(i - lastO)) {
                return false;
            }
            int idx = i + indexOffset;
            if (lastIndex[idx] == -1) {
                i = lastV.nextValue(i);
                lastIndex[idx] = i + indexOffset;
            } else {
                i = lastIndex[idx] - indexOffset;
            }
        }

        return true;
    }

	/**
     * checks if var=val has no support within the domain of v
     * @param var a variable index
     * @param val a value for var
     * @param v a variable
     * @return true iff there exists no support for v where var = val
     * 
     * 
     * Uses caching to speed up repeated queries on the same variable.
     */
    public boolean checkUnsupportedValue(int var, int val, IntVar v) {
        if (var == lastVar && v == lastV) {
            return checkUnsupportedValueOptimized(val);
        } else {
            // Cache current state
            lastVar = var;
            lastV = v;
            lastLB = v.getLB();
            lastUB = v.getUB();
            lastTable = table[var];
            lastOffset = offsets[var];
            lastO = offsets[1 - var];

            BitSet tableForVal = lastTable[val - lastOffset];
            indexOffset = -lastLB;
            int indexSize = lastUB - lastLB + 1;
            // Ensure lastIndex is large enough
            if (lastIndex == null || lastIndex.length < indexSize) {
                lastIndex = new int[indexSize];
            }
            Arrays.fill(lastIndex, -1);

            // Perform full scan
            for (int i = lastLB; i <= lastUB; i = v.nextValue(i)) {
                if (tableForVal.get(i - lastO)) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public Tuples convert() {
        Tuples tuples = new Tuples(true);
        for(int i = 0; i < table[0].length; i++) {
            for (int b = table[0][i].nextSetBit(0); b > -1; b = table[0][i].nextSetBit(b + 1)) {
                tuples.add(i + offsets[0], b + offsets[1]);
            }
        }
        return tuples;
    }
}
