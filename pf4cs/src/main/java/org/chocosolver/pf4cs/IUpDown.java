/*
 * This file is part of pf4cs, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.pf4cs;

/**
 * An interface that allows pre-process (with {@link #setUp(String...)})
 * and pos-process (with {@link #tearDown()}) actions.
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 03/01/2017
 */
public interface IUpDown {

    /**
     * Set up the concrete class with the arugments defined by <i>args</i>.
     * @param args arguments to set up the concrete class.
     * @throws SetUpException if one or more argument is not valid.
     */
    default void setUp(String... args) throws SetUpException{}

    /**
     * Action to run on exit.
     */
    default void tearDown(){}
}
