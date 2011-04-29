/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
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
package solver.search.strategy.enumerations;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.validators.IValid;

import java.util.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/10
 */
public class MyCollectionStatic<A> extends MyCollection<A> {

    LinkedList<Integer> indices = new LinkedList<Integer>();
    IStateInt from;
    final int to;
    IValid<A> vs;
    A current;

    public MyCollectionStatic(A[] c, LinkedList<AbstractSorter<A>> cs, IValid<A> vs, IEnvironment env) {
        super(c);
        indices.add(0);
        indices.add(c.length - 1);
        this.vs = vs;
        this.from = env.makeInt();
        this.to = c.length;
        sort(cs);
    }

    @SuppressWarnings({"StatementWithEmptyBody"})
    void filter() {
        int _from = from.get();
        while (_from < to && !vs.valid(elements[_from])) {
            _from++;
        }
        from.set(_from);
    }

    void sort(LinkedList<AbstractSorter<A>> cs) {
        for (Comparator<A> c : cs) {
            Iterator<Integer> iter = indices.iterator();
            // we could do better than copy the list of indexes
            LinkedList<Integer> tmp = new LinkedList<Integer>(indices);
            while (iter.hasNext()) {
                int from = iter.next();
                int to = iter.next();
                // sort each sublist
                Arrays.sort(elements, from, to + 1, c);
                // compute the indices where the sub list has been broken
                // two consecutive elements are not equal
                for (int i = from; i < to; i++) {
                    if (c.compare(elements[i], elements[i + 1]) != 0) {
                        tmp.add(i);
                        tmp.add(i + 1);
                    }
                }
                Collections.sort(tmp);
                indices = tmp;
            }
        }
    }

    public boolean hasNext() {
        filter(); // filter may change from !
        boolean isNotEmpty = (from.get() < elements.length);
        if (isNotEmpty) {
            current = elements[from.get()];
            //from.add(1);
        }
        return isNotEmpty;
    }

    public A next() {
        return current;
    }
}
