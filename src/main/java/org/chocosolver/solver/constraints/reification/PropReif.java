/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.ReificationConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

/**
 * Implication propagator
 * <p>
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
                bVar.setToTrue(this);
                reifCons.activate(0);
            } else if (sat == ESat.FALSE) {
                setPassive();
                bVar.setToFalse(this);
                reifCons.activate(1);
            }
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
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        if (var.equals(bVar)) {
            for (int i = 1; i < vars.length; i++) { // vars[0] is bVar
                newrules |= ruleStore.addFullDomainRule((IntVar) vars[i]);
            }
        } else {
            newrules |= super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }

    @Override
    public String toString() {
        return bVar.toString() + "=>" + trueCons.toString() + ", !" + bVar.toString() + "=>" + falseCons.toString();
    }

}
