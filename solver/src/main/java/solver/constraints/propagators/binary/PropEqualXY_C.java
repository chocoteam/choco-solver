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

/**
 * X + Y = C
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */

@PropAnn
public final class PropEqualXY_C extends Propagator<IntVar> {

    IntVar x;
    IntVar y;
    int cste;

    protected final RemProc rem_proc;

    @SuppressWarnings({"unchecked"})
    public PropEqualXY_C(IntVar[] vars, int c, Solver solver, IntConstraint constraint) {
        super(vars.clone(), solver, constraint, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    private void updateInfV0() throws ContradictionException {
        x.updateLowerBound(cste - y.getUB(), this, false);
    }

    private void updateInfV1() throws ContradictionException {
        y.updateLowerBound(cste - x.getUB(), this, false);
    }

    private void updateSupV0() throws ContradictionException {
        x.updateUpperBound(cste - y.getLB(), this, false);
    }

    private void updateSupV1() throws ContradictionException {
        y.updateUpperBound(cste - x.getLB(), this, false);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateInfV0();
        updateSupV0();
        updateInfV1();
        updateSupV1();
        // ensure that, in case of enumerated domains, holes are also propagated
        if (y.hasEnumeratedDomain() && x.hasEnumeratedDomain()) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!y.contains(cste - val)) {
                    x.removeValue(val, this);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!x.contains(cste - val)) {
                    y.removeValue(val, this);
                }
            }
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else {
            if (EventType.isBound(mask)) {
                if (EventType.isInclow(mask)) {
                    this.awakeOnLow(varIdx);

                }
                if (EventType.isDecupp(mask)) {
                    this.awakeOnUpp(varIdx);

                }
            }
            if (EventType.isRemove(mask)) {
                eventRecorder.getDeltaMonitor(vars[varIdx]).forEach(rem_proc.set(varIdx), EventType.REMOVE);
            }
        }
    }

    void awakeOnInst(int index) throws ContradictionException {
        if (index == 0) y.instantiateTo(cste - x.getValue(), this, false);
        else x.instantiateTo(cste - y.getValue(), this, false);
    }

    void awakeOnLow(int index) throws ContradictionException {
        if (index == 0) updateSupV1();
        else updateSupV0();
    }

    void awakeOnUpp(int index) throws ContradictionException {
        if (index == 0) updateInfV1();
        else updateInfV0();
    }


    void awakeOnRem(int index, int val) throws ContradictionException {
        if (index == 0) {
            y.removeValue(cste - val, this);
        } else {
            x.removeValue(cste - val, this);
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
    public Explanation explain(Deduction d) {
        if (d.getVar() == x) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add(y.explain(VariableState.REM, cste - ((ValueRemoval) d).getVal()));
            } else {
                throw new UnsupportedOperationException("PropEqualXY_C only knows how to explain ValueRemovals");
            }
            return explanation;
        } else if (d.getVar() == y) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add(x.explain(VariableState.REM, cste - ((ValueRemoval) d).getVal()));
            } else {
                throw new UnsupportedOperationException("PropEqualXY_C only knows how to explain ValueRemovals");
            }
            return explanation;
        } else {
            return super.explain(d);
        }
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {


        private final PropEqualXY_C p;
        private int idxVar;

        public RemProc(PropEqualXY_C p) {
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
}
