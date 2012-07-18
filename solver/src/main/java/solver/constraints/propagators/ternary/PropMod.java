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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * X = Y  mod Z
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class PropMod extends Propagator<IntVar> {

    public PropMod(IntVar X, IntVar Y, IntVar Z, Solver solver, Constraint<IntVar,
            Propagator<IntVar>> intVarPropagatorConstraint) {
        super(new IntVar[]{X, Y, Z}, solver, intVarPropagatorConstraint, PropagatorPriority.TERNARY, true);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public int getPropagationConditions() {
        return EventType.CUSTOM_PROPAGATION.mask + EventType.FULL_PROPAGATION.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }


    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
        filter();
    }

    private void filter() throws ContradictionException {
        int c = 0;
        c += (vars[0].instantiated() ? 1 : 0); //  X
        c += (vars[1].instantiated() ? 2 : 0); // Y
        c += (vars[2].instantiated() ? 4 : 0); // Z
        switch (c) {
            case 7: // everything is instantiated
            {
                int z = vars[2].getValue();
                if (z == 0) {
                    if (vars[0].getValue() != vars[1].getValue()) {
                        contradiction(null, "");
                    }
                } else if (vars[0].getValue() != vars[1].getValue() % z) {
                    contradiction(null, "");
                }
                setPassive();
            }
            break;
            case 6: // Z and Y are instantiated
            {
                int z = vars[2].getValue();
                if (z == 0) {
                    vars[0].instantiateTo(vars[1].getValue(), this);
                } else {
                    vars[0].instantiateTo(vars[1].getValue() % vars[2].getValue(), this);
                }
                setPassive();
            }
            break;
            case 5: //  X and Z are instantiated
            {

            }
            break;
            case 4: // Z is instantiated
            {

            }
            break;
            case 3://  X and Y are instantiated
            {

            }
            break;
            case 2: // Y is instantiated
            {

            }
            break;
            case 1: // X is instantiated
            {

            }

            break;
            case 0: // otherwise
                break;
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (vars[0].getValue() != Math.min(vars[1].getValue(), vars[2].getValue())) {
                return ESat.FALSE;
            } else {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].toString() + "=" + vars[1].toString() + "mod" + vars[2].toString();
    }
}
