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

import choco.kernel.common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.nary.Count;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.EngineStrategyFactory;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.propagation.engines.group.Group;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class BIBD extends AbstractProblem {

    @Option(name = "-v", usage = "matrix first dimension.", required = false)
    private int v = 7;

    @Option(name = "-k", usage = "ones per column.", required = false)
    private int k = 3;

    @Option(name = "-l", usage = "scalar product.", required = false)
    private int l = 60;

    @Option(name = "-b", usage = "matrix second dimension.", required = false)
    private int b = -1;

    @Option(name = "-r", usage = "ones per row.", required = false)
    private int r = -1;


    BoolVar[][] vars, _vars;

    @Override
    public void buildModel() {
        if (b == -1) {
            b = (v * (v - 1) * l) / (k * (k - 1));
        }
        if (r == -1) {
            r = (l * (v - 1)) / (k - 1);
        }

        solver = new Solver();
        vars = new BoolVar[v][b];
        _vars = new BoolVar[b][v];
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < b; j++) {
                vars[i][j] = VariableFactory.bool(String.format("V(%d,%d)", i, j), solver);
                _vars[j][i] = vars[i][j];
            }

        }
        // r ones per row
        IntVar R = VariableFactory.fixed(r, solver);
        for (int i = 0; i < v; i++) {
            solver.post(new Count(1, vars[i], Count.Relop.EQ, R, solver));
        }
        // k ones per column
        IntVar K = VariableFactory.fixed(k, solver);
        for (int j = 0; j < b; j++) {
            solver.post(new Count(1, _vars[j], Count.Relop.EQ, K, solver));
        }

        // Exactly l ones in scalar product between two different rows
        IntVar L = VariableFactory.fixed(l, solver);
        for (int i1 = 0; i1 < v - 1; i1++) {
            for (int i2 = i1 + 1; i2 < v; i2++) {
                BoolVar[] row = VariableFactory.boolArray(String.format("row(%d,%d)", i1, i2), b, solver);
                for (int j = 0; j < b; j++) {
                    iff(row[j], vars[i1][j], vars[i2][j]);
                }
                solver.post(new Count(1, row, Count.Relop.EQ, L, solver));
            }
        }
    }

    private void iff(BoolVar row, BoolVar row1, BoolVar row2) {
        ALogicTree tree = Node.ifOnlyIf(
                Node.and(Literal.pos(row1), Literal.pos(row2)),
                Literal.pos(row)
        );
        solver.post(new ConjunctiveNormalForm(tree, solver));
    }


    @Override
    public void configureSolver() {
        //TODO: changer la strategie pour une plus efficace
        solver.set(StrategyFactory.inputOrderMinVal(ArrayUtils.flatten(vars), solver.getEnvironment()));
        //solver.set(StrategyFactory.domwdegMindom(ArrayUtils.flatten(vars), solver));

        // TODO chercher un meilleur ordre de propagation
        solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicate.TRUE,
                        EngineStrategyFactory.comparator(solver, EngineStrategyFactory.ARITY_CSTR),
                        Policy.ITERATE
                ));

    }

    @Override
    public void solve() {
        SearchMonitorFactory.log(solver, false, false);
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("BIBD({},{},{},{},{})", new Object[]{v, b, r, k, l});
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < v; i++) {
            st.append("\t");
            for (int j = 0; j < b; j++) {
                st.append(vars[i][j].getValue()).append(" ");
            }
            st.append("\n");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new BIBD().execute(args);
    }
}
