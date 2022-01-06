/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.testng.annotations.Test;

public class RestTrigTest extends RealBase {

    @Test
    public void atan2Test() throws Exception {
        postExpression(x.eq(y.atan2(Math.toRadians(45))));
        model.getSolver().propagate();
        assertBound(x, -1.562942, 1.562942);
        assertBound(y, -100.0, 100.0);
    }

    @Test
    public void atan22Test() throws Exception {
        postExpression(z.eq(Math.toRadians(45)));
        postExpression(x.eq(y.atan2(z)));
        model.getSolver().propagate();
        assertBound(x, -1.562942, 1.562942);
        assertBound(y, -100.0, 100.0);
        assertBound(z, Math.toRadians(45), Math.toRadians(45));
    }

    @Test
    public void cosTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/cos.png
        postExpression(x.ge(-2.0));
        postExpression(x.le(2.0));
        postExpression(y.eq(x.cos()));
        model.getSolver().propagate();
        assertBound(x, -2.0, 2.0);
        assertBound(y, -0.416147, 1.0);
    }

    @Test
    public void sinTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/sin.png
        postExpression(x.ge(-1.0));
        postExpression(x.le(1.0));
        postExpression(y.eq(x.sin()));
        model.getSolver().propagate();
        assertBound(x, -1.0, 1.0);
        assertBound(y, -0.841471, 0.841471);
    }

    @Test
    public void tanTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/tan.png
        postExpression(x.ge(-1.0));
        postExpression(x.le(1.0));
        postExpression(y.eq(x.tan()));
        model.getSolver().propagate();
        assertBound(x, -1.0, 1.0);
        assertBound(y, -1.557408, 1.557408);
    }

    @Test
    public void acosTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/acos.png
        postExpression(x.ge(-1.0));
        postExpression(x.le(1.0));
        postExpression(y.eq(x.acos()));
        model.getSolver().propagate();
        assertBound(x, -1.0, 1.0);
        assertBound(y, 0.0, 3.141593);
    }

    @Test
    public void asinTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/asin.png
        postExpression(x.ge(-1.0));
        postExpression(x.le(1.0));
        postExpression(y.eq(x.asin()));
        model.getSolver().propagate();
        assertBound(x, -1.0, 1.0);
        assertBound(y, -1.570796, 1.570796);
    }

    @Test
    public void atanTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/atan.png
        postExpression(x.ge(-1.0));
        postExpression(x.le(1.0));
        postExpression(y.eq(x.atan()));
        model.getSolver().propagate();
        assertBound(x, -1.0, 1.0);
        assertBound(y, -0.785398, 0.785398);
    }

    @Test
    public void coshTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/cosh.png
        postExpression(x.ge(-2.0));
        postExpression(x.le(2.0));
        postExpression(y.eq(x.cosh()));
        model.getSolver().propagate();
        assertBound(x, -2.0, 2.0);
        assertBound(y, 1.0, 3.762196);
    }

    @Test
    public void sinhTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/sinh.png
        postExpression(x.ge(-2.5));
        postExpression(x.le(2.5));
        postExpression(y.eq(x.sinh()));
        model.getSolver().propagate();
        assertBound(x, -2.5, 2.5);
        assertBound(y, -6.050204, 6.050204);
    }

    @Test
    public void tanhTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/tanh.png
        postExpression(x.ge(-2.0));
        postExpression(x.le(2.0));
        postExpression(y.eq(x.tanh()));
        model.getSolver().propagate();
        assertBound(x, -2.0, 2.0);
        assertBound(y, -0.964028, 0.964028);
    }

    @Test
    public void acoshTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/acosh.png
        postExpression(x.ge(1.0));
        postExpression(x.le(10.0));
        postExpression(y.eq(x.acosh()));
        model.getSolver().propagate();
        assertBound(x, 1.0, 10.0);
        assertBound(y, 0.0, 2.993223);
    }

    @Test
    public void asinhTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/asinh.png
        postExpression(x.ge(-4.0));
        postExpression(x.le(4.0));
        postExpression(y.eq(x.asinh()));
        model.getSolver().propagate();
        assertBound(x, -4.0, 4.0);
        assertBound(y, -2.094713, 2.094713);
    }

    @Test
    public void atanhTest() throws Exception {
        // https://www.medcalc.org/manual/_help/functions/tanh.png
        postExpression(x.ge(-0.5));
        postExpression(x.le(0.5));
        postExpression(y.eq(x.atanh()));
        model.getSolver().propagate();
        assertBound(x, -0.5, 0.5);
        assertBound(y, -0.549306, 0.549306);
    }

}
