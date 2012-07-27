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

import gnu.trove.map.hash.THashMap;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.misc.Mapper;
import org.slf4j.LoggerFactory;
import parser.flatzinc.FZNException;
import parser.flatzinc.FZNLayout;
import parser.flatzinc.ast.*;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import solver.Solver;

import java.io.*;
import java.util.Collections;
import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
* 
*/

public final class FZNParser {

    public final Solver solver = new Solver();
    public final FZNLayout layout = new FZNLayout();

    /**
     * Map to find an object with its name.
     */
    public final THashMap<String, Object> map = new THashMap<String, Object>();

    public String instance;

    public FZNParser() {
        PARVAR.setParser(this);
    }

    /**
     * Scanners for "true" or "false" keyword.
     * Create a {@link parser.flatzinc.ast.expression.EBool} object.
     */
    static final Parser<EBool> BOOL_CONST = Parsers.or(
            TerminalParser.term("true").retn(EBool.instanceTrue),
            TerminalParser.term("false").retn(EBool.instanceFalse));

    /**
     * Scanner for int_const.
     * Create a {@link parser.flatzinc.ast.expression.EInt} object.
     */
    static final Parser<EInt> INT_CONST =
            Mapper.curry(EInt.class).sequence(Parsers.or(TerminalParser.term("+"), TerminalParser.term("-")).optional().source(),
                    TerminalParser.NUMBER);

    /**
     * Scanner for bounded set declaration, like 1..6.
     * Create {@link parser.flatzinc.ast.expression.ESetBounds} object.
     */
    static final Parser<ESetBounds> SET_CONST_1 =
            Mapper.curry(ESetBounds.class).sequence(INT_CONST, TerminalParser.term(".."), INT_CONST);

    /**
     * Scanner for listed set declaration, like {1,6,7}.
     * Create {@link parser.flatzinc.ast.expression.ESetList} object.
     */
    static final Parser<ESetList> SET_CONST_2 =
            Mapper.curry(ESetList.class).sequence(TerminalParser.term("{"), INT_CONST.sepBy(TerminalParser.term(",")),
                    TerminalParser.term("}"));
    /**
     * Scanner for sequence like "id[1]".
     */
    final Parser<EIdArray> ID_ARRAY =
            Mapper.curry(EIdArray.class).sequence(Parsers.constant(this.map), TerminalParser.IDENTIFIER, TerminalParser.term("["), INT_CONST, TerminalParser.term("]"));


    /**
     * Scanner for expression surrounded by parenthesis.
     *
     * @param parser Scanner for expression
     * @param <T>    expected type
     * @return {@link Parser<T>}
     */
    static final <T> Parser<T> paren(Parser<T> parser) {
        return parser.between(TerminalParser.term("("), TerminalParser.term(")"));
    }

    /**
     * Scanner for a list of {@link T} separated by comma.
     *
     * @param parser Scanner for expression
     * @param <T>    expected type
     * @return List of {@link T}
     */
    static <T> Parser<List<T>> list(Parser<T> parser) {
        return paren(parser.sepBy(TerminalParser.term(",")));
    }


    final Parser<EIdentifier> IDENTIFIER = Mapper.curry(EIdentifier.class).sequence(Parsers.constant(this.map), TerminalParser.IDENTIFIER);

    static final Parser<EString> STRING_LITERAL = Mapper.curry(EString.class).sequence(Terminals.StringLiteral.PARSER);

    /**
     * Scanner for expression.
     *
     * @return {@link Parser} of {@link Expression}
     */
    final Parser<Expression> expression() {
        Parser.Reference<Expression> ref = Parser.newReference();
        Parser<Expression> lazy = ref.lazy();
        Parser<Expression> parser = Parsers.or(
                // set_const
                Parsers.or(SET_CONST_1, SET_CONST_2),
                // bool_const
                BOOL_CONST,
                // int_const
                INT_CONST,
                // []
                Mapper.curry(EArray.class).sequence(Parsers.constant(Collections.<Expression>emptyList()),
                        TerminalParser.term("["), TerminalParser.term("]")),
                // [expr,...]
                Mapper.curry(EArray.class).sequence(TerminalParser.term("["), lazy.sepBy(TerminalParser.term(",")),
                        TerminalParser.term("]")),
                // annotation
                Mapper.curry(EAnnotation.class).sequence(
                        IDENTIFIER,
                        Parsers.between(TerminalParser.term("("), lazy.sepBy(TerminalParser.term(",")),
                                TerminalParser.term(")"))
                ),
                // identifier[int_const]
                Mapper.curry(EIdArray.class).sequence(Parsers.constant(this.map), TerminalParser.IDENTIFIER,
                        TerminalParser.term("["), INT_CONST, TerminalParser.term("]")),
                // identifier
                IDENTIFIER,
                // "...string constant..."
                STRING_LITERAL
        );
        ref.set(parser);
        return parser;
    }


    /**
     * Scanner for "int" keyword.
     * Create {@link parser.flatzinc.ast.declaration.DInt} object.
     */
    static final Parser<DBool> BOOL = Mapper.curry(DBool.class).sequence(TerminalParser.term("var").succeeds(), TerminalParser.term("bool"));


    /**
     * Scanner for "int" keyword.
     * Create {@link parser.flatzinc.ast.declaration.DInt} object.
     */
    static final Parser<DInt> INT =
            Mapper.curry(DInt.class).sequence(TerminalParser.term("var").succeeds(), TerminalParser.term("int"));

    /**
     * Scanner for bounds of int declaration, like 1..3.
     * Create a {@link parser.flatzinc.ast.declaration.DInt2} object.
     */
    static final Parser<DInt2> INT2 =
            Mapper.curry(DInt2.class).sequence(
                    TerminalParser.term("var").succeeds(), INT_CONST, TerminalParser.term(".."), INT_CONST
            );
    /**
     * Scanner for list of int declaration, like {1, 5, 8}.
     * Create a {@link parser.flatzinc.ast.declaration.DManyInt} object.
     */
    static final Parser<DManyInt> MANY_INT =
            Mapper.curry(DManyInt.class).sequence(
                    TerminalParser.term("var").succeeds(), TerminalParser.term("{"), INT_CONST.sepBy(TerminalParser.term(",")), TerminalParser.term("}")
            );

    /**
     * Scanners for every int-like expression.
     * Create a {@link parser.flatzinc.ast.declaration.Declaration} object
     */
    static final Parser<Declaration> INTS = Parsers.or(INT, INT2, MANY_INT);

    /**
     * Scanner for every primitive-like expression.
     * Create a {@link Declaration} object.
     */
    static final Parser<Declaration> PRIMITIVES = Parsers.or(BOOL, INTS);

    /**
     * Scanner for a set of int, like "set of int", "set of 1..3" or "set of {1,2,3}".
     * Create a {@link DSet} object.
     */
    static final Parser<DSet> SET_OF_INT =
            Mapper.curry(DSet.class).sequence(
                    TerminalParser.term("var").succeeds(), TerminalParser.phrase("set of"),
                    INTS
            );

    static final Parser<Declaration> INDEX_SET = Parsers.or(INT, INT2);

    /**
     * Scanner for array of smth, like "array [int] of bool".
     * Creat a {@link DArray} object
     */
    static final Parser<DArray> ARRAY_OF =
            Mapper.curry(DArray.class).sequence(
                    TerminalParser.phrase("array ["),
                    INDEX_SET.sepBy(TerminalParser.term(",")),
                    TerminalParser.phrase("] of"),
                    Parsers.or(PRIMITIVES, SET_OF_INT)
            );

    /**
     * Scanner for parameter types or variables types.
     * See FZN specifications for more informations.
     * Create {@link Declaration} object
     */
    static final Parser<Declaration> TYPE =
            Parsers.or(PRIMITIVES, SET_OF_INT, ARRAY_OF);

    /**
     * Scanner for multiples annotations.
     * Create a {@link List} of {@link EAnnotation}.
     */
    final Parser<List<Expression>> ANNOTATIONS =
            Parsers.sequence(TerminalParser.term("::"), expression()).many();

    /**
     * Scanner for predicate parameters.
     * Create a {@link PredParam} object.
     */
    static final Parser<PredParam> PRED_PARAM =
            Mapper.curry(PredParam.class).sequence(TYPE, TerminalParser.term(":"), TerminalParser.IDENTIFIER);


    /**
     * Scanner for predicate declaration
     * Create a {@link parser.flatzinc.ast.Predicate} object
     */
    static final Parser<Predicate> PRED_DECL =
            Mapper.curry(Predicate.class).sequence(TerminalParser.term("predicate"), TerminalParser.IDENTIFIER, list(PRED_PARAM), TerminalParser.term(";"));

    /**
     * Mapper for Parameter or PVariable declaration
     */
    private final Map5Extended PARVAR = new Map5Extended<THashMap<String, Object>, Declaration, String, List<EAnnotation>, Expression, ParVar>() {

        @Override
        public ParVar map(THashMap<String, Object> map, Declaration type, String id, List<EAnnotation> annotations,
                          Expression expression) {
            return ParVar.build(map, type, id, annotations, expression, parser);
        }

        @Override
        public String toString() {
            return "parvar sequence";
        }

    };

    /**
     * Scanner for variable declaration.
     * Create {@link ?} object
     */
    @SuppressWarnings({"unchecked"})
    public final Parser<ParVar> PAR_VAR_DECL =
            Parsers.sequence(Parsers.constant(this.map), TYPE.followedBy(TerminalParser.term(":")), TerminalParser.IDENTIFIER, ANNOTATIONS,
                    Parsers.sequence(TerminalParser.term("="), expression()).optional(), PARVAR).followedBy(TerminalParser.term(";"));

    /**
     * Scanner for constraint declaration.
     * Create a {@link parser.flatzinc.ast.PConstraint} object.
     */
    public final Parser<PConstraint> CONSTRAINT =
            Mapper.curry(PConstraint.class).sequence(TerminalParser.term("constraint"), Parsers.constant(this), TerminalParser.IDENTIFIER,
                    list(expression()), ANNOTATIONS, TerminalParser.term(";"));


    /**
     * Scanner for satisfy declaration.
     */
    final Parser<SatisfyGoal> SATISFY =
            Mapper.curry(SatisfyGoal.class).sequence(
                    Parsers.constant(this),
                    TerminalParser.term("solve"),
                    ANNOTATIONS,
                    TerminalParser.term("satisfy"),
                    TerminalParser.term(";")
            );

    /**
     * Scanner for optimize declaration.
     */
    final Parser<SolveGoal> OPTIMIZE =
            Mapper.curry(SolveGoal.class).sequence(
                    Parsers.constant(this),
                    TerminalParser.term("solve"),
                    ANNOTATIONS,
                    Parsers.or(
                            TerminalParser.term("maximize").retn(SolveGoal.Resolution.MAXIMIZE),
                            TerminalParser.term("minimize").retn(SolveGoal.Resolution.MINIMIZE)
                    ),
                    expression(),
                    TerminalParser.term(";"));
    /**
     * Scanner for solve goals declaration.
     * Create a {@link parser.flatzinc.ast.SolveGoal} object.
     */
    @SuppressWarnings({"unchecked"})
    public final Parser<SolveGoal> SOLVE_GOAL =
            Parsers.or(SATISFY, OPTIMIZE);

    /**
     * Scanner for flatzinc model.
     */
    public Solver parse() {
        Parser<?> parser = Parsers.sequence(
                PRED_DECL.many(),
                PAR_VAR_DECL.many(),
                CONSTRAINT.many(),
                SOLVE_GOAL
        );
        TerminalParser.parse(parser, instance);
        if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
            layout.setSearchLoop(solver.getSearchLoop());
        }
        return solver;
    }

    /**
     * Read a {@link File} as a {@link String}.
     *
     * @param file path name of the file
     * @return {@link String}
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static String readFileAsString(File file) {
        byte[] buffer = new byte[(int) file.length()];
        try {
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
        } catch (FileNotFoundException fne) {
            throw new FZNException("File not found: " + fne.getMessage());
        } catch (IOException ioe) {
            throw new FZNException("IO exception: " + ioe.getMessage());
        }
        return new String(buffer);
    }

    /**
     * Load the instance.
     *
     * @param file flatzinc file
     */
    public void loadInstance(File file) {
        this.instance = readFileAsString(file);
    }

    public void loadInstance(String instance) {
        this.instance = instance;
    }
}
