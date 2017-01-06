/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017-01-06T09:54:20Z, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.kohsuke.args4j.Argument;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

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

    // Contains mapping with variables and output prints
    public Datas[] datas;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Flatzinc() {
        this(false, false, 1, -1);
    }

    public Flatzinc(boolean all, boolean free, int nb_cores, long tl) {
        super("ChocoFZN");
        this.all = all;
        this.free = free;
        this.nb_cores = nb_cores;
        this.tl_ = tl;
        this.defaultSettings = new FznSettings();
    }

    @Override
    public Thread actionOnKill() {
        return new Thread() {
            public void run() {
                if (userinterruption) {
                    datas[bestModelID()].doFinalOutPut(userinterruption);
                    System.out.printf("%% Unexpected resolution interruption!");
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        assert nb_cores > 0;
        if (nb_cores > 1) {
            System.out.printf("%% " + nb_cores + " solvers in parallel\n");
        } else {
            System.out.printf("%% simple solver\n");
        }
        datas = new Datas[nb_cores];
        String iname = Paths.get(instance).getFileName().toString();
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1));
            threadModel.set(defaultSettings);
            portfolio.addModel(threadModel);
            datas[i] = new Datas(threadModel, all, stat);
        }
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws Exception {
        listeners.forEach(ParserListener::beforeParsingFile);
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
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
    }

    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);
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
        datas[bestModelID()].doFinalOutPut(userinterruption);
        listeners.forEach(ParserListener::afterSolving);
    }

    private int bestModelID() {
        Model best = getModel();
        for (int i = 0; i < nb_cores; i++) {
            if (best == portfolio.getModels().get(i)) {
                return i;
            }
        }
        return -1;
    }
}
