/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.parser.flatzinc;

import gnu.trove.set.hash.THashSet;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.IParser;
import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.layout.ASolutionPrinter;
import org.chocosolver.parser.flatzinc.layout.SolutionPrinter;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.Once;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public class Flatzinc implements IParser {

    @Argument(required = true, metaVar = "VAL", usage = "Flatzinc file path.")
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions.", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy.", required = false)
    protected boolean free = false;

    @Option(name = "-e", aliases = {"--explanations"}, usage = "Plug explanations in (cbj).", required = false)
    protected boolean expl = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search", required = false)
    protected int nb_cores = 1; // SEEMS USELESS, BUT NEEDED BY CHOCOFZN

    @Option(name = "-ps", required = false, handler = StringArrayOptionHandler.class)
    protected String[] ps = new String[]{"0", "1", "3", "5"};

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    protected String tl = "-1";

    @Option(name = "-stat", aliases = {"--print-statistics"}, usage = "Print statistics on each solution.", required = false)
    protected boolean stat = false;

    protected long tl_ = -1;
    // A unique solver
    protected Solver mSolver;
    // Datas
    public Datas datas;
    public ASolutionPrinter sprinter;
    // List of listeners plugged, ease user interactions.
    List<ParserListener> listeners = new LinkedList<>();
    protected Settings defaultSettings = new FznSettings();


    public Flatzinc() {
    }

    public Flatzinc(boolean all, boolean free, int nb_cores, long tl) {
        this.all = all;
        this.free = free;
        this.nb_cores = nb_cores;
        this.tl_ = tl;
    }

    @Override
    public void addListener(ParserListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ParserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void parseParameters(String[] args) {
        listeners.forEach(ParserListener::beforeParsingParameters);
        System.out.printf("%% %s\n", Arrays.toString(args));
        CmdLineParser cmdparser = new CmdLineParser(this);
        cmdparser.setUsageWidth(160);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("ParseAndSolve [options...] VAL");
            cmdparser.printUsage(System.err);
            System.err.println();
            return;
        }
        cmdparser.getArguments();
        tl_ = SMF.convertInMilliseconds(tl);
        listeners.forEach(ParserListener::afterParsingParameters);
    }

    @Override
    public void defineSettings(Settings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        if (nb_cores > 1) {
            throw new UnsupportedOperationException();
        } else {
            System.out.printf("%% simple solver\n");
            mSolver = new Solver(instance);
            mSolver.set(defaultSettings);
            sprinter = new SolutionPrinter(mSolver, all, stat);
//            Chatterbox.showSolutions(mSolver);
        }
        datas = new Datas();
        datas.setSolPrint(sprinter);

        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws FileNotFoundException {
        listeners.forEach(ParserListener::beforeParsingFile);
        parse(mSolver, new FileInputStream(new File(instance)));
        sprinter.immutable();
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Solver target, InputStream is) {
        CharStream input = new UnbufferedCharStream(is);
        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        parser.flatzinc_model(target, datas, all, free);
        // make complementary search
        makeComplementarySearch(target);
    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param solver a solver
     */
    private void makeComplementarySearch(Solver solver) {
        IntVar[] ovars = new IntVar[solver.getNbVars()];
        THashSet<Variable> dvars = new THashSet<>(Arrays.asList(solver.getStrategy().getVariables()));
        int k = 0;
        for (int i = 0; i < solver.getNbVars(); i++) {
            Variable ivar = solver.getVar(i);
            if (!dvars.contains(ivar) && (ivar.getTypeAndKind() & Variable.INT) != 0) {
                ovars[k++] = (IntVar) ivar;
            }
        }
        solver.set(solver.getStrategy(), new Once(Arrays.copyOf(ovars, k), ISF.lexico_var_selector(), ISF.min_value_selector()));
    }


    @Override
    public void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);

        if (tl_ > -1) {
            SearchMonitorFactory.limitTime(mSolver, tl);
        }
        if (free) {
            mSolver.set(ISF.lastConflict(mSolver));
        }
        if (expl) {
            ExplanationFactory.CBJ.plugin(mSolver, false, false);
        }
        listeners.forEach(ParserListener::afterConfiguringSearch);
    }

    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);
        if (mSolver.getObjectiveManager().isOptimization()) { // optimization problem
            // To deal with Portfolios, we need to get policy and objective
            ResolutionPolicy policy = mSolver.getObjectiveManager().getPolicy();
            IntVar objective = (IntVar) mSolver.getObjectiveManager().getObjective();
            mSolver.findOptimalSolution(policy, objective);
//                mSolver.findSolution();
        } else {
            if (all) {
                mSolver.findAllSolutions();
            } else {
                mSolver.findSolution();
            }
        }
        sprinter.finalOutPut();
        listeners.forEach(ParserListener::afterSolving);
    }

    @Override
    public Solver getSolver() {
        return mSolver;
    }
}
