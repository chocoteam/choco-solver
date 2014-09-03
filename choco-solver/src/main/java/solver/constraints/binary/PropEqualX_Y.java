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

package solver.constraints.binary;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ValueRemoval;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;
import util.tools.ArrayUtils;

/**
 * X = Y
 * <p/>
 * Ensures Arc-Consistency
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 1 oct. 2010
 */
public final class PropEqualX_Y extends Propagator<IntVar> {

    private IntVar x, y;
    // enumerated domains
    private boolean bothEnumerated;
    private IIntDeltaMonitor[] idms;
    private RemProc rem_proc;
    private int indexToFilter;

    public PropEqualX_Y(IntVar x, IntVar y) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
            bothEnumerated = true;
            idms = new IIntDeltaMonitor[2];
            idms[0] = vars[0].monitorDelta(this);
            idms[1] = vars[1].monitorDelta(this);
            rem_proc = new RemProc();
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (bothEnumerated)
            return EventType.INT_ALL_MASK();
        else
            return EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    private void updateBounds() throws ContradictionException {
        while (x.updateLowerBound(y.getLB(), aCause) | y.updateLowerBound(x.getLB(), aCause)) ;
        while (x.updateUpperBound(y.getUB(), aCause) | y.updateUpperBound(x.getUB(), aCause)) ;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateBounds();
        // ensure that, in case of enumerated domains,  holes are also propagated
        if (bothEnumerated) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!(y.contains(val))) {
                    x.removeValue(val, aCause);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!(x.contains(val))) {
                    y.removeValue(val, aCause);
                }
            }
            idms[0].unfreeze();
            idms[1].unfreeze();
        }
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            // no more test should be done on the value,
            // filtering algo ensures that both are assigned to the same value
            setPassive();
        }
    }


    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        updateBounds();
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            setPassive();
        } else if (bothEnumerated) {
            indexToFilter = 1 - varIdx;
            idms[varIdx].freeze();
            idms[varIdx].forEach(rem_proc, EventType.REMOVE);
            idms[varIdx].unfreeze();
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB()) ||
                (x.getLB() > y.getUB()) ||
                x.hasEnumeratedDomain() && y.hasEnumeratedDomain() && !match()
                )
            return ESat.FALSE;
        else if (x.isInstantiated() &&
                y.isInstantiated() &&
                (x.getValue() == y.getValue()))
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    private boolean match() {
        int lb = x.getLB();
        int ub = x.getUB();
        for (; lb <= ub; lb = x.nextValue(lb)) {
            if (y.contains(lb)) return true;
        }
        return false;
    }

    private class RemProc implements IntProcedure {
        @Override
        public void execute(int i) throws ContradictionException {
            vars[indexToFilter].removeValue(i, aCause);
        }
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        bf.append("prop(").append(vars[0].getName()).append(".EQ.").append(vars[1].getName()).append(")");
        return bf.toString();
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        if (d.getVar() == x) {
            e.add(solver.getExplainer().getPropagatorActivation(this));
            e.add(aCause);
            if (d.getmType() == Deduction.Type.ValRem) {
                y.explain(VariableState.REM, ((ValueRemoval) d).getVal(), e);
            } else {
                throw new UnsupportedOperationException("PropEqualXY only knows how to explain ValueRemovals");
            }
        } else if (d.getVar() == y) {
            e.add(solver.getExplainer().getPropagatorActivation(this));
            e.add(aCause);
            if (d.getmType() == Deduction.Type.ValRem) {
                x.explain(VariableState.REM, ((ValueRemoval) d).getVal(), e);
            } else {
                throw new UnsupportedOperationException("PropEqualXY only knows how to explain ValueRemovals");
            }
        } else {
            super.explain(d, e);
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.vars[0].duplicate(solver, identitymap);
            IntVar X = (IntVar) identitymap.get(this.vars[0]);
            this.vars[1].duplicate(solver, identitymap);
            IntVar Y = (IntVar) identitymap.get(this.vars[1]);

            identitymap.put(this, new PropEqualX_Y(X, Y));
        }
    }
}
