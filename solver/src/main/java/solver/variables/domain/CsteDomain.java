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

package solver.variables.domain;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.OneValueIterator;
import solver.exception.SolverException;
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.NoDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public class CsteDomain implements IIntDomain {

    /**
     * indicate the value of the domain : false = 0, true = 1
     */
    protected int value;

    IntDelta delta = NoDelta.singleton;

    public CsteDomain(int value) {
        this.value = value;
    }

    @Override
    public int getLB() {
        return value;
    }

    @Override
    public int getUB() {
        return value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean empty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override
    public boolean contains(int aValue) {
        return value == aValue;
    }

    @Override
    public boolean hasNextValue(int aValue) {
        return aValue < value;
    }

    @Override
    public int nextValue(int aValue) {
        final int val = value;
        return (val > aValue) ? val : Integer.MAX_VALUE;
    }

    @Override
    public boolean hasPreviousValue(int aValue) {
        return aValue > value;
    }

    @Override
    public int previousValue(int aValue) {
        if (aValue > getUB()) return getUB();
        if (aValue > getLB()) return getLB();
        return Integer.MIN_VALUE;
    }

    @Override
    public DisposableIntIterator getIterator() {
        return OneValueIterator.getIterator(value);
    }

    @Override
    public boolean restrict(int aValue) {
        throw new SolverException("Unexpected call of restrict");
    }

    @Override
    public boolean restrictAndUpdateDelta(int aValue) {
        throw new SolverException("Unexpected call of restrictAndUpdateDelta");
    }

    @Override
    public boolean updateLowerBound(int aValue) {
        throw new SolverException("Unexpected call of updateInf");
    }

    @Override
    public boolean updateLowerBoundAndDelta(int aValue) {
        throw new SolverException("Unexpected call of updateInf");
    }

    @Override
    public boolean updateUpperBound(int aValue) {
        throw new SolverException("Unexpected call of updateInf");
    }

    @Override
    public boolean updateUpperBoundAndDelta(int aValue) {
        throw new SolverException("Unexpected call of updateInf");
    }

    @Override
    public boolean remove(int aValue) {
        throw new SolverException("Unexpected call of remove");
    }

    @Override
    public boolean removeAndUpdateDelta(int aValue) {
        throw new SolverException("Unexpected call of remove");
    }

    @Override
    public boolean isEnumerated() {
        return true;
    }

    @Override
    public IntDelta getDelta() {
        return delta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordRemoveValues() {
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
