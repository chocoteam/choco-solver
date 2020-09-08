package org.chocosolver.solver.constraints.real.power;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.relational.CReExpression;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.RealVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public abstract class RealPowerBase {

    protected static final double MIN_LIM = -100.0;
    protected static final double MAX_LIM = 100.0;
    protected static final double PRECISION = 0.000_001;

    protected Model model;

    protected RealVar x;

    protected RealVar y;

    protected RealVar z;

    protected RealVar v;

    protected RealVar e;

    protected BoolVar w;

    @BeforeMethod
    protected void before() {
        model = new Model();
        x = model.realVar("x", MIN_LIM, MAX_LIM, PRECISION);
        y = model.realVar("y", MIN_LIM, MAX_LIM, PRECISION);
        z = model.realVar("z", MIN_LIM, MAX_LIM, PRECISION);
        v = model.realVar("v", MIN_LIM, MAX_LIM, PRECISION);
        e = model.realVar("e", 0.0, MAX_LIM, PRECISION);
        w = model.boolVar("w");
    }

    protected void assertBound(RealVar var, double lb, double ub) {
        Assert.assertEquals(var.getLB(), lb, PRECISION);
        Assert.assertEquals(var.getUB(), ub, PRECISION);
    }

    protected void postGreaterEqual(RealVar var, double val) {
        var.ge(val).ibex(PRECISION).post();
    }

    protected void postLessEqual(RealVar var, double val) {
        var.le(val).ibex(PRECISION).post();
    }

    protected void postEqual(RealVar var, double val) {
        var.eq(val).ibex(PRECISION).post();
    }

    protected void postExpression(CReExpression expression) {
        expression.ibex(PRECISION).post();
    }

    protected ReExpression getExpression(CReExpression expression) {
        return expression.ibex(PRECISION).reify().eq(1);
    }
}
