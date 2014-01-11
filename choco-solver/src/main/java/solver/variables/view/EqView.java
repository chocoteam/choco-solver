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
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.delta.IIntDeltaMonitor;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;

/**
 * A specific view for equality on bool var
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public class EqView extends IntView {

    public EqView(IntVar var, Solver solver) {
        super("eq(" + var.getName() + ")", var, solver);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        return var.monitorDelta(propagator);
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int inf = getLB();
        int sup = getUB();
        if (inf <= value && value <= sup) {
            EventType e = EventType.REMOVE;

            boolean done = var.removeValue(value, this);
            if (done) {
                if (value == inf) {
                    e = EventType.INCLOW;
                } else if (value == sup) {
                    e = EventType.DECUPP;
                }
                if (this.instantiated()) {
                    e = EventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else {
            boolean done = var.removeInterval(from, to, this);
            if (done) {
                notifyPropagators(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean done = var.instantiateTo(value, this);
        if (done) {
            notifyPropagators(EventType.INSTANTIATE, cause);
            return true;
        }

        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            EventType e = EventType.INCLOW;
            boolean done = var.updateLowerBound(value, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        //        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            EventType e = EventType.DECUPP;
            boolean done = var.updateUpperBound(value, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(value);
    }

    @Override
    public int getValue() {
        return var.getValue();
    }

    @Override
    public int getLB() {
        return var.getLB();
    }

    @Override
    public int getUB() {
        return var.getUB();
    }

    @Override
    public int nextValue(int v) {
        return var.nextValue(v);
    }

    @Override
    public int previousValue(int v) {
        return var.previousValue(v);
    }

    @Override
    public String toString() {
        return "eq(" + this.var.toString() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntVar duplicate() {
        return VariableFactory.eq(this.var);
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        var.explain(what, val, to);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        return var.getValueIterator(bottomUp);
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        return var.getRangeIterator(bottomUp);
    }
}
