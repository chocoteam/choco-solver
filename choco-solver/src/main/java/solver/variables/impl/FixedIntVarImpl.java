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

package solver.variables.impl;

import memory.IStateBool;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomBitset;
import solver.explanations.antidom.AntiDomain;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.view.IView;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;

/**
 * A IntVar with one domain value.
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public class FixedIntVarImpl extends AbstractVariable implements IntVar {

	private static final long serialVersionUID = 1L;
    protected final int constante;
    protected IStateBool empty;
    private DisposableValueIterator _viterator;
    private DisposableRangeIterator _riterator;

    public FixedIntVarImpl(String name, int constante, Solver solver) {
		super(name,solver);
        this.constante = constante;
        this.empty = solver.getEnvironment().makeBool(false);
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (value == constante) {
            assert cause != null;
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.REMOVE, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= constante && constante <= to) {
            assert cause != null;
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.REMOVE, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (value != constante) {
            assert cause != null;
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.INSTANTIATE, "outside domain instantitation");
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > constante) {
            assert cause != null;
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.INCLOW, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < constante) {
            assert cause != null;
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.DECUPP, "outside domain update bound");
        }
        return false;
    }

    @Override
    public void wipeOut(ICause cause) throws ContradictionException {
        removeValue(constante, cause);
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
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override//void (a constant receives no event)
    public void addMonitor(IVariableMonitor monitor) {}

    @Override
    public AntiDomain antiDomain() {
        return new AntiDomBitset(this);
    }

    @Override
    public void explain(VariableState what, Explanation to) {
        if (empty.get()) {
            to.add(solver.getExplainer().explain(this, constante));
        }
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        if (empty.get()) {
            to.add(solver.getExplainer().explain(this, constante));
        }
    }

    @Override//void (a constant receives no event)
    public void subscribeView(IView view) {}

    @Override//void (a constant receives no event)
    public void recordMask(int mask) {}

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        return IIntDeltaMonitor.Default.NONE;
    }

    @Override
    public void createDelta() {}

    @Override//void (a constant receives no event)
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {}

    @Override//void (a constant receives no event)
    public void notifyMonitors(EventType event) throws ContradictionException {}

    @Override//void (a constant receives no event)
    public void notifyViews(EventType event, ICause cause) throws ContradictionException {}

    @Override
    public String toString() {
        return name + "=" + String.valueOf(constante);
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        this.empty.set(true);
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.INT | Variable.CSTE;
    }

    @Override
    public IntVar duplicate() {
        throw new UnsupportedOperationException("Cannot duplicate a constant");
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                boolean _next;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    _next = true;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    _next = true;
                }

                @Override
                public boolean hasNext() {
                    return _next;
                }

                @Override
                public boolean hasPrevious() {
                    return _next;
                }

                @Override
                public int next() {
                    _next = false;
                    return constante;
                }

                @Override
                public int previous() {
                    _next = false;
                    return constante;
                }

            };
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
            _riterator = new DisposableRangeIterator() {
                boolean _next;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    _next = true;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    _next = true;
                }

                @Override
                public boolean hasNext() {
                    return _next;
                }

                @Override
                public boolean hasPrevious() {
                    return _next;
                }

                @Override
                public void next() {
                    _next = false;
                }

                @Override
                public void previous() {
                    _next = false;
                }

                @Override
                public int min() {
                    return constante;
                }

                @Override
                public int max() {
                    return constante;
                }
            };
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }
}
