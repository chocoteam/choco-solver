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

import choco.kernel.ResolutionPolicy;
import org.kohsuke.args4j.Option;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.unary.Relation;
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
 * CSPLib prob006:<br/>
 * A Golomb ruler may be defined as a set of m integers 0 = a_1 < a_2 < ... < a_m such that
 * the m(m-1)/2 differences a_j - a_i, 1 <= i < j <= m are distinct.
 * Such a ruler is said to contain m marks and is of length a_m.
 * <br/>
 * The objective is to find optimal (minimum length) or near optimal rulers.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class GolombRuler extends AbstractProblem {

    @Option(name = "-m", usage = "Golomb ruler order.", required = false)
    private int m = 10;

    @Option(name = "-c", usage = "Alldifferent consistency.", required = false)
    AllDifferent.Type type = AllDifferent.Type.BC;

    IntVar[] ticks;
    IntVar[] diffs;

    @Override
    public void buildModel() {
        solver = new Solver("Golomb Ruler " + m);

        ticks = new IntVar[m];
        for (int i = 0; i < ticks.length; i++) {
            ticks[i] = VariableFactory.enumerated("a_" + i, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        }

        solver.post(ConstraintFactory.eq(ticks[0], 0, solver));
        for (int i = 0; i < ticks.length - 1; i++) {
            solver.post(ConstraintFactory.lt(ticks[i], ticks[i + 1], solver));
        }

        diffs = new IntVar[(m * m - m) / 2];

        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                diffs[k] = VariableFactory.enumerated("d_" + k, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
                solver.post(Sum.eq(new IntVar[]{ticks[j], ticks[i], diffs[k]}, new int[]{1, -1, -1}, 0, solver));
                solver.post(new Relation(diffs[k], Relation.R.GQ, (j - i) * (j - i + 1) / 2, solver));
                solver.post(Sum.leq(new IntVar[]{diffs[k], ticks[m - 1]}, new int[]{1, -1}, -((m - 1 - j + i) * (m - j + i)) / 2, solver));
            }
        }
        solver.post(new AllDifferent(diffs, solver, type));

        // break symetries
        if (m > 2) {
            solver.post(ConstraintFactory.lt(diffs[0], diffs[diffs.length - 1], solver));
        }
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.inputOrderMinVal(ticks, solver.getEnvironment()));
        IPropagationEngine engine = solver.getEngine();
        engine.addGroup(Group.buildQueue(
                new MemberV<IntVar>(new HashSet<IntVar>(Arrays.asList(ticks))),
                        Policy.FIXPOINT
        ));
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
