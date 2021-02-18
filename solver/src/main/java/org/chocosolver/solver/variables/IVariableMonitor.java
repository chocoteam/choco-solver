/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.IEventType;



/**
 * A monitor for Variable, to observe variable modification (for integer variable : value removals, bounds modification
 * or instantiation) and do something right after the modification.
 * <p/>
 * This differs from {@link org.chocosolver.solver.constraints.Propagator} because it is not scheduled in the propagation engine.
 * However, it assumes that <code>this</code> executes fast and low complexity operations.
 * Otherwise, it should be a propagator.
 * <p/>
 * This also differs from {@link org.chocosolver.solver.variables.view.IView} because it is not a specific variable, and can connect
 * two or more variables together. For instance, this can be used for logging issue.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/11/11
 */
public interface IVariableMonitor<V extends Variable> extends ICause {

    /**
     * Operations to execute after updating the domain variable
     *  @param var variable concerned
     * @param evt modification event
	 */
    void onUpdate(V var, IEventType evt) throws ContradictionException;
}
