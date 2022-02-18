/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.delta;

import org.chocosolver.memory.IEnvironment;



/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 oct. 2010
 */
public interface IDelta  {

    /**
     * Lazy clear the delta, on world change
     */
    void lazyClear();

    /**
     * Return the associate environment
     *
     * @return associated environment
     */
    IEnvironment getEnvironment();
}
