package org.chocosolver.solver.constraints.real.power;

import org.chocosolver.solver.exception.ContradictionException;
import org.testng.annotations.Test;

public class RealNegativePowerTest extends RealPowerBase {

    /**
     * With even operations
     */

    @Test
    public void powerEven1Test() throws ContradictionException {
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, -100.0, 100.0);
        assertBound(y, 0.0001, 100.0);
    }

    @Test
    public void powerEven2Test() throws ContradictionException {
        postExpression(x.ge(-10));
        postExpression(x.le(10));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, 0.01, 100.0);
        assertBound(y, -10.0, 10.0);
    }

    @Test
    public void powerEven3Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(x.le(2));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, 0.1, 2.0);
        assertBound(y, 0.25, 100.0);
    }

    @Test
    public void powerEven4Test() throws ContradictionException {
        postExpression(x.ge(2.0));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, 2.0, 100.0);
        assertBound(y, 0.0001, 0.25);
    }

    @Test
    public void powerEven5Test() throws ContradictionException {
        postExpression(x.ge(-2));
        postExpression(x.le(0));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, -2.0, -0.1);
        assertBound(y, 0.25, 100.0);
    }

    @Test
    public void powerEven6Test() throws ContradictionException {
        postExpression(x.ge(-10000));
        postExpression(x.le(-2));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, -100.0, -2.0);
        assertBound(y, 0.0001, 0.25);
    }

    @Test
    public void powerEven7Test() throws ContradictionException {
        postExpression(x.ge(-2));
        postExpression(x.le(2));
        postExpression(y.eq(x.pow(-2)));
        model.getSolver().propagate();
        assertBound(x, -2.0, 2.0);
        assertBound(y, 0.25, 100.0);
    }

    /**
     * With odd operations
     */

    @Test
    public void powerOdd3Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(x.le(2));
        postExpression(y.eq(x.pow(-3)));
        model.getSolver().propagate();
        assertBound(x, 0.215443, 2.0);
        assertBound(y, 0.125, 100.0);
    }

    @Test
    public void powerOdd4Test() throws ContradictionException {
        postExpression(x.ge(2));
        postExpression(y.eq(x.pow(-3)));
        model.getSolver().propagate();
        assertBound(x, 2.0, 100.0);
        assertBound(y, 0.000001, 0.125);
    }

    @Test
    public void powerOdd5Test() throws ContradictionException {
        postExpression(x.ge(-2));
        postExpression(x.le(0));
        postExpression(y.eq(x.pow(-3)));
        model.getSolver().propagate();
        assertBound(x, -2.0, -0.215443);
        assertBound(y, -100.0, -0.125);
    }

    @Test
    public void powerOdd6Test() throws ContradictionException {
        postExpression(x.ge(-10000.0));
        postExpression(x.le(-2));
        postExpression(y.eq(x.pow(-3)));
        model.getSolver().propagate();
        assertBound(x, -100.0, -2.0);
        assertBound(y, -0.125, -0.000001);
    }

}
