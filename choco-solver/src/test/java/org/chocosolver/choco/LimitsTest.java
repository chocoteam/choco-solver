/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public class LimitsTest {

    protected static Solver modelit() {
        Solver solver = new Solver();
        int n = 12;
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = IntConstraintFactory.arithm(vars[i], "!=", vars[j]);
                solver.post(neq);
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        return solver;
    }


    @Test(groups = "1s")
    public void testTime() {
        Solver s = modelit();
        long tl = 500;
        SearchMonitorFactory.limitTime(s, tl);
        s.findAllSolutions();
		int tc = (int)(s.getMeasures().getTimeCount()*1000);
        Assert.assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl + " vs. " + tc);
    }

    @Test(groups = "1s")
    public void testThreadTime() {
        Solver s = modelit();
        long tl = 500;
        SearchMonitorFactory.limitThreadTime(s, tl);
        s.findAllSolutions();
        int tc = (int)(s.getMeasures().getTimeCount()*1000);
        Assert.assertTrue(tl - (tl * 10 / 100) <= tc && tc <= tl + (tl * 10 / 100), tl + " vs. " + tc);
    }

    @Test(groups = "1s")
    public void testNode() {
        Solver s = modelit();
        long nl = 50;
        SearchMonitorFactory.limitNode(s, nl);
        s.findAllSolutions();
        long nc = s.getMeasures().getNodeCount();
        Assert.assertEquals(nc, nl);
    }

    @Test(groups = "1s")
    public void testBacktrack() {
        Solver s = modelit();
        long bl = 50;
        SearchMonitorFactory.limitBacktrack(s, bl);
        s.findAllSolutions();
        long bc = s.getMeasures().getBackTrackCount();
        Assert.assertEquals(bc, bl);
    }

    @Test(groups = "1s")
    public void testFail() {
        Solver s = modelit();
        long fl = 50;
        SearchMonitorFactory.limitFail(s, fl);
        s.findAllSolutions();
        long fc = s.getMeasures().getFailCount();
        Assert.assertEquals(fc, fl);
    }

    @Test(groups = "1s")
    public void testSolution() {
        Solver s = modelit();
        long sl = 50;
        SearchMonitorFactory.limitSolution(s, sl);
        s.findAllSolutions();
        long sc = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sc, sl);
    }

    @Test(groups = "1s")
    public void durationTest() {
        long d = SMF.convertInMilliseconds("0.50s");
        Assert.assertEquals(d, 500);
        d += SMF.convertInMilliseconds("30s");
        Assert.assertEquals(d, 30500);
        d += SMF.convertInMilliseconds("30m");
        Assert.assertEquals(d, 1830500);
        d += SMF.convertInMilliseconds("12h");
        Assert.assertEquals(d, 45030500);
        d += SMF.convertInMilliseconds("2d");
        Assert.assertEquals(d, 217830500);

        long t = SMF.convertInMilliseconds("2d12h30m30.5s");
        Assert.assertEquals(t, d);

        d = SMF.convertInMilliseconds("71s");
        Assert.assertEquals(d, 71000);
    }

}
