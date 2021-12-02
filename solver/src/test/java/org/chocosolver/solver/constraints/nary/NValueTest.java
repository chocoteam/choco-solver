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
 * @author Jean-Guillaume Fages
 * @since 17/09/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;

public class NValueTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testAtLeast() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atLeastNValues(XS, N, false).post();


        final BitSet values = new BitSet(3);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            values.clear();
            for (IntVar v : XS) {
                if (!v.isInstantiated()) {
                    throw new UnsupportedOperationException();
                }
                values.set(v.getValue());
            }
            if (!N.isInstantiated()) {
                throw new UnsupportedOperationException();
            }
            if (values.cardinality() < N.getValue()) {
                throw new UnsupportedOperationException();
            }
        });
        while (model.getSolver().solve()) ;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAtMost() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atMostNValues(XS, N, false).post();


        final BitSet values = new BitSet(3);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            values.clear();
            for (IntVar v : XS) {
                if (!v.isInstantiated()) {
                    throw new UnsupportedOperationException();
                }
                values.set(v.getValue());
            }
            if (!N.isInstantiated()) {
                throw new UnsupportedOperationException();
            }
            if (values.cardinality() > N.getValue()) {
                throw new UnsupportedOperationException();
            }
        });
        while (model.getSolver().solve()) ;
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testAtLeastFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.atLeastNValues(XS, N, false).post();


        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAtLeastFixed2() {
        Model model = new Model();
        for (int i = 0; i < 1_000; i++) {
            final IntVar[] XS = model.intVarArray("XS", 3, 0, 80, false);
            final IntVar N = model.intVar("N", 2, 3);
            model.atLeastNValues(XS, N, true).post();
        }

//
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAtMostFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.atMostNValues(XS, N, false).post();


        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNValuesFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.nValues(XS, N).post();


        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testNValuesOrderSearch() {
        int nVars = 5;
        int domainSize = 10;
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", nVars, 0, domainSize, false);
        IntVar n = model.intVar("n", 1, nVars, true);
        model.nValues(x, n).post();
        model.getSolver().setSearch(Search.inputOrderLBSearch(ArrayUtils.append(new IntVar[]{n}, x)));
        while (model.getSolver().solve()) ;

        Model model2 = new Model();
        IntVar[] x2 = model2.intVarArray("x2", nVars, 0, domainSize, false);
        IntVar n2 = model2.intVar("n2", 1, nVars, true);
        model2.nValues(x2, n2).post();
        model2.getSolver().setSearch(Search.inputOrderLBSearch(ArrayUtils.concat(x2, n2)));
        while (model2.getSolver().solve()) ;

        Model model3 = new Model();
        IntVar[] x3 = model3.intVarArray("x3", nVars, 0, domainSize, false);
        IntVar n3 = model3.intVar("n3", 1, nVars, true);
        model3.nValues(x3, n3).post();
        while (model3.getSolver().solve()) ;

        Assert.assertEquals(model.getSolver().getSolutionCount(), model2.getSolver().getSolutionCount());
        Assert.assertEquals(model.getSolver().getSolutionCount(), model3.getSolver().getSolutionCount());
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testNValuesRnd() {
        for (int i = 0; i < 50; i++) {
            Model model = new Model();
            final IntVar[] XS = model.intVarArray("XS", 6, 0, 3, false);
            final IntVar N = model.intVar("N", 3, 4);
            model.nValues(XS, N).post();
            model.getSolver().setSearch(new FullyRandom(ArrayUtils.append(XS, new IntVar[]{N}), i));
            model.getSolver().findAllSolutions();
            Assert.assertEquals(model.getSolver().getSolutionCount(), 3720, "Seed:" + i);
        }
    }
}
