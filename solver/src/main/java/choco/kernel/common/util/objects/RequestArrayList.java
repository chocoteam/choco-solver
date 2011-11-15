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

package choco.kernel.common.util.objects;

import choco.kernel.common.MultiDimensionIndex;
import choco.kernel.common.util.procedure.Procedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.exception.ContradictionException;

/**
 * [<--inactive-->|<--active--->|<---entailed-->]<br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public final class RequestArrayList<E extends MultiDimensionIndex> implements IList<E> {

    protected E[] elements;

    protected IStateInt firstActive;
    protected IStateInt firstPassive;

    protected final int DIMENSION;

    public RequestArrayList(IEnvironment environment, int dim) {
        elements = (E[]) new MultiDimensionIndex[0];
        firstActive = environment.makeInt();
        firstPassive = environment.makeInt();
        this.DIMENSION = dim;
    }

    @Override
    public void setActive(E element) {
        int first = this.firstActive.get();
        int i = element.getIndex(DIMENSION);
        if (first > i) {
            // swap element at pos "first" with element at pos "i"
            E tmp1 = elements[--first];
            elements[first] = elements[i];
            elements[first].setIndex(DIMENSION, first);
            elements[i] = tmp1;
            elements[i].setIndex(DIMENSION, i);
        }
        if (first < i) {
            throw new UnsupportedOperationException("Cannot reactivate a request");
        }
        firstActive.add(-1);
    }

    @Override
    public void setPassive(E element) {
        int last = this.firstPassive.get();
        int i = element.getIndex(DIMENSION);
        if (last > i) {
            // swap element at pos "last" with element at pos "i"
            E tmp1 = elements[--last];
            elements[last] = elements[i];
            elements[last].setIndex(DIMENSION, last);
            elements[i] = tmp1;
            elements[i].setIndex(DIMENSION, i);
        }
        firstPassive.add(-1);
    }


    @Override
    public void add(E element, boolean dynamic) {
        if (firstActive.get() != firstPassive.get()) {
            throw new UnsupportedOperationException("Can not add an element: activation has already started");
        }
        E[] tmp = elements;
        elements = (E[]) new MultiDimensionIndex[tmp.length + 1];
        System.arraycopy(tmp, 0, elements, 0, tmp.length);
        elements[tmp.length] = element;
        element.setIndex(DIMENSION, tmp.length);
        this.firstActive.add(1);
        this.firstPassive.add(1);
    }

    @Override
    public void remove(E element) {
        int i = 0;
        for (; i < elements.length && elements[i] != element; i++) {
        }
        if (i == elements.length) return;
        E[] tmp = elements;
        elements = (E[]) new MultiDimensionIndex[tmp.length - 1];
        System.arraycopy(tmp, 0, elements, 0, i);
        System.arraycopy(tmp, i + 1, elements, i, tmp.length - i - 1);
        for (int j = i; j < elements.length; j++) {
            elements[j].setIndex(DIMENSION, j);
        }
        assert (this.firstPassive.getEnvironment().getWorldIndex() == 0);
        if (i < firstActive.get()) {
            this.firstActive.add(-1);
        }
        this.firstPassive.add(-1);
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public int cardinality() {
        return firstPassive.get();
    }

    @Override
    public E get(int i) {
        return elements[i];
    }

    @Override
    public void forEach(Procedure proc) throws ContradictionException {
        int first = firstActive.get();
        int last = firstPassive.get();
        for (int a = first; a < last; a++) {
            proc.execute(elements[a]);
        }
    }


}
