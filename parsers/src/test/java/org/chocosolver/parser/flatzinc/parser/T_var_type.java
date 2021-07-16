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
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_var_type extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var bool");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DBool);
        Assert.assertEquals(DBool.me, d);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var float");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DFloat);
        Assert.assertEquals(DFloat.me, d);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var int");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DInt);
        Assert.assertEquals(DInt.me, d);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var 1 .. 4");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DManyInt);
    }

    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var set of 1 .. 4");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("var set of {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DManyInt);
    }

    @Test(groups = "1s")
    public void test8() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var bool");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DBool.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test10() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var int");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DInt.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test11() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var 1..3");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test12() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DManyInt);
    }


    @Test(groups = "1s")
    public void test13() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var set of 1..3");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DSet);
    }

    @Test(groups = "1s")
    public void test14() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of var set of {1,2,3}");
        Declaration d = fp.var_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertTrue(((DArray) d).getWhat() instanceof DSet);
    }


}
