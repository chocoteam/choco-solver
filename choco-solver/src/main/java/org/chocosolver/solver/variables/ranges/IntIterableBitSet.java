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
package org.chocosolver.solver.variables.ranges;

import java.util.BitSet;

/**
 * An IntIterableBitSet based on a BitSet
 *
 * Created by cprudhom on 09/07/15.
 * Project: choco.
 * @author Charles Prud'homme
 */
public class IntIterableBitSet implements IntIterableSet {

    public BitSet VALUES;
    int OFFSET;

    public IntIterableBitSet() {
        this.VALUES = new BitSet();
    }

    public void setOffset(int offset) {
        this.OFFSET = offset;
    }

    @Override
    public int first(){
        if(VALUES.cardinality() == 0){
            return Integer.MIN_VALUE;
        }
        return VALUES.nextSetBit(0) + OFFSET;
    }


    @Override
    public int last() {
        if(VALUES.cardinality() == 0){
            return Integer.MAX_VALUE;
        }
        return VALUES.previousSetBit(VALUES.size()) + OFFSET;
    }

    @Override
    public boolean add(int e) {
        boolean add = !VALUES.get(e - OFFSET);
        VALUES.set(e - OFFSET);
        return add;
    }

    @Override
    public boolean addAll(int... values) {
        int card = VALUES.cardinality();
        for(int i = 0; i < values.length; i++){
            VALUES.set(values[i] - OFFSET);
        }
        return VALUES.cardinality() - card > 0;
    }

    @Override
    public boolean addAll(IntIterableSet set) {
        int card = VALUES.cardinality();
        int v = set.first();
        while(v < Integer.MAX_VALUE){
            add(v);
            v = set.nextValue(v);
        }
        return VALUES.cardinality() - card > 0;
    }

    @Override
    public boolean retainAll(IntIterableSet set) {
        boolean modified = false;
        for (int i = VALUES.nextSetBit(0); i >= 0; i = VALUES.nextSetBit(i + 1)) {
            if (!set.contains(i + OFFSET)) {
                VALUES.clear(i);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean remove(int e) {
        boolean rem  = VALUES.get(e - OFFSET);
        VALUES.clear(e - OFFSET);
        return rem;
    }

    @Override
    public boolean removeAll(IntIterableSet set) {
        boolean modified = false;
        for (int i = VALUES.nextSetBit(0); i >= 0; i = VALUES.nextSetBit(i + 1)) {
            if (set.contains(i + OFFSET)) {
                VALUES.clear(i);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        VALUES.clear();
    }

    @Override
    public int nextValue(int aValue) {
        int lb = VALUES.nextSetBit(0);
        if (lb >= 0) {
            aValue -= OFFSET;
            if (aValue < 0 || aValue < lb) {
                return lb + OFFSET;
            }
            if(aValue < Integer.MAX_VALUE) {
                aValue = VALUES.nextSetBit(aValue + 1);
            }
            if (aValue > -1) {
                return aValue + OFFSET;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int previousValue(int aValue) {
        int ub = VALUES.previousSetBit(VALUES.length());
        if (ub >= 0) {
            aValue -= OFFSET;
            if (aValue > ub) {
                return ub + OFFSET;
            }
            if (aValue > -1) {
                aValue = VALUES.previousSetBit(aValue - 1);
            }
            if (aValue > -1) {
                return aValue + OFFSET;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean contains(int aValue) {
        aValue -= OFFSET;
        return aValue > -1 && aValue < VALUES.length() && VALUES.get(aValue);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('{');

        int i = VALUES.nextSetBit(0);
        if (i != -1) {
            b.append(i + OFFSET);
            for (i = VALUES.nextSetBit(i + 1); i >= 0; i = VALUES.nextSetBit(i + 1)) {
                int endOfRun = VALUES.nextClearBit(i);
                do {
                    b.append(", ").append(i + OFFSET);
                }
                while (++i < endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }

    public IntIterableSet duplicate() {
        IntIterableBitSet bsrm = new IntIterableBitSet();
        bsrm.setOffset(this.OFFSET);
        bsrm.VALUES.or(this.VALUES);
        return bsrm;
    }

    @Override
    public int size() {
        return VALUES.cardinality();
    }
}
