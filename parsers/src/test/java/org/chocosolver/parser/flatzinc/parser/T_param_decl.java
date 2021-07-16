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

import org.chocosolver.parser.flatzinc.Flatzinc4Parser;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_param_decl extends GrammarTest {

    @Test(groups = "1s")
    public void test1() throws IOException {
        Flatzinc4Parser fp = parser("bool: beer_is_good = true;", null, new Datas());
        fp.param_decl();
        Object o = fp.datas.get("beer_is_good");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Boolean);
        Assert.assertEquals(true, ((Boolean) o).booleanValue());
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        Flatzinc4Parser fp = parser("int: n = 4;", null, new Datas());
        fp.datas = new Datas();
        fp.param_decl();
        Object o = fp.datas.get("n");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Integer);
        Assert.assertEquals(4, ((Integer) o).intValue());
    }


    @Test(groups = "1s")
    public void test3() throws IOException {
        Flatzinc4Parser fp = parser("array [1..7] of int: fib = [1,1,2,3,5,8,13];", null, new Datas());
        fp.datas = new Datas();
        fp.param_decl();
        Object o = fp.datas.get("fib");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof int[]);
        Assert.assertEquals(new int[]{1, 1, 2, 3, 5, 8, 13}, o);
    }

    @Test(groups = "1s")
    public void test4() throws IOException {
        Flatzinc4Parser fp = parser("array [1..3] of set of int: suc = [{5, 10, 14}, {}, {}];", null, new Datas());
        fp.datas = new Datas();
        fp.param_decl();
        Object o = fp.datas.get("suc");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof int[][]);
        Assert.assertEquals(new int[][]{{5,10,14},{},{}}, o);
    }
}
