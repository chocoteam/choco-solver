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

package solver.constraints.probabilistic.propagators.nary;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Xavier Lorca
 * @since 28 nov. 2011
 */
public class Union {

    /**
     * values in the union from 0 to idx included
     */
    Value[] values;

    /**
     * available values in the table values[]
     */
    IStateBitSet indices;

    /**
     * position in the table values[] of the last removed value
     */
    IStateInt posLastRemVal;

    public Union(IntVar[] variables, IEnvironment environment) {
        Set<Value> vals = new HashSet<Value>();
        TIntObjectHashMap<Value> int2Value = new TIntObjectHashMap<Value>();
        for (IntVar var : variables) {
            int ub = var.getUB();
            for (int value = var.getLB(); value <= ub; value = var.nextValue(value)) {
                Value v = int2Value.get(value);
                if (v == null) {
                    v = new Value(value, environment);
                    vals.add(v);
                    int2Value.put(value, v);
                }
                v.incrOcc();
            }
        }
        values = vals.toArray(new Value[vals.size()]);
        this.indices = environment.makeBitSet(values.length);//new BitSet(values.length);

        this.indices.set(0, values.length);
        this.posLastRemVal = environment.makeInt(-1);
    }

    public int getSize() {
        return this.indices.cardinality();
    }

    public int getPositionLastRemVal() {
        return this.posLastRemVal.get();
    }

    public void remove(int value) {
        int rankV = this.getRank(value);
        Value val = values[rankV];
        val.decrOcc();
        if (val.getOcc() == 0) {
            this.indices.set(rankV, false);
            this.posLastRemVal.set(rankV);
        }
    }

    private int getRank(int v) {
        int rank = 0;
        int next = this.indices.nextSetBit(rank);
        while (next > -1 && this.values[next].getValue() != v) {
            rank++;
            next = this.indices.nextSetBit(rank);
        }
        return rank;
    }

    public int[] getValues() {
        int[] tmp = new int[this.getSize()];
        int idx = 0;
        for (int i = this.indices.nextSetBit(0); i >= 0; i = this.indices.nextSetBit(i + 1)) {
            tmp[idx++] = values[i].getValue();
        }
        return tmp;
    }

    public String toString() {
        String res = "[";
        int[] arrayValues = this.getValues();
        for (int i = 0; i < arrayValues.length; i++) {
            res += arrayValues[i] + ", ";
        }
        res = res.substring(0, res.length() - 2);
        res += "]\n";
        res += this.printDetails();
        return res;
    }

    private String printDetails() {
        String details = "[";
        for (int i = 0; i < values.length; i++) {
            details += values[i] + ", ";
        }
        details = details.substring(0, details.length() - 2);
        details += "]";
        return details;
    }

}

final class Value {

    private int value;
    private IStateInt occ;

    Value(int value, IEnvironment environment) {
        this.value = value;
        this.occ = environment.makeInt();
    }

    public final int getValue() {
        return value;
    }

    public final int getOcc() {
        return occ.get();
    }

    public final void incrOcc() {
        this.occ.add(1);
    }

    public final void decrOcc() {
        this.occ.add(-1);
    }

    public boolean equals(Object o) {
        Value v = (Value) o;
        return this.value == v.getValue();
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    public String toString() {
        return "<" + value + "," + occ + ">";
    }
}