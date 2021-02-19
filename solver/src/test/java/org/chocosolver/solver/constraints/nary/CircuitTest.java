/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 12/06/12
 * Time: 21:29
 */

package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

public class CircuitTest {

    @Test(groups="1s", timeOut=60000)
    public static void test1() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 20, true);
        model.circuit(x).post();
        model.getSolver().solve();
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test2() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 10, 0, 10, false);
        model.circuit(x).post();
        model.getSolver().solve();
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test3() {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", 5, 0, 4, true);
        IntVar[] y = model.intVarArray("y", 5, 5, 9, true);
        IntVar[] vars = append(x, y);
        model.circuit(vars).post();
        model.getSolver().solve();
        assertEquals(0, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public static void test4() {
        for (int n = 2; n < 8; n++) {
            Model model = new Model();
            IntVar[] x = model.intVarArray("x", n, 0, n - 1, true);
            model.circuit(x).post();
            while (model.getSolver().solve()) ;
            assertEquals(factorial(n - 1), model.getSolver().getSolutionCount());
        }
    }

    private static int factorial(int n) {
        if (n == 1) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }
}
