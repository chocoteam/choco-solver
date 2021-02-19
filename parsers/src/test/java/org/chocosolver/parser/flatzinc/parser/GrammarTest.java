/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.chocosolver.parser.flatzinc.Flatzinc4Lexer;
import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;

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

    public Flatzinc4Parser parser(String st, Model aModel, Datas aDatas) throws IOException {
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
        parser.mModel = aModel;
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL); // try with simpler/faster SLL(*)
        parser.setBuildParseTree(false);
        parser.setTrimParseTree(false);
        return parser;
    }

    public Flatzinc4Parser parser(String st) throws IOException {
        return parser(st, new Model(), new Datas());
    }


}
