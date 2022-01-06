/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.dimacs;

import org.chocosolver.parser.Level;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.util.logger.Logger;
import org.kohsuke.args4j.Option;

import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * @author Charles Prud'homme
 * @since 04/03/2021
 */
public class DIMACS extends RegParser {

    // Contains mapping with variables and output prints
    public DIMACSParser[] parsers;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-cp", usage = "Pure CP approach (does not rely on the underlying SAT solver).")
    private boolean cp = false;

    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public DIMACS() {
        super("ChocoDimacs");
    }

    @Override
    public void createSettings() {
        defaultSettings = Settings.prod()
                .setEnableSAT(!cp);
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                finalOutPut(getModel().getSolver());
                if (level.isLoggable(Level.COMPET)) {
                    getModel().getSolver().log().bold().red().println("Unexpected resolution interruption!");
                }
            }
        });
    }

    @Override
    public void createSolver() {
        super.createSolver();
        String iname = Paths.get(instance).getFileName().toString();
        parsers = new DIMACSParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            portfolio.addModel(threadModel);
            parsers[i] = new DIMACSParser();
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
                    models.get(i).getSolver().log().red().print("UNSUPPORTED\n");
                    models.get(i).getSolver().log().printf("%s\n", e.getMessage());
                }
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
        if (level.isLoggable(Level.INFO)) {
            getModel().displayVariableOccurrences();
            getModel().displayPropagatorOccurrences();
        }
    }

    public void parse(Model target, DIMACSParser parser, int i) throws Exception {
        parser.model(target, instance);
        if (i == 0) {
            Solver solver = target.getSolver();
            if (target.getNbRealVar() == 0) {
                target.getSolver().setSearch(
                        Search.domOverWDegSearch(getModel().retrieveBoolVars())
                );
                solver.setLubyRestart(500, new FailCounter(target, 0), 5000);
            } else {
                solver.setSearch(Search.defaultSearch(target));
                solver.setLubyRestart(500, new FailCounter(target, 0), 5000);
            }
        }
    }

    protected void singleThread() {
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
        if (level.isLoggable(Level.INFO)) {
            solver.printShortFeatures();
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


    private void onSolution(Solver solver, DIMACSParser parser) {
        if (solver.getObjectiveManager().isOptimization()) {
            if (level.is(Level.RESANA))
                solver.log().printf(java.util.Locale.US, "o %d %.1f\n",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
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
    }

    private void finalOutPut(Solver solver) {
        boolean complete = !userinterruption && runInTime();
        Logger log = solver.log().bold();
        if (solver.getSolutionCount() > 0) {
            log = log.green();
            if (solver.getObjectiveManager().isOptimization() && complete) {
                output.insert(0, "OPTIMUM FOUND\n");
            } else {
                output.insert(0, "SATISFIABLE\n");
            }
        } else if (complete) {
            log = log.red();
            output.insert(0, "UNSATISFIABLE\n");
        } else {
            log = log.black();
            output.insert(0, "UNKNOWN\n");
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
        if (level.is(Level.IRACE)) {
            solver.log().printf(Locale.US, "%d %d",
                    solver.getObjectiveManager().isOptimization() ?
                            (solver.getObjectiveManager().getPolicy().equals(ResolutionPolicy.MAXIMIZE) ? -1 : 1)
                                    * solver.getObjectiveManager().getBestSolutionValue().intValue() :
                            -solver.getSolutionCount(),
                    complete ?
                            (int) Math.ceil(solver.getTimeCount()) :
                            Integer.MAX_VALUE);
        }
        if (level.isLoggable(Level.INFO)) {
            solver.printShortFeatures();
            solver.getMeasures().toOneLineString();
        }
    }
}
