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

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * X*Y=Z filters from left to right
 *
 * @author Jean-Guillaume Fages
 * @since Dec 2012
 */
public class PropTimesXY extends Propagator<IntVar> {

    private IntVar X, Y, Z;

    public PropTimesXY(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y}, PropagatorPriority.UNARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        this.Z = z;
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
                fails(); // TODO: could be more precise, for explanation purpose
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
