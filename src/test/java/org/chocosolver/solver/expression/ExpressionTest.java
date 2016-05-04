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
package org.chocosolver.solver.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.arithmetic.ArExpression;
import org.chocosolver.solver.expression.relational.ReExpression;
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
        Assert.assertEquals(model.streamSolutions().count(), nbsol);
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

    @Test(groups="1s", timeOut=6000000, dataProvider = "post")
    public void testLongExpression(int p){
        Model model  = new Model();
        IntVar[] XS = model.intVarArray("X", 10, 0, 4);
        IntVar Y = model.intVar("Y", 0, 2);
        final ArExpression[] r = {XS[0]};
        IntStream.range(1, XS.length).forEach(i -> r[0] = r[0].add(XS[i]));
        eval(model, Y.eq(r[0]), p, 66);
    }
}
