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
package org.chocosolver.parser;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.Once;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A regular parser with default and common services
 * Created by cprudhom on 01/09/15.
 * Project: choco-parsers.
 */
public abstract class RegParser implements IParser {


    private final String parser_cmd;

    @Option(name = "-tl", aliases = {"--time-limit"}, metaVar = "TL", usage = "Time limit.", required = false)
    protected String tl = "-1";

    @Option(name = "-stat", aliases = {"--print-statistics"}, usage = "Print statistics on each solution (default: false).", required = false)
    protected boolean stat = false;

    @Option(name = "-e", aliases = {"--explanations"}, usage = "Plug explanations in : CBJ, DBT or NONE (default: NONE).", required = false)
    protected ExplanationFactory expl = ExplanationFactory.NONE;

    // A unique solver
    public Solver mSolver;
    protected List<Solver> solvers;

    protected long tl_ = -1;
    // List of listeners plugged, ease user interactions.
    protected List<ParserListener> listeners = new LinkedList<>();
    protected Settings defaultSettings;

    protected RegParser(String parser_cmd) {
        this.parser_cmd = parser_cmd;
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
        tl_ = SMF.convertInMilliseconds(tl);
        listeners.forEach(ParserListener::afterParsingParameters);
    }


    @Override
    public final void defineSettings(Settings defaultSettings) {
        this.defaultSettings = defaultSettings;
    }


    @Override
    public void configureSearch() {

    }

    /**
     * Create a complementary search on non-decision variables
     *
     * @param solver a solver
     */
    protected static void makeComplementarySearch(Solver solver) {
        //TODO deal with no strategy
        IntVar[] ovars = new IntVar[solver.getNbVars()];
        THashSet<Variable> dvars = new THashSet<>(Arrays.asList(solver.getStrategy().getVariables()));
        int k = 0;
        for (int i = 0; i < solver.getNbVars(); i++) {
            Variable ivar = solver.getVar(i);
            if (!dvars.contains(ivar) && (ivar.getTypeAndKind() & Variable.INT) != 0) {
                ovars[k++] = (IntVar) ivar;
            }
        }
        solver.set(solver.getStrategy(), new Once(Arrays.copyOf(ovars, k), ISF.lexico_var_selector(), ISF.min_value_selector()));
    }

    @Override
    public void solve() {

    }

    @Override
    public Solver getSolver() {
        return null;
    }
}
