/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.reification;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.ReificationConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.Deduction;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

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
        super(allVars, computePrority(consIfBoolTrue, consIfBoolFalse), false);
        this.bVar = (BoolVar) vars[0];
        this.trueCons = consIfBoolTrue;
        this.falseCons = consIfBoolFalse;
    }

    public void setReifCons(ReificationConstraint reifCons) {
        assert this.reifCons == null : "cannot change the ReificationConstraint of a PropReif";
        this.reifCons = reifCons;
    }

    private static PropagatorPriority computePrority(Constraint consIfBoolTrue, Constraint consIfBoolFalse) {
        int p = Math.min(consIfBoolTrue.computeMaxPriority().priority, consIfBoolFalse.computeMaxPriority().priority);
        return PropagatorPriority.get(Math.min(p, PropagatorPriority.TERNARY.priority));
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (bVar.isInstantiated()) {
            setPassive();
            if (bVar.getBooleanValue() == ESat.TRUE) {
                reifCons.activate(0);
            } else {
                reifCons.activate(1);
            }
        } else {
            ESat sat = trueCons.isSatisfied();
            if (sat == ESat.TRUE) {
                setPassive();
                bVar.setToTrue(aCause);
                reifCons.activate(0);
            } else if (sat == ESat.FALSE) {
                setPassive();
                bVar.setToFalse(aCause);
                reifCons.activate(1);
            }
//			else {// in case the entailment has not the same implementation
//				sat = falseCons.isSatisfied();
//				if (sat == ESat.FALSE) {
//					bVar.setToTrue(aCause);
//					reifCons.activate(0);
//					setPassive();
//				}else if(sat == ESat.TRUE){
//					bVar.setToFalse(aCause);
//					reifCons.activate(1);
//					setPassive();
//				}
//			}
        }
    }

    @Override
    public ESat isEntailed() {
        if (bVar.isInstantiated()) {
            if (bVar.getValue() == 1) {
                return trueCons.isSatisfied();
            } else {
                return falseCons.isSatisfied();
            }
        } else {
            // a constraint an its opposite can neither be both true nor both false
            ESat tie = trueCons.isSatisfied();
            if (tie != ESat.UNDEFINED) {
                ESat fie = falseCons.isSatisfied();
                if (tie == fie) {
                    return ESat.FALSE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void explain(ExplanationEngine xengine, Deduction d, Explanation e) {
        e.add(xengine.getPropagatorActivation(this));
        if (d.getVar() == bVar) {
            // the current deduction is due to the current domain of the involved variables
            for (Variable v : vars) {
                v.explain(xengine, VariableState.DOM, e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return bVar.toString() + "=>" + trueCons.toString() + ", !" + bVar.toString() + "=>" + falseCons.toString();
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        throw new SolverException("PropReif cannot be duplicated!");
    }
}
