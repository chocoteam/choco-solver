/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.discrete;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
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
        return new Object[][]{{0}, {1}, {2}};
    }

    private void eval(Model model, ReExpression ex, int postAs, int nbsol){
        switch (postAs){
            case 0:
                ex.decompose().post();
                break;
            case 1:
                ex.extension().post();
                break;
            case 2:
                ex.boolVar().eq(1).post();
                break;
        }
        Assert.assertEquals(model.getSolver().streamSolutions().count(), nbsol);

    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test1(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.eq(1), p, 1);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test2(int p) {
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

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test37(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).or(x.eq(y)), p, 11);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test38(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).or(x.eq(y).and(x.eq(1))), p, 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test39(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).and(x.add(2).le(6)), p, 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test40(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).iff(x.add(2).le(6)), p, 9);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test40_0(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 2);
        IntVar y = model.intVar(0, 2);
        eval(model, x.eq(1).iff(y.eq(2)), p, 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test41(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).imp(x.add(2).le(6)), p, 35);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test41_0(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 2);
        IntVar y = model.intVar(0, 2);
        eval(model, x.eq(1).imp(y.eq(2)), p, 7);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test42(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        IntVar y = model.intVar(0, 5);
        eval(model, x.eq(y.add(1)).xor(x.add(2).le(6)), p, 27);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test42_0(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 2);
        IntVar y = model.intVar(0, 2);
        eval(model, x.eq(1).xor(y.eq(2)), p, 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test43(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 3);
        IntVar y = model.intVar(0, 3);
        eval(model, x.ge(2).add(y.ge(2)).eq(1), p, 8);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test44(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 3);
        IntVar y = model.intVar(0, 3);
        eval(model, x.ge(2).not().add(y.ge(2)).eq(1), p, 8);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test45(int p) {
        Model model = new Model();
        BoolVar x = model.boolVar();
        IntVar y = model.intVar(0, 3);
        eval(model, x.eq(y.eq(1).not().and(y.eq(2).not())), p, 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test46(int p){
        Model model = new Model();
        IntVar y = model.intVar(0, 3);
        IntVar w = model.intVar(0, 3);
        eval(model, y.eq(w.ge(2).ift(2,3)), p, 4);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test47(int p){
        Model model = new Model();
        IntVar y = model.intVar(0, 3);
        IntVar t = model.intVar(0, 3);
        IntVar w = model.intVar(0, 3);
        eval(model, y.eq(w.ge(2).ift(t.add(1),t.sub(1))), p, 12);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test48(int p){
        Model model = new Model();
        IntVar y = model.intVar(0, 3);
        IntVar t = model.intVar(0, 3);
        BoolVar b = model.boolVar();
        eval(model, y.eq(b.ift(t.add(1),t.sub(1))), p, 6);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "post")
    public void test49(int p){
        Model model = new Model();
        //variables
        IntVar d = model.intVar("D",0,100);
        IntVar x1 = model.intVar("X1", 0, 2);
        IntVar y1= model.intVar("Y1", 0, 2);
        IntVar x2= model.intVar("X2", 0, 2);
        IntVar y2= model.intVar("Y2", 0, 2);
        model.getSolver().showSolutions(()->String.format("%d = V[(%d - %d)^2 + (%d - %d)^2]",
            d.getValue(), x1.getValue(), x2.getValue(), y1.getValue(), y2.getValue()));
        eval(model, d.eq(((x1.sub(x2)).pow(2).add((y1.sub(y2)).pow(2))).sqr()), p, 81);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test50(int p) {
        Model model = new Model();
        IntVar x = model.intVar(0, 5);
        eval(model, x.in(1, 2, 3), p, 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "post")
    public void test51(int p) {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 5);
        IntVar y = model.intVar("y", -2, 2);
        IntVar z = model.intVar("z", 1, 3);
        model.getSolver().showSolutions();
        eval(model, x.in(y, z), p, 22);
    }

    @Test(groups = "1s")
    public void testJoao1() throws ContradictionException {
        Model model = new Model();
                IntVar qtdActive = model.intVar("qtdActive", 0, 2, true);
        BoolVar active = model.boolVar("active");
        IntVar qtd = model.intVar("qtd", 0, 2, true);

        qtdActive.ge(1).post();
        active.eq(1).post();
        active.eq(1).imp(qtd.eq(qtdActive)).post();

        model.getSolver().propagate();
        Assert.assertTrue(active.isInstantiatedTo(1));
        Assert.assertEquals(qtd.getLB(), 1);
        Assert.assertEquals(qtdActive.getLB(), 1);
    }

    @Test(groups = "1s")
    public void testJoao2_1() throws ContradictionException {
        Model model = new Model();
        BoolVar x = model.boolVar("x");
        IntVar y = model.intVar("y", new int[]{0, 1, 2});
        IntVar z = model.intVar("z", new int[]{0, 1, 2});

        z.ne(1).post();
        x.eq(1).imp(y.eq(z)).post(); // wrong -> y = {0..2}
        x.eq(1).post();

        model.getSolver().propagate();
        Assert.assertTrue(x.isInstantiatedTo(1));
        Assert.assertEquals(y.getLB(), 0);
        Assert.assertEquals(y.getUB(), 2);
        Assert.assertEquals(y.getDomainSize(), 2);
        Assert.assertEquals(z.getLB(), 0);
        Assert.assertEquals(z.getUB(), 2);
        Assert.assertEquals(z.getDomainSize(), 2);
    }

    @Test(groups = "1s")
    public void testJoao2_2() throws ContradictionException {
        Model model = new Model();
        BoolVar x = model.boolVar("x");
        IntVar y = model.intVar("y", new int[]{0, 1, 2});
        IntVar z = model.intVar("z", new int[]{0, 1, 2});

        z.ne(1).post();
        x.eq(1).imp(y.sub(z).eq(0)).post(); // wrong -> y = {0..2}
        x.eq(1).post();

        model.getSolver().propagate();
        Assert.assertTrue(x.isInstantiatedTo(1));
        Assert.assertEquals(y.getLB(), 0);
        Assert.assertEquals(y.getUB(), 2);
        Assert.assertEquals(y.getDomainSize(), 2);
        Assert.assertEquals(z.getLB(), 0);
        Assert.assertEquals(z.getUB(), 2);
        Assert.assertEquals(z.getDomainSize(), 2);
    }

    @Test(groups = "1s")
    public void testJoao2_3() throws ContradictionException {
        Model model = new Model();
        BoolVar x = model.boolVar("x");
        IntVar y = model.intVar("y", new int[]{0, 1, 2});
        IntVar z = model.intVar("z", new int[]{0, 1, 2});

        z.ne(1).post();
        y.eq(z).post(); // right -> y = {0,2}
        x.eq(1).post();

        model.getSolver().propagate();
        Assert.assertTrue(x.isInstantiatedTo(1));
        Assert.assertEquals(y.getLB(), 0);
        Assert.assertEquals(y.getUB(), 2);
        Assert.assertEquals(y.getDomainSize(), 2);
        Assert.assertEquals(z.getLB(), 0);
        Assert.assertEquals(z.getUB(), 2);
        Assert.assertEquals(z.getDomainSize(), 2);
    }

    @Test(groups = "1s")
    public void testJoao2_4() throws ContradictionException {
        Model model = new Model();
        BoolVar x = model.boolVar("x");
        IntVar y = model.intVar("y", new int[]{0, 1, 2});
        IntVar z = model.intVar("z", new int[]{0, 1, 2});

        z.ne(1).post();
        y.sub(z).eq(0).post(); // right -> y = {0,2}
        x.eq(1).post();

        model.getSolver().propagate();
        Assert.assertTrue(x.isInstantiatedTo(1));
        Assert.assertEquals(y.getLB(), 0);
        Assert.assertEquals(y.getUB(), 2);
        Assert.assertEquals(y.getDomainSize(), 2);
        Assert.assertEquals(z.getLB(), 0);
        Assert.assertEquals(z.getUB(), 2);
        Assert.assertEquals(z.getDomainSize(), 2);
    }
}
