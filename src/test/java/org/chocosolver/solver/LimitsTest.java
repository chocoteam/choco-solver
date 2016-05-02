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

import org.chocosolver.util.tools.TimeUtils;
import org.testng.Assert;
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
        while (s.solve()) ;
        int tc = (int) (s.getSolver().getTimeCount() * 1000);
        assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl + " vs. " + tc);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNode() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long nl = 50;
        s.getSolver().limitNode(nl);
        while (s.solve()) ;
        long nc = s.getSolver().getNodeCount();
        assertEquals(nc, nl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBacktrack() {
        for(long bl=10;bl<200;bl+=7) {
            Model s = makeNQueenWithBinaryConstraints(12);
            s.getSolver().limitBacktrack(bl);
            while (s.solve()) ;
            long bc = s.getSolver().getBackTrackCount();
            assertTrue(bc <= bl + s.getSolver().getNodeCount());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testFail() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long fl = 50;
        s.getSolver().limitFail(fl);
        while (s.solve()) ;
        long fc = s.getSolver().getFailCount();
        assertEquals(fc, fl);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSolution() {
        Model s = makeNQueenWithBinaryConstraints(12);
        long sl = 50;
        s.getSolver().limitSolution(sl);
        while (s.solve()) ;
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
