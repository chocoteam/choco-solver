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

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.IndexedBipartiteSet;
import solver.exception.SolverException;
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.NoDelta;
import solver.variables.domain.delta.OneValueDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public class BooleanDomain implements IIntDomain {

    /**
     * The offset, that is the minimal value of the domain (stored at index 0).
     * Thus the entry at index i corresponds to x=i+offset).
     */

    protected final int offset;


    /**
     * indicate the value of the domain : false = 0, true = 1
     */
    protected int value;

    /**
     * A bi partite set indicating for each value whether it is present or not.
     * If the set contains the domain, the variable is not instanciated.
     */

    protected final IndexedBipartiteSet notInstanciated;

    IntDelta delta = NoDelta.singleton;

    public BooleanDomain(IEnvironment env) {
        notInstanciated = env.getSharedBipartiteSetForBooleanVars();
        this.offset = env.getNextOffset();
        value = 0;

    }

    @Override
    public int getLB() {
        if (!notInstanciated.contains(offset)) {
            return value;
        }
        return 0;
    }

    @Override
    public int getUB() {
        if (!notInstanciated.contains(offset)) {
            return value;
        }
        return 1;
    }

    @Override
    public int getSize() {
        return (notInstanciated.contains(offset) ? 2 : 1);
    }

    @Override
    public boolean empty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean instantiated() {
        return !notInstanciated.contains(offset);
    }

    @Override
    public boolean contains(int aValue) {
        if (!notInstanciated.contains(offset)) {
            return value == aValue;
        }
        return aValue == 0 || aValue == 1;
    }

    @Override
    public boolean hasNextValue(int aValue) {
        if (!notInstanciated.contains(offset)) {
            return aValue < value;
        }
        return aValue < 1;
    }

    @Override
    public int nextValue(int aValue) {
        if (!notInstanciated.contains(offset)) {
            final int val = value;
            return (val > aValue) ? val : Integer.MAX_VALUE;
        } else {
            if (aValue < 0) return 0;
            if (aValue == 0) return 1;
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean hasPreviousValue(int aValue) {
        if (!notInstanciated.contains(offset)) {
            return aValue > value;
        }
        return aValue > 0;
    }

    @Override
    public int previousValue(int aValue) {
        if (aValue > getUB()) return getUB();
        if (aValue > getLB()) return getLB();
        return Integer.MIN_VALUE;
    }


    @Override
    public boolean restrict(int aValue) {
        boolean change = notInstanciated.contains(aValue);
        notInstanciated.remove(offset);
        value = aValue;
        return change;
    }

    @Override
    public boolean restrictAndUpdateDelta(int aValue) {
        boolean change = notInstanciated.contains(aValue);
        notInstanciated.remove(offset);
        delta.add(1 - aValue);
        value = aValue;
        return change;
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
        delta = new OneValueDelta();
    }

    @Override
    public String toString() {
        if (!notInstanciated.contains(offset)) {
            return Integer.toString(value);
        } else {
            return "[0,1]";
        }
    }
}
