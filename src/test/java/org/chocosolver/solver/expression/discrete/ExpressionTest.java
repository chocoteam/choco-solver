/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

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
        return new Object[][]{{0}, {1}};
    }

    public void eval(Model model, ReExpression ex, int postAs, int nbsol){
        switch (postAs){
            case 0:
                ex.decompose().post();
                break;
            case 1:
                ex.extension().post();
                break;
        }
        System.out.printf("%s\n", model);
        Assert.assertEquals(model.getSolver().streamSolutions().count(), nbsol);
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test1(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.eq(1), p, 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.ne(1), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test3(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.lt(1), p, 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test4(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.le(1), p, 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test5(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.gt(1), p, 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test6(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.ge(1), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test7(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.add(y).eq(1), p, 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test8(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.sub(1).eq(y), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test9(int p) {
        Model model = new Model();
        IntVar x = model.intVar(1, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.mul(2).eq(y), p, 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test10(int p) {
        Model model = new Model();
        IntVar x = model.intVar(1, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.div(2).eq(y), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test11(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.mod(2).eq(y), p, 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test112(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 7);
        IntVar y = model.intVar(0, 7);
        IntVar z = model.intVar(0, 9);
        eval(model, x.add(y).mod(10).eq(z), p, 64);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test12(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.min(2).eq(y), p, 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test13(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.max(2).eq(y), p, 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test14(int p) {
        Model model = new Model();
        IntVar x = model.intVar(-2, 2);
        IntVar y = model.intVar(0, 5);
        eval(model, x.abs().eq(y), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test15(int p) {
        Model model = new Model();
        IntVar x = model.intVar(-2, 2);
        IntVar y = model.intVar(0, 5);
        eval(model, x.neg().eq(y), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test16(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.sqr().eq(y), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test17(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.pow(3).eq(y), p, 2);
    }

    @Test(groups="10s", timeOut=60000, dataProvider = "post")
    public void testLongExpression(int p){
        Model model  = new Model();
        IntVar[] XS = model.intVarArray("X", 10, 0, 4);
        IntVar Y = model.intVar("Y", 0, 2);
        final ArExpression[] r = {XS[0]};
        IntStream.range(1, XS.length).forEach(i -> r[0] = r[0].add(XS[i]));
        eval(model, Y.eq(r[0]), p, 66);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test18(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar[] y = model.intVarArray(2, 0, 5);
        eval(model, x.add(y).eq(10), p, 21);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test19(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar[] y = model.intVarArray(2, 0, 5);
        eval(model, x.mul(y).eq(20), p, 9);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test20(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar[] y = model.intVarArray(2, 0, 5);
        eval(model, x.min(y).eq(1), p, 61);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test21(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar[] y = model.intVarArray(2, 0, 5);
        eval(model, x.max(y).eq(1), p, 7);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test22(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.add(2).eq(4), p, 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test23(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.sub(y).eq(3), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test24(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.mul(y).eq(4), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test25(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.div(y).eq(1), p, 9);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test26(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.mod(y).eq(1), p, 8);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test27(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.pow(y).eq(4), p, 2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test28(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.min(y).eq(1), p, 9);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test29(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.max(y).eq(1), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test30(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.dist(y).eq(1), p, 10);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test31(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.dist(2).eq(y), p, 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test32(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.lt(y), p, 15);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test33(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.le(y), p, 21);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test34(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.gt(y), p, 15);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test35(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.ge(y), p, 21);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test36(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.ne(y), p, 30);
    }
}
