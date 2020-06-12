/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.pf4cs.SetUpException;
import org.chocosolver.solver.*;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainLast;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.TimeUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.chocosolver.solver.search.strategy.Search.lastConflict;

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

    @Option(name = "-stat", aliases = {
        "--print-statistics"}, usage = "Print statistics on each solution (default: false).")
    protected boolean stat = false;

    @Option(name = "-f", aliases = {
        "--free-search"}, usage = "Ignore search strategy (default: false). ")
    protected boolean free = false;

    @Option(name = "-exp", usage = "Plug explanation in (default: false).")
    public boolean exp = false;

    @Option(name = "-dfx", usage = "Force default explanation algorithm.")
    public boolean dftexp = false;

    @Option(name = "-oes", usage = "Override default explanations for sum constraints.")
    public boolean sumdft = false;

    @Option(name = "-sumglb", usage = "Learn permanent nogood when sum fails.")
    public boolean sumglb = false;

    @Option(name = "-bb", usage = "Set the search strategy to a black-box one (-1 is the default one).")
    public int bbox = -1;

    @Option(name = "-splitsum", usage = "Split sum composed of more than N elements (N = 1000 by default).")
    public int sum = 1000;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).")
    public boolean all = false;

    @Option(name = "-p", aliases = {
        "--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).")
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
    protected final Thread statOnKill = actionOnKill();

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
    }

    public abstract char getCommentChar();

    public abstract Settings createDefaultSettings();

    public final Settings getSettings() {
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
    public final boolean setUp(String... args) throws SetUpException {
        listeners.forEach(ParserListener::beforeParsingParameters);
        System.out.printf("%s %s\n", getCommentChar(), Arrays.toString(args));
        CmdLineParser cmdparser = new CmdLineParser(this);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(parser_cmd + " [options...] file");
            cmdparser.printUsage(System.err);
            return false;
        }
        cmdparser.getArguments();
        tl_ = TimeUtils.convertInMilliseconds(tl);
        listeners.forEach(ParserListener::afterParsingParameters);
        defaultSettings = createDefaultSettings();
        if (settingsFile != null) {
            try {
                FileInputStream fileInputStream = new FileInputStream(settingsFile);
                defaultSettings.load(fileInputStream);
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Runtime.getRuntime().addShutdownHook(statOnKill);
        return true;
    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    private static void makeComplementarySearch(Model m) {
        Solver solver = m.getSolver();
        if (solver.getSearch() != null) {
            IntVar[] ovars = new IntVar[m.getNbVars()];
            THashSet<Variable> dvars = new THashSet<>();
            dvars.addAll(Arrays.asList(solver.getSearch().getVariables()));
            int k = 0;
            for (IntVar iv : m.retrieveIntVars(true)) {
                if (!dvars.contains(iv)) {
                    ovars[k++] = iv;
                }
            }
            // do not enumerate on the complementary search (greedy assignment)
            if (k > 0) {
                solver.setSearch(solver.getSearch(),
                    Search.lastConflict(Search.domOverWDegSearch(Arrays.copyOf(ovars, k))));
            }
        }
    }

    @Override
    public final void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);
        Solver solver = portfolio.getModels().get(0).getSolver();
        if (nb_cores == 1 && exp) {
            solver.setLearningSignedClauses();
            // THEN PARAMETERS
            XParameters.DEFAULT_X = dftexp;
            XParameters.PROOF = XParameters.FINE_PROOF = false;
            XParameters.PRINT_CLAUSE = false;
            XParameters.ASSERT_UNIT_PROP = true; // todo : attention aux clauses globales
            XParameters.ASSERT_NO_LEFT_BRANCH = false;
            XParameters.INTERVAL_TREE = true;
            if (solver.hasObjective()) {
                solver.setRestartOnSolutions();
            }
        }
        if (bbox > 0) {
            IntVar[] dvars = Arrays.stream(solver.getMove().getStrategy().getVariables())
                .map(Variable::asIntVar)
                .toArray(IntVar[]::new);
            solver.getMove().removeStrategy();
            solver.setMove(new MoveBinaryDFS());
            switch (bbox) {
                case 1:
                    solver.setSearch(Search.domOverWDegSearch(dvars));
                    break;
                case 2:
                    solver.setSearch(new DomOverWDeg(dvars, 0, new IntDomainBest()));
                    break;
                case 3:
                    solver.setSearch(Search.activityBasedSearch(dvars));
                    break;
                case 4:
                    ImpactBased ibs = new ImpactBased(dvars, 2, 1024, 2048, 0, false);
                    solver.setSearch(ibs);
                    break;
                case 5: {
                    IntValueSelector valueSelector;
                    if (getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                        || !(getModel().getObjective() instanceof IntVar)) {
                        valueSelector = new IntDomainMin();
                    } else {
                        valueSelector = new IntDomainBest();
                        Solution lastSolution = new Solution(getModel(), dvars);
                        solver.attach(lastSolution);
                        int[] t = new int[2];
                        valueSelector = new IntDomainLast(lastSolution, valueSelector,
                            (x, v) -> {
                                int c = 0;
                                for (int idx = 0; idx < dvars.length; idx++) {
                                    if (dvars[idx]
                                        .isInstantiatedTo(lastSolution.getIntVal(dvars[idx]))) {
                                        c++;
                                    }
                                }
                                double d = (c * 1. / dvars.length);
                                double r = Math.exp(-t[0]++ / 25);
                                if (solver.getRestartCount() > t[1]) {
                                    t[1] += 150;
                                    t[0] = 0;
                                }
                                return d > r;
                            });
                    }
                    solver.setSearch(new DomOverWDeg(dvars, 0, valueSelector));
                }
                break;
                case 6: {
                    IntValueSelector valueSelector;
                    if (getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                        || !(getModel().getObjective() instanceof IntVar)) {
                        valueSelector = new IntDomainMin();
                    } else {
                        valueSelector = new IntDomainBest();
                    }
                    solver.setSearch(new DomOverWDeg(dvars, 0, valueSelector));
                }
                break;
                case 7: {
                    IntValueSelector valueSelector;
                    if (getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION
                        || !(getModel().getObjective() instanceof IntVar)) {
                        valueSelector = new IntDomainMin();
                    } else {
                        valueSelector = new IntDomainBest();
                        Solution lastSolution = new Solution(getModel(), dvars);
                        solver.attach(lastSolution);
                        valueSelector = new IntDomainLast(lastSolution, valueSelector, null);
                    }
                    solver.setSearch(new DomOverWDeg(dvars, 0, valueSelector));
                }
                break;
            }
            solver.setNoGoodRecordingFromRestarts();
            solver.setNoGoodRecordingFromSolutions(dvars);
            solver.setRestarts(count -> solver.getFailCount() >= count, new LubyCutoffStrategy(500),
                5000);
            solver.setSearch(lastConflict(solver.getSearch()));

        } else if (nb_cores == 1 && free) { // add last conflict
            solver.getMove().removeStrategy();
            solver.setMove(new MoveBinaryDFS());
            solver.setSearch(Search.defaultSearch(solver.getModel()));
            solver.setNoGoodRecordingFromRestarts();
            solver.setNoGoodRecordingFromSolutions(getModel().retrieveIntVars(true));
            solver.setRestarts(count -> solver.getFailCount() >= count, new LubyCutoffStrategy(1),
                5000);
        }
        for (int i = 0; i < nb_cores; i++) {
            if (tl_ > -1) {
                portfolio.getModels().get(i).getSolver().limitTime(tl);
            }
            makeComplementarySearch(portfolio.getModels().get(i));
        }
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

    protected boolean runInTime() {
        long rtime = (System.currentTimeMillis() - time);
        return tl_ < 0 || rtime < tl_;
    }
}
