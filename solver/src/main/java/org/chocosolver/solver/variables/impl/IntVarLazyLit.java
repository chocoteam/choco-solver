/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.lazyness.ILazyBound;
import org.chocosolver.solver.variables.impl.lazyness.StrongBound;
import org.chocosolver.solver.variables.impl.lazyness.WeakBound;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;

import static org.chocosolver.sat.MiniSat.C_Undef;

/**
 * A wrapper for integer variables, that maintains an internal data structure to ease the creation of clauses.
 * This class is based on the paper: "Lazy Clause Generation Reengineered", Thibaut Feydy & Peter J. Stuckey , CP 2009.
 * <br/>
 * It is designed to manage bound lits only.
 * So, the domain is supposed to be large quit large and value removal are ignored.
 * Consequently, the observed variable can only be of type {@link IntervalIntVarImpl}.
 *
 * @author Charles Prud'homme
 * @since 12/10/2023
 */
@Explained
public final class IntVarLazyLit extends AbstractVariable implements IntVar, LitVar {

    final IntVar var; // the observed variable
    final MiniSat sat; // the sat solver

    boolean channeling = true; // to communicate with the sat solver or not

    private final ILazyBound bnd;
    int valLit;

    final int min0;
    final int max0;

    /**
     * Create a variable wrapper with eager literals
     *
     * @param var variable to wrap
     */
    public IntVarLazyLit(IntVar var) {
        super(var.getName(), var.getModel());
        this.model.unassociates(var);
        this.var = var;
        this.min0 = var.getLB();
        this.max0 = var.getUB();
        if (var.hasEnumeratedDomain()) {
            throw new UnsupportedOperationException("IntVarLazyLit can only wrap bounded integer variables");
        }
        this.sat = getModel().getSolver().getSat();
        if (getModel().getSettings().intVarLazyLitWithWeakBounds()) {
            bnd = new WeakBound(getModel());
        } else {
            bnd = new StrongBound(getModel(), min0, max0);
        }
        valLit = MiniSat.makeLiteral(sat.nVars(), true);
        sat.newVariable(new MiniSat.ChannelInfo(this, 1, 2, 0, false));
        if (var.isInstantiated()) {
            sat.cEnqueue(getLit(valLit, LR_EQ), Reason.undef());
        }
    }

    // duplicated code from IntVarEagerLit
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
                    updateLowerBound(val + 1, Cause.Null, Reason.undef());
                    break;
                case LR_LE:
                    updateUpperBound(val, Cause.Null, Reason.undef());
                    break;
                default:
                    throw new UnsupportedOperationException("IntVarLazyLit#channel : are you trying to fix valLit?");
            }
        } catch (ContradictionException ce) {
            // ignore: should be detected by the SAT
            assert (sat.confl != C_Undef);
        }
        channeling = true;
    }

    @Override
    public int getLit(int val, int type) {
        if (val < getLB()) {
            return 1 ^ (type & 1);  // undefined, undefined, true, false
        }
        if (val > getUB()) {
            return type & 1;  // undefined, undefined, false, true
        }
        switch (type) {
            case LR_GE:
                return getGELit(val);
            case LR_LE:
                return getLELit(val);
            default:
                throw new UnsupportedOperationException("IntVarLazyLit does not support this type of literal");
        }
    }

    @Override
    public int getMinLit() {
        return MiniSat.makeLiteral(bnd.currentMinVar(), false);
    }

    @Override
    public int getMaxLit() {
        return MiniSat.makeLiteral(bnd.currentMaxVar(), true);
    }

    @Override
    public int getValLit() {
        assert (isInstantiated());
        return MiniSat.neg(valLit);
    }

    @Override
    public int getGELit(int v) {
        if (v < min0) {
            return 1;
        } else if (v > max0) {
            return 0;
        } else if (v > getUB()) {
            return getMaxLit();
        }
        assert (v >= getLB()) : var + " >= " + v;
        return extracted(v);
    }

    private int extracted(int v) {
        int prev = previousValue(v);
        prev = (prev == Integer.MIN_VALUE) ? v - 1 : prev;
        int var = bnd.getSATVar(prev, this, sat);
        return MiniSat.makeLiteral(var, true);
    }

    @Override
    public int getLELit(int v) {
        if (v < min0) {
            return 0;
        } else if (v > max0) {
            return 1;
        } else if (v < getLB()) {
            return getMinLit();
        }
        return MiniSat.neg(getGELit(v + 1));
    }

    void channelMin(int v, int p) {
        Reason r = Reason.r(MiniSat.neg(p));
        int prev = previousValue(v);
        bnd.channelMin(prev, sat, r);
    }

    void channelMax(int v, int p) {
        Reason r = Reason.r(MiniSat.neg(p));
        bnd.channelMax(v, sat, r);
    }

    void updateFixed() {
        Reason r = Reason.r(getMinLit(), getMaxLit());
        sat.cEnqueue(valLit, r);
    }

    @Override
    public boolean removeValue(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        if (value == getLB()) {
            return updateLowerBound(value + 1, cause, Reason.gather(reason, getMinLit()));
        } else if (value == getUB()) {
            return updateUpperBound(value - 1, cause, Reason.gather(reason, getMaxLit()));
        }
        return false;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause, Reason reason) throws ContradictionException {
        throw new UnsupportedOperationException("#removeValues");
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause, Reason reason) throws ContradictionException {
        throw new UnsupportedOperationException("#removeAllValuesBut");
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        return updateLowerBound(value, cause, reason) | updateUpperBound(value, cause, reason);
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause, Reason reason) throws ContradictionException {
        if (value > getLB()) {
            int p = getGELit(value);
            if (channeling) {
                this.notify(reason, cause, sat, p);
            }
            if (value > getUB()) {
                // ignore: should be detected by the SAT
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            channelMin(value, p);
            int ub = getUB();
            IntEventType e = IntEventType.INCLOW;
            if (value == ub) {// || var.nextValue(value - 1) == ub) {
                updateFixed();
                e = IntEventType.INSTANTIATE;
            }
            // then update the variable, should not fail...
            var.updateLowerBound(value, Cause.Null);
            this.notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause, Reason reason) throws ContradictionException {
        if (value < getUB()) {
            int p = getLELit(value);
            if (channeling) {
                this.notify(reason, cause, sat, p);
            }
            if (value < getLB()) {
                // ignore: should be detected by the SAT
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            channelMax(value, p);
            int lb = getLB();
            IntEventType e = IntEventType.DECUPP;
            if (value == lb) {// || var.previousValue(value + 1) == lb) {
                updateFixed();
                e = IntEventType.INSTANTIATE;
            }
            // then update the variable, should not fail...
            var.updateUpperBound(value, Cause.Null);
            this.notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause, Reason reason) throws ContradictionException {
        throw new UnsupportedOperationException("#updateBounds");
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        throw new UnsupportedOperationException("#removeInterval");
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  THE REST IS DELEGATED TO var                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public boolean contains(int value) {
        return var.contains(value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(value);
    }

    @Override
    public int getValue() throws IllegalStateException {
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
    public int getRange() {
        return var.getRange();
    }

    @Override
    public int nextValue(int v) {
        return var.nextValue(v);
    }

    @Override
    public int nextValueOut(int v) {
        return var.nextValueOut(v);
    }

    @Override
    public int previousValue(int v) {
        return var.previousValue(v);
    }

    @Override
    public int previousValueOut(int v) {
        return var.previousValueOut(v);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        return var.getValueIterator(bottomUp);
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        return var.getRangeIterator(bottomUp);
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        return var.monitorDelta(propagator);
    }

    @Override
    public Iterator<Integer> iterator() {
        return var.iterator();
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
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
    public int getTypeAndKind() {
        return var == null ? VAR | INT : var.getTypeAndKind();
    }

    @Override
    protected EvtScheduler<IntEventType> createScheduler() {
        return new IntEvtScheduler();
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
