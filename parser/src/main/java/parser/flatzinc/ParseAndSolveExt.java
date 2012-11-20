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
import solver.Solver;
import solver.explanations.ExplanationFactory;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.Arc;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.hardcoded.ConstraintEngine;
import solver.propagation.hardcoded.SevenQueuesConstraintEngine;
import solver.propagation.hardcoded.VariableEngine;
import solver.search.loop.monitors.AverageCSV;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class ParseAndSolveExt {

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

    @Option(name = "-e", aliases = {"--engine"}, usage = "Engine Number.\n1: constraint\n2: variable\n3: 7q cstrs" +
            "\n4: dsl constraint\n5: dsl variable\n6: dsl 7q cstrs\n0: default", required = false)
    private byte eng = -1;

    @Option(name = "-csv", usage = "CSV file path to trace the results.", required = false)
    private String csv = "";

    @Option(name = "-exp", usage = "Explanation engine.", required = false)
    protected ExplanationFactory expeng = ExplanationFactory.NONE;

    @Option(name = "-l", aliases = {"--loop"}, usage = "Loooooop.", required = false)
    private long l = 1;


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, RecognitionException {
        new ParseAndSolveExt().doMain(args);
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

    public void buildParser(InputStream is, Solver mSolver, THashMap<String, Object> map) {
        try {
            // Create an input character stream from standard in
            ANTLRInputStream input = new ANTLRInputStream(is);
            // Create an ExprLexer that feeds from that stream
            FlatzincFullExtLexer lexer = new FlatzincFullExtLexer(input);
            // Create a stream of tokens fed by the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Create a parser that feeds off the token stream
            FlatzincFullExtParser parser = new FlatzincFullExtParser(tokens);
            // Begin parsing at rule prog, get return value structure
            FlatzincFullExtParser.flatzinc_ext_model_return r = parser.flatzinc_ext_model();

            // WALK RESULTING TREE
            CommonTree t = (CommonTree) r.getTree(); // get tree from parser
            // Create a tree node stream from resulting tree
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
            FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes); // create a tree parser
            walker.flatzinc_model(mSolver, map);                 // launch at start rule prog
        } catch (IOException io) {
            Exit.log(io.getMessage());
        } catch (RecognitionException re) {
            Exit.log(re.getMessage());
        }
    }


    private void parseandsolve() throws IOException, RecognitionException {
        for (String instance : instances) {
            AverageCSV acsv = null;
            if (!csv.equals("")) {
                acsv = new AverageCSV(instance, csv, l);
            }
            for (int i = 0; i < l; i++) {
                LOGGER.info("% parse instance...");
                Solver solver = new Solver();
                THashMap<String, Object> map = new THashMap<String, Object>();
                buildParser(new FileInputStream(new File(instance)), solver, map);
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
                    case 4:
                    case 5:
                    case 6:
                        makeEngine(eng, solver);
                        break;
                    case -1:
                    default:
                        if (solver.getNbCstrs() > solver.getNbVars()) {
                            solver.set(new VariableEngine(solver));
                        } else {
                            solver.set(new ConstraintEngine(solver));
                        }

                }
                if (!csv.equals("")) {
                    assert acsv != null;
                    acsv.setSolver(solver);
                }
                expeng.make(solver);
                if (tl > -1) {
                    solver.getSearchLoop().getLimitsBox().setTimeLimit(tl);
                }
//            solver.getSearchLoop().getLimitsBox().setNodeLimit(2);
                LOGGER.info("% solve instance...");
                //SearchMonitorFactory.log(solver, true, true);
                solver.solve();
            }
            if (!csv.equals("")) {
                assert acsv != null;
                acsv.record();
            }
        }
    }

    private void makeEngine(byte eng, Solver solver) throws IOException, RecognitionException {
        PropagationEngine pe = new PropagationEngine(solver);
        ArrayList<Arc> pairs = Arc.populate(solver);
        THashMap<String, ArrayList> groups = new THashMap<String, ArrayList>(1);
        groups.put("All", pairs);
        THashMap<String, Object> map = new THashMap<String, Object>();

        String st;

        switch (eng) {
            case 4:
                st = "All as queue(wone) of {each prop as list(for)};";
                break;
            case 5:
                st = "All as queue(wone) of {each var as list(for)};";
                break;
            case 6:
                st = "All as list(wone) of {each prop.prioDyn as queue(one) of {each prop as list(for)}};";
                break;
            default:
                st = "";
        }

        // Create an input character stream from standard in
        InputStream in = new ByteArrayInputStream(st.getBytes());
        // Create an input character stream from standard in
        ANTLRInputStream input = new ANTLRInputStream(in);
        // Create an ExprLexer that feeds from that stream
        FlatzincFullExtLexer lexer = new FlatzincFullExtLexer(input);
        // Create a stream of tokens fed by the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // Create a parser that feeds off the token stream
        FlatzincFullExtParser parser = new FlatzincFullExtParser(tokens);
        // Begin parsing at rule prog, get return value structure
        FlatzincFullExtParser.structure_return r = parser.structure();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        walker.mSolver = solver;
        walker.map = map;
        walker.groups = groups;
        PropagationStrategy ps = walker.structure(pe);
        pe.set(ps);
        solver.set(pe);
    }

}
