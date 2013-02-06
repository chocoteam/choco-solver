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

package solver.constraints.propagators.ternary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;

/**
 * X = MAX(Y,Z)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class PropMax extends Propagator<IntVar> {

    IntVar MAX, v1, v2;
    protected final RemProc rem_proc;
    protected final IIntDeltaMonitor[] idms;

    public PropMax(IntVar X, IntVar Y, IntVar Z, Solver solver, Constraint<IntVar,
            Propagator<IntVar>> intVarPropagatorConstraint) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = vars[i].hasEnumeratedDomain() ? vars[i].monitorDelta(this) : IIntDeltaMonitor.Default.NONE;
        }
        this.MAX = X;
        this.v1 = Y;
        this.v2 = Z;
        rem_proc = new RemProc(this);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INT_ALL_MASK();
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter(true, true);
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            filterHoles(true, true);
        }
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    protected void filterHoles(boolean hv1, boolean hv2) throws ContradictionException {
        if (MAX.hasEnumeratedDomain()) {
            for (int valeur = MAX.getLB(); valeur <= MAX.getUB(); valeur = MAX.nextValue(valeur)) {
                if (!v1.contains(valeur) && !v2.contains(valeur)) {
                    MAX.removeValue(valeur, aCause);
                }
            }
        }
        if (hv1) {
            if (v1.hasEnumeratedDomain()) {
                int UB = v2.getUB();
                for (int valeur = v1.getLB(); valeur <= v1.getUB(); valeur = v1.nextValue(valeur)) {
                    if (!MAX.contains(valeur) && valeur > UB) {
                        v1.removeValue(valeur, aCause);
                    }
                }
            }
        }
        if (v2.hasEnumeratedDomain()) {
            int UB = v1.getUB();
            for (int valeur = v2.getLB(); valeur <= v2.getUB(); valeur = v2.nextValue(valeur)) {
                if (!MAX.contains(valeur) && valeur > UB) {
                    v2.removeValue(valeur, aCause);
                }
            }
        }
    }

    protected void filter(boolean low, boolean upp) throws ContradictionException {
        boolean hasChanged;
        // check upper bound
        if (upp) {
            do {
                hasChanged = MAX.updateUpperBound(Math.max(v1.getUB(), v2.getUB()), aCause);
                hasChanged |= v1.updateUpperBound(MAX.getUB(), aCause);
                hasChanged |= v2.updateUpperBound(MAX.getUB(), aCause);
            } while (hasChanged);
        }
        //then check lower bound
        if (low || upp) {
            do {
                hasChanged = MAX.updateLowerBound(Math.max(v1.getLB(), v2.getLB()), aCause);
                if (v1.getUB() < MAX.getLB()) {
                    hasChanged |= v2.updateLowerBound(MAX.getLB(), aCause);
                    if (MAX.hasEnumeratedDomain() && v2.hasEnumeratedDomain()) {
                        filterHoles(false, true);
                    }
                }
                if (v2.getUB() < MAX.getLB()) {
                    hasChanged |= v1.updateLowerBound(MAX.getLB(), aCause);
                    if (MAX.hasEnumeratedDomain() && v1.hasEnumeratedDomain()) {
                        filterHoles(true, false);
                    }
                }
            } while (hasChanged);
        }
    }


    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            filter(true, true);
        } else if (EventType.isInclow(mask)) {
            filter(true, false);
        } else if (EventType.isDecupp(mask)) {
            filter(false, true);
        }
        if (EventType.isRemove(mask)) {
            idms[varIdx].freeze();
            idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
            idms[varIdx].unfreeze();
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0) {
            if (x > v2.getUB()) {
                v1.removeValue(x, aCause);
            }

            if (x > v1.getUB()) {
                v2.removeValue(x, aCause);
            }
        } else {
            if (!v1.contains(x) && !v2.contains(x)) {
                MAX.removeValue(x, aCause);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (MAX.getValue() != Math.max(v1.getValue(), v2.getValue())) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return MAX.toString() + ".MAX(" + v1.toString() + "," + v2.toString() + ")";
    }

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropMax p;
        private int idxVar;

        public RemProc(PropMax p) {
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
