/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package org.chocosolver.parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.layout.ASolutionPrinter;
import org.chocosolver.parser.flatzinc.layout.SharedSolutionPrinter;
import org.chocosolver.parser.flatzinc.layout.SolutionPrinter;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public class FlatzincPortfolio {//extends RegParser {

//    @Argument(required = true, metaVar = "file", usage = "Flatzinc file to parse.")
//    public String instance;
//
//    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).", required = false)
//    protected boolean all = false;
//
//    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ", required = false)
//    protected boolean free = false;
//
//    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).", required = false)
//    protected int nb_cores = 1; // SEEMS USELESS, BUT NEEDED BY CHOCOFZN
//
//    @Option(name = "-ps", required = false, handler = StringArrayOptionHandler.class)
//    protected String[] ps = new String[]{"0", "1", "3", "5"};
//
//    // Datas
//    public Datas datas;
//    public ASolutionPrinter sprinter;
//    protected ParallelPortfolio portfolio;
//
//    public FlatzincPortfolio(boolean all, boolean free, int nb_cores, long tl) {
//        super("ChocoFZN");
//        this.all = all;
//        this.free = free;
//        this.nb_cores = nb_cores;
//        this.tl_ = tl;
//        this.defaultSettings = new FznSettings();
//    }
//
//    @Override
//    public void createSolver() {
//        listeners.forEach(ParserListener::beforeSolverCreation);
//        nb_cores = Math.min(nb_cores, ps.length);
//        assert nb_cores>1;
//        System.out.printf("%% solvers in parallel (%s)\n", Arrays.toString(Arrays.copyOf(ps, nb_cores)));
//        ParallelPortfolio portfolio = new ParallelPortfolio();
//        for (int i = 0; i < nb_cores; i++) {
//            Model threadModel = new Model(instance + "_" + (i+1));
//            threadModel.set(defaultSettings);
//            portfolio.addModel(threadModel);
//        }
//        mModel = portfolio.getModels().get(0);
//        sprinter = new SharedSolutionPrinter(portfolio.getModels(), all, stat);
//        datas = new Datas();
//        datas.setSolPrint(sprinter);
//
//        listeners.forEach(ParserListener::afterSolverCreation);
//    }
//
//    @Override
//    public void parseInputFile() throws FileNotFoundException {
//        listeners.forEach(ParserListener::beforeParsingFile);
//        parse(mModel, new FileInputStream(new File(instance)));
//        sprinter.immutable();
//        for (int i = 1; i < nb_cores; i++) {
//            parse(solvers.get(i), new FileInputStream(new File(instance)));
//        }
//        listeners.forEach(ParserListener::afterParsingFile);
//    }
//
//    public void parse(Model target, InputStream is) {
//        CharStream input = new UnbufferedCharStream(is);
//        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
//        lexer.setTokenFactory(new CommonTokenFactory(true));
//        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
//        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
//        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
//        parser.setBuildParseTree(false);
//        parser.setTrimParseTree(false);
//        parser.flatzinc_model(target, datas, all, free);
//        // make complementary search
//        makeComplementarySearch(target);
//    }
//
//
//    @Override
//    public void configureSearch() {
//        listeners.forEach(ParserListener::beforeConfiguringSearch);
//
//        if (tl_ > -1) {
//            if (nb_cores > 1) {
//                long _tl = SMF.convertInMilliseconds(tl);
//                // The time is now initialized for all workers at the same point
//                solvers.forEach(solver -> SMF.limitTime(solver, _tl));
//            } else {
//                SearchMonitorFactory.limitTime(mSolver, tl);
//            }
//        }
//
//        if (nb_cores > 1) {
//            ResolutionPolicy policy = mSolver.getObjectiveManager().getPolicy();
//            for (int i = 0; i < nb_cores; i++) {
//                Solver worker = solvers.get(i);
//                Variable[] vars = worker.getVars();
//                if (worker.getStrategy() != null
//                        && worker.getStrategy().getVariables().length > 0) {
//                    vars = worker.getStrategy().getVariables();
//                }
//                IntVar[] dvars = new IntVar[vars.length];
//                int k = 0;
//                for (int j = 0; j < vars.length; j++) {
//                    if ((vars[j].getTypeAndKind() & Variable.INT) > 0) {
//                        dvars[k++] = (IntVar) vars[j];
//                    }
//                }
//                dvars = Arrays.copyOf(dvars, k);
//                pickStrategy(i, Integer.parseInt(ps[i]), dvars, policy);
//            }
//        } else {
//            if (free) {
//                mSolver.set(ISF.lastConflict(mSolver));
//            }
//            expl.plugin(mSolver, false, false);
//        }
//        listeners.forEach(ParserListener::afterConfiguringSearch);
//    }
//
//    @Override
//    public void solve() {
//        listeners.forEach(ParserListener::beforeSolving);
//        if (mModel.getResolutionPolicy()!=ResolutionPolicy.SATISFACTION) { // optimization problem
//            // To deal with Portfolios, we need to get policy and objective
//            ResolutionPolicy policy = mModel.getResolutionPolicy();
//            if (nb_cores > 1) {
//                solvers.parallelStream().forEach(solver -> {
//                    solver.findOptimalSolution(policy);
//                    sprinter.imdone(solver);
//                    solvers.forEach(other -> other.getSearchLoop().interrupt("Loose thread race", false));
//                });
//            } else {
//                mSolver.findOptimalSolution(policy);
//            }
//        } else {
//            if (all) {
//                mSolver.findAllSolutions();
//            } else {
//                if (nb_cores > 1) {
//                    solvers.parallelStream().forEach(solver -> {
//                        solver.findSolution();
//                        sprinter.imdone(solver);
//                        solvers.forEach(other -> other.getSearchLoop().interrupt("Loose thread race", false));
//                    });
//                } else {
//                    mSolver.findSolution();
//                }
//            }
//        }
//        sprinter.finalOutPut();
//        listeners.forEach(ParserListener::afterSolving);
//    }
//
//    @Override
//    public Model getModel() {
//        return mModel;
//    }
//
//    /**
//     * @param w      worker id
//     * @param s      strategy id
//     * @param vars   decision vars
//     * @param policy resolution policy
//     */
//    void pickStrategy(int w, int s, IntVar[] vars, ResolutionPolicy policy) {
//        Solver solver = solvers.get(w);
//        switch (s) {
//            case 0:
////                System.out.printf("%% worker %d: fixed + LC\n", w);
//                solver.set(ISF.lastConflict(solver));
//                break;
//            case 1: {
////                System.out.printf("%% worker %d: wdeg + LC\n", w);
//                solver.set(ISF.lastConflict(solver, ISF.domOverWDeg(vars, w)));
//                //SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(0), 1000);
//                //SMF.nogoodRecordingFromRestarts(solver);
//            }
//            break;
//            case 2: {
////                System.out.printf("%% worker %d: wdeg + LNS + LC\n", w);
//                solver.set(ISF.lastConflict(solver, ISF.domOverWDeg(vars, w)));
//                SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(solver, 0), 1000);
//                LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
//            }
//            break;
//            case 3:
////                System.out.printf("%% worker %d: abs + LC\n", w);
//                solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
//                SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(solver, 0), 1000);
//                SMF.nogoodRecordingFromRestarts(solver);
//                break;
//            case 4: {
////                System.out.printf("%% worker %d: fixed + cbj + lc\n", w);
//                solver.set(ISF.lastConflict(solver));
//                ExplanationFactory.CBJ.plugin(solver, false, false);
//            }
//            break;
//            case 5:
////                System.out.printf("%% worker %d: abs / LNS\n", w);
//                switch (policy) {
//                    case SATISFACTION: {
//                        solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
//                        SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(solver, 0), 1000);
//                        SMF.nogoodRecordingFromRestarts(solver);
//                    }
//                    break;
//                    default: {
//                        solver.set(ISF.lastConflict(solver));
//                        LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
//                    }
//                    break;
//                }
//                break;
//            case 6:
////                System.out.printf("%% worker %d: abs + LC / LNS\n", w);
//                switch (policy) {
//                    case SATISFACTION: {
//                        solver.set(ISF.lastConflict(solver, ISF.activity(vars, w)));
//                        SearchMonitorFactory.geometrical(solver, vars.length * 3, 1.1d, new FailCounter(solver, 0), 1000);
//                        SMF.nogoodRecordingFromRestarts(solver);
//                    }
//                    break;
//                    default: {
//                        solver.set(ISF.lastConflict(solver));
//                        LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
//                    }
//                    break;
//                }
//                break;
//        }
//    }
}
