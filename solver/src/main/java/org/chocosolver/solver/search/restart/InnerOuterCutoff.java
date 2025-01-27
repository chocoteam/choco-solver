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

/**
 * An inner/outer cutoff strategy.
 * <p>
 *     At step <i>n</i>, the next cutoff is computed with the following function : <i>s*g^n</i>
 *     If the inner cutoff is greater than the outer cutoff, the outer cutoff is multiplied by the outer factor.
 *     Otherwise, the inner cutoff is multiplied by the geometrical factor.
 * @author Charles Prud'homme
 * @since 11/10/2024
 */
public final class InnerOuterCutoff extends GeometricalCutoff {

    private final double outerFactor;
    private double outerFactorPower;


    /**
     * A geometrical cutoff strategy.
     * At step <i>n</i>, the next cutoff is computed with the following function : <i>s*g^n</i>
     *
     * @param s  scale factor
     * @param gi geometrical factor
     * @param go outer factor
     * @throws IllegalArgumentException if <i>g</i> is not strictly greater than 1
     */
    @SuppressWarnings("WeakerAccess")
    public InnerOuterCutoff(long s, double gi, double go) throws IllegalArgumentException {
        super(s, gi);
        this.outerFactor = go;
        this.outerFactorPower = 1;
    }

    /**
     * @return at call <i>n</i>, the next cutoff is computed with the following function :
     * <i>s*g^n</i>
     */
    @Override
    public long getNextCutoff() {
        final long inner = (long) Math.floor(scaleFactor * geometricalFactorPower) * grower.getAsInt();
        final long outer = (long) Math.floor(scaleFactor * outerFactorPower) * grower.getAsInt();
        if (inner >= outer) {
            outerFactorPower *= outerFactor;
            geometricalFactorPower = 1.;
        } else {
            geometricalFactorPower *= geometricalFactor;
        }
        return inner;
    }


    @Override
    public void reset() {
        super.reset();
        this.outerFactorPower = 1;
    }

    @Override
    public String toString() {
        return "INNEROUTER(s=" + scaleFactor + ", g=" + geometricalFactor + ')';
    }
}
