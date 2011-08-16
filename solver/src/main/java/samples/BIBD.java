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
import solver.constraints.nary.Sum;
import solver.constraints.nary.cnf.ALogicTree;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.constraints.nary.lex.LexChain;
import solver.constraints.ternary.Times;
import solver.propagation.engines.comparators.predicate.MemberV;
import solver.propagation.engines.comparators.predicate.Not;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;
import java.util.HashSet;

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
            //solver.post(Sum.eq(vars[i], R, solver));
        }
        // k ones per column
        IntVar K = VariableFactory.fixed(k, solver);
        for (int j = 0; j < b; j++) {
            solver.post(new Count(1, _vars[j], Count.Relop.EQ, K, solver));
            //solver.post(Sum.eq(_vars[j], K, solver));
        }

        // Exactly l ones in scalar product between two different rows
        IntVar L = VariableFactory.fixed(l, solver);
        for (int i1 = 0; i1 < v; i1++) {
            for (int i2 = i1 + 1; i2 < v; i2++) {
                BoolVar[] score = VariableFactory.boolArray(String.format("row(%d,%d)", i1, i2), b, solver);
                for (int j = 0; j < b; j++) {
                    //iff(score[j], vars[i1][j], vars[i2][j]);
                    solver.post(new Times(_vars[j][i1], _vars[j][i2], score[j], solver));
                }
                //solver.post(new Count(1, score, Count.Relop.EQ, L, solver));
                solver.post(Sum.eq(score, L, solver));
            }
        }
        // Symmetry breaking
        for (int i = 1; i < v; i++) {
            solver.post(new LexChain(false, solver, vars[i], vars[i - 1]));
        }
        for (int j = 1; j < b; j++) {
            solver.post(new LexChain(false, solver, _vars[j], _vars[j - 1]));
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

        // BEWARE: le nombre de propagation reste stable, ce qui change, c'est le temps d'exec.
        // etrangement, gecode effectue presque 2 fois moins de propagations...
        // les OCCURR peuvent tre remplacees par des SUM, mais plus lent (bien que nb prop < )
        HashSet<BoolVar> hs = new HashSet<BoolVar>(Arrays.asList(ArrayUtils.flatten(vars)));
        solver.getEngine().addGroup(
                Group.buildQueue(
                        new Not(new MemberV<BoolVar>(hs))
                ));
        solver.getEngine().addGroup(
                Group.buildQueue(
                        new MemberV<BoolVar>(hs)
                ));
        //EngineStrategyFactory.constraintOriented(solver);

    }

    @Override
    public void solve() {
//        SearchMonitorFactory.statEveryXXms(solver, 1000);
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("BIBD({},{},{},{},{})", new Object[]{v, b, r, k, l});
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == Boolean.TRUE) {
            for (int i = 0; i < v; i++) {
                st.append("\t");
                for (int j = 0; j < b; j++) {
                    st.append(_vars[j][i].getValue()).append(" ");
                }
                st.append("\n");
            }
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new BIBD().execute(args);
    }
}
