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
package samples;

import common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.strategy.Assignment;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <a href="http://www.g12.cs.mu.oz.au/mzn/stress_tests/propagation_stress1.mzn">Stress test 2</a>
 * propagation stress
 * Problem is unsatisfiable
 * int: k; %% number of times round the loop
 * int: n; %% number of iterations of change per loop
 * int: m; %% m^2 propagators per change of loop
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/03/12
 */
public class StressTest2 extends AbstractProblem {

    @Option(name = "-k", usage = "number of times round the loop.", required = false)
    int k = 100;

    @Option(name = "-n", usage = "number of iterations of change per loop .", required = false)
    int n = 100;

    @Option(name = "-m", usage = "m^2 propagators per change of loop.", required = false)
    int m = 100;

    IntVar[] x, y;

    @Override
    public void createSolver() {
        solver = new Solver("StressTest2(" + k + "," + n + "," + m + ")");
    }

    @Override
    public void buildModel() {
        y = VariableFactory.boundedArray("y", n + 1, 0, k * n, solver);
        x = VariableFactory.boundedArray("x", m + 1, 0, k * n, solver);

        for (int i = 2; i <= n; i++) {
            solver.post(IntConstraintFactory.arithm(y[i - 1], "-", y[i], "<=", 0));
        }
        for (int i = 1; i <= n; i++) {
            solver.post(IntConstraintFactory.arithm(y[0], "-", y[i], "<=", n - i + 1));
        }
        solver.post(IntConstraintFactory.arithm(y[n], "-", x[0], "<=", 0));

        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j <= m; j++) {
                solver.post(IntConstraintFactory.arithm(x[i], "-", x[j], "<=", 0));
            }
        }
        solver.post(IntConstraintFactory.arithm(x[m], "-", y[0], "<=", -2));
    }

    @Override
    public void configureSearch() {
        IntVar[] vars = ArrayUtils.append(y, x);
        solver.set(new Assignment(new InputOrder(vars), new InDomainMin()));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new StressTest2().execute(args);
    }
}
