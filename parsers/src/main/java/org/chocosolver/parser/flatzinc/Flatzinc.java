/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;
import org.kohsuke.args4j.Option;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public class Flatzinc extends RegParser {

    @Option(name = "-stasol", usage = "Output statistics for solving (default: false).")
    protected boolean oss = false;

    @Option(name = "-ocs", usage = "Opens the complementary search to all variables of the problem " +
            "(default: false, i.e., restricted to the variables declared in output).")
    protected boolean ocs = false;

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
                .set("adhocReification", true);
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
        super.createSolver();
        datas = new Datas[nb_cores];
        String iname = instance == null ? "" : Paths.get(instance).getFileName().toString();
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            portfolio.addModel(threadModel);
            datas[i] = new Datas(threadModel, level, oss);
            threadModel.addHook("CUMULATIVE", "GLB");
        }
    }

    @Override
    public void buildModel() {
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            try {
                long ptime = -System.currentTimeMillis();
                FileInputStream fileInputStream = new FileInputStream(instance);
                parse(models.get(i), datas[i], fileInputStream);
                fileInputStream.close();
                models.get(i).getSolver().logWithANSI(ansi);
                if (level.isLoggable(Level.INFO)) {
                    models.get(i).getSolver().log().white().printf(String.format("File parsed in %d ms%n", (ptime + System.currentTimeMillis())));
                }
                if (level.is(Level.JSON)) {
                    models.get(i).getSolver().log().printf("{\"name\":\"%s\",\"stats\":[", instance);
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

    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    protected void makeComplementarySearch(Model m, int i) {
        if (ocs) {
            super.makeComplementarySearch(m, i);
        } else {
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
                    strats.add(Search.lastConflict(Search.minDomLBSearch(ivars)));
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
