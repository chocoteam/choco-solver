/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.view;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;

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
			IntEventType e = IntEventType.REMOVE;

            boolean done = var.removeValue(value, this);
            if (done) {
                if (value == inf) {
                    e = IntEventType.INCLOW;
                } else if (value == sup) {
                    e = IntEventType.DECUPP;
                }
                if (this.isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
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
                notifyPropagators(IntEventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean done = var.instantiateTo(value, this);
        if (done) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }

        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
			IntEventType e = IntEventType.INCLOW;
            boolean done = var.updateLowerBound(value, this);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
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
			IntEventType e = IntEventType.DECUPP;
            boolean done = var.updateUpperBound(value, this);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
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
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(value);
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
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.var.duplicate(solver, identitymap);
            EqView clone = new EqView((IntVar) identitymap.get(this.var), solver);
            identitymap.put(this, clone);
        }
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
