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
import choco.kernel.memory.IEnvironment;
import gnu.trove.TIntHashSet;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropMemberEnum extends Propagator<IntVar> {

    final TIntHashSet values;


    public PropMemberEnum(IntVar var, TIntHashSet values, IEnvironment environment,
                          Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint,
                          PropagatorPriority priority, boolean reactOnPromotion) {
        super(new IntVar[]{var}, environment, intVarPropagatorConstraint, priority, reactOnPromotion);
        this.values = values;
    }

    @Override
    public void propagate() throws ContradictionException {
        int left = Integer.MIN_VALUE;
        int right = left;
        boolean rall = true;
        int ub = this.vars[0].getUB();
        for (int val = this.vars[0].getLB(); val <= ub; val = this.vars[0].nextValue(val)) {
            if (!values.contains(val)) {
                if (val == right + 1) {
                    right = val;
                } else {
                    rall &= vars[0].removeInterval(left, right, this);
                    left = right = val;
                }
            }
        }
        rall &= vars[0].removeInterval(left, right, this);
        if (rall) {
            this.setPassive();
        }
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> intVarIFineRequest, int varIdx, int mask) throws ContradictionException {
        propagate();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.ALL_MASK();
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public ESat isEntailed() {
        int nb = 0;
        int ub = this.vars[0].getUB();
        for (int val = this.vars[0].getLB(); val <= ub; val = this.vars[0].nextValue(val)) {
            if (values.contains(val)) {
                nb++;
            }
        }
        if (nb == 0) return ESat.FALSE;
        else if (nb == vars[0].getDomainSize()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }
}
