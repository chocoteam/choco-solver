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

import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <a href="http://www.g12.cs.mu.oz.au/mzn/stress_tests/propagation_stress0.mzn">Stress test 1</a>
 * <p/>
 * propagation stress
 * Problem is unsatisfiable
 * int: n; % number of variables
 * int: d; % domain size
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/03/12
 */
public class StressTest1 extends AbstractProblem {

    @Option(name = "-n", aliases = "--number", usage = "number of variables.", required = false)
    int n = 100;

    @Option(name = "-d", aliases = "--domain", usage = "domain size.", required = false)
    int d = 100000;

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("StressTest1(" + n + "," + d + ")");
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.boundedArray("v", n, 1, d, solver);
        for (int i = 0; i < n - 1; i++) {
            solver.post(ConstraintFactory.leq(vars[i], vars[i + 1], solver));
        }
        solver.post(ConstraintFactory.lt(vars[n - 1], vars[0], solver));
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        //SearchMonitorFactory.log(solver, true, true);
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new StressTest1().execute(args);
    }
}
