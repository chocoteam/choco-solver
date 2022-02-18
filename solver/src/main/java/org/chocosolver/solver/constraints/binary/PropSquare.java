/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
        if (vars[1].isInstantiated()) {
            vars[0].instantiateTo(sqr(vars[1].getValue()), this);
        }
    }


    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < 0) {
            return ESat.FALSE;
        } else if (vars[0].isInstantiated()) {
            if (vars[1].isInstantiated()) {
                return ESat.eval(vars[0].getValue() == sqr(vars[1].getValue()));
            } else if (vars[1].getDomainSize() == 2 &&
                    vars[1].contains(-floor_sqrt(vars[0].getValue())) &&
                    vars[1].contains(-floor_sqrt(vars[0].getValue()))) {
                return ESat.TRUE;
            } else if (!vars[1].contains(floor_sqrt(vars[0].getValue())) &&
                    !vars[1].contains(-floor_sqrt(vars[0].getValue()))) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
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
        if (n < 0)
            return 0;
        return (int) Math.floor(Math.sqrt(n));
    }

    private static int ceil_sqrt(int n) {
        if (n < 0)
            return 0;
        return (int) Math.ceil(Math.sqrt(n));
    }

    private static int sqr(int n) {
        if (n > Integer.MAX_VALUE / 2 || n < Integer.MIN_VALUE / 2) {
            return Integer.MAX_VALUE;
        }
        return n * n;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void updateLowerBoundofX() throws ContradictionException {
        int a0 = vars[1].nextValue(-1);
        int b0 = Math.max(Integer.MIN_VALUE + 1, vars[1].previousValue(1));
        vars[0].updateLowerBound(Math.min(sqr(a0), sqr(b0)), this);
    }

    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(sqr(vars[1].getLB()), sqr(vars[1].getUB())), this);

    }

    protected boolean updateHolesinX() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (bothEnum) {
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!(MathUtils.isPerfectSquare(value) &&
                        (vars[1].contains(floor_sqrt(value)) || vars[1].contains(-floor_sqrt(value))))) {
                    vrms.add(value);
                }
            }
            return vars[0].removeValues(vrms, this);
        } else if (vars[0].hasEnumeratedDomain()) {
            int value = vars[0].getLB();
            int nlb = value - 1;
            while (nlb == value - 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nlb = value;
                }
                value = vars[0].nextValue(value);
            }
            boolean filter = vars[0].updateLowerBound(nlb, this);

            value = vars[0].getUB();
            int nub = value + 1;
            while (nub == value + 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nub = value;
                }
                value = vars[0].previousValue(value);
            }
            return filter | vars[0].updateUpperBound(nub, this);
        }
        return false;
    }

    protected boolean updateLowerBoundofY() throws ContradictionException {
        return vars[1].updateLowerBound(-ceil_sqrt(vars[0].getUB()), this);
    }

    protected boolean updateUpperBoundofY() throws ContradictionException {
        return vars[1].updateUpperBound(floor_sqrt(vars[0].getUB()), this);
    }

    protected boolean updateHolesinY() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (bothEnum) {
            int ub = vars[1].getUB();
            vrms.clear();
            vrms.setOffset(vars[1].getLB());
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(sqr(value))) {
                    vrms.add(value);
                }
            }
            return vars[1].removeValues(vrms, this);
        } else if (vars[1].hasEnumeratedDomain()) {
            int lb = vars[1].getLB();
            int ub = vars[1].getUB();
            while (!vars[0].contains(sqr(lb))) {
                lb = vars[1].nextValue(lb);
                if (lb > ub) break;
            }
            boolean filter = vars[1].updateLowerBound(lb, this);

            while (!vars[0].contains(sqr(ub))) {
                ub = vars[1].nextValue(ub);
                if (ub < lb) break;
            }
            return filter | vars[1].updateUpperBound(ub, this);
        }
        return false;
    }


}
