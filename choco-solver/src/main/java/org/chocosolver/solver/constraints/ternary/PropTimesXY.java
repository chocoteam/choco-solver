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
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * X*Y=Z filters from left to right
 *
 * @author Jean-Guillaume Fages
 * @since Dec 2012
 */
public class PropTimesXY extends Propagator<IntVar> {

    IntVar X, Y, Z;

    public PropTimesXY(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y}, PropagatorPriority.UNARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        this.Z = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        // sign reasoning
        if (X.getLB() >= 0 && Y.getLB() >= 0) {// Z>=0
            Z.updateBounds(X.getLB() * Y.getLB(), X.getUB() * Y.getUB(), this);
        } else if (X.getUB() < 0 && Y.getUB() < 0) { // Z>0
            Z.updateBounds(X.getUB() * Y.getUB(), X.getLB() * Y.getLB(), this);
        } else if (X.getLB() > 0 && Y.getUB() < 0
                || X.getUB() < 0 && Y.getLB() > 0) { // Z<0
            int a = X.getLB() * Y.getUB();
            int b = X.getUB() * Y.getLB();
            Z.updateBounds(min(a, b), max(a, b), this);
        }
        // instantiation reasoning
        if (X.isInstantiated()) {
            instantiated(X, Y);
        } else if (Y.isInstantiated()) {
            instantiated(Y, X);
        }
    }

    @Override
    public final void propagate(int varIdx, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public final ESat isEntailed() {
        if (X.isInstantiated() && Y.isInstantiated() && Z.isInstantiated()) {
            return ESat.eval(X.getValue() * Y.getValue() == Z.getValue());
        } // TODO can be improved if necessary (reification)
        return ESat.UNDEFINED;
    }

    //****************************************************************************************************************//
    //******* INSTANTIATION  *****************************************************************************************//
    //****************************************************************************************************************//

    private void instantiated(IntVar X, IntVar Y) throws ContradictionException {
        if (X.getValue() == 0) {
            Z.instantiateTo(0, this);
            setPassive();
        } else if (Y.isInstantiated()) {
            Z.instantiateTo(X.getValue() * Y.getValue(), this);    // fix Z
            setPassive();
        } else if (Z.isInstantiated()) {
            double a = (double) Z.getValue() / (double) X.getValue();
            if (Math.abs(a - Math.round(a)) > 0.001) {
                contradiction(Z, "");                        // not integer
            }
            Y.instantiateTo((int) Math.round(a), this);        // fix Y
            setPassive();
        } else {
            int a = X.getValue() * Y.getLB();
            int b = X.getValue() * Y.getUB();
            Z.updateBounds(min(a, b), max(a, b), this);
        }
    }

}
