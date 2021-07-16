/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.solver.Model;

/**
 * An interface for all parsers
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public interface IParser {

    /**
     * Set up the concrete class with the arguments defined by <i>args</i>.
     * @param args arguments to set up the concrete class.
     * @throws SetUpException if one or more argument is not valid.
     * @return true if argument parsing goes right
     */
    boolean setUp(String... args) throws SetUpException;

    /**
     * Action to run on exit.
     */
    default void tearDown(){}

    /**
     * Call the model creation.
     * <ul>
     * <li>add variables</li>
     * <li>post constraints</li>
     * <li>(for optimization problems) define the objective(s) here or later in {@link #configureSearch()})</li>
     * </ul>
     */
    void buildModel();
    
    /**
     * Call search configuration.
     * For optimization problems, define the objective if it has not been done in {@link #buildModel()}.
     */
    void configureSearch();

    /**
     * Call problem resolution.
     * For optimization problems, the objective(s) must be defined before this call.
     */
    void solve();

    /**
     * @return a thread to execute on unexpected exit
     */
    Thread actionOnKill();

    /**
     * Get the solver
     *
     * @return solver
     */
    Model getModel();
}
