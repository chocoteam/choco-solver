/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

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
