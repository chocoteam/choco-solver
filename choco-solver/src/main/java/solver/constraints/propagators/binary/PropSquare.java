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
package solver.constraints.propagators.binary;

import choco.annotations.PropAnn;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ValueRemoval;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.procedure.UnaryIntProcedure;
import util.tools.ArrayUtils;

/**
 * Enforces X = Y^2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
@PropAnn(tested = PropAnn.Status.EXPLAINED)
public class PropSquare extends Propagator<IntVar> {

    protected final RemProc rem_proc;
    protected final IIntDeltaMonitor[] idms;

    public PropSquare(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, false, true);
        this.idms = new IIntDeltaMonitor[vars.length];
        for (int i = 0; i < vars.length; i++) {
            idms[i] = vars[i].hasEnumeratedDomain() ? vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
        }
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INT_ALL_MASK();
        } else {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
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
            if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
                updateLowerBoundofY();
                updateUpperBoundofY();
                updateHolesinY();
            } else {
                idms[varIdx].freeze();
                idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
                idms[varIdx].unfreeze();
//                updateHolesinY();
            }
        } else { // filter from Y to X
            // <nj> originally we had the following condition
//            if (EventType.isRemove(mask) && EventType.isRemove(getPropagationConditions(idxVarInProp))) {
            // this led to a nasty bug due to event promotion

            if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
                updateLowerBoundofX();
                updateUpperBoundofX();
                updateHolesinX();
            } else {
                idms[varIdx].freeze();
                idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
                idms[varIdx].unfreeze();
//                updateHolesinX();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < 0) {
            return ESat.FALSE;
        } else if (vars[0].instantiated()) {
            if (vars[1].instantiated()) {
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
        vars[0].updateLowerBound(Math.min(sqr(a0), sqr(b0)), aCause);
    }

    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(sqr(vars[1].getLB()), sqr(vars[1].getUB())), aCause);

    }

    protected void updateHolesinX() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[1].hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE, right = Integer.MIN_VALUE;
            int ub = vars[0].getUB();
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    if (value == right + 1) {
                        right = value;
                    } else {
                        vars[0].removeInterval(left, right, aCause);
                        left = right = value;
                    }
                }
            }
            vars[0].removeInterval(left, right, aCause);
        } else {
            int value = vars[0].getLB();
            int nlb = value - 1;
            while (nlb == value - 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nlb = value;
                }
                value = vars[0].nextValue(value);
            }
            vars[0].updateLowerBound(nlb, aCause);

            value = vars[0].getUB();
            int nub = value + 1;
            while (nub == value + 1) {
                if (!vars[1].contains(floor_sqrt(value)) && !vars[1].contains(-floor_sqrt(value))) {
                    nub = value;
                }
                value = vars[0].previousValue(value);
            }
            vars[0].updateUpperBound(nub, aCause);
        }
    }

    protected void updateHoleinX(int remVal) throws ContradictionException {
        if (!vars[1].contains(-remVal)) {
            vars[0].removeValue(sqr(remVal), aCause);
        }
    }

    protected void updateLowerBoundofY() throws ContradictionException {
        vars[1].updateLowerBound(-ceil_sqrt(vars[0].getUB()), aCause);
    }

    protected void updateUpperBoundofY() throws ContradictionException {
        vars[1].updateUpperBound(floor_sqrt(vars[0].getUB()), aCause);
    }

    protected void updateHolesinY() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[0].hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE, right = Integer.MIN_VALUE;
            int ub = vars[1].getUB();
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(sqr(value))) {
                    if (value == right + 1) {
                        right = value;
                    } else {
                        vars[1].removeInterval(left, right, aCause);
                        left = right = value;
                    }
                }
            }
            vars[1].removeInterval(left, right, aCause);
        } else {
            int lb = vars[1].getLB();
            int ub = vars[1].getUB();
            while (!vars[0].contains(sqr(lb))) {
                lb = vars[1].nextValue(lb + 1);
                if (lb > ub) break;
            }
            vars[1].updateLowerBound(lb, aCause);

            while (!vars[0].contains(sqr(ub))) {
                ub = vars[1].nextValue(ub + 1);
                if (ub < lb) break;
            }
            vars[1].updateUpperBound(ub, aCause);
        }
    }

    protected void updateHoleinY(int remVal) throws ContradictionException {
        vars[1].removeValue(floor_sqrt(remVal), aCause);
        vars[1].removeValue(-ceil_sqrt(remVal), aCause);
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        //        return super.explain(d);
        if (d.getVar() == vars[0]) {
            e.add(solver.getExplainer().getPropagatorActivation(this));
            e.add(aCause);
            if (d instanceof ValueRemoval) {
                int val = (int) Math.sqrt(((ValueRemoval) d).getVal());
                vars[1].explain(VariableState.REM, val, e);
                vars[1].explain(VariableState.REM, -val, e);
            } else {
                throw new UnsupportedOperationException("PropSquare only knows how to explain ValueRemovals");
            }
        } else if (d.getVar() == vars[1]) {
            e.add(solver.getExplainer().getPropagatorActivation(this));
            e.add(aCause);
            if (d instanceof ValueRemoval) {
                int val = ((ValueRemoval) d).getVal() ^ 2;
                vars[0].explain(VariableState.REM, val, e);
            } else {
                throw new UnsupportedOperationException("PropSquare only knows how to explain ValueRemovals");
            }
        } else {
            super.explain(d, e);
        }
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
