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
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.unary.Relation;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.*;
import solver.propagation.engines.comparators.predicate.*;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;

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
    int N = 48;

    IntVar[] vars;

    @Override
    public void buildModel() {
        int size = this.N / 2;
        solver = new Solver();
        IntVar[] x, y;
        x = VariableFactory.enumeratedArray("x", size, 1, 2 * size, solver);
        y = VariableFactory.enumeratedArray("y", size, 1, 2 * size, solver);

        // break symmetries
        for (int i = 0; i < size - 1; i++) {
            solver.post(lt(x[i], x[i + 1], solver));
            solver.post(lt(y[i], y[i + 1], solver));
        }
        solver.post(lt(x[0], y[0], solver));

        IntVar[] xy = new IntVar[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            xy[i] = x[i];
            xy[size + i] = y[i];
        }

        int[] coeffs = new int[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            coeffs[i] = 1;
            coeffs[size + i] = -1;
        }
        solver.post(Sum.eq(xy, coeffs, 0, solver));

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = Views.sqr(x[i]);
            sxy[i] = sx[i];
            sy[i] = Views.sqr(y[i]);
            sxy[size + i] = sy[i];
            solver.post(new Relation(sx[i], Relation.R.GQ, 1, solver));
            solver.post(new Relation(sy[i], Relation.R.GQ, 1, solver));
            solver.post(new Relation(sx[i], Relation.R.LQ, 4 * size * size, solver));
            solver.post(new Relation(sy[i], Relation.R.LQ, 4 * size * size, solver));
        }
        solver.post(Sum.eq(sxy, coeffs, 0, solver));

        coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        solver.post(Sum.eq(x, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        solver.post(Sum.eq(y, coeffs, 2 * size * (2 * size + 1) / 4, solver));
        solver.post(Sum.eq(sx, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));
        solver.post(Sum.eq(sy, coeffs, 2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver));

        solver.post(new AllDifferent(xy, solver));

        vars = xy;
    }

    @Override
    public void configureSolver() {

        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));

        IPropagationEngine engine = solver.getEngine();
        Predicate light = Predicates.light();
        engine.addGroup(
                Group.buildGroup(
                        Predicates.priority_light(PropagatorPriority.TERNARY),
                        new Cond(
                                Predicates.lhs(),
                                new IncrOrderV(vars),
                                new Decr(new IncrOrderV(vars))),
                        Policy.ITERATE
                ));
        // set default
        engine.addGroup(
                Group.buildGroup(
                        light,
                        new Seq(
                                IncrArityP.get(),
                                new Decr(IncrDomDeg.get())
                        ),
                        Policy.FIXPOINT
                ));

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
        while (true) new Partition().execute(args);
    }

}
