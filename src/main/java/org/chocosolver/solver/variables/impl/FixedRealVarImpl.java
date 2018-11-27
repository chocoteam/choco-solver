/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.util.iterators.EvtScheduler;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 13/05/2016.
 */
public class FixedRealVarImpl extends AbstractVariable implements RealVar {

    /**
     * The constant this variable relies on.
     */
    double value;

    /**
     * Create the shared data of any type of variable.
     *
     * @param name  name of the variable
     * @param value a double value
     * @param model model which declares this variable
     */
    public FixedRealVarImpl(String name, double value, Model model) {
        super(name, model);
        this.value = value;
    }

    @Override
    public double getLB() {
        return value;
    }

    @Override
    public double getUB() {
        return value;
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        if (value > this.value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (value < this.value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        if (lowerbound > value || upperbound < value) {
            assert cause != null;
            this.contradiction(cause, "outside domain update bound");
        }
        return false;
    }

    @Override
    public double getPrecision() {
        return Double.MIN_VALUE;
    }

    @Override
    public boolean isInstantiated() {
        return true;
    }

    @Override
    public IDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {

    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {

    }

    @Override
    public int getTypeAndKind() {
        return Variable.REAL | Variable.CSTE;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new RealEvtScheduler();
    }
}
