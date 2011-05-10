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

package solver.variables;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.OneValueIterator;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.requests.list.IRequestList;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.domain.IIntDomain;
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.NoDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class IntCste implements IntVar {

    protected final int constante;
    protected final String name;

    public IntCste(String name, int constante) {
        this.name = name;
        this.constante = constante;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (value == constante) {
            ContradictionException.throwIt(cause, this, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= constante && constante <= to) {
            ContradictionException.throwIt(cause, this, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (value != constante) {
            ContradictionException.throwIt(cause, this, "outside domain instantitation");
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > constante) {
            ContradictionException.throwIt(cause, this, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < constante) {
            ContradictionException.throwIt(cause, this, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return constante == value;
    }

    @Override
    public boolean instantiatedTo(int value) {
        return constante == value;
    }

    @Override
    public int getValue() {
        return constante;
    }

    @Override
    public int getLB() {
        return constante;
    }

    @Override
    public int getUB() {
        return constante;
    }

    @Override
    public IIntDomain getDomain() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDomainSize() {
        return 1;
    }

    @Override
    public int nextValue(int v) {
        if (v < constante) {
            return constante;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > constante) {
            return constante;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public IntDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public DisposableIntIterator getIterator() {
        return OneValueIterator.getIterator(constante);
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //useless
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        //useless
        return null;
    }

    @Override
    public void updateEntailment(IRequest request) {
        //useless
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addRequest(IRequest request) {
        //useless
    }

    @Override
    public void deleteRequest(IRequest request) {
        //useless
    }

    @Override
    public IRequestList getRequests() {
        return null;
    }

    @Override
    public int nbConstraints() {
        //who cares?
        return 0;
    }

    @Override
    public Explanation explain() {
        return null;
    }

    @Override
    public int nbRequests() {
        return 0;
    }

    @Override
    public void addPropagator(Propagator observer, int idxInProp) {
    }

    @Override
    public void deletePropagator(Propagator observer) {
    }

    @Override
    public void notifyPropagators(EventType eventType, ICause o) throws ContradictionException {
    }

    @Override
    public String toString() {
        return name + "=" + String.valueOf(constante);
    }
}
