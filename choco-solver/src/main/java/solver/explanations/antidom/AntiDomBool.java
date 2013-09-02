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
package solver.explanations.antidom;

import memory.structure.IndexedBipartiteSet;
import solver.variables.fast.BooleanBoolVarImpl;
import util.iterators.DisposableValueIterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/12/12
 */
public class AntiDomBool implements AntiDomain {

    /**
     * The offset, that is the minimal value of the domain (stored at index 0).
     * Thus the entry at index i corresponds to x=i+offset).
     */
    protected final int offset;

    /**
     * indicate the value of the domain : false = 0, true = 1
     */
    protected int mValue;

    /**
     * A bi partite set indicating for each value whether it is present or not.
     * If the set contains the domain, the variable is not instanciated.
     */
    protected final IndexedBipartiteSet notInstanciated;

    private DisposableValueIterator _viterator;

    public AntiDomBool(BooleanBoolVarImpl var) {
        notInstanciated = var.getSolver().getEnvironment().getSharedBipartiteSetForBooleanVars();
        this.offset = var.getSolver().getEnvironment().getNextOffset();
        mValue = 0;
    }

    @Override
    public void add(int outsideval) {
        if (outsideval == 0 || outsideval == 1) {
            notInstanciated.remove(offset);
            mValue = outsideval;
        }
    }

    @Override
    public void updateLowerBound(int oldLB, int newLB) {
        for (int i = oldLB; i < newLB; i++) {
            add(i);
        }
    }

    @Override
    public void updateUpperBound(int oldUB, int newUB) {
        for (int i = oldUB; i > newUB; i--) {
            add(i);
        }
    }

    @Override
    public boolean get(int outsideval) {
        return (!notInstanciated.contains(offset) && mValue == outsideval);
    }

    @Override
    public int getKeyValue(int outsideval) {
        return outsideval;
    }

    @Override
    public int size() {
        return notInstanciated.contains(offset) ? 0 : 1;
    }

    @Override
    public DisposableValueIterator getValueIterator() {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                boolean next;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    next = !notInstanciated.contains(offset);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    next = !notInstanciated.contains(offset);
                }

                @Override
                public boolean hasNext() {
                    return next;
                }

                @Override
                public boolean hasPrevious() {
                    return next;
                }

                @Override
                public int next() {
                    next = false;
                    return mValue;
                }

                @Override
                public int previous() {
                    next = false;
                    return mValue;
                }
            };
        }
        _viterator.bottomUpInit();
        return _viterator;
    }

    @Override
    public boolean isEnumerated() {
        return true;
    }
}
