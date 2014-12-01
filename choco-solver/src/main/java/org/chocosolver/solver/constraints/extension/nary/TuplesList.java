/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A simple way of storing the tuples as a list. This doesn't allow
 * consistency check (TuplesTable is made for that)
 * or iteration over supports of each value (IterTuplesTable is made for that)
 * This simple way of storing supports only allow fast iteration over the all
 * set of tuples and is used by STR gac scheme.
 */
public class TuplesList extends LargeRelation {

    // each tuple (a int[]) has its own index
    protected final int[][] tuplesIndexes;

    protected static final Comparator<int[]> TCOMP = (o1, o2) -> {
        int n = o1.length;
        int i = 0;
        while (i < n && o1[i] == o2[i]) i++;
        if (i == n) return 0;
        if (o1[i] < o2[i]) return -1;
        return 1;
    };

    // required for duplicate method, should not be called by default
    TuplesList(int[][] tuplesIndexes) {
        this.tuplesIndexes = tuplesIndexes;
        Arrays.sort(tuplesIndexes, TCOMP);
    }

    public TuplesList(Tuples tuples, IntVar[] vars) {
        int nb = tuples.nbTuples();
        int[][] _tuplesIndexes = new int[nb][];
        int k = 0;
        for (int i = 0; i < nb; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, vars)) {
                _tuplesIndexes[k++] = tuple;
            }
        }
        tuplesIndexes = new int[k][];
        System.arraycopy(_tuplesIndexes, 0, tuplesIndexes, 0, k);
        Arrays.sort(tuplesIndexes, TCOMP);

    }

    public int[] getTuple(int support) {
        return tuplesIndexes[support];
    }

    public int[][] getTupleTable() {
        return tuplesIndexes;
    }

    public boolean checkTuple(int[] tuple) {
        throw new SolverException("TuplesList is an unusual large relation...");
    }

    public boolean isConsistent(int[] tuple) {
        return Arrays.binarySearch(tuplesIndexes, tuple, TCOMP) >= 0;
    }

    @Override
    public LargeRelation duplicate() {
        return new TuplesList(this.tuplesIndexes.clone());
    }
}
