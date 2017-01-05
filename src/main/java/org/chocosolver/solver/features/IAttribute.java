/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.features;

import org.chocosolver.solver.Model;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 04/05/2016.
 */
public interface IAttribute {

    /**
     * Method to evaluate a specific attribute over a model
     *
     * @param model to evaluate
     * @return the value of the attribute
     */
    double evaluate(Model model);

    /**
     * @return a short description of the attribute
     */
    String description();
}
