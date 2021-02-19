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

import org.chocosolver.solver.exception.ContradictionException;
import org.testng.annotations.Test;

public class RealExponentTest extends RealBase {

    @Test
    public void realExp1Test() throws ContradictionException {
        postExpression(y.eq(x.pow(r)));
        model.getSolver().propagate();
        assertBound(x, 0.0, 100.0);
        assertBound(r, 0.0, 100.0);
        assertBound(y, 0.0, 100.0);
    }

    @Test
    public void realExp2Test() throws ContradictionException {
        postExpression(r.ge(10.0));
        postExpression(y.eq(x.pow(r)));
        model.getSolver().propagate();
        assertBound(x, 0.0, 1.584893);
        assertBound(r, 10.0, 100.0);
        assertBound(y, 0.0, 100.0);
    }

    @Test
    public void realExp3Test() throws ContradictionException {
        postExpression(r.le(10.0));
        postExpression(y.eq(x.pow(r)));
        model.getSolver().propagate();
        assertBound(x, 0.0, 100.0);
        assertBound(r, 0.0, 10.0);
        assertBound(y, 0.0, 100.0);
    }

    @Test
    public void realExp4Test() throws ContradictionException {
        postExpression(x.ge(8.0));
        postExpression(x.le(10.0));
        postExpression(y.eq(x.pow(r)));
        model.getSolver().propagate();
        assertBound(x, 8.0, 10.0);
        assertBound(r, 0.0, 2.214619);
        assertBound(y, 1.0, 100.0);
    }

}
