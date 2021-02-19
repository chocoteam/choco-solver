/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.*;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
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

/**
 * A regular parser with default and common services
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public abstract class RegParser implements IParser {
    public static boolean PRINT_LOG = true;
    /**
     * Name of the parser
     */
    private final String parser_cmd;

    @Argument(required = true, metaVar = "file", usage = "File to parse.")
    public String instance;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @Option(name = "-pa", aliases = {"--parser"}, usage = "Parser to use.\n" +
            "0: automatic\n " +
            "1: FlatZinc (.fzn)\n" +
            "2: XCSP3 (.xml or .lzma)\n" +
            "3: MPS (.mps)\n" +
            "4: JSON (.json).")
    private int pa = 0;

    @Option(name = "-limit",
            handler = LimitHandler.class,
            usage = "Resolution limits (XXhYYmZZs,Nruns,Msols) where each is optional (no space allowed).")
    protected ParserParameters.LimConf limits = new ParserParameters.LimConf(-1, -1, -1);

    @Option(name = "-stat", aliases = {
            "--print-statistics"}, usage = "Print statistics on each solution (default: false).")
    protected boolean stat = false;

    @Option(name = "-csv", aliases = {
            "--print-csv"}, usage = "Print statistics on exit (default: false).")
    protected boolean csv = false;

    @Option(name = "-f", aliases = {
            "--free-search"}, usage = "Ignore search strategy.")
    protected boolean free = false;

    @Option(name = "-varh", aliases = {"--varHeuristic"},
            depends = {"-f"},
            usage = "Define the variable heuristic to use.")
    public Search.VarH varH = Search.VarH.DEFAULT;

    @Option(name = "-valh", aliases = {"--valHeuristic"},
            depends = {"-f"},
            usage = "Define the value heuristic to use.")
    public Search.ValH valH = Search.ValH.DEFAULT;

    @Option(name = "-restarts",
            handler = RestartHandler.class,
            depends = {"-f"},
            usage = "Define the restart heuristic to use. Expected format: (policy,cutoff,offset)  (no space allowed)")
    public ParserParameters.ResConf restarts =
            new ParserParameters.ResConf(Search.Restarts.LUBY, 500, 5000);

    @Option(name = "-lc",
            aliases = {"--lact-conflict"},
            depends = {"-f"},
            forbids = {"-cos"},
            usage = "Tell the solver to use last-conflict reasoning.")
    protected int lc = 1;

    @Option(name = "-cos",
            depends = {"-f"},
            forbids = {"-lc"},
            usage = "Tell the solver to use conflict ordering search.")
    protected boolean cos = false;

    @Option(name = "-last",
            depends = {"-f"},
            usage = "Tell the solver to use use progress (or phase) saving.")
    protected boolean last = false;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).")
    public boolean all = false;

    @Option(name = "-p", aliases = {
            "--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).")
    protected int nb_cores = 1;

    @Option(name = "-seed", usage = "Set the seed for random number generator. ")
    protected long seed = 0L;

    @Option(name = "-exp", usage = "Plug explanation in (default: false).")
    public boolean exp = false;

    @Option(name = "-dfx", usage = "Force default explanation algorithm.")
    public boolean dftexp = false;

    @Option(name = "-s", aliases = {"--settings"}, usage = "Configuration settings.")
    protected File settingsFile = null;

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
     *
     * @param parser_cmd name of the parser
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

    /**
     * Create the solver
     */
    public abstract void createSolver();

    public final void addListener(ParserListener listener) {
        listeners.add(listener);
    }

    public final void removeListener(ParserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public final boolean setUp(String... args) throws SetUpException {
        listeners.forEach(ParserListener::beforeParsingParameters);
        if(PRINT_LOG)System.out.printf("%s %s\n", getCommentChar(), Arrays.toString(args));
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
        if (nb_cores == 1) {
            if (exp) {
                if(PRINT_LOG)System.out.printf("%s exp is on\n", getCommentChar());
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
            if (free) {
                if(PRINT_LOG)System.out.printf("%s set search to: (%s,%s) + %s\n", getCommentChar(), varH, valH, restarts.pol);
                if(lc > 0 || cos || last){
                    if(PRINT_LOG)System.out.printf("%s add techniques: ", getCommentChar());
                    if(cos){
                        if(PRINT_LOG)System.out.print("-cos ");
                    }else if(lc>0){
                        if(PRINT_LOG)System.out.printf("-lc %d ", lc);
                    }
                    if(last){
                        if(PRINT_LOG)System.out.print("-last");
                    }
                    if(PRINT_LOG)System.out.print("\n");
                }
                IntVar obj = (IntVar) solver.getObjectiveManager().getObjective();
                IntVar[] dvars = Arrays.stream(solver.getMove().getStrategy().getVariables())
                        .map(Variable::asIntVar)
                        .filter(v -> v != obj)
                        .toArray(IntVar[]::new);
                solver.getMove().removeStrategy();
                solver.setMove(new MoveBinaryDFS());
                AbstractStrategy<IntVar> strategy = varH.make(solver, dvars, valH, last);

                if (obj != null) {
                    boolean max = solver.getObjectiveManager().getPolicy() == ResolutionPolicy.MAXIMIZE;
                    solver.setSearch(
                            strategy,
                            max ? Search.minDomUBSearch(obj) : Search.minDomLBSearch((obj))
                    );
                } else {
                    solver.setSearch(strategy);
                }
                if(cos){
                    solver.setSearch(Search.conflictOrderingSearch(solver.getSearch()));
                }else if(lc>0){
                    solver.setSearch(Search.lastConflict(solver.getSearch(), lc));
                }
                restarts.declare(solver);
                //solver.plugMonitor(new BackjumpRestart(solver));
                //solver.showDecisions();
            }
        }
        for (int i = 0; i < nb_cores; i++) {
            if (limits.time > -1) {
                portfolio.getModels().get(i).getSolver().limitTime(limits.time);
            }
            if (limits.sols > -1) {
                portfolio.getModels().get(i).getSolver().limitSolution(limits.sols);
            }
            if (limits.runs > -1) {
                portfolio.getModels().get(i).getSolver().limitRestart(limits.runs);
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
        return limits.time < 0 || rtime < limits.time;
    }
}
