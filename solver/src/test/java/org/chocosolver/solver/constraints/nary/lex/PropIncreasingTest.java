/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.lex;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/01/2022
 */
public class PropIncreasingTest {

    @Test(groups = "1s")
    public void test1() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        IntVar z = m.intVar("z", 1, 3);
        m.increasing(new IntVar[]{x, y, z}, 0).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 10);
    }

    @Test(groups = "1s")
    public void test2() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        IntVar z = m.intVar("z", 1, 3);
        m.increasing(new IntVar[]{x, y, z}, 1).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test3() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 5);
        IntVar y = m.intVar("y", 1, 5);
        IntVar z = m.intVar("z", 1, 5);
        m.increasing(new IntVar[]{x, y, z}, 2).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test10() {
        Model m = new Model();
        IntVar[] x = m.intVarArray("x", 10, 1, 10);
        m.increasing(x, 0).post();
        Solver s = m.getSolver();
        s.setSearch(new FullyRandom(x, 97L));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 92378);
        Assert.assertEquals(s.getNodeCount(), 184755);
    }

    @Test(groups = "1s")
    public void test1d() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        IntVar z = m.intVar("z", 1, 3);
        m.decreasing(new IntVar[]{x, y, z}, 0).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 10);
    }

    @Test(groups = "1s")
    public void test2d() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        IntVar z = m.intVar("z", 1, 3);
        m.decreasing(new IntVar[]{x, y, z}, 1).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test3d() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 5);
        IntVar y = m.intVar("y", 1, 5);
        IntVar z = m.intVar("z", 1, 5);
        m.decreasing(new IntVar[]{x, y, z}, 2).post();
        Solver s = m.getSolver();
        s.setSearch(Search.inputOrderLBSearch(z, y, x));
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test040() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        BoolVar b = m.increasing(new IntVar[]{x, y}, 0).reify();
        m.arithm(b, "=", 1).post();
        Solver s = m.getSolver();
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 6);
    }

    @Test(groups = "1s")
     public void test041() {
         Model m = new Model();
         IntVar x = m.intVar("x", 1, 3);
         IntVar y = m.intVar("y", 1, 3);
         BoolVar b = m.increasing(new IntVar[]{x, y}, 1).reify();
         m.arithm(b, "=", 1).post();
         Solver s = m.getSolver();
         s.findAllSolutions();
         Assert.assertEquals(s.getSolutionCount(), 3);
     }

     @Test(groups = "1s")
     public void test050() {
         Model m = new Model();
         IntVar x = m.intVar("x", 1, 3);
         IntVar y = m.intVar("y", 1, 3);
         BoolVar b = m.increasing(new IntVar[]{x, y}, 0).reify();
         m.arithm(b, "=", 0).post();
         Solver s = m.getSolver();
         s.findAllSolutions();
         Assert.assertEquals(s.getSolutionCount(), 3);
     }

    @Test(groups = "1s")
    public void test051() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3);
        IntVar y = m.intVar("y", 1, 3);
        BoolVar b = m.increasing(new IntVar[]{x, y}, 1).reify();
        m.arithm(b, "=", 0).post();
        Solver s = m.getSolver();
        s.findAllSolutions();
        Assert.assertEquals(s.getSolutionCount(), 6);
    }

}