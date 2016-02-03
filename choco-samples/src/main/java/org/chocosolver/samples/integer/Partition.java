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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.kohsuke.args4j.Option;

import java.util.Arrays;

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
    int N = 2 * 32;

    IntVar[] vars;
    IntVar[] Ovars;

    Constraint[] heavy = new Constraint[3];

    @Override
    public void createSolver() {
        solver = new Solver("Partition " + N);
    }

    @Override
    public void buildModel() {
        int size = this.N / 2;
        IntVar[] x, y;
        x = solver.makeIntVarArray("x", size, 1, 2 * size, false);
        y = solver.makeIntVarArray("y", size, 1, 2 * size, false);

//        break symmetries
        for (int i = 0; i < size - 1; i++) {
            solver.post(IntConstraintFactory.arithm(x[i], "<", x[i + 1]));
            solver.post(IntConstraintFactory.arithm(y[i], "<", y[i + 1]));
        }
        solver.post(IntConstraintFactory.arithm(x[0], "<", y[0]));
        solver.post(IntConstraintFactory.arithm(x[0], "=", 1));

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
        heavy[0] = IntConstraintFactory.scalar(xy, coeffs, "=", 0);
        solver.post(heavy[0]);

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = solver.makeIntVar("x^", 0, x[i].getUB() * x[i].getUB(), true);
            sxy[i] = sx[i];
            sy[i] = solver.makeIntVar("y^", 0, y[i].getUB() * y[i].getUB(), true);
            sxy[size + i] = sy[i];
            solver.post(IntConstraintFactory.times(x[i], x[i], sx[i]));
            solver.post(IntConstraintFactory.times(y[i], y[i], sy[i]));
            solver.post(IntConstraintFactory.member(sx[i], 1, 4 * size * size));
            solver.post(IntConstraintFactory.member(sy[i], 1, 4 * size * size));
        }
        heavy[1] = IntConstraintFactory.scalar(sxy, coeffs, "=", 0);
        solver.post(heavy[1]);

        coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        solver.post(IntConstraintFactory.scalar(x, coeffs, "=", 2 * size * (2 * size + 1) / 4));
        solver.post(IntConstraintFactory.scalar(y, coeffs, "=", 2 * size * (2 * size + 1) / 4));
        solver.post(IntConstraintFactory.scalar(sx, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12));
        solver.post(IntConstraintFactory.scalar(sy, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12));

        heavy[2] = IntConstraintFactory.alldifferent(xy, "BC");
        solver.post(heavy[2]);

        vars = xy;
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(Ovars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
        if (ESat.TRUE == solver.isFeasible()) {
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
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Partition().execute(args);
    }

}
