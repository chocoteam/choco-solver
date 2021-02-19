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
import org.chocosolver.parser.flatzinc.FznSettings;
import org.chocosolver.parser.flatzinc.ast.Datas;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_var_decl extends GrammarTest {

    Model mSolver;
    Datas datas;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        mSolver = new Model(new FznSettings().setEnableViews(true));
        datas = new Datas();
    }


    @Test(groups = "1s")
    public void test1() throws IOException {
        Flatzinc4Parser fp = parser("var 0..9: digit;", mSolver, datas);
        fp.var_decl();
        Object o = datas.get("digit");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("digit", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(9, var.getUB());
    }

    @Test(groups = "1s")
    public void test2() throws IOException {
        Flatzinc4Parser fp = parser("var bool: b::var_is_introduced::is_defined_var;", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("b");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof BoolVar);
        BoolVar var = ((BoolVar) o);
        Assert.assertEquals("b", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(1, var.getUB());
    }

    @Test(groups = "1s")
    public void test3() throws IOException {
        Flatzinc4Parser fp = parser("var set of 1..3: s;", mSolver, datas);
        fp.var_decl();
        Object o = datas.get("s");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof SetVar);
        SetVar var = ((SetVar) o);
        Assert.assertEquals("s", var.getName());
        int[] UB = var.getUB().toArray();
        int[] values = new int[]{1,2,3};
        for(int i=0;i<UB.length;i++){
            Assert.assertEquals(UB[i],values[i]);
        }
    }

    @Test(groups = "1s")
    public void test3bis() throws IOException {
        Flatzinc4Parser fp = parser("array[1..2] of var set of 1..3: sets;", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("sets");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof SetVar[]);
        SetVar[] vars = ((SetVar[]) o);
        Assert.assertEquals(2, vars.length);
    }

    @Test(groups = "1s", expectedExceptions = NullPointerException.class)
    public void test4() throws IOException {
        Flatzinc4Parser fp = parser("var 0.1..1.0: f;", mSolver, datas);

        fp.var_decl();
    }

    @Test(groups = "1s")
    public void test5() throws IOException {
        Flatzinc4Parser fp = parser("var int : y :: mip; % 'mip' annotation\n", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("y");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("y", var.getName());
    }

    @Test(groups = "1s")
    public void test6() throws IOException {
        Flatzinc4Parser fp = parser("array [1..3] of var 1..10:a;", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("a");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar[]);
        IntVar[] a = ((IntVar[]) o);
        Assert.assertEquals(3, a.length);
        Assert.assertEquals(10, a[0].getDomainSize());

        fp = parser("array [1..3] of var 1..10:b = [a[3],a[2],a[1]];", mSolver, datas);
        fp.var_decl();
        o = datas.get("b");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar[]);
        IntVar[] b = ((IntVar[]) o);
        Assert.assertEquals(3, b.length);
        Assert.assertEquals(10, b[0].getDomainSize());
        Assert.assertEquals(a[0], b[2]);
    }

    @Test(groups = "1s")
    public void test7() throws IOException {
        Flatzinc4Parser fp = parser("var {0,3,18}: B::var_is_introduced;", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("B");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("B", var.getName());
        Assert.assertEquals(0, var.getLB());
        Assert.assertEquals(18, var.getUB());
    }


    @Test(groups = "1s")
    public void test8() throws IOException {
        datas = new Datas(null, false, false);

        Flatzinc4Parser fp = parser("var 123456789..987654321: INT____00001 :: is_defined_var :: var_is_introduced;", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("INT____00001");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof IntVar);
        IntVar var = ((IntVar) o);
        Assert.assertEquals("INT____00001", var.getName());
        Assert.assertEquals(123456789, var.getLB());
        Assert.assertEquals(987654321, var.getUB());

        fp = parser("var 123456789..987654321: num :: output_var = INT____00001;", mSolver, datas);
        fp.var_decl();

        o = datas.get("num");
        Assert.assertNotNull(o);
    }

    @Test(groups = "1s")
    public void test9() throws IOException {
        datas = new Datas(null, false, false);

        Flatzinc4Parser fp = parser("array[1 .. 3] of var 0 .. 9: C::output_array([ 1 .. 3 ]);", mSolver, datas);

        fp.var_decl();
        Object o = datas.get("C");
        Assert.assertTrue(o.getClass().isArray());
        IntVar[] oi = (IntVar[]) o;
        Assert.assertEquals(oi.length, 3);
        Assert.assertEquals(oi[0].getName(), "C_1");
        Assert.assertEquals(oi[0].getDomainSize(), 10);
    }

    @Test(groups = "1s")
    public void test10() throws IOException {
        datas = new Datas(null, false, false);

        Flatzinc4Parser fp = parser("var 1 .. 5: a ::output_var;", mSolver, datas);
        fp.var_decl();
        fp = parser("var 1 .. 5: b::output_var;", mSolver, datas);
        fp.var_decl();


        fp = parser("var 1 .. 5: c::output_var;", mSolver, datas);
        fp.var_decl();

        fp = parser("array[1 .. 3] of var 1 .. 5: alpha = [ a, b, c];", mSolver, datas);
        fp.var_decl();
        Object o = datas.get("alpha");
        Assert.assertTrue(o.getClass().isArray());
        IntVar[] oi = (IntVar[]) o;
        Assert.assertEquals(oi.length, 3);
        Assert.assertEquals(oi[0].getName(), "a");
        Assert.assertEquals(oi[0].getDomainSize(), 5);
    }

    @Test(groups = "1s")
    public void test11() throws IOException {
        datas = new Datas(null, false, false);

        Flatzinc4Parser fp = parser("array [1..8] of var 1..8: queens " +
                ":: output_array([1..8]) " +
                ":: viz([viztype(\"vector\"), vizpos(0, 2), vizdisplay(\"expanded\"), vizwidth(8), vizheight(8), vizrange(1, 8)]);", mSolver, datas);

        fp.var_decl();
    }

}
