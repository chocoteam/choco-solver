/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 29/04/2016.
 */
public class ExpressionTest {

    @DataProvider(name = "post")
    public Object[][] provider() {
        return new Object[][]{{0}};
    }

    public void eval(Model model, CReExpression ex, int postAs, int nbsol){
        switch (postAs){
            case 0:
                ex.ibex(1.d).post();
                break;
        }
        System.out.printf("%s\n", model);
        model.getSolver().showSolutions();
        Assert.assertEquals(model.getSolver().streamSolutions().count(), nbsol);
        model.getIbex().release();
    }


    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test1(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        eval(model, x.eq(1), p, 1);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test3(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        eval(model, x.lt(1), p, 2);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test4(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        eval(model, x.le(1), p, 2);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test5(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        eval(model, x.gt(1), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test6(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        eval(model, x.ge(1), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test7(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.add(y).eq(1), p, 2);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test8(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.sub(1).eq(y), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test9(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.mul(2).eq(y), p, 4);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test10(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.div(2).eq(y), p, 8);
    }


    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test12(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.min(2).eq(y), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test13(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.max(2).eq(y), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test14(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.abs().eq(y), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test15(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.neg().eq(y), p, 4);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test16(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.pow(2).eq(y), p, 6);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test17(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.pow(3).eq(y), p, 9);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test18(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.atan2(3).eq(y), p, 1);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test19(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.ln().eq(y), p, 7);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test20(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.sqrt().eq(y), p, 7);
    }


    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test21(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.cos().eq(y), p, 4);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test22(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.sin().eq(y), p, 6);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test23(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.tan().eq(y), p, 15);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test24(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.acos().eq(y), p, 3);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test25(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.asin().eq(y), p, 3);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test26(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.atan().eq(y), p, 7);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test27(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.cosh().eq(y), p, 6);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test28(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.sinh().eq(y), p, 6);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test29(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.tanh().eq(y), p, 8);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test30(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.acosh().eq(y), p, 7);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test31(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.asinh().eq(y), p, 7);
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "post")
    public void test32(int p) {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 1.d);
        RealVar y = model.realVar("y", 0, 5, 1.d);
        eval(model, x.atanh().eq(y), p, 9);
    }

}
