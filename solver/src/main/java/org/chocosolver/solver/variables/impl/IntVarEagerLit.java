/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
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
 * It is designed to manage bound lits and also value lits.
 * So, the domain is supposed to be small enough to avoid too many lits.
 * Consequently, the observed variable can either be of type {@link org.chocosolver.solver.variables.impl.BitsetIntVarImpl}
 * or {@link org.chocosolver.solver.variables.impl.FixedIntVarImpl}.
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
@Explained
public final class IntVarEagerLit extends AbstractVariable implements IntVar, LitVar {

    IntVar var; // the observed variable
    MiniSat sat; // the sat solver
    boolean channeling = true; // to communicate with the sat solver or not
    int lit_min; // the initial lower bound of the domain
    int lit_max; // the initial upper bound of the domain
    int base_vlit; // the base index of value literals
    int base_blit; // the base index of bound literals

    /**
     * Create a variable wrapper with eager literals
     *
     * @param var variable to wrap
     */
    public IntVarEagerLit(IntVar var) {
        super(var.getName(), var.getModel());
        this.model.unassociates(var);
        this.var = var;
        if (!var.hasEnumeratedDomain()) {
            throw new UnsupportedOperationException("IntVarEagerLit can only wrap enumerated integer variables");
        }
        this.sat = getModel().getSolver().getSat();
        int min = var.getLB(); // todo: keep ?
        int max = var.getUB();
        lit_min = min;
        lit_max = max;

        // init vlits
        base_vlit = 2 * (sat.nVars() - lit_min);
        for (int v = lit_min; v <= lit_max; v++) {
            sat.newVariable(new MiniSat.ChannelInfo(this, 1, 0, v));
            if (!var.contains(v)) {
                sat.cEnqueue(getNELit(v), Reason.undef());
            }
        }
        if (var.isInstantiated()) {
            sat.cEnqueue(getEQLit(lit_min), Reason.undef());
        }
        // init blits
        base_blit = 2 * (sat.nVars() - lit_min) + 1;
        for (int v = lit_min - 1; v <= lit_max; v++) {
            sat.newVariable(new MiniSat.ChannelInfo(this, 1, 1, v));
        }
        for (int i = lit_min; i <= min; i++) {
            sat.cEnqueue(getGELit(i), Reason.undef());
        }
        for (int i = max; i <= lit_max; i++) {
            sat.cEnqueue(getLELit(i), Reason.undef());
        }
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
                    updateLowerBound(val + 1, Cause.Null, Reason.undef());
                    break;
                case LR_LE:
                    updateUpperBound(val, Cause.Null, Reason.undef());
                    break;
                default:
                    throw new UnsupportedOperationException("IntVarEagerLit#channel");
            }
        } catch (ContradictionException ce) {
            // ignore: should be detected by the SAT
            assert (sat.confl != C_Undef);
        }
        channeling = true;
    }

    @Override
    public int getLit(int v, int t) {
        if (v < lit_min) {
            return 1 ^ (t & 1);  // true, false, true, false
        }
        if (v > lit_max) {
            return t - 1 >> 1 & 1;  // true, false, false, true
        }
        switch (t) {
            case LR_NE:
                return base_vlit + 2 * v;
            case LR_EQ:
                return base_vlit + 2 * v + 1;
            case LR_GE:
                return base_blit + 2 * v;
            case LR_LE:
                return base_blit + 2 * v + 1;
            default:
                throw new UnsupportedOperationException("IntVarEagerLit#getLit");
        }
    }

    @Override
    public int getMinLit() {
        return MiniSat.neg(getGELit(getLB()));
    }

    @Override
    public int getMaxLit() {
        return MiniSat.neg(getLELit(getUB()));
    }

    @Override
    public int getValLit() {
        assert (isInstantiated()) : var + " is not instantiated";
        return getNELit(getLB());
    }

    private int getNELit(int v) {
        return getLit(v, LR_NE);
    }

    private int getEQLit(int v) {
        return getLit(v, LR_EQ);
    }

    private int getGELit(int v) {
        return getLit(v, LR_GE);
    }

    private int getLELit(int v) {
        return getLit(v, LR_LE);
    }

    // Use when you've just set [x >= v]
    private void channelMin(int v) {
        // Set [x >= v-1] to [x >= min+1] using [x >= i] \/ ![x >= v]
        // Set [x != v-1] to [x != min] using [x != i] \/ ![x >= v]
        Reason r = Reason.r(MiniSat.neg(getGELit(v)));
        int min = getLB();
        for (int i = v - 1; i > min; i--) {
            sat.cEnqueue(getGELit(i), r);
            if (var.contains(i)) {
                sat.cEnqueue(getNELit(i), r);
            }
        }
        sat.cEnqueue(getNELit(min), r);
    }

    private void updateMin(int oldMin, int newMin) {
        int v = oldMin;
        while (v < newMin) {
            // Set [x >= v+1] using [x >= v+1] \/ [x <= v-1] \/ [x = v]
            Reason r = Reason.r(getLELit(v - 1), getEQLit(v));
            sat.cEnqueue(getGELit(v + 1), r);
            v++;
        }
    }

    private void channelMax(int v) {
        // Set [x <= v+1] to [x <= max-1] to using [x <= i] \/ ![x <= v]
        // Set [x != v+1] to [x != max] to using ![x = i] \/ ![x <= v]
        Reason r = Reason.r(MiniSat.neg(getLELit(v)));
        int max = getUB();
        for (int i = v + 1; i < max; i++) {
            sat.cEnqueue(getLELit(i), r);
            if (var.contains(i)) {
                sat.cEnqueue(getNELit(i), r);
            }
        }
        sat.cEnqueue(getNELit(max), r);
    }

    private void updateMax(int oldMax, int newMax) {
        int v = oldMax;
        while (v > newMax) {
            // Set [x <= v-1] using [x <= v-1] \/ [x >= v+1] \/ [x = v]
            Reason r = Reason.r(getGELit(v + 1), getEQLit(v));
            sat.cEnqueue(getLELit(v - 1), r);
            v--;
        }
    }

    private void channelFix(int v) {
        Reason r = Reason.r(getNELit(v));
        if (getLB() < v) {
            // Set [x >= v] using [x >= v] \/ ![x = v]
            sat.cEnqueue(getGELit(v), r);
            channelMin(v);
        }
        if (getUB() > v) {
            // Set [x <= v] using [x <= v] \/ ![x = v]
            sat.cEnqueue(getLELit(v), r);
            channelMax(v);
        }
    }

    private void updateFixed(int v) {
        // Set [x = v] using [x = v] \/ [x <= v-1] \/ [x >= v+1]
        Reason r = Reason.r(getLELit(v - 1), getGELit(v + 1));
        sat.cEnqueue(getEQLit(v), r);
    }


    @Override
    public boolean removeValue(int value, ICause cause, Reason reason) throws ContradictionException {
        if (contains(value)) {
            if (channeling) {
                this.notify(reason, cause, sat, getLit(value, LR_NE));
            }
            if (isInstantiated()) {
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            IntEventType e = IntEventType.REMOVE;
            // clear value
            if (value == getLB()) {
                updateMin(value, var.nextValue(value));
                e = IntEventType.INCLOW;
            }
            if (value == getUB()) {
                updateMax(value, var.previousValue(value));
                e = IntEventType.DECUPP;
            }
            if (var.getDomainSize() == 2) { // == 2 because value is still in the domain of var
                updateFixed(getLB() == value ? getUB() : getLB());
                e = IntEventType.INSTANTIATE;
            }
            var.removeValue(value, Cause.Null);
            this.notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause, Reason reason) throws ContradictionException {
        /*boolean hasChanged = false;
        int value = values.min();
        int vub = values.max();
        while (value <= vub) {
            hasChanged |= this.removeValue(value, cause, reason);
            value = values.nextValue(value);
        }
        return hasChanged;*/
        throw new UnsupportedOperationException("#removeValues");
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause, Reason reason) throws ContradictionException {
        /*boolean hasChanged = false;
        int value = this.getLB();
        int vub = this.getUB();
        while (value <= vub) {
            if(!values.contains(value)) {
                hasChanged |= this.removeValue(value, cause, reason);
            }
            value = this.nextValue(value);
        }
        return hasChanged;*/
        throw new UnsupportedOperationException("#removeAllValuesBut");
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        throw new UnsupportedOperationException("#removeInterval");
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        if (!isInstantiatedTo(value)) {
            if (channeling) {
                this.notify(reason, cause, sat, getLit(value, LR_EQ));
            }
            if (!var.contains(value)) {
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            channelFix(value);
            var.instantiateTo(value, Cause.Null);
            this.notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause, Reason reason) throws ContradictionException {
        if (value > getLB()) {
            if (channeling) {
                this.notify(reason, cause, sat, getLit(value, LR_GE));
            }
            if (value > getUB()) {
                // ignore: should be detected by the SAT
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            channelMin(value);
            updateMin(value, var.nextValue(value - 1));
            int ub = getUB();
            IntEventType e = IntEventType.INCLOW;
            if (value == ub || var.nextValue(value - 1) == ub) {
                updateFixed(ub);
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
            if (channeling) {
                this.notify(reason, cause, sat, getLit(value, LR_LE));
            }
            if (value < getLB()) {
                // ignore: should be detected by the SAT
                assert (sat.confl != C_Undef);
                this.contradiction(cause, "sat failure");
            }
            channelMax(value);
            updateMax(value, var.previousValue(value + 1));
            int lb = getLB();
            IntEventType e = IntEventType.DECUPP;
            if (value == lb || var.previousValue(value + 1) == lb) {
                updateFixed(lb);
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
