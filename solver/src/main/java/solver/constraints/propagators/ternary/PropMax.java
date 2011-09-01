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

package solver.constraints.propagators.ternary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure1;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IntDelta;

/**
 * X = MAX(Y,Z)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class PropMax extends Propagator<IntVar> {

    IntVar v0, v1, v2;
    protected final RemProc rem_proc;

    public PropMax(IntVar X, IntVar Y, IntVar Z, Solver solver, Constraint<IntVar,
            Propagator<IntVar>> intVarPropagatorConstraint) {
        super(new IntVar[]{X, Y, Z}, solver, intVarPropagatorConstraint, PropagatorPriority.TERNARY, true);
        this.v0 = X;
        this.v1 = Y;
        this.v2 = Z;
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.ALL_MASK();
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        filter(0);
        filter(1);
        filter(2);
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else if (EventType.isInclow(mask)) {
            this.awakeOnLow(varIdx);
        } else if (EventType.isDecupp(mask)) {
            this.awakeOnUpp(varIdx);
        } else if (EventType.isRemove(mask)) {
            IntVar var = request.getVariable();
            IntDelta delta = var.getDelta();
            int f = request.fromDelta();
            int l = request.toDelta();
            delta.forEach(rem_proc.set(varIdx), f, l);
        }
    }

    public void filter(int idx) throws ContradictionException {
        if (idx == 0) {
            v0.updateUpperBound(Math.max(v1.getUB(), v2.getUB()), this);
            v0.updateLowerBound(Math.max(v1.getLB(), v2.getLB()), this);

            if (v0.hasEnumeratedDomain()) {
                for (int valeur = v0.getLB(); valeur <= v0.getUB(); valeur = v0.nextValue(valeur)) {
                    if (!v1.contains(valeur) && !v2.contains(valeur)) {
                        v0.removeValue(valeur, this);
                    }
                }
            }
        } else if (idx == 1) {
            v1.updateUpperBound(v0.getUB(), this);
            if (v1.getLB() > v2.getUB()) {
                v0.updateLowerBound(v1.getLB(), this);
                v1.updateLowerBound(v0.getLB(), this);
            }

            if (v1.hasEnumeratedDomain()) {
                for (int valeur = v1.getLB(); valeur <= v1.getUB(); valeur = v1.nextValue(valeur)) {
                    if (!v0.contains(valeur) && valeur > v2.getUB()) {
                        v1.removeValue(valeur, this);
                    }
                }
            }

        } else if (idx == 2) {
            v2.updateUpperBound(v0.getUB(), this);
            if (v2.getLB() > v1.getUB()) {
                v0.updateLowerBound(v2.getLB(), this);
                v2.updateLowerBound(v0.getLB(), this);
            }
            if (v2.hasEnumeratedDomain()) {
                for (int valeur = v2.getLB(); valeur <= v2.getUB(); valeur = v2.nextValue(valeur)) {
                    if (!v0.contains(valeur) && valeur > v1.getUB()) {
                        v2.removeValue(valeur, this);
                    }
                }
            }
        }
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        int val;
        if (idx == 0) {
            val = v0.getValue();
            v1.updateUpperBound(val, this);
            v2.updateUpperBound(val, this);
            if (!v1.contains(val)){
                v2.instantiateTo(val, this);
            }
            if (!v2.contains(val)){
                v1.instantiateTo(val, this);
            }
        } else if (idx == 1) {
            val = v1.getValue();
            if (val > v2.getUB()){
                v0.instantiateTo(val, this);
                setPassive();
            } else{
                v0.updateUpperBound(Math.max(val, v2.getUB()), this);
            }
        } else if (idx == 2) {
            val = v2.getValue();
            if (val > v1.getUB()){
                v0.instantiateTo(val, this);
                setPassive();
            } else{
                v0.updateUpperBound(Math.max(val, v1.getUB()), this);
            }
        }
    }

    public void awakeOnUpp(int idx) throws ContradictionException {
        if (idx == 0) {
            v1.updateUpperBound(v0.getUB(), this);
            v2.updateUpperBound(v0.getUB(), this);
        } else {
            v0.updateUpperBound(Math.max(v1.getUB(), v2.getUB()), this);
        }
    }

    public void awakeOnLow(int idx) throws ContradictionException {
        if (idx == 0) {
            if (v1.getLB() > v2.getUB()) {
                v1.updateLowerBound(v0.getLB(), this);
            }
            if (v2.getLB() > v1.getUB()) {
                v2.updateLowerBound(v0.getLB(), this);
            }
        } else {
            v0.updateLowerBound(Math.max(v1.getLB(), v2.getLB()), this);
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0) {
            if (x > v2.getUB()) {
                v1.removeValue(x, this);
            }

            if (x > v1.getUB()) {
                v2.removeValue(x, this);
            }
        } else {
            if (!v1.contains(x) && !v2.contains(x)) {
                v0.removeValue(x, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (v0.getValue() != Math.max(v1.getValue(), v2.getValue())) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    private static class RemProc implements IntProcedure1<Integer> {

        private final PropMax p;
        private int idxVar;

        public RemProc(PropMax p) {
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
}
