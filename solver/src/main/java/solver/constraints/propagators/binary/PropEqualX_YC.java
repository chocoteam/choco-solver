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
import choco.kernel.common.util.procedure.IntProcedure1;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.ValueRemoval;
import solver.explanations.VariableState;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;

/**
 * X = Y + C
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
@PropAnn(tested=PropAnn.Status.CORRECTION)
public final class PropEqualX_YC extends Propagator<IntVar> {

    IntVar x;
    IntVar y;
    int cste;

    protected final RemProc rem_proc;

    @SuppressWarnings({"unchecked"})
    public PropEqualX_YC(IntVar[] vars, int c, Solver solver, IntConstraint constraint) {
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

    private void updateInfX() throws ContradictionException {
        x.updateLowerBound(y.getLB() + cste, this, false);
    }

    private void updateInfY() throws ContradictionException {
        y.updateLowerBound(x.getLB() - cste, this, false);
    }

    private void updateSupX() throws ContradictionException {
        x.updateUpperBound(y.getUB() + cste, this, false);
    }

    private void updateSupY() throws ContradictionException {
        y.updateUpperBound(x.getUB() - cste, this, false);
    }

    @Override
    public void propagate() throws ContradictionException {
        updateInfX();
        updateSupX();
        updateInfY();
        updateSupY();
        // ensure that, in case of enumerated domains,  holes are also propagated
        if (y.hasEnumeratedDomain() && x.hasEnumeratedDomain()) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!(y.contains(val - cste))) {
                    x.removeValue(val, this, false);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!(x.contains(val + cste))) {
                    y.removeValue(val, this, false);
                }
            }
        }
    }


    @Override
    public void propagateOnRequest
            (IRequest<IntVar> request, int varIdx,
             int mask) throws ContradictionException {
        IntDelta delta = request.getVariable().getDelta();
        int f = request.fromDelta();
        int l = request.toDelta();

        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else {
            if (EventType.isInclow(mask)) {
                this.awakeOnLow(varIdx);
            }
            if (EventType.isDecupp(mask)) {
                this.awakeOnUpp(varIdx);
            }
            if (EventType.isRemove(mask)) {
                delta.forEach(rem_proc.set(varIdx), f, l);
            }
        }
    }

    void awakeOnInst(int index) throws ContradictionException {
        if (index == 0) {
            y.instantiateTo(x.getValue() - cste, this, false);
        } else {
            x.instantiateTo(y.getValue() + cste, this, false);
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
        if (index == 0) y.removeValue(val - cste, this, false);
        else {
            x.removeValue(val + cste, this, false);
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB() + cste) ||
                (x.getLB() > y.getUB() + cste))
            return ESat.FALSE;
        else if (x.instantiated() &&
                y.instantiated() &&
                (x.getValue() == y.getValue() + cste))
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    private static class RemProc implements IntProcedure1<Integer> {

        private final PropEqualX_YC p;
        private int idxVar;

        public RemProc(PropEqualX_YC p) {
            this.p = p;
        }

        @Override
        public IntProcedure1 set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.awakeOnRem(idxVar, i);
        }
    }

    @Override
    public Explanation explain(Deduction d) {
   //     return super.explain(d);

        if (d.getVar() == x) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add (y.explain(VariableState.REM,((ValueRemoval) d).getVal() - cste));
            }
            else {
                throw new UnsupportedOperationException("PropEqualX_YC only knows how to explain ValueRemovals");
            }
            return explanation;
        } else if (d.getVar() == y) {
            Explanation explanation = new Explanation(this);
            if (d instanceof ValueRemoval) {
                explanation.add (x.explain(VariableState.REM,((ValueRemoval) d).getVal() + cste));
            }
            else {
                throw new UnsupportedOperationException("PropEqualX_YC only knows how to explain ValueRemovals");
            }
            return explanation;
        }
        else {
            return super.explain(d);
        }

    }
}
