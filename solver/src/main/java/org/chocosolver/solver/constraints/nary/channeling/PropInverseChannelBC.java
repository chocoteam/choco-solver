/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.channeling;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * X[i] = j+Ox <=> Y[j] = i+Oy
 * <p/>
 * Propagator for bounded variables
 * it ensures :
 * LB(X[i]) = j+Ox => Y[j].contains(i+Oy)
 * UB(X[i]) = j+Ox => Y[j].contains(i+Oy)
 * and reciprocally for Y
 * It however does not performs BC on the conjunction
 * of this and the allDifferent constraint implied by InverseChanneling constraint
 * Such a consistency would require to know somehow holes in (bounded) domains
 * Again, AC is strongly advised
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class PropInverseChannelBC extends Propagator<IntVar> {

    private final int minX;
    private final int minY;
    private final int n;
    private final IntVar[] X;
    private final IntVar[] Y;
    private final BitSet toCompute;

    public PropInverseChannelBC(IntVar[] X, IntVar[] Y, int minX, int minY) {
        super(ArrayUtils.append(X, Y), PropagatorPriority.LINEAR, true);
        this.X = Arrays.copyOfRange(this.vars, 0, X.length);
        this.Y = Arrays.copyOfRange(this.vars, X.length, vars.length);
        n = Y.length;
        this.minX = minX;
        this.minY = minY;
        toCompute = new BitSet(2 * n);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < n; i++) {
                X[i].updateBounds(minX, n - 1 + minX, this);
                Y[i].updateBounds(minY, n - 1 + minY, this);
            }
            toCompute.clear();
            for (int i = 0; i < n; i++) {
                boundedFilteringOfX(i);
                boundedFilteringOfY(i);
            }
        }
        while (!toCompute.isEmpty()) {
            int next = toCompute.nextSetBit(0);
            toCompute.clear(next);
            if (next < n) {
                boundedFilteringOfX(next);
            } else {
                boundedFilteringOfY(next - n);
            }
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        //bounds
        if (varIdx < n) {
            boundedFilteringOfX(varIdx);
        } else {
            boundedFilteringOfY(varIdx - n);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    private void boundedFilteringOfX(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = X[var].getLB();
        int max = X[var].getUB();
        for (int v = min; v <= max; v = X[var].nextValue(v)) {
            if (!Y[v - minX].contains(var + minY)) {
                X[var].removeValue(v, this);
                toCompute.set(v - minX);
            } else {
                break;
            }
        }
        for (int v = max; v >= min; v = X[var].previousValue(v)) {
            if (!Y[v - minX].contains(var + minY)) {
                X[var].removeValue(v, this);
                toCompute.set(v - minX);
            } else {
                break;
            }
        }
    }

    private void boundedFilteringOfY(int var) throws ContradictionException {
        // X[i] = j+Ox <=> Y[j] = i+Oy
        int min = Y[var].getLB();
        int max = Y[var].getUB();
        for (int v = min; v <= max; v = Y[var].nextValue(v)) {
            if (!X[v - minY].contains(var + minX)) {
                Y[var].removeValue(v, this);
                toCompute.set(v - minY);
            } else {
                break;
            }
        }
        for (int v = max; v >= min; v = Y[var].previousValue(v)) {
            if (!X[v - minY].contains(var + minX)) {
                Y[var].removeValue(v, this);
                toCompute.set(v - minY);
            } else {
                break;
            }
        }
    }

    @Override
    public ESat isEntailed() {
        boolean allInst = true;
        for (int i = 0; i < n; i++) {
            if (!(vars[i].isInstantiated() && vars[i + n].isInstantiated())) {
                allInst = false;
            }
            if (X[i].isInstantiated() && !Y[X[i].getValue() - minX].contains(i + minY)) {
                return ESat.FALSE;
            }
            if (Y[i].isInstantiated() && !X[Y[i].getValue() - minY].contains(i + minX)) {
                return ESat.FALSE;
            }
        }
        if (allInst) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "Inverse_BC({" + X[0] + "...}{" + Y[0] + "...})";
    }

}
