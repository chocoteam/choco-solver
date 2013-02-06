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

import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.Exit;
import parser.flatzinc.ast.GoalConf;
import solver.Solver;
import solver.explanations.ExplanationFactory;
import solver.propagation.hardcoded.ConstraintEngine;
import solver.propagation.hardcoded.SevenQueuesConstraintEngine;
import solver.propagation.hardcoded.VariableEngine;
import solver.search.loop.monitors.AverageCSV;
import solver.search.strategy.pattern.SearchPattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    protected List<String> instances = new ArrayList<String>();

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions.", required = false)
    protected boolean all = false;

    @Option(name = "-i", aliases = {"--ignore-search"}, usage = "Ignore search strategy.", required = false)
    protected boolean free = false;

    @Option(name = "-bbss", usage = "Black box search strategy:\n1(*): activity based\n2: impact based\n3: dom/wdeg", required = false)
    protected int bbss = 1;

    @Option(name = "-dv", usage = "Use same decision variables as declared in file (default false)", required = false)
    protected boolean decision_vars = false;

    @Option(name = "-seed", usage = "Seed for randomness", required = false)
    protected long seed = 29091981L;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search", required = false)
    protected int nb_cores = 1;

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    protected long tl = -1;

    @Option(name = "-e", aliases = {"--engine"}, usage = "Engine Number.\n0: constraint\n1: variable\n2: 7q cstrs\n3: 8q cstrs." +
            "\n4: 8q vars\n5: abs\n6: arcs\n-1: default", required = false)
    protected byte eng = -1;

    @Option(name = "-csv", usage = "CSV file path to trace the results.", required = false)
    protected String csv = "";

    @Option(name = "-sp", usage = "Search pattern.", required = false)
    protected SearchPattern searchp = SearchPattern.NONE;

    @Option(name = "-exp", usage = "Explanation engine.", required = false)
    protected ExplanationFactory expeng = ExplanationFactory.NONE;


    @Option(name = "-l", aliases = {"--loop"}, usage = "Loooooop.", required = false)
    protected long l = 1;

    private boolean userinterruption = true;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, RecognitionException {
        new ParseAndSolve().doMain(args);
    }

    public void doMain(String[] args) throws IOException, RecognitionException {
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

    public void buildParser(InputStream is, Solver mSolver, THashMap<String, Object> map, GoalConf gc) {
        try {
            // Create an input character stream from standard in
            ANTLRInputStream input = new ANTLRInputStream(is);
            // Create an ExprLexer that feeds from that stream
            FlatzincLexer lexer = new FlatzincLexer(input);
            // Create a stream of tokens fed by the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Create a parser that feeds off the token stream
            FlatzincParser parser = new FlatzincParser(tokens);
            // Begin parsing at rule prog, get return value structure
            FlatzincParser.flatzinc_model_return r = parser.flatzinc_model();

            // WALK RESULTING TREE
            CommonTree t = (CommonTree) r.getTree(); // get tree from parser
            // Create a tree node stream from resulting tree
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
            FlatzincWalker walker = new FlatzincWalker(nodes); // create a tree parser
            walker.flatzinc_model(mSolver, map, gc);                 // launch at start rule prog
        } catch (IOException io) {
            Exit.log(io.getMessage());
        } catch (RecognitionException re) {
            Exit.log(re.getMessage());
        }
    }


    protected void parseandsolve() throws IOException {
        for (final String instance : instances) {
            AverageCSV acsv = null;
            if (!csv.equals("")) {
                acsv = new AverageCSV(csv, l);
                final AverageCSV finalAcsv = acsv;
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        if (isUserinterruption()) {
                            finalAcsv.record(instance, ";**ERROR**;");
                        }
                    }
                });
            }
            GoalConf gc = new GoalConf(free, bbss, decision_vars, all, seed, searchp, tl);
            for (int i = 0; i < l; i++) {
                LOGGER.info("% parse instance...");
                Solver solver = new Solver();
                THashMap<String, Object> map = new THashMap<String, Object>();
                buildParser(new FileInputStream(new File(instance)), solver, map, gc);
                makeEngine(solver);
                if (!csv.equals("")) {
                    assert acsv != null;
                    acsv.setSolver(solver);
                }
                expeng.make(solver);

                LOGGER.info("% solve instance...");
                solver.solve();
            }
            if (!csv.equals("")) {
                assert acsv != null;
                acsv.record(instance, gc.getDescription());
            }
        }
        userinterruption = false;
    }

    protected void makeEngine(Solver solver) {
        switch (eng) {
            case 0:
                // let the default propagation strategy,
                break;
            case 1:
                solver.set(new ConstraintEngine(solver));
                break;
            case 2:
                solver.set(new VariableEngine(solver));
                break;
            case 3:
                solver.set(new SevenQueuesConstraintEngine(solver));
                break;
            case -1:
            default:
                if (solver.getNbCstrs() > solver.getNbVars()) {
                    solver.set(new VariableEngine(solver));
                } else {
                    solver.set(new ConstraintEngine(solver));
                }

        }
    }

    private boolean isUserinterruption() {
        return userinterruption;
    }
}
