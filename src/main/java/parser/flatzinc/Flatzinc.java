/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import parser.IParser;
import parser.ParserListener;
import parser.flatzinc.ast.Datas;
import parser.flatzinc.ast.GoalConf;
import solver.Solver;
import solver.propagation.NoPropagationEngine;
import solver.propagation.hardcoded.TwoBucketPropagationEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A Flatzinc to Choco parser.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public class Flatzinc implements IParser {

    @Argument(required = true, usage = "Flatzinc file path.")
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions.", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy.", required = false)
    protected boolean free = false;

    @Option(name = "-p", aliases = {"--nb-cores"}, usage = "Number of cores available for parallel search", required = false)
    protected int nb_cores = 1; // SEEMS USELESS, BUT NEEDED BY CHOCOFZN

    @Option(name = "-tl", aliases = {"--time-limit"}, usage = "Time limit.", required = false)
    protected long tl = -1;

    @Option(name = "-seed", usage = "Seed for randomness", required = false)
    public long seed = 29091981L;


    // A unique olver
    protected Solver mSolver;
    // Goals
    public GoalConf gc;
    // Datas
    public Datas datas;
    // List of listeners plugged, ease user interactions.
    List<ParserListener> listeners = new LinkedList<>();

    @Override
    public void addListener(ParserListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ParserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void parseParameters(String[] args) {
        listeners.forEach(ParserListener::afterParsingFile);

        CmdLineParser cmdparser = new CmdLineParser(this);
        cmdparser.setUsageWidth(160);
        try {
            cmdparser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("ParseAndSolve [options...] VAL");
            cmdparser.printUsage(System.err);
            System.err.println();
            return;
        }
        cmdparser.getArguments();

        listeners.forEach(ParserListener::afterParsingParameters);
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);

        gc = new GoalConf(free, 0, true, all, seed, true, tl, true);
        mSolver = new Solver();
        datas = new Datas(gc);

        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws FileNotFoundException {
        listeners.forEach(ParserListener::beforeParsingFile);

        InputStream is = new FileInputStream(new File(instance));
        CharStream input = new UnbufferedCharStream(is);
        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        parser.flatzinc_model(mSolver, datas);

        listeners.forEach(ParserListener::afterParsingFile);
    }

    @Override
    public void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);

        if (mSolver.getEngine() == NoPropagationEngine.SINGLETON) {
            mSolver.set(new TwoBucketPropagationEngine(mSolver));
        }

        listeners.forEach(ParserListener::afterConfiguringSearch);
    }

    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);

        mSolver.getSearchLoop().launch((!mSolver.getObjectiveManager().isOptimization()) && !gc.all);

        listeners.forEach(ParserListener::afterSolving);
    }

    @Override
    public Solver getSolver() {
        return mSolver;
    }

}
