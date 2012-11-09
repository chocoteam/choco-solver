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

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.memory.IStateBool;
import com.sun.istack.internal.NotNull;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.zeroary.Empty;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.domain.CsteDomain;
import solver.variables.domain.IIntDomain;

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
public class ConstantView implements IntVar {

    protected final int constante;
    protected final String name;
    protected final IIntDomain domain;
    protected final Solver solver;

    protected int ID;

    protected IStateBool empty;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    public ConstantView(String name, int constante, Solver solver) {
        this.name = name;
        this.solver = solver;
        this.constante = constante;
        this.domain = new CsteDomain(constante);
        this.empty = solver.getEnvironment().makeBool(false);
        ID = solver.nextId();
    }

    public Constraint[] getConstraints() {
        return new Constraint[0];
    }

    public void declareIn(Constraint constraint) {
    }

    @Override
    public Propagator[] getPropagators() {
        return new Propagator[0];
    }

    @Override
    public int getNbProps() {
        return 0;
    }

    @Override
    public int[] getPIndices() {
        return new int[0];
    }

    @Override
    public IntView[] getViews() {
        return new IntView[0];
    }

    @Override
    public int getId() {
        return ID;
    }


    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (value == constante) {
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.REMOVE, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= constante && constante <= to) {
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.REMOVE, "unique value removal");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (value != constante) {
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.INSTANTIATE, "outside domain instantitation");
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > constante) {
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.INCLOW, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < constante) {
            if (Configuration.PLUG_EXPLANATION) solver.getExplainer().removeValue(this, constante, cause);
            this.contradiction(cause, EventType.DECUPP, "outside domain update bound");
        }
        return false;
    }

    @Override
    public void wipeOut(@NotNull ICause cause) throws ContradictionException {
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
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        //useless
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return Empty.get();
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
    public void addMonitor(IVariableMonitor monitor) {
        //useless
    }

    @Override
    public void removeMonitor(IVariableMonitor monitor) {
        //useless
    }

    @Override
    public int nbConstraints() {
        //who cares?
        return 0;
    }

    @Override
    public Explanation explain(VariableState what) {
        Explanation explanation = Explanation.build();
        if (empty.get()) {
            explanation.add(solver.getExplainer().explain(this, constante));
        }
        return explanation;
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        Explanation explanation = Explanation.build();
        if (empty.get()) {
            explanation.add(solver.getExplainer().explain(this, constante));
        }
        return explanation;
    }

    @Override
    public void subscribeView(IView view) {
    }

    @Override
    public int link(Propagator propagator, int idxInProp) {
        return -1;
    }

    @Override
    public void recordMask(int mask) {
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        return IIntDeltaMonitor.Default.NONE;
    }

    @Override
    public void createDelta() {
    }

    @Override
    public void unlink(Propagator propagator, int idx) {
    }

    @Override
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        //void
    }

    @Override
    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
    }

    @Override
    public void notifyViews(EventType event, @NotNull ICause cause) throws ContradictionException {
        //void
    }

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
    public Solver getSolver() {
        return solver;
    }

    @Override
    public int getTypeAndKind() {
        return Variable.INT + Variable.CSTE;
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
