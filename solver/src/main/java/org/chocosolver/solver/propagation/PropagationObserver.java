/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

/**
 * This interface makes possible to observer the propagation and be notified of events.
 * <br/>
 *
 * @author Charles Prud'homme
 * @see Solver#profilePropagation()
 * @see PropagationProfiler
 * @since 13/10/2021
 */
public interface PropagationObserver {

    /**
     * Anytime a propagator is being called on a coarse event, this method is called first.
     *
     * @param propagator the propagator to awake.
     */
    void onCoarseEvent(Propagator<?> propagator);

    /**
     * Anytime a propagator is being called on a fine event, this method is called first.
     *
     * @param propagator the propagator to awake.
     */
    void onFineEvent(Propagator<?> propagator);

    /**
     * Anytime a propagator throws a failure, this method is called first.
     * The failing cause may differ from the current propagator in some cases (views, decisions, etc).
     *
     * @param cause the cause of the failure.
     * @param propagator the last propagator being called in the propagation engine (may differ from cause).
     */
    void onFailure(ICause cause, Propagator<?> propagator);

    /**
     * Anytime a propagator filters some values from its variables' domain, this method is called first.
     * The filtering cause may differ from the current propagator in some cases (views, decisions, etc).
     *
     * @param cause the cause of the filtering.
     * @param propagator the last propagator being called in the propagation engine (may differ from cause).
     */
    void onFiltering(ICause cause, Propagator<?> propagator);

    /**
     * Anytime a variable is being modified by a cause, this method is called after.
     *
     * @param variable the modified variable
     * @param type the type of modification
     * @param cause the cause of the filtering
     */
    void onVariableModification(Variable variable, IEventType type, ICause cause);
}
