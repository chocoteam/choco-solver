/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * Z = ceil(X/Y)
 * A propagator for the constraint Z = ceil(X / Y) where X, Y and Z are possibly negative integer variables.
 * The constructor introduces views of the variables to handle the sign of the variables correctly.
 */
@Explained
public class PropDivXYZLight extends Propagator<IntVar> {

    // Z = X/Y
    private final IntVar X, Y, Z;

    public static int getSign(IntVar var) {
        if (var.getLB() >= 0) {
            return 1; // positive
        } else if (var.getUB() < 0) {
            return -1; // negative
        } else {
            return 0; // zero
        }
    }

    private static IntVar[] scope(IntVar x, IntVar y, IntVar z) {
        boolean x_flip = (getSign(x) == -1);
        boolean y_flip = (getSign(y) == -1);
        boolean z_flip = (getSign(z) == -1);
        Model m = x.getModel();
        if (!x_flip && !y_flip && !z_flip) {
            // ceil(x+1 / y) = z+1
            return new IntVar[]{m.intView(1, x, 1), y, m.intView(1, z, 1)};
        } else if (!x_flip && y_flip && z_flip) {
            // ceil(x / -y) = -z
            return new IntVar[]{x, m.intView(-1, y, 0), z};
        } else if (x_flip && !y_flip && z_flip) {
            // ceil(-x / y) = -z
            return new IntVar[]{m.intView(-1, x, 0), y, m.intView(-1, z, 0)};
        } else if (x_flip && y_flip && !z_flip) {
            // ceil(-x+1 / -y) = z+1
            return new IntVar[]{m.intView(-1, x, 1), m.intView(-1, y, 0), m.intView(1, z, 1)};
        } else {
            throw new SolverException("Unexpected sign combination for variables in PropDivXYZLight");
        }
    }


    public PropDivXYZLight(IntVar x, IntVar y, IntVar z) {
        super(scope(x, y, z), PropagatorPriority.TERNARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        this.Z = vars[2];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    /**
     * The main propagation method that filters, according to the constraint definition
     *
     * @param evtmask is it the initial propagation or not?
     * @throws ContradictionException if failure occurs
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged;
        do {
            int x_min = X.getLB();
            int x_max = X.getUB();
            int y_min = Y.getLB();
            int y_max = Y.getUB();
            int z_min = Z.getLB();
            int z_max = Z.getUB();

            // z >= ceil(x.min / y.max)
            hasChanged = Z.updateLowerBound((x_min + y_max - 1) / y_max, this,
                    lcg() ? Reason.r(X.getMinLit(), Y.getMaxLit()) : Reason.undef());
            // z <= ceil(x.max / y.min)
            hasChanged |= Z.updateUpperBound((x_max + y_min - 1) / y_min, this,
                    lcg() ? Reason.r(X.getMaxLit(), Y.getMinLit()) : Reason.undef());

            // x >= y.min * (z.min - 1) + 1
            hasChanged |= X.updateLowerBound(y_min * (z_min - 1) + 1, this,
                    lcg() ? Reason.r(Y.getMinLit(), Z.getMinLit()) : Reason.undef());
            // x <= y.max * z.max
            hasChanged |= X.updateUpperBound(y_max * z_max, this,
                    lcg() ? Reason.r(Y.getMaxLit(), Z.getMaxLit()) : Reason.undef());

            // y >= ceil(x.min / z.max)
            if (z_max >= 1) {
                hasChanged |= Y.updateLowerBound((x_min + z_max - 1) / z_max, this,
                        lcg() ? Reason.r(X.getMinLit(), Z.getMaxLit()) : Reason.undef());
            }

            // y <= ceil(x.max / z.min-1) - 1
            if (z_min >= 2) {
                hasChanged |= Y.updateUpperBound((x_max + z_min - 2) / (z_min - 1) - 1, this,
                        lcg() ? Reason.r(X.getMaxLit(), Z.getMinLit()) : Reason.undef());
            }

        } while (hasChanged);
    }


    @Override
    public ESat isEntailed() {
        // forbid Y=0
        if (Y.isInstantiatedTo(0)) {
            return ESat.FALSE;
        }
        // check tuple
        if (isCompletelyInstantiated()) {
            return ESat.eval(X.getValue() / Y.getValue() == Z.getValue());
        }
        // X=0 => Z=0
        if (X.isInstantiatedTo(0) && !Z.contains(0)) {
            return ESat.FALSE;
        }
        // check sign
        boolean pos = (X.getLB() >= 0 && Y.getLB() >= 0) || (X.getUB() < 0 && Y.getUB() < 0);
        if (pos && Z.getUB() < 0) {
            return ESat.FALSE;
        }
        boolean neg = (X.getLB() >= 0 && Y.getUB() < 0) || (X.getUB() < 0 && Y.getLB() >= 0);
        if (neg && Z.getLB() > 0) {
            return ESat.FALSE;
        }
        // compute absolute bounds
        int minAbsX;
        if (X.getLB() > 0) {
            minAbsX = X.getLB();
        } else if (X.getUB() < 0) {
            minAbsX = -X.getUB();
        } else {
            minAbsX = 0;
        }
        int maxAbsX = Math.max(X.getUB(), -X.getLB());
        int minAbsY;
        if (Y.getLB() > 0) {
            minAbsY = Y.getLB();
        } else if (Y.getUB() < 0) {
            minAbsY = -Y.getUB();
        } else {
            minAbsY = 1;
        }
        int maxAbsY = Math.max(Y.getUB(), -Y.getLB());
        int minAbsZ;
        if (Z.getLB() > 0) {
            minAbsZ = Z.getLB();
        } else if (Z.getUB() < 0) {
            minAbsZ = -Z.getUB();
        } else {
            minAbsZ = 0;
        }
        int maxAbsZ = Math.max(Z.getUB(), -Z.getLB());
        // check absolute bounds
        if ((minAbsZ > maxAbsX / minAbsY) || (maxAbsZ < minAbsX / maxAbsY)) {
            return ESat.FALSE;
        }
        // check case Z=0
        if ((Z.isInstantiatedTo(0) && minAbsX > maxAbsY) || (maxAbsX < minAbsY && !Z.contains(0))) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

}
