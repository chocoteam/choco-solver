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
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropGreaterOrEqualX_YC extends Propagator<IntVar> {

    IntVar x;
    IntVar y;
    int cste;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualX_YC(IntVar[] vars, int c, Solver solver, IntConstraint constraint) {
        super(vars.clone(), solver, constraint, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return EventType.INSTANTIATE.mask + EventType.DECUPP.mask;
        } else {
            return EventType.INSTANTIATE.mask + EventType.INCLOW.mask;
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(y.getLB() + this.cste, this);
        y.updateUpperBound(x.getUB() - this.cste, this);
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            if (idxVarInProp == 0) {
                y.updateUpperBound(x.getUB() - this.cste, this);
            } else {
                x.updateLowerBound(y.getLB() + this.cste, this);
            }
        } else {
            if (EventType.isInclow(mask)) {
                x.updateLowerBound(y.getLB() + this.cste, this);
            }
            if (EventType.isDecupp(mask)) {
                y.updateUpperBound(x.getUB() - this.cste, this);
            }
        }
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB() + cste)
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB() + this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(x.getName()).append(" >= ").append(y.getName()).append(" + ").append(cste);
        return st.toString();
    }
}
