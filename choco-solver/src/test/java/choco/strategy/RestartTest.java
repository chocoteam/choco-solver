/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package choco.strategy;

import memory.Environments;
import org.testng.annotations.Test;
import solver.Solver;
import solver.SolverProperties;
import solver.constraints.ICF;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/04/2014
 */
public class RestartTest {

    @Test
    public void test1() {

        for (int j = 1; j < 5; j++) {
            int n = 200;
            Solver solver = new Solver(Environments.COPY.make(), "Test", SolverProperties.DEFAULT);
            IntVar[] X = VF.enumeratedArray("X", n, 1, n, solver);
            IntVar[] Y = VF.enumeratedArray("Y", n, n + 1, 2 * (n + 1), solver);
            solver.post(ICF.alldifferent(X));
            for (int i = 0; i < n; i++) {
                solver.post(ICF.arithm(Y[i], "=", X[i], "+", n));
            }
			SMF.restartAfterEachSolution(solver);
            solver.set(ISF.lexico_LB(X));
//            SMF.log(solver, false, false);
            SMF.limitSolution(solver, 100);
            solver.findAllSolutions();
            //System.out.printf("%d - %.3fms \n", n, solver.getMeasures().getTimeCount());
        }
    }
}
