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
import solver.Solver;
import solver.explanations.ExplanationFactory;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.EngineStrategies;
import solver.propagation.engines.comparators.Shuffle;
import solver.propagation.engines.comparators.predicate.Predicates;
import solver.propagation.engines.group.Group;
import solver.search.loop.monitors.SearchMonitorFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractProblem {

    enum Level {
        SILENT(-10), QUIET(0), VERBOSE(10), SOLUTIONS(20), SEARCH(30);

        int level;

        Level(int level) {
            this.level = level;
        }


        public int getLevel() {
            return level;
        }
    }

    @Option(name = "-log", usage = "Quiet resolution", required = false)
    Level level = Level.VERBOSE;

    @Option(name = "-policy", usage = "Propagation policy", required = false)
    EngineStrategies policy = EngineStrategies.DEFAULT;

    @Option(name = "-seed", usage = "Seed for Shuffle propagation engine.", required = false)
    protected long seed = 29091981;

    @Option(name = "-exp", usage = "Explanation engine.", required = false)
    protected ExplanationFactory expeng = ExplanationFactory.NONE;

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
            System.exit(-1);
        }
    }

    protected void overridePolicy() {
        IPropagationEngine engine = solver.getEngine();
        switch (policy) {
            case DEFAULT:
                break;
            case SHUFFLE:
                engine.deleteGroups();
                solver.getEngine().addGroup(Group.buildGroup(Predicates.all(), new Shuffle(seed), Policy.FIXPOINT));
                break;
            default:
                engine.deleteGroups();
                policy.defineIn(solver);
                break;
        }
    }

    protected void overrideExplanation() {
        expeng.make(solver);
    }

    public final void execute(String... args) {
        this.readArgs(args);
        Logger log = LoggerFactory.getLogger("bench");
        this.printDescription();
        this.buildModel();
        this.configureSolver();

        overrideExplanation();

        overridePolicy();

        if (level.getLevel() > Level.QUIET.getLevel()) {
            SearchMonitorFactory.log(solver,
                    level.getLevel() > Level.VERBOSE.getLevel(),
                    level.getLevel() > Level.SOLUTIONS.getLevel());
        }

        this.solve();
        if (level.getLevel() > Level.QUIET.getLevel()) {
            this.prettyOut();
        }
        if (level.getLevel() > Level.SILENT.getLevel()) {
            log.info("[STATISTICS {}]", solver.getMeasures().toOneLineString());
        }
    }

}
