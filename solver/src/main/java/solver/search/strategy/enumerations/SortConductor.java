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

package solver.search.strategy.enumerations;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.validators.IValid;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/10
 */
public abstract class SortConductor<E> implements Iterator<E>, Serializable {

    public enum Type {
        STA, DYN
    }

    final E[] elements;

    final AbstractSorter<E> sorter;
    final IStateInt from;
    final int to;
    final IValid<E> vs;

    E current;


    public static <E> SortConductor dyn(E[] elements, AbstractSorter<E> sorter, IValid<E> vs, IEnvironment env) {
        return new Dynamic<E>(elements, sorter, vs, env);
    }

    public static <E> SortConductor sta(E[] elements, AbstractSorter<E> sorter, IValid<E> vs, IEnvironment env) {
        return new Static<E>(elements, sorter, vs, env);
    }

    protected SortConductor(E[] elements, AbstractSorter<E> sorter, IValid<E> vs, IEnvironment env) {
        this.elements = elements.clone();
        this.vs = vs;
        this.from = env.makeInt();
        this.to = elements.length;
        this.sorter = sorter;
    }

    abstract void filter();

    public boolean hasNext() {
        filter(); // filter may change from
        int _from = from.get();
        boolean isNotEmpty = (_from < to);
        if (isNotEmpty) {
            current = elements[_from];
        }
        return isNotEmpty;
    }

    public E next() {
        return current;
    }

    public final void remove() {
        throw new UnsupportedOperationException("SortConductor.remove() not implemented");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Dynamic<E> extends SortConductor<E> {

        Dynamic(E[] c, AbstractSorter<E> sorter, IValid<E> vs, IEnvironment env) {
            super(c, sorter, vs, env);
        }

        void filter() {
            int _from = from.get();
            for (int i = _from; i < to; i++) {
                if (!vs.valid(elements[i])) {
                    E tmp = elements[i];
                    elements[i] = elements[_from];
                    elements[_from] = tmp;
                    _from++;

                }
            }
            if (_from < to - 1) {
                sorter.minima(elements, _from, to - 1); // look for the first element
            }
            from.set(_from);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class Static<E> extends SortConductor<E> {

        Static(E[] elements, AbstractSorter<E> sorter, IValid<E> vs, IEnvironment env) {
            super(elements, sorter, vs, env);
            Arrays.sort(this.elements, 0, to, this.sorter);
        }

        @SuppressWarnings({"StatementWithEmptyBody"})
        void filter() {
            int _from = from.get();
            while (_from < to && !vs.valid(elements[_from])) {
                _from++;
            }
            from.set(_from);
        }
    }

}
