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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;

/**
 * This class extends {@link PropagationEngine} in order to collect
 * data relative to propagation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/10/2021
 */
public class PropagationEngineObserver extends PropagationEngine {

    /**
     * The observer to notify
     */
    private final PropagationObserver observer;

    /**
     * A propagation engine profiler.
     *
     * @param model    the declaring model
     * @param observer the propagation engine observer to notify
     */
    public PropagationEngineObserver(Model model, PropagationObserver observer) {
        super(model);
        this.observer = observer;
    }

    /**
     * @apiNote This is the main reason this class exists, to notify a propagation observer
     */
    @Override
    public void execute(Propagator<?> propagator) throws ContradictionException {
        if (propagator.isStateLess() || propagator.isActive()) {
            observer.onCoarseEvent(propagator);
        }
        super.execute(propagator);
    }

    /**
     * @apiNote This is the main reason this class exists, to notify a propagation observer
     */
    @Override
    protected void propagateEvents() throws ContradictionException {
        try {
            //lastProp
            if (lastProp.reactToFineEvent()) {
                observer.onFineEvent(lastProp);
                lastProp.doFinePropagation();
                // now we can check whether a delayed propagation has been scheduled
                int dp = getDelayedPropagation();
                if (dp > 0) {
                    observer.onCoarseEvent(lastProp);
                    lastProp.propagate(dp);
                }
            } else if (lastProp.isActive()) { // need to be checked due to views
                observer.onCoarseEvent(lastProp);
                lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            }
        } catch (ContradictionException cex) {
            observer.onFailure(cex.c, lastProp);
            throw cex;
        }
    }

    /**
     * @apiNote This is the main reason this class exists, to notify a propagation observer
     */
    @Override
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        if (cause instanceof Propagator<?>) {
            observer.onFiltering(cause, lastProp);
        }
        observer.onVariableModification(variable, type, cause);
        super.onVariableUpdate(variable, type, cause);
    }
}
