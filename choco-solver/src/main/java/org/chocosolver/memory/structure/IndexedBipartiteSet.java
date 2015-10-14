/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import java.util.Arrays;
import org.chocosolver.memory.EnvironmentException;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.IStateIntVector;

/**
 * A stored list dedicated to positive integers and three operations :
 * - iteration
 * - removal of an element
 * - check if an element is or not within the list
 * It only requires a StoredInt to denote the first element of the list
 * and proceeds by swapping element with the first one to remove them and incrementing
 * the index of the first element.
 * IT DOES NOT PRESERVE THE ORDER OF THE LIST
 */
public class IndexedBipartiteSet extends IStateIntVector {

    /**
     * The list of values
     */
    protected int[] list;

    /**
     * The position of each element within the list.
     * indexes[3] = k <=> list[k] = 3
     * we assume that elements ranges from 0 ... list.lenght
     * in other words the elements must be indexed.
     */
    protected int[] position;

    /**
     * The first element of the list
     */
    protected IStateInt last;

    public void buildList(final IEnvironment environment, final int[] values) {
        this.list = values;
        int maxElt = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > maxElt) {
                maxElt = values[i];
            }
        }
        this.position = new int[maxElt + 1];
        // Use Integer.MAX_VALUE to denote missing value.
        // list cannot contain Integer.MAX_VALUE because maxElt + 1 would overflow
        // so the above statement will fail.
        // Therefore it is safe to use Integer.MAX_VALUE to denote missing value.
        Arrays.fill(position, Integer.MAX_VALUE);
        for (int i = 0; i < values.length; i++) {
            position[values[i]] = i;
        }
        this.last = environment.makeInt(list.length - 1);
    }

    /**
     * Create a stored bipartite set with a size.
     * Thus the value stored will go from 0 to nbValues.
     *
     * @param environment backtrable environment
     * @param nbValues capacity
     */
    public IndexedBipartiteSet(final IEnvironment environment, final int nbValues) {
        super(environment);
        final int[] values = new int[nbValues];
        for (int i = 0; i < nbValues; i++) {
            values[i] = i;
        }
        buildList(environment, values);
    }
    
    public IndexedBipartiteSet(final IEnvironment environment, final int[] values) {
        super(environment);
        buildList(environment, values);
    }

    /**
     * Increase the number of value watched.
     * BEWARE: be sure your are correctly calling this method.
     * It deletes everything already declared
     *
     * @param gap the gap the reach the expected size
     */
    public final void increaseSize(final int gap) {
        final int l = list.length;
        final int[] newList = new int[l + gap];
        for (int i = 0; i < l + gap; i++) {
            newList[i] = i;
        }
        int maxElt = 0;
        for (int i = 0; i < newList.length; i++) {
            if (newList[i] > maxElt) {
                maxElt = newList[i];
            }
        }
        final int[] newPosition = new int[maxElt + 1];
        Arrays.fill(newPosition, Integer.MAX_VALUE);
        for (int i = 0; i < newList.length; i++) {
            newPosition[newList[i]] = i;
        }
        // record already removed values
        final int end = last.get() + 1;
        final int[] removed = new int[list.length - end];
        System.arraycopy(list, end, removed, 0, list.length - end);

        this.list = newList;
        this.position = newPosition;
        final IEnvironment env = last.getEnvironment();
        this.last = null;
        this.last = env.makeInt(list.length - 1);
        for (int i = 0; i < removed.length; i++) {
            remove(removed[i]);
        }
    }

    public final int size() {
        return last.get() + 1;
    }

    public final boolean isEmpty() {
        return last.get() == -1;
    }

    public final void add(final int i) {
        throw new UnsupportedOperationException("adding element is not permitted in this structure (the list is only meant to decrease during search)");
    }

    public final void clear() {
        last.set(-1);
    }

    public final void removeLast() {
        remove(list[last.get()]);
    }

    public void remove(final int object) {
        if (contains(object)) {
            final int idxToRem = position[object];
            if (idxToRem == last.get()) {
                last.add(-1);
            } else {
                final int temp = list[last.get()];
                list[last.get()] = object;
                list[idxToRem] = temp;
                position[object] = last.get();
                position[temp] = idxToRem;
                last.add(-1);
            }
        }
    }

    public boolean contains(final int object) {
        // The IStateIntVector interface allows for negatives so need to
        // check for negatives.
        return object >= 0 && object < position.length && position[object] <= last.get();
    }


    public final int get(final int index) {
        return list[index];
    }

    @Override
    public final int quickGet(final int index) {
        return get(index);
    }

    public final int set(final int index, final int val) {
        throw new EnvironmentException("setting an element is not permitted on this structure");
    }

    @Override
    public final int quickSet(final int index, final int val) {
        return set(index, val);
    }

    public final String pretty() {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i <= last.get(); i++) {
            s.append(list[i]).append(i == (last.get()) ? "" : ",");
        }
        return s.append(']').toString();
    }

    //a is not in the list, returns its index k in the table from
    //the end of the list.
    //It basically means that a was the k element to be removed

    public final int findIndexOfInt(final int a) {
        return position[a] == Integer.MAX_VALUE
                ? -1
                : list.length - position[a];
    }
}
