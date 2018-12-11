/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;



/**
 * A delta monitor.
 * It is based on a specific delta and stores some specific information about it.
 * It can freeze it.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public interface IDeltaMonitor  {

    /**
     * Freeze the delta for iteration
     */
    void freeze();

    /**
     * Release the delta
     */
    void unfreeze();
}
