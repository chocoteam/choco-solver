/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.util.objects.setDataStructures.iterable;

import org.chocosolver.util.objects.setDataStructures.bitset.Set_BitSet;

/**
 * An IntIterableBitSet based on a BitSet
 *
 * Created by cprudhom on 09/07/15.
 * Project: choco.
 * @author Charles Prud'homme
 */
public class IntIterableBitSet extends Set_BitSet implements IntIterableSet {

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an IntIterable object relying on an offseted bitset implementation.
     */
    public IntIterableBitSet() {
        super(0);
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * Creates an IntIterable object relying on a bitset implementation.
	 * For memory consumption purpose, an offset is needed to indicate the lowest value stored in this set.
	 * @param offset lowest value to be stored in this set
	 */
	public void setOffset(int offset){
		this.offset = offset;
	}

    @Override
    public boolean addAll(int... values) {
        int prevCard = size();
        for(int i = 0; i < values.length; i++){
            add(values[i]);
        }
        return size() - prevCard > 0;
    }

    @Override
    public boolean addAll(IntIterableSet set) {
		if(set.isEmpty())return false;
        int prevCard = size();
        int v = set.min();
        while(v < Integer.MAX_VALUE){
            add(v);
            v = set.nextValue(v);
        }
        return size() - prevCard > 0;
    }

    @Override
    public boolean retainAll(IntIterableSet set) {
		int prevCard = card;
        for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1)) {
            if (!set.contains(i + offset)) {
                values.clear(i);
				card--;
            }
        }
        return card < prevCard;
    }

    @Override
    public boolean removeAll(IntIterableSet set) {
		int prevCard = card;
        for (int i = values.nextSetBit(0); i >= 0; i = values.nextSetBit(i + 1)) {
            if (set.contains(i + offset)) {
                values.clear(i);
				card--;
            }
        }
        return card < prevCard;
    }

	@Override
    public boolean removeBetween(int f, int t) {
        f -= offset;
        t -= offset;
        int prevCard = card;
        values.clear(f, t);
		card = values.cardinality();
        return card - prevCard != 0;
    }

    @Override
    public int nextValue(int aValue) {
        int lb = values.nextSetBit(0);
        if (lb >= 0) {
            aValue -= offset;
            if (aValue < 0 || aValue < lb) {
                return lb + offset;
            }
            if(aValue < Integer.MAX_VALUE) {
                aValue = values.nextSetBit(aValue + 1);
            }
            if (aValue > -1) {
                return aValue + offset;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int previousValue(int aValue) {
        int ub = values.previousSetBit(values.length());
        if (ub >= 0) {
            aValue -= offset;
            if (aValue > ub) {
                return ub + offset;
            }
            if (aValue > -1) {
                aValue = values.previousSetBit(aValue - 1);
            }
            if (aValue > -1) {
                return aValue + offset;
            }
        }
        return Integer.MIN_VALUE;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append('{');

        int i = values.nextSetBit(0);
        if (i != -1) {
            b.append(i + offset);
            for (i = values.nextSetBit(i + 1); i >= 0; i = values.nextSetBit(i + 1)) {
                int endOfRun = values.nextClearBit(i);
                do {
                    b.append(", ").append(i + offset);
                }
                while (++i < endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }

	@Override
    public IntIterableSet duplicate() {
        IntIterableBitSet bsrm = new IntIterableBitSet();
		bsrm.setOffset(this.offset);
        bsrm.values.or(this.values);
        return bsrm;
    }

    @Override
    public void plus(int x) {
        this.offset += x;
    }

    @Override
    public void minus(int x) {
        this.offset -= x;
    }
}
