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
import org.chocosolver.parser.flatzinc.ast.declaration.DInt;
import org.chocosolver.parser.flatzinc.ast.declaration.DInt2;
import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_index_set extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("1..3");
        Declaration d = fp.index_set().decl;
        Assert.assertTrue(d instanceof DInt2);
        Assert.assertEquals(1, ((DInt2) d).getLow());
        Assert.assertEquals(3, ((DInt2) d).getUpp());
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("int");
        Declaration d = fp.index_set().decl;
        Assert.assertEquals(DInt.me, d);
    }


}
