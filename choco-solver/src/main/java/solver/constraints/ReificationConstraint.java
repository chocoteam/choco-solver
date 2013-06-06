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

package solver.constraints;

import solver.constraints.reification.PropReif;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Implication constraint: boolean b => constraint c
 * Also known as half reification
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public class ReificationConstraint extends Constraint<Variable, Propagator<Variable>> {

    // boolean variable of the reification
    private final BoolVar bool;
    // constraint to apply if bool = true
    private final Constraint trueCons;
    // constraint to apply if bool = false
    private final Constraint falseCons;
    // indices of propagators
    private int[] indices;

    protected ReificationConstraint(BoolVar bVar, Constraint consIfBoolTrue, Constraint consIfBoolFalse) {
        super(ArrayUtils.append(new Variable[]{bVar}, consIfBoolTrue.getVariables(), consIfBoolFalse.getVariables()),
                bVar.getSolver());
        trueCons = consIfBoolTrue;
        falseCons = consIfBoolFalse;
        bool = bVar;
        indices = new int[3];
        PropReif reifProp = new PropReif(bVar, this, trueCons, falseCons);
        setPropagators(
                ArrayUtils.append(new Propagator[]{reifProp},
                        trueCons.getPropagators().clone(),
                        falseCons.getPropagators().clone()));
        indices[0] = 1;
        indices[1] = indices[0] + trueCons.getPropagators().length;
        indices[2] = indices[1] + falseCons.getPropagators().length;
        for (int i = 1; i < propagators.length; i++) {
            propagators[i].setReifiedSilent();
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void activate(int idx) throws ContradictionException {
        assert bool.instantiatedTo(1 - idx);
        for (int p = indices[idx]; p < indices[idx + 1]; p++) {
            assert (propagators[p].isReifiedAndSilent());
            propagators[p].setReifiedTrue();
            solver.getExplainer().activePropagator(bool, propagators[p]);
            propagators[p].propagate(EventType.FULL_PROPAGATION.strengthened_mask);
            solver.getEngine().onPropagatorExecution(propagators[p]);
        }
    }

    @Override
    public ESat isSatisfied() {
        if (bool.instantiated()) {
            if (bool.getValue() == 1) {
                return trueCons.isSatisfied();
            } else {
                return falseCons.isSatisfied();
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public ESat isEntailed() {
        if (bool.instantiated()) {
            if (bool.getValue() == 1) {
                return trueCons.isEntailed();
            } else {
                return falseCons.isEntailed();
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return bool.toString() + "=>" + trueCons.toString() + ", !" + bool.toString() + "=>" + falseCons.toString();
    }
}
