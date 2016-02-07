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
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveLNS;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.loop.lns.LNSFactory.random;
import static org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory.*;
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
        limitTime(s, tl);
        while (s.solve()) ;
        int tc = (int) (s.getMeasures().getTimeCount() * 1000);
        assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testThreadTime() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long tl = 500;
        limitTime(s, tl);
        while (s.solve()) ;
        int tc = (int) (s.getMeasures().getTimeCount() * 1000);
        assertTrue(tl - (tl * 10 / 100) <= tc && tc <= tl + (tl * 10 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNode() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long nl = 50;
        limitNode(s, nl);
        while (s.solve()) ;
        long nc = s.getMeasures().getNodeCount();
        assertEquals(nc, nl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBacktrack() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long bl = 50;
        limitBacktrack(s, bl);
        while (s.solve()) ;
        long bc = s.getMeasures().getBackTrackCount();
        assertEquals(bc, bl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFail() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long fl = 50;
        limitFail(s, fl);
        while (s.solve()) ;
        long fc = s.getMeasures().getFailCount();
        assertEquals(fc, fl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSolution() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long sl = 50;
        limitSolution(s, sl);
        while (s.solve()) ;
        long sc = s.getMeasures().getSolutionCount();
        assertEquals(sc, sl);
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
        Model model = makeNQueenWithBinaryConstraints(12);
        NodeCounter nodeCounter = new NodeCounter(model, 100);
        INeighbor rnd = random(model, model.retrieveIntVars(true), 30, 0);
        Move currentMove = model.getResolver().getMove();
        model.getResolver().set(new MoveLNS(currentMove, rnd, new FailCounter(model, 100)) {
            @Override
            public boolean extend(Resolver resolver) {
                if (nodeCounter.isMet()) {
                    super.extend(resolver);
                }
                return currentMove.extend(resolver);
            }

            @Override
            public boolean repair(Resolver resolver) {
                if (nodeCounter.isMet()) {
                    super.repair(resolver);
                } else if (this.solutions > 0
                        // the second condition is only here for intiale calls, when solutions is not already up to date
                        || model.getMeasures().getSolutionCount() > 0) {
                    // the detection of a new solution can only be met here
                    if (solutions < model.getMeasures().getSolutionCount()) {
                        assert solutions == model.getMeasures().getSolutionCount() - 1;
                        solutions++;
                        neighbor.recordSolution();
                    }
                }
                return currentMove.repair(resolver);
            }
        });
        while (model.solve()) ;
        long sc = model.getMeasures().getSolutionCount();
        assertEquals(sc, 11);
    }
}
