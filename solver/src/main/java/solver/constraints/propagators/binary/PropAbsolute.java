/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.propagators.binary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ValueRemoval;
import solver.explanations.VariableState;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Enforces X = |Y|
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropAbsolute extends Propagator<IntVar> {

    protected final RemProc rem_proc;

    public PropAbsolute(IntVar X, IntVar Y, Solver solver,
                        Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(ArrayUtils.toArray(X, Y), solver, intVarPropagatorConstraint, PropagatorPriority.BINARY, false);
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
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) { // filter from X to Y
            if (EventType.isInstantiate(mask) || EventType.isDecupp(mask)) {
                updateLowerBoundofY();
                updateUpperBoundofY();
                updateHolesinY();
            } else {
                eventRecorder.getDeltaMonitor(this, vars[idxVarInProp]).forEach(rem_proc.set(idxVarInProp), EventType.REMOVE);
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
                eventRecorder.getDeltaMonitor(this, vars[idxVarInProp]).forEach(rem_proc.set(idxVarInProp), EventType.REMOVE);
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
                return ESat.eval(vars[0].getValue() == Math.abs(vars[1].getValue()));
            } else if (vars[1].getDomainSize() == 2 &&
                    vars[1].contains(vars[0].getValue()) &&
                    vars[1].contains(-vars[0].getValue())) {
                return ESat.TRUE;
            } else if (!vars[1].contains(vars[0].getValue()) &&
                    !vars[1].contains(-vars[0].getValue())) {
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
        return String.format("%s = |%s|", vars[0].toString(), vars[1].toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void updateLowerBoundofX() throws ContradictionException {
        int a0 = vars[1].nextValue(-1);
        int b0 = Math.max(Integer.MIN_VALUE + 1, vars[1].previousValue(1));
        vars[0].updateLowerBound(Math.min(a0, -b0), this);
    }

    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(Math.abs(vars[1].getLB()), Math.abs(vars[1].getUB())), this);

    }

    protected void updateHolesinX() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[1].hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE, right = Integer.MIN_VALUE;
            int ub = vars[0].getUB();
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!vars[1].contains(value) && !vars[1].contains(-value)) {
                    if (value == right + 1) {
                        right = value;
                    } else {
                        vars[0].removeInterval(left, right, this);
                        left = right = value;
                    }
                }
            }
            vars[0].removeInterval(left, right, this);
        } else {
            int value = vars[0].getLB();
            int nlb = value - 1;
            while (nlb == value - 1) {
                if (!vars[1].contains(value) && !vars[1].contains(-value)) {
                    nlb = value;
                }
                value = vars[0].nextValue(value);
            }
            vars[0].updateLowerBound(nlb, this);

            value = vars[0].getUB();
            int nub = value + 1;
            while (nub == value + 1) {
                if (!vars[1].contains(value) && !vars[1].contains(-value)) {
                    nub = value;
                }
                value = vars[0].previousValue(value);
            }
            vars[0].updateUpperBound(nub, this);
        }
    }

    protected void updateHoleinX(int remVal) throws ContradictionException {
        if (!vars[1].contains(-remVal)) {
            vars[0].removeValue(Math.abs(remVal), this);
        }
    }

    protected void updateLowerBoundofY() throws ContradictionException {
        vars[1].updateLowerBound(-vars[0].getUB(), this);
    }

    protected void updateUpperBoundofY() throws ContradictionException {
        vars[1].updateUpperBound(vars[0].getUB(), this);
    }

    protected void updateHolesinY() throws ContradictionException {
        // remove intervals to deal with consecutive value removal and upper bound modification
        if (vars[0].hasEnumeratedDomain()) {
            int left = Integer.MIN_VALUE, right = Integer.MIN_VALUE;
            int ub = vars[1].getUB();
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(Math.abs(value))) {
                    if (value == right + 1) {
                        right = value;
                    } else {
                        vars[1].removeInterval(left, right, this);
                        left = right = value;
                    }
                }
            }
            vars[1].removeInterval(left, right, this);
        } else {
            int value = vars[1].getLB();
            int nlb = value - 1;
            while (nlb == value - 1) {
                if (!vars[0].contains(Math.abs(value))) {
                    nlb = value;
                }
                value = vars[1].nextValue(value);
            }
            vars[1].updateLowerBound(nlb, this);

            value = vars[1].getUB();
            int nub = value + 1;
            while (nub == value + 1) {
                if (!vars[0].contains(Math.abs(value))) {
                    nub = value;
                }
                value = vars[1].previousValue(value);
            }
            vars[1].updateUpperBound(nub, this);
        }
    }

    protected void updateHoleinY(int remVal) throws ContradictionException {
        vars[1].removeValue(remVal, this);
        vars[1].removeValue(-remVal, this);
    }


    @Override
    public Explanation explain(Deduction d) {
//        return super.explain(d);
        if (d.getVar() == vars[0]) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add (vars[1].explain(VariableState.REM,((ValueRemoval) d).getVal()));
                explanation.add( vars[1].explain(VariableState.REM,- ((ValueRemoval) d).getVal()));
            }
            else {
                throw new UnsupportedOperationException("PropAbsolute only knows how to explain ValueRemovals");
            }
            return explanation;
        } else if (d.getVar() == vars[1]) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add (vars[0].explain(VariableState.REM, Math.abs(((ValueRemoval) d).getVal())));
            }
            else {
                throw new UnsupportedOperationException("PropBasolute only knows how to explain ValueRemovals");
            }
            return explanation;
        }
        else {
            return super.explain(d);
        }
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropAbsolute p;
        private int idxVar;

        public RemProc(PropAbsolute p) {
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
