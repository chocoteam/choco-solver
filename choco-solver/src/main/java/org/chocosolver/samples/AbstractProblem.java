/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import static java.lang.Runtime.getRuntime;
import static org.chocosolver.samples.AbstractProblem.Level.QUIET;
import static org.chocosolver.samples.AbstractProblem.Level.SILENT;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractProblem {

    public enum Level {
        SILENT(-10), QUIET(0), VERBOSE(10), SOLUTION(20), SEARCH(30);

        int level;

        Level(int level) {
            this.level = level;
        }


        public int getLevel() {
            return level;
        }
    }

    @Option(name = "-log", usage = "Quiet resolution", required = false)
    protected Level level = Level.SOLUTION;

    @Option(name = "-seed", usage = "Seed for Shuffle propagation engine.", required = false)
    protected long seed = 29091981;

    @Option(name = "-ee", aliases = "--exp-eng", usage = "Type of explanation engine to plug in")
    ExplanationFactory expeng = ExplanationFactory.NONE;

    @Option(name = "-ng", aliases = "--nogoods", usage = "Extract nogoods from explanations (required \"-ee\").", required = false)
    protected boolean ng = false;

    protected Solver solver;

    private boolean userInterruption = true;

    public void printDescription() {
    }

    public Solver getSolver() {
        return solver;
    }

    public abstract void createSolver();

    public abstract void buildModel();

    public abstract void configureSearch();

    public abstract void solve();

    public abstract void prettyOut();

    public final boolean readArgs(String... args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
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

    protected void overrideExplanation() {
        if (solver.getExplainer() == null) {
            expeng.plugin(solver, ng, false);
        }
    }

    private boolean userInterruption() {
        return userInterruption;
    }

    public final void execute(String... args) {
        if (this.readArgs(args)) {
            this.printDescription();
            this.createSolver();
            this.buildModel();
            this.configureSearch();

            overrideExplanation();

            if (level.getLevel() > SILENT.getLevel()) {
                Chatterbox.showStatistics(solver);
                if (level.getLevel() > Level.VERBOSE.getLevel()) Chatterbox.showSolutions(solver);
                if (level.getLevel() > Level.SOLUTION.getLevel()) Chatterbox.showDecisions(solver);
            }

            Thread statOnKill = new Thread() {
                public void run() {
                    if (userInterruption()) {
                        if (level.getLevel() > SILENT.getLevel()) {
                            System.out.println(solver.getMeasures().toString());
                        }
                    }

                }
            };

            getRuntime().addShutdownHook(statOnKill);

            this.solve();
            if (level.getLevel() > QUIET.getLevel()) {
                prettyOut();
            }
            userInterruption = false;
            getRuntime().removeShutdownHook(statOnKill);
        }
    }

}
