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
import solver.constraints.nary.Sum;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Costas Arrays
 * "Given n in N, find an array s = [s_1, ..., s_n], such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the vectors v(i,j) = (j-i)x + (s_j-s_i)y are all different </li>
 * </ul>
 * <br/>
 * An array v satisfying these conditions is called a Costas array of size n;
 * the problem of finding such an array is the Costas Array problem of size n."
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 25/01/11
 */
public class CostasArrays extends AbstractProblem {

    @Option(name = "-o", usage = "Costas array size.", required = false)
    private static int n = 14;  // should be <15 to be solved quickly

    IntVar[] vars, vectors;

    @Override
    public void createSolver() {
        solver = new Solver("CostasArrays");
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.enumeratedArray("v", n, 0, n - 1, solver);
        vectors = new IntVar[n * n - n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    vectors[idx] = VariableFactory.offset(Sum.var(vars[j], VariableFactory.minus(vars[i])), 2 * n * (j - i));
                    idx++;
                }
            }
        }
        solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
        solver.post(IntConstraintFactory.alldifferent(vectors, "AC"));
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.force_InputOrder_InDomainMin(vars));
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
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "|";
            for (int j = 0; j < n; j++) {
                if (j == vars[i].getValue()) {
                    s += "x|";
                } else {
                    s += "-|";
                }
            }
            s += "\n";
        }
        System.out.println(s);
    }

    public static void main(String[] args) {
        new CostasArrays().execute(args);
    }
}
