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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A view for boolean variable, that enforce not(b).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/07/12
 */
public final class BoolNotView extends IntView implements BoolVar {

    protected final BoolVar var;

    public BoolNotView(BoolVar var) {
        super("not(" + var.getName() + ")", var);
        this.var = var;
    }

    @Override
    public ESat getBooleanValue() {
        return ESat.not(var.getBooleanValue());
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        if (var.setToFalse(this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        if (var.setToTrue(this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (var.removeValue(1 - value, this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        else if (hasEnumeratedDomain()) {
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
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (var.instantiateTo(1 - value, this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > 0) {
            if (var.instantiateTo(1 - value, this)) {
                notifyPropagators(IntEventType.INSTANTIATE, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < 1) {
            if (var.instantiateTo(1 - value, this)) {
                notifyPropagators(IntEventType.INSTANTIATE, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(1 - value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(1 - value);
    }

    @Override
    public int getValue() {
        int v = var.getValue();
        return 1 - v;
    }

    @Override
    public int getLB() {
        if (var.isInstantiated()) {
            return getValue();
        } else return 0;
    }

    @Override
    public int getUB() {
        if (var.isInstantiated()) {
            return getValue();
        } else return 1;
    }

    @Override
    public int nextValue(int v) {
        if (v < 0 && contains(0)) {
            return 0;
        }
        return v <= 0 && contains(1) ? 1 : Integer.MAX_VALUE;
    }

    @Override
    public int previousValue(int v) {
        if (v > 1 && contains(1)) {
            return 1;
        }
        return v >= 1 && contains(0) ? 0 : Integer.MIN_VALUE;
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor((IIntDeltaMonitor) var.monitorDelta(propagator), propagator) {

            @Override
            protected int transform(int value) {
                return 1 - value;
            }
        };
    }

    public String toString() {
        return "not(" + var.getName() + ")";
    }

    @Override
    public BoolVar duplicate() {
        return VariableFactory.not(this.var);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.var.duplicate(solver, identitymap);
            BoolNotView clone = new BoolNotView((BoolVar) identitymap.get(this.var));
            identitymap.put(this, clone);
            for (int i = mIdx - 1; i >= 0; i--) {
                monitors[i].duplicate(solver, identitymap);
            }
        }
    }

    @Override
    public BoolVar not() {
        return var;
    }

    @Override
    public void _setNot(BoolVar not) {
        assert not == var;
    }

    @Override
    public boolean hasNot() {
        return true;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return !var.isNot();
    }

    @Override
    public void setNot(boolean isNot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }
}
