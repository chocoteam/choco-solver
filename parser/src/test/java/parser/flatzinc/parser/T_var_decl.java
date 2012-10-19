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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincParser;
import parser.flatzinc.FlatzincWalker;
import solver.Solver;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.view.IntView;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_var_decl extends GrammarTest {

    Solver mSolver;
    THashMap<String, Object> map;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new THashMap<String, Object>();
    }


    public void var_decl(FlatzincParser parser) throws RecognitionException {
        FlatzincParser.var_decl_return r = parser.var_decl();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincWalker walker = new FlatzincWalker(nodes);
        walker.mSolver = mSolver;
        walker.map = map;
        walker.var_decl();
    }

    @Test
    public void test1() throws IOException {
        FlatzincParser fp = parser("var 0..9: digit;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("digit");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("digit", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(9, var.getUB());
    }

    @Test
    public void test2() throws IOException {
        FlatzincParser fp = parser("var bool: b::var_is_introduced::is_defined_var;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("b");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof BoolVar);
        BoolVar var = ((BoolVar) o);
        Assert.assertEquals("b", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(1, var.getUB());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test3() throws IOException {
        FlatzincParser fp = parser("var set of 1..3: s;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test4() throws IOException {
        FlatzincParser fp = parser("var 0.1..1.0: f;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

    @Test
    public void test5() throws IOException {
        FlatzincParser fp = parser("var int : y :: mip; % 'mip' annotation\n");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("y");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("y", var.getName());
    }

    @Test
    public void test6() throws IOException {
        FlatzincParser fp = parser("array [1..3] of var 1..10:a;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("a");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar[]);
        IntVar[] a = ((IntVar[]) o);
        Assert.assertEquals(3, a.length);
        Assert.assertEquals(10, a[0].getDomainSize());

        fp = parser("array [1..3] of var 1..10:b = [a[3],a[2],a[1]];");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        o = map.get("b");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar[]);
        IntVar[] b = ((IntVar[]) o);
        Assert.assertEquals(3, b.length);
        Assert.assertEquals(10, b[0].getDomainSize());
        Assert.assertEquals(a[0], b[2]);
    }

    @Test
    public void test7() throws IOException {
        FlatzincParser fp = parser("var {0,3,18}: B::var_is_introduced;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("B");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("B", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(18, var.getUB());
    }


    @Test
    public void test8() throws IOException {
        FlatzincParser fp = parser("var 123456789..987654321: INT____00001 :: is_defined_var :: var_is_introduced;");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("INT____00001");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("INT____00001", var.getName());
        Assert.assertEquals(123456789, var.getLB());
        Assert.assertEquals(987654321, var.getUB());

        fp = parser("var 123456789..987654321: num :: output_var = INT____00001;");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }

        o = map.get("num");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntView);
    }

    @Test
    public void test9() throws IOException {
        FlatzincParser fp = parser("array[1 .. 3] of var 0 .. 9: C::output_array([ 1 .. 3 ]);");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("C");
        Assert.assertTrue(o.getClass().isArray());
        IntVar[] oi = (IntVar[]) o;
        Assert.assertEquals(oi.length, 3);
        Assert.assertEquals(oi[0].getName(), "C_1");
        Assert.assertEquals(oi[0].getDomainSize(), 10);
    }

    @Test
    public void test10() throws IOException {
        FlatzincParser fp = parser("var 1 .. 5: a ::output_var;");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        fp = parser("var 1 .. 5: b::output_var;");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }


        fp = parser("var 1 .. 5: c::output_var;");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }

        fp = parser("array[1 .. 3] of var 1 .. 5: alpha = [ a, b, c];");
        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
        Object o = map.get("alpha");
        Assert.assertTrue(o.getClass().isArray());
        IntVar[] oi = (IntVar[]) o;
        Assert.assertEquals(oi.length, 3);
        Assert.assertEquals(oi[0].getName(), "a");
        Assert.assertEquals(oi[0].getDomainSize(), 5);
    }

    @Test
    public void test11() throws IOException {
        FlatzincParser fp = parser("array [1..8] of var 1..8: queens " +
                ":: output_array([1..8]) " +
                ":: viz([viztype(\"vector\"), vizpos(0, 2), vizdisplay(\"expanded\"), vizwidth(8), vizheight(8), vizrange(1, 8)]);");

        try {
            var_decl(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

}
