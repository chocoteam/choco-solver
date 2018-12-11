/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

/**
 * Interface to make constraints over BoolVar, IntVar, RealVar and SetVar
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 */
public interface IConstraintFactory extends IIntConstraintFactory, IRealConstraintFactory, ISetConstraintFactory {

}
