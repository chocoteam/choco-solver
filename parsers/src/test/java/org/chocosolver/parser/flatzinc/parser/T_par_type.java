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
public class T_par_type extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("bool");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DBool);
        Assert.assertEquals(DBool.me, d);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("float");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DFloat);
        Assert.assertEquals(DFloat.me, d);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("int");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DInt);
        Assert.assertEquals(DInt.me, d);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("set of int");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DSetOfInt);
        Assert.assertEquals(DSetOfInt.me, d);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of bool");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DBool.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of float");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DFloat.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of int");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DInt.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test8() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..2] of set of int");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DSetOfInt.me, ((DArray) d).getWhat());
    }

    @Test(groups = "1s")
    public void test9() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("array [1..4] of set of int: a = [1..2, 2..3, {1, 3}, 2..2];");
        Declaration d = fp.par_type().decl;
        Assert.assertTrue(d instanceof DArray);
        Assert.assertEquals(1, ((DArray) d).getDimension());
        Assert.assertEquals(Declaration.DType.INT2, ((DArray) d).getIndex(0).typeOf);
        Assert.assertEquals(DSetOfInt.me, ((DArray) d).getWhat());
    }

}
