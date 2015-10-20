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
package org.chocosolver.util.objects;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A specific implementation for hash map of integers wherein all keys and values are greater or equal to 0.
 * <p>
 * Created by cprudhom on 20/10/2015.
 * Project: choco.
 */
public class IntHash implements Serializable{

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * No entry key
     */
    private static final int FREE = -1;

    /**
     * The array buffer into which the elements of the values are stored.
     */
    int[] elements;

    /**
     * The number of key-value mappings contained in this map.
     */
    int size;


    public IntHash() {
        this(DEFAULT_CAPACITY);
    }

    public IntHash(int initialCapacity) {
        this.elements = new int[initialCapacity];
        Arrays.fill(elements, FREE);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - elements.length > 0){
            int oldCapacity = elements.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = MAX_ARRAY_SIZE;
            // minCapacity is usually close to size, so this is a win:
            elements = Arrays.copyOf(elements, newCapacity);
            Arrays.fill(elements, oldCapacity, newCapacity, FREE);
        }
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no elements.
     *
     * @return <tt>true</tt> if this map contains no elements.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void put(int key, int value) {
        ensureCapacity(key + 1);
        if (elements[key] == FREE) {
            size++;
        }
        if (value == FREE) {
            size--;
        }
        elements[key] = value;
    }

    /**
     * Adjusts the specified value associated with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is adjusted, otherwise, the value is put.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void putOrAdjust(int key, int value) {
        ensureCapacity(key + 1);
        if (elements[key] == FREE) {
            size++;
            elements[key] = value;
        } else {
            elements[key] += value;
        }

    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code -1} if this map contains no mapping for the key.
     * <p>
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==-1 ? k==-1 :
     * key == k)}, then this method returns {@code v}; otherwise
     * it returns {@code -1}.  (There can be at most one such mapping.)
     * <p>
     * <p>A return value of {@code -1} <i>necessarily</i>
     * indicate that the map contains no mapping for the key.
     *
     * @see #put(int, int)
     */
    public int get(int key) {
        if (key <= 0 || key >= elements.length) {
            return -1;
        }
        return elements[key];
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(int key) {
        return get(key) != FREE;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Arrays.fill(elements, 0, size, FREE);
        size = 0;
    }

}
