/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
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
public class PropScale extends Propagator<IntVar> {

    protected static final int MAX = Integer.MAX_VALUE - 1, MIN = Integer.MIN_VALUE + 1;

    private final IntVar X, Z;
    private final int Y;
    private final boolean enumerated;
    private final IntIterableBitSet values;

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
        this.values = enumerated?new IntIterableBitSet():null;
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        X.updateBounds(MathUtils.divCeil(Z.getLB(), Y), MathUtils.divFloor(Z.getUB(), Y), this);
        boolean hasChanged;
        hasChanged = Z.updateBounds(X.getLB() *  Y, X.getUB() *  Y, this);
        if (enumerated) {
            int ub = X.getUB();
            for (int v = X.getLB(); v <= ub; v = X.nextValue(v)) {
                if (!Z.contains(v * Y)) {
                    X.removeValue(v, this);
                }
            }
            int v = Z.getLB();
            this.values.clear();
            this.values.setOffset(v);
            ub = Z.getUB();
            for (; v <= ub; v = Z.nextValue(v)) {
                if ((v / Y) * Y != v || !X.contains(v / Y)) {
                    this.values.add(v);
                }
            }
            Z.removeValues(values, this);
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
