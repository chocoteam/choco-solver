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

import choco.kernel.common.util.tools.StringUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * CSPLib prob019:<br/>
 * "An order n magic square is a n by n matrix containing the numbers 1 to n^2, with each row,
 * column and main diagonal equal the same sum.
 * As well as finding magic squares, we are interested in the number of a given size that exist."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class MagicSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Magic square size.", required = false)
    int n = 5;

    @Option(name = "-c", usage = "Alldifferent consistency.", required = false)
    AllDifferent.Type type = AllDifferent.Type.BC;

    IntVar[] vars;

    @Override
    public void buildModel() {
        solver = new Solver();
        int ms = n * (n * n + 1) / 2;

        IntVar[][] matrix = new IntVar[n][n];
        IntVar[][] invMatrix = new IntVar[n][n];
        vars = new IntVar[n * n];

        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++, k++) {
                matrix[i][j] = VariableFactory.enumerated("square" + i + "," + j, 1, n * n, solver);
                vars[k] = matrix[i][j];
                invMatrix[j][i] = matrix[i][j];
            }
        }

        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            diag1[i] = matrix[i][i];
            diag2[i] = matrix[(n - 1) - i][i];
        }

        solver.post(new AllDifferent(vars, solver, type));

        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 1);
        for (int i = 0; i < n; i++) {
            solver.post(Sum.eq(matrix[i], coeffs, ms, solver));
            solver.post(Sum.eq(invMatrix[i], coeffs, ms, solver));
        }
        solver.post(Sum.eq(diag1, coeffs, ms, solver));
        solver.post(Sum.eq(diag2, coeffs, ms, solver));

        // Symetries breaking
        solver.post(ConstraintFactory.lt(matrix[0][n - 1], matrix[n - 1][0], solver));
        solver.post(ConstraintFactory.lt(matrix[0][0], matrix[n - 1][n - 1], solver));
        solver.post(ConstraintFactory.lt(matrix[0][0], matrix[n - 1][0], solver));

    }

    @Override
    public void configureSolver() {


        //solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));
        HeuristicValFactory.indomainMiddle(vars);
        solver.set(StrategyVarValAssign.dyn(vars,
                SorterFactory.minDomain(),
                ValidatorFactory.instanciated,
                solver.getEnvironment()));


        //TODO: choisir une meilleure stratŽgie
        // default group
        /*solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicates.light(),
                        new Seq(
                                IncrArityP.get(),
                                new IncrOrderV(vars)
                        ),
                        Policy.FIXPOINT
                ));*/
        if (n > 4) {
            long nl = (long) Math.pow(10, n);
            solver.getSearchLoop().getLimitsBox().setNodeLimit(nl);
        }
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
        String line = "+";
        for (int i = 0; i < n; i++) {
            line += "----+";
        }
        line += "\n";
        st.append(line);
        for (int i = 0; i < n; i++) {
            st.append("|");
            for (int j = 0; j < n; j++) {
                st.append(StringUtils.pad(vars[i * n + j].getValue() + "", -3, " ")).append(" |");
            }
            st.append(MessageFormat.format("\n{0}", line));
        }
        st.append("\n\n\n");
        LoggerFactory.getLogger("bench").info(st.toString());    }

    public static void main(String[] args) {
        new MagicSquare().execute(args);
    }
}
