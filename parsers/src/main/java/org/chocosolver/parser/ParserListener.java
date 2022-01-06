/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

/**
 * A parser listener to ease user interaction.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public interface ParserListener {

    /**
     * Actions to do before starting the parsing the parameters
     */
    void beforeParsingParameters();

    /**
     * Actions to do after ending the parsing  the parameters
     */
    void afterParsingParameters();

    /**
     * Actions to do before creating the solver
     */
    void beforeSolverCreation();

    /**
     * Actions to do after the solver is created
     */
    void afterSolverCreation();

    /**
     * Actions to do before starting the parsing the input file
     */
    void beforeParsingFile();

    /**
     * Actions to do after ending the parsing  the input file
     */
    void afterParsingFile();

    /**
     * Actions to do before configuring the search
     */
    void beforeConfiguringSearch();

    /**
     * Actions to do after ending the search configuration
     */
    void afterConfiguringSearch();

    /**
     * Actions to do before starting the resolution
     */
    void beforeSolving();

    /**
     * Actions to do after ending the resolution
     */
    void afterSolving();
}
