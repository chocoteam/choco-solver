/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.dimacs;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.mps.MPSSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.kohsuke.args4j.Option;

import java.nio.file.Paths;
import java.util.List;

/**
 * @author Charles Prud'homme
 * @since 04/03/2021
 */
public class DIMACS extends RegParser {

    // Contains mapping with variables and output prints
    public DIMACSParser[] parsers;

    @Option(name = "-cp", usage = "Pure CP approach (does not rely on the underlying SAT solver).")
    private boolean cp = false;

    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public DIMACS() {
        super("ChocoDimacs");
        this.defaultSettings = new MPSSettings();
    }

    @Override
    public char getCommentChar() {
        return 'c';
    }

    @Override
    public Settings createDefaultSettings() {
        return new MPSSettings().setEnableSAT(!cp);
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
        parsers = new DIMACSParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1), defaultSettings);
            portfolio.addModel(threadModel);
            parsers[i] = new DIMACSParser();
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
                System.out.print("s UNSUPPORTED\n");
                System.out.printf("%c %s\n", getCommentChar(), e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
        listeners.forEach(ParserListener::afterParsingFile);
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


    private void onSolution(Solver solver, DIMACSParser parser) {
        output.setLength(0);
        output.append(parser.printSolution());
        if (stat) {
            solver.getOut().printf("%c %s \n", getCommentChar(), solver.getMeasures().toOneLineString());
        }
    }

    private void finalOutPut(Solver solver) {
        boolean complete = !userinterruption && runInTime();
        if (solver.getSolutionCount() > 0) {
            output.insert(0, "s SATISFIABLE\n");
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
