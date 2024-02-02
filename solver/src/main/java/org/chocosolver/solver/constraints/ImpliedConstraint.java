/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.reification.PropImplied;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Equivalence constraint: boolean b <=> constraint c
 * Also known as reification
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 017/05/2022
 */
public class ImpliedConstraint extends Constraint {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // boolean variable of the reification
    // required visibility to allow exportation
    protected final BoolVar bool;
    // constraint to apply if bool = true
    @SuppressWarnings("WeakerAccess") // required visibility to allow exportation
    protected final Constraint trueCons;
    // indices of propagators
    private final int[] indices;
    // reification propagator;
    private final PropImplied propReif;

    //***********************************************************************************
    // CONSTRUCTION
    //***********************************************************************************

    protected ImpliedConstraint(BoolVar bVar, Constraint consIfBoolTrue) {
        super(ConstraintsName.REIFICATIONCONSTRAINT, createProps(bVar, consIfBoolTrue));
        this.propReif = (PropImplied) propagators[0];
        propReif.setReifCons(this);
        trueCons = consIfBoolTrue;
        bool = bVar;
        indices = new int[3];
        indices[0] = 1;
        indices[1] = indices[0] + trueCons.getPropagators().length;
        for (int p = indices[0]; p < indices[1]; p++) {
            propagators[p].setReifiedSilent(bool);
        }
        trueCons.declareAs(Status.REIFIED, -1);
    }

    private static Propagator<?>[] createProps(BoolVar bVar, Constraint trueCons) {
        Set<Variable> setOfVars = new HashSet<>();
        prepareConstraint(bVar, trueCons, setOfVars);
        Variable[] allVars = ArrayUtils.append(new Variable[]{bVar}, setOfVars.toArray(
                new Variable[0]));
        PropImplied reifProp = new PropImplied(allVars, trueCons);
        return ArrayUtils.append(new Propagator[]{reifProp},
                trueCons.getPropagators().clone()
        );
    }

    private static void prepareConstraint(BoolVar bVar, Constraint c, Set<Variable> setOfVars) {
        for (Propagator<?> p : c.getPropagators()) {
            for (Variable v : p.getVars()) {
                if (v != bVar) {
                    setOfVars.add(v);
                }
            }
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void activate(int idx) throws ContradictionException {
        Model model = propagators[0].getModel();
        assert bool.isInstantiatedTo(1 - idx);
        for (int p = indices[idx]; p < indices[idx + 1]; p++) {
            assert (propagators[p].isReifiedAndSilent());
            propagators[p].setReifiedTrue();
            propagators[p].propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            model.getSolver().getEngine().onPropagatorExecution(propagators[p]);
        }
    }

    @Override
    public ESat isSatisfied() {
        return propReif.isEntailed();
    }

    @Override
    public String toString() {
        return bool.toString() + "=>" + trueCons.toString();
    }

}
