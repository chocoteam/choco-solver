/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import java.util.function.IntSupplier;

/**
 * Abstract class to generate cutoff sequence
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractCutoff implements ICutoff {

    /**
     * The scale factor, should be strictly positive
     */
    protected final long scaleFactor;

    protected IntSupplier grower =  () -> 1;

    /**
     * Create an abstract class with the specific <i>scaleFactor</i>.
     * @param s scale factor (should be strictly positive)
     * @exception IllegalArgumentException if <i>scaleFactor</i> is not strictly positive
     */
    public AbstractCutoff(long s) throws IllegalArgumentException{
        super();
        if (s < 1) {
            throw new IllegalArgumentException("The scale factor of a restart strategy must be strictly positive.");
        }
        this.scaleFactor = s;
    }

    @Override
    public void setGrower(IntSupplier grower) {
        this.grower = grower;
    }
}
