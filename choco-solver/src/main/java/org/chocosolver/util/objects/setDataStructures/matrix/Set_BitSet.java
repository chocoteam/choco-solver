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
package org.chocosolver.util.objects.setDataStructures.matrix;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 feb. 2011
 */
public class Set_BitSet extends BitSet implements ISet {

    private int current;//enables to iterate
    private int card;    // enable to get the cardinality in O(1)
	private int n;

    public Set_BitSet(int nbits) {
        super(nbits);
        card = 0;
        current = 0;
		n = nbits;
    }

    @Override
    public boolean add(int element) {
        if (!get(element)) {
            card++;
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
            card--;
        }
        return isIn;
    }

    @Override
    public boolean contain(int element) {
        return this.get(element);
    }

    @Override
    public boolean isEmpty() {
        return this.cardinality() == 0;
    }

    @Override
    public int getSize() {
        return this.card;
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
        card = 0;
        super.clear();
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
