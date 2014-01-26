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

package solver.constraints.reification;

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.constraints.ReificationConstraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;
import util.ESat;

/**
 * Implication propagator
 * <p/>
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public class PropReif extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // boolean variable of the reification
    private final BoolVar bVar;
    // constraint to apply if bVar = true
    private final Constraint trueCons;
    // constraint to apply if bVar = false
    private final Constraint falseCons;
	// constraint in charge of the reification process (constraint of this propagator)
    private ReificationConstraint reifCons;

    //***********************************************************************************
    // CONSTRUCTION
    //***********************************************************************************

    public PropReif(Variable[] allVars, Constraint consIfBoolTrue, Constraint consIfBoolFalse) {
        super(allVars, PropagatorPriority.LINEAR, false);
        this.bVar = (BoolVar) vars[0];
        this.trueCons = consIfBoolTrue;
        this.falseCons = consIfBoolFalse;
    }

	public void setReifCons(ReificationConstraint reifCons){
		assert this.reifCons==null:"cannot change the ReificationConstraint of a PropReif";
		this.reifCons = reifCons;
	}

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (bVar.instantiated()) {
            if (bVar.getBooleanValue() == ESat.TRUE) {
                reifCons.activate(0);
            } else {
                reifCons.activate(1);
            }
            setPassive();
        } else {
            ESat sat = trueCons.isSatisfied();
            if (sat == ESat.FALSE) {
                bVar.setToFalse(aCause);
                reifCons.activate(1);
                setPassive();
            }
            sat = falseCons.isSatisfied();
            if (sat == ESat.FALSE) {
                bVar.setToTrue(aCause);
                reifCons.activate(0);
                setPassive();
            }
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        if (bVar.instantiated()) {
            if (bVar.getValue() == 1) {
                return trueCons.isSatisfied();
            } else {
                return falseCons.isSatisfied();
            }
        }else{
			// a constraint an its opposite can neither be both true nor both false
			ESat tie = trueCons.isSatisfied();
			if(tie!=ESat.UNDEFINED){
				ESat fie = falseCons.isSatisfied();
				if(tie==fie){
					return ESat.FALSE;
				}
			}
		}
        return ESat.UNDEFINED;
    }

    @Override
    public void explain(Deduction d, Explanation e) {
        e.add(solver.getExplainer().getPropagatorActivation(this));
        e.add(this);
        if (d.getVar() == bVar) {
            // the current deduction is due to the current domain of the involved variables
            for (Variable v : vars) {
                v.explain(VariableState.DOM, e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
        // and the application of the current propagator
    }

    @Override
    public String toString() {
        return bVar.toString() + "=>" + trueCons.toString()+", !"+bVar.toString() + "=>" + falseCons.toString();
    }
}
