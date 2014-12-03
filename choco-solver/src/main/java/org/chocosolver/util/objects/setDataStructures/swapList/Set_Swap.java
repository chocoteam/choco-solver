/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.util.objects.setDataStructures.swapList;

import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * List of m elements based on Array int_swaping
 * add : O(1)
 * testPresence: O(1)
 * remove: O(1)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/11/2011
 */
public abstract class Set_Swap implements ISet {

    protected int arrayLength, currentIdx, size;
	protected final int sizeMax;
    protected int[] array;

    public Set_Swap(int n) {
        size = 0;
        sizeMax = n;
        arrayLength = 16;
        array = new int[arrayLength];
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public int getSize() {
        return size;
    }

    protected void setSize(int s) {
        size = s;
    }

    protected void addSize(int delta) {
        size += delta;
    }

    @Override
    public String toString() {
        int size = getSize();
        if (size == 0) {
            return "empty";
        }
        String res = "";
        for (int i = 0; i < size - 1; i++) {
            res += array[i] + " -> ";
        }
        res += array[size - 1];
        return res;
    }

    @Override
    public void clear() {
        setSize(0);
    }

    // --- Iterations
    @Override
    public int getFirstElement() {
        if (getSize() == 0) {
            return -1;
        }
        currentIdx = 0;
        return array[currentIdx];
    }

    @Override
    public int getNextElement() {
        currentIdx++;
        if (currentIdx >= getSize()) {
            return -1;
        }
        return array[currentIdx];
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
		return sizeMax;
	}
}
