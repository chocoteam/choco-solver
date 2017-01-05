/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 08/02/13
 * Time: 20:30
 */

package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;

public enum NoPropagationEngine implements IPropagationEngine {

    SINGLETON {
        //***********************************************************************************
        // METHODS
        //***********************************************************************************

        private final ContradictionException e = new ContradictionException();

        @Override
        public void fails(ICause cause, Variable variable, String message) throws ContradictionException {
            /*throw new UnsupportedOperationException("A failure occurred before a propagation engine has been defined." +
                    "This probably means that one variable domain has been wiped out (i.e. the problem has no solution)" +
                  "before starting resolution.");*/
            throw e.set(cause, variable, message);
        }

        @Override
        public ContradictionException getContradictionException() {
            return e;
            /*throw new UnsupportedOperationException("A failure occurred before a propagation engine has been defined." +
                    "This probably means that one variable domain has been wiped out (i.e. the problem has no solution)" +
                  "before starting resolution.");*/
        }
    }
}
