/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;

/**
 * Set a limit over the number of restarts allowed during the search.
 * When this limit is reached, the search loop is informed and the resolution is stopped.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15 juil. 2010
 */
public final class RestartCounter extends ACounter {

    public RestartCounter(Model model, long restartLimit) {
        this(model.getSolver().getMeasures(), restartLimit);
    }

    public RestartCounter(IMeasures measures, long nodelimit) {
        super(measures, nodelimit);
    }

    @Override
    public long currentValue() {
        return measures.getRestartCount();
    }
}
