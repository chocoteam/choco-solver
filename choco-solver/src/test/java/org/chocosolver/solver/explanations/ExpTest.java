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

/**
 * @author Jean-Guillaume Fages
 * @since 07/10/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExpTest {

	@Test(groups = "1s")
	public void test01() {
		int n = 6;
		int m = 10;
		Solver s1 = test(n,m,1);
		Solver s2 = test(n,m,2);
		Solver s3 = test(n,m,3);
		Assert.assertEquals(s1.getMeasures().getSolutionCount(),s2.getMeasures().getSolutionCount());
		Assert.assertEquals(s1.getMeasures().getSolutionCount(),s3.getMeasures().getSolutionCount());
		Assert.assertTrue(s1.getMeasures().getNodeCount() >= s2.getMeasures().getNodeCount());
		Assert.assertTrue(s2.getMeasures().getNodeCount() >= s3.getMeasures().getNodeCount());
	}

	private Solver test(int n, int m, int expMode) {
        // infeasible problem
        Solver s = new Solver();
        IntVar[] x = VF.boundedArray("x", n, 0, m, s);
        s.post(ICF.alldifferent(x, "NEQS"));
        s.post(ICF.arithm(x[n - 2], "=", x[n - 1]));
        // explanations
        if (expMode == 2) {
            ExplanationFactory.CBJ.plugin(s, false, false);
        } else if (expMode == 3) {
            ExplanationFactory.DBT.plugin(s, false, false);
        }
        // logging and solution
        Chatterbox.showStatistics(s);
        Chatterbox.showSolutions(s);
        s.findAllSolutions();
        return s;
    }
}
