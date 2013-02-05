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
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.strategy.Assignment;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <a href="http://www.g12.cs.mu.oz.au/mzn/stress_tests/init_stress1.mzn">Stress test 3</a>
 * <p/>
 * Tests the initialization speed of the engine.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/03/12
 */
public class StressTest4 extends AbstractProblem {

    @Option(name = "-s", usage = "size.", required = false)
    int s = 10000000;

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("StressTest3(" + s + ")");
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.boundedArray("v", 2, 0, s, solver);
        solver.post(IntConstraintFactory.arithm(vars[0], "<", vars[1]));
        solver.post(IntConstraintFactory.arithm(vars[1], "<", vars[0]));
    }

    @Override
    public void configureSearch() {
        solver.set(new Assignment(vars, new InputOrder(vars, solver.getEnvironment()), new InDomainMin()));
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
        new StressTest4().execute(args);
    }
}
