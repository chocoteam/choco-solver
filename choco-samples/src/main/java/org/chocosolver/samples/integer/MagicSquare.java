/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;
import org.kohsuke.args4j.Option;

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

    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("Magic square");
    }

    @Override
    public void buildModel() {
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

        solver.post(IntConstraintFactory.alldifferent(vars, "BC"));

        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 1);
        for (int i = 0; i < n; i++) {
            solver.post(IntConstraintFactory.scalar(matrix[i], coeffs, "=", ms));
            solver.post(IntConstraintFactory.scalar(invMatrix[i], coeffs, "=", ms));
        }
        solver.post(IntConstraintFactory.scalar(diag1, coeffs, "=", ms));
        solver.post(IntConstraintFactory.scalar(diag2, coeffs, "=", ms));

        // Symetries breaking
        solver.post(IntConstraintFactory.arithm(matrix[0][n - 1], "<", matrix[n - 1][0]));
        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][n - 1]));
        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][0]));

    }

    @Override
    public void configureSearch() {
        solver.set(ISF.lexico_LB(vars));
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
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new MagicSquare().execute(args);
    }
}
