/*
 * This file is part of cutoffseq, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.cutoffseq;

/**
 * Abstract class to generate cutoff sequence
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractCutoffStrategy implements ICutoffStrategy {

    /**
     * The scale factor, should be strictly positive
     */
    protected final long scaleFactor;

    /**
     * Create a abstract class with the specific <i>scaleFactor</i>.
     * @param s scale factor (should be strictly positive)
     * @exception IllegalArgumentException if <i>scaleFactor</i> is not strictly positive
     */
    public AbstractCutoffStrategy(long s) throws IllegalArgumentException{
        super();
        if (s < 1) {
            throw new IllegalArgumentException("The scale factor of a restart strategy must be strictly positive.");
        }
        this.scaleFactor = s;
    }
}
