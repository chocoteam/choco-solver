/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * A propagator to enforce the constraint <code>absY = |Y|</code>, where
 * <code>absY</code> is a variable representing the absolute value of <code>Y</code>.
 * <p>
 * This propagator ensures that:
 * <ul>
 *     <li><code>absY</code> is always non-negative,</li>
 *     <li>the domain of <code>Y</code> is restricted to <code>[-absY.getUB(), absY.getUB()]</code>,</li>
 *     <li>the domain of <code>absY</code> is restricted to <code>[0, max(|Y.getLB()|, |Y.getUB()|)]</code>.</li>
 * </ul>
 * <p>
 * If both variables have enumerated domains, additional filtering is applied to ensure
 * that for every value <code>v</code> in the domain of <code>absY</code>, either <code>v</code> or <code>-v</code>
 * is in the domain of <code>Y</code>, and vice versa.
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 18/05/11
 */
@Explained
public class PropAbsolute extends Propagator<IntVar> {

    /**
     * Variable representing the absolute value, i.e., <code>absY = |Y|</code>.
     */
    final IntVar absY;
    /**
     * Variable whose absolute value is computed.
     */
    final IntVar Y;
    /**
     * Indicates whether both variables have unfixed enumerated domains.
     */
    private final boolean bothEnumerated;

    /**
     * Creates a propagator to enforce <code>X = |Y|</code>.
     *
     * @param X    variable representing the absolute value
     * @param Y    variable whose absolute value is computed
     */
    public PropAbsolute(IntVar X, IntVar Y) {
        this(X, Y, true);
    }

    /**
     * Creates a propagator to enforce <code>X = |Y|</code>.
     *
     * @param X       variable representing the absolute value
     * @param Y       variable whose absolute value is computed
     * @param react   if <code>true</code>, the propagator is initially active
     */
    public PropAbsolute(IntVar X, IntVar Y, boolean react) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, react);
        this.absY = vars[0];
        this.Y = vars[1];
        bothEnumerated = X.hasUnfixedEnumeratedDomain() && Y.hasUnfixedEnumeratedDomain();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        // Full propagation for enumerated domains, bound and instantiation events otherwise
        if (absY.hasEnumeratedDomain() && Y.hasEnumeratedDomain()) {
            return IntEventType.all();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        // If the upper bound of absY is negative, the constraint is always false (absY = |Y| >= 0)
        if (absY.getUB() < 0) {
            return ESat.FALSE;
        } else if (absY.isInstantiated()) {
            int absVal = absY.getValue();
            if (Y.isInstantiated()) {
                // Both variables are instantiated: check if absY.getValue() == |Y.getValue()|
                return ESat.eval(absVal == Math.abs(Y.getValue()));
            } else {
                if (absVal == 0) {
                    // |Y| = 0 ⇔ Y = 0
                    if (Y.getDomainSize() == 1 && Y.contains(0)) {
                        return ESat.TRUE; // Y can only be 0
                    } else if (!Y.contains(0)) {
                        return ESat.FALSE; // Y cannot be 0
                    } else {
                        return ESat.UNDEFINED; // Y may or may not be 0
                    }
                } else {
                    // |Y| = absVal ⇔ Y ∈ {absVal, -absVal}
                    if (Y.getDomainSize() == 2 &&
                            Y.contains(absVal) &&
                            Y.contains(-absVal)) {
                        return ESat.TRUE; // Y can only be absVal or -absVal
                    } else if (!Y.contains(absVal) && !Y.contains(-absVal)) {
                        return ESat.FALSE; // Y can be neither absVal nor -absVal
                    } else {
                        return ESat.UNDEFINED;
                    }
                }
            }
        } else if (Y.isInstantiated()) {
            // Y is instantiated, absY is not: check if |Y.getValue()| is in absY's domain
            int absYVal = Math.abs(Y.getValue());
            if (!absY.contains(absYVal)) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    public String toString() {
        return String.format("%s = |%s|", absY.toString(), Y.toString());
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Initial propagation: ensure absY is non-negative and apply bounds filtering
        absY.updateLowerBound(0, this, Reason.undef());
        setBounds();
        if (bothEnumerated) {
            enumeratedFiltering();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            if (varIdx == 1) {
                // Y is instantiated: set absY to |Y|
                absY.instantiateTo(Math.abs(Y.getValue()), this, lcg() ? this.r(Y.getValLit()) : Reason.undef());
                setPassive();
            } else if (Y.hasEnumeratedDomain()) {
                // absY is instantiated: restrict Y to [-absY, absY] and remove (0, absY) if needed
                int val = absY.getValue();
                Y.updateLowerBound(-val, this, lcg() ? this.r(absY.getValLit()) : Reason.undef());
                Y.updateUpperBound(val, this, lcg() ? this.r(absY.getValLit()) : Reason.undef());
                val--;
                if (val >= 0) {
                    removeInterval(Y, -val, val, lcg() ? this.r(absY.getValLit()) : Reason.undef());
                }
                setPassive();
            } else {
                setBounds();
            }
        } else {
            if (IntEventType.isBound(mask)) {
                setBounds();
            }
            if (bothEnumerated) {
                enumeratedFiltering();
            }
        }
    }

    private void setBounds() throws ContradictionException {
        // X = |Y|
        int max = absY.getUB();
        int min = absY.getLB();
        Y.updateLowerBound(-max, this, lcg() ? this.r(absY.getMaxLit()) : Reason.undef());
        Y.updateUpperBound(max, this, lcg() ? this.r(absY.getMaxLit()) : Reason.undef());
        if (1 - min <= min -1) {
            removeInterval(Y, 1 - min, min - 1, lcg() ? this.r(absY.getMinLit()) : Reason.undef());
        }
        /////////////////////////////////////////////////
        int prevLB = absY.getLB();
        int prevUB = absY.getUB();
        min = Y.getLB();
        max = Y.getUB();
        if (max <= 0) {
            absY.updateLowerBound(-max, this,
                    lcg() ? this.r(Y.getMaxLit()) : Reason.undef());
            absY.updateUpperBound(-min, this,
                    lcg() ? this.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        } else if (min >= 0) {
            absY.updateLowerBound(min, this,
                    lcg() ? this.r(Y.getMinLit()) : Reason.undef());
            absY.updateUpperBound(max, this,
                    lcg() ? this.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        } else {
            if (Y.hasEnumeratedDomain() && !lcg()) {
                int mP = Y.nextValue(-1);
                int mN = -Y.previousValue(1);
                absY.updateLowerBound(Math.min(mP, mN), this);
            }
                absY.updateUpperBound(Math.max(-min, max), this,
                        lcg() ? this.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        }
        if (prevLB != absY.getLB() || prevUB != absY.getUB()) setBounds();
    }

    private void enumeratedFiltering() throws ContradictionException {
        int min = absY.getLB();
        int max = absY.getUB();
        for (int v = min; v <= max; v = absY.nextValue(v)) {
            if (!(Y.contains(v) || Y.contains(-v))) {
                absY.removeValue(v, this,
                        lcg() ? this.r(Y.getLit(v, IntVar.LR_EQ), Y.getLit(-v, IntVar.LR_EQ)) : Reason.undef());
            }
        }
        min = Y.getLB();
        max = Y.getUB();
        for (int v = min; v <= max; v = Y.nextValue(v)) {
            if (!(absY.contains(Math.abs(v)))) {
                Y.removeValue(v, this,
                        lcg() ? this.r(absY.getLit(Math.abs(v), IntVar.LR_EQ)) : Reason.undef());
            }
        }
    }

    //***********************************************************************************
    // EXPLANATIONS
    //***********************************************************************************

    private void removeInterval(IntVar intVar, int fromIncl, int toIncl, Reason reason) throws ContradictionException {
        if (!lcg()) {
            intVar.removeInterval(fromIncl, toIncl, this);
        } else {
            for (int i=fromIncl; i<=toIncl; i++) {
                intVar.removeValue(i, this, reason);
            }
        }
    }
}
