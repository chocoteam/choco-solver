/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

/**
 * An interface to define views.
 * A view is a specific variable that does not declare any domain but relies on another variable.
 * It converts getters and setters to ensure that the semantic of the view is respected.
 * <p/>
 * This is intend to replace very specific propagator such as equality.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public interface IView extends ICause, Variable {

    /**
     * Return the basis variable
     *
     * @return variable observed
     */
    Variable getVariable();

    /**
     * This methods is related to explanations, it binds an event occurring on the observed
     * variable to the view.
     * @param mask  type of modification
     * @param one   an int
     * @param two   an int
     * @param three an int
     */
    void justifyEvent(IntEventType mask, int one, int two, int three);

    /**
     * To notify a view that the variable is observed has been modified.
     * @param event the event received by the observed variable
     * @throws ContradictionException if a failure occurs
     */
    void notify(IEventType event) throws ContradictionException;
}
