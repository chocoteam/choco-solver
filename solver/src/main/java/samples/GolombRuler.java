/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples;

import choco.kernel.ResolutionPolicy;
import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * CSPLib prob006:<br/>
 * A Golomb ruler may be defined as a set of m integers 0 = a_1 < a_2 < ... < a_m such that
 * the m(m-1)/2 differences a_j - a_i, 1 <= i < j <= m are distinct.
 * Such a ruler is said to contain m marks and is of length a_m.
 * <br/>
 * The objective is to find optimal (minimum length) or near optimal rulers.
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 revise model : remove views
 * @since 31/03/11
 */
public class GolombRuler extends AbstractProblem {

    @Option(name = "-m", usage = "Golomb ruler order.", required = false)
    private int m = 10;

    @Option(name = "-c", usage = "Alldifferent consistency.", required = false)
    AllDifferent.Type type = AllDifferent.Type.BC;

    IntVar[] ticks;
    IntVar[] diffs;
    IntVar[][] m_diffs;

    Constraint[] lex;
    Constraint alldiff;
    Constraint[] distances;

    @Override
    public void createSolver() {
        solver = new Solver("Golomb Ruler");
    }

    @Override
    public void buildModel() {
        ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);

        solver.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        lex = new Constraint[m - 1];
        for (int i = 0; i < m - 1; i++) {
            lex[i] = IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]);
        }
        solver.post(lex);


        diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        m_diffs = new IntVar[m][m];
        distances = new Constraint[(m * m - m) / 2];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                // <cpru 04/03/12> it is worth adding a constraint instead of a view
                distances[k] = Sum.eq(new IntVar[]{ticks[j], ticks[i], diffs[k]}, new int[]{1, -1, -1}, 0, solver);
                solver.post(distances[k]);
                solver.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                solver.post(Sum.leq(new IntVar[]{diffs[k], ticks[m - 1]}, new int[]{1, -1}, -((m - 1 - j + i) * (m - j + i)) / 2, solver));
                m_diffs[i][j] = diffs[k];
            }
        }
        alldiff = new AllDifferent(diffs, solver, type);
        solver.post(alldiff);

        // break symetries
        if (m > 2) {
            solver.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.inputOrderMinVal(ticks, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, (IntVar) solver.getVars()[m - 1]);
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new GolombRuler().execute(args);
    }
}
