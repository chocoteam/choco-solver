/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/09/2020
 */
public class FindAndProveTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test() {
        Model choco = new Model();

        // obvious problem to maximize a+b with no other constraint.
        IntVar a = choco.intVar("a", 0, 10);
        IntVar b = choco.intVar("b", 0, 10);
        IntVar sum = a.add(b).intVar();


        IntStrategy badHeuristic = Search.inputOrderLBSearch(a, b);
        IntStrategy goodHeuristic = Search.inputOrderUBSearch(a, b);
        StrategiesSequencer<IntVar> seq = new StrategiesSequencer<>(goodHeuristic, badHeuristic);

        FindAndProve<IntVar> fap = new FindAndProve<>(new IntVar[]{a, b}, badHeuristic, seq);

        Solver solver = choco.getSolver();
        solver.setSearch(fap);
        Solution sol = solver.findOptimalSolution(sum, true);
        Assert.assertEquals(sol.getIntVal(a), 10);
        Assert.assertEquals(sol.getIntVal(b), 10);
    }

}