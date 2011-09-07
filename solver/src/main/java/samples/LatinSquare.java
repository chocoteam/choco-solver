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
import solver.constraints.nary.GlobalCardinality;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.predicate.MemberV;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;
import java.util.HashSet;

/**
 * <a href="http://en.wikipedia.org/wiki/Latin_square">wikipedia</a>:<br/>
 * "A Latin square is an n ? n array filled with n different Latin letters,
 * each occurring exactly once in each row and exactly once in each column"
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class LatinSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Latin square size.", required = false)
    int m = 10;
    IntVar[] vars;

    @Override
    public void buildModel() {
        solver = new Solver("Latin square " + m);

        vars = new IntVar[m * m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                vars[i * m + j] = VariableFactory.enumerated("C" + i + "_" + j, 0, m - 1, solver);
            }
        }

        // Constraints
        for (int i = 0; i < m; i++) {
            int[] low = new int[m];
            int[] up = new int[m];
            IntVar[] row = new IntVar[m];
            IntVar[] col = new IntVar[m];
            for (int x = 0; x < m; x++) {
                row[x] = vars[i * m + x];
                col[x] = vars[x * m + i];
                low[x] = 0;
                up[x] = 1;
            }
            solver.post(GlobalCardinality.make(row, low, up, 0, GlobalCardinality.Consistency.BC, solver));
            solver.post(GlobalCardinality.make(col, low, up, 0, GlobalCardinality.Consistency.BC, solver));
        }
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
        //SearchMonitorFactory.log(solver, true, true);
        IPropagationEngine engine = solver.getEngine();
        engine.addGroup(Group.buildQueue(
                new MemberV<IntVar>(new HashSet<IntVar>(Arrays.asList(vars))), Policy.FIXPOINT
        ));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new LatinSquare().execute(args);
    }
}
