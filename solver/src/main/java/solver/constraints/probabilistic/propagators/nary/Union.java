/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.probabilistic.propagators.nary;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import solver.variables.IntVar;

import java.util.HashSet;
import java.util.Set;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15 nov. 2010
 */
public class Union {

    /**
     * link between a value and its position among all the values
     */
    TIntIntHashMap val2idx;

    /**
     * values in the union from 0 to idx included
     */
    Value[] values;
    IStateInt idx;

    /**
     * sum of the domain size
     */
    IStateInt nbOcc;


    public Union(IntVar[] variables, IEnvironment environment) {
        nbOcc = environment.makeInt(0);
        Set<Value> vals = new HashSet<Value>();
        TIntObjectHashMap<Value> int2Value = new TIntObjectHashMap<Value>();
        for (IntVar var : variables) {
            DisposableIntIterator it = var.getIterator();
            while (it.hasNext()) {
                int value = it.next();
                Value v = int2Value.get(value);
                if (v == null) {
                    v = new Value(value,environment);
                    vals.add(v);
                    int2Value.put(value,v);
                }
                v.incrOcc();
                nbOcc.add(1);
            }
        }
        values = vals.toArray(new Value[vals.size()]);
        idx = environment.makeInt(values.length-1);
        val2idx = new TIntIntHashMap();
        for(int i = 0; i < values.length; i++){
            val2idx.put(values[i].getValue(), i);
        }
    }

    public void remove(int value){
        int lastPresent = idx.get();
        int indice = val2idx.get(value);
        Value v = values[indice];
        v.decrOcc();
        nbOcc.add(-1);
        if (v.getOcc() == 0) {
            Value lastElement = values[lastPresent];
            values[lastPresent] = values[indice];
            values[indice] = lastElement;
            val2idx.put(lastElement.getValue(), indice);
            val2idx.put(value, lastPresent);
            idx.add(-1);
        }
    }


    public int getUnionSize(){
        return idx.get();
    }

    public int getNbOcc() {
        return nbOcc.get();
    }

    public int[] getValues(){
        int[] tmp = new int[idx.get()+1];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = values[i].getValue();
        }
        return tmp;
    }

    public String toString() {
        String res = "union : [";
        for (int i = 0; i <= idx.get(); i++) {
            res += values[i].getValue() + ", ";
        }
        res = res.substring(0,res.length()-2);
        res += "]";
        return res;
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
}