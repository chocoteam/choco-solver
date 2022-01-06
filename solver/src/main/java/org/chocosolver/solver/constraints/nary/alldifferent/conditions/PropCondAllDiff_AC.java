/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffAC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * subject to conditions (e.g. allDifferent_except_0)
 * AllDiff only applies on the subset of variables satisfying the given condition
 *
 * @author Jean-Guillaume Fages
 */
public class PropCondAllDiff_AC extends Propagator<IntVar> {

    private final Condition condition;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * Holds only on the subset of variables satisfying the given condition
     *
     * @param variables array of integer variables
     * @param condition defines the subset of variables which is considered by the
     *                  allDifferent constraint
     */
    public PropCondAllDiff_AC(IntVar[] variables, Condition condition) {
        super(variables, PropagatorPriority.QUADRATIC, false);
        this.condition = condition;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nb = 0;
        for (IntVar v : vars) {
            if (condition.holdOnVar(v)) {
                nb++;
            }
        }
        IntVar[] vs = new IntVar[nb];
        for (IntVar v : vars) {
            if (condition.holdOnVar(v)) {
                nb--;
                vs[nb] = v;
            }
        }
        AlgoAllDiffAC filter = new AlgoAllDiffAC(vs, this);
        filter.propagate();
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************
    @Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].isInstantiated()) {
                nbInst++;
                if (condition.holdOnVar(vars[i])) {
                    for (int j = i + 1; j < vars.length; j++) {
                        if (condition.holdOnVar(vars[j]))
                            if (vars[j].isInstantiated() && vars[i].getValue() == vars[j].getValue()) {
                                return ESat.FALSE;
                            }
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
