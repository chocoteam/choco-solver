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
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.Model;
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
        Flatzinc4Parser fp = parser("a[1]", new Model(), da);
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
