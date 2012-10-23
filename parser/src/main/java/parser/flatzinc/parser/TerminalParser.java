/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package parser.flatzinc.parser;

import org.codehaus.jparsec.*;
import org.codehaus.jparsec.misc.Mapper;


/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
* 
*/

public final class TerminalParser {

    final static String[] OPERATORS =
            {"..", ".", "{", "}", ",", "[", "]", "=", "+", "-", ";", ":", "::", "(", ")"};

    final static String[] KEYWORDS =
            {"bool", "int", "set", "of", "array", "var", "true", "false", "predicate",
                    "constraint", "solve", "satisfy", "minimize", "maximize"};

    /**
     * {@link Terminals} object for lexing and parsing the operators with names specified in
     * {@code ops}, and for lexing and parsing the keywords case sensitively.
     */
    final static Terminals TERMS = Terminals.caseSensitive(OPERATORS, KEYWORDS);

    static final Parser<String> NUMBER = Terminals.IntegerLiteral.PARSER;

    /**
     * Scanner for fzn line comment
     */
    private static final Parser<Void> COMMENT = Scanners.lineComment("%");

    private static final Parser<Void> EOF = Scanners.isChar('\n');

    private static final Parser<Void> IGNORED =
            Parsers.or(COMMENT, Scanners.WHITESPACES, EOF).skipMany();

    static final Parser<String> IDENTIFIER = Terminals.Identifier.PARSER;


    static final Parser<?> TOKENIZER =
            Parsers.or(
                    Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
                    Terminals.CharLiteral.SINGLE_QUOTE_TOKENIZER,
                    Terminals.IntegerLiteral.TOKENIZER,
                    TERMS.tokenizer()
            );

//    /**
//     * Scanner for a line feed character ({@code '\n'}).
//     */
//    private static final Parser<Void> BACKSLASH = Scanners.isChar('\n');

    static final Indentation INDENTATION = new Indentation();

    /**
     * A {@link Parser} that takes as input the tokens returned by {@code TOKENIZER}
     * delimited by {@code FZN_DELIMITER}, and runs {@code this} to parse the tokens.
     * <p/>
     * <p> {@code this} must be a token level parser.
     */
    public static <T> T parse(Parser<T> parser, String source) {
        return parser.from(TOKENIZER, IGNORED).parse(source);
    }


    /**
     * Scans anw skip the {@code name} expression when encountered
     */
    public static Parser<?> term(String name) {
        return Mapper._(TERMS.token(name));
    }

    public static Parser<?> phrase(String phrase) {
        return Mapper._(TERMS.phrase(phrase.split("\\s+")));
    }

}
