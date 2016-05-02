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
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

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

	/**
     * checks if var=val has no support within the domain of v
     * @param var a variable index
     * @param val a value for var
     * @param v a variable
     * @return true iff there exists no support for v where var = val
     */
    public boolean checkUnsupportedValue(int var, int val, IntVar v) {
        int UB = v.getUB();
        BitSet _table = table[var][val - offsets[var]];
        int o = offsets[1 - var];
        for (int i = v.getLB(); i <= UB; i = v.nextValue(i)) {
            if (_table.get(i - o)) {
                return false;
            }
        }
        return true;
    }
}
