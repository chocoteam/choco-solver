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
import solver.constraints.extension.ExtensionalBinRelation;

import java.util.BitSet;

public class CouplesTable extends ConsistencyRelation implements ExtensionalBinRelation {

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
    protected int n2;


    public CouplesTable() {
    }

    public CouplesTable(boolean feas, int offset1, int offset2, int n1, int n2) {
        this.offset1 = offset1;
        this.offset2 = offset2;
        this.n2 = n2;
        this.table = new BitSet(n1 * n2);
        this.feasible = feas;
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
        table.set((x - offset1) * n2 + y - offset2);
    }

    public void setCoupleWithoutOffset(int x, int y) {
        table.set(x * n2 + y);
    }

    public boolean isConsistent(int x, int y) {
        return table.get((x - offset1) * n2 + y - offset2) == feasible;
    }

    public boolean checkCouple(int x, int y) {
        return table.get((x - offset1) * n2 + y - offset2);
    }

}
