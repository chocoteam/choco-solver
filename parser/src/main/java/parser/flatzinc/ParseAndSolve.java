/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.flatzinc;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.parser.FZNParser;
import solver.Solver;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.PropagationStrategies;
import solver.propagation.hardcoded.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class ParseAndSolve {

    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    // receives other command line parameters than options
    @Argument
    private List<String> instances = new ArrayList<String>();

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions.", required = false)
    private boolean all = false;

    @Option(name = "-i", aliases = {"--ignore-search"}, usage = "Ignore search strategy.", required = false)
    private boolean free = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search", required = false)
    private int nb_cores = 1;

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    private long tl = -1;

    @Option(name = "-e", aliases = {"--engine"}, usage = "Engine Number.\n0: constraint\n1: variable\n2: 7q cstrs\n3: 8q cstrs." +
            "\n4: 8q vars\n5: abs\n6: arcs\n-1: default", required = false)
    private byte eng = 0;


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        new ParseAndSolve().doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(160);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("ParseAndSolve [options...] fzn_instance...");
            parser.printUsage(System.err);
            System.err.println("\nCheck MiniZinc is correctly installed.");
            System.err.println();
            return;
        }
        parseandsolve();
    }

    private void parseandsolve() {
        for (String instance : instances) {
            final FZNParser parser = new FZNParser(all, free);
            LOGGER.info("% load file ...");
            parser.loadInstance(new File(instance));
            LOGGER.info("% parse instance...");
            parser.parse();
            LOGGER.info("% solve instance...");
            final Solver solver = parser.solver;

            switch (eng) {
                case 0:
                    solver.set(new ConstraintEngine(solver));
                    break;
                case 1:
                    solver.set(new VariableEngine(solver));
                    break;
                case 2:
                    solver.set(new SevenQueuesConstraintEngine(solver));
                    break;
                case 3:
                    solver.set(new EightQueuesConstraintEngine(solver));
                    break;
                case 4:
                    solver.set(new EightQueuesVariableEngine(solver));
                    break;
                case 5:
                    solver.set(new ABConstraintEngine(solver));
                    break;
                case 6:
                    IPropagationEngine pe = new PropagationEngine(solver.getEnvironment());
                    PropagationStrategies.TWO_QUEUES_WITH_ARCS.make(solver, pe);
                    solver.set(pe);
                    break;
                case -1:
                default:
                    if (solver.getNbCstrs() > solver.getNbVars()) {
                        solver.set(new VariableEngine(solver));
                    } else {
                        solver.set(new ConstraintEngine(solver));
                    }

            }
//        SearchMonitorFactory.logWithRank(solver, 4783, 4785);
//        solver.getSearchLoop().getLimitsBox().setNodeLimit(4785);
//        SearchMonitorFactory.log(solver, true, true);
//        SearchMonitorFactory.statEveryXXms(solver, 1000);
            if (tl > -1) {
                solver.getSearchLoop().getLimitsBox().setTimeLimit(tl);
            }
//        final boolean[] stop = {true};
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                if (stop[0]) {
//                    LOGGER.info("% User interruption...");
//                    parser.layout.beforeClose();
//                }
//            }
//        });
            solver.solve();
//        stop[0] = false;
        }
    }

}
