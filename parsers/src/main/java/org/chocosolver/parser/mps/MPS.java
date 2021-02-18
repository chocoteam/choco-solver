/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.mps;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.nio.file.Paths;
import java.util.List;

/**
 * Created by cprudhom on 01/09/15. Project: choco-parsers.
 */
public class MPS extends RegParser {

    // Contains mapping with variables and output prints
    public MPSParser[] parsers;

    @Option(name = "-max", usage = "define to maximize (default: to minimize).")
    private boolean maximize = false;

    @Option(name = "-prec", usage = "set to the precision (default: 1.0E-4D).")
    private double precision = 1.0E-4D;

    @Option(name = "-ibex", usage = "Use Ibex for non-full integer equations (default: false).")
    private boolean ibex = false;

    @Option(name = "-ninf", usage = "define negative infinity (default: " + IntVar.MIN_INT_BOUND + ").")
    private double ninf = IntVar.MIN_INT_BOUND;

    @Option(name = "-pinf", usage = "define positive infinity (default: " + IntVar.MAX_INT_BOUND + ").")
    private double pinf = IntVar.MAX_INT_BOUND;

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
        this.defaultSettings = new MPSSettings();
    }

    @Override
    public char getCommentChar() {
        return 'c';
    }

    @Override
    public Settings createDefaultSettings() {
        return new MPSSettings();
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                finalOutPut(getModel().getSolver());
                System.out.printf("%c Unexpected resolution interruption!", getCommentChar());
            }
        });
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        assert nb_cores > 0;
        if (nb_cores > 1) {
            System.out.printf("%c %s solvers in parallel\n", getCommentChar(), nb_cores);
        } else {
            System.out.printf("%c simple solver\n", getCommentChar());
        }
        String iname = Paths.get(instance).getFileName().toString();
        parsers = new MPSParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            threadModel.setPrecision(precision);
            portfolio.addModel(threadModel);
            parsers[i] = new MPSParser();
        }
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void buildModel() {
        listeners.forEach(ParserListener::beforeParsingFile);
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            try {
                parse(models.get(i), parsers[i], i);
            } catch (Exception e) {
                System.out.printf("s UNSUPPORTED\n");
                System.out.printf("%c %s\n", getCommentChar(), e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Model target, MPSParser parser, int i) throws Exception {
        parser.model(target, instance, maximize, ninf, pinf, ibex, noeq);
        if (i == 0) {
            Solver solver = target.getSolver();
            if (target.getNbRealVar() == 0) {
                target.getSolver().setSearch(
                        Search.intVarSearch(new FirstFail(target),
                                /*new org.chocosolver.parser.mps.IntDomainBest()*/
                                new org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest(),
//                                new IntDomainMin(),
                                target.retrieveIntVars(true))
                );
            } else {
                solver.setSearch(Search.defaultSearch(target));
                solver.setLubyRestart(500, new FailCounter(target, 0), 5000);
            }
        }
    }


    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);
        if (portfolio.getModels().size() == 1) {
            singleThread();
        } else {
            manyThread();
        }
        listeners.forEach(ParserListener::afterSolving);
    }

    private void singleThread() {
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
//        solver.showDashboard();
        if (stat) {
            solver.getOut().print("c ");
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

    private void manyThread() {
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
            solver.getOut().printf("o %.12f \n", solver.getObjectiveManager().getBestSolutionValue().doubleValue());
        }
        output.setLength(0);
        output.append(parser.printSolution());
        if (stat) {
            solver.getOut().printf("%c %s \n", getCommentChar(), solver.getMeasures().toOneLineString());
        }
    }

    private void finalOutPut(Solver solver) {
        boolean complete = !userinterruption && runInTime();
        if (solver.getSolutionCount() > 0) {
            if (solver.getObjectiveManager().isOptimization() && complete) {
                output.insert(0, "s OPTIMUM FOUND\n");
            } else {
                output.insert(0, "s SATISFIABLE\n");
            }
        } else if (complete) {
            output.insert(0, "s UNSATISFIABLE\n");
        } else {
            output.insert(0, "s UNKNOWN\n");
        }
        solver.getOut().printf("%s", output);
        if (stat) {
            solver.getOut().print("c ");
            solver.printShortFeatures();
            solver.getOut().printf("%c %s \n", getCommentChar(), solver.getMeasures().toOneLineString());
        }
    }
}
