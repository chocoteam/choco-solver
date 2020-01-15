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
 * A class that provides a pattern to declare a model and solve it. <br/>
 * The methods are considered to be called in the following order:
 * <pre> {@code this.setUp(args); // read program arguments
 * this.buildModel(); // build the model (variables and constraints)
 * this.configureSearch(); // configure the search strategy
 * this.solve(); // launch the resolution process
 * this.tearDown(); // run actions on exit}</pre>
 *
 * By default, {@link #setUp(String...)} is based on <a href="http://args4j.kohsuke.org/">args4j</a>.
 *
 * @author Charles Prud'homme
 * @since 03/01/2017
 * @param <M> the constraint model object built thanks to this interface (designed to be library independent except for this)
 */
public interface IProblem<M> extends IUpDown {

    @Override
    void setUp(String... args) throws SetUpException;

    /**
     * Call the model creation.
     * <ul>
     * <li>add variables</li>
     * <li>post constraints</li>
     * <li>(for optimization problems) define the objective(s) here or later in {@link IProblem#configureSearch()})</li>
     * </ul>
     */
    void buildModel();

    /**
     * Get constraint model object.
     * @return the current model
     */
    M getModel();
   
    /**
     * Call search configuration.
     * For optimization problems, define the objective if it has not been done in {@link IProblem#buildModel()}. 
     */
    void configureSearch();

    /**
     * Call problem resolution. 
     * For optimization problems, the objective(s) must be defined before this call. 
     */
    void solve();
}
