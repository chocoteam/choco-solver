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
public class T_var_type_u extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("bool");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DBool);
        Assert.assertEquals(DBool.me, d);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("float");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DFloat);
        Assert.assertEquals(DFloat.me, d);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("int");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DInt);
        Assert.assertEquals(DInt.me, d);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("1..4");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("{1,2,3}");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DManyInt);
    }


    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("set of 1..4");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DInt2);
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("set of {1,2,3}");
        Declaration d = fp.var_type_u().decl;
        Assert.assertTrue(d instanceof DSet);
        Assert.assertTrue(((DSet) d).getWhat() instanceof DManyInt);
    }
}
