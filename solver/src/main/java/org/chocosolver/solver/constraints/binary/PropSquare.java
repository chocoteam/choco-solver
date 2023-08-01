/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.MathUtils;

/**
 * Enforces X = Y^2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropSquare extends Propagator<IntVar> {

    private final IntIterableBitSet vrms;
    private final boolean bothEnum;

    public PropSquare(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, false);
        bothEnum = X.hasEnumeratedDomain() && Y.hasEnumeratedDomain();
        vrms = new IntIterableBitSet();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return bothEnum ? IntEventType.all() : IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        do {
            setBounds();
        } while (updateHolesinX() | updateHolesinY());
    }


    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < 0) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.eval(vars[0].getValue() == sqr(vars[1].getValue()));
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return String.format("%s = %s^2", vars[0].toString(), vars[1].toString());
    }

    private void setBounds() throws ContradictionException {
        updateLowerBoundofX();
        updateUpperBoundofX();
        if(updateLowerBoundofY() | updateUpperBoundofY()){
            setBounds();
        }
    }

    private static int floor_sqrt(int n) {
        if (n < 0) {
            return 0;
        }
        return (int) Math.floor(Math.sqrt(n));
    }

    private static int ceil_sqrt(int n) {
        if (n < 0) {
            return 0;
        }
        return (int) Math.ceil(Math.sqrt(n));
    }

    private static int sqr(int n) {
        if (n > Integer.MAX_VALUE / 2 || n < Integer.MIN_VALUE / 2) {
            return Integer.MAX_VALUE;
        }
        return n * n;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void updateLowerBoundofX() throws ContradictionException {
        int a0 = vars[1].nextValue(-1);
        int b0 = Math.max(Integer.MIN_VALUE + 1, vars[1].previousValue(1));
        vars[0].updateLowerBound(Math.min(sqr(a0), sqr(b0)), this);
    }

    private void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(sqr(vars[1].getLB()), sqr(vars[1].getUB())), this);
    }

    private boolean updateHolesinX() throws ContradictionException {
        if (!vars[0].hasEnumeratedDomain()) {
            return false;
        }
        boolean impact = false;
        if (model.getSolver().getNodeCount() == 0) { // only at root node propagation
            // check perfect squares once and for all
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!MathUtils.isPerfectSquare(value)) {
                    vrms.add(value);
                }
            }
            impact = vars[0].removeValues(vrms, this);
        }

        // remove intervals to deal with consecutive value removal and upper bound modification
        if (bothEnum) {
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                int sqrt = floor_sqrt(value);
                if (!vars[1].contains(sqrt) && !vars[1].contains(-sqrt)) {
                    vrms.add(value);
                }
            }
            impact |= vars[0].removeValues(vrms, this);
        } 
        return impact;
    }

    private boolean updateLowerBoundofY() throws ContradictionException {
        if (vars[1].getLB() >= 0) {
            return vars[1].updateLowerBound(ceil_sqrt(vars[0].getLB()), this);
        } else {
            return vars[1].updateLowerBound(-floor_sqrt(vars[0].getUB()), this);
        }
    }

    private boolean updateUpperBoundofY() throws ContradictionException {
        if (vars[1].getUB() < 0) {
            return vars[1].updateUpperBound(-ceil_sqrt(vars[0].getLB()), this);
        } else {
            return vars[1].updateUpperBound(floor_sqrt(vars[0].getUB()), this);
        }
    }

    private boolean updateHolesinY() throws ContradictionException {
        if (!vars[1].hasEnumeratedDomain()) {
            return false;
        }
        boolean impact = false;
        // remove interval around 0 based on X LB
        int val = ceil_sqrt(vars[0].getLB()) - 1;
        if (val >= 0) {
            impact = vars[1].removeInterval(-val, val, this);
        }
        // remove values based on X holes
        if (bothEnum) {
            int ub = vars[1].getUB();
            vrms.clear();
            vrms.setOffset(vars[1].getLB());
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(sqr(value))) {
                    vrms.add(value);
                }
            }
            impact |= vars[1].removeValues(vrms, this);
        }
        return impact;
    }
}
