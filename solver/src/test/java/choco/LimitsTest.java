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
import samples.nqueen.NQueenBinary;
import solver.Solver;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public class LimitsTest {

    protected static Solver modelit() {
        NQueenBinary pb =new NQueenBinary();
        pb.readArgs("-q", "12");
        pb.createSolver();
        pb.buildModel();
        pb.configureSearch();
        return pb.getSolver();
    }


    @Test(groups = "1s")
    public void testTime() {
        Solver s = modelit();
        long tl = 500;
        s.getSearchLoop().getLimitsBox().setTimeLimit(tl);
        s.findAllSolutions();
        float tc = s.getMeasures().getTimeCount();
        Assert.assertTrue(tl - (tl * 5 / 100) <= tc && tc <= tl + (tl * 5 / 100), tl+" vs. "+ tc);
    }

    @Test(groups = "1s")
    public void testThreadTime() {
        Solver s = modelit();
        long tl = 500;
        s.getSearchLoop().getLimitsBox().setThreadTimeLimit(tl);
        s.findAllSolutions();
        float tc = s.getMeasures().getTimeCount();
        Assert.assertTrue(tl - (tl * 10 / 100) <= tc && tc <= tl + (tl * 10 / 100), tl+" vs. "+ tc);
    }

    @Test(groups = "1s")
    public void testNode() {
        Solver s = modelit();
        long nl = 50;
        s.getSearchLoop().getLimitsBox().setNodeLimit(nl);
        s.findAllSolutions();
        long nc = s.getMeasures().getNodeCount();
        Assert.assertEquals(nc, nl);
    }

    @Test(groups = "1s")
    public void testBacktrack() {
        Solver s = modelit();
        long bl = 50;
        s.getSearchLoop().getLimitsBox().setBacktrackLimit(bl);
        s.findAllSolutions();
        long bc = s.getMeasures().getBackTrackCount();
        Assert.assertEquals(bc, bl);
    }

    @Test(groups = "1s")
    public void testFail() {
        Solver s = modelit();
        long fl = 50;
        s.getSearchLoop().getLimitsBox().setFailLimit(fl);
        s.findAllSolutions();
        long fc = s.getMeasures().getFailCount();
        Assert.assertEquals(fc, fl);
    }

    @Test(groups = "1s")
    public void testSolution() {
        Solver s = modelit();
        long sl = 50;
        s.getSearchLoop().getLimitsBox().setSolutionLimit(sl);
        s.findAllSolutions();
        long sc = s.getMeasures().getSolutionCount();
        Assert.assertEquals(sc, sl);
    }

}
