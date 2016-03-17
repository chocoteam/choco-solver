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
import org.chocosolver.util.tools.MathUtils;

/**
 * declare an IntVar based on X and C, such as X * C
 * <p>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class ScaleView extends IntView {

    public final int cste;

    /**
     * Create a <i>cste<i/> &times; <i>var<i/> view
     * @param var a variable
     * @param cste a positive integer
     */
    public ScaleView(final IntVar var, final int cste) {
        super("(" + var.getName() + "*" + cste + ")", var);
        assert (cste > 0) : "view cste must be >0";
        this.cste = cste;
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
//            throw new UnsupportedOperationException();
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator), propagator) {
            @Override
            protected int transform(int value) {
                return cste * value;
            }
        };
    }

    @Override
    boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(value / cste, this);
    }

    @Override
    boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(MathUtils.divCeil(value, cste), this);
    }

    @Override
    boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(MathUtils.divFloor(value, cste), this);
    }

    @Override
    boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(value / cste, this);
    }

    @Override
    boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(MathUtils.divCeil(from, cste), MathUtils.divFloor(to, cste), this);
    }

    @Override
    public boolean contains(int value) {
        return value % cste == 0 && var.contains(value / cste);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return value % cste == 0 && var.isInstantiatedTo(value / cste);
    }

    @Override
    public int getValue() {
        return var.getValue() * cste;
    }

    @Override
    public int getLB() {
        return var.getLB() * cste;
    }

    @Override
    public int getUB() {
        return var.getUB() * cste;
    }

    @Override
    public int nextValue(int v) {
        int value = var.nextValue(MathUtils.divFloor(v, cste));
        if (value == Integer.MAX_VALUE) {
            return value;
        }
        return value * cste;
    }

    @Override
    public int nextValueOut(int v) {
        return var.nextValue(MathUtils.divFloor(v, cste)) * cste;
    }

    @Override
    public int previousValue(int v) {
        int value = var.previousValue(MathUtils.divCeil(v, cste));
        if (value == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value * cste;
    }

    @Override
    public int previousValueOut(int v) {
        return var.previousValueOut(MathUtils.divCeil(v, cste)) * cste;
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + " * " + this.cste + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntVar duplicate() {
        return model.intScaleView(this.var, this.cste);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
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
                    return vit.next() * cste;
                }

                @Override
                public int previous() {
                    return vit.previous() * cste;
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
        if (cste == 1) return var.getRangeIterator(bottomUp);
        // cste > 2, so no range anymore!
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeIterator() {


                DisposableValueIterator vit;
                int min,
                        max;

                @Override
                public void bottomUpInit() {
                    vit = getValueIterator(true);
                    if (vit.hasNext()) {
                        min = vit.next();
                    }
                    max = min;
                }

                @Override
                public void topDownInit() {
                    vit = getValueIterator(false);
                    if (vit.hasPrevious()) {
                        max = vit.previous();
                    }
                    min = max;
                }

                @Override
                public boolean hasNext() {
                    return min != Integer.MAX_VALUE;
                }

                @Override
                public boolean hasPrevious() {
                    return max != -Integer.MAX_VALUE;
                }

                @Override
                public void next() {
                    if (vit.hasNext()) {
                        min = max = vit.next();
                    } else {
                        min = Integer.MAX_VALUE;
                    }
                }

                @Override
                public void previous() {
                    if (vit.hasPrevious()) {
                        max = vit.previous();
                        min = max;
                    } else {
                        max = -Integer.MAX_VALUE;
                    }
                }

                @Override
                public int min() {
                    return min;
                }

                @Override
                public int max() {
                    return max;
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
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = false;
        assert var == this.var;
        IntEventType ievt = (IntEventType) evt;
        switch (ievt) {
            case REMOVE:
                newrules |= ruleStore.addRemovalRule(this, value  * cste);
                break;
            case DECUPP:
                newrules |= ruleStore.addUpperBoundRule(this);
                break;
            case INCLOW:
                newrules |= ruleStore.addLowerBoundRule(this);
                break;
            case INSTANTIATE:
                newrules |= ruleStore.addFullDomainRule(this);
                break;
        }
        return newrules;
    }
}
