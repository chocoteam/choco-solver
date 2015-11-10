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
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.variables.IntVar;

/**
 * A decision made of multiple instantiation.
 * Required for large neighborhood search, for example.
 * Cannot be refuted.
 * Created by cprudhom on 04/09/15.
 * Project: choco.
 */
public class IntMetaDecision extends Decision<IntVar[]> {

    protected int size;
    protected int[] val;
    protected DecisionOperator<IntVar>[] dop; // is assignment?

    public IntMetaDecision() {
        super(1);
        var = new IntVar[64];
        val = new int[64];
        dop = new DecisionOperator[64];
    }

    @Override
    public void apply() throws ContradictionException {
        for (int i = 0; i < size; i++) {
            dop[i].apply(var[i], val[i], this);
        }
    }

    public void add(IntVar aVar, int aVal) {
        add(aVar, aVal, DecisionOperator.int_eq);
    }

    public void add(IntVar aVar, int aVal, DecisionOperator<IntVar> aDop) {
        if (size >= var.length) {
            increase();
        }
        var[size] = aVar;
        val[size] = aVal;

        this.dop[size++] = aDop;
    }

    public IntVar getVar(int i) {
        return var[i];
    }

    public int getVal(int i) {
        return val[i];
    }

    public DecisionOperator<IntVar> getDop(int i){
        return dop[i];
    }

    public int size() {
        return size;
    }

    public void remove(int from, int to){
        System.arraycopy(var, to, var, from, size - to);
        System.arraycopy(val, to, val, from, size - to);
        System.arraycopy(dop, to, dop, from, size - to);
        size -= (to - from);
    }

    /**
     * Flip the decision at position i
     * @param idx
     */
    public void flip(int idx){
        if(dop[idx] == DecisionOperator.int_split){
            val[idx]++;
        }
        else if(dop[idx] == DecisionOperator.int_reverse_split){
            val[idx]--;
        }
        dop[idx].opposite();
    }

    @Override
    public Object getDecisionValue() {
        return val;
    }


    @Override
    public void free() {
        size = 0;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("(");
        int i = 0;
        switch (size) {
            case 0:
                break;
            default:
            case 3:
                st.append(var[i].getName()).append(dop[i]).append(val[i++]).append(" & ");
            case 2:
                st.append(var[i].getName()).append(dop[i]).append(val[i++]).append(" & ");
            case 1:
                st.append(var[i].getName()).append(dop[i]).append(val[i++]);
        }
        if (i < size) {
            if (size > 4) {
                st.append(" & ...");
            }
            st.append(" & ").append(var[size - 1].getName()).append(dop[i]).append(val[size - 1]);
        }
        st.append(')');

        return st.toString();
    }

    private void increase() {
        int oldCapacity = var.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);

        IntVar[] varBigger = new IntVar[newCapacity];
        System.arraycopy(var, 0, varBigger, 0, oldCapacity);
        var = varBigger;

        int[] valBigger = new int[newCapacity];
        System.arraycopy(val, 0, valBigger, 0, oldCapacity);
        val = valBigger;
    }
}
