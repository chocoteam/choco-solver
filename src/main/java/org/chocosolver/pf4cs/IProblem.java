/**
 * This file is part of pf4cs, https://github.com/chocoteam/pf4cs
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.pf4cs;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

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
 */
public interface IProblem extends IUpDown {

    @Override
    default void setUp(String... args) throws SetUpException{
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java " + this.getClass() + " [options...]");
            parser.printUsage(System.err);
            System.err.println();
            throw new SetUpException("Invalid problem options");
        }
    }

    /**
     * Call the model creation
     */
	void buildModel();

    /**
     * Call search configuration
     */
	void configureSearch();

	/**
     * Call problem resolution
     */
	void solve();
}
