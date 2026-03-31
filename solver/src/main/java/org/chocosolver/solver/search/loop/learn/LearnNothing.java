/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.learn;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class LearnNothing implements Learn {
    @Override
    public void init() {
        // nothing to do by default
    }

    @Override
    public boolean record() {
        return false;
    }

    @Override
    public void forget() {
        // nothing to do by default
    }
}
