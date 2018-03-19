/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.kohsuke.args4j.Argument;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * <p>
 * Project: choco-parsers.
 * @author Charles Prud'homme
 * @since 27/09/2017.
 */
public class JSONParser extends RegParser{

    @Argument(required = true, metaVar = "file", usage = "JSON file to parse.")
    public String instance;

    /**
     * Create a default JSON parser
     */
    protected JSONParser() {
        super("ChocoJSON");
    }

    @Override
    public char getCommentChar() {
        return '#';
    }

    @Override
    public void defineSettings(Settings defaultSettings) {
        throw new UnsupportedOperationException("Cannot override Settings for JSON");
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        assert nb_cores > 0;
        if (nb_cores > 1) {
            System.out.printf("%s " + nb_cores + " solvers in parallel\n", getCommentChar());
        } else {
            System.out.printf("%s simple solver\n", getCommentChar());
        }
        // nothing is really done here
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                System.out.printf("%s Unexpected resolution interruption!", getCommentChar());
            }
        });
    }

    @Override
    public void buildModel() {
        listeners.forEach(ParserListener::beforeParsingFile);
        String iname = instance == null?"": Paths.get(instance).getFileName().toString();
        for (int i = 0; i < nb_cores; i++) {
            Model model = JSON.readInstance(new File(instance));
            model.setName(iname + "_" + (i + 1));
            portfolio.addModel(model);
            model.getSolver().showSolutions();
        }
        listeners.forEach(ParserListener::afterParsingFile);
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

    private void singleThread(){
        Model model = portfolio.getModels().get(0);
        boolean enumerate = model.getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        Solver solver = model.getSolver();
        if (stat) {
            solver.getOut().print("c ");
            solver.printShortFeatures();
        }
        if (enumerate) {
            while (solver.solve()) {
                // nothing to do
            }
        } else {
            if (solver.solve()) {
                // nothing to do
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        finalOutPut(solver);
    }

    private void manyThread(){
        boolean enumerate = portfolio.getModels().get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (portfolio.solve()) {
                // nothing to do
            }
        } else {
            if (portfolio.solve()) {
                // nothing to do
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
        finalOutPut(getModel().getSolver());
    }

    private void finalOutPut(Solver solver) {
        solver.printShortStatistics();
    }

}
