/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.parser.Level;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.logger.Logger;
import org.kohsuke.args4j.Option;
import org.xcsp.parser.callbacks.SolutionChecker;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSP extends RegParser {

    // Contains mapping with variables and output prints
    public XCSPParser[] parsers;

    @Option(name = "-cs", usage = "set to true to check solution with org.xcsp.checker.SolutionChecker")
    private boolean cs = false;

    @Option(name = "-cst")
    private boolean cst = false;

    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public XCSP() {
        super("ChocoXCSP");
    }

    @Override
    public void createSettings() {
        defaultSettings = Settings.init()
                .setEnableSAT(true)
                .setModelChecker(solver -> true);
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                finalOutPut(getModel().getSolver());
                if (level.isLoggable(Level.COMPET)) {
                    getModel().getSolver().log().bold().red().print("c Unexpected resolution interruption!");
                }
            }
        });
    }

    @Override
    public void createSolver() {
        super.createSolver();
        if (level.isLoggable(Level.COMPET)) {
            System.out.print("c Choco e747e1e\n");
        }
        String iname = Paths.get(instance).getFileName().toString();
        parsers = new XCSPParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            portfolio.addModel(threadModel);
            parsers[i] = new XCSPParser();
        }
    }

    @Override
    public void buildModel() {
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            try {
                long ptime = -System.currentTimeMillis();
                parse(models.get(i), parsers[i], i);
                models.get(i).getSolver().logWithANSI(ansi);
                if (level.isLoggable(Level.INFO)) {
                    models.get(i).getSolver().log().white().printf("File parsed in %d ms%n", (ptime + System.currentTimeMillis()));
                }
                if (level.is(Level.JSON)) {
                    models.get(i).getSolver().log().printf("{\"name\":\"%s\",\"stats\":[", instance);
                }
            } catch (Exception e) {
                if (level.isLoggable(Level.INFO)) {
                    models.get(i).getSolver().log().red().print("s UNSUPPORTED\n");
                    models.get(i).getSolver().log().printf("c %s\n", e.getMessage());
                }
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
    }

    public void parse(Model target, XCSPParser parser, int i) throws Exception {
        parser.model(target, instance);
        if (i == 0) {
            IntVar[] decVars = (IntVar[]) getModel().getHook("decisions");
            if (decVars == null) {
                decVars = parser.mvars.values().toArray(new IntVar[0]);
            }
            Arrays.sort(decVars, Comparator.comparingInt(IntVar::getId));
            Solver solver = target.getSolver();
            solver.setSearch(Search.defaultSearch(target));
            solver.setNoGoodRecordingFromRestarts();
            solver.setRestarts(count -> solver.getFailCount() >= count, new LubyCutoffStrategy(500), 5000);
        }
    }


    protected void singleThread() {
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
        if (level.isLoggable(Level.INFO)) {
            //solver.printShortFeatures();
            getModel().displayVariableOccurrences();
            getModel().displayPropagatorOccurrences();
        }
        if (enumerate) {
            while (solver.solve()) {
                onSolution(solver, parsers[0]);
            }
        } else {
            if (solver.solve()) {
                onSolution(solver, parsers[0]);
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        finalOutPut(solver);
    }

    protected void manyThread() {
        boolean enumerate = portfolio.getModels().get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (portfolio.solve()) {
                onSolution(getModel().getSolver(), parsers[bestModelID()]);
            }
        } else {
            if (portfolio.solve()) {
                onSolution(getModel().getSolver(), parsers[bestModelID()]);
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        finalOutPut(getModel().getSolver());
    }


    private void onSolution(Solver solver, XCSPParser parser) {
        if (solver.getObjectiveManager().isOptimization()) {
            if (level.is(Level.RESANA)) {
                solver.log().printf(java.util.Locale.US, "o %d %.1f\n",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
            }
            if (level.is(Level.JSON)) {
                solver.log().printf(Locale.US, "%s{\"bound\":%d,\"time\":%.1f}",
                        solver.getSolutionCount() > 1 ? "," : "",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
            }
        } else {
            if (level.is(Level.JSON)) {
                solver.log().printf("{\"time\":%.1f},",
                        solver.getTimeCount());
            }
        }
        output.setLength(0);
        output.append(parser.printSolution());
        if (level.isLoggable(Level.INFO)) {
            solver.log().white().printf("%s %n", solver.getMeasures().toOneLineString());
        }
        if (cs) {
            try {
                new SolutionChecker(true, instance, new ByteArrayInputStream(output.toString().getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("wrong solution found twice");
            }
        }
    }

    private void finalOutPut(Solver solver) {
        boolean complete = !userinterruption && runInTime();//solver.getSearchState() == SearchState.TERMINATED;
        Logger log = solver.log().bold();
        if (solver.getSolutionCount() > 0) {
            log = log.green();
            if (solver.getObjectiveManager().isOptimization() && complete) {
                output.insert(0, "s OPTIMUM FOUND\n");
            } else {
                output.insert(0, "s SATISFIABLE\n");
            }
        } else if (complete) {
            output.insert(0, "s UNSATISFIABLE\n");
            log = log.red();
        } else {
            output.insert(0, "s UNKNOWN\n");
            log = log.black();
        }
        if (level.isLoggable(Level.COMPET)) {
            log.println(output.toString());
        }
        log.reset();
        if (level.is(Level.RESANA)) {
            solver.log().printf(java.util.Locale.US, "s %s %.1f\n",
                    complete ? "T" : "S",
                    solver.getTimeCount());
        }
        if (level.is(Level.JSON)) {
            solver.log().printf(Locale.US, "],\"exit\":{\"time\":%.1f,\"status\":\"%s\"}}",
                    solver.getTimeCount(), complete ? "terminated" : "stopped");
        }
        if (level.isLoggable(Level.INFO)) {
            solver.getMeasures().toOneLineString();
        }
        if (csv) {
            solver.printCSVStatistics();
        }
        if (cs) {
            try {
                new SolutionChecker(true, instance, new ByteArrayInputStream(output.toString().getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
