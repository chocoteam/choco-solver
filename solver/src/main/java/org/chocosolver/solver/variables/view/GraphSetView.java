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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.SetDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.scheduler.SetEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * An abstract class for set views over graph variables.
 * @author Dimitri Justeau-Allaire
 * @since 01/03/2021
 */
public abstract class GraphSetView<E extends GraphVar> extends AbstractVariable implements IView, SetVar {

    protected E graphVar;
    protected SetDelta delta;
    protected boolean reactOnModification;

    /**
     * Create the shared data of any type of variable.
     *
     * @param name  name of the variable
     * @param model model which declares this variable
     */
    protected GraphSetView(String name, Model model) {
        super(name, model);
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

    @Override
    public boolean force(int element, ICause cause) throws ContradictionException {
        return false;
    }

    @Override
    public boolean remove(int element, ICause cause) throws ContradictionException {
        return false;
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new SetDelta(model.getEnvironment());
        }
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.SET;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new SetEvtScheduler();
    }


    @Override
    public E getVariable() {
        return graphVar;
    }

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        throw new UnsupportedOperationException("GraphSetView does not support explanation.");
    }

    @Override
    public void explain(int p, ExplanationForSignedClause clause) {
        throw new UnsupportedOperationException("GraphSetView does not support explanation.");
    }

    @Override
    public void notify(IEventType event) throws ContradictionException {

    }
}
