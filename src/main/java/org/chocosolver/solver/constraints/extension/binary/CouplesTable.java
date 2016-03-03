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

class CouplesTable extends BinRelation {

    /**
     * matrix of consistency/inconsistency
     */
    protected final BitSet table;

    /**
     * first value of x, and y
     */
    protected final int offset1, offset2;


    /**
     * size of the initial domain of x and y
     */
    protected final int range1, range2;


    protected final boolean feasible;

    public CouplesTable(Tuples tuples, IntVar var1, IntVar var2) {
        offset1 = var1.getLB();
        offset2 = var2.getLB();
        range1 = var1.getUB() - offset1 + 1;
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

    // required for duplicate method, should not be called by default
    private CouplesTable(BitSet table, int offset1, int offset2, int range1, int range2, boolean feasible) {
        this.table = table;
        this.offset1 = offset1;
        this.offset2 = offset2;
        this.range1 = range1;
        this.range2 = range2;
        this.feasible = feasible;
    }

    public boolean isConsistent(int x, int y) {
        return table.get((x - offset1) * range2 + y - offset2) == feasible;
    }

    public boolean checkCouple(int x, int y) {
        return table.get((x - offset1) * range2 + y - offset2);
    }

    @Override
    public BinRelation duplicate() {
        return new CouplesTable((BitSet) table.clone(), offset1, offset2, range1, range2, feasible);
    }
}
