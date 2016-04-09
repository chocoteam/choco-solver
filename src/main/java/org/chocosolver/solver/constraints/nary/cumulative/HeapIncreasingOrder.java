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
package org.chocosolver.solver.constraints.nary.cumulative;

/**
 * Heap used in TTEF algorithm
 * @author Alban DERRIEN
 */
public class HeapIncreasingOrder {

    private int[] key; // Key for the ordering.
    private int[] task;
    private int nbItems;

    public HeapIncreasingOrder(int _size) {
        this.nbItems = 0;
        key = new int[_size];
        task = new int[_size];
    }

    public void clear() {
        nbItems = 0;
    }

    /**
     * Inserts the specified item into this heap
     * @param _key
     * @param _task
     */
    public void add(int _key, int _task) {
        int i = nbItems;

        int parent = parent(i);
        while ( (i>0) && (key[parent] > _key) ) {
            key[i] = key[parent];
            task[i] = task[parent];
            i = parent;
            parent = parent(i);
        }
        key[i] = _key;
        task[i] = _task;
        nbItems++;

    }

    /**
     * Removes the top of this heap. (without any check)
     * @return
     */
    public void remove() {
        nbItems--;
        key[0] = key[nbItems];
        task[0] = task[nbItems];
        int vheight = key[0];
        int vtask = task[0];
        int i = 0;
        int j;
        while (!isLeaf(i)) {
            j = leftChild(i);
            if (hasRightChild(i) && key[rightChild(i)] < key[leftChild(i)]) {
                j = rightChild(i);
            }
            if (vheight <= key[j]) break;
            key[i] = key[j];
            task[i] = task[j];
            i = j;
        }
        key[i] = vheight;
        task[i] = vtask;

    }

    /**
     * Retrieves, but does not remove, the height of the top item of this heap. Doesn't check if the heap is empty.
     * @return
     */
    public int getHeadKey() {
        return key[0];
    }

    /**
     * Retrieves, but does not remove, the task of the top item of this heap. Doesn't check if the heap is empty.
     * @return
     */
    public int getHeadTask() {
        return task[0];
    }

    public boolean isEmpty() {
        return (nbItems == 0);
    }

    private int parent(int _child) {
        return ((_child + 1) >> 1) - 1;
    }

    private int leftChild(int _parent) {
        return ((_parent + 1) << 1) - 1;
    }

    private int rightChild(int _parent) {
        return ((_parent + 1) << 1);
    }

    private boolean isLeaf(int i) {
        return ( (((i + 1) << 1) - 1) >= nbItems);
    }

    private boolean hasRightChild(int i) {
        return ( ((i + 1) << 1) < nbItems);
    }

    public String toString() {
        String res = "";
        for(int i=0;i<nbItems;i++) {
            res += "<key="+key[i]+",task="+task[i]+"< ";
        }
        return res;
    }

    public int nbItems() {
        return nbItems;
    }
}


