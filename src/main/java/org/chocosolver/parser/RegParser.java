/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.parser;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.TimeUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.chocosolver.solver.search.strategy.Search.*;

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

    @Option(name = "-tl", aliases = {"--time-limit"}, metaVar = "TL", usage = "Time limit.", required = false)
    protected String tl = "-1";

    @Option(name = "-stat", aliases = {"--print-statistics"}, usage = "Print statistics on each solution (default: false).", required = false)
    protected boolean stat = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ", required = false)
    protected boolean free = false;

    @Option(name = "-cos", usage = "set to true to use free + COS, false to use free+LC", required = false)
    protected boolean cos = false;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).", required = false)
    protected boolean all = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search (default: 1).", required = false)
    protected int nb_cores = 1;
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
     * Create a default regular parser
     * @param parser_cmd name of the parser
     *
     */
    protected RegParser(String parser_cmd) {
        this.parser_cmd = parser_cmd;
        statOnKill = actionOnKill();
        Runtime.getRuntime().addShutdownHook(statOnKill);
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
    public final void parseParameters(String[] args) {
        listeners.forEach(ParserListener::beforeParsingParameters);
        System.out.printf("%% %s\n", Arrays.toString(args));
        CmdLineParser cmdparser = new CmdLineParser(this);
        cmdparser.setUsageWidth(160);
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
    }

    @Override
    public final void defineSettings(Settings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param m a Model
     */
    protected static void makeComplementarySearch(Model m) {
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
                solver.setSearch(solver.getSearch(), greedySearch(inputOrderLBSearch(Arrays.copyOf(ovars, k))));
            }
        }
    }

    @Override
    public final void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);
        if(nb_cores == 1 && free){ // add last conflict
            Solver solver = portfolio.getModels().get(0).getSolver();
            if(cos) {
                solver.setSearch(Search.conflictOrderingSearch(solver.getSearch()));
            }else {
                solver.setSearch(lastConflict(solver.getSearch()));
            }
        }
        if (tl_ > -1) {
            for (int i = 0; i < nb_cores; i++) {
                portfolio.getModels().get(i).getSolver().limitTime(tl);
                makeComplementarySearch(portfolio.getModels().get(i));
            }
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
}
