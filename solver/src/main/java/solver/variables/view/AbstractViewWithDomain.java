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

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.memory.IStateInt;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.Delta;

/**
 * An abstract view for SumView, to avoid duplicate methods
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public abstract class AbstractViewWithDomain extends AbstractView {

    final IntVar A, B;

    final IStateInt LB, UB, SIZE;

    protected DisposableValueIterator _viterator;

    protected DisposableRangeIterator _riterator;

    public AbstractViewWithDomain(IntVar a, IntVar b, Solver solver) {
        super(solver);
        this.A = a;
        this.B = b;
        this.LB = solver.getEnvironment().makeInt(0);
        this.UB = solver.getEnvironment().makeInt(0);
        this.SIZE = solver.getEnvironment().makeInt(0);

        A.subscribeView(this);
        B.subscribeView(this);
        this.makeList(this);
    }

    /////////////// SERVICES REQUIRED FROM INTVAR //////////////////////////

    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
        modificationEvents |= propagator.getPropagationConditions(idxInProp);
        if (!reactOnRemoval && ((modificationEvents & EventType.REMOVE.mask) != 0)) {
            delta = new Delta();
            reactOnRemoval = true;
        }
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

    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
        if ((modificationEvents & event.mask) != 0) {
            records.forEach(afterModification.set(this, event, cause));
        }
        notifyViews(event, cause);
    }

    @Override
    public Explanation explain(VariableState what) {
        Explanation explanation = new Explanation();
        explanation.add(A.explain(what));
        explanation.add(B.explain(what));
        return explanation;
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        Explanation explanation = new Explanation();
        explanation.add(A.explain(VariableState.DOM));
        explanation.add(B.explain(VariableState.DOM));
        return explanation;
    }

    @Override
    public Explanation explain(Deduction d) {
        Explanation explanation = A.explain(VariableState.DOM);
        explanation.add(B.explain(VariableState.DOM));
        return explanation;
        // return A.explain(VariableState.DOM).add(B.explain(VariableState.DOM));
        // throw new UnsupportedOperationException("AbstractView (as a cause)::can not be explained");
    }

}
