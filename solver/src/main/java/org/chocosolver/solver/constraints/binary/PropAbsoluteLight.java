/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;

/**
 * A light propagator to enforce the constraint <code>absY = |Y|</code>.
 * <p>
 * This is a simplified version of {@link PropAbsolute} that only performs
 * bounds-based filtering (no enumerated domain filtering). It is more efficient
 * for problems where variables have large domains or when full domain filtering is not required.
 * <p>
 * The propagator is created with <code>react = false</code>, meaning it is not initially active
 * in the propagation engine. It only reacts to bound and instantiation events.
 *
 * @author Charles Prud'homme
 * @since 04/07/2025
 * @see PropAbsolute
 */
@Explained
public class PropAbsoluteLight extends PropAbsolute {

    /**
     * Creates a light propagator to enforce <code>X = |Y|</code>.
     * <p>
     * Note: This propagator is created with <code>react = false</code>.
     *
     * @param X    variable representing the absolute value
     * @param Y    variable whose absolute value is computed
     */
    public PropAbsoluteLight(IntVar X, IntVar Y) {
        super(X, Y, false);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        // For absY (vIdx=0): only react to upper bound and instantiation events
        // For Y (vIdx=1): react to all bound and instantiation events
        if (vIdx == 0) {
            return IntEventType.upperBoundAndInst();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Ensure absY is non-negative
        absY.updateLowerBound(0, this, Reason.undef());
        boolean loop;
        do {
            int l = Y.getLB();
            int u = Y.getUB();
            // Update absY bounds based on Y's domain
            if (l >= 0) {
                // Y is non-negative: absY ∈ [l, u]
                absY.updateLowerBound(l, this, lcg() ? this.r(Y.getMinLit()) : Reason.undef());
                absY.updateUpperBound(u, this, lcg() ? this.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
            } else if (u <= 0) {
                // Y is non-positive: absY ∈ [-u, -l]
                absY.updateLowerBound(-u, this, lcg() ? this.r(Y.getMaxLit()) : Reason.undef());
                absY.updateUpperBound(-l, this, lcg() ? this.r(Y.getMaxLit(), Y.getMinLit()) : Reason.undef());
            } else {
                // Y spans zero: absY ∈ [0, max(-l, u)]
                int t = Math.max(-l, u);
                absY.updateUpperBound(t, this, lcg() ? this.r(Y.getMaxLit(), Y.getMinLit()) : Reason.undef());
            }
            // Update Y bounds based on absY's upper bound
            int au = absY.getUB();
            loop = Y.updateUpperBound(au, this, lcg() ? this.r(absY.getMaxLit()) : Reason.undef());
            loop |= Y.updateLowerBound(-au, this, lcg() ? this.r(absY.getMaxLit()) : Reason.undef());
        } while (loop);
    }

}
