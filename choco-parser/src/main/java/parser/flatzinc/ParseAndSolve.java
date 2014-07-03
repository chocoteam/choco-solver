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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.Exit;
import parser.flatzinc.ast.GoalConf;
import solver.Solver;
import solver.constraints.Constraint;
import solver.explanations.ExplanationFactory;
import solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import solver.propagation.hardcoded.TwoBucketPropagationEngine;
import solver.search.loop.monitors.SMF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class ParseAndSolve {

    protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  MINIZINC OPTIONS   ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // receives other command line parameters than options
    @Argument
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions.", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy.", required = false)
    protected boolean free = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search", required = false)
    protected int nb_cores = 1;

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    protected long tl = -1;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  CHOCO OPTIONS   ///////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Option(name = "-seed", usage = "Seed for randomness", required = false)
    protected long seed = 29091981L;

    @Option(name = "-csv", usage = "CSV file path to trace the results.", required = false)
    public String csv = "";

    @Option(name = "-db", aliases = {"--database"}, usage = "Query a database", required = false)
    public String dbproperties = "";

    @Option(name = "-dbbn", aliases = {"--database-bench-name"}, usage = "Benchmark name", required = false)
    public String dbbenchname = "";

    @Option(name = "-bbss", usage = "Black box search strategy:\n1(*): activity based\n2: impact based\n3: dom/wdeg", required = false)
    protected int bbss = 1;

    @Option(name = "-dv", usage = "Use same decision variables as declared in file (default false)", required = false)
    protected boolean decision_vars = false;

    @Option(name = "-lf", usage = "Last Conflict.", required = false)
    protected boolean lastConflict;

    @Option(name = "-lns", usage = "Plug Large Neighborhood Seach in", required = false)
    protected GoalConf.LNS lns = GoalConf.LNS.NONE;

    @Option(name = "-fr", aliases = "--fast-restart", usage = "Force fast restart (fail 20).", required = false)
    protected boolean fr = false;

    @Option(name = "-exp", aliases = "--exp-eng", usage = "Type of explanation engine to plug in")
    protected ExplanationFactory expeng = ExplanationFactory.NONE;

    @Option(name = "-fe", aliases = "--flatten-expl", usage = "Flatten explanations (automatically plug ExplanationFactory.SILENT in if undefined).", required = false)
    protected boolean fexp = false;

    @Option(name = "-e", aliases = {"--engine"}, usage = "Engine Number.\n1: constraint\n2: variable\n3(*): 7q cstrs\n4: fast variable", required = false)
    protected byte eng = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Solver solver;
    public GoalConf gc;


    public void doMain(String[] args) throws IOException, RecognitionException {
        parse(args);
		if(ParserConfiguration.PRINT_CONSTRAINT && LOGGER.isInfoEnabled()){
			ArrayList<String> l = new ArrayList<>();
			LOGGER.info("% INVOLVED CONSTRAINTS (CHOCO) ");
			for(Constraint c:solver.getCstrs()){
				if(!l.contains(c.getName())) {
					l.add(c.getName());
					LOGGER.info("% {}", c.getName());
				}
			}
		}
        solve();
    }

    public void parse(String[] args) throws IOException, RecognitionException {
        CmdLineParser cmdparser = new CmdLineParser(this);
        cmdparser.setUsageWidth(160);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("ParseAndSolve [options...] fzn_instance...");
            cmdparser.printUsage(System.err);
            System.err.println("\nCheck MiniZinc is correctly installed.");
            System.err.println();
            return;
        }
        gc = new GoalConf(free, bbss, decision_vars, all, seed, lastConflict, tl, lns, fr);
        LOGGER.info("% parse instance...");
        solver = new Solver();
        long creationTime = -System.nanoTime();
        Datas datas = new Datas(gc);
        buildLayout(datas);
        boolean removeB2I = false;
        if (removeB2I) {// pas forcement plus rapide mais facilite l'analyse
            PreprocessFZN.processB2I(instance);
            buildParser(new FileInputStream(new File(instance + "_")), solver, datas);
        } else {
            buildParser(new FileInputStream(new File(instance)), solver, datas);
        }
        makeEngine(solver, datas);
        if (!solver.getExplainer().isActive()) {
            if (expeng != ExplanationFactory.NONE) {
                expeng.plugin(solver, fexp);
            } else if (fexp) {
                ExplanationFactory.SILENT.plugin(solver, fexp);
            }
        }
        datas.clear();
        solver.getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
    }

    public void solve() throws IOException {
        LOGGER.info("% solve instance...");
        if(ParserConfiguration.PRINT_SEARCH) SMF.log(solver, true, true);
        solver.getSearchLoop().launch((!solver.getObjectiveManager().isOptimization()) && !gc.all);
    }

    public void buildParser(InputStream is, Solver mSolver, Datas datas) {
        try {
            // Create an input character stream from standard in
            ANTLRInputStream input = new ANTLRInputStream(is);
            // Create an ExprLexer that feeds from that stream
            Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
            // Create a stream of tokens fed by the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // Create a parser that feeds off the token stream
            Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL); // try with simpler/faster SLL(*)
            parser.flatzinc_model(mSolver, datas);
        } catch (IOException io) {
            Exit.log(io.getMessage());
        }
    }

    protected void makeEngine(Solver solver, Datas datas) {
        switch (eng) {
            case 1:
                solver.set(new TwoBucketPropagationEngine(solver));
                break;
            default:
            case 2:
                solver.set(new SevenQueuesPropagatorEngine(solver));
                break;
        }
    }

    public Solver getSolver() {
        return solver;
    }

    public void buildLayout(Datas datas) {
        FZNLayout fl = new FZNLayout(instance, csv, gc, dbproperties, dbbenchname);
        datas.setmLayout(fl);
        fl.makeup();
    }
}
