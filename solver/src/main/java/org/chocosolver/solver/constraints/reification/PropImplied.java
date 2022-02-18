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
 * r &rArr; c
 * </p>
 * where 'r' is a boolean variable and 'c' a constraint (ie, set of propagators).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/06/2021
 */
public class PropImplied extends Propagator<Variable> {

    /**
     * Implying BoolVar
     */
    private final BoolVar impR;
    /**
     * Implied constraint
     */
    private final Constraint impC;

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

    public PropImplied(BoolVar r, Constraint c) {
        super(extractVars(c, r), PropagatorPriority.LINEAR, false);
        this.impC = c;
        this.impC.ignore();
        this.impR = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (impR.isInstantiated()) {
            if (impR.isInstantiatedTo(1)) {
                this.model.postTemp(impC);
            }
            this.setPassive();
        } else {
            if (ESat.FALSE.equals(impC.isSatisfied())) {
                impR.instantiateTo(0, this);
                this.setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (impR.isInstantiatedTo(1)) {
            return impC.isSatisfied();
        } else if (impR.isInstantiatedTo(0)) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }
}
