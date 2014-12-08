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
package org.chocosolver.parser.flatzinc.parser;

import org.antlr.runtime.RecognitionException;
import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.Solver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_expr extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("true");
        Expression d =fp.expr().exp;
        Assert.assertTrue(d instanceof EBool);
        Assert.assertEquals(true, ((EBool) d).value);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("false");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EBool);
        Assert.assertEquals(false, ((EBool) d).value);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("12");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EInt);
        Assert.assertEquals(12, ((EInt) d).value);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("-12");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EInt);
        Assert.assertEquals(-12, ((EInt) d).value);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("1..3");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof ESetBounds);
    }

    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("{1,2,3}");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof ESetList);
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("a");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EIdentifier);
    }

    @Test(groups = "1s")
    public void test8() throws IOException, RecognitionException {
        String[] _a = {"a","b"};
        Datas da = new Datas();
        da.register("a", _a);
        Flatzinc4Parser fp = parser("a[1]", new Solver(), da);
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EIdArray);
    }

    @Test(groups = "1s")
    public void test9() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("[]");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EArray);
    }


    @Test(groups = "1s")
    public void test10() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("[a,b]");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EArray);
    }

    @Test(groups = "1s")
    public void test11() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("\"toto\"");
        Expression d = fp.expr().exp;
        Assert.assertTrue(d instanceof EString);
    }

}
