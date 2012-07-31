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

import choco.kernel.ESat;
import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.common.util.procedure.IntProcedure;
import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.delta.monitor.IntDeltaMonitor;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/07/12
 */
public class BoolNotView extends IntView<BoolVar> implements BoolVar {

    public BoolNotView(BoolVar var, Solver solver) {
        super("not("+var.getName()+")", var, solver);
    }

    @Override
    public ESat getBooleanValue() {
        return ESat.not(var.getBooleanValue());
    }

    @Override
    public boolean setToTrue(@NotNull ICause cause, boolean informCause) throws ContradictionException {
        return var.setToFalse(cause, informCause);
    }

    @Override
    public boolean setToFalse(@NotNull ICause cause, boolean informCause) throws ContradictionException {
        return var.setToTrue(cause, informCause);
    }

    @Override
    public boolean removeValue(int value, @NotNull ICause cause) throws ContradictionException {
        return var.removeValue(1 - value, cause);
    }

    @Override
    public boolean removeInterval(int from, int to, @NotNull ICause cause) throws ContradictionException {
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        else if (hasEnumeratedDomain()) {     // TODO: really ugly .........
            boolean anyChange = false;
            for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
                anyChange |= removeValue(v, cause);
            }
            return anyChange;
        } else {
            return false;
        }
    }

    @Override
    public boolean instantiateTo(int value, @NotNull ICause cause) throws ContradictionException {
        return var.instantiateTo(1 - value, cause);
    }

    @Override
    public boolean updateLowerBound(int value, @NotNull ICause cause) throws ContradictionException {
        return value > 0 && var.instantiateTo(1 - value, cause);
    }

    @Override
    public boolean updateUpperBound(int value, @NotNull ICause cause) throws ContradictionException {
        return value < 1 && var.instantiateTo(1 - value, cause);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(1 - value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(1 - value);
    }

    @Override
    public int getValue() {
        int v = var.getValue();
        return 1 - v;
    }

    @Override
    public int getLB() {
        if (var.instantiated()) {
            return getValue();
        } else return 0;
    }

    @Override
    public int getUB() {
        if (var.instantiated()) {
            return getValue();
        } else return 1;
    }

    @Override
    public int nextValue(int v) {
        return var.previousValue(v);
    }

    @Override
    public int previousValue(int v) {
        return var.nextValue(v);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        return var.getValueIterator(!bottomUp);
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        return var.getRangeIterator(!bottomUp);
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        return var.explain(what, val);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new IntDeltaMonitor(var.getDelta(), propagator) {
            @Override
            public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
                if (EventType.isRemove(eventType.mask)) {
                    for (int i = frozenFirst; i < frozenLast; i++) {
                        if (propagator != delta.getCause(i)) {
                            proc.execute(1 - delta.get(i));
                        }
                    }
                }
            }
        };
    }
}
