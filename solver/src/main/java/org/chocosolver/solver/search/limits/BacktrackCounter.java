/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;

/**
 * Set a limit over the number of backtracks allowed during the search.
 * When this limit is reached, the search loop is informed and the resolution is stopped.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15 juil. 2010
 */
public final class BacktrackCounter extends ACounter {

    public BacktrackCounter(Model model, long backtracklimit) {
        this(model.getSolver().getMeasures(), backtracklimit);
    }

    public BacktrackCounter(IMeasures measures, long backtracklimit) {
        super(measures, backtracklimit);
    }

    @Override
    public long currentValue() {
        return measures.getBackTrackCount();
    }
}
