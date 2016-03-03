/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

/**
 *
 **/
class IterTuplesTable extends TuplesList {

    /**
     * number of variables
     */
    protected final int nbVar;
    /**
     * lower bound of each variable
     */
    protected final int[] lowerbounds;

    /**
     * upper bound of each variable
     */
    protected final int[] ranges;

    /**
     * table[i][j] gives the table of supports as an int[] for value j of variable i
     */
    protected int[][][] table;

    public IterTuplesTable(Tuples tuples, IntVar[] vars) {
        super(tuples, vars);
        nbVar = vars.length;
        lowerbounds = new int[nbVar];
        ranges = new int[nbVar];
        for (int i = 0; i < nbVar; i++) {
            lowerbounds[i] = vars[i].getLB();
            ranges[i] = vars[i].getUB() - lowerbounds[i] + 1;
        }

        table = new int[nbVar][][];
        for (int i = 0; i < nbVar; i++) {
            table[i] = new int[ranges[i]][];
            int[] nbsups = getNbSupportFor(i);
            for (int j = 0; j < nbsups.length; j++) {
                table[i][j] = new int[nbsups[j]];
            }
        }
        buildInitialListOfSupports();
    }

    // required for duplicate method, should not be called by default
    private IterTuplesTable(int[][] tuplesIndexes, int n, int[] lowerbounds, int[] ranges, int[][][] table) {
        super(tuplesIndexes);
        this.nbVar = n;
        this.lowerbounds = lowerbounds;
        this.ranges = ranges;
        this.table = table;
    }

    /**
     * return the number of tuples supporting each value of variable i
     *
     * @param i a variable
     * @return nb supports
     */
    public int[] getNbSupportFor(int i) {
        int[] nbsup = new int[ranges[i]];
        int nt = tuplesIndexes.length;
        for (int j = 0; j < nt; j++) {
            int[] tuple = tuplesIndexes[j];
            nbsup[tuple[i] - lowerbounds[i]]++;
        }
        return nbsup;
    }

    public void buildInitialListOfSupports() {
        int cpt = 0;
        int[][] level = new int[nbVar][];
        for (int i = 0; i < nbVar; i++) {
            level[i] = new int[ranges[i]];
        }
        int nt = tuplesIndexes.length;
        for (int j = 0; j < nt; j++) {
            int[] tuple = tuplesIndexes[j];
            for (int i = 0; i < tuple.length; i++) {
                int value = tuple[i] - lowerbounds[i];
                table[i][value][level[i][value]] = cpt;
                level[i][value]++;
            }
            cpt++;
        }
    }

    /**
     * for fast access
     *
     * @return table
     */
    public int[][][] getTableLists() {
        return table;
    }

    /**
     * This relation do not take advantage of the knowledge of the
     * previous support ! so start from scratch
     *
     * @param val is the value assuming the offset has already been
     *            removed
     */
    public int seekNextTuple(int oldidx, int var, int val) {
        int nidx = oldidx + 1;
        if (nidx < table[var][val].length) {
            return table[var][val][nidx];
        } else {
            return -1;
        }
    }

    /**
     * return the number of supports of the pair (var, val) assuming the
     * offset has already been removed
     */
    public int getNbSupport(int var, int val) {
        return table[var][val].length;
    }

    public int getRelationOffset(int var) {
        return lowerbounds[var];
    }

    public boolean checkTuple(int[] tuple) {
        throw new SolverException("checkTuple should not be used on an IterRelation");
    }

    @Override
    public LargeRelation duplicate() {
        return new IterTuplesTable(this.tuplesIndexes.clone(), this.nbVar, this.lowerbounds.clone(), this.ranges.clone(), this.table.clone());
    }
}
