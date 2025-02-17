/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.TimeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.util.ProblemMaker.makeNQueenWithBinaryConstraints;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public class LimitsTest {

    @Test(groups="1s", timeOut=60000)
    public void testTime() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long tl = 500;
        s.getSolver().limitTime(tl);
        while (s.getSolver().solve()) ;
        int tc = (int) (s.getSolver().getTimeCount() * 1000);
        assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testTime2() throws SolverException {
        Model model = new Model();

        IntVar v7 = model.intVar("@v7", IntVar.MIN_INT_BOUND, IntVar.MAX_INT_BOUND, true);

        model.post(
            model.arithm(v7, ">", v7),
            model.arithm(v7, "<", v7)
        );
        // TODO : such a simple case should be detected within the constraint declaration
        // TODO : this test might need to be changed if better model analysis is done during model declaration

        Solver solver = model.getSolver();
        solver.limitTime(250);

        long start = System.currentTimeMillis();
        boolean solved = solver.solve();
        long took = System.currentTimeMillis() - start;

        assertFalse(solved);
        assertEquals(solver.getNodeCount(), 1);
        assertEquals(solver.getBackTrackCount(), 0);
        assertEquals(solver.getFailCount(), 0);
        assertEquals(solver.getSolutionCount(), 0);
        assertTrue(solver.isStopCriterionMet());
        assertTrue(took <= 1000); // less than 1 second
    }

    @Test(groups="1s", timeOut=60000)
    public void testNode() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long nl = 50;
        s.getSolver().limitNode(nl);
        while (s.getSolver().solve()) ;
        long nc = s.getSolver().getNodeCount();
        assertEquals(nc, nl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBacktrack() {
        for(long bl=10;bl<200;bl+=7) {
            Model s = makeNQueenWithBinaryConstraints(12);
            s.getSolver().limitBacktrack(bl);
            while (s.getSolver().solve()) ;
            long bc = s.getSolver().getBackTrackCount();
            assertTrue(bc <= bl + s.getSolver().getNodeCount());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testFail() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long fl = 50;
        s.getSolver().limitFail(fl);
        while (s.getSolver().solve()) ;
        long fc = s.getSolver().getFailCount();
        assertEquals(fc, fl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSolution() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long sl = 50;
        s.getSolver().limitSolution(sl);
        while (s.getSolver().solve()) ;
        long sc = s.getSolver().getSolutionCount();
        assertEquals(sc, sl);
    }

    @Test(groups="1s", timeOut=60000)
    public void durationTest() {
        long d = TimeUtils.convertInMilliseconds("0.50s");
        Assert.assertEquals(d, 500);
        d += TimeUtils.convertInMilliseconds("30s");
        Assert.assertEquals(d, 30500);
        d += TimeUtils.convertInMilliseconds("30m");
        Assert.assertEquals(d, 1830500);
        d += TimeUtils.convertInMilliseconds("12h");
        Assert.assertEquals(d, 45030500);
        d += TimeUtils.convertInMilliseconds("2d");
        Assert.assertEquals(d, 217830500);

        long t = TimeUtils.convertInMilliseconds("2d12h30m30.5s");
        Assert.assertEquals(t, d);

        d = TimeUtils.convertInMilliseconds("71s");
        Assert.assertEquals(d, 71000);
    }
}
