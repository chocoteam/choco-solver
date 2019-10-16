/*
 * This file is part of cutoffseq, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.cutoffseq;

/**
 * A geometrical cutoff strategy.
 * It is based on two parameters: g for <i>geometricalFactor</i> and s for <i>scaleFactor</i>.
 * At step <i>n</i>, the next cutoff is computed with the following function : <i>s*g^n</i>
 *
 * Example with <i>s</i>=1 and <i>g</i>=1.3:
 * 1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40, ...
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public final class GeometricalCutoffStrategy extends AbstractCutoffStrategy {

    /**
     * Declared geometrical factor
     */
    private final double geometricalFactor;
    /**
     * Current geometrical factor, after n calls to {@link #getNextCutoff()}.
     */
    private double geometricalFactorPower;

    /**
     * A geometrical cutoff strategy.
     * At step <i>n</i>, the next cutoff is computed with the following function : <i>s*g^n</i>
     * @param s scale factor
     * @param g geometrical factor
     * @exception IllegalArgumentException if <i>g</i> is not strictly greater than 1

     */
    @SuppressWarnings("WeakerAccess")
    public GeometricalCutoffStrategy(long s, double g) throws IllegalArgumentException{
        super(s);
        if (g <= 1) {
            throw new IllegalArgumentException("The geometrical factor of the restart strategy must be strictly greater than 1.");
        }
        this.geometricalFactor = g;
        this.geometricalFactorPower = 1;
    }

    /**
     * @return at call <i>n</i>, the next cutoff is computed with the following function :
     * <i>s*g^n</i>
     */
    @Override
    public long getNextCutoff() {
        final long cutoff = (long) Math.ceil(scaleFactor * geometricalFactorPower);
        geometricalFactorPower *= geometricalFactor;
        return cutoff;
    }



    @Override
    public String toString() {
        return "GEOMETRICAL(s=" + scaleFactor + ", g=" + geometricalFactor + ')';
    }
}
