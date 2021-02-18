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

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_annotation extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("anAnnotation");
        EAnnotation d = fp.annotation().ann;
        Assert.assertEquals("anAnnotation", d.id.value);
        Assert.assertEquals(0, d.exps.size());
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("anAnnotation(true)");
        EAnnotation d = fp.annotation().ann;
        Assert.assertEquals("anAnnotation", d.id.value);
        Assert.assertEquals(1, d.exps.size());
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        Flatzinc4Parser fp = parser("anAnnotation(true, false)");
        EAnnotation d = fp.annotation().ann;
        Assert.assertEquals("anAnnotation", d.id.value);
        Assert.assertEquals(2, d.exps.size());
    }

}


