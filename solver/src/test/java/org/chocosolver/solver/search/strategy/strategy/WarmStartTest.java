/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/05/2022
 */
public class WarmStartTest {

    @Test(groups = "1s")
    public void test1() {
        Model m = ProblemMaker.makeGolombRuler(8);
        IntVar[] vars = (IntVar[]) m.getHook("diffs");
        Solver s = m.getSolver();
        s.addHint(vars[6], 34);
        while (s.solve()) ;
        Assert.assertEquals(s.getSolutionCount(), 1);
        assertEquals(s.getObjectiveManager().getBestSolutionValue(), 34);
    }

    @Test(groups = "1s")
    public void test2() {
        Model m = ProblemMaker.makeGolombRuler(8);
        IntVar[] vars = (IntVar[]) m.getHook("diffs");
        Solver s = m.getSolver();
        s.addHint(vars[6], 34);
        s.removeHints();
        while (s.solve()) ;
        Assert.assertEquals(s.getSolutionCount(), 8);
        assertEquals(s.getObjectiveManager().getBestSolutionValue(), 34);

    }

    @Test(groups = "1s")
    public void test3() {
        Model m = ProblemMaker.makeGolombRuler(8);
        IntVar[] vars = (IntVar[]) m.getHook("diffs");
        Solver s = m.getSolver();
        s.addHint(vars[6], 34);
        while (s.solve()) ;
        Assert.assertEquals(s.getSolutionCount(), 1);
        assertEquals(s.getObjectiveManager().getBestSolutionValue(), 34);
        s.removeHints();
        s.hardReset();
        while (s.solve()) ;
        Assert.assertEquals(s.getSolutionCount(), 8);
        assertEquals(s.getObjectiveManager().getBestSolutionValue(), 34);
    }

}