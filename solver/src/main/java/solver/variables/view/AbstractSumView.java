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
package solver.variables.view;

import choco.kernel.memory.IStateInt;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.propagation.engines.IPropagationEngine;
import solver.requests.IRequest;
import solver.requests.PropRequest;
import solver.requests.list.IRequestList;
import solver.requests.list.RequestListBuilder;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;

import java.io.Serializable;

import static solver.variables.AbstractVariable.MSG_EMPTY;

/**
 * An abstract view for SumView, to avoid duplicate method
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public abstract class AbstractSumView implements IntVar, IView, Serializable {

    final IntVar A, B;

    final IStateInt LB, UB;

    IStateInt SIZE;

    protected final Solver solver;

    protected final IRequestList<IRequest> requests;

    protected final IPropagationEngine engine;

    public AbstractSumView(IntVar a, IntVar b, Solver solver) {
        this.A = a;
        this.B = b;
        this.solver = solver;
        this.engine = solver.getEngine();
        this.requests = RequestListBuilder.preset(solver.getEnvironment());
        int lbA = A.getLB();
        int ubA = A.getUB();
        int lbB = B.getLB();
        int ubB = B.getUB();
        int min = lbA + lbB;
        int max = ubA + ubB;
        this.LB = solver.getEnvironment().makeInt(min);
        this.UB = solver.getEnvironment().makeInt(max);
        this.SIZE = solver.getEnvironment().makeInt(max - min + 1);

        A.subscribeView(this);
        B.subscribeView(this);
    }

    /////////////// SERVICES REQUIRED FROM INTVAR //////////////////////////

    public int getUniqueID() {
        throw new UnsupportedOperationException();
    }

    public void setUniqueID(int uniqueID) {
        throw new UnsupportedOperationException();
    }

    public void updateEntailment(IRequest request) {
        requests.setPassive(request);
    }

    public String getName() {
        return String.format("(%s + %s)", A, B);
    }

    public void addRequest(IRequest request) {
        requests.addRequest(request);
    }

    public void deleteRequest(IRequest request) {
        requests.deleteRequest(request);
    }

    public IRequestList getRequests() {
        return requests;
    }

    public void subscribeView(IView view) {
        A.subscribeView(view);
        B.subscribeView(view);
    }

    public int nbRequests() {
        return requests.cardinality();
    }

    public int nbConstraints() {
        return requests.size();
    }

    public Explanation explain() {
        throw new UnsupportedOperationException("ViewSumXY::can not be explained");
    }

    public IntDelta getDelta() {
        return NoDelta.singleton;
    }

    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
    }

    public void deletePropagator(Propagator observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        PropRequest<AbstractSumView, Propagator<AbstractSumView>> request =
                new PropRequest<AbstractSumView, Propagator<AbstractSumView>>(propagator, this, idxInProp);
        propagator.addRequest(request);
        this.addRequest(request);
    }


    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        engine.fails(cause, this, message);
    }

    @Override
    public boolean instantiated() {
        return SIZE.get() == 1;
    }

    @Override
    public boolean instantiatedTo(int aValue) {
        return getLB() == aValue && getUB() == aValue;
    }

    @Override
    public int getValue() {
        return getLB();
    }

    @Override
    public int getLB() {
        return LB.get();
    }

    @Override
    public int getUB() {
        return UB.get();
    }

    @Override
    public int getDomainSize() {
        return SIZE.get();
    }


    public void notifyPropagators(EventType e, ICause o) {
        requests.notifyButCause(o, e, getDelta());
    }

    @Override
    public void notifyViews(EventType e) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public Solver getSolver() {
        return solver;
    }

    public int getType() {
        return Variable.INTEGER;
    }

    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //TODO: allow branching
        throw new UnsupportedOperationException("IntVarAbs#setHeuristicVal: wrong usage");
    }

    public HeuristicVal getHeuristicVal() {
        throw new UnsupportedOperationException();
    }

    /////////////// SERVICES REQUIRED FROM SUM //////////////////////////

    void filterOnLeq(ICause cause, int ub) throws ContradictionException {
        int lbA = A.getLB(), lbB = B.getLB();
        int sumLB = lbA + lbB - ub;
        if (-sumLB < 0) contradiction(cause, MSG_EMPTY);
        int ubA = A.getUB(), ubB = B.getUB();
        if (ubA - lbA + sumLB > 0) {
            A.updateUpperBound(-sumLB + lbA, cause);
        }
        if (ubB - lbB + sumLB > 0) {
            B.updateUpperBound(-sumLB + lbB, cause);
        }
        //TODO: back propager?
    }

    void filterOnGeq(ICause cause, int lb) throws ContradictionException {
        int ubA = A.getUB(), ubB = B.getUB();
        int sumUB = ubA + ubB - lb;
        if (-sumUB > 0) contradiction(cause, MSG_EMPTY);
        int lbA = A.getLB(), lbB = B.getLB();
        if (ubA - lbA - sumUB > 0) {
            A.updateLowerBound(-sumUB + ubA, cause);
        }
        if (ubB - lbB - sumUB > 0) {
            B.updateLowerBound(-sumUB + ubB, cause);
        }
        //TODO: back propager?
    }

}
