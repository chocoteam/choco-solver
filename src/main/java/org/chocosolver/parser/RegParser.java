/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import static org.chocosolver.solver.search.strategy.Search.lastConflict;

import gnu.trove.set.hash.THashSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.chocosolver.pf4cs.SetUpException;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.clauses.ClauseStore;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.TimeUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * A regular parser with default and common services
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public abstract class RegParser implements IParser {

    /**
     * Name of the parser
     */
    private final String parser_cmd;

    @Argument(required = true, metaVar = "file", usage = "File to parse.")
    public String instance;

    @SuppressWarnings("unused")
    @Option(name = "-pa", aliases = {"--parser"}, usage = "Parser to use (" +
            "0: automatic -- based on file name extension (compression is allowed), " +
            "1: FlatZinc (.fzn)," +
            "2: XCSP3 (.xml)," +
            "3: MPS (.mps)," +
            "4: JSON (.json).")
    private int pa = 0;

    @Option(name = "-tl", aliases = {"--time-limit"}, metaVar = "TL", usage = "Time limit.")
    protected String tl = "-1";

    @Option(name = "-stat", aliases = {"--print-statistics"}, usage = "Print statistics on each solution (default: false).")
    protected boolean stat = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ")
    protected boolean free = false;

    @Option(name = "-exp", usage ="Plug explanation in (default: false).")
    public boolean exp = false;

    @Option(name = "-dfx", usage ="Force default explanation algorithm.")
    public boolean dftexp = false;

    @Option(name = "-oes", usage ="Override default explanations for sum constraints.")
    public boolean sumdft = false;

    @Option(name = "-sumglb", usage ="Learn permanent nogood when sum fails.")
    public boolean sumglb = false;

    @Option(name = "-bb", usage ="Set the search strategy to a black-box one.")
    public int bbox = 0;

    @Option(name = "-splitsum", usage ="Split sum composed of more than N elements (N = 1000 by default).")
    public int sum = 1000;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).")
    public boolean all = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).")
    protected int nb_cores = 1;

    @Option(name = "-s", aliases = {"--settings"}, usage = "Configuration settings.")
    protected File settingsFile = null;

    /**
     * Default time limit, as long, in ms
     */
    protected long tl_ = -1;
    /**
     * List of listeners
     */
    protected List<ParserListener> listeners = new LinkedList<>();
    /**
     * Default settings to apply
     */
    protected Settings defaultSettings;

    /**
     * The resolution portfolio
     */
    protected ParallelPortfolio portfolio = new ParallelPortfolio();

    /**
     * Indicates that the resolution stops on user instruction
     */
    protected boolean userinterruption = true;
    /**
     * Action to do on user interruption
     */
    protected final Thread statOnKill;

    /**
     * Execution time
     */
    long time;

    /**
     * Create a default regular parser
     * @param parser_cmd name of the parser
     *
     */
    protected RegParser(String parser_cmd) {
        this.time = System.currentTimeMillis();
        this.parser_cmd = parser_cmd;
        statOnKill = actionOnKill();
        Runtime.getRuntime().addShutdownHook(statOnKill);
    }

    public abstract char getCommentChar();

    public abstract Settings createDefaultSettings();

    public final Settings getSettings(){
        return defaultSettings;
    }

    @Override
    public final void addListener(ParserListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeListener(ParserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public final void setUp(String... args) throws SetUpException {
        listeners.forEach(ParserListener::beforeParsingParameters);
        System.out.printf("%s %s\n", getCommentChar(), Arrays.toString(args));
        CmdLineParser cmdparser = new CmdLineParser(this);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(parser_cmd + " [options...] file");
            cmdparser.printUsage(System.err);
            System.err.println();
            return;
        }
        cmdparser.getArguments();
        tl_ = TimeUtils.convertInMilliseconds(tl);
        listeners.forEach(ParserListener::afterParsingParameters);
        defaultSettings = createDefaultSettings();
        if(settingsFile != null){
            try {
                defaultSettings.load(new FileInputStream(settingsFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    private static void makeComplementarySearch(Model m) {
        Solver solver = m.getSolver();
        if(solver.getSearch() != null) {
            IntVar[] ovars = new IntVar[m.getNbVars()];
            THashSet<Variable> dvars = new THashSet<>();
            dvars.addAll(Arrays.asList(solver.getSearch().getVariables()));
            int k = 0;
            for (IntVar iv:m.retrieveIntVars(true)) {
                if (!dvars.contains(iv)) {
                    ovars[k++] = iv;
                }
            }
            // do not enumerate on the complementary search (greedy assignment)
            if(k>0) {
                solver.setSearch(solver.getSearch(), Search.lastConflict(Search.domOverWDegSearch(Arrays.copyOf(ovars, k))));
            }
        }
    }

    @Override
    public final void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);
        Solver solver = portfolio.getModels().get(0).getSolver();
        if(nb_cores == 1 && exp){
            solver.setLearningSignedClauses();
            // THEN PARAMETERS
            ExplanationForSignedClause.DEFAULT_X = dftexp;
            ExplanationForSignedClause.PROOF = ExplanationForSignedClause.FINE_PROOF = false;
            ClauseStore.PRINT_CLAUSE = false;
            ClauseStore.ASSERT_UNIT_PROP = true; // todo : attention aux clauses globales
            ExplanationForSignedClause.ASSERT_NO_LEFT_BRANCH = false;
//            LearnSignedClauses.LIMIT = 100;
            ClauseStore.INTERVAL_TREE = true;
            if(solver.hasObjective()) {
                solver.setRestartOnSolutions();
            }
        }
//        solver.limitSolution(6);
//        solver.limitFail(71);
//        solver.showDecisions(()->"");
        /*try {
            getModel().getObjective().asIntVar().instantiateTo(162, Null);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }*/
//        solver.showDashboard();
        if(bbox>0) {
            solver.getMove().removeStrategy();
            solver.setMove(new MoveBinaryDFS());
            switch (bbox) {
                case 1:
                    solver.setSearch(Search.domOverWDegSearch(getModel().retrieveIntVars(true)));
                    break;
                case 2:
                    solver.setSearch(new DomOverWDeg(getModel().retrieveIntVars(true), 0, new IntDomainBest()));
                    break;
                case 3:
                    solver.setSearch(Search.activityBasedSearch(getModel().retrieveIntVars(true)));
                    break;
                case 4:
                    ImpactBased ibs = new ImpactBased(getModel().retrieveIntVars(true), 2, 1024, 2048, 0, false);
                    solver.setSearch(ibs);
            }
            solver.setNoGoodRecordingFromRestarts();
            solver.setNoGoodRecordingFromSolutions(getModel().retrieveIntVars(true));
            solver.setLubyRestart(500, new FailCounter(getModel(), 500), 500);
            solver.setSearch(lastConflict(solver.getSearch()));

        }else if(nb_cores == 1 && free){ // add last conflict
            solver.getMove().removeStrategy();
            solver.setMove(new MoveBinaryDFS());
            solver.setSearch(Search.defaultSearch(solver.getModel()));
            solver.setNoGoodRecordingFromRestarts();
            solver.setNoGoodRecordingFromSolutions(getModel().retrieveIntVars(true));
            solver.setLubyRestart(500, new FailCounter(getModel(), 0), 500);
        }
        for (int i = 0; i < nb_cores; i++) {
            if (tl_ > -1)portfolio.getModels().get(i).getSolver().limitTime(tl);
            makeComplementarySearch(portfolio.getModels().get(i));
        }
        solver.showDecisions();
        listeners.forEach(ParserListener::afterConfiguringSearch);
    }

    @Override
    public final Model getModel() {
        Model m = portfolio.getBestModel();
        if (m == null) {
            m = portfolio.getModels().get(0);
        }
        return m;
    }

    public final int bestModelID() {
        Model best = getModel();
        for (int i = 0; i < nb_cores; i++) {
            if (best == portfolio.getModels().get(i)) {
                return i;
            }
        }
        return -1;
    }

    protected boolean runInTime(){
        long rtime = (System.currentTimeMillis() - time) ;
        return tl_ < 0 || rtime < tl_;
    }
}
