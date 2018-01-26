/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
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
//			else {// in case the entailment has not the same implementation
//				sat = falseCons.isSatisfied();
//				if (sat == ESat.FALSE) {
//					bVar.setToTrue(this);
//					reifCons.activate(0);
//					setPassive();
//				}else if(sat == ESat.TRUE){
//					bVar.setToFalse(this);
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
