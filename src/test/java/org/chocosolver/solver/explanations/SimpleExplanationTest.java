/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30 oct. 2010
 * Time: 17:09:26
 */
public class SimpleExplanationTest {

    /**
     * Refactored by JG to have no static fields (for parallel execution)
     *
     * @param enumerated true -> enumerated domains
     */
    public static void test(boolean enumerated) {
        // initialize
        Model s = new Model();
        // set varriables
        IntVar[] vars = new IntVar[3];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = enumerated ? s.intVar("x" + i, 1, vars.length, false)
                    : s.intVar("x" + i, 1, vars.length + 1, true);
        }
        // post constraints
        s.arithm(vars[0], "<", vars[1]).post();
        s.arithm(vars[1], "<", vars[2]).post();
        s.arithm(vars[0], "!=", vars[1]).post();
        // configure Solver
        s.getSolver().setSearch(inputOrderLBSearch(vars));
        // solve
        s.getSolver().solve();
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 1, "nb sol incorrect");
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        test(true);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        test(false);
    }
}
