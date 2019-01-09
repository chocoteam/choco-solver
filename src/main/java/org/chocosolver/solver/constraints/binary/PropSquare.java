/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Enforces X = Y^2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropSquare extends Propagator<IntVar> {

    private final RemProc rem_proc;
    private final IIntDeltaMonitor[] idms;
    private final IntIterableBitSet vrms;

    public PropSquare(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, true);
        this.idms = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            idms[i] = vars[i].hasEnumeratedDomain() ? vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
        }
        vrms = new IntIterableBitSet();
        rem_proc = new RemProc(this);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Filter on X from Y
        updateLowerBoundofX();
        updateUpperBoundofX();
        updateHolesinX();

        // Filter on Y from X
        updateLowerBoundofY();
        updateUpperBoundofY();
        updateHolesinY();
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx == 0) { // filter from X to Y
            if (IntEventType.isInstantiate(mask) || IntEventType.isBound(mask)) {
                updateLowerBoundofY();
                updateUpperBoundofY();
                updateHolesinY();
            } else {
                idms[varIdx].freeze();
                idms[varIdx].forEachRemVal(rem_proc.set(varIdx));
                idms[varIdx].unfreeze();
//                updateHolesinY();
            }
        } else { // filter from Y to X
            // <nj> originally we had the following condition
//            if (EventType.isRemove(mask) && EventType.isRemove(getPropagationConditions(idxVarInProp))) {
            // this led to a nasty bug due to event promotion

            if (IntEventType.isInstantiate(mask) || IntEventType.isBound(mask)) {
                updateLowerBoundofX();
                updateUpperBoundofX();
                updateHolesinX();
            } else {
                idms[varIdx].freeze();
                idms[varIdx].forEachRemVal(rem_proc.set(varIdx));
                idms[varIdx].unfreeze();
//                updateHolesinX();
            }
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

    protected void updateHolesinX() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[1].hasEnumeratedDomain()) {
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    vrms.add(value);
                }
            }
            vars[0].removeValues(vrms, this);
        } else {
            int value = vars[0].getLB();
            int nlb = value - 1;
            while (nlb == value - 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nlb = value;
                }
                value = vars[0].nextValue(value);
            }
            vars[0].updateLowerBound(nlb, this);

            value = vars[0].getUB();
            int nub = value + 1;
            while (nub == value + 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nub = value;
                }
                value = vars[0].previousValue(value);
            }
            vars[0].updateUpperBound(nub, this);
        }
    }

    protected void updateHoleinX(int remVal) throws ContradictionException {
        if (!vars[1].contains(-remVal)) {
            vars[0].removeValue(sqr(remVal), this);
        }
    }

    protected void updateLowerBoundofY() throws ContradictionException {
        vars[1].updateLowerBound(-ceil_sqrt(vars[0].getUB()), this);
    }

    protected void updateUpperBoundofY() throws ContradictionException {
        vars[1].updateUpperBound(floor_sqrt(vars[0].getUB()), this);
    }

    protected void updateHolesinY() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[0].hasEnumeratedDomain()) {
            int ub = vars[1].getUB();
            vrms.clear();
            vrms.setOffset(vars[1].getLB());
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(sqr(value))) {
                    vrms.add(value);
                }
            }
            vars[1].removeValues(vrms, this);
        } else {
            int lb = vars[1].getLB();
            int ub = vars[1].getUB();
            while (!vars[0].contains(sqr(lb))) {
                lb = vars[1].nextValue(lb + 1);
                if (lb > ub) break;
            }
            vars[1].updateLowerBound(lb, this);

            while (!vars[0].contains(sqr(ub))) {
                ub = vars[1].nextValue(ub + 1);
                if (ub < lb) break;
            }
            vars[1].updateUpperBound(ub, this);
        }
    }

    protected void updateHoleinY(int remVal) throws ContradictionException {
        vars[1].removeValue(floor_sqrt(remVal), this);
        vars[1].removeValue(-ceil_sqrt(remVal), this);
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropSquare p;
        private int idxVar;

        public RemProc(PropSquare p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            if (idxVar == 0) {
                p.updateHoleinY(i);
            } else {
                p.updateHoleinX(i);
            }
        }
    }

}
