/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

/**
 * Interface defining service for cutoff strategy: a sequence of cutoff
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public interface ICutoff {

    /**
     * @return the next restart cutoff
     * */
    long getNextCutoff();

    /**
     * Reset the sequence
     */
    void reset();
}
