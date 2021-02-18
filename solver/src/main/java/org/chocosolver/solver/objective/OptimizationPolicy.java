/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 26/10/12
 * Time: 14:03
 */

package org.chocosolver.solver.objective;

/**
 * Class which defines a policy to adopt for the optimization process
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public enum OptimizationPolicy {
    /**
     * Set the objective variable to its lowest value
     */
    BOTTOM_UP,
    /**
     * Set the objective variable to its highest value
     */
    TOP_DOWN,
    /**
     * Split the domain of the objective variable
     */
    DICHOTOMIC
}
