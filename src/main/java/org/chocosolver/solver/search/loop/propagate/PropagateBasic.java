/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.propagate;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * A basic {@link Propagate} implementation
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 02/09/15
 */
public class PropagateBasic implements Propagate {

    @Override
    public void execute(Solver solver) throws ContradictionException {
        solver.getDecisionPath().apply();
        solver.getObjectiveManager().postDynamicCut();
        solver.getEngine().propagate();
    }
}
