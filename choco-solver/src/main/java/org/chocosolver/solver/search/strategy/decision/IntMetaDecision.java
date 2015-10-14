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
import org.chocosolver.solver.variables.IntVar;

/**
 * A decision made of multiple instantiation.
 * Required for large neighborhood search, for example.
 * Cannot be refuted.
 * Created by cprudhom on 04/09/15.
 * Project: choco.
 */
public class IntMetaDecision extends Decision<IntVar[]> {

    int idx;
    int[] val;

    public IntMetaDecision() {
        super(1);
        var = new IntVar[64];
        val = new int[64];
    }

    @Override
    public void apply() throws ContradictionException {
        for (int i = 0; i < idx; i++) {
            var[i].instantiateTo(val[i], this);
        }
    }

    public void add(IntVar aVar, int aVal) {
        if (idx >= var.length) {
            increase();
        }
        var[idx] = aVar;
        val[idx++] = aVal;
    }

    @Override
    public Object getDecisionValue() {
        return val;
    }


    @Override
    public void free() {
        idx = 0;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("(");
        int i = 0;
        switch (idx) {
            case 0:
                break;
            default:
            case 3:
                st.append(var[i].getName()).append(" == ").append(val[i++]).append(" & ");
            case 2:
                st.append(var[i].getName()).append(" == ").append(val[i++]).append(" & ");
            case 1:
                st.append(var[i].getName()).append(" == ").append(val[i++]);
        }
        if (i < idx) {
            if (idx > 4) {
                st.append(" & ...");
            }
            st.append(" & ").append(var[idx - 1].getName()).append(" == ").append(val[idx - 1]);
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
