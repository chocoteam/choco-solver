/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
 * This is an implementation of domain views, as described in:
 * Van Hentenryck P., Michel L. (2014) Domain Views for Constraint Programming
 * https://link.springer.com/chapter/10.1007/978-3-319-10428-7_51
 *
 * --- UPDATE 08/03/2021 (Dimitri Justeau-Allaire) ---
 * Views are now by default defined as global for more genericity.
 * Global views are an extension of views for observing several variables.
 * A global view is a variable that does not declare any domain but relies on an array of variables.
 * Global views are particularly useful to provide a more efficient and more expressive way than channelling
 * constraints to link different types of variables such as a SetVar and an array of IntVar.
 * ---------------------------------------------------
 *
 * @author Charles Prud'homme
 * @since 26/08/11
 */
public interface IView<V extends Variable> extends ICause, Variable {

    /**
     * Return the array of observed variables
     *
     * @return variables observed
     */
    V[] getVariables();

    /**
     * @return The number of observed variables
     */
    default int getNbObservedVariables() {
        return getVariables().length;
    }

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
     * @param variableIdx the index of the variable in the view's observed variables
     * @throws ContradictionException if a failure occurs
     */
    default void notify(IEventType event, int variableIdx) throws ContradictionException{
        notifyPropagators(event, this);
    }

}
