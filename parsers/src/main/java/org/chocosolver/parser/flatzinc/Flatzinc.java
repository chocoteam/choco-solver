/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.Level;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;
import org.kohsuke.args4j.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public class Flatzinc extends RegParser {

    public enum CompleteSearch{
        /**
         * No complementary search (might be incorrect though)
         */
        NO,
        /**
         * Complete the search with a search on variables declared in output annotations
         */
        OUTPUT,
        /**
         * Complete the search with a search on all variables.
         */
        ALL
    }

    @Option(name = "-stasol", usage = "Output statistics for solving (default: false).")
    protected boolean oss = false;

    @Option(name = "-ocs", usage = "Opens the complementary search to all variables of the problem\n" +
            "(default: OUTPUT, i.e., restricted to the variables declared in output).")
    protected CompleteSearch ocs = CompleteSearch.OUTPUT;

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // Contains mapping with variables and output prints
    public Datas[] datas;


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Flatzinc() {
        this(false, false, 1);
    }

    public Flatzinc(boolean all, boolean free, int nb_cores) {
        super("ChocoFZN");
        this.all = all;
        this.free = free;
        this.nb_cores = nb_cores;
    }

    @Override
    public void createSettings() {
        defaultSettings = Settings.prod()
                .setMinCardinalityForSumDecomposition(256)
                .setLearntClausesDominancePerimeter(0)
                .setNbMaxLearntClauses(Integer.MAX_VALUE)
                .setRatioForClauseStoreReduction(.66f)
                .set("adhocReification", true)
                .setLCG(lcg)
//                .setWarnUser(true)
        ;
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                datas[bestModelID()].doFinalOutPut(false);
                if (level.isLoggable(Level.COMPET)) {
                    getModel().getSolver().log().bold().red().print("%% Unexpected resolution interruption!");
                }
            }
        });
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void createSolver() {
        if (level.isLoggable(Level.COMPET)) {
            System.out.printf("%% Choco%s 250127_16:13\n", lcg? " with LCG" : "");
        }
        super.createSolver();
        datas = new Datas[nb_cores];
        String iname = instance == null ? "" : Paths.get(instance).getFileName().toString();
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            threadModel.getSolver().logWithANSI(ansi);
            portfolio.addModel(threadModel);
            datas[i] = new Datas(threadModel, level, oss);
            threadModel.addHook("CUMULATIVE", "GLB");
        }
    }

    @Override
    public void buildModel() {
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            Model m = models.get(i);
            Solver s = m.getSolver();
            try {
                long ptime = -System.currentTimeMillis();
                FileInputStream fileInputStream = new FileInputStream(instance);
                parse(m, datas[i], fileInputStream);
                fileInputStream.close();
                if(logFilePath != null) {
                    s.log().remove(System.out);
                    s.log().add(new PrintStream(Files.newOutputStream(Paths.get(logFilePath)), true));
                } else {
                    s.logWithANSI(ansi);
                }
                if (level.isLoggable(Level.INFO)) {
                    s.log().white().printf(String.format("File parsed in %d ms%n", (ptime + System.currentTimeMillis())));
                }
                if (level.is(Level.JSON)) {
                    s.getMeasures().setReadingTimeCount(System.nanoTime() - s.getModel().getCreationTime());
                    s.log().printf(Locale.US,
                            "{\t\"name\":\"%s\",\n" +
                                    "\t\"variables\": %d,\n" +
                                    "\t\"constraints\": %d,\n" +
                                    "\t\"policy\": \"%s\",\n" +
                                    "\t\"parsing time\": %.3f,\n" +
                                    "\t\"building time\": %.3f,\n" +
                                    "\t\"memory\": %d,\n" +
                                    "\t\"stats\":[",
                            instance,
                            m.getNbVars(),
                            m.getNbCstrs(),
                            m.getSolver().getObjectiveManager().getPolicy(),
                            (ptime + System.currentTimeMillis()) / 1000f,
                            s.getReadingTimeCount(),
                            m.getEstimatedMemory()
                            );
                }
            } catch (IOException e) {
                throw new Error(e.getMessage());
            }
        }
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
        //parser.setProfile(true);
        parser.flatzinc_model(target, data);
        /*ParseInfo parseInfo = parser.getParseInfo();
        ATN atn = parser.getATN();
        for (DecisionInfo di : parseInfo.getDecisionInfo()) {
            DecisionState ds = atn.decisionToState.get(di.decision);
            String ruleName = Flatzinc4Parser.ruleNames[ds.ruleIndex];
            System.out.println(ruleName +" -> " + di.toString());
        }*/
    }

    @Override
    public void freesearch(Solver solver) {
        BlackBoxConfigurator bb = BlackBoxConfigurator.init();
        boolean opt = solver.getObjectiveManager().isOptimization();
        // variable selection
        SearchParams.ValSelConf defaultValSel = new SearchParams.ValSelConf(
                SearchParams.ValueSelection.MIN, opt, 1, opt);
        SearchParams.VarSelConf defaultVarSel = new SearchParams.VarSelConf(
                SearchParams.VariableSelection.DOMWDEG, Integer.MAX_VALUE);
        bb.setIntVarStrategy((vars) -> defaultVarSel.make().apply(vars, defaultValSel.make().apply(vars[0].getModel())));
        // restart policy
        SearchParams.ResConf defaultResConf = new SearchParams.ResConf(
                SearchParams.Restart.LUBY, 500, 50_000, true);
        bb.setRestartPolicy(defaultResConf.make());
        // other parameters
        bb.setNogoodOnRestart(true)
                .setRestartOnSolution(true)
                .setExcludeObjective(true)
                .setExcludeViews(false)
                .setMetaStrategy(m -> Search.lastConflict(m, 1));
        if (level.isLoggable(Level.INFO)) {
            solver.log().println(bb.toString());
        }
        bb.make(solver.getModel());
    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    protected void makeComplementarySearch(Model m, int i) {
        if (ocs == CompleteSearch.ALL) {
            super.makeComplementarySearch(m, i);
        } else if (ocs == CompleteSearch.OUTPUT) {
            Solver solver = m.getSolver();
            List<AbstractStrategy<?>> strats = new LinkedList<>();
            strats.add(solver.getSearch());
            if (solver.getSearch() != null) {
                IntVar[] ivars = Stream.of(datas[i].allOutPutVars())
                        .filter(VariableUtils::isInt)
                        .filter(v -> !VariableUtils.isConstant(v))
                        .map(Variable::asIntVar)
                        .sorted(Comparator.comparingInt(IntVar::getDomainSize))
                        .toArray(IntVar[]::new);
                if (ivars.length > 0) {
                    IntValueSelector valueSelector;
                    if (m.getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                            || !(m.getObjective() instanceof IntVar)) {
                        valueSelector = new IntDomainMin();
                    } else {
                        valueSelector = new IntDomainBest();
                        valueSelector = new IntDomainLast(m.getSolver().defaultSolution(), valueSelector, null);
                    }
                    strats.add(Search.lastConflict(new IntStrategy(ivars, new FirstFail(m), valueSelector)));
                }
                SetVar[] svars = Stream.of(datas[i].allOutPutVars())
                        .filter(VariableUtils::isSet)
                        .map(Variable::asSetVar)
                        .sorted(Comparator.comparingInt(s -> s.getUB().size()))
                        .toArray(SetVar[]::new);
                if (svars.length > 0) {
                    strats.add(Search.setVarSearch(svars));
                }
                solver.setSearch(strats.toArray(new AbstractStrategy[0]));
            }
        }
    }

    protected void singleThread() {
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
        /*solver.plugMonitor(new IMonitorDownBranch() {
            @Override
            public void beforeDownBranch(boolean left) {
                solver.log().printf("[%d] %s %s %s %n",
                        solver.getEnvironment().getWorldIndex() - 2,
                        solver.getDecisionPath().getLastDecision().getDecisionVariable().getName(),
                        left ? "=" : "!=",
                        solver.getDecisionPath().getLastDecision().getDecisionValue().toString());
            }
        });*/
        //solver.showShortStatistics();
        if (level.isLoggable(Level.INFO)) {
            solver.log().bold().printf("== %d flatzinc ==%n", datas[0].cstrCounter().values().stream().mapToInt(i -> i).sum());
            datas[0].cstrCounter().entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(e ->
                            solver.log().printf("\t%s #%d\n", e.getKey(), e.getValue())
                    );
            solver.printShortFeatures();
            getModel().displayVariableOccurrences();
            getModel().displayPropagatorOccurrences();
        }
        if (enumerate) {
            while (solver.solve()) {
                datas[0].onSolution();
            }
        } else {
            if (solver.solve()) {
                datas[0].onSolution();
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        datas[0].doFinalOutPut(!userinterruption && runInTime());
    }

    protected void manyThread() {
        boolean enumerate = portfolio.getModels().get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (portfolio.solve()) {
                datas[bestModelID()].onSolution();
            }
        } else {
            if (portfolio.solve()) {
                datas[bestModelID()].onSolution();
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        datas[bestModelID()].doFinalOutPut(!userinterruption && runInTime());
    }
}
