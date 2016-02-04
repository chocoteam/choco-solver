/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.Move;
import org.chocosolver.solver.search.loop.MoveLNS;
import org.chocosolver.solver.search.loop.SearchLoop;
import org.chocosolver.solver.search.loop.lns.LNSFactory;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public class LimitsTest {

    @Test(groups="1s", timeOut=60000)
    public void testTime() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long tl = 500;
        SearchMonitorFactory.limitTime(s, tl);
        s.findAllSolutions();
        int tc = (int) (s.getMeasures().getTimeCount() * 1000);
        Assert.assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testThreadTime() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long tl = 500;
        SearchMonitorFactory.limitTime(s, tl);
        s.findAllSolutions();
        int tc = (int) (s.getMeasures().getTimeCount() * 1000);
        Assert.assertTrue(tl - (tl * 10 / 100) <= tc && tc <= tl + (tl * 10 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNode() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long nl = 50;
        SearchMonitorFactory.limitNode(s, nl);
        s.findAllSolutions();
        long nc = s.getMeasures().getNodeCount();
        Assert.assertEquals(nc, nl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBacktrack() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long bl = 50;
        SearchMonitorFactory.limitBacktrack(s, bl);
        s.findAllSolutions();
        long bc = s.getMeasures().getBackTrackCount();
        Assert.assertEquals(bc, bl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFail() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long fl = 50;
        SearchMonitorFactory.limitFail(s, fl);
        s.findAllSolutions();
        long fc = s.getMeasures().getFailCount();
        Assert.assertEquals(fc, fl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSolution() {
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        long sl = 50;
        SearchMonitorFactory.limitSolution(s, sl);
        s.findAllSolutions();
        long sc = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sc, sl);
    }

    @Test(groups="1s", timeOut=60000)
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


    @Test(groups="1s", timeOut=60000)
    public void testGregy4() {
        Solver solver = ProblemMaker.makeNQueenWithBinaryConstraints(12);
        NodeCounter nodeCounter = new NodeCounter(solver, 100);
        INeighbor rnd = LNSFactory.random(solver, solver.retrieveIntVars(true), 30, 0);
        Move currentMove = solver.getSearchLoop().getMove();
        solver.getSearchLoop().setMove(new MoveLNS(currentMove, rnd, new FailCounter(solver, 100)) {
            @Override
            public boolean extend(SearchLoop searchLoop) {
                if (nodeCounter.isMet()) {
                    super.extend(searchLoop);
                }
                return currentMove.extend(searchLoop);
            }

            @Override
            public boolean repair(SearchLoop searchLoop) {
                if (nodeCounter.isMet()) {
                    super.repair(searchLoop);
                } else if (this.solutions > 0
                        // the second condition is only here for intiale calls, when solutions is not already up to date
                        || solver.getMeasures().getSolutionCount() > 0) {
                    // the detection of a new solution can only be met here
                    if (solutions < solver.getMeasures().getSolutionCount()) {
                        assert solutions == solver.getMeasures().getSolutionCount() - 1;
                        solutions++;
                        neighbor.recordSolution();
                    }
                }
                return currentMove.repair(searchLoop);
            }
        });
        solver.findAllSolutions();
        long sc = solver.getMeasures().getSolutionCount();
        Assert.assertEquals(sc, 11);
    }
}
