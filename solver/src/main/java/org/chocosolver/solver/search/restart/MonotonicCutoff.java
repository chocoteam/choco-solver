/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import java.util.function.IntSupplier;

/**
 * Restart strategy to restart every <tt>gap</tt> restarts
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public class MonotonicCutoff implements ICutoff {

    private final long gap;

    public MonotonicCutoff(long gap) {
        this.gap = gap;
    }

    @Override
    public long getNextCutoff() {
        return gap;
    }

    @Override
    public void reset() {
        // nothing
    }

    @Override
    public void setGrower(IntSupplier grower) {
        // nothing
    }
}
