/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.procedure.UnaryIntProcedure;

/**
 * Propagator for table constraint based on
 * "Extending Compact-Table to Negative and Short Tables",
 * H. Verhaeghe and C. Lecoutre and P. Schauss, AAAI-17.
 * It deals with short tuples.
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume FAGES
 * @since 16/05/2017
 */
public class PropCompactTableStar extends PropCompactTable {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private long[][][] inc_supports;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create a propagator for table constraint
     * Only for feasible Tuples
     * @param vars   scope
     * @param tuples list of feasible tuples
     */
    public PropCompactTableStar(IntVar[] vars, Tuples tuples) {
        super(vars, tuples);
        assert tuples.allowUniversalValue();
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    protected UnaryIntProcedure<Integer> makeProcedure() {
        return new UnaryIntProcedure<Integer>() {
            int var, off;

            @Override
            public UnaryIntProcedure set(Integer o) {
                var = o;
                off = offset[var];
                return this;
            }

            @Override
            public void execute(int i) throws ContradictionException {
                // main reason we re-wrote the class
                currTable.addToMask((inc_supports[var][i - off]));
            }
        };
    }

    protected void computeSupports(Tuples tuples) {
        int n = vars.length;
        offset = new int[n];
        supports = new long[n][][];
        inc_supports = new long[n][][];
        residues = new int[n][];
        for (int i = 0; i < n; i++) {
            int lb = vars[i].getLB();
            int ub = vars[i].getUB();
            offset[i] = lb;
            supports[i] = new long[ub - lb + 1][currTable.words.length];
            inc_supports[i] = new long[ub - lb + 1][currTable.words.length];
            residues[i] = new int[ub - lb + 1];
        }
        int wI = 0;
        byte bI = 63;
        int star = tuples.getStarValue();
        top:
        for (int ti = 0; ti < tuples.nbTuples(); ti++) {
            int[] tuple = tuples.get(ti);
            for (int i = 0; i < tuple.length; i++) {
                if (!vars[i].contains(tuple[i]) && tuple[i] != star) {
                    continue top;
                }
            }
            long index = 1L << bI;
            for (int i = 0; i < tuple.length; i++) {
                int val = tuple[i];
                if (val != star) {
                    supports[i][val - offset[i]][wI] |= index;
                    inc_supports[i][val - offset[i]][wI] |= index;
                } else {
                    int u = supports[i].length + offset[i];
                    for (val = offset[i]; val <= u; val = vars[i].nextValue(val)) {
                        supports[i][val - offset[i]][wI] |= index;
                    }
                }
            }
            if (--bI < 0) {
                bI = 63;
                wI++;
            }
        }
    }
}