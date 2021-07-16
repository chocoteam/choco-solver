/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;

class CouplesTable extends BinRelation {

    /**
     * matrix of consistency/inconsistency
     */
    private final BitSet table;

    /**
     * first value of x, and y
     */
    private final int offset1, offset2;


    private final int range2;

    private final boolean feasible;

    public CouplesTable(Tuples tuples, IntVar var1, IntVar var2) {
        offset1 = var1.getLB();
        offset2 = var2.getLB();
        int range1 = var1.getUB() - offset1 + 1;
        range2 = var2.getUB() - offset2 + 1;
        table = new BitSet(range1 * range2);
        feasible = tuples.isFeasible();
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            if (var1.contains(tuple[0]) && var2.contains(tuple[1]))
                table.set((tuple[0] - offset1) * range2 + tuple[1] - offset2);
        }
    }

    public boolean isConsistent(int x, int y) {
        return table.get((x - offset1) * range2 + y - offset2) == feasible;
    }

    public boolean checkCouple(int x, int y) {
        return table.get((x - offset1) * range2 + y - offset2);
    }

    @Override
    public Tuples convert() {
        Tuples tuples = new Tuples(feasible);
        for (int b = table.nextSetBit(0); b > -1; b = table.nextSetBit(b + 1)) {
            int v1 = (b / range2) + offset1;
            int v2 = (b % range2) + offset2;
            tuples.add(v1, v2);
        }
        return tuples;
    }
}
