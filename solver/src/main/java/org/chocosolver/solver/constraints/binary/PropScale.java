/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.MathUtils;

/**
 * Scale propagator : ensures x * y = z
 * With y a constant greater than one
 * Ensures AC
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 08/04/2014
 */
@Explained(partial = true, comment = "not tested yet")
public class PropScale extends Propagator<IntVar> {

    protected static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    private final IntVar X, Z;
    private final int Y;
    private final boolean enumerated;

    /**
     * Scale propagator : ensures x * y = z
     *
     * @param x an integer variable
     * @param y a constant (should be >1)
     * @param z an integer variable
     */
    public PropScale(IntVar x, int y, IntVar z) {
        super(new IntVar[]{x, z}, PropagatorPriority.BINARY, false);
        this.X = vars[0];
        this.Z = vars[1];
        this.Y = y;
        assert y > 1;
        this.enumerated = X.hasEnumeratedDomain() && Z.hasEnumeratedDomain();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        X.updateLowerBound(MathUtils.divCeil(Z.getLB(), Y), this, lcg() ? Reason.r(Z.getMinLit()) : Reason.undef());
        X.updateUpperBound(MathUtils.divFloor(Z.getUB(), Y), this, lcg() ? Reason.r(Z.getMaxLit()) : Reason.undef());
        boolean hasChanged;
        hasChanged = Z.updateLowerBound(X.getLB() * Y, this, lcg() ? Reason.r(X.getMinLit()) : Reason.undef());
        hasChanged |= Z.updateUpperBound(X.getUB() * Y, this, lcg() ? Reason.r(X.getMaxLit()) : Reason.undef());
        if (enumerated) {
            int zub = X.getUB();
            for (int v = X.getLB(); v <= zub; v = X.nextValue(v)) {
                if (!Z.contains(v * Y)) {
                    X.removeValue(v, this, lcg() ? Reason.r(Z.getLit(v * Y, IntVar.LR_NE)) : Reason.undef());
                }
            }
            int v = Z.getLB();
            zub = Z.getUB();
            for (; v <= zub; v = Z.nextValue(v)) {
                if ((v / Y) * Y != v) {
                    Z.removeValue(v, this, Reason.undef());
                }
                if (!X.contains(v / Y)) {
                    Z.removeValue(v, this, lcg() ? Reason.r(X.getLit(v / Y, IntVar.LR_NE)) : Reason.undef());
                }
            }
        } else if (hasChanged && Z.hasEnumeratedDomain()) {
            if (Z.getLB() > X.getLB() * Y || Z.getUB() < X.getUB() * Y) {
                propagate(evtmask);
            }
        }
    }

    @Override
    public final ESat isEntailed() {
        if (X.getUB() * Y < Z.getLB() || X.getLB() * Y > Z.getUB()) {
            return ESat.FALSE;
        }
        if (X.isInstantiated() && Z.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
