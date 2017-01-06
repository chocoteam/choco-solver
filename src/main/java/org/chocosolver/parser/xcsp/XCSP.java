/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
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
