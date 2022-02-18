/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.ISetDelta;
import org.chocosolver.solver.variables.view.SetView;

/**
 * An abstract class for set views over graph variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 01/03/2021
 */
public abstract class SetGraphView<E extends GraphVar> extends SetView<E> {

    protected E graphVar;

    /**
     * Create a set view on a graph variable.
     *
     * @param name  name of the variable
     * @param graphVar observed graph variable
     */
    protected SetGraphView(String name, E graphVar) {
        super(name, graphVar);
        this.graphVar = graphVar;
    }

    /**
     * Action to execute on graph var when this view requires to remove an element from its upper bound
     * @param element element to remove from the set view
     * @return true if the observed graph variable has been modified
     * @throws ContradictionException
     */
    protected abstract boolean doRemoveSetElement(int element) throws ContradictionException;

    /**
     * Action to execute on graph var when this view requires to force an element to its lower bound
     * @param element element to force to the set view
     * @return true if the observed graph variable has been modified
     * @throws ContradictionException
     */
    protected abstract boolean doForceSetElement(int element) throws ContradictionException;

    public E getVariable() {
        return graphVar;
    }

    @Override
    public ISetDelta getDelta() {
        throw new UnsupportedOperationException("SetGraphView does not support getDelta()");
    }
}
