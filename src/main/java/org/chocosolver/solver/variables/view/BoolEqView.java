/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;

/**
 * A specific view for equality on bool var
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public final class BoolEqView extends EqView implements BoolVar {

    protected final BoolVar var;

    public BoolEqView(BoolVar var) {
        super(var);
        this.var = var;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        return contains(value) && instantiateTo(1 - value, cause);
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (values.contains(0)) {
            hasChanged = instantiateTo(0, cause);
        }
        if (values.contains(1)) {
            hasChanged = instantiateTo(1, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (!values.contains(0)) {
            hasChanged = instantiateTo(0, cause);
        }
        if (!values.contains(1)) {
            hasChanged = instantiateTo(1, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (from <= to && from <= 1 && to >= 0) {
            if (from == 1) {
                hasChanged = instantiateTo(0, cause);
            } else if (to == 0) {
                hasChanged = instantiateTo(1, cause);
            } else {
                instantiateTo(2, cause);
            }
        }
        return hasChanged;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
        if (var.instantiateTo(value, this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }else{
            model.getSolver().getEventObserver().undo();
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        if (value > 0) {
            model.getSolver().getEventObserver().updateLowerBound(this, value, getLB(), cause);
            if (var.instantiateTo(value, this)) {
                notifyPropagators(IntEventType.INSTANTIATE, cause);
                return true;
            }else{
                model.getSolver().getEventObserver().undo();
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < 1) {
            model.getSolver().getEventObserver().updateUpperBound(this, value, getUB(), cause);
            if (var.instantiateTo(value, this)) {
                notifyPropagators(IntEventType.INSTANTIATE, cause);
                return true;
            }else{
                model.getSolver().getEventObserver().undo();
            }
        }
        return false;
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (lb > 1) {
            var.instantiateTo(-1, cause);
        } else if (ub < 0) {
            var.instantiateTo(2, cause);
        } else {
            if (lb == 1) {
                hasChanged = instantiateTo(1, cause);
            } else if (ub == 0) {
                hasChanged = instantiateTo(0, cause);
            }
        }
        return hasChanged;
    }

    @Override
    public ESat getBooleanValue() {
        return var.getBooleanValue();
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        return instantiateTo(1, cause);
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        return instantiateTo(0, cause);
    }

    @Override
    public BoolVar duplicate() {
        return model.boolEqView(this.var);
    }

    @Override
    public BoolVar not() {
        return var.not();
    }

    @Override
    public boolean hasNot() {
        return var.hasNot();
    }

    @Override
    public void _setNot(BoolVar not) {
        throw new SolverException("Unexpected call to BoolEqView._setNot()");
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return var.isNot();
    }

    @Override
    public void setNot(boolean isNot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        assert var == this.var;
        return ruleStore.addFullDomainRule(this);
    }
}
