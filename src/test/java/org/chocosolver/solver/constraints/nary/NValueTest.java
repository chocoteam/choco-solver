/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.BitSet;

public class NValueTest {

	@Test(groups="1s", timeOut=60000)
	public void testAtLeast() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atLeastNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
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

	@Test(groups="1s", timeOut=60000)
	public void testAtMost() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 4, 0, 2, false);
        final IntVar N = model.intVar("N", 2, 3, false);
        model.atMostNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
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


    @Test(groups="1s", timeOut=60000)
    public void testAtLeastFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.atLeastNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showDecisions();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAtLeastFixed2() {
        Model model = new Model();
        for(int i = 0 ; i < 1_000; i++) {
            final IntVar[] XS = model.intVarArray("XS", 3, 0, 80, false);
            final IntVar N = model.intVar("N", 2, 3);
            model.atLeastNValues(XS, N, true).post();
        }
        model.getSolver().showStatistics();
//        model.getSolver().showDecisions();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAtMostFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.atMostNValues(XS, N, false).post();
        model.getSolver().showStatistics();
        model.getSolver().showDecisions();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNValuesFixed() {
        Model model = new Model();
        final IntVar[] XS = model.intVarArray("XS", 6, 0, 5, false);
        final IntVar N = model.intVar("N", 6);
        model.nValues(XS, N).post();
        model.getSolver().showStatistics();
        model.getSolver().showDecisions();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getBackTrackCount(), 0);
    }
}
