/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/01/2020
 */
public interface RealInterval {

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    double getLB();

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    double getUB();


    /**
     * Modifies the bounds for intersecting with the specified interval.
     *
     * @param interval an interval
     * @param cause who launches the intersection
     * @throws ContradictionException if a failure occurs
     */
    default void intersect(RealInterval interval, ICause cause) throws ContradictionException {
        intersect(interval.getLB(), interval.getUB(), cause);
    }

    /**
     * Modifies the bounds for intersecting with the specified interval.
     *
     * @param l lower bound
     * @param u upper bound
     * @param cause who launches the intersection
     * @throws ContradictionException if a failure occurs
     */
    void intersect(double l, double u, ICause cause) throws ContradictionException;
}
