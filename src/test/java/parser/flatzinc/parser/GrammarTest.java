/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package parser.flatzinc.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import parser.flatzinc.layout.FZNLayout;
import parser.flatzinc.Flatzinc4Lexer;
import parser.flatzinc.Flatzinc4Parser;
import parser.flatzinc.ast.Datas;
import solver.Solver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class GrammarTest {

    public Flatzinc4Parser parser(String st, Solver aSolver, Datas aDatas) throws IOException {
        InputStream in = new ByteArrayInputStream(st.getBytes());
        CharStream input = new UnbufferedCharStream(in);
        // Create an ExprLexer that feeds from that stream
        Flatzinc4Lexer lexer = new Flatzinc4Lexer(input);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        // Create a stream of tokens fed by the lexer
        TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lexer);
        // Create a parser that feeds off the token stream
        Flatzinc4Parser parser = new Flatzinc4Parser(tokens);
        parser.datas = aDatas;
        parser.mSolver = aSolver;
        FZNLayout fl = new FZNLayout("", parser.datas.goals());
        parser.datas.setmLayout(fl);
//        fl.makeup();
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL); // try with simpler/faster SLL(*)
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        return parser;
    }

    public Flatzinc4Parser parser(String st) throws IOException {
        return parser(st, new Solver(), new Datas());
    }


}
