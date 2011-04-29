/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public class LimitsTest {

    protected static Solver modelit(int k) {
        int n = (2 * k);
        int m = n - 1;
        int min = 1;
        int max = k - 2;

        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
        }

        int i;
        for (i = 0; i < (n / 2) - 1; i++) {
            s.post(ConstraintFactory.neq(vars[i], vars[i + 1], s));
            int j = (n / 2);
            s.post(ConstraintFactory.neq(vars[i + j], vars[i + j + 1], s));
        }
        s.post(ConstraintFactory.lt(vars[(n / 2) - 1], vars[n / 2], s));

        s.set(StrategyFactory.firstFailInDomainMin(vars, s.getEnvironment()));
        return s;
    }


    @Test(groups = "1s")
    public void testTime() {
        Solver s = modelit(7);
        long tl = 500;
        s.getSearchLoop().getLimitsFactory().setThreadTimeLimit(tl);
        s.findAllSolutions();
        long tc = s.getMeasures().getTimeCount();
        Assert.assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100));
    }

    @Test(groups = "1s")
    public void testNode() {
        Solver s = modelit(6);
        long nl = 500;
        s.getSearchLoop().getLimitsFactory().setNodeLimit(nl);
        s.findAllSolutions();
        long nc = s.getMeasures().getNodeCount();
        Assert.assertEquals(nc, nl);
    }

    @Test(groups = "1s")
    public void testBacktrack() {
        Solver s = modelit(6);
        long bl = 500;
        s.getSearchLoop().getLimitsFactory().setBacktrackLimit(bl);
        s.findAllSolutions();
        long bc = s.getMeasures().getBackTrackCount();
        Assert.assertEquals(bc, bl);
    }

    @Test(groups = "1s")
    public void testFail() {
        Solver s = modelit(6);
        long fl = 500;
        s.getSearchLoop().getLimitsFactory().setFailLimit(fl);
        s.findAllSolutions();
        long fc = s.getMeasures().getFailCount();
        Assert.assertEquals(fc, fl);
    }

    @Test(groups = "1s")
    public void testSolution() {
        Solver s = modelit(6);
        long sl = 5;
        s.getSearchLoop().getLimitsFactory().setSolutionLimit(sl);
        s.findAllSolutions();
        long sc = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sc, sl);
    }

}
