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
import solver.constraints.nary.Count;
import solver.constraints.nary.Sum;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrArityP;
import solver.propagation.engines.comparators.IncrOrderV;
import solver.propagation.engines.comparators.Seq;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.propagation.engines.group.Group;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class MagicSeries extends AbstractProblem {

    @Option(name = "-s", usage = "Magic series size.", required = true)
    int n;
    IntVar[] vars;

    @Override
    public void buildModel() {
        solver = new Solver();
        vars = new IntVar[n];

        vars = VariableFactory.enumeratedArray("var", n, 0, n - 1, solver);

        for (int i = 0; i < n; i++) {
            solver.post(new Count(i, vars, Count.Relop.EQ, vars[i], solver));
        }
        solver.post(Sum.eq(vars, n, solver)); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        solver.post(Sum.eq(vs2, coeff2, n, solver)); // cstr redundant 1
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));
        // default group
        solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicate.TRUE,
                        new Seq(
                                IncrArityP.get(),
                                new IncrOrderV(vars)
                        ),
                        Policy.FIXPOINT
                ));
    }

    @Override
    public void solve() {
        SearchMonitorFactory.log(solver, true, false);
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new MagicSeries().execute(args);
    }
}
