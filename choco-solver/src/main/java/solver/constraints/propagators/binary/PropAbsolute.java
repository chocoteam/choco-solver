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
import common.ESat;
import common.util.procedure.UnaryIntProcedure;
import common.util.tools.ArrayUtils;
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

/**
 * Enforces X = |Y|
 * <br/>
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 18/05/11
 */
@PropAnn(tested = PropAnn.Status.EXPLAINED)
public class PropAbsolute extends Propagator<IntVar> {

    protected RemProc rem_proc;
    protected IIntDeltaMonitor[] idms;
    protected IntVar X, Y;
    protected boolean bothEnumerated;

    public PropAbsolute(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, false);
        this.X = vars[0];
        this.Y = vars[1];
        bothEnumerated = X.hasEnumeratedDomain() && Y.hasEnumeratedDomain();
        if (bothEnumerated) {
            rem_proc = new RemProc();
            this.idms = new IIntDeltaMonitor[this.vars.length];
            for (int i = 0; i < this.vars.length; i++) {
                idms[i] = vars[i].hasEnumeratedDomain() ? this.vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
            }
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (bothEnumerated) {
            return EventType.INT_ALL_MASK();
        } else {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
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

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        X.updateLowerBound(0, aCause);
        setBounds();
        if (bothEnumerated) {
            enumeratedFiltering();
            idms[0].unfreeze();
            idms[1].unfreeze();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (bothEnumerated) {
            idms[varIdx].freeze();
            idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
            idms[varIdx].unfreeze();
        } else {
            setBounds();
        }
    }

    private void setBounds() throws ContradictionException {
        // X = |Y|
        int max = X.getUB();
        int min = X.getLB();
        Y.updateUpperBound(max, aCause);
        Y.updateLowerBound(-max, aCause);
        Y.removeInterval(1 - min, min - 1, aCause);
        /////////////////////////////////////////////////
        int prevLB = X.getLB();
        int prevUB = X.getUB();
        min = Y.getLB();
        max = Y.getUB();
        if (max <= 0) {
            X.updateLowerBound(-max, aCause);
            X.updateUpperBound(-min, aCause);
        } else if (min >= 0) {
            X.updateLowerBound(min, aCause);
            X.updateUpperBound(max, aCause);
        } else {
            if (Y.hasEnumeratedDomain()) {
                int mP = Y.nextValue(-1);
                int mN = -Y.previousValue(1);
                X.updateLowerBound(Math.min(mP, mN), aCause);
            }
            X.updateUpperBound(Math.max(-min, max), aCause);
        }
        if (prevLB != X.getLB() || prevUB != X.getUB()) setBounds();
    }

    private void enumeratedFiltering() throws ContradictionException {
        int min = X.getLB();
        int max = X.getUB();
        for (int v = min; v <= max; v = X.nextValue(v)) {
            if (!(Y.contains(v) || Y.contains(-v))) {
                X.removeValue(v, aCause);
            }
        }
        min = Y.getLB();
        max = Y.getUB();
        for (int v = min; v <= max; v = Y.nextValue(v)) {
            if (!(X.contains(Math.abs(v)))) {
                Y.removeValue(v, aCause);
            }
        }
    }

    private class RemProc implements UnaryIntProcedure<Integer> {
        private int var;

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.var = idxVar;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
            if (var == 0) {
                vars[1].removeValue(val, aCause);
                vars[1].removeValue(-val, aCause);
            } else {
                if (!vars[1].contains(-val))
                    vars[0].removeValue(Math.abs(val), aCause);
            }
        }
    }

    //***********************************************************************************
    // EXPLANATIONS
    //***********************************************************************************

    @Override
    public void explain(Deduction d, Explanation e) {
        if (d.getVar() == vars[0]) {
            e.add(aCause);
            if (d.getmType() == Deduction.Type.ValRem) {
                vars[1].explain(VariableState.REM, ((ValueRemoval) d).getVal(), e);
                vars[1].explain(VariableState.REM, -((ValueRemoval) d).getVal(), e);
            } else {
                throw new UnsupportedOperationException("PropAbsolute only knows how to explain ValueRemovals");
            }
        } else if (d.getVar() == vars[1]) {
            e.add(aCause);
            if (d.getmType() == Deduction.Type.ValRem) {
                vars[0].explain(VariableState.REM, Math.abs(((ValueRemoval) d).getVal()), e);
            } else {
                throw new UnsupportedOperationException("PropAbsolute only knows how to explain ValueRemovals");
            }
        } else {
            super.explain(d, e);
        }
    }
}
