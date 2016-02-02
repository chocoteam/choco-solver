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
package org.chocosolver.solver.variables.ranges;

import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 25/01/2016.
 */
public class IntIterableSetFactory {

    /**
     * TODO: more efficient operation
     *
     * @param var an integer variable
     * @param set set to transfer values to
     */
    public static void dvar_set(IntVar var, IntIterableRangeSet set) {
        set.clear();
        fd_union(set, var);
    }

    /**
     * TODO: more efficient operation
     *
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return the union of set and set2
     */
    public static IntIterableRangeSet fd_union(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t;
        if (set1.size() < set2.size()) {
            t = (IntIterableRangeSet) set1.duplicate();
            t.addAll(set2);
        } else {
            t = (IntIterableRangeSet) set2.duplicate();
            t.addAll(set1);
        }
        return t;
    }

    /**
     * TODO: more efficient operation
     *
     * @param set a set of ints
     * @param var a integer variable
     */
    public static void fd_union(IntIterableRangeSet set, IntVar var) {
        int ub = var.getUB();
        for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
            set.add(v);
        }
    }


    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set which is the intersection of set1 and set2
     */
    public static IntIterableRangeSet fd_intersection(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t;
        if (set1.size() < set2.size()) {
            t = (IntIterableRangeSet) set1.duplicate();
            t.retainAll(set2);
        } else {
            t = (IntIterableRangeSet) set2.duplicate();
            t.retainAll(set1);
        }
        return t;
    }


    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set {a + b | a in set1, b in set2}
     */
    public static IntIterableRangeSet plus(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        t.ELEMENTS = new int[set1.SIZE * set2.SIZE];
        int i = 0, j = 0;
        int[] is = new int[s2];
        Arrays.fill(is, -1);
        is[0] = 0;
        int lb = set1.ELEMENTS[0] + set2.ELEMENTS[0];
        int ub = set1.ELEMENTS[1] + set2.ELEMENTS[1];
        do {
            boolean extend = false;
            for (int k = i; k <= j; k++) {
                int _lb = set1.ELEMENTS[is[k] << 1] + set2.ELEMENTS[k << 1];
                if (lb <= _lb && _lb <= ub + 1) {
                    ub = Math.max(set1.ELEMENTS[(is[k] << 1) + 1] + set2.ELEMENTS[(k << 1) + 1], ub);
                    extend = true;
                    // add neighbors to evaluate
                    // 1. left neighbor
                    if (k < s2 - 1 && k == j) {
                        is[k + 1]++;
                        if (is[k + 1] == 0) {
                            j++;
                        }
                    }
                    // 2. bottom neighbor
                    is[k]++;
                    if (is[k] == s1) {
                        i++;
                    }
                }
            }
            if (!extend) {
                t.ELEMENTS[t.SIZE++] = lb;
                t.ELEMENTS[t.SIZE++] = ub;
                t.CARDINALITY += ub - lb + 1;
                lb = Integer.MAX_VALUE;
                for (int k = i; k <= j; k++) {
                    int _lb = set1.ELEMENTS[is[k] << 1] + set2.ELEMENTS[k << 1];
                    if (lb > _lb) {
                        lb = _lb;
                        ub = set1.ELEMENTS[(is[k] << 1) + 1] + set2.ELEMENTS[(k << 1) + 1];
                    }
                }
            }
        } while (is[s2 - 1] < s1);
        t.ELEMENTS[t.SIZE++] = lb;
        t.ELEMENTS[t.SIZE++] = ub;
        t.CARDINALITY += ub - lb + 1;
        return t;
    }


    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set {a - b | a in set1, b in set2}
     */
    public static IntIterableRangeSet minus(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        t.ELEMENTS = new int[set1.SIZE * set2.SIZE];
        int i = s2 - 1, j = s2 - 1;
        int[] is = new int[s2];
        Arrays.fill(is, -1);
        is[s2 - 1] = 0;
        int lb = set1.ELEMENTS[0] - set2.ELEMENTS[((s2 - 1) << 1) + 1];
        int ub = set1.ELEMENTS[1] - set2.ELEMENTS[(s2 - 1) << 1];
        do {
            boolean extend = false;
            for (int k = j; k >= i; k--) {
                int _lb = set1.ELEMENTS[is[k] << 1] - set2.ELEMENTS[(k << 1) + 1];
                if (lb <= _lb && _lb <= ub + 1) {
                    ub = Math.max(set1.ELEMENTS[(is[k] << 1) + 1] - set2.ELEMENTS[(k << 1)], ub);
                    extend = true;
                    // add neighbors to evaluate
                    // 1. left neighbor
                    if (k > 0 && k == i) {
                        is[k - 1]++;
                        if (is[k - 1] == 0) {
                            i--;
                        }
                    }
                    // 2. bottom neighbor
                    is[k]++;
                    if (is[k] == s1) {
                        j--;
                    }
                }
            }
            if (!extend) {
                t.ELEMENTS[t.SIZE++] = lb;
                t.ELEMENTS[t.SIZE++] = ub;
                t.CARDINALITY += ub - lb + 1;
                lb = Integer.MAX_VALUE;
                for (int k = i; k <= j; k++) {
                    int _lb = set1.ELEMENTS[is[k] << 1] - set2.ELEMENTS[(k << 1) + 1];
                    if (lb > _lb) {
                        lb = _lb;
                        ub = set1.ELEMENTS[(is[k] << 1) + 1] - set2.ELEMENTS[k << 1];
                    }
                }
            }
        } while (is[0] < s1);
        t.ELEMENTS[t.SIZE++] = lb;
        t.ELEMENTS[t.SIZE++] = ub;
        t.CARDINALITY += ub - lb + 1;
        return t;
    }


}
