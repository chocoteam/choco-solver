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
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.requests.IRequest;

/**
 * A specific <code>Propagator</code> extension defining filtering algorithm for:
 * <br/>
 * <b>X =/= Y + C</b>
 * <br>where <i>X</i> and <i>Y</i> are <code>Variable</code> objects and <i>C</i> a constant.
 * <br>
 * This <code>Propagator</code> defines the <code>propagate</code> and <code>awakeOnInst</code> methods. The other ones
 * throw <code>UnsupportedOperationException</code>.
 * <br/>
 * <br/>
 * <i>Based on Choco-2.1.1</i>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Arnaud Malapert
 * @version 0.01, june 2010
 * @since 0.01
 */
public class PropNotEqualX_YC extends Propagator<IntVar> {

    IntVar x;
    IntVar y;
    int cste;

    @SuppressWarnings({"unchecked"})
    public PropNotEqualX_YC(IntVar[] vars, int c, IEnvironment environment, Constraint constraint) {
        super(vars.clone(), environment, constraint, PropagatorPriority.BINARY, false);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions() {
        if(vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain()){
            return EventType.INSTANTIATE.mask;
        }
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        if (x.instantiated()) {
            removeValV1();
        } else if (y.instantiated()) {
            removeValV0();
        }else if (x.getUB() < (y.getLB() + cste) || (y.getUB() + cste) < x.getLB()) {
            setPassive();
        }
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int varIdx, int mask) throws ContradictionException {
        if (EventType.isInstantiate(mask)) {
            if (varIdx == 0) {
                removeValV1();
            } else {
                removeValV0();
            }
        } else if (EventType.isBound(mask)) {
            // typical case: A=[1,4], B=[1,4] (bounded domains)
            // A instantiated to 3 => nothing can be done on B
            // then B dec supp to 3 => 3 can also be removed du to A = 3.
            propagate();
        }
    }

    private void removeValV0() throws ContradictionException {
        if (x.removeValue(y.getValue() + this.cste, this)) {
            this.setPassive();
        } else if (!x.contains(y.getValue() + cste)) {
            this.setPassive();
        }
    }

    private void removeValV1() throws ContradictionException {
        if (y.removeValue(x.getValue() - this.cste, this)) {
            this.setPassive();
        } else if (!y.contains(x.getValue() - cste)) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() < y.getLB() + this.cste) ||
                (y.getUB() < x.getLB() - this.cste))
            return ESat.TRUE;
        else if (x.instantiated()
                && y.instantiated()
                && x.getValue() == y.getValue() + this.cste)
            return ESat.FALSE;
        else
            return ESat.UNDEFINED;
    }
}