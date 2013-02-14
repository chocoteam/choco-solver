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

package solver.search.strategy.enumerations.sorters;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/02/11
 */
public abstract class AbstractSorter<E> implements Comparator<E>, Serializable {

    /**
     * sort <code>elements</code> from the index <code>from</code> to the index <code>to</code> using <code>this</code>
     * as a comparator.
     *
     * @param elements array of elements to sort
     * @param from     from index (included)
     * @param to       to index (included)
     * @return new "to" index
     */
    public int sort(E[] elements, int from, int to) {
        Arrays.sort(elements, from, to + 1, this);
        // compute the last index of the first sub list
        for (int i = from; i < to; i++) {
            if (compare(elements[i], elements[i + 1]) != 0) {
                return i;
            }
        }
        return to;
    }


    /**
     * Swap minimum elements of <code>elements</code> at the beginning of the sub array
     * defined by <code>from</code> and <code>to</code>
     *
     * @param elements array of elements to sort
     * @param from     from index (included)
     * @param to       to index (included)
     * @return index of the last minimum element
     */
    public int minima(E[] elements, int from, int to) {
        E minElt = elements[from];
        int minIdx = from;
        boolean unique = true;
        for (int i = from + 1; i <= to; i++) {
            int comp = compare(minElt, elements[i]);
            if (comp == 0) {
                unique = false;
            } else if (comp > 0) {
                unique = true;
                minIdx = i;
                minElt = elements[i];
            }
        }
        swap(elements, from, minIdx);
        int idx = from + 1;
        if (!unique) {
            for (int i = from + 1; i <= to; i++) {
                if (compare(minElt, elements[i]) == 0) {
                    swap(elements, i, idx++);
                }
            }
        }
        return idx - 1;
    }

    protected static <E> void swap(E[] elements, int i, int j) {
        E tmp = elements[i];
        elements[i] = elements[j];
        elements[j] = tmp;
    }

}
