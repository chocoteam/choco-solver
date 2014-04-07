/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.variables.view;


import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomBitset;
import solver.explanations.antidom.AntiDomain;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IDelta;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;
import solver.variables.impl.AbstractVariable;
import util.iterators.DisposableRangeBoundIterator;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueBoundIterator;
import util.iterators.DisposableValueIterator;

/**
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view executes the appropriate operation on the view's varaible."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class IntView extends AbstractVariable implements IView, IntVar {

    protected final IntVar var;

    protected IntDelta delta;

    protected DisposableValueIterator _viterator;

    protected DisposableRangeIterator _riterator;

    public IntView(String name, IntVar var, Solver solver) {
        super(name, solver);
        this.var = var;
        this.delta = NoDelta.singleton;
        this.var.subscribeView(this);
    }

    @Override
    public final void recordMask(int mask) {
        super.recordMask(mask);
        var.recordMask(mask);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.INT;
    }

	@Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

	@Override
    public IDelta getDelta() {
        return var.getDelta();
    }

    @Override
    public void createDelta() {
        var.createDelta();
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

	@Override
    public void notifyMonitors(EventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void transformEvent(EventType evt, ICause cause) throws ContradictionException {
        notifyPropagators(evt, this);
    }

    @Override
    public void explain(VariableState what, Explanation to) {
        var.explain(what, to);
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        var.explain(VariableState.DOM, e);
    }

    @Override
    public AntiDomain antiDomain() {
        return new AntiDomBitset(this);
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        assert cause != null;
//        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }


    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueBoundIterator(this);
        }
        if (bottomUp) {
            _viterator.bottomUpInit();
        } else {
            _viterator.topDownInit();
        }
        return _viterator;
    }

	@Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeBoundIterator(this);
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }

    @Override
    public void wipeOut(ICause cause) throws ContradictionException {
        assert cause != null;
        var.wipeOut(cause);
    }
}
