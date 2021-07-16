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
 * A limit over run time.
 * It acts as a monitor, to be up-to-date when the search loop asks for limit reaching.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class TimeCounter extends ACounter {

    /**
     * @param model the model to instrument
     * @param timeLimitInNano in nanosecond
     */
    public TimeCounter(Model model, long timeLimitInNano) {
        this(model.getSolver().getMeasures(), timeLimitInNano);
    }

    /**
     * @param measures the measures recorder to check
     * @param timeLimitInNano in nanosecond
     */
    public TimeCounter(IMeasures measures, long timeLimitInNano) {
        super(measures, timeLimitInNano);
    }

    @Override
    public long currentValue() {
        return measures.getTimeCountInNanoSeconds();
    }
}
