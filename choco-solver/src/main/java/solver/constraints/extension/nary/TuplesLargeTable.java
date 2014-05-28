/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import solver.constraints.extension.Tuples;
import solver.exception.SolverException;

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
    protected int n;
    /**
     * The consistency matrix
     */
    protected TIntObjectHashMap<TIntSet> tables;

    /**
     * offset (lower bound) of each variable
     */
    protected int[] offsets;

    /**
     * domain size of each variable
     */
    protected int[] sizes;

    protected boolean feasible;

    /**
     * in order to speed up the computation of the index of a tuple
     * in the table, blocks[i] stores the product of the size of variables j with j < i.
     */
    protected long[] blocks;

    public TuplesLargeTable(Tuples tuples, int[] offsetTable, int[] sizesTable) {
        offsets = offsetTable;
        sizes = sizesTable;
        n = offsetTable.length;
        feasible = tuples.isFeasible();
        long totalSize = 1;
        blocks = new long[n];
        for (int i = 0; i < n; i++) {
            blocks[i] = totalSize;
            totalSize *= sizes[i];
        }

        long nb = (totalSize / Integer.MAX_VALUE) + 1;
        if (nb < 0 || nb > Integer.MAX_VALUE) throw new SolverException("Tuples required too much memory ...");

        tables = new TIntObjectHashMap<>();
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            if (valid(tuple, offsets, sizes)) {
                setTuple(tuple);
            }
        }
    }

    public boolean checkTuple(int[] tuple) {
        long address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            if ((tuple[i] < offsets[i]) || (tuple[i] > (offsets[i] + sizes[i] - 1))) {
                return false;
            }
            address += (tuple[i] - offsets[i]) * blocks[i];
        }
        int a = (int) (address % Integer.MAX_VALUE);
        int t = (int) (address / Integer.MAX_VALUE);
        TIntSet ts = tables.get(t);
        return ts != null && ts.contains(a);
    }

    public boolean isConsistent(int[] tuple) {
        return checkTuple(tuple) == feasible;
    }

    void setTuple(int[] tuple) {
        long address = 0;
        for (int i = (n - 1); i >= 0; i--) {
            address += (tuple[i] - offsets[i]) * blocks[i];
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

}