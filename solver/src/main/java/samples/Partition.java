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
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.unary.Member;
import solver.propagation.generator.PArc;
import solver.propagation.generator.PCoarse;
import solver.propagation.generator.PCons;
import solver.propagation.generator.Sort;
import solver.propagation.generator.predicate.NotInCstrSet;
import solver.propagation.generator.predicate.Predicate;
import solver.propagation.generator.sorter.Increasing;
import solver.propagation.generator.sorter.Seq;
import solver.propagation.generator.sorter.evaluator.EvtRecEvaluators;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;

import static solver.constraints.ConstraintFactory.eq;
import static solver.constraints.ConstraintFactory.lt;

/**
 * CSPLib prob049:<br/>
 * "This problem consists in finding a partition of numbers 1..N into two sets A and B such that:
 * <ul>
 * <li>A and B have the same cardinality</li>
 * <li>sum of numbers in A = sum of numbers in B</li>
 * <li>sum of squares of numbers in A = sum of squares of numbers in B</li>
 * </ul>
 * <p/>
 * More constraints can thus be added, e.g also impose the equality on the sum of cubes.
 * There is no solution for N < 8."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Partition extends AbstractProblem {
    @Option(name = "-n", usage = "Partition size.", required = false)
    int N = 2 * 24;

    IntVar[] vars;
    IntVar[] Ovars;

    Constraint[] heavy = new Constraint[3];

    @Override
    public void buildModel() {
        int size = this.N / 2;
        solver = new Solver();
        IntVar[] x, y;
        x = VariableFactory.enumeratedArray("x", size, 1, 2 * size, solver);
        y = VariableFactory.enumeratedArray("y", size, 1, 2 * size, solver);
        Sum.incr = false;

//        break symmetries
        for (int i = 0; i < size - 1; i++) {
            solver.post(lt(x[i], x[i + 1], solver));
            solver.post(lt(y[i], y[i + 1], solver));
        }
        solver.post(lt(x[0], y[0], solver));
        solver.post(eq(x[0], 1, solver));

        IntVar[] xy = new IntVar[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            xy[i] = x[i];
            xy[size + i] = y[i];
        }

        Ovars = new IntVar[2 * size];
        for (int i = 0; i < size; i++) {
            Ovars[i * 2] = x[i];
            Ovars[i * 2 + 1] = y[i];
        }

        int[] coeffs = new int[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            coeffs[i] = 1;
            coeffs[size + i] = -1;
        }
        heavy[0] = Sum.eq(xy, coeffs, 0, solver);
        solver.post(heavy[0]);

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = Views.sqr(x[i]);
            sxy[i] = sx[i];
            sy[i] = Views.sqr(y[i]);
            sxy[size + i] = sy[i];
            solver.post(new Member(sx[i], 1, 4 * size * size, solver));
            solver.post(new Member(sy[i], 1, 4 * size * size, solver));
        }
        heavy[1] = Sum.eq(sxy, coeffs, 0, solver);
        solver.post(heavy[1]);

        coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        solver.post(Sum.eq(x, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        solver.post(Sum.eq(y, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        solver.post(Sum.eq(sx, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));
        solver.post(Sum.eq(sy, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));

        heavy[2] = new AllDifferent(xy, solver);
        solver.post(heavy[2]);

        vars = xy;
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.minDomMinVal(Ovars, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
        Sort ad1 = new Sort(
                new Seq(
                        new Increasing(EvtRecEvaluators.MinArityC),
                        new Increasing(EvtRecEvaluators.MinDomSize)
                ),
                new PArc(vars, new Predicate[]{new NotInCstrSet(heavy)}));
        Sort ad2 = new Sort(new PCons(heavy));
        Sort coar = new Sort(new PCoarse(heavy[2]));
        solver.set(new Sort(ad1.clearOut(), ad2.pickOne(), coar.pickOne()).clearOut());
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
        if (Boolean.TRUE == solver.isFeasible()) {
            int sum1 = 0, sum2 = 0;
            int i = 0;
            st.append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i++].getValue();
            for (; i < N / 2; i++) {
                st.append(", ").append(vars[i].getValue());
                sum1 += vars[i].getValue();
                sum2 += vars[i].getValue() * vars[i].getValue();
            }
            st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
            sum1 = sum2 = 0;
            st.append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i++].getValue();
            for (; i < N; i++) {
                st.append(", ").append(vars[i].getValue());
                sum1 += vars[i].getValue();
                sum2 += vars[i].getValue() * vars[i].getValue();
            }
            st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
        } else {
            st.append("INFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Partition().execute(args);
    }

}
