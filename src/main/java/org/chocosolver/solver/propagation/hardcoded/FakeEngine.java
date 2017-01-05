/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation.hardcoded;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;

/**
 * An fake propagation engine for debugging uses only.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/02/13
 */
class FakeEngine implements IPropagationEngine {

    private static FakeEngine singleton = new FakeEngine();

    @Override
    public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
        throw new SolverException("The Propagator " + cause + " is not idempotent!\n" +
                "See stack trace for more details -- it can be due to a view!");
    }

    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        throw new SolverException("The Propagator " + cause + " is not idempotent!");
    }

    public static void checkIdempotency(Propagator lastProp) throws ContradictionException {
        switch (lastProp.getModel().getSettings().getIdempotencyStrategy()) {
            case force:
                if (lastProp.isActive()) {
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                break;
            case error:
                IPropagationEngine engine = lastProp.getModel().getSolver().getEngine();
                lastProp.getModel().getSolver().setEngine(singleton);
                if (lastProp.isActive()) {
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                lastProp.getModel().getSolver().setEngine(engine);
                break;
            default:
            case disabled:break;
        }
    }
}
