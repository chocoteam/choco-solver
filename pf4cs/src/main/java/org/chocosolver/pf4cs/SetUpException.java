/*
 * This file is part of pf4cs, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.pf4cs;

/**
 * Defines an exception to catch invalid arguments.
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 03/01/2017
 */
public class SetUpException extends Exception {

    public SetUpException(String message) {
        super(message);
    }

    public SetUpException(String message, Throwable cause) {
        super(message, cause);
    }
}
