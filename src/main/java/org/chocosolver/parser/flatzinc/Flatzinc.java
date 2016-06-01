/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
