/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.cutoffseq.AbstractCutoffStrategy;

/**
 * <br/>
 *
 * @author Charles Prud'homme, , Arnaud Malapert
 * @since 13/05/11
 */
public abstract class AbstractRestartStrategy extends AbstractCutoffStrategy {

    protected AbstractRestartStrategy(int scaleFactor, double geometricalFactor) {
        super(scaleFactor);
    }

    @Deprecated
    public int[] getSequenceExample(int length) {
        int[] res = new int[length];
        for (int i = 0; i < res.length; i++) {
            res[i] = getNextCutoff();
        }
        return res;
    }

}
