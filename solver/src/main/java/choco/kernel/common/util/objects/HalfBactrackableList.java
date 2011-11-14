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

import choco.kernel.common.IIndex;
import choco.kernel.common.util.procedure.Procedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/11/11
 */
public class HalfBactrackableList<E extends IIndex> {

    private static final int OFFSET = 100000;
    private static final int SIZE = 8;


    protected int sIdx;  // index of static objects
    protected int sFirstActive;
    protected int sFirstPassive;
    protected final IStateInt dIdx; // index of dynamic objects
    protected final IStateInt dFirstActive;
    protected final IStateInt dFirstPassive;
    protected int world; // world of first adding in dynamic structure

    protected E[] sElements, dElements; // null at creation


    public HalfBactrackableList(IEnvironment environment) {
        sIdx = sFirstActive = sFirstPassive = 0;
        dIdx = environment.makeInt();
        dFirstActive = environment.makeInt();
        dFirstPassive = environment.makeInt();
    }

    public void setActive(E element) {
        if (element.getIndex() < OFFSET) {
            sActivate(element);
        } else {
            dActivate(element);
        }
    }

    private void sActivate(E element) {
        int first = this.sFirstActive;
        int i = element.getIndex();
        if (first > i) {
            // swap element at pos "first" with element at pos "i"
            E tmp1 = sElements[--first];
            sElements[first] = sElements[i];
            sElements[first].setIndex(first);
            sElements[i] = tmp1;
            sElements[i].setIndex(i);
        }
        if (first < i) {
            throw new UnsupportedOperationException("Cannot reactivate " + element);
        }
        sFirstActive++;
    }

    private void dActivate(E element) {
        int first = this.dFirstActive.get();
        int i = element.getIndex() - OFFSET;
        if (first > i) {
            // swap element at pos "first" with element at pos "i"
            E tmp1 = dElements[--first];
            dElements[first] = dElements[i];
            dElements[first].setIndex(first + OFFSET);
            dElements[i] = tmp1;
            dElements[i].setIndex(i + OFFSET);
        }
        if (first < i) {
            throw new UnsupportedOperationException("Cannot reactivate " + element);
        }
        dFirstActive.add(-1);
    }


    public void setPassive(E element) {
        if (element.getIndex() < OFFSET) {
            sPassivate(element);
        } else {
            dPassivate(element);
        }
    }

    private void sPassivate(E element) {
        int last = this.sFirstPassive;
        int i = element.getIndex();
        if (last > i) {
            // swap element at pos "last" with element at pos "i"
            E tmp1 = sElements[--last];
            sElements[last] = sElements[i];
            sElements[last].setIndex(last);
            sElements[i] = tmp1;
            sElements[i].setIndex(i);
        }
        sFirstPassive++;

    }

    public void dPassivate(E element) {
        int last = this.dFirstPassive.get();
        int i = element.getIndex() - OFFSET;
        if (last > i) {
            // swap element at pos "last" with element at pos "i"
            E tmp1 = dElements[--last];
            dElements[last] = dElements[i];
            dElements[last].setIndex(last + OFFSET);
            dElements[i] = tmp1;
            dElements[i].setIndex(i + OFFSET);
        }
        dFirstPassive.add(-1);
    }


    public void addStatic(E element) {
        if (sElements == null) {
            sElements = (E[]) new IIndex[SIZE];
        }
        if (sElements.length >= sIdx) {
            E[] tmp = sElements;
            sElements = (E[]) new IIndex[3 / 2 * SIZE + 1];
            System.arraycopy(tmp, 0, sElements, 0, sIdx);
        }
        element.setIndex(sIdx);
        sElements[sIdx++] = element;
        this.sFirstActive++;
        this.sFirstPassive++;
    }

    public void addDynamic(E element) {
        if (dElements == null) {
            dElements = (E[]) new IIndex[SIZE];
            world = dIdx.getEnvironment().getWorldIndex();
        }
        int idx = dIdx.get();
        if (dElements.length >= idx) {
            E[] tmp = dElements;
            dElements = (E[]) new IIndex[3 / 2 * SIZE + 1];
            System.arraycopy(tmp, 0, dElements, 0, idx);
        }
        element.setIndex(idx + OFFSET);
        dElements[idx++] = element;
        dIdx.add(1);
        this.dFirstActive.add(1);
        this.dFirstPassive.add(1);
    }

    public void remove(E element) {
        if (element.getIndex() < OFFSET) {

        } else {

        }
    }

    private void sRemove(E e) {
        int i = e.getIndex();
        E[] tmp = sElements;
        sElements = (E[]) new IIndex[tmp.length - 1];
        System.arraycopy(tmp, 0, sElements, 0, i);
        System.arraycopy(tmp, i + 1, sElements, i, tmp.length - i - 1);
        for (int j = i; j < sElements.length; j++) {
            sElements[j].setIndex(j);
        }
        if (i < sFirstActive) {
            this.sFirstActive--;
        }
        this.sFirstPassive--;
    }

    private void dRemove(E e) {
        int i = e.getIndex() - OFFSET;
        E[] tmp = dElements;
        dElements = (E[]) new IIndex[tmp.length - 1];
        System.arraycopy(tmp, 0, dElements, 0, i);
        System.arraycopy(tmp, i + 1, dElements, i, tmp.length - i - 1);
        for (int j = i; j < dElements.length; j++) {
            dElements[j].setIndex(j + OFFSET);
        }
        assert (this.dFirstPassive.getEnvironment().getWorldIndex() == world);
        if (i < dFirstActive.get()) {
            this.dFirstActive.add(-1);
        }
        this.dFirstPassive.add(-1);
    }


    public void forEach(Procedure proc) throws ContradictionException {
        for (int i = 0; i < sIdx; i++) {
            proc.execute(sElements[i]);
        }
        int idx = dIdx.get();
        for (int i = 0; i < idx; i++) {
            proc.execute(dElements[i]);
        }
    }
}
