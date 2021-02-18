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

/**
 * A specific <code>RuntimeException</code> that can be thrown during the normal execution of the
 * problem resolution.
 * <br/>
 * A method is not required to declare in its <code>throws</code>
 * clause a <code>SolverException</code> that might
 * be thrown during the execution of the method but not caught.
 * <p/>
 *
 * @author Arnaud Malapert
 * @author Charles Prud'homme
 * @since 20 juil. 2010
 */
public class SolverException extends RuntimeException {

    /**
     * Constructs a new solver exception with the specified detailed message.
     *
     * @param message message to print
     */
    public SolverException(String message) {
		super(message);
    }
}
