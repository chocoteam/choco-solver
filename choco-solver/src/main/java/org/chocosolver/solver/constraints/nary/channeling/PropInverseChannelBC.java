/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * of this and the alldifferent constraint implied by InverseChanneling constraint
 * Such a consistency would require to know somehow holes in (bounded) domains
 * Again, AC is strongly advised
 *
 * @author Jean-Guillaume Fages
 * @since Nov 2012
 */
public class PropInverseChannelBC extends Propagator<IntVar> {

    protected int minX, minY;
    protected int n;
    protected IntVar[] X, Y;
    protected BitSet toCompute;

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
                X[i].updateLowerBound(minX, this);
                X[i].updateUpperBound(n - 1 + minX, this);
                Y[i].updateLowerBound(minY, this);
                Y[i].updateUpperBound(n - 1 + minY, this);
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
