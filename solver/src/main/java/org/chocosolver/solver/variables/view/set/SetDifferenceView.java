/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.SetView;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDifference;

/**
 * Set view representing the set difference of two set variables: z = x \ y.
 *
 * @author Dimitri Justeau-Allaire
 * @since 29/03/2021
 */
public class SetDifferenceView extends SetView<SetVar> {

    protected SetDifference lb;
    protected SetDifference ub;

    protected SetVar x, y;

    /**
     * Create a set difference view z = x \ y
     *
     * @param name      name of the variable
     * @param x A set variable.
     * @param y A set variable.
     */
    public SetDifferenceView(String name, SetVar x, SetVar y) {
        super(name, x, y);
        this.x = x;
        this.y = y;
        this.lb = new SetDifference(getModel(), x.getLB(), y.getLB());
        this.ub = new SetDifference(getModel(), x.getUB(), y.getUB());
    }

    @Override
    public ISet getLB() {
        return lb;
    }

    @Override
    public ISet getUB() {
        return ub;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        return false;
    }

    @Override
    public boolean isInstantiated() {
        return getLB().size() == getUB().size();
    }

    @Override
    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        notifyPropagators(event, this);
    }

    @Override
    protected boolean doRemoveSetElement(int element) throws ContradictionException {
        // Remove element from x iff:
        //      element in x.getUB() && element not in y.getUB()
        if (x.getUB().contains(element) && !y.getUB().contains(element)) {
            return x.remove(element, this);
        }
        // Force element to y iff:
        //      element in x.getLB() && element in y.getUB()
        if (x.getLB().contains(element) && y.getUB().contains(element)) {
            return y.force(element, this);
        }
        return false;
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        // Force element to x and remove it from y
        boolean b = x.force(element, this) || y.remove(element, this);
        return b;
    }
}
