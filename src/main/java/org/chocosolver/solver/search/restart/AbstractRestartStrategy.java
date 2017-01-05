/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

/**
 * <br/>
 *
 * @author Charles Prud'homme, , Arnaud Malapert
 * @since 13/05/11
 */
public abstract class AbstractRestartStrategy implements IRestartStrategy {

    protected int scaleFactor = 1;

    protected double geometricalFactor = 1d;

    protected AbstractRestartStrategy(int scaleFactor, double geometricalFactor) {
        setScaleFactor(scaleFactor);
        setGeometricalFactor(geometricalFactor);
    }

    @Override
    public int getFirstCutOff() {
        return scaleFactor;
    }

    protected static void checkPositiveValue(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("arguments should be strictly positive.");
        }
    }

    protected void setGeometricalFactor(double geometricalFactor) {
        checkPositiveValue(geometricalFactor);
        this.geometricalFactor = geometricalFactor;
    }

    protected final void setScaleFactor(int scaleFactor) {
        checkPositiveValue(scaleFactor);
        this.scaleFactor = scaleFactor;
    }

    public int[] getSequenceExample(int length) {
        int[] res = new int[length];
        for (int i = 0; i < res.length; i++) {
            res[i] = getNextCutoff(i);
        }
        return res;
    }

}
