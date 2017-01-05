/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.features;

import java.io.Serializable;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @author Arnaud Malapert
 * @since 04/05/2016.
 */
public interface IFeatures extends Serializable {

    /**
     * @return name of the underlying model
     */
    String getModelName();

    /**
     * @return number of variables declared in the underlying model
     */
    int getNbVars();

    /**
     * @return number of constraints posted in the underlying model
     */
    int getNbCstrs();

    /**
     * @param attribute attribute to evaluate
     * @return the value of the attribute
     */
    double getValue(Attribute attribute);

}
