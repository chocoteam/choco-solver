/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static java.lang.Runtime.getRuntime;

/**
 * A class that provides a pattern to declare a model and solve it. <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractProblem implements IUpDown {

    /**
     * A seed for random purpose
     */
    @Option(name = "-seed", usage = "Seed for Shuffle propagation engine.", required = false)
    protected long seed = 29091981;

    /**
     * Declared problem
     */
    protected Model model;

    private boolean userInterruption = true;

    /**
     * @return the current model
     */
    public Model getModel() {
	return model;
    }

    /**
     * Call the model creation:
     * <ul>
     * <li>create a Model</li>
     * <li>add variables</li>
     * <li>post constraints</li>
     * </ul>
     */
    public abstract void buildModel();

    /**
     * Call search configuration
     */
    public void configureSearch() {}

    /**
     * Call problem resolution
     */
    public abstract void solve();

    @Override
    public void setUp(String... args) throws SetUpException {
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

    @Override
    public void tearDown() {}

    /**
     * Read program arguments
     * @param args list of arguments
     * @return <tt>true</tt> if arguments were correctly read
     */
    public final boolean readArgs(String... args) {
	CmdLineParser parser = new CmdLineParser(this);
	try {
	    parser.parseArgument(args);
	} catch (CmdLineException e) {
	    System.err.println(e.getMessage());
	    System.err.println("java " + this.getClass() + " [options...]");
	    parser.printUsage(System.err);
	    System.err.println();
	    return false;
	}
	return true;
    }

    private boolean userInterruption() {
	return userInterruption;
    }

    /**
     * Main method: from argument reading to resolution.
     * <ul>
     * <li>read program arguments</li>
     * <li>build the model</li>
     * <li>configure the search</li>
     * <li>launch the resolution</li>
     * </ul>
     * 
     * @param args
     *            list of arguments to pass to the problem
     */
    public final void execute(String... args) {
	if (this.readArgs(args)) {
	    this.buildModel();
	    this.configureSearch();

	    Thread statOnKill = new Thread() {
		public void run() {
		    if (userInterruption()) {
			System.out.println(model.getSolver().getMeasures().toString());
		    }
		}
	    };

	    getRuntime().addShutdownHook(statOnKill);

	    this.solve();
	    userInterruption = false;
	    getRuntime().removeShutdownHook(statOnKill);
	}
    }

}
