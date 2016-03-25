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
import java.util.List;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public class Flatzinc extends RegParser {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    @Argument(required = true, metaVar = "file", usage = "Flatzinc file to parse.")
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ", required = false)
    protected boolean free = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).", required = false)
    protected int nb_cores = 1;

    @Option(name = "-ps", required = false, handler = StringArrayOptionHandler.class)
    protected String[] ps = new String[]{"0", "1", "3", "4"};

    // Contains mapping with variables and output prints
    public Datas[] datas;

    protected ParallelPortfolio portfolio = new ParallelPortfolio();

    boolean userinterruption = true;
    final Thread statOnKill;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Flatzinc() {
        this(false,false,1,-1);
    }

    public Flatzinc(boolean all, boolean free, int nb_cores, long tl) {
        super("ChocoFZN");
        this.all = all;
        this.free = free;
        this.nb_cores = nb_cores;
        this.tl_ = tl;
        this.defaultSettings = new FznSettings();
        statOnKill = new Thread() {
            public void run() {
                if (userinterruption) {
                    datas[bestID()].doFinalOutPut(userinterruption);
                    System.out.printf("%% Unexpected resolution interruption!");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(statOnKill);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        nb_cores = Math.min(nb_cores, ps.length);
        assert nb_cores>0;
        if(nb_cores>1) {
            System.out.printf("%% solvers in parallel (%s)\n", Arrays.toString(Arrays.copyOf(ps, nb_cores)));
        }else{
            System.out.printf("%% simple solver\n");
        }
        datas = new Datas[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(instance + "_" + (i+1));
            threadModel.set(defaultSettings);
            portfolio.addModel(threadModel);
            datas[i] = new Datas(threadModel,all,stat);
        }
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws FileNotFoundException {
        listeners.forEach(ParserListener::beforeParsingFile);
        List<Model> models = portfolio.getModels();
        for (int i=0;i<models.size();i++) {
            parse(models.get(i), datas[i], new FileInputStream(new File(instance)));
        }
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Model target, Datas data, InputStream is) {
        CharStream input = new UnbufferedCharStream(is);
        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        parser.flatzinc_model(target, data, all, free);
        // make complementary search
        makeComplementarySearch(target);
    }


    @Override
    public void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);
        if (tl_ > -1) {
            // The time is now initialized for all workers at the same point
            portfolio.getModels().forEach(solver -> solver.getSolver().limitTime(tl));
        }
        for (int i = 0; i < nb_cores; i++) {
            Model worker = portfolio.getModels().get(i);
            Solver s = worker.getSolver();
            Variable[] vars = worker.getVars();
            if (s.getStrategy() != null && s.getStrategy().getVariables().length > 0) {
                vars = s.getStrategy().getVariables();
            }
            IntVar[] dvars = new IntVar[vars.length];
            int k = 0;
            for (int j = 0; j < vars.length; j++) {
                if ((vars[j].getTypeAndKind() & Variable.INT) > 0) {
                    dvars[k++] = (IntVar) vars[j];
                }
            }
            dvars = Arrays.copyOf(dvars, k);
            pickStrategy(i, Integer.parseInt(ps[i]), dvars, worker.getResolutionPolicy());
        }
        listeners.forEach(ParserListener::afterConfiguringSearch);
    }

    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);

        boolean enumerate = portfolio.getModels().get(0).getResolutionPolicy()!=ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (portfolio.solve()){
                datas[bestID()].onSolution();
            }
        }else{
            if(portfolio.solve()){
                datas[bestID()].onSolution();
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        datas[bestID()].doFinalOutPut(userinterruption);
        listeners.forEach(ParserListener::afterSolving);
    }

    @Override
    public Model getModel() {
        Model m = portfolio.getBestModel();
        if (m==null){
            m = portfolio.getModels().get(0);
        }
        return m;
    }

    private int bestID(){
        Model best = getModel();
        for(int i=0;i<nb_cores;i++){
            if(best == portfolio.getModels().get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param w      worker id
     * @param s      strategy id
     * @param vars   decision vars
     * @param policy resolution policy
     */
    void pickStrategy(int w, int s, IntVar[] vars, ResolutionPolicy policy) {
        Solver solver = portfolio.getModels().get(w).getSolver();
        switch (s) {
            case 0:
                // MZN Search + LC (if free)
                if(free)solver.set(lastConflict(solver.getStrategy()));
                break;
            case 1:
                // MZN Search + LC (if first thread is not free)
                if(!free)solver.set(lastConflict(solver.getStrategy()));
                break;
            case 2:
                // Choco default search
                solver.set(intVarSearch(vars));
                break;
            case 3:
                // Choco default search with restars and pglns
                solver.set(intVarSearch(vars));
                solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(solver.getModel(), 0), 1000);
                // TODO LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
                break;
            case 4:
                solver.set(lastConflict(activityBasedSearch(vars)));
                solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(solver.getModel(), 0), 1000);
                solver.setNoGoodRecordingFromRestarts();
                break;
            case 5:
                solver.set(lastConflict(solver.getStrategy()));
                solver.setCBJLearning(false,false);
                break;
            case 6:
                switch (policy) {
                    case SATISFACTION: {
                        solver.set(lastConflict(activityBasedSearch(vars)));
                        solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(solver.getModel(), 0), 1000);
                        solver.setNoGoodRecordingFromRestarts();
                    }
                    break;
                    default: {
                        solver.set(lastConflict(solver.getStrategy()));
                        // TODO LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
                    }
                    break;
                }
                break;
            case 7:
                switch (policy) {
                    case SATISFACTION: {
                        solver.set(lastConflict(activityBasedSearch(vars)));
                        solver.setGeometricalRestart(vars.length * 3, 1.1d, new FailCounter(solver.getModel(), 0), 1000);
                        solver.setNoGoodRecordingFromRestarts();
                    }
                    break;
                    default: {
                        solver.set(lastConflict(solver.getStrategy()));
                        // TODO LNSFactory.pglns(solver, vars, 30, 10, 200, w, new FailCounter(solver, 100));
                    }
                    break;
                }
                break;
        }
    }
}
