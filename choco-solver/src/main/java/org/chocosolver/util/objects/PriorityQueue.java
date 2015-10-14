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

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/04/2014
 */
public class PriorityQueue {

    private int n;
    private int[] indices;
    private int[] values;
    private int[] pointers;
    private int first, lastElt;

    public PriorityQueue(int _n) {
        this.n = _n;
        this.indices = new int[_n];
        this.pointers = new int[_n];
        this.values = new int[_n];
        this.clear();
    }

    /**
     * Adds an integer into the list. The element is inserted at its right
     * place (the list is sorted) in O(n).
     *
     * @param index the element to insert.
     * @param value the value to be used for the comparison of the elements to add.
     * @return <code>true</code> if and only if the list is not full.
     */
    public boolean addElement(int index, int value) {
        int i;
        int j = -1;
        if (this.lastElt == this.n) {
            return false;
        }
        this.indices[this.lastElt] = index;
        this.values[this.lastElt] = value;

        for (i = this.first; i != -1 && this.values[i] <= value; i = this.pointers[i]) {
            j = i;
        }
        this.pointers[this.lastElt] = i;
        if (j == -1) {
            this.first = this.lastElt;
        } else {
            this.pointers[j] = this.lastElt;
        }
        this.lastElt++;
        return true;
    }

    /**
     * Returns and removes the element with highest priority (i.e. lowest value) in O(1).
     *
     * @return the lowest element.
     */
    public int pop() {
        if (this.isEmpty()) {
            return -1;
        }
        int elt = this.indices[this.first];
        this.first = this.pointers[this.first];
        return elt;
    }

    /**
     * Tests if the list is empty or not.
     *
     * @return <code>true</code> if and only if the list is empty.
     */
    public boolean isEmpty() {
        return (this.first == -1);
    }

    /**
     * Clears the list.
     */
    public void clear() {
        this.first = -1;
        this.lastElt = 0;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("<");
        for (int i = this.first; i != -1; i = this.pointers[i]) {
            s.append(" ").append(this.indices[i]);
        }
        s.append(" >");
        return s.toString();
    }
}
