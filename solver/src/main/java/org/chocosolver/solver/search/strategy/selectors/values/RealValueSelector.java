/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.RealVar;



/**
 * A value selector specifies which value should be chosen to constrain the selected variable.
 * The value chosen must belong to the domain of the selected variable.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 sept. 2010
 */
public interface RealValueSelector  {

    /**
     * Selects and returns the value to constrained chosen variable with.
     * The chosen value must belong to the domain of <code>variable</code>.
     *
     * @return the value, based on the domain of variable
     */
    double selectValue(RealVar var);

}
