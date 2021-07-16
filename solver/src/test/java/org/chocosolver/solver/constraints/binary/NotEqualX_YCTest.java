/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * User: cprudhom
 * Mail: cprudhom(a)emn.fr
 * Date: 15 juin 2010
 * Since: Choco 2.1.1
 */
public class NotEqualX_YCTest {
    @Test(groups="1s", timeOut=60000)
    public void test1() {
        int n = 2;

        Model s = new Model();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, false);
        }
        s.arithm(vars[0], "!=", vars[1]).post();

        s.getSolver().setSearch(inputOrderLBSearch(vars));
        while (s.getSolver().solve()) ;
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 6, "nb sol incorrect");

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int n = 2;

        Model s = new Model();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, true);
        }
        s.arithm(vars[0], "!=", vars[1]).post();
        s.getSolver().setSearch(inputOrderLBSearch(vars));
//        ChocoLogging.toSolution();
        while (s.getSolver().solve()) ;
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 6, "nb sol incorrect");
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        int n = 2;

        Model s = new Model();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, 0, n, true);
        }
        s.arithm(vars[0], "!=", vars[1]).post();
        s.getSolver().setSearch(inputOrderLBSearch(vars));

        try {
            s.getSolver().propagate();
            vars[0].instantiateTo(1, Null);
            s.getSolver().propagate();
            assertEquals(vars[1].getLB(), 0);
            assertEquals(vars[1].getUB(), 2);
            vars[1].removeValue(2, Null);
            s.getSolver().propagate();
            assertEquals(vars[1].getLB(), 0);
            assertEquals(vars[1].getUB(), 0);
        } catch (ContradictionException e) {
            fail();
        }
    }

}
