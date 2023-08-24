/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.mps;

import org.chocosolver.parser.Level;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.restart.LubyCutoff;
import org.chocosolver.solver.search.restart.Restarter;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.logger.Logger;
import org.kohsuke.args4j.Option;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Created by cprudhom on 01/09/15. Project: choco-parsers.
 */
public class MPS extends RegParser {

    // Contains mapping with variables and output prints
    public MPSParser[] parsers;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-max", usage = "define to maximize (default: to minimize).")
    private boolean maximize = false;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-prec", usage = "set to the precision (default: 1.0E-4D).")
    private double precision = 1.0E-4D;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-ibex", usage = "Use Ibex for non-full integer equations (default: false).")
    private boolean ibex = false;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-ninf", usage = "define negative infinity (default: " + IntVar.MIN_INT_BOUND + ").")
    private double ninf = Integer.MIN_VALUE / 10d;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-pinf", usage = "define positive infinity (default: " + IntVar.MAX_INT_BOUND + ").")
    private double pinf = Integer.MAX_VALUE / 10d;

    @SuppressWarnings("FieldMayBeFinal")
    @Option(name = "-noeq", usage = "Split EQ constraints into a LQ and a GQ constraint.")
    private boolean noeq = false;

    @Option(name = "-split", usage = "Split any contraints of cardinality greater than this value (default: 100).")
    int split = 100;


    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public MPS() {
        super("ChocoMPS");
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                finalOutPut(getModel().getSolver());
                if (level.isLoggable(Level.COMPET)) {
                    getModel().getSolver().log().bold().red().println("c Unexpected resolution interruption!");
                }
            }
        });
    }

    @Override
    public void createSolver() {
        super.createSolver();
        String iname = Paths.get(instance).getFileName().toString();
        parsers = new MPSParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            threadModel.getSolver().logWithANSI(ansi);
            threadModel.setPrecision(precision);
            portfolio.addModel(threadModel);
            parsers[i] = new MPSParser();
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
                if (logFilePath != null) {
                    s.log().remove(System.out);
                    s.log().add(new PrintStream(Files.newOutputStream(Paths.get(logFilePath)), true));
                } else {
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
                    s.log().red().print("UNSUPPORTED\n");
                    s.log().printf("%s\n", e.getMessage());
                }
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
    }

    public void parse(Model target, MPSParser parser, int i) throws Exception {
        parser.model(target, instance, maximize, ninf, pinf, ibex, noeq);
        if (i == 0) {
            if (target.getNbRealVar() == 0) {
                BlackBoxConfigurator.init()
                        .setIntVarStrategy(vs -> new IntStrategy(
                                vs,
                                new FirstFail(target),
                                new IntDomainBest()))
                        .make(target);
            } else {
                BlackBoxConfigurator.init()
                        .setRestartPolicy(
                                s -> new Restarter(
                                        new LubyCutoff(500),
                                        c -> s.getFailCount() >= c, 50_000, true))
                        .setNogoodOnRestart(false) // not supported for real variables
                        .make(target);
            }
        }
    }


    protected void singleThread() {
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
        if (level.isLoggable(Level.INFO)) {
            solver.printShortFeatures();
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


    private void onSolution(Solver solver, MPSParser parser) {
        if (solver.getObjectiveManager().isOptimization()) {
            if (level.is(Level.RESANA))
                solver.log().printf(java.util.Locale.US, "o %d %.1f\n",
                        solver.getObjectiveManager().getBestSolutionValue().intValue(),
                        solver.getTimeCount());
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
            if (level.is(Level.JSON)) {
                solver.log().printf("\t\t{\"time\":%.1f," +
                                "\"solutions\":%d, \"nodes\":%d, \"failures\":%d, \"restarts\":%d}",
                        solver.getTimeCount(),
                        solver.getSolutionCount(),
                        solver.getNodeCount(),
                        solver.getFailCount(),
                        solver.getRestartCount());
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
            log.printf("s %s", output.toString());
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
            solver.printShortFeatures();
            solver.log().white().printf("%s %n", solver.getMeasures().toOneLineString());
        }
    }
}
