/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.expression.continuous;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme, Albert Hendriks
 * @since 29/04/2016.
 */
public class IbexTest {

    public void eval(Model model, CReExpression ex, int nbsol){
        ex.ibex(0.1d).post();
        Assert.assertEquals(model.getSolver().streamSolutions().count(), nbsol);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test1() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        eval(model, x.eq(1), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test1a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1.11, 5, 0.1d);
        eval(model, x.eq(1), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test2() {
        Model model = new Model();
        RealVar x = model.realVar("x", 2.15, 5, 0.1d);
        eval(model, x.lt(2), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test3() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 0.1d);
        eval(model, x.le(1), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test5() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 4.9, 0.1d);
        eval(model, x.gt(5), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test6() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        eval(model, x.ge(5), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test7() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 0.1d);
        RealVar y = model.realVar("y", 1, 5, 0.1d);
        eval(model, x.add(y).eq(2), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test7a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 0.1d);
        RealVar y = model.realVar("y", 1, 5, 0.1d);
        eval(model, x.add(y).eq(10), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test7b() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 5, 0.1d);
        RealVar y = model.realVar("y", 1, 5, 0.1d);
        eval(model, x.add(y).eq(1), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test8() {
        Model model = new Model();
        RealVar x = model.realVar("x", 2, 5, 0.1d);
        RealVar y = model.realVar("y", 0, 1, 0.1d);
        eval(model, x.sub(1).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test9() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 2, 0.1d);
        RealVar y = model.realVar("y", 4, 5, 0.1d);
        eval(model, x.mul(2).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test10() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 4, 0.1d);
        RealVar y = model.realVar("y", 2, 5, 0.1d);
        eval(model, x.div(2).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test100() {
        Model model = new Model();
        RealVar w = model.realVar("w", 2, 3, 0.1d);
        RealVar x = model.realVar("x", 0, 4, 0.1d);
        RealVar y = model.realVar("y", 2, 5, 0.1d);
        eval(model, x.div(w).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test101() {
        Model model = new Model();
        RealVar w = model.realVar("w", -2, 2, .1d);
        RealVar x = model.realVar("x", -2, 2, .1d);
        eval(model, x.div(w).eq(.1d), 58);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test11() {
        Model model = new Model();
        RealVar x = model.realVar("x", 2.5, 4, 0.1d);
        RealVar y = model.realVar("y", 2, 2.8, 0.1d);
        RealVar z = model.realVar("z", 1, 1.1, 0.1d);
        eval(model, x.div(y).eq(z), 32);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test12() {
        Model model = new Model();
        RealVar x = model.realVar("x", 5, 5, 0.1d);
        RealVar y = model.realVar("y", 0, 3, 0.1d);
        eval(model, x.min(2).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test120() {
        Model model = new Model();
        RealVar w = model.realVar("w", 1.6, 1.8, 0.1d);
        RealVar x = model.realVar("x", 1.8, 2.5, 0.1d);
        RealVar y = model.realVar("y", 1.7, 2, 0.1d);
        eval(model, x.min(w).eq(y), 16);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test13() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 1, 0.1d);
        RealVar y = model.realVar("y", 0, 5, 0.1d);
        eval(model, x.max(3).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test130() {
        Model model = new Model();
        RealVar w = model.realVar("w", 1.6, 1.8, 0.1d);
        RealVar x = model.realVar("x", 1.8, 2.5, 0.1d);
        RealVar y = model.realVar("y", 1.7, 2, 0.1d);
        eval(model, x.max(w).eq(y), 9);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test13a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 3, 0.1d);
        RealVar y = model.realVar("y", 3, 5, 0.1d);
        eval(model, x.max(1).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test14() {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 0.1d);
        RealVar y = model.realVar("y", 2, 5, 0.1d);
        eval(model, x.abs().eq(y), 2);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test15() {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 0.1d);
        RealVar y = model.realVar("y", 2, 5, 0.1d);
        eval(model, x.neg().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test16() {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 0.1d);
        RealVar y = model.realVar("y", 4, 5, 0.1d);
        // See ISSUE #702, now pow(double) can recognize integer variables
        eval(model, x.pow(2.0d).eq(y), 2);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test17() {
        Model model = new Model();
        RealVar x = model.realVar("x", -2, 2, 0.1d);
        RealVar y = model.realVar("y", 8, 25, 0.1d);
        eval(model, x.pow(3).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test18() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", -5, 0, 0.1d);
        eval(model, x.atan2(3).eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test19() {
        Model model = new Model();
        RealVar x = model.realVar("x", 1, 10, 0.1d);
        RealVar y = model.realVar("y", 0.99, 1, 0.1d);
        eval(model, x.ln().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
     public void test20() {
         Model model = new Model();
         RealVar x = model.realVar("x", 3, 5, 0.1d);
         RealVar y = model.realVar("y", 0, 9, 0.1d);
         eval(model, x.sqr().eq(y), 1);
     }

    @Test(groups = "ibex", timeOut = 60000)
    public void test200() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 9, 0.1d);
        RealVar y = model.realVar("y", 3, 5, 0.1d);
        eval(model, x.sqrt().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test201() {
        Model model = new Model();
        RealVar x = model.realVar("x", 2, 4, 0.1d);
        RealVar y = model.realVar("y", 0, 8, 0.1d);
        eval(model, x.cub().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test202() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 8, 0.1d);
        RealVar y = model.realVar("y", 2, 4, 0.1d);
        eval(model, x.cbrt().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test21() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 4 * Math.PI, 0.1d);
        RealVar y = model.realVar("y", 1, 5, 0.1d);
        eval(model, x.cos().eq(y), 3);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test22() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 4 * Math.PI, 0.1d);
        RealVar y = model.realVar("y", 1, 5, 0.1d);
        eval(model, x.sin().eq(y), 2);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test23() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", 0, 0, 0.1d);
        eval(model, x.tan().eq(y), 2);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test24() {
        Model model = new Model();
        RealVar x = model.realVar("x", -10, 10, 0.1d);
        RealVar y = model.realVar("y", -10, 0, 0.1d);
        eval(model, x.acos().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test25() {
        Model model = new Model();
        RealVar x = model.realVar("x", -10, 10, 0.1d);
        RealVar y = model.realVar("y", 0.5 * Math.PI, 100, 0.1d);
        eval(model, x.asin().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test26() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 10, 0.1d);
        RealVar y = model.realVar("y", 0.5d * Math.PI - 0.1d, 5, 0.1d);
        eval(model, x.atan().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test27() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", -5, 1, 0.1d);
        eval(model, x.cosh().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test27a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", -5, 0.9, 0.1d);
        eval(model, x.cosh().eq(y), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test28() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", 0, 0.01, 0.1d);
        eval(model, x.sinh().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test28a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", -0.2, -0.1, 0.1d);
        eval(model, x.sinh().eq(y), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test29() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", 0.7, 0.7, 0.1d);
        eval(model, x.tanh().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test29a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 5, 0.1d);
        RealVar y = model.realVar("y", 1.01, 5, 0.1d);
        eval(model, x.tanh().eq(y), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test30() {
        Model model = new Model();
        RealVar x = model.realVar("x", -5, 5, 0.1d);
        RealVar y = model.realVar("y", -5, 0, 0.1d);
        eval(model, x.acosh().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test30a() {
        Model model = new Model();
        RealVar x = model.realVar("x", -5, 5, 0.1d);
        RealVar y = model.realVar("y", -5, -0.01, 0.1d);
        eval(model, x.acosh().eq(y), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test31() {
        Model model = new Model();
        RealVar x = model.realVar("x", 5, 5, 0.1d);
        RealVar y = model.realVar("y", 2, 2.5, 0.1d);
        eval(model, x.asinh().eq(y), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test31a() {
        Model model = new Model();
        RealVar x = model.realVar("x", 2, 3, 0.1d);
        RealVar y = model.realVar("y", 2, 2.5, 0.1d);
        eval(model, x.asinh().eq(y), 0);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void test32() {
        Model model = new Model();
        RealVar x = model.realVar("x", 0, 1, 0.1d);
        RealVar y = model.realVar("y", 1, 1, 0.1d);
        eval(model, x.atanh().eq(y), 1);
    }

    @Test(groups = "ibex")
    public void testJoao2() throws ContradictionException {
        Model model = new Model();
        RealVar x = model.realVar(360.0, 360.0, 0.0001);
        RealVar y = model.realVar(0.0, 4.0, 0.0001);
        RealVar z = model.realVar(0.0, 360.0, 0.0001);

        // It's working on version 4.10.2 of choco-sover but not 4.10.3-SNAPSHOT
        y.eq(4.0).ibex(.001).post();
        z.eq(x.div(y)).ibex(.001).post();

    }

    @Test(groups = "ibex")
    public void testAR1() {
        Model model = new Model("Environment Generation");
        RealVar x_a = model.realVar("X_a", .1d, 4.d, 1.E-1);
        RealConstraint c1 = model.realIbexGenericConstraint("{0} > 0.8;", x_a);
        RealConstraint c2 = model.realIbexGenericConstraint("{0} < 3 ", x_a);
        RealConstraint c3 = model.realIbexGenericConstraint("{0} > 0.7", x_a);
        RealConstraint c4 = model.realIbexGenericConstraint("{0} < 4", x_a);
        Constraint server = model.and(c1, c2);
        Constraint client = model.and(c3, c4);
        model.not(model.or(model.not(server), client)).post();
        Solver solver = model.getSolver();
        Assert.assertFalse(solver.solve());
    }
    
    @Test(groups = "ibex")
    public void testAR2() {
        Model model = new Model("Environment Generation");
        RealVar x_a = model.realVar("X_a", .1d, 4.d, 1.E-1);
        Constraint c1 = x_a.gt(0.8).ibex(1.E-1);
        Constraint c2 = x_a.lt(3).ibex(1.E-1);
        Constraint c3 = x_a.gt(0.7).ibex(1.E-1);
        Constraint c4 = x_a.lt(4).ibex(1.E-1);
        Constraint server = model.and(c1, c2);
        Constraint client = model.and(c3, c4);
        model.not(model.or(model.not(server), client)).post();
        Solver solver = model.getSolver();
        Assert.assertFalse(solver.solve());
    }

}
