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

import org.antlr.runtime.RecognitionException;
import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.expression.EAnnotation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_annotations extends GrammarTest {

    @Test(groups = "1s")
    public void test0() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("");
        List<EAnnotation> as = fp.annotations().anns;
        Assert.assertEquals(0, as.size());
    }

    @Test(groups = "1s")
    public void testa1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("::ann1");
        List<EAnnotation> as = fp.annotations().anns;
        Assert.assertEquals(1, as.size());
        Assert.assertEquals("ann1", as.get(0).id.value);
    }

    @Test(groups = "1s")
    public void testa2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("::ann1::ann2");
        List<EAnnotation> as = fp.annotations().anns;
        Assert.assertEquals(2, as.size());
        Assert.assertEquals("ann1", as.get(0).id.value);
        Assert.assertEquals("ann2", as.get(1).id.value);
    }

    private void fastcheck(String toParse) throws IOException {
        Flatzinc4Parser fp = parser(toParse);
        List<EAnnotation> as = fp.annotations().anns;
        Assert.assertTrue(as.size() > 0);
    }

    @Test(groups = "1s")
    public void test1() throws IOException {
        fastcheck("::is_defined_var");
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        fastcheck("::toto(titi)");
    }

    @Test(groups = "1s")
    public void test3() throws IOException {
        fastcheck("::toto([])");
    }

    @Test(groups = "1s")
    public void test4() throws IOException {
        fastcheck("::toto([titi])");
    }

    @Test(groups = "1s")
    public void test5() throws IOException {
        fastcheck("::toto(0, 2)");
    }

    @Test(groups = "1s")
    public void test6() throws IOException {
        fastcheck("::toto(\"titi\")");
    }

    @Test(groups = "1s")
    public void test7() throws IOException {
        fastcheck("::is_defined_var");
    }

    @Test(groups = "1s")
    public void test8() throws IOException {
        fastcheck("::var_is_introduced::is_defined_var");
    }

    @Test(groups = "1s")
    public void test9() throws IOException {
        fastcheck("::output_array([1.. 3])");
    }

    @Test(groups = "1s")
    public void test10() throws IOException {
        fastcheck(":: viz([viztype(\"vector\"), vizpos(0, 2), vizdisplay(\"expanded\"), vizwidth(8), vizheight(8), vizrange(1, 8)])");
    }

    @Test(groups = "1s")
    public void test11() throws IOException {
        fastcheck("::int_search([a],input_order,indomain_min,complete)");
    }

    @Test(groups = "1s")
    public void test12() throws IOException {
        fastcheck("::seq([a1(a2),a3])");
    }

    @Test(groups = "1s")
    public void test13() throws IOException {
        fastcheck("::seq_search([int_search([a],input_order,indomain_min,complete)])");
    }
//
//    @Test( groups = "1s" )
//    public void testOutput() {
//        fzn = new FZNParser(true, false);
//        fzn.loadInstance("array [1..2] of var 1..3: xs :: output_array([1..2]);\n" +
//                "constraint int_lt(xs[1], xs[2]); % x[1] < x[2].\n" +
//                "solve satisfy;");
//        fzn.parse();
//        fzn.solver.solve();
//    }
//
//    @Test( groups = "1s" )
//    public void testOutput2() {
//        fzn = new FZNParser(true, false);
//        fzn.loadInstance("array [1..0] of var 0..315: st_rec_y :: output_array([{}]);\n" +
//                "solve satisfy;");
//        fzn.parse();
//        fzn.solver.solve();
//    }

}
