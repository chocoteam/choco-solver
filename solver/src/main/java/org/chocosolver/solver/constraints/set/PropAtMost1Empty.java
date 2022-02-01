/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;

/**
 * At most one set can be empty
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMost1Empty extends Propagator<SetVar> {

    private final IStateInt emptySetIndex;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * At most one set in the array sets can be empty
     *
     * @param sets array of set variables
     */
    public PropAtMost1Empty(SetVar[] sets) {
        super(sets, PropagatorPriority.UNARY, true);
        emptySetIndex = model.getEnvironment().makeInt(-1);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < vars.length; i++) {
            propagate(i, 0);
        }
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        if (vars[v].getUB().size() == 0) {
            if (emptySetIndex.get() != -1) {
                fails(); // TODO: could be more precise, for explanation purpose
            } else {
                emptySetIndex.set(v);
                for (int i = 0; i < vars.length; i++) {
                    int s = vars[i].getUB().size();
                    if (i != v && s != vars[i].getLB().size()) {
                        if (s == 0) {
                            fails(); // TODO: could be more precise, for explanation purpose
                        } else if (s == 1) {
                            vars[i].force(vars[i].getUB().iterator().next(), this);
                        }
                    }
                }
            }
        }
        if (vars[v].getUB().size() == 1 && emptySetIndex.get() != -1) {
            vars[v].force(vars[v].getUB().iterator().next(), this);
        }
    }

    @Override
    public ESat isEntailed() {
        boolean none = true;
        boolean allInstantiated = true;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].getUB().size() == 0) {
                if (!none) {
                    return ESat.FALSE;
                }
                none = false;
            } else if (!vars[i].isInstantiated()) {
                allInstantiated = false;
            }
        }
        if (allInstantiated) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
