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

import junit.framework.Assert;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.misc.Mapper;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import parser.flatzinc.ast.expression.EString;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/07/12
 */
public class AnnotationTest {

    FZNParser fzn;

    @BeforeTest
    public void before() {
        fzn = new FZNParser();
    }

    @Test
    public void test1() {
        TerminalParser.parse(fzn.expression(), "is_defined_var");
    }

    @Test
    public void test2() {
        TerminalParser.parse(fzn.expression(), "toto(titi)");
    }

    @Test
    public void test3() {
        TerminalParser.parse(fzn.expression(), "toto([])");
    }

    @Test
    public void test4() {
        TerminalParser.parse(fzn.expression(), "toto([titi])");
    }

    @Test
    public void test5() {
        TerminalParser.parse(fzn.expression(), "toto(0, 2)");
    }

    @Test
    public void test6() {
        Mapper.curry(EString.class).sequence(Scanners.DOUBLE_QUOTE_STRING).parse("\"toto\"");
    }


    @Test
    public void testStringLiteral() {
        Parser<EString> parser = FZNParser.STRING_LITERAL;
        assertResult(parser, "\"\"", EString.class, "");
        assertResult(parser, "\"foo\"", EString.class, "foo");
        assertResult(parser, "\"\\\"\"", EString.class, "\"");
    }

    static <T> void assertResult(
            Parser<T> parser, String source, Class<? extends T> expectedType, String expectedResult) {
        assertToString(expectedType, expectedResult, TerminalParser.parse(parser, source));
    }

    static <T> void assertToString(
            Class<? extends T> expectedType, String expectedResult, T result) {
        Assert.assertTrue(expectedType.isInstance(result));
        Assert.assertEquals(expectedResult, result.toString());
    }

    @Test
    public void test8() {
        TerminalParser.parse(fzn.expression(), "toto(\"titi\")");
    }

    @Test
    public void test9() {
        TerminalParser.parse(fzn.ANNOTATIONS, "::is_defined_var");
    }

    @Test
    public void test10() {
        TerminalParser.parse(fzn.ANNOTATIONS, "::var_is_introduced::is_defined_var");
    }

    @Test
    public void test11() {
        TerminalParser.parse(fzn.ANNOTATIONS, "::output_array([1.. 3])");
    }

    @Test
    public void test12() {
        TerminalParser.parse(fzn.ANNOTATIONS, ":: viz([viztype(\"vector\"), vizpos(0, 2), vizdisplay(\"expanded\"), vizwidth(8), vizheight(8), vizrange(1, 8)])");
    }

    @Test
    public void test13a() {
        TerminalParser.parse(fzn.expression(), "int_search([a],input_order,indomain_min,complete)");
    }

    @Test
    public void test13b() {
        TerminalParser.parse(fzn.expression(), "seq([a1(a2),a3])");
    }

    @Test
    public void test13c() {
        TerminalParser.parse(fzn.expression(), "seq_search([int_search([a],input_order,indomain_min,complete)])");
    }

    @Test
    public void testOutput() {
        fzn = new FZNParser(true, false);
        fzn.loadInstance("array [1..2] of var 1..3: xs :: output_array([1..2]);\n" +
                "constraint int_lt(xs[1], xs[2]); % x[1] < x[2].\n" +
                "solve satisfy;");
        fzn.parse();
        fzn.solver.solve();
    }

    @Test
    public void testOutput2() {
        fzn = new FZNParser(true, false);
        fzn.loadInstance("array [1..0] of var 0..315: st_rec_y :: output_array([{}]);\n" +
                "solve satisfy;");
        fzn.parse();
        fzn.solver.solve();
    }

}
