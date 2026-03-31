/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffAC;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffACFast;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBimodal;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.IAlldifferentAlgorithm;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * <p/>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation
 * but has a good average behavior in practice
 * <p/>
 * Runs incrementally for maintaining a matching
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
@Explained(partial = true, comment = "AC_ZHANG not explained")
public class PropAllDiffAC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IAlldifferentAlgorithm filter;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables array of integer variables
     * @param mode      name of the filtering algorithm
     */
    public PropAllDiffAC(IntVar[] variables, AllDifferent.Consistency mode) {
        super(variables, PropagatorPriority.QUADRATIC, false);
        switch (mode) {
            case AC_REGIN:
                this.filter = new AlgoAllDiffAC(variables, this);
                break;
            case AC_ZHANG:
                this.filter = new AlgoAllDiffACFast(variables, this);
                break;
            default:
                this.filter = new AlgoAllDiffBimodal(variables, this, mode);
                break;
        }
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter.propagate();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator (used with PropAllDiffInst)
    }

}
