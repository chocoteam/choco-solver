/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 18/11/12
 * Time: 15:18
 */

package solver.constraints.propagators.gary.tsp.specificHeaps;

/**
 * Binary heap
 *
 * @author Jean-Guillaume Fages
 */
public class BinarySimpleHeap implements ISimpleHeap {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int[] elements, positions;
    private double[] values;
    private int firstEmpty;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Binary heap for storing elements in the range [0,n-1]
     *
     * @param n maximal number of elements
     */
    public BinarySimpleHeap(int n) {
        elements = new int[n];
        positions = new int[n];
        values = new double[n];
        for (int i = 0; i < n; i++) {
            elements[i] = -1;
            positions[i] = -1;
        }
        firstEmpty = 0;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public boolean isEmpty() {
        return firstEmpty == 0;
    }

    public int removeFirstElement() {
        if (firstEmpty == 0) {
            return -1;
        }
        int first = elements[0];
        positions[first] = -1;
        // Replace the root of the heap with the last element on the last level
        firstEmpty--;
        int last = firstEmpty;
        if (last > 0) {
            move(last, 0);
            decrease(0);
        } else {
            positions[first] = -1;
            elements[0] = -1;
        }
        return first;
    }

    private void decrease(int node) {
        int left = (node << 1) + 1;
        int right = (node << 1) + 2;
        int next = left;
        if (left < firstEmpty) {
            if (right < firstEmpty && values[elements[right]] < values[elements[left]]) {
                next = right;
            }
            if (values[elements[node]] > values[elements[next]]) {
                swap(node, next);
                decrease(next);
            }
        }
    }

    public boolean addElement(int element, double value) {
        assert positions[element] == -1 : element + " is already in the heap";
        // add new data at the first available position in the binary heap
        elements[firstEmpty] = element;
        values[element] = value;
        positions[element] = firstEmpty;
        firstEmpty++;
        if (firstEmpty > 1) {
            // search for the parent at the correct position to swap with
            int last = firstEmpty - 1;
            int current = ((last - 1) >> 1);
            int idx = last;
            while (current != idx && current >= 0 && values[elements[idx]] < values[elements[current]]) {
                if (current != idx) {
                    swap(current, idx);
                }
                idx = current;
                current = ((current - 1) >> 1);
            }
        }
        return true;
    }

    public boolean updateElement(int element, double value) {
        assert positions[element] != -1 : element + " is not in the heap";
        if (value >= values[element]) {
            return false;
        }
        values[element] = value;
        // search for the parent at the correct position to swap with
        int last = positions[element];
        int current = ((last - 1) >> 1);
        int idx = last;
        while (current != idx && current >= 0 && values[elements[idx]] < values[elements[current]]) {
            if (current != idx) {
                swap(current, idx);
            }
            idx = current;
            current = ((current - 1) >> 1);
        }
        return true;
    }

    public boolean addOrUpdateElement(int element, double value) {
        if (positions[element] != -1) {
            return updateElement(element, value);
        } else {
            return addElement(element, value);
        }
    }

    /**
     * swap data in node n with data in node m
     *
     * @param n index of the node containing a data
     * @param m index of the node containing a data
     */
    private void swap(int n, int m) {
        int eM = elements[m];
        int eN = elements[n];
        elements[m] = eN;
        elements[n] = eM;
        positions[eM] = n;
        positions[eN] = m;
    }

    /**
     * move data in node n_from to node n_to
     * Erase previous information of node n_to
     *
     * @param n_from index of the node of the source data
     * @param n_to   index of the target node
     */
    private void move(int n_from, int n_to) {
        int eT = elements[n_to];
        int eF = elements[n_from];
        positions[eT] = -1;
        positions[eF] = n_to;
        elements[n_to] = eF;
        elements[n_from] = -1;
    }

    public String toString() {
        String s = "heap:\n";
        int k = 1;
        int tot = 0;
        while (tot < firstEmpty) {
            String st = "\n";
            for (int i = tot; i < tot + k && i < firstEmpty; i++) {
                st += elements[i] + "(" + values[elements[i]] + ")\t";
            }
            tot += k;
            k *= 2;
            s += st;
        }
        return s;
    }

    public void clear() {
        while (firstEmpty > 0) {
            firstEmpty--;
            positions[elements[firstEmpty]] = -1;
            elements[firstEmpty] = -1;
        }
    }
}
