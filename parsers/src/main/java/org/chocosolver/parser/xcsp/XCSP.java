/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.xcsp;

import org.chocosolver.solver.search.restart.LubyCutoff;
import org.chocosolver.parser.Level;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.logger.Logger;
import org.kohsuke.args4j.Option;
import org.xcsp.parser.callbacks.SolutionChecker;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.nio.file.Files;
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

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-cs", usage = "set to true to check solution with org.xcsp.checker.SolutionChecker")
    private boolean cs = false;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-flt")
    private boolean flatten = false;

    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public XCSP() {
        super("ChocoXCSP");
    }

    @Override
    public void createSettings() {
        defaultSettings = Settings.prod();
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
            threadModel.getSolver().logWithANSI(ansi);
            portfolio.addModel(threadModel);
            parsers[i] = new XCSPParser();
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
                parse(m, parsers[i], i);
                if(logFilePath != null) {
                    s.log().remove(System.out);
                    s.log().add(new PrintStream(Files.newOutputStream(Paths.get(logFilePath)), true));
                }else {
                    s.logWithANSI(ansi);
                }
                if (level.isLoggable(Level.INFO)) {
                    s.log().white().printf("File parsed in %d ms%n", (ptime + System.currentTimeMillis()));
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
            } catch (Exception e) {
                if (level.isLoggable(Level.INFO)) {
                    s.log().red().print("s UNSUPPORTED\n");
                    s.log().printf("c %s\n", e.getMessage());
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
            solver.setRestarts(count -> solver.getFailCount() >= count, new LubyCutoff(500), 5000);
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
        output.setLength(0);
        output.append(parser.printSolution(!flatten));
        if (solver.getObjectiveManager().isOptimization()) {
            if (level.isLoggable(Level.COMPET) || level.is(Level.RESANA)) {
                solver.log().printf(java.util.Locale.US, "o %d %.1f\n",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
            }
            if (level.is(Level.JSON)) {
                solver.log().printf(Locale.US, "%s\n\t\t{\"bound\":%d, \"time\":%.1f, " +
                                "\"solutions\":%d, \"nodes\":%d, \"failures\":%d, \"restarts\":%d}",
                        solver.getSolutionCount() > 1 ? "," : "",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount(),
                        solver.getSolutionCount(),
                        solver.getNodeCount(),
                        solver.getFailCount(),
                        solver.getRestartCount());
            }
        } else {
            if (level.isLoggable(Level.COMPET)) {
                solver.log().println(output.toString());
            }
            if (level.is(Level.JSON)) {
                solver.log().printf(Locale.US, "\t\t{\"time\":%.1f," +
                                "\"solutions\":%d, \"nodes\":%d, \"failures\":%d, \"restarts\":%d}",
                        solver.getTimeCount(),
                        solver.getSolutionCount(),
                        solver.getNodeCount(),
                        solver.getFailCount(),
                        solver.getRestartCount());
            }
        }
        
        if (level.isLoggable(Level.INFO)) {
            solver.log().white().printf("%s %n", solver.getMeasures().toOneLineString());
        }
        if (cs) {
            try {
                output.insert(0, "s SATISFIABLE\n");
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
            output.append("d FOUND SOLUTIONS ").append(solver.getSolutionCount()).append("\n");
            log.println(output.toString());
        }
        log.reset();
        if (level.is(Level.RESANA)) {
            solver.log().printf(java.util.Locale.US, "s %s %.1f\n",
                    complete ? "T" : "S",
                    solver.getTimeCount());
        }
        if (level.is(Level.JSON)) {
            solver.log().printf(Locale.US, "\n\t],\n\t\"exit\":{\"time\":%.1f, " +
                            "\"nodes\":%d, \"failures\":%d, \"restarts\":%d, \"status\":\"%s\"}\n}",
                    solver.getTimeCount(),
                    solver.getNodeCount(),
                    solver.getFailCount(),
                    solver.getRestartCount(),
                    solver.getSearchState()
            );
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
