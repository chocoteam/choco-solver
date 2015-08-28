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

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.IParser;
import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.layout.ASolutionPrinter;
import org.chocosolver.parser.flatzinc.layout.SharedSolutionPrinter;
import org.chocosolver.parser.flatzinc.layout.SolutionPrinter;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.LNSFactory;
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

    @Argument(required = true, metaVar = "FZN", usage = "Flatzinc file path.")
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ", required = false)
    protected boolean free = false;

    @Option(name = "-e", aliases = {"--explanations"}, usage = "Plug explanations in : CBJ, DBT or NONE (default: NONE).", required = false)
    protected ExplanationFactory expl = ExplanationFactory.NONE;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).", required = false)
    protected int nb_cores = 1; // SEEMS USELESS, BUT NEEDED BY CHOCOFZN

    @Option(name = "-ps", required = false, handler = StringArrayOptionHandler.class)
    protected String[] ps = new String[]{"0", "1", "3", "5"};

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    protected String tl = "-1";

    @Option(name = "-stat", aliases = {"--print-statistics"}, usage = "Print statistics on each solution (default: false).", required = false)
    protected boolean stat = false;

    protected long tl_ = -1;
    // A unique solver
    protected Solver mSolver;
    protected List<Solver> solvers;
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
        nb_cores = Math.min(nb_cores, ps.length);
        if (nb_cores == 1) {
            System.out.printf("%% simple solver\n");
            mSolver = new Solver(instance);
            mSolver.set(defaultSettings);
            sprinter = new SolutionPrinter(mSolver, all, stat);
//            Chatterbox.showSolutions(mSolver);
        } else {
            System.out.printf("%% solvers in parallel (%s)\n", Arrays.toString(Arrays.copyOf(ps, nb_cores)));
            solvers = new LinkedList<>();
            mSolver = new Solver(instance + "_1");
            mSolver.set(defaultSettings);
            solvers.add(mSolver);
            for (int i = 1; i < nb_cores; i++) {
                Solver solver = new Solver(instance + "_" + (i + 1));
                solver.set(defaultSettings);
                solvers.add(solver);
            }
            sprinter = new SharedSolutionPrinter(solvers, all, stat);
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
        for (int i = 1; i < nb_cores; i++) {
            parse(solvers.get(i), new FileInputStream(new File(instance)));
        }
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
            if (nb_cores > 1) {
                long _tl = SMF.convertInMilliseconds(tl);
                // The time is now initialized for all workers at the same point
                solvers.forEach(solver -> SMF.limitTime(solver, _tl));
            } else {
                SearchMonitorFactory.limitTime(mSolver, tl);
            }
        }

        if (nb_cores > 1) {
            ResolutionPolicy policy = mSolver.getObjectiveManager().getPolicy();
            for (int i = 0; i < nb_cores; i++) {
                Solver worker = solvers.get(i);
                Variable[] vars = worker.getVars();
                if (worker.getStrategy() != null
                        && worker.getStrategy().getVariables().length > 0) {
                    vars = worker.getStrategy().getVariables();
                }
                IntVar[] dvars = new IntVar[vars.length];
                int k = 0;
                for (int j = 0; j < vars.length; j++) {
                    if ((vars[j].getTypeAndKind() & Variable.INT) > 0) {
                        dvars[k++] = (IntVar) vars[j];
                    }
                }
                dvars = Arrays.copyOf(dvars, k);
                pickStrategy(i, Integer.parseInt(ps[i]), dvars, policy);
            }
        } else {
            if (free) {
                mSolver.set(ISF.lastConflict(mSolver));
            }
            expl.plugin(mSolver, false, false);
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
            if (nb_cores > 1) {
                THashMap<Solver, IntVar> objs = new THashMap<>();
                for (int i = 0; i < solvers.size(); i++) {
                    objs.put(solvers.get(i), (IntVar) solvers.get(i).getObjectiveManager().getObjective());
                }
                solvers.parallelStream().forEach(solver -> {
                    solver.findOptimalSolution(policy, objs.get(solver));
                    sprinter.imdone(solver);
                    solvers.forEach(other -> other.getSearchLoop().interrupt("Loose thread race", false));
                });
            } else {
                mSolver.findOptimalSolution(policy, objective);
            }
        } else {
            if (all) {
                mSolver.findAllSolutions();
            } else {
                if (nb_cores > 1) {
                    solvers.parallelStream().forEach(solver -> {
                        solver.findSolution();
                        sprinter.imdone(solver);
                        solvers.forEach(other -> other.getSearchLoop().interrupt("Loose thread race", false));
                    });
                } else {
                    mSolver.findSolution();
                }
            }
        }
        sprinter.finalOutPut();
        listeners.forEach(ParserListener::afterSolving);
    }

    @Override
    public Solver getSolver() {
        return mSolver;
    }

    /**
     * @param w      worker id
     * @param s      strategy id
     * @param vars   decision vars
     * @param policy resolution policy
     */
    void pickStrategy(int w, int s, IntVar[] vars, ResolutionPolicy policy) {
        Solver solver = solvers.get(w);
        switch (s) {
            case 0:
//                System.out.printf("%% worker %d: fixed + LC\n", w);
                solver.set(ISF.lastConflict(solver));
                break;
            case 1: {
//                System.out.printf("%% worker %d: wdeg + LC\n", w);
                solver.set(ISF.lastConflict(solver, ISF.domOverWDeg(vars, w)));
                //SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
                //SMF.nogoodRecordingFromRestarts(solver);
            }
            break;
            case 2: {
//                System.out.printf("%% worker %d: wdeg + LNS + LC\n", w);
                solver.set(ISF.lastConflict(solver, ISF.domOverWDeg(vars, w)));
                SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
                LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(100));
            }
            break;
            case 3:
//                System.out.printf("%% worker %d: abs + LC\n", w);
                solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
                SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
                SMF.nogoodRecordingFromRestarts(solver);
                break;
            case 4: {
//                System.out.printf("%% worker %d: fixed + cbj + lc\n", w);
                solver.set(ISF.lastConflict(solver));
                ExplanationFactory.CBJ.plugin(solver, false, false);
            }
            break;
            case 5:
//                System.out.printf("%% worker %d: abs / LNS\n", w);
                switch (policy) {
                    case SATISFACTION: {
                        solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
                        SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
                        SMF.nogoodRecordingFromRestarts(solver);
                    }
                    break;
                    default: {
                        solver.set(ISF.lastConflict(solver));
                        LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(100));
                    }
                    break;
                }
                break;
            case 6:
//                System.out.printf("%% worker %d: abs + LC / LNS\n", w);
                switch (policy) {
                    case SATISFACTION: {
                        solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
                        SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
                        SMF.nogoodRecordingFromRestarts(solver);
                    }
                    break;
                    default: {
                        solver.set(ISF.lastConflict(solver));
                        LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(100));
                    }
                    break;
                }
                break;
        }
    }
}
