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
package solver.constraints.extension.binary;

import solver.constraints.extension.ConsistencyRelation;
import solver.constraints.extension.Tuples;
import solver.variables.IntVar;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/04/2014
 */
class CouplesBitSetTable extends ConsistencyRelation implements BinRelation {

    /**
     * table[0][i] gives the supports of value i of variable 0
     * table[1][i] gives the supports of value i of variable 1
     */
    protected BitSet[][] table;

    /**
     * first value of x
     */
    protected int[] offsets;

    protected int[] ns;

    protected CouplesBitSetTable() {
    }

    /**
     * Create a tuple list for AC3bit+rm
     *
     * @param tuples list of tuples
     * @param min1   v1.getLB()
     * @param max1   v1.getUB()
     * @param min2   v2.getLB()
     * @param max2   v2.getUB()
     */
    public CouplesBitSetTable(Tuples tuples, int min1, int max1, int min2, int max2) {
        this.offsets = new int[]{min1, min2};
        int n1 = max1 - min1 + 1;
        int n2 = max2 - min2 + 1;
        this.table = new BitSet[2][];
        this.table[0] = new BitSet[n1];
        this.ns = new int[]{n1, n2};
        this.feasible = tuples.isFeasible();
        for (int i = 0; i < n1; i++) {
            table[0][i] = new BitSet(n2);
            if (!feasible) table[0][i].set(0, n2);
        }
        this.table[1] = new BitSet[n2];
        for (int i = 0; i < n2; i++) {
            table[1][i] = new BitSet(n1);
            if (!feasible) table[1][i].set(0, n1);
        }

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
        CouplesBitSetTable t = new CouplesBitSetTable();
        t.feasible = !feasible;
        t.table = new BitSet[2][];
        this.table[0] = new BitSet[ns[0]];
        this.table[1] = new BitSet[ns[1]];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < table[i].length; j++) {
                t.table[i][j] = (BitSet) table[i][j].clone();
                t.table[i][j].flip(0, ns[i]);
            }
        }
        t.ns = ns;
        t.offsets = offsets;
        return t;
    }


    private void setCouple(int x, int y) {
        // stored only couples which are between ranges
        if (between(x - offsets[0], 0, ns[0]) && between(y - offsets[1], 0, ns[1])) {
            if (feasible) {
                table[0][x - offsets[0]].set(y - offsets[1]);
                table[1][y - offsets[1]].set(x - offsets[0]);
            } else {
                table[0][x - offsets[0]].clear(y - offsets[1]);
                table[1][y - offsets[1]].clear(x - offsets[0]);
            }
        }
    }

    public boolean isConsistent(int x, int y) {
        return table[0][x - offsets[0]].get(y - offsets[1]);
    }

    public boolean checkCouple(int x, int y) {
        return table[0][x - offsets[0]].get(y - offsets[1]);
    }

    /**
     * check is there exist a support for value val of variable var
     * within the domain of v
     *
     */
    public boolean checkValue(int var, int val, IntVar v) {
        int UB = v.getUB();
        BitSet _table = table[var][val - offsets[var]];
        int o = offsets[1 - var];
        for (int i = v.getLB(); i <= UB; i = v.nextValue(i)) {
            if (_table.get(i - o)) {
                return true;
            }
        }
        return false;
    }

    private static boolean between(int v, int low, int upp) {
        return (low <= v) && (v <= upp);
    }
}
