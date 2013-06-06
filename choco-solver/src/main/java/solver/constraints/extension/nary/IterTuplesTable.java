/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.extension.nary;

import solver.exception.SolverException;

import java.util.Iterator;
import java.util.List;

/**
 *
 **/
public class IterTuplesTable extends TuplesList implements IterLargeRelation, LargeRelation {

    /**
     * table[i][j] gives the table of supports as an int[] for value j of variable i
     */
    protected int[][][] table;

    /**
     * number of variables
     */
    protected int nbVar = 0;

    /**
     * The sizes of the domains
     */
    protected int[] dsizes;

    /**
     * The lower bound of each variable
     */
    protected int[] offsets;

    public IterTuplesTable(List<int[]> tuples, int[] offsets, int[] domSizes) {
        super(tuples);
        nbVar = domSizes.length;
        dsizes = domSizes;
        this.offsets = offsets;
        table = new int[nbVar][][];
        for (int i = 0; i < domSizes.length; i++) {
            table[i] = new int[domSizes[i]][];
            int[] nbsups = getNbSupportFor(tuples, i);
            for (int j = 0; j < nbsups.length; j++) {
                table[i][j] = new int[nbsups[j]];
            }
        }
        buildInitialListOfSupports(tuples);
    }

    /**
     * return the number of tuples supporting each value of variable i
     *
     * @param tups
     * @param i    a variable
     * @return
     */
    public int[] getNbSupportFor(List<int[]> tups, int i) {
        int[] nbsup = new int[dsizes[i]];
        for (Iterator it = tups.iterator(); it.hasNext(); ) {
            int[] tuple = (int[]) it.next();
            nbsup[tuple[i] - offsets[i]]++;
        }
        return nbsup;
    }

    public void buildInitialListOfSupports(List<int[]> tuples) {
        int cpt = 0;
        int[][] level = new int[nbVar][];
        for (int i = 0; i < nbVar; i++) {
            level[i] = new int[dsizes[i]];
        }
        for (Iterator<int[]> it = tuples.iterator(); it.hasNext(); ) {
            int[] tuple = it.next();
            for (int i = 0; i < tuple.length; i++) {
                int value = tuple[i] - offsets[i];
                table[i][value][level[i][value]] = cpt;
                level[i][value]++;
            }
            cpt++;
        }
    }

    /**
     * for fast access
     *
     * @return
     */
    public int[][][] getTableLists() {
        return table;
    }

    /**
     * This relation do not take advantage of the knowledge of the
     * previous support ! so start from scratch
     *
     * @param oldidx
     * @param var
     * @param val    is the value assuming the offset has already been
     *               removed
     * @return
     */
    public int seekNextTuple(int oldidx, int var, int val) {
        int nidx = oldidx++;
        if (nidx < table[var][val].length) {
            return table[var][val][nidx];
        } else {
            return -1;
        }
    }

    /**
     * return the number of supports of the pair (var, val) assuming the
     * offset has already been removed
     *
     * @param var
     * @param val
     * @return
     */
    public int getNbSupport(int var, int val) {
        return table[var][val].length;
    }

    public int getRelationOffset(int var) {
        return offsets[var];
    }

    public boolean checkTuple(int[] tuple) {
        throw new SolverException("checkTuple should not be used on an IterRelation");
    }

    public boolean isConsistent(int[] tuple) {
        return checkTuple(tuple);
    }
}
