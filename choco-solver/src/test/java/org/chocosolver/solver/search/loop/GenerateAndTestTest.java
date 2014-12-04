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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.IntConstraintFactory.arithm;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 21/08/2014
 */
public class GenerateAndTestTest {

    @Test(groups = "10s")
    public void testNQ1() {
        int[] nbSol = {2, 10, 4, 40, 92};
        for (int n = 4; n < 9; n++) {
            Solver solver = new Solver();

            IntVar[] vars = VF.enumeratedArray("Q", n, 1, n, solver);
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    int k = j - i;
                    solver.post(arithm(vars[i], "!=", vars[j]));
                    solver.post(arithm(vars[i], "!=", vars[j], "+", -k));
                    solver.post(arithm(vars[i], "!=", vars[j], "+", k));
                }
            }
            solver.set(ISF.generateAndTest(solver));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbSol[n - 4]);
        }
    }

    @Test(groups = "10s")
    public void testNQ2() {
        int[] nbSol = {2, 10, 4, 40, 92};
        for (int n = 4; n < 9; n++) {
            final Solver solver = new Solver();

            final IntVar[] vars = VF.enumeratedArray("Q", n, 1, n, solver);
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    int k = j - i;
                    solver.post(arithm(vars[i], "!=", vars[j]));
                    solver.post(arithm(vars[i], "!=", vars[j], "+", -k));
                    solver.post(arithm(vars[i], "!=", vars[j], "+", k));
                }
            }
            solver.set(ISF.generateAndTest(solver, ISF.lexico_LB(vars), n*n));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbSol[n - 4]);
        }
    }

}
