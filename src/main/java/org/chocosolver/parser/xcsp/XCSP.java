/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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
package org.chocosolver.parser.xcsp;

import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.kohsuke.args4j.Argument;

import java.nio.file.Paths;
import java.util.List;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSP extends RegParser {

    @Argument(required = true, metaVar = "file", usage = "XCSP file to parse.")
    public String instance;

    public XCSP() {
        super("ChocoXCSP");
        this.defaultSettings = new FznSettings(); // todo: rename or create the right one
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        assert nb_cores > 0;
        if (nb_cores > 1) {
            System.out.printf("%% " + nb_cores + " solvers in parallel\n");
        } else {
            System.out.printf("%% simple solver\n");
        }
        String iname = Paths.get(instance).getFileName().toString();
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1));
            threadModel.set(defaultSettings);
            portfolio.addModel(threadModel);
            if (stat) {
                threadModel.getSolver().plugMonitor(
                        (IMonitorSolution) ()
                                -> System.out.printf("%% %s \n", threadModel.getSolver().toOneLineString()));
            }
        }
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws Exception {
        listeners.forEach(ParserListener::beforeParsingFile);
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            parse(models.get(i));
        }
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Model target) throws Exception {
        new XCSPParser().model(target, instance);
//        Files.move(Paths.get(instance),
//                Paths.get("/Users/cprudhom/Sources/XCSP/ok/"+ Paths.get(instance).getFileName().toString()),
//                StandardCopyOption.REPLACE_EXISTING);
//        System.exit(-1);
    }


    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);
        boolean enumerate = portfolio.getModels().get(0).getResolutionPolicy() != ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (portfolio.solve()) {
//                datas[bestModelID()].onSolution();
            }
        } else {
            if (portfolio.solve()) {
//                datas[bestModelID()].onSolution();
            }
        }
        userinterruption = false;
        Runtime.getRuntime().removeShutdownHook(statOnKill);
//        datas[bestModelID()].doFinalOutPut(userinterruption);
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

    @Override
    public Thread actionOnKill() {
        return new Thread();
    }
}
