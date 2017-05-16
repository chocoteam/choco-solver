/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
 * @since 16/05/2017
 */
public class PropCompactTableStar extends PropCompactTable {

    //***********************************************************************************
   	// VARIABLES
   	//***********************************************************************************

    private int star;
    private long[][][] inc_supports;

    //***********************************************************************************
   	// CONSTRUCTOR
   	//***********************************************************************************

	/**
     * Create a propagator for table constraint
     * Only for feasible Tuples
	 * @param vars scope
     * @param tuples list of feasible tuples
     */
    public PropCompactTableStar(IntVar[] vars, Tuples tuples) {
        super(vars, tuples);

    }

    //***********************************************************************************
   	// INITIALIZATION
   	//***********************************************************************************

    protected UnaryIntProcedure<Integer> makeProcedure(){
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

    protected void copyValidTuples(Tuples tuples) {
        // main reason we re-wrote the class
        this.star = tuples.getStarValue();
        this.tuples = new Tuples(tuples.isFeasible());
        this.tuples.setUniversalValue(this.star);
        for (int ti = 0; ti < tuples.nbTuples(); ti++) {
            int[] tuple = tuples.get(ti);
            boolean valid = true;
            for (int i = 0; i < vars.length && valid; i++) {
                // main reason we re-wrote the class
                valid = vars[i].contains(tuple[i]) || tuple[i] == star;
            }
            if (valid) {
                this.tuples.add(tuple);
            }
        }
        currTable = new RSparseBitSet(model.getEnvironment(), this.tuples.nbTuples());
    }

    protected void computeSupports() {
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
            for (int v=lb ; v<=ub; v=vars[i].nextValue(v)) {
                long[] tmp = supports[i][v - lb];
                long[] inc_tmp = inc_supports[i][v - lb];
                int wI = 0;
                int bI = 63;
                for (int ti = 0; ti < tuples.nbTuples(); ti++) {
                    int val = tuples.get(ti)[i];
                    long index = 1L << bI;
                    if (val == v) {
                        // main reason we re-wrote the class
                        inc_tmp[wI] |= index;
                        tmp[wI] |= index;
                    }else if(val == star){
                        // main reason we re-wrote the class
                        tmp[wI] |= index;
                    }
                    bI--;
                    if (bI < 0) {
                        bI = 63;
                        wI++;
                    }
                }
            }
        }
    }
}