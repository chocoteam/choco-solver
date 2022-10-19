/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

/**
 * Linear restart strategy
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/10/22
 */
public class LinearCutoff extends AbstractCutoff {

    private long calls;

    public LinearCutoff(long scale) {
        super(scale);
        this.calls = 0;
    }

    @Override
    public long getNextCutoff() {
        calls++;
        return scaleFactor * calls;
    }

    @Override
    public void reset() {
        calls = 0;
    }
}
