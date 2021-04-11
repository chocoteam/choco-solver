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
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.SetView;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetIntersection;

/**
 * Set view over set variables representing the intersection of these variables.
 *
 * @author Dimitri Justeau-Allaire
 * @since 29/03/2021
 */
public class SetIntersectionView extends SetView<SetVar> {

    public SetIntersection lb;
    public SetIntersection ub;

    /**
     * Create a set intersection view.
     *
     * @param name      name of the variable
     * @param variables observed variables
     */
    public SetIntersectionView(String name, SetVar... variables) {
        super(name, variables);
        ISet[] LBs = new ISet[variables.length];
        ISet[] UBs = new ISet[variables.length];
        for (int i = 0; i < variables.length; i++) {
            LBs[i] = variables[i].getLB();
            UBs[i] = variables[i].getUB();
        }
        this.lb = new SetIntersection(variables[0].getModel(), LBs);
        this.ub = new SetIntersection(variables[0].getModel(), UBs);
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
        // If there is only one observed set var containing the element, remove the element from this variable.
        int nb = 0;
        int idx = -1;
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].getUB().contains(element)) {
                nb++;
                idx = i;
                if (nb > 1) {
                    break;
                }
            }
        }
        if (nb == 1) {
            variables[idx].remove(element, this);
            return true;
        }
        return false;
    }

    @Override
    protected boolean doForceSetElement(int element) throws ContradictionException {
        // Force the element in every set
        boolean b = true;
        for (SetVar set : variables) {
            b = b && set.force(element, this);
        }
        return b;
    }
}
