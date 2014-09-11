/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package util.objects.setDataStructures;

import memory.IEnvironment;
import memory.IStateInt;
import memory.structure.S64BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 feb. 2011
 */
public class Set_Std_BitSet extends S64BitSet implements ISet {

    protected int current;    //enables to iterate
    protected IStateInt card;    // enables to get the cardinality in O(1)
	protected int n;

    public Set_Std_BitSet(IEnvironment environment, int nbits) {
        super(environment, nbits);
        current = 0;
        card = environment.makeInt(0);
		n = nbits;
    }

    @Override
    public boolean add(int element) {
        if (!get(element)) {
            card.add(1);
            this.set(element, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        boolean isIn = this.get(element);
        if (isIn) {
            this.set(element, false);
            card.add(-1);
        }
        return isIn;
    }

    @Override
    public boolean contain(int element) {
        return this.get(element);
    }

    @Override
    public int getSize() {
        return this.card.get();
    }

    @Override
    public int getFirstElement() {
        current = nextSetBit(0);
        return current;
    }

    @Override
    public int getNextElement() {
        current = nextSetBit(current + 1);
        return current;
    }

    @Override
    public void clear() {
        super.clear();
        card.set(0);
    }

	@Override
	public SetType getSetType(){
		return SetType.BITSET;
	}

	@Override
	public int[] toArray(){
		int[] a = new int[getSize()];
		int idx = 0;
		for(int i=getFirstElement();i>=0;i=getNextElement()){
			a[idx++] = i;
		}
		return a;
	}

	@Override
	public int getMaxSize(){
		return n;
	}
}
