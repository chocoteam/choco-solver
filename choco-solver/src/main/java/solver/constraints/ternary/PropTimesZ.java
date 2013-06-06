/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

/**
 * X*Y=Z filters from right to left
 *
 * @author Jean-Guillaume Fages
 * @since Dec 2012
 */
public class PropTimesZ extends Propagator<IntVar> {

    IntVar X, Y, Z;

    public PropTimesZ(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y, z}, PropagatorPriority.UNARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        this.Z = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        if (vIdx != 2) return 0;
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        // sign reasoning
        if (Z.getLB() >= 0) {
            positiveOrNul();
            if (Z.getUB() == 0) {
                nul();
            } else if (Z.getLB() > 0) {
                positiveStrict();
            }
        } else if (Z.getUB() < 0) {
            negativeStrict();
        }
        // instantiation reasoning
        if (Z.instantiated()) {
            instantiated(X, Y);
            instantiated(Y, X);
        }
        if (X.instantiated()) {
            instantiatedFromXY(X, Y);
        }
        if (Y.instantiated()) {
            instantiatedFromXY(Y, X);
        }
    }

    @Override
    public final void propagate(int varIdx, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public final ESat isEntailed() {
        if (X.instantiated() && Y.instantiated() && Z.instantiated()) {
            return ESat.eval(X.getValue() * Y.getValue() == Z.getValue());
        } // TODO can be improved
        return ESat.UNDEFINED;
    }

    //****************************************************************************************************************//
    //******* 	SIGN	   	 *****************************************************************************************//
    //****************************************************************************************************************//

    private void positiveOrNul() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateUpperBound(0, aCause);
        } else if (X.getLB() > 0) {
            Y.updateLowerBound(0, aCause);
        } else {
            if (Y.getUB() < 0) {
                X.updateUpperBound(0, aCause);
            } else if (Y.getLB() > 0) {
                X.updateLowerBound(0, aCause);
            }
        }
    }

    private void positiveStrict() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateUpperBound(-1, aCause);
        } else {
            if (X.getLB() >= 0) {
                X.updateLowerBound(1, aCause);
                Y.updateLowerBound(1, aCause);
            } else {
                if (Y.getUB() < 0) {
                    X.updateUpperBound(-1, aCause);
                } else if (Y.getLB() >= 0) {
                    X.updateLowerBound(1, aCause);
                    Y.updateLowerBound(1, aCause);
                }
            }
        }
    }

    private void negativeStrict() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateLowerBound(1, aCause);
        } else {
            if (X.getLB() >= 0) {
                X.updateLowerBound(1, aCause);
                Y.updateUpperBound(-1, aCause);
            } else {
                if (Y.getUB() < 0) {
                    X.updateLowerBound(1, aCause);
                } else if (Y.getLB() >= 0) {
                    X.updateUpperBound(-1, aCause);
                    Y.updateLowerBound(1, aCause);
                }
            }
        }
    }

    private void nul() throws ContradictionException {
        if (!X.contains(0)) {
            Y.instantiateTo(0, aCause);
        } else if (!Y.contains(0)) {
            X.instantiateTo(0, aCause);
        } else if (X == Y) {
            Y.instantiateTo(0, aCause);
        }
    }

    //****************************************************************************************************************//
    //******* INSTANTIATION  *****************************************************************************************//
    //****************************************************************************************************************//

    private void instantiated(IntVar X, IntVar Y) throws ContradictionException {
        if (X.instantiated() && Y.instantiated()) {
            if (X.getValue() * Y.getValue() != Z.getValue()) {
                contradiction(Z, "");                             // checker
            }
        } else if (X.instantiated()) {
            if (X.getValue() != 0) {
                double a = (double) Z.getValue() / (double) X.getValue();
                if (Math.abs(a - Math.round(a)) > 0.001) {
                    contradiction(Z, "");                        // not integer
                }
                Y.instantiateTo((int) Math.round(a), aCause);        // fix v1
                setPassive();
            }
        } else {
            double z = Z.getValue();
            if (z >= 0) {
                if (X.getLB() > 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateUpperBound((int) a, aCause);
                    Y.updateLowerBound((int) Math.ceil(b), aCause);
                }
                if (X.getUB() < 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateUpperBound((int) a, aCause);
                    Y.updateLowerBound((int) b, aCause);
                }
            } else {
                if (X.getLB() > 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateLowerBound((int) a, aCause);
                    Y.updateUpperBound((int) b, aCause);
                }
                if (X.getUB() < 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateLowerBound((int) a, aCause);
                    Y.updateUpperBound((int) b, aCause);
                }
            }
        }
    }

    private void instantiatedFromXY(IntVar v1, IntVar v2) throws ContradictionException {
        int value = v1.getValue();
        int lb = v2.getLB();
        int ub = v2.getUB();
        while (lb <= ub && (!Z.contains(value * lb))) {
            lb = v2.nextValue(lb);
        }
        v2.updateLowerBound(lb, aCause);
        while (lb <= ub && (!Z.contains(value * ub))) {
            ub = v2.previousValue(ub);
        }
        v2.updateUpperBound(ub, aCause);

    }
}
