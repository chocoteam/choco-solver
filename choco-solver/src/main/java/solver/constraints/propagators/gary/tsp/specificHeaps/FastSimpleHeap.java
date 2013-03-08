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


package solver.constraints.propagators.gary.tsp.specificHeaps;

import java.util.BitSet;

/**
 * Fast heap which stores in a priority stack elements
 * of value Integer.MIN_VALUE (lower values are forbidden)
 * Created by IntelliJ IDEA.
 *
 * @author Jean-Guillaume Fages
 * @since 18/11/12
 */
public class FastSimpleHeap implements ISimpleHeap {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private BitSet inBest;
    private BitSet obsolete;
    private int[] stack;
    private int k;
    private ISimpleHeap heap_rest;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public FastSimpleHeap(int n) {
        this(n, new BinarySimpleHeap(n));
    }

    public FastSimpleHeap(int n, ISimpleHeap heap) {
        inBest = new BitSet(n);
        obsolete = new BitSet(n);
        stack = new int[n];
        k = 0;
        heap_rest = heap;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    public boolean isEmpty() {
        return k == 0 && heap_rest.isEmpty();
    }

    public int removeFirstElement() {
        if (k == 0) {
            int f;
            do {
                f = heap_rest.removeFirstElement();
            } while (f != -1 && obsolete.get(f));
            return f;
        }
        int first = stack[--k];
        inBest.clear(first);
        return first;
    }

    public boolean addOrUpdateElement(int element, double value) {
        if (value < Integer.MIN_VALUE)
            throw new UnsupportedOperationException("cannot use a FastSimpleHeap on such data");
        if (inBest.get(element)) {
            return false;
        }
        if (value == Double.MIN_VALUE) {
            inBest.set(element);
            stack[k++] = element;
            obsolete.set(element);
            return true;
        }
        return heap_rest.addOrUpdateElement(element, value);
    }

    public void clear() {
        k = 0;
        inBest.clear();
        obsolete.clear();
        heap_rest.clear();
    }
}
