/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.exception;

import org.chocosolver.solver.Model;

/**
 * A specific exception for invalid solution.
 * Helpful for {@link org.chocosolver.solver.ParallelPortfolio} and unreliable models.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/12/2020
 */
public class InvalidSolutionException extends SolverException {

    private final Model model;

    /**
     * Constructs a new solver exception with the specified detailed message.
     *
     * @param message message to print
     */
    public InvalidSolutionException(String message, Model model) {
        super(message);
        this.model = model;
    }

    public final Model getModel() {
        return model;
    }


}
