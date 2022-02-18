/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * Based on: </br>
 * "A Fast and Simple Algorithm for Bounds Consistency of the AllDifferent Constraint"</br>
 * A. Lopez-Ortiz, CG. Quimper, J. Tromp, P.van Beek
 * <br/>
 *
 * @author Hadrien Cambazard, Charles Prud'homme, Jean-Guillaume fages
 * @since 07/02/11
 * <p>
 */
public class PropAllDiffBC extends Propagator<IntVar> {

    private final AlgoAllDiffBC filter;

    public PropAllDiffBC(IntVar[] variables) {
        super(variables, PropagatorPriority.LINEAR, false);
        filter = new AlgoAllDiffBC(this);
        filter.reset(vars);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter.filter();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator (use PropAllDiffInst)
    }

}
