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

import org.chocosolver.solver.search.measure.IMeasures;

/**
 * Overrides all but one services for ICounter and provides easy to implement counter based on {@link IMeasures}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public abstract class ACounter implements ICounter {

    protected IMeasures measures;
    protected long max;

    public ACounter(IMeasures measures, long limit) {
        this.max = limit;
        this.measures = measures;
    }

    @Override
    public void init() {
        // nothing
    }

    @Override
    public void update() {
        // nothing
    }

    @Override
    public boolean isMet() {
        return isMet(max);
    }

    @Override
    public boolean isMet(long value) {
        update();
        return currentValue()>= value;
    }

    @Override
    public final void overrideLimit(long newLimit) {
        max = newLimit;
    }


    @Override
    public long getLimitValue() {
        return max;
    }
}
