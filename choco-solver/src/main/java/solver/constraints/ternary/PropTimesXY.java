/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.ternary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.events.IntEventType;
import util.ESat;

/**
 * X*Y=Z filters from left to right
 *
 * @author Jean-Guillaume Fages
 * @since Dec 2012
 */
public class PropTimesXY extends Propagator<IntVar> {

    IntVar X, Y, Z;

    public PropTimesXY(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y, z}, PropagatorPriority.UNARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        this.Z = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        if (vIdx == 2) return 0;
        return IntEventType.boundAndInst();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        // sign reasoning
        if (X.getLB() >= 0 && Y.getLB() >= 0) {// Z>=0
            Z.updateLowerBound(X.getLB() * Y.getLB(), aCause);
            Z.updateUpperBound(X.getUB() * Y.getUB(), aCause);
        } else if (X.getUB() < 0 && Y.getUB() < 0) { // Z>0
            Z.updateLowerBound(X.getUB() * Y.getUB(), aCause);
            Z.updateUpperBound(X.getLB() * Y.getLB(), aCause);
        } else if (X.getLB() > 0 && Y.getUB() < 0
                || X.getUB() < 0 && Y.getLB() > 0) { // Z<0
            int a = X.getLB() * Y.getUB();
            int b = X.getUB() * Y.getLB();
            Z.updateLowerBound(Math.min(a, b), aCause);
            Z.updateUpperBound(Math.max(a, b), aCause);
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
            Z.instantiateTo(0, aCause);
            setPassive();
        } else if (Y.isInstantiated()) {
            Z.instantiateTo(X.getValue() * Y.getValue(), aCause);    // fix Z
            setPassive();
        } else if (Z.isInstantiated()) {
            if (X.getValue() != 0) {
                double a = (double) Z.getValue() / (double) X.getValue();
                if (Math.abs(a - Math.round(a)) > 0.001) {
                    contradiction(Z, "");                        // not integer
                }
                Y.instantiateTo((int) Math.round(a), aCause);        // fix Y
                setPassive();
            }
        } else {
            int a = X.getValue() * Y.getLB();
            int b = X.getValue() * Y.getUB();
            Z.updateLowerBound(Math.min(a, b), aCause);
            Z.updateUpperBound(Math.max(a, b), aCause);
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = vars.length;
            IntVar[] ivars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                vars[i].duplicate(solver, identitymap);
                ivars[i] = (IntVar) identitymap.get(vars[i]);
            }
            identitymap.put(this, new PropTimesXY(ivars[0], ivars[1], ivars[2]));
        }
    }
}
