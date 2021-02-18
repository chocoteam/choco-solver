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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_bool_const extends GrammarTest {


    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        Assert.assertTrue(parser("true").bool_const().value);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        Assert.assertFalse(parser("false").bool_const().value);
    }
}
