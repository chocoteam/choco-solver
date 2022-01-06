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

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default opposite of a constraint
 *
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 20/09/2017.
 */
public class Opposite extends Constraint {
    /**
     * Make a new opposite constraint defined as a set of given propagators
     */
    public Opposite(Constraint cons) {
        super(ConstraintsName.OPPOSITE, createPropagator(cons));
    }

    private static Propagator createPropagator(Constraint cons) {
        Variable[] vars;
        if (cons.getPropagators().length == 1) {
            vars = cons.getPropagator(0).getVars();
        } else {
            Set<Variable> allvars = new HashSet<>();
            for (Propagator p : cons.getPropagators()) {
                Collections.addAll(allvars, p.getVars());
            }
            vars = allvars.toArray(new Variable[0]);
        }
        return new PropOpposite(cons, vars);
    }
}
