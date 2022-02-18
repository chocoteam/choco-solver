/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.Identity;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.*;

/**
 * This propagator ensures the following relationship:
 * <p>
 * c &rArr; r
 * </p>
 * or
 * <p>
 * &not;c &or; r
 * </p>
 * <p>
 * where 'c' a constraint (ie, set of propagators) and 'r' is a boolean variable
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/06/2021
 */
public class PropImplies extends Propagator<Variable> {

    /**
     * Implied BoolVar
     */
    private final BoolVar impR;
    /**
     * Implying constraint
     */
    private final Constraint impC;
    /**
     * Lazily created opposite constraint
     */
    private Constraint oppC = null;

    private static Variable[] extractVars(Constraint c, BoolVar r) {
        Set<Variable> setOfVars = new HashSet<>();
        setOfVars.add(r);
        for (Propagator<?> p : c.getPropagators()) {
            Collections.addAll(setOfVars, p.getVars());
        }
        Variable[] allVars = setOfVars.toArray(new Variable[0]);
        Arrays.sort(allVars, Comparator.comparingInt(Identity::getId));
        return allVars;
    }

    public PropImplies(Constraint c, BoolVar r) {
        super(extractVars(c, r), PropagatorPriority.LINEAR, false);
        this.impC = c;
        this.impR = r;
        this.oppC = impC.getOpposite();
        this.impC.ignore();
        this.oppC.ignore();
        this.impC.setEnabled(false);
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (impR.isInstantiated()) {
            if (impR.isInstantiatedTo(0)) {
                //todo: is this a bottleneck?
                this.model.postTemp(this.oppC);
            }
            this.setPassive();
        } else {
            ESat sat = impC.isSatisfied();
            switch (sat) {
                case TRUE:
                    impR.setToTrue(this);
                    this.setPassive();
                    break;
                case FALSE:
                    this.setPassive();
                    break;
            }
        }
    }

    @Override
    public ESat isEntailed() {
        switch (impC.isSatisfied()) {
            case TRUE:
                return ESat.eval(this.impR.isInstantiatedTo(1));
            case FALSE:
                return ESat.TRUE;
            default:
            case UNDEFINED:
                return ESat.UNDEFINED;
        }
    }
}
