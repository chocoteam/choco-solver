/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package samples;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Constant;
import solver.Solver;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.EngineStrategyFactory;
import solver.propagation.engines.comparators.IncrPriorityP;
import solver.propagation.engines.comparators.Queue;
import solver.propagation.engines.comparators.Shuffle;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractProblem {

    @Option(name = "-quiet", usage = "Quiet resolution", required = false)
    boolean quiet = false;

    @Option(name = "-policy", usage = "Propagation policy", required = false)
    String policy = "";


    protected Solver solver;

    public void printDescription() {
    }

    public Solver getSolver() {
        return solver;
    }

    public abstract void buildModel();

    public abstract void configureSolver();

    public abstract void solve();

    public abstract void prettyOut();

    public final void readArgs(String... args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java " + this.getClass() + " [options...]");
            parser.printUsage(System.err);
            System.err.println();
        }
    }

    protected void overridePolicy() {
        IPropagationEngine engine = solver.getEngine();
        if (policy.equals("shuffle")) {
            engine.deleteGroups();
            engine.setDefaultComparator(new Shuffle());
            engine.setDefaultPolicy(Policy.FIXPOINT);
        } else if (policy.equals("oldest")) {
            engine.deleteGroups();
            engine.setDefaultComparator(Queue.get());
            engine.setDefaultPolicy(Policy.FIXPOINT);
        } else if (policy.equals("priorityC")) {
            engine.deleteGroups();
            engine.setDefaultComparator(IncrPriorityP.get());
            engine.setDefaultPolicy(Policy.FIXPOINT);
        } else if (policy.equals("var-oriented")) {
            engine.deleteGroups();
            EngineStrategyFactory.variableOriented(solver);
        } else if (policy.equals("cstr-oriented")) {
            engine.deleteGroups();
            EngineStrategyFactory.constraintOriented(solver);
        }/*else if(policy.equals("specific")){
        }*/
    }

    public final void execute(String... args) {
        this.readArgs(args);
        Logger log = LoggerFactory.getLogger("bench");
        if (!quiet) {
            log.info(Constant.WELCOME_TITLE);
            log.info(Constant.WELCOME_VERSION);
            log.info("* Sample library: executing {}.java ... \n", getClass().getName());
        }

        this.printDescription();
        this.buildModel();
        this.configureSolver();

        if (policy.length() > 0) {
            overridePolicy();
        }

        this.solve();
        if (!quiet) {
            this.prettyOut();
        }

        log.info("[STATISTICS {}]", solver.getMeasures().toOneLineString());
    }

}
