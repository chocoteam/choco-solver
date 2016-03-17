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
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * View for -V, where V is a IntVar or view
 * <p>
 * <p>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class MinusView extends IntView {


    /**
     * Create a -<i>var<i/> view
     * @param var a integer variable
     */
    public MinusView(final IntVar var) {
        super("-(" + var.getName() + ")", var);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator), propagator) {
            @Override
            protected int transform(int value) {
                return -value;
            }
        };
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = this.getLB();
        int oub = this.getUB();
        boolean hasChanged = false;
        if (olb < lb || oub > ub) {
            IntEventType e = null;

            if (olb < lb) {
                model.getSolver().getEventObserver().updateLowerBound(this, lb, getLB(), cause);
                e = IntEventType.INCLOW;
                if(var.updateUpperBound(-lb, this)){
                    hasChanged = true;
                }else{
                    model.getSolver().getEventObserver().undo();
                }
            }
            if (oub > ub) {
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                model.getSolver().getEventObserver().updateUpperBound(this, ub, getUB(), cause);
                if(var.updateLowerBound(-ub, this)){
                    hasChanged |= true;
                }else{
                    model.getSolver().getEventObserver().undo();
                }
            }
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (hasChanged) {
                this.notifyPropagators(e, cause);
            }
        }
        return hasChanged;
    }

    @Override
    boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(-value, this);
    }

    @Override
    boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(-value, this);
    }

    @Override
    boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(-value, this);
    }

    @Override
    boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(-value, this);
    }

    @Override
    boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(-to, -from, this);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(-value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(-value);
    }

    @Override
    public int getValue() {
        return -var.getValue();
    }

    @Override
    public int getLB() {
        return -var.getUB();
    }

    @Override
    public int getUB() {
        return -var.getLB();
    }

    @Override
    public int nextValue(int v) {
        int value = var.previousValue(-v);
        if (value == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        return -value;
    }

    @Override
    public int nextValueOut(int v) {
        return -var.previousValueOut(-v);
    }

    @Override
    public int previousValue(int v) {
        int value = var.nextValue(-v);
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -value;
    }

    @Override
    public int previousValueOut(int v) {
        return -var.nextValueOut(-v);
    }

    @Override
    public String toString() {
        return "-(" + this.var.toString() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntVar duplicate() {
        return model.intMinusView(this.var);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator vit;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vit = var.getValueIterator(false);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vit = var.getValueIterator(true);
                }

                @Override
                public boolean hasNext() {
                    return vit.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return vit.hasNext();
                }

                @Override
                public int next() {
                    return -vit.previous();
                }

                @Override
                public int previous() {
                    return -vit.next();
                }

                @Override
                public void dispose() {
                    super.dispose();
                    vit.dispose();
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

                DisposableRangeIterator vir;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vir = var.getRangeIterator(false);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vir = var.getRangeIterator(true);
                }

                @Override
                public boolean hasNext() {
                    return vir.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return vir.hasNext();
                }

                @Override
                public void next() {
                    vir.previous();
                }

                @Override
                public void previous() {
                    vir.next();
                }

                @Override
                public int min() {
                    return -vir.max();
                }

                @Override
                public int max() {
                    return -vir.min();
                }

                @Override
                public void dispose() {
                    super.dispose();
                    vir.dispose();
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

    @Override
    public void transformEvent(IEventType evt, ICause cause) throws ContradictionException {
        if (evt == IntEventType.INCLOW) {
            evt = IntEventType.DECUPP;
        } else if (evt == IntEventType.DECUPP) {
            evt = IntEventType.INCLOW;
        }
        notifyPropagators(evt, this);
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = false;
        assert var == this.var;
        IntEventType ievt = (IntEventType) evt;
        switch (ievt) {
            case REMOVE:
                newrules |= ruleStore.addRemovalRule(this, -value);
                break;
            case DECUPP:
                newrules |= ruleStore.addLowerBoundRule(this);
                break;
            case INCLOW:
                newrules |= ruleStore.addUpperBoundRule(this);
                break;
            case INSTANTIATE:
                newrules |= ruleStore.addFullDomainRule(this);
                break;
        }
        return newrules;
    }
}
