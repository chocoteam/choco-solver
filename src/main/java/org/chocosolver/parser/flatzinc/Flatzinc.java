/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package org.chocosolver.parser.flatzinc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.ParserListener;
import org.chocosolver.parser.RegParser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.layout.SolutionPrinter;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A Flatzinc to Choco parser.
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public class Flatzinc extends RegParser {

    @Argument(required = true, metaVar = "file", usage = "Flatzinc file to parse.")
    public String instance;

    @Option(name = "-a", aliases = {"--all"}, usage = "Search for all solutions (default: false).", required = false)
    protected boolean all = false;

    @Option(name = "-f", aliases = {"--free-search"}, usage = "Ignore search strategy (default: false). ", required = false)
    protected boolean free = false;

    @Option(name = "-ps", required = false, handler = StringArrayOptionHandler.class)
    protected String[] ps = new String[]{"0", "1", "3", "5"};

    // Datas
    public Datas datas;
    public SolutionPrinter sprinter;

    // A unique solver
    public Model mModel;

    public Flatzinc() {
        this(false,false,-1);
    }

    public Flatzinc(boolean all, boolean free, long tl) {
        super("ChocoFZN");
        this.all = all;
        this.free = free;
        this.tl_ = tl;
        this.defaultSettings = new FznSettings();
    }

    @Override
    public void createSolver() {
        listeners.forEach(ParserListener::beforeSolverCreation);
        System.out.printf("%% simple solver\n");
        mModel = new Model(instance);
        mModel.set(defaultSettings);
        datas = new Datas();
        sprinter = new SolutionPrinter(mModel,all,stat);
        datas.setSolPrint(sprinter);
        listeners.forEach(ParserListener::afterSolverCreation);
    }

    @Override
    public void parseInputFile() throws FileNotFoundException {
        listeners.forEach(ParserListener::beforeParsingFile);
        parse(mModel, new FileInputStream(new File(instance)));
        sprinter.immutable();
        listeners.forEach(ParserListener::afterParsingFile);
    }

    public void parse(Model target, InputStream is) {
        CharStream input = new UnbufferedCharStream(is);
        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        parser.flatzinc_model(target, datas, all, free);
        // make complementary search
        makeComplementarySearch(target);
    }

    @Override
    public void configureSearch() {
        listeners.forEach(ParserListener::beforeConfiguringSearch);

        if (tl_ > -1) {
            mModel.getSolver().limitTime(tl);
        }

        if (free) {
            mModel.getSolver().set(SearchStrategyFactory.lastConflict(getModel().getSolver().getStrategy()));
        }

//        expl.plugin(mSolver, false, false);
        listeners.forEach(ParserListener::afterConfiguringSearch);
    }

    @Override
    public void solve() {
        listeners.forEach(ParserListener::beforeSolving);
        boolean enumerate = mModel.getResolutionPolicy()!=ResolutionPolicy.SATISFACTION || all;
        if (enumerate) {
            while (mModel.getSolver().solve()){
                sprinter.onSolution();
            }
        }else{
            if(mModel.getSolver().solve()){
                sprinter.onSolution();
            }
        }
        sprinter.finalOutPut();
        listeners.forEach(ParserListener::afterSolving);
    }

    @Override
    public Model getModel() {
        return mModel;
    }
}
