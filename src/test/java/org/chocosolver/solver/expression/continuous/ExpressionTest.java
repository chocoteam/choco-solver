/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
