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
package solver.search.restart;

import org.testng.Assert;
import org.testng.annotations.Test;
import samples.nqueen.NQueenBinary;
import solver.Solver;
import solver.search.limits.LimitBox;
import solver.search.loop.monitors.SearchMonitorFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    static Solver buildQ(int n) {
        NQueenBinary nq = new NQueenBinary();
        nq.readArgs("-q", "" + n);
        nq.buildModel();
        nq.configureSolver();
        return nq.getSolver();
    }

    @Test(groups = "1s")
    public void testGeometricalRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.restart(solver, RestartFactory.geometrical(2, 1.2),
                LimitBox.nodeLimit(solver, 2), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 12);
    }

    @Test(groups = "1s")
    public void testLubyRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.restart(solver, RestartFactory.luby(2, 2),
                LimitBox.nodeLimit(solver, 2), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 11);
    }


    public final static int[] LUBY_2 = {1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,1,1,2,1,1,2,4,1,1,2,1,1,2,4,8,16};

	public final static int[] LUBY_3 = {1,1,1,3,1,1,1,3,1,1,1,3,9,
		1,1,1,3,1,1,1,3,1,1,1,3,9,
		1,1,1,3,1,1,1,3,1,1,1,3,9,27};

	public final static int[] LUBY_4 = {1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,
		1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,1,1,1,1,4,16,64
	};

	public final static int[] GEOMETRIC_1_3 = {1,2,2,3,3,4,5,7,9,11,14,18,24,31,40};

	private void checkRestart(AbstractRestartStrategy r,double factor,int[] expected) {
		r.setGeometricalFactor(factor);
		int[] computed = r.getSequenceExample(expected.length);
		Assert.assertEquals(computed, expected);
	}

	@Test
	public void testRestartStrategy() {
		AbstractRestartStrategy r = new LubyRestartStrategy(1,2);
		checkRestart(r, 2, LUBY_2);
		checkRestart(r, 3, LUBY_3);
		checkRestart(r, 4, LUBY_4);
		r = new GeometricalRestartStrategy(1,1.3);
		checkRestart(r, 1.3, GEOMETRIC_1_3);
	}

}
