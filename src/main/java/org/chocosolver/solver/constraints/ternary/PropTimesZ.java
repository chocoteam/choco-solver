/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static java.lang.Math.ceil;

/**
 * X*Y=Z filters from right to left
 *
 * @author Jean-Guillaume Fages
 * @since Dec 2012
 */
public class PropTimesZ extends Propagator<IntVar> {

    private IntVar X, Y, Z;

    public PropTimesZ(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{z}, PropagatorPriority.UNARY, false);
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
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
        if (Z.isInstantiated()) {
            instantiated(X, Y);
            instantiated(Y, X);
        }
        if (X.isInstantiated()) {
            instantiatedFromXY(X, Y);
        }
        if (Y.isInstantiated()) {
            instantiatedFromXY(Y, X);
        }
    }

    @Override
    public final ESat isEntailed() {
        if (X.isInstantiated() && Y.isInstantiated() && Z.isInstantiated()) {
            return ESat.eval(X.getValue() * Y.getValue() == Z.getValue());
        } // TODO can be improved
        return ESat.UNDEFINED;
    }

    //****************************************************************************************************************//
    //******* 	SIGN	   	 *****************************************************************************************//
    //****************************************************************************************************************//

    private void positiveOrNul() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateUpperBound(0, this);
        } else if (X.getLB() > 0) {
            Y.updateLowerBound(0, this);
        } else {
            if (Y.getUB() < 0) {
                X.updateUpperBound(0, this);
            } else if (Y.getLB() > 0) {
                X.updateLowerBound(0, this);
            }
        }
    }

    private void positiveStrict() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateUpperBound(-1, this);
        } else {
            if (X.getLB() >= 0) {
                X.updateLowerBound(1, this);
                Y.updateLowerBound(1, this);
            } else {
                if (Y.getUB() < 0) {
                    X.updateUpperBound(-1, this);
                } else if (Y.getLB() >= 0) {
                    X.updateLowerBound(1, this);
                    Y.updateLowerBound(1, this);
                }
            }
        }
    }

    private void negativeStrict() throws ContradictionException {
        if (X.getUB() < 0) {
            Y.updateLowerBound(1, this);
        } else {
            if (X.getLB() >= 0) {
                X.updateLowerBound(1, this);
                Y.updateUpperBound(-1, this);
            } else {
                if (Y.getUB() < 0) {
                    X.updateLowerBound(1, this);
                } else if (Y.getLB() >= 0) {
                    X.updateUpperBound(-1, this);
                    Y.updateLowerBound(1, this);
                }
            }
        }
    }

    private void nul() throws ContradictionException {
        if (!X.contains(0)) {
            Y.instantiateTo(0, this);
        } else if (!Y.contains(0)) {
            X.instantiateTo(0, this);
        } else if (X == Y) {
            Y.instantiateTo(0, this);
        }
    }

    //****************************************************************************************************************//
    //******* INSTANTIATION  *****************************************************************************************//
    //****************************************************************************************************************//

    private void instantiated(IntVar X, IntVar Y) throws ContradictionException {
        if (X.isInstantiated() && Y.isInstantiated()) {
            if (X.getValue() * Y.getValue() != Z.getValue()) {
                fails(); // TODO: could be more precise, for explanation purpose
            }
        } else if (X.isInstantiated()) {
            if (X.getValue() != 0) {
                double a = (double) Z.getValue() / (double) X.getValue();
                if (Math.abs(a - Math.round(a)) > 0.001) {
                    fails(); // TODO: could be more precise, for explanation purpose
                }
                Y.instantiateTo((int) Math.round(a), this);        // fix v1
                setPassive();
            }
        } else {
            double z = Z.getValue();
            if (z >= 0) {
                if (X.getLB() > 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateBounds((int) ceil(b), (int) a, this);
                }
                if (X.getUB() < 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateBounds((int) b, (int) a, this);
                }
            } else {
                if (X.getLB() > 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateBounds((int) a, (int) b, this);
                }
                if (X.getUB() < 0) {
                    double a = z / (double) X.getLB();
                    double b = z / (double) X.getUB();
                    Y.updateBounds((int) a, (int) b, this);
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
        v2.updateLowerBound(lb, this);
        while (lb <= ub && (!Z.contains(value * ub))) {
            ub = v2.previousValue(ub);
        }
        v2.updateUpperBound(ub, this);

    }

}
