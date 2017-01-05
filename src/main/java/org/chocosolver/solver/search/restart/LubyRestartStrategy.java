/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.util.tools.MathUtils;

/**
 * Restart strategy based on:
 * <br/>
 * "Optimal Speedup of Las Vegas Algorithms",
 * M. Luby, A. Sinclair, D. Zuckerman,
 * IPL: Information Processing Letters, 1993, 47, 173-180.
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert, Hadrien Cambazard
 * @since 13/05/11
 */
public class LubyRestartStrategy extends AbstractRestartStrategy {

    private int geometricalIntFactor;

    private int divFactor;

    public LubyRestartStrategy(int scaleFactor, int geometricalFactor) {
        super(scaleFactor, geometricalFactor);
    }

    @Override
    public final void setGeometricalFactor(double geometricalFactor) {
        checkPositiveValue(geometricalFactor);
        double f = Math.floor(geometricalFactor);
        if (f != geometricalFactor) {
            throw new IllegalArgumentException("Luby geometrical parameter should be an integer");
        }
        super.setGeometricalFactor(geometricalFactor);
        geometricalIntFactor = (int) geometricalFactor;
        divFactor = geometricalIntFactor - 1;
    }

    private static int geometricalSum(int value, int exponent) {
        return (MathUtils.pow(value, exponent) - 1) / (value - 1);
    }


    /**
     * Returns the Las Vegas coefficient corresponding to the i^h calls.
     * @param i number of calls
     * @return the LV coefficient
     */
    private int getLasVegasCoef(int i) {
        //<hca> I round it to PRECISION because of issues between versions of the jvm on mac and pc
        final double log = MathUtils.roundedLog(i * divFactor + 1, geometricalIntFactor);
        final int k = (int) Math.floor(log);
        if (log == k) {
            return MathUtils.pow(geometricalIntFactor, k - 1);
        } else {
            //recursion
            return getLasVegasCoef(i - geometricalSum(geometricalIntFactor, k));
        }
    }

    @Override
    public int getNextCutoff(int nbRestarts) {
        return getLasVegasCoef(nbRestarts + 1) * scaleFactor;
    }


    @Override
    public String toString() {
        return "LUBY(" + scaleFactor + ',' + geometricalFactor + ')';
    }
}
