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
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * X >= Y
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public final class PropGreaterOrEqualX_Y extends Propagator<IntVar> {

    final IntVar x;
    final IntVar y;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualX_Y(IntVar[] vars, Solver solver, IntConstraint constraint) {
        super(vars, solver, constraint, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if(vIdx == 0){
            return EventType.INSTANTIATE.mask + EventType.DECUPP.mask;
        }else{
            return EventType.INSTANTIATE.mask + EventType.INCLOW.mask;
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(y.getLB(), this);
        y.updateUpperBound(x.getUB(), this);
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }


    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            this.awakeOnInst(varIdx);
        } else {
            if (EventType.isInclow(mask)) {
                x.updateLowerBound(y.getLB(), this);
            }
            if (EventType.isDecupp(mask)) {
                y.updateUpperBound(x.getUB(), this);
            }
        }
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }

    void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0) {
            y.updateUpperBound(x.getUB(), this);
        } else {
            x.updateLowerBound(y.getLB(), this);
        }

    }

    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB())
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB())
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }


    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        bf.append("prop(").append(vars[0].getName()).append(".GEQ.").append(vars[1].getName()).append(")");
        return bf.toString();
    }

    @Override
    public Explanation explain(Deduction d) {
        Explanation expl = new Explanation(this);
        // the current deduction is due to the current domain of the involved variables
        Variable var = d.getVar();
        if (var.equals(x)) {
            // a deduction has been made on x ; this is related to y only
            expl.add(y.explain(VariableState.LB));
        }
        else if (var.equals(y)) {
            expl.add(x.explain(VariableState.UB));
        } else {
            return super.explain(d);
        }
        return expl;
    }
}
