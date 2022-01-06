/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.trace.CPProfiler;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.System.out;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 13/09/2016.
 */
public class CPProfilerTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        try (CPProfiler profiler = new CPProfiler(s1.getSolver(), true)) {
            while (s1.getSolver().solve()) ;
            out.println(s1.getSolver().getSolutionCount());
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        CPProfiler profiler = new CPProfiler(s1.getSolver(), true);
        while (s1.getSolver().solve()) ;
        out.println(s1.getSolver().getSolutionCount());
        profiler.close();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() throws IOException {
        Model s1 = ProblemMaker.makeGolombRuler(11);
        s1.getSolver().setLNS(new RandomNeighborhood((IntVar[]) s1.getHook("ticks"), 10, 0));
        CPProfiler profiler = new CPProfiler(s1.getSolver(), true);
        s1.getSolver().limitSolution(9);
        while (s1.getSolver().solve()) ;
        out.println(s1.getSolver().getSolutionCount());
        profiler.close();
    }

}