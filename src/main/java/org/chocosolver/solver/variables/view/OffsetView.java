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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;


/**
 * declare an IntVar based on X and C, such as X + C
 * <br/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class OffsetView extends IntView {

    /**
     * A constant value
     */
    public final int cste;

    /**
     * A view based on <i>var<i/> such that <i>var<i/> + <i>cste<i/>
     * @param var an integer variable
     * @param cste an int
     */
    public OffsetView(final IntVar var, final int cste) {
        super("(" + var.getName() + "+" + cste + ")", var);
        this.cste = cste;
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator)) {
            @Override
            protected int transform(int value) {
                return value + cste;
            }
        };
    }

    @Override
    protected boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(value - cste, this);
    }

    @Override
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(value - cste, this);
    }

    @Override
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(value - cste, this);
    }

    @Override
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(value - cste, this);
    }

    @Override
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(from - cste, to - cste, this);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value - cste);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(value - cste);
    }

    @Override
    public int getValue() {
        return var.getValue() + cste;
    }

    @Override
    public int getLB() {
        return var.getLB() + cste;
    }

    @Override
    public int getUB() {
        return var.getUB() + cste;
    }

    @Override
    public int nextValue(int v) {
        int value = var.nextValue(v - cste);
        if (value == Integer.MAX_VALUE) {
            return value;
        }
        return value + cste;
    }

    @Override
    public int nextValueOut(int v) {
        return var.nextValueOut(v - cste) + cste;
    }

    @Override
    public int previousValue(int v) {
        int value = var.previousValue(v - cste);
        if (value == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value + cste;
    }

    @Override
    public int previousValueOut(int v) {
        return var.previousValueOut(v - cste) + cste;
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + " + " + this.cste + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator vit;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vit = var.getValueIterator(true);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vit = var.getValueIterator(false);
                }

                @Override
                public boolean hasNext() {
                    return vit.hasNext();
                }

                @Override
                public boolean hasPrevious() {
                    return vit.hasPrevious();
                }

                @Override
                public int next() {
                    return vit.next() + cste;
                }

                @Override
                public int previous() {
                    return vit.previous() + cste;
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
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeIterator() {

                DisposableRangeIterator vir;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vir = var.getRangeIterator(true);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vir = var.getRangeIterator(false);
                }

                @Override
                public boolean hasNext() {
                    return vir.hasNext();
                }

                @Override
                public boolean hasPrevious() {
                    return vir.hasPrevious();
                }

                @Override
                public void next() {
                    vir.next();
                }

                @Override
                public void previous() {
                    vir.previous();
                }

                @Override
                public int min() {
                    return vir.min() + cste;
                }

                @Override
                public int max() {
                    return vir.max() + cste;
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
    public int transformValue(int value) {
        return value + cste;
    }

    @Override
    public int reverseValue(int value) {
        return value - cste;
    }

    @Override
    public void justifyEvent(IntVar var, ICause cause, IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                model.getSolver().getExplainer().updateUpperBound(this, one + cste, two + cste, var);
                break;
            case INCLOW:
                model.getSolver().getExplainer().updateLowerBound(this, one + cste, two + cste, var);
                break;
            case REMOVE:
                model.getSolver().getExplainer().removeValue(this, one + cste, var);
                break;
            case INSTANTIATE:
                model.getSolver().getExplainer().instantiateTo(this, one + cste, var, two + cste, three + cste);
                break;
        }
    }
}
