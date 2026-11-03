/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.util.tools.TimeUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.chocosolver.util.ProblemMaker.makeNQueenWithBinaryConstraints;
import static org.testng.Assert.assertEquals;
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

    @DataProvider
    public Object[][] durations() {
        return new Object[][]{
                {"0s", 0},
                {"0", 0},
                {"50", 50},
                {" 2h 30m ", 9_000_000},
                {"50ms", 50},
                {"0.50s", 500},
                {"30s", 30_000},
                {"71s", 71_000},
                {"1m30s", 90_000},
                {"1m500ms", 60_500},
                {"30m", 1_800_000},
                {"12h", 43_200_000},
                {"2d", 172_800_000},
                {"2d12h30m30.5s", 217_830_500},
        };
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "durations")
    public void durationTest(String str, long dur) {
        long d = TimeUtils.convertInMilliseconds(str);
        Assert.assertEquals(d, dur);
    }

    @DataProvider
    public Object[][] invalidDurations() {
        return new Object[][]{{"1.5m"}, {"0.5h"}, {"2min"}, {"3x"}, {""}, {"s"}, {"30s2m"}, {"-5s"}, {"1.5"}, {"10 seconds"}};
    }

    @Test(groups = "1s", dataProvider = "invalidDurations", expectedExceptions = IllegalArgumentException.class)
    public void invalidDurationTest(String str) {
        TimeUtils.convertInMilliseconds(str);
    }
}
