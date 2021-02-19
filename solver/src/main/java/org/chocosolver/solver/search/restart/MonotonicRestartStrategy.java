/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.cutoffseq.ICutoffStrategy;

/**
 * Restart strategy to restart every <tt>gap</tt> restarts
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public class MonotonicRestartStrategy implements ICutoffStrategy{

    private final int gap;

    public MonotonicRestartStrategy(int gap) {
        this.gap = gap;
    }

    @Override
    public long getNextCutoff() {
        return gap;
    }
}
