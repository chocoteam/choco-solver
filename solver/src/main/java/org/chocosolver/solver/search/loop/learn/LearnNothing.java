/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

import org.chocosolver.solver.Solver;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class LearnNothing implements Learn {
    @Override
    public boolean record(Solver solver) {
        return false;
    }

    @Override
    public void forget(Solver solver) {
        // nothing to do by default
    }
}
