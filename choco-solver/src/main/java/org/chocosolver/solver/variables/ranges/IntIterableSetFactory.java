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
     * @return return the intersection of set1 and set2
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
}
