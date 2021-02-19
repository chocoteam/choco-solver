/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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
    public int nextValueOut(int aValue) {
	    int val = aValue - offset;
	    if(val < -1 || val >= values.length()){
	        return aValue + 1;
        }else{
            val = values.nextClearBit(val + 1);
            return val + offset;
        }
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

    @Override
    public int previousValueOut(int aValue) {
        int val = aValue - offset;
        if(val <= -1 || val > values.length()){
            return aValue - 1;
        }else{
            val = values.previousClearBit(val - 1);
            return val + offset;
        }
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
