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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.ternary.Times;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

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

    final IntVar X, Z;
    final int Y;
    final boolean enumerated;

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
        if (!(Times.inIntBounds((long) X.getLB() * Y) && Times.inIntBounds((long) X.getUB() * Y))) {
            throw new SolverException("Integer overflow.\nConsider reducing the variable domains.");
        }
        this.enumerated = X.hasEnumeratedDomain() && Z.hasEnumeratedDomain();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        X.updateBounds((int) ceil((double) Z.getLB() / (double) Y - 0.0001), (int) floor((double) Z.getUB() / (double) Y + 0.0001), this);
        boolean hasChanged;
        hasChanged = Z.updateLowerBound(X.getLB() * Y, this);
        hasChanged |= Z.updateUpperBound(X.getUB() * Y, this);
        if (enumerated) {
            int ub = X.getUB();
            for (int v = X.getLB(); v <= ub; v = X.nextValue(v)) {
                if (!Z.contains(v * Y)) {
                    X.removeValue(v, this);
                }
            }
            ub = Z.getUB();
            for (int v = Z.getLB(); v <= ub; v = Z.nextValue(v)) {
                if ((v / Y) * Y != v || !X.contains(v / Y)) {
                    Z.removeValue(v, this);
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
