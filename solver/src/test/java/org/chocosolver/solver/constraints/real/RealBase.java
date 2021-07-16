/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public abstract class RealBase {

    protected static final double MIN_LIM = -100.0;
    protected static final double MAX_LIM = 100.0;
    protected static final double PRECISION = 0.000_001;

    protected Model model;

    protected RealVar x;

    protected RealVar y;

    protected RealVar z;

    protected RealVar v;

    protected RealVar r;

    protected BoolVar w;

    @BeforeMethod
    protected void before() {
        model = new Model();
        x = model.realVar("x", MIN_LIM, MAX_LIM, PRECISION);
        y = model.realVar("y", MIN_LIM, MAX_LIM, PRECISION);
        z = model.realVar("z", MIN_LIM, MAX_LIM, PRECISION);
        v = model.realVar("v", MIN_LIM, MAX_LIM, PRECISION);
        r = model.realVar("r", 0.0, MAX_LIM, PRECISION);
        w = model.boolVar("w");
    }

    protected void assertBound(RealVar var, double lb, double ub) {
        Assert.assertEquals(var.getLB(), lb, PRECISION);
        Assert.assertEquals(var.getUB(), ub, PRECISION);
    }

    protected void postExpression(CReExpression expression) {
        expression.ibex(PRECISION).post();
    }

    protected ReExpression getExpression(CReExpression expression) {
        return expression.ibex(PRECISION).reify().eq(1);
    }
}
