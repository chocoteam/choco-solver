/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.constraints.IConstraintFactory;
import org.chocosolver.solver.constraints.IReificationFactory;
import org.chocosolver.solver.constraints.ISatFactory;
import org.chocosolver.solver.constraints.IDecompositionFactory;
import org.chocosolver.solver.variables.IResultVariableFactory;
import org.chocosolver.solver.variables.IVariableFactory;
import org.chocosolver.solver.variables.IViewFactory;

/**
 * Interface to ease modeling
 * Enables to make variables, views and constraints
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface IModel extends IVariableFactory, IViewFactory, IConstraintFactory, ISatFactory,
        IReificationFactory, IDecompositionFactory, IResultVariableFactory {

}
