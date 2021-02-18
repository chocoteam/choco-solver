/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IntDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.siglit.SignedLiteral;
import org.chocosolver.util.iterators.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view executes the appropriate operation on the view's variable."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class IntView<I extends IntVar> extends AbstractVariable implements IView, IntVar {

    /**
     * Observed variable
     */
    protected final I var;

    /**
     * To store removed values
     */
    protected IntDelta delta;

    /**
     * Value iterator
     */
    DisposableValueIterator _viterator;

    /**
     * Range iterator
     */
    DisposableRangeIterator _riterator;

    /**
     * Value iterator allowing for(int i:this) loops
     */
    private IntVarValueIterator _javaIterator = new IntVarValueIterator(this);

    /**
     * Signed Literal
     */
    protected SignedLiteral literal;

    /**
     * Create a view based on {@code var}
     * @param name name of the view
     * @param var observed variable
     */
    IntView(String name, I var) {
        super(name, var.getModel());
        this.var = var;
        this.delta = NoDelta.singleton;
        this.var.subscribeView(this);
    }

    /**
     * Action to execute on {@link #var} when this view requires to instantiate it
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doInstantiateVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to update its lower bound
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to update its upper bound
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to remove a value from it
     * @param value value before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    /**
     * Action to execute on {@link #var} when this view requires to remove an interval from it
     * @param from first value of the interval before modification of the view
     * @param to last value of the interval before modification of the view
     * @return <tt>true</tt> if {@link #var} has been modified
     * @throws ContradictionException if modification fails
     */
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException{
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int inf = getLB();
        int sup = getUB();
        if (inf <= value && value <= sup) {
            IntEventType e = IntEventType.REMOVE;
            model.getSolver().getEventObserver().removeValue(this, value, cause);
            if (doRemoveValueFromVar(value)) {
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
            }else{
                model.getSolver().getEventObserver().undo();
            }
        }
        return false;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        if (nlb > oub || nub < olb) {
            return false;
        }
        if (nlb == olb) {
            // look for the new lb
            do {
                olb = nextValue(olb);
                nlb = values.nextValue(olb - 1);
            } while (olb < Integer.MAX_VALUE && oub < Integer.MAX_VALUE && nlb == olb);
        }
        if (nub == oub) {
            // look for the new ub
            do {
                oub = previousValue(oub);
                nub = values.previousValue(oub + 1);
            } while (olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE && nub == oub);
        }
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(olb, oub, cause);
        // now deal with holes
        int value = nlb, to = nub;
        boolean hasRemoved = false;
        while (value <= to) {
            model.getSolver().getEventObserver().removeValue(this, value, cause);
            if(doRemoveValueFromVar(value)){
                hasRemoved = true;
            }else{
                model.getSolver().getEventObserver().undo();
            }
            value = values.nextValue(value);
        }
        if (hasRemoved) {
            IntEventType e = IntEventType.REMOVE;
            if (var.isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else if(var.hasEnumeratedDomain()){
            for (int v = from; v <= to; v++) {
                if (this.contains(v)) {
                    model.getSolver().getEventObserver().removeValue(this, v, cause);
                }
            }
            boolean done = doRemoveIntervalFromVar(from, to);
            if (done) {
                notifyPropagators(IntEventType.REMOVE, cause);
            }// no else needed, since all values were checked
            return done;
        }
        return false;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(nlb, nub, cause);
        // now deal with holes
        int to = previousValue(nub);
        boolean hasRemoved = false;
        int value = nextValue(nlb);
        // iterate over the values in the domain, remove the ones that are not in values
        for (; value <= to; value = nextValue(value)) {
            if (!values.contains(value)) {
                model.getSolver().getEventObserver().removeValue(this, value, cause);
                if(doRemoveValueFromVar(value)){
                    hasRemoved = true;
                }else{
                    model.getSolver().getEventObserver().undo();
                }
            }
        }
        if (hasRemoved) {
            IntEventType e = IntEventType.REMOVE;
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
        boolean done = doInstantiateVar(value);
        if (done) {
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }else{
            model.getSolver().getEventObserver().undo();
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            model.getSolver().getEventObserver().updateLowerBound(this, value, getLB(), cause);
            IntEventType e = IntEventType.INCLOW;
            boolean done = doUpdateLowerBoundOfVar(value);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }else{
                model.getSolver().getEventObserver().undo();
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            model.getSolver().getEventObserver().updateUpperBound(this, value, getUB(), cause);
            IntEventType e = IntEventType.DECUPP;
            boolean done = doUpdateUpperBoundOfVar(value);
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }else{
                model.getSolver().getEventObserver().undo();
            }
        }
        return false;
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
                if(doUpdateLowerBoundOfVar(lb)){
                    hasChanged = true;
                }else{
                    model.getSolver().getEventObserver().undo();
                }
            }
            if (oub > ub) {
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                model.getSolver().getEventObserver().updateUpperBound(this, ub, getUB(), cause);
                if(doUpdateUpperBoundOfVar(ub)){
                    hasChanged = true;
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
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.INT;
    }

	@Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public int getRange() {
        return var.getRange();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

	@Override
    public IDelta getDelta() {
        return var.getDelta();
    }

    @Override
    public void createDelta() {
        var.createDelta();
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public void notify(IEventType event) throws ContradictionException {
        super.notifyPropagators(transformEvent(event), this);
    }

    @Override
    public IEventType transformEvent(IEventType evt){
        return evt;
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueBoundIterator(this);
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
            _riterator = new DisposableRangeBoundIterator(this);
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }

    @Override
    public Iterator<Integer> iterator() {
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public void forEachIntVar(Consumer<IntVar> action) {
        action.accept(var);
        action.accept(this);
    }

    @Override
    public void createLit(IntIterableRangeSet rootDomain) {
        if (this.literal != null) {
            throw new IllegalStateException("createLit(Implications) called twice");
        }
        this.literal = new SignedLiteral.Set(rootDomain);
    }

    @Override
    public SignedLiteral getLit() {
        if (this.literal == null) {
            throw new NullPointerException("getLit() called on null, a call to createLit(Implications) is required");
        }
        return this.literal;
    }
}
