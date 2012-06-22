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

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
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
import solver.variables.delta.IIntDeltaMonitor;

/**
 * X = Y
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
@PropAnn(tested = PropAnn.Status.CORRECTION)
public final class PropEqualX_Y extends Propagator<IntVar> {

    IntVar x;
    IntVar y;

    IIntDeltaMonitor[] idms;

    protected final RemProc rem_proc;

    @SuppressWarnings({"unchecked"})
    public PropEqualX_Y(IntVar x, IntVar y, Solver solver, IntConstraint constraint) {
        super(ArrayUtils.toArray(x, y), solver, constraint, PropagatorPriority.BINARY, true);
        this.x = x;
        this.y = y;
        idms = new IIntDeltaMonitor[2];
        idms[0] = x.monitorDelta(this);
        idms[1] = y.monitorDelta(this);
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        int et = EventType.INSTANTIATE.mask + EventType.DECUPP.mask + EventType.INCLOW.mask;
        if (vars[vIdx].hasEnumeratedDomain()) {
            et += EventType.REMOVE.mask;
        }
        return et;
    }

    private void updateInfX() throws ContradictionException {
        x.updateLowerBound(y.getLB(), this);
    }

    private void updateInfY() throws ContradictionException {
        y.updateLowerBound(x.getLB(), this);
    }

    private void updateSupX() throws ContradictionException {
        x.updateUpperBound(y.getUB(), this);
    }

    private void updateSupY() throws ContradictionException {
        y.updateUpperBound(x.getUB(), this);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateInfX();
        updateSupX();
        updateInfY();
        updateSupY();
        // ensure that, in case of enumerated domains,  holes are also propagated
        if (y.hasEnumeratedDomain() && x.hasEnumeratedDomain()) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!(y.contains(val))) {
                    x.removeValue(val, this);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!(x.contains(val))) {
                    y.removeValue(val, this);
                }
            }
        }
        if (x.instantiated() && y.instantiated()) {
            // no more test should be done on the value,
            // filtering algo ensures that both are assigned to the same value
            setPassive();
        }
		for(int i=0;i<idms.length;i++){
			idms[i].unfreeze();
		}
    }


    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
            setPassive();
        } else {
            if (EventType.isInclow(mask)) {
                this.awakeOnLow(varIdx);
            }
            if (EventType.isDecupp(mask)) {
                this.awakeOnUpp(varIdx);
            }
            if (EventType.isRemove(mask)) {
                idms[varIdx].freeze();
                idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
                idms[varIdx].unfreeze();
//                eventRecorder.getDeltaMonitor(this, vars[varIdx]).forEach(rem_proc.set(varIdx), EventType.REMOVE);
            }
        }
    }

    void awakeOnInst(int index) throws ContradictionException {
        if (index == 0) {
            y.instantiateTo(x.getValue(), this);
        } else {
            x.instantiateTo(y.getValue(), this);
        }
    }

    void awakeOnLow(int index) throws ContradictionException {
        if (index == 0) updateInfY();
        else updateInfX();
    }

    void awakeOnUpp(int index) throws ContradictionException {
        if (index == 0) updateSupY();
        else updateSupX();
    }

    void awakeOnRem(int index, int val) throws ContradictionException {
        if (index == 0) y.removeValue(val, this);
        else {
            x.removeValue(val, this);
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB()) ||
                (x.getLB() > y.getUB()))
            return ESat.FALSE;
        else if (x.instantiated() &&
                y.instantiated() &&
                (x.getValue() == y.getValue()))
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropEqualX_Y p;
        private int idxVar;

        public RemProc(PropEqualX_Y p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.awakeOnRem(idxVar, i);
        }
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        bf.append("prop(").append(vars[0].getName()).append(".EQ.").append(vars[1].getName()).append(")");
        return bf.toString();
    }

    @Override
    public Explanation explain(Deduction d) {
        //     return super.explain(d);

        if (d.getVar() == x) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add(y.explain(VariableState.REM, ((ValueRemoval) d).getVal()));
            } else {
                throw new UnsupportedOperationException("PropEqualXY only knows how to explain ValueRemovals");
            }
            return explanation;
        } else if (d.getVar() == y) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add(x.explain(VariableState.REM, ((ValueRemoval) d).getVal()));
            } else {
                throw new UnsupportedOperationException("PropEqualXY only knows how to explain ValueRemovals");
            }
            return explanation;
        } else {
            return super.explain(d);
        }

    }
}
