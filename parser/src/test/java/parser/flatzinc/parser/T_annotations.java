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

import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.Assert;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincParser;
import parser.flatzinc.FlatzincWalker;
import parser.flatzinc.ast.expression.EAnnotation;

import java.io.IOException;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_annotations extends GrammarTest {


    public List<EAnnotation> annotations(FlatzincParser parser) throws RecognitionException {
        FlatzincParser.annotations_return r = parser.annotations();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincWalker walker = new FlatzincWalker(nodes);
        walker.map = new THashMap<String, Object>();
        return walker.annotations();
    }

    @Test(groups = "1s")
    public void test0() throws IOException, RecognitionException {
        FlatzincParser fp = parser("");
        List<EAnnotation> as = annotations(fp);
        Assert.assertEquals(0, as.size());
    }

    @Test(groups = "1s")
    public void testa1() throws IOException, RecognitionException {
        FlatzincParser fp = parser("::ann1");
        List<EAnnotation> as = annotations(fp);
        Assert.assertEquals(1, as.size());
        Assert.assertEquals("ann1", as.get(0).id.value);
    }

    @Test(groups = "1s")
    public void testa2() throws IOException, RecognitionException {
        FlatzincParser fp = parser("::ann1::ann2");
        List<EAnnotation> as = annotations(fp);
        Assert.assertEquals(2, as.size());
        Assert.assertEquals("ann1", as.get(0).id.value);
        Assert.assertEquals("ann2", as.get(1).id.value);
    }

    private void fastcheck(String toParse) throws IOException {
        FlatzincParser fp = parser(toParse);
        List<EAnnotation> as = null;
        try {
            as = annotations(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
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
