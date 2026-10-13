/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.exception.SolverException;

/**
 * Thrown when a {@link ModelSpec} is ill-formed or cannot be instantiated.
 *
 * @author Charles Prud'homme
 */
public class SpecException extends SolverException {

    public SpecException(String message) {
        super(message);
    }

    public SpecException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
