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
package solver.constraints.propagators.unary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropLessOrEqualXC extends Propagator<IntVar> {

    private final int constant;

    public PropLessOrEqualXC(IntVar var, int cste, Solver solver,
                             Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(new IntVar[]{var}, solver, intVarPropagatorConstraint, PropagatorPriority.UNARY, false);
        this.constant = cste;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        if(vars[0].updateUpperBound(constant, this, false)|| vars[0].getUB()<=constant){
            this.setPassive();
        }
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        if(vars[0].updateUpperBound(constant, this, false)|| vars[0].getUB()<=constant){
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.eval(vars[0].getUB() <= constant);
    }

    @Override
    public String toString() {
        return vars[0].getName() + " <= " + constant;
    }

     @Override
    public Explanation explain(Deduction d) {
        return new Explanation(this);
    }
}
