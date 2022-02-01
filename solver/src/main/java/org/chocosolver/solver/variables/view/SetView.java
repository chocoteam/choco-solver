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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.set.PropCardinality;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.impl.scheduler.SetEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 * An abstract class for set views over other variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 01/03/2021
 */
public abstract class SetView<V extends Variable> extends AbstractView<V> implements SetVar {

    protected IntVar cardinality = null;

    /**
     * Create a set view.
     *
     * @param name  name of the variable
     * @param variables observed variables
     */
    protected SetView(String name, V... variables) {
        super(name, variables);
    }

    /**
     * Action to execute on observed variables when this view requires to remove an element from its upper bound
     * @param element element to remove from the set view
     * @return true if at least one of the observed variables has been modified
     * @throws ContradictionException
     */
    protected abstract boolean doRemoveSetElement(int element) throws ContradictionException;

    /**
     * Action to execute on observed variables when this view requires to force an element to its lower bound
     * @param element element to force to the set view
     * @return true if at least one of the observed variables has been modified
     * @throws ContradictionException
     */
    protected abstract boolean doForceSetElement(int element) throws ContradictionException;

    @Override
    public boolean force(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (!getLB().contains(element) && doForceSetElement(element)) {
            SetEventType e = SetEventType.ADD_TO_KER;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element, ICause cause) throws ContradictionException {
        assert cause != null;
        if (getUB().contains(element) && doRemoveSetElement(element)) {
            SetEventType e = SetEventType.REMOVE_FROM_ENVELOPE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public void createDelta() {
        for (Variable v : getVariables()) {
            v.createDelta();
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
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        throw new UnsupportedOperationException("SetView does not support explanation.");
    }

    @Override
    public void explain(int p, ExplanationForSignedClause clause) {
        throw new UnsupportedOperationException("SetView does not support explanation.");
    }

    @Override
    public IntVar getCard() {
        if(!hasCard()){
            int ubc =  getUB().size();
            int lbc = getLB().size();
            if(ubc==lbc) cardinality = model.intVar(ubc);
            else{
                cardinality = model.intVar(name+".card", lbc, ubc);
                new Constraint(ConstraintsName.SETCARD, new PropCardinality(this, cardinality)).post();
            }
        }
        return cardinality;
    }

    @Override
    public boolean hasCard() {
        return cardinality != null;
    }

    @Override
    public void setCard(IntVar card) {
        if(!hasCard()){
            cardinality=card;
            new Constraint(ConstraintsName.SETCARD, new PropCardinality(this, card)).post();
        } else {
            model.arithm(cardinality, "=", card).post();
        }
    }
}
