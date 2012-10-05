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
package solver.propagation.queues;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/03/12
 */
public class DoubleMinHeap {

    private static final boolean PRINT = false;

    private double[] keys;
    private int[] elts;
    private int[] posOf;
    private int size;

    public DoubleMinHeap(int max) {
        keys = new double[max + 1];
        elts = new int[max + 1];
        posOf = new int[max + 1];
        size = 0;
        keys[0] = Integer.MIN_VALUE;
        elts[0] = Integer.MIN_VALUE;
    }

    public void clear() {
        if (PRINT) System.out.printf("xxxxx clear xxxxx");
        size = 0;
    }

    public int size() {
        return size - 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private int leftchild(int pos) {
        return pos << 1;
    }

    private int rightchild(int pos) {
        return pos << 1 + 1;
    }

    private int parent(int pos) {
        return pos >> 1;
    }

    private boolean isleaf(int pos) {
        return ((pos > size >> 1) && (pos <= size));
    }

    private void swap(int pos1, int pos2) {
        double dtmp;

        dtmp = keys[pos1];
        keys[pos1] = keys[pos2];
        keys[pos2] = dtmp;

        posOf[elts[pos1]] = pos2;
        posOf[elts[pos2]] = pos1;

        int tmp = elts[pos1];
        elts[pos1] = elts[pos2];
        elts[pos2] = tmp;
    }

    public void insert(double key, int elem) {
        if (PRINT) System.out.printf("<< INSERT (%.3f, %d)\n", key, elem);
        if (PRINT) print();
        if (size == keys.length - 1 || elem > keys.length - 1) {
            doubleCapacity(elem);
        }
        size++;
        int current = size;
        int parent = parent(current);
        while (current > 0 && keys[parent] > key) {
            keys[current] = keys[parent];
            elts[current] = elts[parent];
            posOf[elts[current]] = current;
            current = parent;
            parent = parent(current);
        }
        keys[current] = key;
        elts[current] = elem;
        posOf[elem] = current;
        if (PRINT) print();
        if (PRINT) System.out.printf(">>>>>>>>\n");
    }

    private void doubleCapacity(int elem) {
        if (PRINT) System.out.printf("DOUBLE CAPA\n");
        int csize = Math.max(keys.length * 2 + 1, elem + 1);
        double[] dtmp = keys;
        keys = new double[csize];
        System.arraycopy(dtmp, 0, keys, 0, size + 1);
        int[] tmp = elts;
        elts = new int[csize];
        System.arraycopy(tmp, 0, elts, 0, size + 1);
        tmp = posOf;
        posOf = new int[csize];
        System.arraycopy(tmp, 0, posOf, 0, tmp.length);
    }


    public void update(double new_value, int elem) {
        if (PRINT) System.out.printf("<< UPDATE (%.3f, %d)\n", new_value, elem);
        if (PRINT) print();
        int current = posOf[elem];//indexOf(elem);
        double amount = new_value - keys[current];
        keys[current] = new_value;
        if (amount < 0) {
            pushup(current);
        } else if (amount > 0) {
            pushdown(current);
        }
        if (PRINT) print();
        if (PRINT) System.out.printf("+++++++++>\n");
    }

    public void print() {
        int i;
        for (i = 1; i <= size; i++)
            System.out.printf("(%.3f, %s) ", keys[i], elts[i]);
        System.out.println();
    }

    public int removemin() {
        if (PRINT) System.out.printf("<< REM MIN\n");
        if (PRINT) print();
        swap(1, size);
        size--;
        if (size != 0)
            pushdown(1);
        if (PRINT) print();
        if (PRINT) System.out.printf("------->\n");
        return elts[size + 1];
    }

    public int remove(int elem) {
        if (PRINT) System.out.printf("<< REMOVE (%d)\n", elem);
        if (PRINT) print();
        int idx = posOf[elem];//indexOf(elem);
        swap(idx, size);
        size--;
        if (size != 0 && idx < size)
            pushdown(idx);
        if (PRINT) print();
        if (PRINT) System.out.printf("- - - - ->\n");
        return elts[size + 1];
    }

    private void pushdown(int position) {
        int smallestchild;
        while (!isleaf(position)) {
            smallestchild = leftchild(position);
            if ((smallestchild < size) && (keys[smallestchild] > keys[smallestchild + 1]))
                smallestchild = smallestchild + 1;
            if (smallestchild > size || keys[position] <= keys[smallestchild]) return;
            swap(position, smallestchild);
            position = smallestchild;
        }
    }

    private void pushup(int position) {
        while (keys[position] < keys[parent(position)]) {
            swap(position, parent(position));
            position = parent(position);
        }
    }

    public static void main(String[] args) {
        DoubleMinHeap mh = new DoubleMinHeap(10);
        for (int i = 1; i < 9; i++) {
            mh.insert(i, i);
        }
        mh.print();
        mh.remove(2);
        mh.print();
        mh.remove(5);
        mh.print();
        mh.remove(8);
        mh.print();
        while (!mh.isEmpty()) {
            System.out.printf("%d\n", mh.removemin());
            mh.print();
        }
    }
}
