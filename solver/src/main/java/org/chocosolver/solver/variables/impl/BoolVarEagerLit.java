/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.OneValueDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.*;

import java.util.Iterator;

import static org.chocosolver.sat.MiniSat.C_Undef;

/**
 * A wrapper for boolean variables, that maintains an internal data structure to ease the creation of clauses.
 * This class is based on the paper: "Lazy Clause Generation Reengineered", Thibaut Feydy & Peter J. Stuckey , CP 2009.
 * <br/>
 * It is designed to manage bound lits and also value lits.
 * So, the domain is supposed to be small enough to avoid too many lits.
 * Consequently, the observed variable can either be of type {@link BitsetIntVarImpl}
 * or {@link FixedIntVarImpl}.
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
@Explained
public class BoolVarEagerLit extends AbstractVariable implements BoolVar, LitVar {

    MiniSat sat; // the sat solver

    boolean channeling = true; // to communicate with the sat solver or not

    int vlit; // the value literal
    /**
     * To iterate over removed values
     */
    private IEnumDelta delta = NoDelta.singleton;
    /**
     * To iterate over values in the domain
     */
    private DisposableValueIterator _viterator;
    /**
     * To iterate over ranges
     */
    private DisposableRangeIterator _riterator;
    /**
     * Value iterator allowing for(int i:this) loops
     */
    private IntVarValueIterator _javaIterator;
    /**
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which reacts
     * on value removal
     */
    private boolean reactOnRemoval = false;
    /**
     * Associate boolean variable expressing not(this)
     */
    private BoolVar not;
    /**
     * For boolean expression purpose
     */
    private boolean isNot = false;
    int type;

    /**
     * Create a boolean variable for LCG.
     */
    public BoolVarEagerLit(String name, Model model, int min, int max) {
        super(name, model);
        this.sat = getModel().getSolver().getSat();
        this.vlit = MiniSat.makeLiteral(sat.nVars(), true);
        sat.newVariable(new MiniSat.ChannelInfo(this, 1, 0, 1));
        int t = VAR;
        if (min == 1) {
            sat.cEnqueue(vlit, Reason.undef());
            t = CSTE;
        }
        if (max == 0) {
            sat.cEnqueue(MiniSat.neg(vlit), Reason.undef());
            t = CSTE;
        }
        type = t;
    }

    @Override
    public boolean removeValue(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        if (value == kFALSE)
            return this.instantiateTo(kTRUE, cause, reason);
        else if (value == kTRUE)
            return this.instantiateTo(kFALSE, cause, reason);
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        if (!channeling) {
            // the variable is instantiated by the SAT solver
            if (sat.confl != C_Undef) {
                this.contradiction(cause, "sat failure");
            }
            this.notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        } else if (!isInstantiatedTo(value)) {
            boolean inconsistent = (isInstantiated() || (value < kFALSE || value > kTRUE));
            this.notify(reason, cause, sat, getLit(value, LR_EQ));
            if (inconsistent) {
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            int cval = sat.valueLit(vlit);
            if ((cval == MiniSat.lTrue && value != kTRUE)
                    || (cval == MiniSat.lFalse && value != kFALSE)) {
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            this.notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        return value > kFALSE && this.instantiateTo(value, cause, reason);
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        return value < kTRUE && this.instantiateTo(value, cause, reason);
    }

    @Override
    public void channel(int val, int val_type, int sign) {
        channeling = false;
        int op = val_type * 3 ^ sign;
        try {
            switch (op) {
                case LR_NE:
                    removeValue(val, Cause.Null, Reason.undef());
                    break;
                case LR_EQ:
                    instantiateTo(val, Cause.Null, Reason.undef());
                    break;
                case LR_GE:
                case LR_LE:
                default:
                    throw new UnsupportedOperationException("BoolVarEagerLit#channel");
            }
        } catch (ContradictionException ce) {
            // ignore: should be detected by the SAT
            assert (sat.confl != C_Undef);
        }
        channeling = true;
    }

    @Override
    public int getLit(int val, int type) {
        if (val < 0) {
            return 1 ^ (type & 1);  // true, false, true, false
        }
        if (val > 1) {
            return type - 1 >> 1 & 1;  // true, false, false, true
        }
        switch (type) {
            case LR_NE:
                return vlit - val;
            case LR_EQ:
                return vlit - 1 + val;
            case LR_GE:
                return val == 1 ? vlit - 1 + val : 1;
            case LR_LE:
                return val == 0 ? vlit - 1 + val : 1;
            default:
                throw new UnsupportedOperationException("BoolVarEagerLit#getLit");
        }
    }

    @Override
    public int getMinLit() {
        return MiniSat.neg(getLit(getLB(), LR_GE));
    }

    @Override
    public int getMaxLit() {
        return MiniSat.neg(getLit(getUB(), LR_LE));
    }

    @Override
    public int getValLit() {
        assert (isInstantiated()) : this + " is not instantiated";
        return getLit(getLB(), LR_NE);
    }


    @Override
    public boolean isInstantiated() {
        return sat.valueLit(vlit) != MiniSat.lUndef;
    }

    @Override
    public boolean isInstantiatedTo(int aValue) {
        switch (sat.valueLit(vlit)) {
            case MiniSat.lTrue:
                return aValue == kTRUE;
            case MiniSat.lFalse:
                return aValue == kFALSE;
            default:
                return false;
        }
    }

    @Override
    public boolean contains(int aValue) {
        switch (sat.valueLit(vlit)) {
            case MiniSat.lTrue:
                return aValue == kTRUE;
            case MiniSat.lFalse:
                return aValue == kFALSE;
            default:
                return aValue == kFALSE || aValue == kTRUE;
        }
    }

    @Override
    public int getValue() throws IllegalStateException {
        if (!isInstantiated()) {
            throw new IllegalStateException("getValue() can be only called on instantiated variable. " +
                    name + " is not instantiated");
        }
        return getLB();
    }

    @Override
    public ESat getBooleanValue() {
        if (isInstantiated()) {
            return ESat.eval(getLB() != kFALSE);
        }
        return ESat.UNDEFINED;
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    @Override
    public int getLB() {
        switch (sat.valueLit(vlit)) {
            case MiniSat.lTrue:
                return kTRUE;
            default:
            case MiniSat.lFalse:
                return kFALSE;
        }
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        switch (sat.valueLit(vlit)) {
            default:
            case MiniSat.lTrue:
                return kTRUE;
            case MiniSat.lFalse:
                return kFALSE;
        }
    }

    @Override
    public int getDomainSize() {
        return (isInstantiated() ? 1 : 2);
    }

    @Override
    public int getRange() {
        return getDomainSize();
    }

    @Override
    public int nextValue(int v) {
        if (isInstantiated()) {
            final int val = getLB();
            return (val > v) ? val : Integer.MAX_VALUE;
        } else {
            if (v < kFALSE) return kFALSE;
            if (v == kFALSE) return kTRUE;
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextValueOut(int v) {
        int lb = 0, ub = 1;
        if (isInstantiated()) { // if this is instantiated
            lb = ub = getLB();
        }
        if (lb - 1 <= v && v <= ub) {
            return ub + 1;
        } else {
            return v + 1;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > getUB()) return getUB();
        if (v > getLB()) return getLB();
        return Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int v) {
        int lb = 0, ub = 1;
        if (isInstantiated()) { // if this is instantiated
            lb = ub = getLB();
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public IEnumDelta getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        if (isInstantiated()) {
            return this.name + " = " + getLB();
        } else {
            return this.name + " = " + "[0,1]";
        }
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void createDelta() {
        if (!reactOnRemoval) {
            delta = new OneValueDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new OneValueDeltaMonitor(delta, propagator);
    }

    @Override
    public int getTypeAndKind() {
        return BOOL | type;
    }

    @Override
    protected EvtScheduler<?> createScheduler() {
        return new BoolEvtScheduler();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        if (_javaIterator == null) {
            _javaIterator = new IntVarValueIterator(this);
        }
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public void _setNot(BoolVar neg) {
        this.not = neg;
    }

    @Override
    public BoolVar not() {
        if (!hasNot()) {
            not = model.boolNotView(this);
            not._setNot(this);
        }
        return not;
    }

    @Override
    public boolean hasNot() {
        return not != null;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return isNot;
    }

    @Override
    public void setNot(boolean isNot) {
        this.isNot = isNot;
    }


    /**
     * Creates, or returns if already existing, the SAT variable twin of this.
     *
     * @return the SAT variable of this
     */
    public int satVar() {
        return MiniSat.var(this.vlit);
    }
}
