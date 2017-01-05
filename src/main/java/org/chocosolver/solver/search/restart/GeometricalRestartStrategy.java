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
 * Restart strategy to restart every :
 * (geoFactor^restart) * scale
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert
 * @since 13/05/11
 */
public class GeometricalRestartStrategy extends AbstractRestartStrategy {

    public GeometricalRestartStrategy(int scaleFactor, double geometricalFactor) {
        super(scaleFactor, geometricalFactor);
    }

    @Override
    public int getNextCutoff(int nbRestarts) {
        return (int) Math.ceil(Math.pow(geometricalFactor, nbRestarts) * scaleFactor);
    }

    @Override
    public String toString() {
        return "GEOMETRICAL(" + scaleFactor + ',' + geometricalFactor + ')';
    }
}
