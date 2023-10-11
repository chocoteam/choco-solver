/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.integer;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.solver.variables.view.IntView;
import org.chocosolver.solver.variables.view.ViewDeltaMonitor;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;

import static org.chocosolver.solver.variables.events.IntEventType.DECUPP;
import static org.chocosolver.solver.variables.events.IntEventType.INCLOW;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/10/2023
 */
public final class IntAffineView<I extends IntVar> extends IntView<I> {

    final boolean p; // positive
    final int a;
    final int b;


    public static IntAffineView<IntVar> make(IntVar var, int a, int b) {
        return new IntAffineView<>(var, a >= 0, Math.abs(a), b);
    }

    public static IntAffineView<IntVar> make(IntAffineView<IntVar> var, int a, int b) {
        return new IntAffineView<>(var.getVariable(), var.p & (a >= 0), var.a * Math.abs(a), var.a * b + var.b);
    }

    /**
     * <i>y</i> is an affine view of <i>x</i>: <i>y = (-1)a*x + b</i>.
     *
     * @param var a integer variable
     */
    private IntAffineView(final I var, boolean p, int a, int b) {
        super((p ? "" : "-") + a + ".(" + var.getName() + ") + " + b, var);
        assert a > 0;
        this.p = p;
        this.a = a;
        this.b = b;
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
                return (p ? value : -value) * a + b;
            }
        };
    }


    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int inf = getLB();
        int sup = getUB();
        if (inf > value || value > sup) return false;
        model.getSolver().getEventObserver().removeValue(this, value, cause);
        value -= b;
        if (a > 1) {
            if ((value % a) != 0) {
                return false;
            }
            value = value / a;
        }
        if (!p) {
            value = -value;
        }
        IntEventType e = IntEventType.REMOVE;
        if (var.removeValue(value, this)) {
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
        } else {
            model.getSolver().getEventObserver().undo();
            return false;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
        value -= b;
        if (a > 1) {
            if ((value % a) != 0) {
                this.contradiction(this, MSG_INST);
            }
            value /= a;
        }
        if (!p) {
            value = -value;
        }
        if (var.instantiateTo(value, this)) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        } else {
            model.getSolver().getEventObserver().undo();
            return false;
        }
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old >= value) return false;
        model.getSolver().getEventObserver().updateLowerBound(this, value, getLB(), cause);
        value--;
        value -= b;
        if (a > 1) {
            value = value / a - (value % a < 0 ? 1 : 0);
        }
        boolean change;
        if (!p) {
            change = var.updateUpperBound(-value - 1, this);
        } else {
            change = var.updateLowerBound(value + 1, this);
        }
        IntEventType e = IntEventType.INCLOW;
        if (isInstantiated()) {
            e = IntEventType.INSTANTIATE;
        }
        if (change) {
            this.notifyPropagators(e, cause);
            return true;
        } else {
            model.getSolver().getEventObserver().undo();
            return false;
        }
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old <= value) return false;
        model.getSolver().getEventObserver().updateUpperBound(this, value, getUB(), cause);
        value -= b;
        if (a > 1) {
            value = value / a - (value % a < 0 ? 1 : 0);
        }
        boolean change = false;
        if (!p) {
            change = var.updateLowerBound(-value, this);
        } else {
            change = var.updateUpperBound(value, this);
        }
        IntEventType e = IntEventType.DECUPP;
        if (isInstantiated()) {
            e = IntEventType.INSTANTIATE;
        }
        if (change) {
            this.notifyPropagators(e, cause);
            return true;
        } else {
            model.getSolver().getEventObserver().undo();
            return false;
        }
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator vit;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vit = var.getValueIterator(p);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vit = var.getValueIterator(!p);
                }

                @Override
                public boolean hasNext() {
                    return p ? vit.hasNext() : vit.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return p ? vit.hasPrevious() : vit.hasNext();
                }

                @Override
                public int next() {
                    return (p ? vit.next() : -vit.previous()) * a + b;
                }

                @Override
                public int previous() {
                    return (p ? vit.previous() : -vit.next()) * a + b;
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
            if (a == 1) {
                // range iterator works on var
                _riterator = new DisposableRangeIterator() {
                    DisposableRangeIterator rit;
                    int min;
                    int max;

                    @Override
                    public void bottomUpInit() {
                        rit = var.getRangeIterator(p);
                        if (p) {
                            min = rit.min() + b;
                            max = rit.max() + b;
                        } else {
                            min = -rit.max() + b;
                            max = -rit.min() + b;
                        }
                    }

                    @Override
                    public void topDownInit() {
                        rit = var.getRangeIterator(!p);
                        if (p) {
                            min = rit.min() + b;
                            max = rit.max() + b;
                        } else {
                            min = -rit.max() + b;
                            max = -rit.min() + b;
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return p ? rit.hasNext() : rit.hasPrevious();
                    }

                    @Override
                    public boolean hasPrevious() {
                        return p ? rit.hasPrevious() : rit.hasNext();
                    }

                    @Override
                    public void next() {
                        if (p) {
                            rit.next();
                            min = rit.min() + b;
                            max = rit.max() + b;
                        } else {
                            rit.previous();
                            min = -rit.max() + b;
                            max = -rit.min() + b;
                        }
                    }

                    @Override
                    public void previous() {
                        if (p) {
                            rit.previous();
                            min = rit.min() + b;
                            max = rit.max() + b;
                        } else {
                            rit.next();
                            min = -rit.max() + b;
                            max = -rit.min() + b;
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
            } else {
                // value iterator works on this
                _riterator = new DisposableRangeIterator() {

                    DisposableValueIterator vit;
                    int min;
                    int max;
                    boolean iterable;

                    @Override
                    public void bottomUpInit() {
                        vit = getValueIterator(true);
                        iterable = vit.hasNext();
                        min = max = vit.next();
                    }

                    @Override
                    public void topDownInit() {
                        vit = getValueIterator(false);
                        iterable = vit.hasPrevious();
                        min = max = vit.previous();
                    }

                    @Override
                    public boolean hasNext() {
                        boolean isIterable = iterable;
                        iterable = vit.hasNext();
                        return isIterable;
                    }

                    @Override
                    public boolean hasPrevious() {
                        boolean isIterable = iterable;
                        iterable = vit.hasPrevious();
                        return isIterable;
                    }

                    @Override
                    public void next() {
                        min = max = vit.next();
                    }

                    @Override
                    public void previous() {
                        min = max = vit.previous();
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
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }

    @Override
    public boolean contains(int value) {
        value -= b;
        if (a > 1) {
            if ((value % a) != 0) {
                return false;
            }
            value /= a;
        }
        value = (!p) ? -value : value;
        return var.contains(value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiated() && contains(value);
    }

    @Override
    public int getValue() throws IllegalStateException {
        if (!isInstantiated()) {
            throw new IllegalStateException("getValue() can be only called on instantiated variable. " +
                    name + " is not instantiated");
        }
        return (p ? var.getValue() : -var.getValue()) * a + b;
    }

    @Override
    public int getLB() {
        return (!p ? -var.getUB() : var.getLB()) * a + b;
    }

    @Override
    public int getUB() {
        return (!p ? -var.getLB() : var.getUB()) * a + b;
    }

    @Override
    public int nextValue(int v) {
        if (v < getLB()) return getLB();
        if (v > getUB()) return Integer.MAX_VALUE;
        // y = p * a * x + b where p in {-1,1}, a and b in Z.
        // => x = (y - b) / (p * a)
        v -= b;
        v = (v < 0 && a > 1) ? v - 1 : v;
        v /= (p ? a : -a);

        if (p) {
            v = var.nextValue(v);
        } else {
            v = var.previousValue(v);
        }
        if (v == Integer.MIN_VALUE || v == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (p ? v : -v) * a + b;
    }

    @Override
    public int nextValueOut(int v) {
        v++;
        if (v < getLB() || v > getUB()) return v;
        // y = p * a * x + b where p in {-1,1}, a and b in Z.
        // => x = (y - b) / (p * a)
        double w = v - b;
        w /= (p ? a : -a);

        int k = (int) w;
        if (w == k && var.contains(k)) {
            if (a > 1) {
                v++;
            } else {
                if (p) {
                    k = var.nextValueOut(k);
                } else {
                    k = var.previousValueOut(k);
                }
                v = ((p ? k : -k) * a + b);
            }
        }
        return v;
    }

    @Override
    public int previousValue(int v) {
        if (v > getUB()) return getUB();
        if (v < getLB()) return Integer.MIN_VALUE;
        // y = p * a * x + b where p in {-1,1}, a and b in Z.
        // => x = (y - b) / (p * a)
        v -= b;
        v = (v > 0 && a > 1) ? v + 1 : v;
        v /= (p ? a : -a);

        if (p) {
            v = var.previousValue(v);
        } else {
            v = var.nextValue(v);
        }
        if (v == Integer.MIN_VALUE || v == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return (p ? v : -v) * a + b;
    }

    @Override
    public int previousValueOut(int v) {
        v--;
        if (v < getLB() || v > getUB()) return v;
        // y = p * a * x + b where p in {-1,1}, a and b in Z.
        // => x = (y - b) / (p * a)
        double w = v - b;
        w /= (p ? a : -a);

        int k = (int) w;
        if (w == k && var.contains(k)) {
            if (a > 1) {
                v--;
            } else {
                if (p) {
                    k = var.previousValueOut(k);
                } else {
                    k = var.nextValueOut(k);
                }
                v = ((p ? k : -k) * a + b);
            }
        }
        return v;
    }


    @Override
    protected EvtScheduler<IntEventType> createScheduler() {
        return new IntEvtScheduler();
    }

    @Override
    public String toString() {
        return this.getName() + "[" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IEventType transformEvent(IEventType evt) {
        if (evt == INCLOW) {
            if (!p) return DECUPP;
        } else if (evt == DECUPP) {
            if (!p) return INCLOW;
        }
        return evt;
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain() || a > 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntAffineView)) return false;

        IntAffineView<?> intView = (IntAffineView<?>) o;
        if (!var.equals(intView.var)) return false;
        return a == intView.a && b == intView.b && p == intView.p;
    }

    public boolean equals(IntVar v, int a, int b) {
        if (!this.var.equals(v)) return false;
        return this.a == Math.abs(a) && this.b == b && this.p == (a >= 0);
    }

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                if (p) {
                    model.getSolver().getEventObserver().updateUpperBound(this, -one * a + b, -two * a + b, this);
                } else {
                    model.getSolver().getEventObserver().updateLowerBound(this, one * a + b, two * a + b, this);
                }
                break;
            case INCLOW:
                if (p) {
                    model.getSolver().getEventObserver().updateLowerBound(this, one * a + b, two * a + b, this);
                } else {
                    model.getSolver().getEventObserver().updateUpperBound(this, -one * a + b, -two * a + b, this);
                }
                break;
            case REMOVE:
                model.getSolver().getEventObserver().removeValue(this, one * (p ? 1 : -1) * a + b, this);
            case INSTANTIATE:
                model.getSolver().getEventObserver().instantiateTo(this, one * (p ? 1 : -1) * a + b, this,
                        (p ? two : -three) * a + b, (p ? three : -two) * a + b);
                break;
        }
    }

}
