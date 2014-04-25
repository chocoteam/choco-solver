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

package solver.constraints.extension.binary;

import solver.constraints.extension.ConsistencyRelation;
import solver.constraints.extension.Tuples;

import java.util.BitSet;

class CouplesTable extends ConsistencyRelation implements BinRelation {

    /**
     * matrix of consistency/inconsistency
     */
    protected BitSet table;

    /**
     * first value of x
     */
    protected int offset1;

    /**
     * first value of y
     */
    protected int offset2;


    /**
     * size of the initial domain of x
     */
    protected int n1;


    /**
     * size of the initial domain of x
     */
    protected int n2;


    protected CouplesTable() {
    }

    public CouplesTable(Tuples tuples, int min1, int max1, int min2, int max2) {
        offset1 = min1;
        offset2 = min2;
        n1 = max1 - min1 + 1;
        n2 = max2 - min2 + 1;
        table = new BitSet(n1 * n2);
        feasible = tuples.isFeasible();
        int nt = tuples.nbTuples();
        for (int i = 0; i < nt; i++) {
            int[] tuple = tuples.get(i);
            setCouple(tuple[0], tuple[1]);
        }
    }

    /**
     * compute the opposite relation by "reusing" the table of consistency
     *
     * @return the opposite relation
     */
    public ConsistencyRelation getOpposite() {
        CouplesTable t = new CouplesTable();
        t.feasible = !feasible;
        t.table = table;
        t.offset1 = offset1;
        t.offset2 = offset2;
        t.n2 = n2;
        return t;
    }

    public void setCouple(int x, int y) {
        if (between(x - offset1, 0, n1) && between(y - offset2, 0, n2)) {
            table.set((x - offset1) * n1 + y - offset2);
        }
    }

    public boolean isConsistent(int x, int y) {
        return table.get((x - offset1) * n1 + y - offset2) == feasible;
    }

    public boolean checkCouple(int x, int y) {
        return table.get((x - offset1) * n1 + y - offset2);
    }


    private static boolean between(int v, int low, int upp) {
        return (low <= v) && (v <= upp);
    }
}
