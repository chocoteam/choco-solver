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
package org.chocosolver.memory.structure;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;

import java.io.Serializable;

/**
 */
public final class BasicIndexedBipartiteSet implements Serializable{

    /**
     * Serial number for serialization purpose
     */
    private static final long serialVersionUID = 1L;

    /**
     * The list of values
     */
    protected int[] list;

    /**
     * The position of each element within the list.
     * indexes[3] = k <=> list[k] = 3
     * we assume that elements ranges from 0 ... list.length
     * in other words the elements must be indexed.
     */
    protected int[] position;

    /**
     * The first element of the list
     */
    protected IStateInt first;

    /**
     * The size of the valid list
     */
    protected int size;

    /**
     * Create a stored bipartite set with a size.
     * Thus the value stored will go from 0 to nbValues.
     *
     * @param environment a bactrackable environment
     * @param nbValues capacity
     */
    public BasicIndexedBipartiteSet(IEnvironment environment, int nbValues) {
        this.list = new int[nbValues];
        this.position = new int[nbValues];
        for (int i = 0; i < nbValues; i++) {
            list[i] = position[i] = i;
        }
        this.first = environment.makeInt(0);
        this.size = 0;
    }

    /**
     * Increase the number of value watched.
     */
    private void increaseSize() {
        final int nexSize = list.length * 3 / 2;
        int[] list_ = list;
        list = new int[nexSize];
        System.arraycopy(list_, 0, list, 0, list_.length);
        int[] position_ = position;
        position = new int[nexSize];
        System.arraycopy(position_, 0, position, 0, position_.length);
    }

    public final int size() {
        return size - first.get() + 1;
    }

    public final boolean isEmpty() {
        return first.get() == size;
    }

    public final int add() {
        if (list.length == size) {
            increaseSize();
        }
        list[size] = size;
        position[list[size]] = size;
        size++;
        return size - 1;
    }

    public void swap(final int object) {
        final int idxToSwap = position[object];
        if (idxToSwap == first.get()) {
            first.add(1);
        } else {
            final int temp = list[first.get()];
            list[first.get()] = object;
            list[idxToSwap] = temp;
            position[object] = first.get();
            position[temp] = idxToSwap;
            first.add(1);
        }
    }

    public int get(final int index) {
        return list[index];
    }

    /**
     * Returns the bundle in which the i^h object is placed.
     * <code>true</code> means IN, the object can be swapped
     * <code>false</code> means OUT, the object is already swapped
     *
     * @param i index of the object
     * @return a boolean value: true means can be swapped, false otherwise
     */
    public boolean bundle(int i) {
        return position[i] >= first.get();
    }

    public boolean contains(int i){
        return bundle(i);
    }

}
