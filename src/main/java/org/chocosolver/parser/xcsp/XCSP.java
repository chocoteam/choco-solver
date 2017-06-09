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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.xcsp.checker.SolutionChecker;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public class XCSP extends RegParser {

    // Contains mapping with variables and output prints
    public XCSPParser[] parsers;

    @Argument(required = true, metaVar = "file", usage = "XCSP file to parse.")
    public String instance;

    @Option(name = "-cs", usage = "set to true to check solution with org.xcsp.checker.SolutionChecker")
    private boolean cs = false;

    /**
     * Needed to print the last solution found
     */
    private final StringBuilder output = new StringBuilder();

    public XCSP() {
        super("ChocoXCSP");
        this.defaultSettings = new FznSettings(); // todo: rename or create the right one
    }

    @Override
    public char getCommentChar() {
        return 'c';
    }

    @Override
    public Thread actionOnKill() {
        return new Thread(() -> {
            if (userinterruption) {
                finalOutPut(getModel().getSolver());
                System.out.printf("c Unexpected resolution interruption!");
            }
        });
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        assert nb_cores > 0;
        if (nb_cores > 1) {
            System.out.printf("c %s solvers in parallel\n", nb_cores );
        } else {
            System.out.printf("c simple solver\n");
        }
        String iname = Paths.get(instance).getFileName().toString();
        parsers = new XCSPParser[nb_cores];
        for (int i = 0; i < nb_cores; i++) {
            Model threadModel = new Model(iname + "_" + (i + 1));
            threadModel.set(defaultSettings);
            portfolio.addModel(threadModel);
            parsers[i] = new XCSPParser();
        }
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void buildModel() {
        listeners.forEach(ParserListener::beforeParsingFile);
        List<Model> models = portfolio.getModels();
        for (int i = 0; i < models.size(); i++) {
            try {
                parse(models.get(i), parsers[i]);
            } catch (Exception e) {
                System.out.printf("s UNSUPPORTED\n");
                System.out.printf("c %s\n", e.getMessage());
                userinterruption = false;
                e.printStackTrace();
                throw new RuntimeException("UNSUPPORTED");
            }
        }
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Model target, XCSPParser parser) throws Exception {
        parser.model(target, instance);
        IntVar[] vars = parser.mvars.values().toArray(new IntVar[parser.mvars.size()]);
        Arrays.sort(vars, Comparator.comparingInt(IntVar::getId));
        Solver solver = target.getSolver();
        solver.setSearch(/*Search.lastConflict*/(Search.intVarSearch(vars)));
        solver.setNoGoodRecordingFromRestarts();
        solver.setLubyRestart(100, new FailCounter(target, 0), 1000);
//        Files.move(Paths.get(instance),
//                Paths.get("/Users/cprudhom/Sources/XCSP/ok/"+ Paths.get(instance).getFileName().toString()),
//                StandardCopyOption.REPLACE_EXISTING);
//        System.exit(-1);
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

    private void manyThread(){
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


    private void onSolution(Solver solver, XCSPParser parser){
        if (solver.getObjectiveManager().isOptimization()){
            solver.getOut().printf("o %d \n", solver.getObjectiveManager().getBestSolutionValue().intValue());
        }
        output.setLength(0);
        output.append(parser.printSolution());
        if (stat) {
            solver.getOut().printf("c %s \n", solver.getMeasures().toOneLineString());
        }
        if(cs) {
            try {
                new SolutionChecker(instance, new ByteArrayInputStream(output.toString().getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("wrong solution found twice");
            }
        }
    }

    private void finalOutPut(Solver solver) {
        boolean complete = solver.getSearchState() == SearchState.TERMINATED;
        if (solver.getSolutionCount() > 0) {
            if (solver.getObjectiveManager().isOptimization() && complete) {
                output.insert(0, "s OPTIMUM FOUND\n");
            }else{
                output.insert(0, "s SATISFIABLE\n");
            }
        } else if (complete) {
            output.insert(0, "s UNSATISFIABLE\n");
        } else {
            output.insert(0, "s UNKNOWN\n");
        }
        solver.getOut().printf("%s", output);
        if (stat) {
            solver.getOut().printf("c %s \n", solver.getMeasures().toOneLineString());
        }
        if(cs) {
            try {
                new SolutionChecker(instance, new ByteArrayInputStream(output.toString().getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
