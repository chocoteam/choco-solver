/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;


import org.chocosolver.cutoffseq.ICutoffStrategy;

/**
 * @deprecated will be removed in the next release
 * @see ICutoffStrategy
 */
@Deprecated
public interface IRestartStrategy extends ICutoffStrategy{

    @Deprecated
    int getNextCutoff(int nbRestarts);
}
