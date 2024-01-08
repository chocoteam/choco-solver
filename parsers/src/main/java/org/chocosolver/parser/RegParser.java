/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.parser.handlers.LimitHandler;
import org.chocosolver.parser.handlers.RestartHandler;
import org.chocosolver.parser.handlers.ValSelHandler;
import org.chocosolver.parser.handlers.VarSelHandler;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

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

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @Option(name = "-pa", aliases = {"--parser"}, usage = "Parser to use.\n" +
            "0: automatic\n " +
            "1: FlatZinc (.fzn)\n" +
            "2: XCSP3 (.xml or .lzma)\n" +
            "3: DIMACS (.cnf),\n" +
            "4: MPS (.mps)")
    private int pa = 0;

    @Option(name = "-ansi", usage = "Enable ANSI colour codes (default: false).")
    protected boolean ansi = false;

    @Option(name = "-lvl",
            aliases = "--log-level",
            usage = "Define log level."
    )
    protected Level level = Level.COMPET;

    @Option(name = "-log",
            aliases = "--log-file-path",
            usage = "Define the log file path."
    )
    protected String logFilePath = null;

    @Option(name = "-limit",
            handler = LimitHandler.class,
            usage = "Resolution limits [XXhYYmZZs,Nruns,Msols] where each is optional (no space allowed).")
    protected SearchParams.LimConf limits = new SearchParams.LimConf(-1, -1, -1);

    @Option(name = "-csv", aliases = {
            "--print-csv"}, usage = "Print statistics on exit (default: false).")
    protected boolean csv = false;

    @Option(name = "-f", aliases = {
            "--free-search"}, usage = "Ignore search strategy.")
    protected boolean free = false;

    @Option(name = "-varh", aliases = {"--varHeuristic"},
            depends = {"-f"},
            forbids = {"-varsel"},
            usage = "Define the variable heuristic to use.")
    public SearchParams.VariableSelection varH = SearchParams.VariableSelection.DOMWDEG_CACD;

    @Option(name = "-flush",
            forbids = {"-varsel"},
            usage = "Autoflush weights on black-box strategies (default: 32).")
    protected int flushRate = 32;

    @Option(name = "-varsel",
            handler = VarSelHandler.class,
            depends = {"-f"},
            forbids = {"-varh", "-tie", "-flush"},
            usage = "Define the variable selector to use. Expected format: [varsel,tie,flush] as [String,String,int] -- no space allowed.")
    public SearchParams.VarSelConf varsel;

    @Option(name = "-valh", aliases = {"--valHeuristic"},
            depends = {"-f"},
            forbids = {"-valsel"},
            usage = "Define the value heuristic to use.")
    public SearchParams.ValueSelection valH = SearchParams.ValueSelection.MIN;

    @Option(name = "-best",
            depends = {"-f"},
            forbids = {"-valsel"},
            usage = "Tell use BIVS as a meta value selector.")
    protected boolean best = false;

    @Option(name = "-bestRate",
            depends = {"-f"},
            forbids = {"-valsel"},
            usage = "BIVS rate call.")
    protected int bestRate = 16;


    @Option(name = "-last",
            depends = {"-f"},
            forbids = {"-valsel"},
            usage = "Tell the solver to use progress (or phase) saving.")
    protected boolean last = false;

    @Option(name = "-valsel",
            handler = ValSelHandler.class,
            depends = {"-f"},
            forbids = {"-valh", "-best", "-bestRate", "-last"},
            usage = "Define the variable selector to use. Expected format: [valsel,best,bestRate,last] " +
                    "as [String,boolean,int,boolean]  -- no space allowed.")
    public SearchParams.ValSelConf valsel;

    @Option(name = "-restarts",
            handler = RestartHandler.class,
            depends = {"-f"},
            usage = "Define the restart heuristic to use. Expected format: [policy,cutoff,geo?,offset] " +
                    "as [String,int,double?,int]  -- no space allowed.")
    public SearchParams.ResConf restarts =
            new SearchParams.ResConf(SearchParams.Restart.GEOMETRIC, 10, 1.05, 50_000, true);

    @Option(name = "-lc",
            aliases = {"--lact-conflict"},
            depends = {"-f"},
            forbids = {"-cos"},
            usage = "Tell the solver to use last-conflict reasoning.")
    protected int lc = 0;
    @Option(name = "-cos",
            depends = {"-f"},
            forbids = {"-lc"},
            usage = "Tell the solver to use conflict ordering search.")
    protected boolean cos = false;

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

    protected long creationTime;
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

    public void createSettings() {
        defaultSettings = Settings.prod();
    }

    public final Settings getSettings() {
        return defaultSettings;
    }

    /**
     * Create the solver
     */
    public void createSolver() {
        creationTime = -System.nanoTime();
        assert nb_cores > 0;
        if (level.isLoggable(Level.INFO)) {
            if (nb_cores > 1) {
                System.out.printf("%s solvers in parallel\n", nb_cores);
            }
        }
    }

    public void freesearch(Solver solver) {
        BlackBoxConfigurator bb;
        if (solver.getObjectiveManager().isOptimization()) {
            bb = BlackBoxConfigurator.forCOP();
        } else {
            bb = BlackBoxConfigurator.forCSP();
        }
        bb.make(solver.getModel());
    }

    @Override
    public final boolean setUp(String... args) throws SetUpException {
        CmdLineParser cmdparser = new CmdLineParser(this);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println(parser_cmd + " [options...] file");
            cmdparser.printUsage(System.err);
            return false;
        }
        if (level.isLoggable(Level.INFO)) {
            System.out.printf("%s\n", Arrays.toString(args));
        }
        if (varsel == null) {
            varsel = new SearchParams.VarSelConf(varH, flushRate);
        }
        if (valsel == null) {
            valsel = new SearchParams.ValSelConf(valH, best, bestRate, last);
        }
        createSettings();
        Runtime.getRuntime().addShutdownHook(statOnKill);
        return true;
    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    protected void makeComplementarySearch(Model m, int i) {
        Solver solver = m.getSolver();
        if (solver.getSearch() != null) {
            THashSet<Variable> dvars = new THashSet<>();
            dvars.addAll(Arrays.asList(solver.getSearch().getVariables()));
            IntVar[] ivars = m.streamVars()
                    .filter(VariableUtils::isInt)
                    .filter(v -> !VariableUtils.isConstant(v))
                    .filter(v -> !dvars.contains(v))
                    .map(Variable::asIntVar)
                    .sorted(Comparator.comparingInt(IntVar::getDomainSize))
                    .toArray(IntVar[]::new);
            // do not enumerate on the complementary search (greedy assignment)
            if (ivars.length > 0) {
                solver.setSearch(solver.getSearch(),
                        Search.lastConflict(Search.inputOrderLBSearch(ivars)));
            }
        }
    }

    @Override
    public void configureSearch() {
        Solver solver = portfolio.getModels().get(0).getSolver();
        if (level.is(Level.VERBOSE)) {
            solver.verboseSolving(1000);
        }
        if (nb_cores == 1) {
            if (exp) {
                if (level.isLoggable(Level.INFO)) {
                    solver.log().white().println("exp is on");
                }
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
                freesearch(solver);
            }
        }
        for (int i = 0; i < nb_cores; i++) {
            if (limits.getTime() > -1) {
                portfolio.getModels().get(i).getSolver().limitTime(limits.getTime());
            }
            if (limits.getSols() > -1) {
                portfolio.getModels().get(i).getSolver().limitSolution(limits.getSols());
            }
            if (limits.getRuns() > -1) {
                portfolio.getModels().get(i).getSolver().limitRestart(limits.getRuns());
            }
            makeComplementarySearch(portfolio.getModels().get(i), i);
        }
    }

    @Override
    public final void solve() {
        getModel().getSolver().getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
        if (level.isLoggable(Level.INFO)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            getModel().getSolver().log().white().printf("Problem solving starts at %s\n", dtf.format(now));
        }
        if (portfolio.getModels().size() == 1) {
            singleThread();
        } else {
            manyThread();
        }
    }

    protected abstract void singleThread();

    protected abstract void manyThread();


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
        return limits.getTime() < 0 || rtime < limits.getTime();
    }
}
