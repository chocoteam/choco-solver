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
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.IntLinComb;
import solver.constraints.nary.Sum;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.ternary.Times;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.*;
import solver.propagation.engines.comparators.predicate.LeftHandSide;
import solver.propagation.engines.comparators.predicate.PriorityP;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;

import static solver.constraints.ConstraintFactory.lt;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Partition extends AbstractProblem {
    @Option(name = "-s", usage = "Partition size.", required = true)
    int size;

    IntVar[] vars;

    @Override
    public void buildModel() {
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
        solver.post(ConstraintFactory.scalar(xy, coeffs, IntLinComb.Operator.EQ, 0, solver));

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = VariableFactory.enumeratedArray("sx", size, 1, 4 * size * size, solver);
        sy = VariableFactory.enumeratedArray("sy", size, 1, 4 * size * size, solver);
        for (int i = size - 1; i >= 0; i--) {
            solver.post(new Times(x[i], x[i], sx[i], solver));
            sxy[i] = sx[i];
            solver.post(new Times(y[i], y[i], sy[i], solver));
            sxy[size + i] = sy[i];
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
        engine.addGroup(
                new Group(
                        new PriorityP(PropagatorPriority.TERNARY.priority),
                        new Cond(
                                new LeftHandSide(),
                                new IncrOrderV(vars),
                                new Decr(new IncrOrderV(vars))),
                        Policy.ITERATE
                ));
        solver.getEngine().setDefaultComparator(
                new Seq(
                        IncrArityP.get(),
                        new Decr(IncrDomDeg.get())
                )
        );

    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
        int sum1 = 0, sum2 = 0;
        int i = 0;
        st.append(vars[i].getValue());
        sum1 += vars[i].getValue();
        sum2 += vars[i].getValue() * vars[i++].getValue();
        for (; i < size; i++) {
            st.append(", ").append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i].getValue();
        }
        st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
        sum1 = sum2 = 0;
        st.append(vars[i].getValue());
        sum1 += vars[i].getValue();
        sum2 += vars[i].getValue() * vars[i++].getValue();
        for (; i < 2 * size; i++) {
            st.append(", ").append(vars[i].getValue());
            sum1 += vars[i].getValue();
            sum2 += vars[i].getValue() * vars[i].getValue();
        }
        st.append(": (").append(sum1).append(")~(").append(sum2).append(")\n");
        LoggerFactory.getLogger("bench").info(st.toString());
        st = null;
    }

    public static void main(String[] args) {
        new Partition().execute(args);
    }

}
