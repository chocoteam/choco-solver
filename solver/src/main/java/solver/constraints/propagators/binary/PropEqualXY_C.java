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
import common.util.procedure.IntProcedure;
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
 * X + Y = C
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 1 oct. 2010
 */

@PropAnn(tested = PropAnn.Status.EXPLAINED)
public final class PropEqualXY_C extends Propagator<IntVar> {

    private IntVar x,y;
	private final int cste;
	// incremental filtering of enumerated domains
	private boolean bothEnumerated;
	private IIntDeltaMonitor[] idms;
	private RemProc rem_proc;
	private int indexToFilter;

    @SuppressWarnings({"unchecked"})
    public PropEqualXY_C(IntVar[] vars, int c) {
        super(vars.clone(), PropagatorPriority.BINARY, true);
        this.x = vars[0];
		this.y = vars[1];
		this.cste = c;
		if(x.hasEnumeratedDomain() && y.hasEnumeratedDomain()){
			bothEnumerated = true;
			idms = new IIntDeltaMonitor[2];
			idms[0] = vars[0].monitorDelta(this);
			idms[1] = vars[1].monitorDelta(this);
			rem_proc = new RemProc();
		}
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if(bothEnumerated)
			return EventType.INT_ALL_MASK();
		else
			return EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		updateBounds();
        x.updateLowerBound(cste - y.getUB(), aCause);
        x.updateUpperBound(cste - y.getLB(), aCause);
        y.updateLowerBound(cste - x.getUB(), aCause);
        y.updateUpperBound(cste - x.getLB(), aCause);
        // ensure that, in case of enumerated domains, holes are also propagated
        if (bothEnumerated) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!y.contains(cste - val)) {
                    x.removeValue(val, aCause);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!x.contains(cste - val)) {
                    y.removeValue(val, aCause);
                }
            }
			idms[0].unfreeze();
			idms[1].unfreeze();
		}
		if (x.instantiated()) {
			assert (y.instantiated());
			setPassive();
		}
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        updateBounds();
		if(x.instantiated()){
			assert (y.instantiated());
			setPassive();
		}else if(bothEnumerated){
			indexToFilter = 1-varIdx;
			idms[varIdx].freeze();
			idms[varIdx].forEach(rem_proc, EventType.REMOVE);
			idms[varIdx].unfreeze();
		}
    }

	private void updateBounds() throws ContradictionException {
		y.updateUpperBound(cste - x.getLB(), aCause);
		y.updateLowerBound(cste - x.getUB(), aCause);
		x.updateUpperBound(cste - y.getLB(), aCause);
		x.updateLowerBound(cste - y.getUB(), aCause);
		if(y.getLB()!=cste-x.getUB() || y.getUB()!=cste-x.getLB()){
			updateBounds();
		}
	}

    @Override
    public ESat isEntailed() {
        if ((x.getUB() + y.getUB() < cste) ||
                (x.getLB() + y.getLB() > cste))
            return ESat.FALSE;
        else if (x.instantiated() &&
                y.instantiated() &&
                (x.getValue() + y.getValue() == cste))     // <nj> was false
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }


    @Override
    public void explain(Deduction d, Explanation e) {
        if (d.getVar() == x) {
            e.add(aCause);
            if (d instanceof ValueRemoval) {
                y.explain(VariableState.REM, cste - ((ValueRemoval) d).getVal(), e);
            } else {
                throw new UnsupportedOperationException("PropEqualXY_C only knows how to explain ValueRemovals");
            }
        } else if (d.getVar() == y) {
            e.add(aCause);
            if (d instanceof ValueRemoval) {
                x.explain(VariableState.REM, cste - ((ValueRemoval) d).getVal(), e);
            } else {
                throw new UnsupportedOperationException("PropEqualXY_C only knows how to explain ValueRemovals");
            }
        } else {
            super.explain(d, e);
        }
    }

    private class RemProc implements IntProcedure {
        @Override
        public void execute(int i) throws ContradictionException {
            vars[indexToFilter].removeValue(cste-i,aCause);
        }
    }
}
